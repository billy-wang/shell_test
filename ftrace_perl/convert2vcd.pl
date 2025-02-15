#!/usr/bin/perl -s
# Copyright Statement:
# 
# This software/firmware and related documentation ("MediaTek Software") are
# protected under relevant copyright laws. The information contained herein is
# confidential and proprietary to MediaTek Inc. and/or its licensors. Without
# the prior written permission of MediaTek inc. and/or its licensors, any
# reproduction, modification, use or disclosure of MediaTek Software, and
# information contained herein, in whole or in part, shall be strictly
# prohibited.
# 
# MediaTek Inc. (C) 2010. All rights reserved.
# 
# BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
# THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
# RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
# ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
# WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
# WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
# NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
# RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
# INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
# TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
# RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
# OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
# SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
# RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
# STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
# ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
# RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
# MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
# CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
# 
# The following software/firmware and/or related documentation ("MediaTek
# Software") have been modified by MediaTek Inc. All revisions are subject to
# any receiver's applicable license agreements with MediaTek Inc.

# author: mtk04259
#   convert trace file to vcd for gtkwave visualization
#
# 2015/04/21    support nested interrupts handling
#               support ipi_raise event to track when ipi is sent
#
# 2014/11/18    find the first executing context at the beginning
#
# 2014/04/01    add irq_{entry,exit} events
#
# 2014/03/10    provide more info about ipi/irq events unpair problem
#               skip those unpair events automiatically
#
# 2013/07/26    adjust the ftrace trimming algorithm
#               once cpu_hotplug events are available, trim to the latest timestamp of the following events
#               1) CPU0's 1st event
#               2) if the 1st event of the other CPU is online event, the last offline timestamp
#               3) timestamp of 1st event if the 1st event of the other CPUs is not online event
#               
# 2013/06/24    reset all waves to sleep when ftrace is disabled
#               add cpu online/offline handling
#
# 2013/04/26    unhandled irq handler events handling
#
# 2013/02/25    redesign to handle irq/softirq events more smoothly
#               enqueue 'proc-$pid-$prio', 'irq-$irq', and 'softirq-$softirq' into exec_stack[$cpu] to track
#               which is executing now
#               record prio additionally from ftrace_cputime.pl to dispict
#
# 2013/02/22    support softirq events
#               softirq_raise as waking up softirq handler
#               softirq_entry/softirq_exit as entry and exit of the softirq handler
#
# 2013/02/19    detect nested interrupts
#               able to handle trace file with irq-info
#
# 2013/01/02    read from stdin and output to stdout if input file not exist
#               for the purpose of redirecting input/output
#
# 2012/12/07    align to the first CPU0 event
#               because of the ring-buffer architecture and CPUs but CPU0 will be hotplugged when system loading not heavy
#               Ftrace may keep events on CPUx long ago (except CPU0)
#               get rid of those events to avoid confusing
#               error handling for invalid input
# 
# 2012/10/23    enhanced with I/O waiting visualization with '-' yellow color
#               fine-tune the task migration between cores
#
# 2012/09/05    initial version   
#
# -- irq-info comment --
#                              _-----=> irqs-off
#                             / _----=> need-resched
#                            | / _---=> hardirq/softirq
#                            || / _--=> preempt-depth
#                            ||| /     delay
#         <idle>-0     [002] d..2 15696.184022: irq_handler_entry: irq=1 name=IPI_CPU_START
# 
# irqs-off: d for IRQS_OFF(I bit disabled), X for IRQS_NOSUPPORT, . otherwise
# need-resched: N for need_resched
# hardirq/softirq: H for TRACE_FLAG_HARDIRQ & TRACE_FLAG_SOFTIRQ both on, h for hardirq, s for softirq
# preempt-depth: preempt_count
# 
# save:  tracing_generic_entry_update() in kernel/trace/trace.c
# print: trace_print_context() in kernel/trace/trace_output.c

use strict;
use warnings;
use vars qw($h $c $d);

BEGIN {
    my $is_linux = $^O =~ /linux/;
    eval <<'USE_LIB' if($is_linux);
        use FindBin;
        use lib "$FindBin::Bin/lib/mediatek/";
USE_LIB
    eval 'use ftrace_parsing_utils;';
}

# enum irq types to sort them in the order of irq, ipi, and softirq
sub irq_type {
    local $_ = $_[0];
    if ($_ eq 'irq') {
        return 0;
    } elsif ($_ eq 'ipi') {
        return 1;
    } elsif ($_ eq 'softirq') {
        return 2;
    } else {
        return 3;
    }
}

my $version = "2015-04-22";

my (%proc_table,            # process table to track cpu it running on, process state, tgid, process name, 
    %irq_table,             # irq table to track irq number & irq name
    %ipi_id_table,          # ipi table to track ipi: name -> id
    %tag_table,             # the waves draw in vcd is described as tag. the hash key is 'proc-<pid>-<cpu>', irq-<irq#>-<cpu>, softirq-<irq#>-<cpu>
    @exec_stack,
    @irq_depth              # to track the irq depth. nested irq is not supported by arm
                            # so track the depth in parsing process to identify the abnormal behavior
);

# irq_table keys: 
# irq-<irq>-<cpu>, softirq-<softirq>-<cpu>, ipi-<ipi>-<cpu>

my $beginning_timestamp = -1; # the first recognized event timestamp

my @first_exec_context; # 1st executing context without starting event;
my $script_name = &_basename($0);

# fix process name of idle/init process
sub fix_cmd{
    (my $proc, my $pid) = @_;
    if(!defined($pid) || ($pid>1)){
        $proc =~ s/\W+/_/g;
        return $proc;
    }elsif($pid == 0){
        return "<idle>";
    }elsif($pid == 1){
        return "init";
    }
}

# determine the character for the process about to schedule out 
# by (1) its priority (2) its state
sub from_proc_char{
    my ($prio, $state) = @_;

    if(index($state, 'R') != -1){
        # schedule out due to preemption
        if($prio < 100){
            return 'L';
        }else{
            return '0'
        }
    }elsif(index($state, 'd') != -1){
        # schedule out due to I/O
        return '-';
    }else{
        # schedule out to sleep/mutex wait/exit
        if($prio < 100){
            return 'W';
        }else{
            return 'Z'
        }
    }

}

# determine the end character of the process about to schedule in
# by its priority
sub to_proc_char{
    local $_ = $_[0];

    if($_ < 100){
        return 'H';
    }else{
        return '1';
    }
}

# generate process tag for gtkwave
sub gentag{
    (local $_) = @_;
    my $ret;
    do{
        $ret .= chr(($_ % 93) +34);
        $_ = int($_ / 93);
    }while($_ > 0);
    return $ret;
}

# set irq & idle process to runnable
sub cpu_online_str{
    local $_;
    my ($cpu) = (@_);
    my $output;

    for(grep {
            m/^(?:irq\-\d+|proc\-0)\-$cpu$/xs
        }keys %tag_table)
    {
        $output .= sprintf "0%s\n", $tag_table{$_};
    }
    return $output;
}

sub cpu_offline_str{
    local $_;
    my ($cpu) = (@_);
    my $output;

    # reset exec_stack
    while(!&_exec_stack_empty($cpu)){
        pop @{$exec_stack[$cpu]};
    }

    # reset irq, softirq, & idle process
    for(grep {
            m/^(?:(?:soft)?irq\-\d+|proc\-0)\-$cpu$/s
        }keys %tag_table)
    {
            $output .= sprintf "Z%s\n", $tag_table{$_};
    }

    for(keys %proc_table){
        if(int($_) != 0 && exists $proc_table{$_}{cpu}
            && defined $proc_table{$_}{cpu} && 
            $cpu == $proc_table{$_}{cpu}
        ){

            if(exists $proc_table{$_}{state} and
                defined $proc_table{$_}{state} and 
                $proc_table{$_}{state} ne 'running')
            {
                $output .= sprintf "Z%s\n", $tag_table{"proc-$_-$proc_table{$_}{cpu}"};
                delete $proc_table{$_}{cpu};
                delete $proc_table{$_}{state};
            }
        }
    }
    return $output;
}

sub usage{
    print <<USAGE
Usage: $script_name <input_file> <output_file>
        convert ftrace into vcd format
        -h: show usage
        -c: console mode, input from stdin and output to stdout
        -d: debug mode, detect nested interrupt
USAGE
    ;
    exit 0;
}

# convert hex bitmask into integer array
sub cpumask_to_ids{
    local $_ ;
    my $cpumask = $_[0];
    my $cpu = 0;
    my @cpus;

    $cpumask =~ s|\,||g;
    $cpumask = hex("0x$cpumask");

    while($cpumask > 0){
        if($cpumask & 1){
            push @cpus, $cpu;
        }
        $cpumask >>= 1;
        ++$cpu;
    }
    return @cpus;
}

# ------------
# collect 1) task cmd, highest priority, and cpus 2)irq/softirq id and cpus
# ------------
sub collect_runtime_info{
    local $_;
    my $ref = $_[0];
    my $event_count;
    my $next_ipi = 0; # next ipi id to be used

    for(@{$ref}){
        chomp;
        my ($cpu, $pid, $tgid);

        if(m/^.{16}\-(\d+)\s+
                (?:\(([\s\d-]{5})\)\s+)?
                \[(\d+)\]\s+
                (?:\S{4}\s+)?
                (\d+\.\d+)\:/xso)
        {
            ($pid, $cpu) = (int($1), int($3));
            if($beginning_timestamp == -1){
                $beginning_timestamp = $4;
                $beginning_timestamp =~ s/\.//;
                $beginning_timestamp = int($beginning_timestamp) - 1000;
            }
            if(defined $2){
                $tgid = $2;
                if($tgid =~ m/^\s*\d+$/o){
                    $proc_table{$pid}{tgid} = int($tgid);
                }
            }

        }else{
            # skip un-recognized strings
            #print "skip: $_\n";
            next;
        }

        if(m/\s
            sched_switch\:\s+
            prev_comm=(.*)\s
            prev_pid=(\d+)\s
            prev_prio=(\d+)\s
            prev_state=\S+\s
            ==>\s
            next_comm=(.*)\s
            next_pid=(\d+)\s
            next_prio=(\d+)
            /xso)
        {
            $proc_table{$2}{name} = $1;
            $proc_table{$2}{prio} = ($proc_table{$2}{prio} && $proc_table{$2}{prio} < $3)?$proc_table{$2}{prio}:$3;
            $proc_table{$2}{cpus} |= (1<<$cpu);
            $proc_table{$5}{name} = $4;
            $proc_table{$5}{prio} = ($proc_table{$5}{prio} && $proc_table{$5}{prio} < $6)?$proc_table{$5}{prio}:$6;
            $proc_table{$5}{cpus} |= (1<<$cpu);
            $event_count++;

        }elsif(m/\s
            sched_wakeup(?:_new)?\:\s+
            comm=(.+)\s
            pid=(\d+)\s
            prio=(\d+)\s
            success=1\s
            target_cpu=(\d+)
            /xso)
        {
            $proc_table{$2}{name} = $1;
            $proc_table{$2}{prio} = ($proc_table{$2}{prio} && $proc_table{$2}{prio}<$3)?$proc_table{$2}{prio}:$3;
            $proc_table{$2}{cpus} |= (1<<$4);
            $event_count++;
        }elsif(m/\s
            sched_migrate_task\:\s+
            comm=(.+)\s
            pid=(\d+)\s
            prio=(\d+)\s
            orig_cpu=(\d+)\s
            dest_cpu=(\d+)
            /xso)
        {
            $proc_table{$2}{name} = $1;
            $proc_table{$2}{prio} = ($proc_table{$2}{prio} && $proc_table{$2}{prio}<$3)?$proc_table{$2}{prio}:$3;
            $proc_table{$2}{cpus} |= (1<<$4 | 1<<$5);
            $event_count++;
        }elsif(m/\s
            (?:irq_handler_entry|irq_entry)\:\s+
            irq=(\d+)\s
            name=(.+)
            /xso)
        {
            $irq_table{"irq-$1-$cpu"} = $2 if(!exists $irq_table{"$1-$cpu"});
            $event_count++;

        }elsif(m/\s
            (?:irq_handler_exit|irq_exit)\:\s+
            /xso)
        {
            $event_count++;
        }elsif(m/\s
            (?:softirq_entry|softirq_exit|softirq_raise)\:\s+
            vec=(\d+)\s
            \[action=(.+)\]/xso)
        {
            $irq_table{"softirq-$1-$cpu"} = $2 if (!exists $irq_table{"softirq-$1-$cpu"});
            $event_count++;
        }elsif(m/\s
            ipi_raise\:\s+
            target_mask=[\d,]*\s
            \([^)]*\)/xso)
        {
            $event_count++;
        }elsif(m/\s
            (?:ipi_entry|ipi_exit)\:\s+
            \(([^)]*)\)/xso)
        {
            if(!exists $ipi_id_table{"$1"}{id}){
                $ipi_id_table{"$1"}{id} = $next_ipi++;
            }
            $ipi_id_table{"$1"}{cpus} |= (1 << $cpu);

            my $ipi = $ipi_id_table{"$1"}{id};
            if (!exists $irq_table{"ipi-$ipi-$cpu"}) {
                $irq_table{"ipi-$ipi-$cpu"} = $ipi;
            }
            $event_count++;
        }
    }
    return $event_count;
}

# check if any process(irq, softirq...etc) executing on specific cpu
# true if no one executing
sub _exec_stack_empty{
    local $_ = $_[0];
    return (!defined $exec_stack[$_] or
        scalar(@{$exec_stack[$_]}) <= 0);
}

# convert proc-<pid>-<prio>, irq-<irq>, ipi-<ipi>, softirq-<softirq> into a list
sub _exec_stack_info{
    local $_ = $_[0];
    return ($_ =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
}

# find the executing irq/ipi/softirq/process at the beginning
# check if these events match in pairs
sub find_1st_exec_context {
    local $_;
    my $ref = $_[0];
    my $first_ts = $_[1];

    for(grep {
            m/\s(?:sched_switch |
            (?:soft)?irq_(?:entry|exit) |
            (?:irq|ipi)_handler_(?:entry|exit) |
            ipi_(?:entry|exit) |
            cpu_hotplug)\:/xso
        } reverse @{$ref})
    {
        chomp;
        my ($cpu, $pid, $tgid, $timestamp);

        if(m/^.{16}\-(\d+)\s+
                (?:\([\s\d-]{5}\)\s+)?
                \[(\d+)\]\s+
                (?:\S{4}\s+)?
                (\d+\.\d+)\:/xso)
        {
            ($pid, $cpu) = (int($1), int($2));
            $timestamp = $3;
            $timestamp =~ s/\.//go;
            $timestamp = int($timestamp);
        }else{
            # skip un-recognized strings
            next;
        }

        if(m/\s
            sched_switch\:\s+
            prev_comm=.*\s
            prev_pid=(\d+)\s
            prev_prio=(\d+)\s
            prev_state=\S+\s
            ==>\s
            next_comm=.*\s
            next_pid=(\d+)\s
            next_prio=(\d+)
            /xso)
        {
            my $pid = $1;
            my $prio = $2;
            my $next_pid = $3;
            my $next_prio = $4;

            if(!&_exec_stack_empty($cpu)){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                if($type eq "proc" and $id eq $next_pid)
                {
                    ${$exec_stack[$cpu]}[-1] = "proc-$pid-$prio";
                }else {
                    die "scheduling event not matched, ts=$timestamp (${$exec_stack[$cpu]}[-1]) on cpu$cpu & (proc-$next_pid-$next_prio)";
                }
            } else {
                push @{$exec_stack[$cpu]}, "proc-$pid-$prio";
            }
        }elsif(m/\s
            (?:irq_handler_entry|irq_entry)\:\s+
            irq=(\d+)\s
            name=.+
            /xso)
        {
            my $irq = $1;
            if(!&_exec_stack_empty($cpu)){
                if(${$exec_stack[$cpu]}[-1] eq "irq-$irq") {
                    pop @{$exec_stack[$cpu]};
                } else {
                    warn "irq entry/exit event not matched (${$exec_stack[$cpu]}[-1]) & (irq-$irq)";
                }
            }
        }elsif(m/\s
            (?:irq_handler_exit|irq_exit)\:\s+
            irq=(\d+)
            (?:\sret=handled)?
            /xso)
        {
            push @{$exec_stack[$cpu]}, "irq-$1";
        }elsif(m/\s
            ipi_entry\:\s+ \( ([^)]+) \)
            /xso)
        {
            my $ipi_name = $1;
            my $irq = $ipi_id_table{"$1"}{id};
            if (!&_exec_stack_empty($cpu)) {
                if (${$exec_stack[$cpu]}[-1] eq "ipi-$irq") {
                    pop @{$exec_stack[$cpu]};
                } else {
                    my ($type, $id) = &_exec_stack_info(${$exec_stack[$cpu]}[-1]);
                    my $name = &_find_id_name("ipi", $id, $cpu);
                    warn "ipi entry/exit event not matched (ipi-$name) & (ipi-$ipi_name)";
                }
            }
        }elsif(m/
            ipi_exit\:\s+ \( ([^)]*) \)
            /xso)
        {
            my $irq = $ipi_id_table{"$1"}{id};
            push @{$exec_stack[$cpu]}, "ipi-$irq";
        }elsif(m/\s
            softirq_entry\:\s+
            vec=(\d+)\s
            \[action=.+\]/xso)
        {
            my $softirq = $1;
            if(!&_exec_stack_empty($cpu)){
                if(${$exec_stack[$cpu]}[-1] eq "softirq-$softirq"){
                    pop @{$exec_stack[$cpu]};
                } else {
                    warn "irq entry/exit event not matched (${$exec_stack[$cpu]}[-1]) & (softirq-$softirq)";
                }
            }
        }elsif(m/\s
            softirq_exit\:\s+
            vec=(\d+)\s
            /xso)
        {
            push @{$exec_stack[$cpu]}, "softirq-$1";
        }elsif(m/\s
            cpu_hotplug\:\s
            cpu=\d+\s
            state=online\s
            last_offline_ts=(\d+)
            /xso)
        {
            my $last_offline_ts = $1;
            if($last_offline_ts < $first_ts){
                @{$exec_stack[$cpu]} = ();
            }
        }
    }
}

sub print_vcd_header{
    local $_;
    my ($fout, $first_ts) = ($_[0], $_[1]);
    my $i=0;

    printf $fout <<'HEADER', $version;
$version
    generated by convert2vcd ver %s
$end
$timescale 1us $end
$scope module sched_switch $end
HEADER
    for (sort {
            my ($atype, $airq, $acpu) = split /\-/, $a;
            my ($btype, $birq, $bcpu) = split /\-/, $b;
            if ($atype eq $btype) {
                return ($airq<=>$birq) || ($acpu<=>$bcpu);
            } else {
                return (&irq_type($atype) <=> &irq_type($btype))
            }
        } 
        keys %irq_table)
    {
        my ($type, $irq, $cpu) = split /\-/, $_;
        $tag_table{"$_"} = &gentag($i++);

        if ($type eq "irq") {
            printf $fout "\$var wire 1 %s 0-IRQ%s-%s[%03d]_nc=0 \$end\n", $tag_table{"$_"}, $irq, &fix_cmd($irq_table{$_}), $cpu;
        } elsif ($type eq "softirq") {
            printf $fout "\$var wire 1 %s 0-SOFTIRQ%s-%s[%03d]_nc=0 \$end\n", $tag_table{"$_"}, $irq, &fix_cmd($irq_table{$_}), $cpu;
        } elsif ($type eq "ipi") {
            printf $fout "\$var wire 1 %s 0-IPI-%s[%03d]_nc=0 \$end\n", $tag_table{"$_"}, &fix_cmd(&_find_id_name($type, $irq, $cpu)), $cpu;
        }
    }

    for(sort {$a <=> $b} keys %proc_table){
        my $j=0;
        while($proc_table{$_}{cpus}>>$j){
            if($proc_table{$_}{cpus} & (1<<$j)){
                $tag_table{"proc-$_-$j"} = &gentag($i++);
                printf $fout "\$var wire 1 %s %s%d-%s[%03d]_%s=%d \$end\n",
                    $tag_table{"proc-$_-$j"},
                    (exists $proc_table{$_}{tgid}?
                        "$proc_table{$_}{tgid}-":""),
                    $_,
                    &fix_cmd($proc_table{$_}{name}, $_),
                    $j,
                    ($proc_table{$_}{prio}<100?q(RT):q(nc)),
                    ($proc_table{$_}{prio}<100?
                        (99-$proc_table{$_}{prio}):
                        ($proc_table{$_}{prio}-120));
            }
            $j++;
        }
    }

    printf $fout <<'HEADER_END', $first_ts;
$upscope $end
$enddefinitions $end
#%d
HEADER_END

# initialize all processes

    my @executing_tasks_at_beginning = map {
            my ($type, $id) = &_exec_stack_info(${$exec_stack[$_]}[-1]);
            "$type-$id-$_";
        } grep {
            !&_exec_stack_empty($_)
        } 0 .. $#exec_stack;

    my %is_task_executing_at_beginning = 
        map { $_ => 1 } @executing_tasks_at_beginning;

    warn "executing context at the beginning:\n" if($d);

    for my $key (sort keys %tag_table){
        if($is_task_executing_at_beginning{$key}){
            my ($context, $id, $cpu) = split  /\-/, $key;

            if($context eq 'proc' and $proc_table{$id}{prio} <100) {
                # executing at the beginning with RT priority
                printf $fout "H%s\n", $tag_table{"$key"}
            }else {
                # executing at the beginning with normal priority
                printf $fout "1%s\n", $tag_table{"$key"}
            }

            if ($d){
                warn "context: $context, id: $id, cpu: $cpu\n";
            }
        }else {
            # initialized as sleep state
            printf $fout "Z%s\n", $tag_table{"$key"}
        }
    }
}

sub _draw_vcd_waves{
    local $_;
    my ($state, $tag_key, $state1, $tag_key1) = @_;
    if(defined $state && defined $tag_key && exists $tag_table{"$tag_key"}){
        $_ .= sprintf <<END_MARK, $state, $tag_table{"$tag_key"};
%s%s
END_MARK
    }
    if(defined $state1 && defined $tag_key1 && exists $tag_table{"$tag_key1"}){
        $_ .= sprintf <<END_MARK, $state1, $tag_table{"$tag_key1"};
%s%s
END_MARK
    }
    return $_;
}

# find id name with key from exec_stack
# _find_id_name(type, id, cpu)
sub _find_id_name{
    local $_;
    my ($type, $id, $cpu) = @_;
    my $name;

    if($type eq 'ipi') {
        for my $ipi_name (keys %ipi_id_table) {
            if ($ipi_id_table{$ipi_name}{id} == $id) {
                return $ipi_name;
            }
        }
    }elsif($type eq 'irq' || $type eq 'softirq'){
        $name = $irq_table{"$type-$id-$cpu"};
    }elsif($type eq 'proc'){
        $name = $proc_table{$id}{name};
    }
    return $name;
}

# ------------
# main parsing subroutine
# ------------
sub parse {
    local $_;
    my $tracing_on = 1;
    my @cpu_online;
        # table to track cpu online state
        # 0: offline, 1: online, undef: no trace events
    my ($fout, $ref) = @_;
    my $nested_irq_warn_already = 0;
    my $output;

    for (@{$ref}) {
        my ($curr_pid, $cpu, $timestamp);
        if((($curr_pid, $cpu, $timestamp) = m/^
                .{16}\-(\d+)\s+
                (?:\([\s\d-]{5}\)\s+)?
                \[(\d+)\]\s+
                (?:\S{4}\s+)?
                (\d+\.\d+)\:
                /xso)<3){
            # skip un-recognized strings
            next;
        }

        $timestamp =~ s/\.//go;
        $timestamp = int($timestamp);
        $cpu = int($cpu);

        if (!defined $cpu_online[$cpu]) {
            $cpu_online[$cpu] = 1;
        }
        
        # reset output string
        $output = undef;

# state characters used internally
#
# R:            running
# r:            runable
# w:            runable, and waked up from state not running or runnable
# m:            waiting for mutex
# d:            waiting for I/O
# s:            sleeping (including interruptible & uninterruptible state)
# align with ftrace_cputime.pl to represent waking up as 'w'
        if(m/\s
            sched_switch\:\s+
            prev_comm=.*\s
            prev_pid=(\d+)\s
            prev_prio=(\d+)\s
            prev_state=(\S+)\s
            ==>\s
            next_comm=.*\s
            next_pid=(\d+)\s
            next_prio=(\d+)
            (?:\sextra_prev_state=(\S+))?
            /xso)
        {
            my ($prev_pid, $prev_prio, $prev_state, $next_pid, $next_prio) =
            ($1, $2, ((defined $6)?"$3|$6":$3), $4, $5);

            if ($cpu_online[$cpu] == 0) {
                # since the target cpu is offline, skip it
                # (maybe that cpu is running out of ring buffer or offline)
                next;
            }

            $output .= "#$timestamp\n";

            if($next_pid != 0 && defined($proc_table{$next_pid}{cpu}) &&
                ($proc_table{$next_pid}{cpu} ne $cpu) && defined($proc_table{$next_pid}{state}) &&
                (($proc_table{$next_pid}{state} eq 'waking' || $proc_table{$next_pid}{state} eq 'runable' || $proc_table{$next_pid}{state} eq 'io') ||
                    $next_prio < 100 # only RT process need to be reset to 'Z'
                )
            ){
                # handle migration
                # note: IDLE process DOESN'T migrate!!
                $output .= &_draw_vcd_waves("Z", "proc-$next_pid-$proc_table{$next_pid}{cpu}");
            }

            if(index($prev_state, 'R') != -1){
                $proc_table{$prev_pid}{state} = 'runable';
                #runable, but not running state
            }elsif(index($prev_state, 'd') != -1){
                $proc_table{$prev_pid}{state} = 'io';
                # d, i/o wait
            }else{
                $proc_table{$prev_pid}{state} = 'sleep'; # restored to "not assigned to cpu" state
            }
            $proc_table{$prev_pid}{cpu} = $cpu;
            $proc_table{$next_pid}{state} = 'running';   # running
            $proc_table{$next_pid}{cpu} = $cpu;

            # check which context we are in
            if(!&_exec_stack_empty($cpu)){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);

                if($type eq 'proc' and $id != $prev_pid){
                    # should not parse anymore because certain ftrace events seems to be lost
                    die "$script_name: scheduling event not matched, ts=$timestamp, proc_to_schedule_out=[$prev_pid:$proc_table{$prev_pid}{name}], proc_in_exec=[$id:$proc_table{$id}{name}]\n";
                }elsif($type ne 'proc'){
                    # since softirq priority > process, the softirq_exit event must be lost
                    warn "$script_name: proc [$next_pid:$proc_table{$next_pid}] schedule-in in $type $id context or $type event lost\n";
                    pop @{$exec_stack[$cpu]};
                }else{
                    # warn "un-recognized exec_stack: ${$exec_stack[$cpu]}[-1]\n";
                    # or pass...
                    pop @{$exec_stack[$cpu]};
                }
            }
            push @{$exec_stack[$cpu]}, "proc-$next_pid-$next_prio";

            $output .= &_draw_vcd_waves(
                &from_proc_char($prev_prio, $prev_state),"proc-$prev_pid-$cpu",
                &to_proc_char($next_prio), "proc-$next_pid-$cpu");
            print $fout $output if(defined $output);
        }elsif(m/\s
            sched_wakeup(?:_new)?\:\s+
            comm=.+\s
            pid=(\d+)\s
            prio=(\d+)\s
            success=1\s
            target_cpu=(\d+)
            (?:\sstate=(\S+))?
            /xso)
        {
            my ($pid, $prio, $target_cpu) = ($1, $2, int($3));
            my $do_print = 0;

            if(!defined $cpu_online[$target_cpu] || $cpu_online[$target_cpu] == 0){
                # since the target cpu is not active, skip it
                # (maybe that cpu is running out of ring buffer or offline)
                next;
            }

            $output .= "#$timestamp\n";

            # waked up on another cpu, we have to make it sleep on current cpu
            if(exists($proc_table{$pid}{cpu}) && ($proc_table{$pid}{cpu} ne $target_cpu) && 
                exists($proc_table{$pid}{state}) && 
                ($proc_table{$pid}{state} eq 'waking' || $proc_table{$pid}{state} eq 'runable' || $proc_table{$pid}{state} eq 'io' || $prio < 100)){
                # for process suddenly migrated to another core
                $output .= &_draw_vcd_waves("Z", "proc-$pid-$proc_table{$pid}{cpu}");
                $do_print = 1;
            }

            # waked up, give it the waking up string
            if(!exists $proc_table{$pid}{state} ||
                ($proc_table{$pid}{state} ne 'running' &&
                    $proc_table{$pid}{state} ne 'runable' &&
                    $proc_table{$pid}{state} ne 'waking')
                ){
                # only wake up tasks that are not running/runable/waking, otherwise this waking event is meaningless
                $proc_table{$pid}{state} = 'waking'; # waked up
                $proc_table{$pid}{cpu} = $target_cpu;
                $output .= &_draw_vcd_waves("X", "proc-$pid-$target_cpu");
                $do_print = 1;
            }
            print $fout $output if($do_print);

        }elsif(m/\s
            sched_migrate_task\:\s+
            comm=.+\s
            pid=(\d+)\s
            prio=(\d+)\s
            orig_cpu=(\d+)\s
            dest_cpu=(\d+)
            (?:\sstate=(\S+))?
            /xso)
        {
            my ($pid, $prio, $orig_cpu, $dest_cpu, $state) = ($1, $2, $3, $4, $5);
            $orig_cpu = int($orig_cpu);
            $dest_cpu = int($dest_cpu);

            if(!defined $cpu_online[$dest_cpu] || $cpu_online[$dest_cpu] == 0){
                # since the target cpu is not active, skip it
                # (maybe that cpu is running out of ring buffer or offline)
                next;
            }
            if(($orig_cpu != $dest_cpu) &&
                (defined($proc_table{$pid}{state}) || $prio < 100)){

                # end the line on the original cpu
                $output .= "#$timestamp\n" . &_draw_vcd_waves("Z", "proc-$pid-$orig_cpu");;
                if(defined $proc_table{$pid}{cpu} and ($proc_table{$pid}{cpu} ne $orig_cpu)){
                    $output .= &_draw_vcd_waves("Z", "proc-$pid-$proc_table{$pid}{cpu}");
                }

                if(defined($proc_table{$pid}{state})){
                    if($proc_table{$pid}{state} eq 'runable'){
                        $output .= &_draw_vcd_waves($prio<100?"L":"0", "proc-$pid-$dest_cpu");
                    }elsif($proc_table{$pid}{state} eq 'waking'){
                        $output .= &_draw_vcd_waves("X", "proc-$pid-$dest_cpu");
                    }elsif($proc_table{$pid}{state} eq 'io'){
                        $output .= &_draw_vcd_waves("-", "proc-$pid-$dest_cpu");
                    }elsif($proc_table{$pid}{state} eq 'running'){
                        # migrate during running, which is weird
                        $output .= &_draw_vcd_waves($prio<100?"H":"1", "proc-$pid-$dest_cpu");
                    }else{
                        $output .= &_draw_vcd_waves($prio<100?"W":"Z", "proc-$pid-$dest_cpu");
                    }
                }
            }
            $proc_table{$pid}{cpu} = int($dest_cpu);
            print $fout $output if(defined $output);

        }elsif(m/\s (?:
            (?:irq_handler_entry|irq_entry)\:\s+ irq=(\d+)\s name=(.+) |
            ipi_entry\:\s+ \( ([^)]+) \) |
            softirq_entry\:\s vec=(\d+)\s \[action=.+\]
            )/xso)
        {
            no warnings 'uninitialized';

            my $irq = $1;
            my $irq_name = $3;
            my $event_type = "irq";
            my $softirq = $4;

            if (defined $irq) {
                $irq_name = $2;
            } elsif(defined $irq_name) {
                $irq = $ipi_id_table{"$irq_name"}{id};
                $event_type = "ipi";
            } elsif(defined $softirq) {
                $irq = $softirq;
                $irq_name = $irq_table{"softirq-$softirq-$cpu"};
                $event_type = "softirq";
            }
 
            my ($type, $id, $prio);

            if ($d) {
                if ($irq_depth[$cpu]>0 && $nested_irq_warn_already == 0) {
                    my $last_irq = (&find_last_event($exec_stack[$cpu], qr!^(?:irq|ipi)\-!o) =~ m/^(?:irq|ipi)\-(\d+)/o);
                    $nested_irq_warn_already++;
                    warn "$script_name: nested irq handler detected, ts=$timestamp cpu=$cpu irq=[$irq:$irq_name] in_irq=@{[ $last_irq || 'unknown' ]}";
                }
                $irq_depth[$cpu]++;
            }

            $output .= "#$timestamp\n";
            $output .= _draw_vcd_waves("1", "$event_type-$irq-$cpu");

            if(!&_exec_stack_empty($cpu)){
                ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
            }
            $output .= _draw_vcd_waves(&from_proc_char((defined($prio)?$prio:'120'), 'R'),
                "@{[defined($type)?$type:'proc']}-@{[defined($id)?$id:$curr_pid]}-$cpu");
            push @{$exec_stack[$cpu]}, "$event_type-$irq";

            print $fout $output if(defined $output);

        }elsif(m/\s (?:
            (?:irq_handler_exit|irq_exit)\:\s+ irq=(\d+) (?:\sret=handled)? |
            ipi_exit\:\s+ \( ([^)]*) \) |
            softirq_exit\:\s vec=(\d+)\s \[action=.+\]
            )/xso)
        {

            my ($irq, $irq_name, $softirq) = ($1, $2, $3);
            my $event_type = "irq";
            # to find ipi name faster

            if(defined $irq){
                $irq_name = $irq_table{"irq-$irq-$cpu"};
            }elsif(defined $irq_name){
                # ipi case
                $irq = $ipi_id_table{"$irq_name"}{id};
                $event_type = "ipi";
            }elsif(defined $softirq){
                $irq = $softirq;
                $irq_name = $irq_table{"softirq-$softirq-$cpu"};
                $event_type = "softirq";
            }

            if($d) {
                $irq_depth[$cpu]-- if(defined($irq_depth[$cpu]) && $irq_depth[$cpu] > 0);
            }

            $output .= "#$timestamp\n";
            if(!&_exec_stack_empty($cpu)){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                my $name = &_find_id_name($type, $id, $cpu);

                # if($type eq 'irq' and ($id != $irq or $event_type eq 'ipi'))
                if($type ne $event_type or $id != $irq){
                    warn qq($script_name: irq entry/exit event not matched, maybe irq event lost, ts=$timestamp, ${event_type}_to_exit=[$irq:$irq_name]", in_${type}=[$id:$name]\n);
                    pop @{$exec_stack[$cpu]};
                    $output .= &_draw_vcd_waves("0", "$type-$irq-$cpu");
                }elsif($type eq 'proc'){
                    warn qq($script_name: irq entry event lost, ts=$timestamp, ${event_type}_to_exit=[$irq:$irq_name], ${type}_in_exec=[$id:$name]\n);
                    next;
                }else{
                    # warn "un-recognized exec_stack: ${$exec_stack[$cpu]}[-1]\n";
                    # or pass...
                    pop @{$exec_stack[$cpu]};
                }
            }

            #$output .= &_draw_vcd_waves(($event_type eq "softirq"?"Z":"0"), "$event_type-$irq-$cpu");
            $output .= &_draw_vcd_waves("Z", "$event_type-$irq-$cpu");

            if(!&_exec_stack_empty($cpu)){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                if(!defined $id) {
                    $id = $curr_pid;
                }
                if(!defined $prio && $type ne 'proc'){
                    $prio = 120;
                }
                $output .= &_draw_vcd_waves(&to_proc_char($prio), "$type-$id-$cpu");
            }
            print $fout $output if(defined $output);
        }elsif(m/\s (?:
            softirq_raise\:\s vec=(\d+)\s \[action=.+\] |
            ipi_raise\:\s+ target_mask=([\d,]+)\s+ \( ([^)]*) \)
            )/xso)
        {
            my $irq = $1;
            my ($cpumask, $ipi_name) = ($2, $3);
            my $event_type = "softirq";
            
            $output .= "#$timestamp\n";
            if (defined $cpumask) {
                $event_type = "ipi";
                $irq = $ipi_id_table{"$ipi_name"}{id};
                for my $id (&cpumask_to_ids($cpumask)) {
                    $output .= &_draw_vcd_waves("X", "$event_type-$irq-$id");
                }
            } else {
                $output .= &_draw_vcd_waves("X", "$event_type-$irq-$cpu");
            }
            print $fout $output if(defined $output);

        }elsif(m/\s
            tracing_on\:\s
            ftrace\s is\s ((?:dis|en)abled)
            /oxs)
        {
            my $enabled = $1;

            if($enabled eq 'disabled' && $tracing_on == 1){
                $tracing_on = 0;
                $output .= "#$timestamp\n";
                for(my $j = 0; $j <= $#exec_stack ; ++$j){
                    $output .= &cpu_offline_str($j);
                }
            }elsif($enabled eq 'enabled' && $tracing_on == 0){
                $tracing_on = 1;
                $output .= "#$timestamp\n";

                for(my $j = 0; $j <= $#exec_stack ; ++$j){
                    $output .= &cpu_online_str($j);
                }
            }
            print $fout $output if(defined $output);
        }elsif(m/\s
            cpu_hotplug\:\s
            cpu=(\d+)\s
            state=(\w+)
            /oxs)
        {
            my $state = $2;
            my $cpu_id = int($1);

            if($state eq 'online'){
                $cpu_online[$cpu_id] = 1;
                $output = sprintf <<"CPU_ONLINE", &cpu_online_str($cpu_id);
#$timestamp
%s
CPU_ONLINE
            }elsif($state eq 'offline'){
                $cpu_online[$cpu_id] = 0;
                $output = sprintf <<"CPU_OFFLINE", &cpu_offline_str($cpu_id);
#$timestamp
%s
CPU_OFFLINE
            }
            print $fout $output if(defined $output);
        }elsif(m/\s
            unnamed_irq\:\s
            irq=(\d+)
            /oxs)
        {
            warn "$script_name: unknown irq=$1, ts=$timestamp\n";
        }
    }
}


# tag_table index: proc-<pid>-<cpu>, irq-<id>-<cpu>, softirq-<id>-<cpu>
sub main{
    my ($input_file, $output_file) = ($_[0]||"SYS_FTRACE", $_[1]||"trace.vcd");
    my ($fout, @inputs);
    my $event_count = 0;

    &usage() if($h);

    if(-e $input_file and !defined $c){
        warn "$script_name: input=$input_file, output=$output_file\n";
        open my $fin, '<', $input_file or die "$script_name: unable to open $input_file\n";
        @inputs = grep {!m/^\s*\#/o} <$fin>;
        close $fin;
        open $fout, '>', $output_file or die "$script_name: unable to open $output_file\n";
    }else{
        warn "$script_name: $input_file not exist and read from stdin instead\n";
        @inputs = @{ &trim_events(<STDIN>) };
        open $fout, ">&=STDOUT" or die "$script_name: unable to alias STDOUT: $!\n"
    }

    $event_count = &collect_runtime_info(\@inputs);
    if($event_count == 0){
        die "$script_name: no recognized events, exit\n";
    }
    if($beginning_timestamp == -1) {
        die "$script_name: unable to find first timestamp\n";
    }

    &find_1st_exec_context(\@inputs, $beginning_timestamp);
    &print_vcd_header($fout, $beginning_timestamp);
    &parse($fout, \@inputs);
    close $fout;
}

&main(@ARGV);
