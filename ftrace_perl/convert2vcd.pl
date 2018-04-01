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
# 2012/09/05    first version   
#
# -- irq-info comment --
#                              _-----=> irqs-off
#                             / _----=> need-resched
#                            | / _---=> hardirq/softirq
#                            || / _--=> preempt-depth
#                            ||| /     delay
#         <idle>-0     [002] d..2 15696.184022: irq_handler_entry: irq=1 name=IPI_CPU_START
# 
# irqs-off: d for IRQS_OFF, X for IRQS_NOSUPPORT, . otherwise
# need-resched: N for need_resched
# hardirq/softirq: H for TRACE_FLAG_HARDIRQ & TRACE_FLAG_SOFTIRQ both on, h for hardirq, s for softirq
# preempt-depth: preempt_count
# 
# save:  tracing_generic_entry_update() in kernel/trace/trace.c
# print: trace_print_context() in kernel/trace/trace_output.c

use strict;
use warnings;
use vars qw($h $c);

my $version = "2013-09-30";
my (%proc_table, %irq_table, %tag_table, $first_timestamp, %softirq_table, @exec_stack, @irq_count);
my $tracing_on_regex = qr/\s
            tracing_on\:\s
            ftrace\sis\s
            /xs;

# fix process name of idle/init process
sub fix_cmd{
    (my $proc, my $pid) = @_;
    if(!defined($pid) || ($pid>1)){
        $proc =~ s/\W+/_/g;
        #tr/]/_/s;
        #tr/./-/s;
        #tr/ /*/s;
        #tr/:/;/s;
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

sub find_last_event{
    my ($ref, $regex) = @_;
    local $_;

    if(!defined($ref) || scalar(@{$ref}) == 0){
        return '';
    }

    for(reverse @{$ref}){
        if($_ =~ $regex){
            return $_;
        }
    }
    return '';
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
    while(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]})>0){
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
Usage: $0 <input_file> <output_file>
        convert ftrace into vcd format
        -h: show usage
        -c: console mode, input from stdin and output to stdout
USAGE
        ;
    exit 0;

}

sub _split_event{
    use bigint;
    local $_ = $_[0];
    my ($pid, $cpu, $ts) = m/^
                .{16}\-(\d+)\s*
                \[(\d+)\]
                \s+(?:\S{4}\s+)?
                (\d+\.\d+)\:
                /xso;
    $ts =~ s/\.//go;
    return ($pid, $cpu, int($ts));
}

sub _max{
    use bigint;
    local $_;
    return (sort {$a <=> $b} grep {defined $_ } @_)[-1];
}

sub _binary_search_ts{
    my ($ref, $ts, $first) = @_;
    my ($last, $index) = ($#{$ref}, 0);
    local $_;

    if(!defined $first){
        $first = 0;
    }

    while($first < $last){
        use bigint;
        $index = int(($first + $last)/2);
        my $index_ts = (&_split_event(${$ref}[$index]))[-1];

        if($ts == $index_ts){
            return $index;
        }elsif ($ts < $index_ts){
            $last = $index-1;
        }else{
            $first = $index+1;
        }
    }

    return $first;
}

# remove events that before cpu0
# detect if this is trace file in old format
# detect if any lost events in ftrace, which may cause parser confused
# delete unhandled irq events
sub filter_events{
    local $_;
    my (@events, @cpu_event_index, $splice_index, $event_count);
    my $cpu_hotplug_event_support = 0;
    $splice_index = 0;

    # filter comment & events not match our patterns out
    for(@_){
        if(m/^\#\senabled\s events\: .*
            \:\bcpu_hotplug\b/xso){
            $cpu_hotplug_event_support = 1;
        }elsif(/^\#/o){
            ; # skip comments
        }elsif(m/^.{16}\-\d+\s*\[(\d+)\]/xso){
            my $cpu = int($1);
            # the parsing will start from first cpu0 event
            if(defined $cpu_event_index[0] || $cpu == 0){
                push (@events, $_);
                $event_count++;
                if(!defined $cpu_event_index[$cpu]){
                    $cpu_event_index[$cpu] = $#events ;
                }
            }
        }

        if(m/^\#\stracer\:\ssched_switch\b/xso){
            warn "$0: sched_switch tracer is not supported to convert with this script\n";
            exit 0;
        }
        if(/\blost\s.+\sevents\b/io){
            warn "$0: some events lost in raw trace data, the generated vcd may be incorrect\n";
        }
    }

    if(!defined $event_count){
        die "$0: no events available\n";
    }

    # if hotplug event support, transform the first index of each cpu
    if($cpu_hotplug_event_support){
        my @cpu_events_ts = map{
            if( m/\s
                cpu_hotplug\:\s
                cpu=\d+\s
                state=online\s
                last_offline_ts=(\d+)
            /xso){
                $1;
            }else{
                (&_split_event($_))[-1];
            }
           } @events[@cpu_event_index];

           #warn "[".join(",", @cpu_events_ts)."], cpu0=$cpu_event_index[0]\n";
        my $pivot_ts = &_max(@cpu_events_ts);
        $splice_index = &_binary_search_ts(\@events, $pivot_ts, $cpu_event_index[0]);
        if((&_split_event($events[$splice_index]))[-1] < $pivot_ts){
            $splice_index++;
        }

        #@{$cpu_events{index}} = map{
        #    if($_ == 0){
        #        # cpu0 should be never offline
        #        $cpu_events{index}[0];
        #    }elsif($events[$cpu_events{index}[$_]] =~ m/\s
        #        cpu_hotplug\:\s
        #        cpu=\d+\s
        #        state=online
        #        /xso)
        #    {
        #        # first event is the cpu_online events
        #        # no need to splice to the first event on this cpu
        #        $cpu_events{index}[0];
        #    }else{
        #        $cpu_events{index}[$_];
        #    }
        #} 1 .. $#{$cpu_events{index}};
    }

    if($splice_index){
        splice @events, 0, $splice_index;
    }

    # filter out unhandled irq events
    my @output_events;
    my @irq_unhandled ;
    for(reverse @events){
        my $cpu = undef; 
        if(m/^.{16}\-\d+\s*\[(\d+)\]/xso){
            $cpu = int($1);
        }

        if(m/\s
            irq_handler_exit\:\s+
            irq=(\d+)\s
            ret=unhandled
            /xso)
        {
            if(defined $cpu){
                $irq_unhandled[$cpu] = 1;
            }
        }elsif(defined $cpu              and 
            defined $irq_unhandled[$cpu] and
            $irq_unhandled[$cpu] == 1 and
            m/\s
            irq_handler_entry\:\s+
            irq=(\d+)\s
            name=(.+)
            /xso)
        {
            $irq_unhandled[$cpu] = 0;
        }else{
            unshift @output_events, $_ ;
        }
    }

    my @tracing_off_index = grep {$output_events[$_] =~ m/${tracing_on_regex}disabled/ } 0 .. $#output_events;
    if(scalar(@tracing_off_index) > 0){
        if($tracing_off_index[-1] == $#output_events){
            splice @output_events, $tracing_off_index[-1];
        }else{
            my $last_ts = (&_split_event($output_events[-1]))[-1];
            my $last_hotplug_ts = (&_split_event($output_events[$tracing_off_index[-1]]))[-1];
            # if the ts that tracing_on toggled to 0 and the last ts differ within 1ms, remove these events
            if(($last_ts - $last_hotplug_ts) < 1000){
                splice @output_events, $tracing_off_index[-1];
            }
            #warn "last_ts=$last_ts hotplug_ts=$last_hotplug_ts\n";
            #if($output_events[$tracing_off_index[-1]] =~ m/${tracing_on_regex}0/){
            #    splice @output_events, $tracing_off_index[-1];
            #}
        }
    }

    # ignore the first event if it is "toggled to 1" event
    if($output_events[0] =~ m/\s
            tracing_on\:\s
            ftrace\s is\s enabled
        /xso)
    {
        shift @output_events;
    }

    return \@output_events;
}

# ------------
# collect 1) task cmd, highest priority, and cpus 2)irq/softirq id and cpus
# ------------
sub collect_runtime_info{
    local $_;
    my $ref = $_[0];
    my $event_count;

    for(@{$ref}){
        chomp;
        my $cpu;

        if(m/^.{16}\-\d+\s*\[(\d+)\]\s+(?:\S{4}\s+)?(\d+\.\d+)\:/xso){
            $cpu = int($1);
            if(!defined($first_timestamp) and $cpu == 0){
                $first_timestamp = $2;
                $first_timestamp =~ s/\.//;
                $first_timestamp = int($first_timestamp) - 1000;
            }
        }else{
            # skip un-recognized strings
            #print "skip: $_\n";
            next;
        }

        if(m/\s
            sched_switch\:\s+
            prev_comm=(.+)\s
            prev_pid=(\d+)\s
            prev_prio=(\d+)\s
            prev_state=\S+\s
            ==>\s
            next_comm=(.+)\s
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
            (?:irq|ipi)_handler_entry\:\s+
            (?:irq|ipi)=(\d+)\s
            name=(.+)
            /xso)
        {
            $irq_table{"$1-$cpu"} = $2;
            $event_count++;

        }elsif(m/\s
            (?:irq|ipi)_handler_exit\:\s+
            /xso){
            $event_count++;
        }elsif(m/\s
            softirq_raise\:\s+
            /xso){
            $event_count ++;
        }elsif(m/\s
            softirq_entry\:\s+
            vec=(\d+)\s
            \[action=(.+)\]/xso){

            $softirq_table{"$1-$cpu"} = $2;
            $event_count++;
        }elsif(m/\s
            softirq_exit\:\s+
            /xso){
            $event_count++;
        }
    }
    return $event_count;
}

sub print_vcd_header{
    local $_;
    my $fout = $_[0];
    my $i=0;

    printf $fout <<'HEADER', $version;
$version
    generated by convert2vcd ver %s
$end
$timescale 1us $end
$scope module sched_switch $end
HEADER
    for (sort {
        my ($airq, $acpu) = split /\-/, $a;
        my ($birq, $bcpu) = split /\-/, $b;
        return ($airq<=>$birq) || ($acpu<=>$bcpu);
        } 
        keys %irq_table)
    {
        my ($irq, $cpu) = split /\-/, $_;
        $tag_table{"irq-$_"} = &gentag($i++);
        printf $fout "\$var wire 1 %s 0-IRQ%s-%s[%03d]_nc=0 \$end\n", $tag_table{"irq-$_"}, $irq, &fix_cmd($irq_table{$_}), $cpu;
    }

    for (sort {
        my ($airq, $acpu) = split /\-/, $a;
        my ($birq, $bcpu) = split /\-/, $b;
        return ($airq<=>$birq) || ($acpu<=>$bcpu);
        } 
        keys %softirq_table)
    {
        my ($softirq, $cpu) = split /\-/, $_;
        $tag_table{"softirq-$_"} = &gentag($i++);
        printf $fout "\$var wire 1 %s 0-SOFTIRQ%s-%s[%03d]_nc=0 \$end\n", $tag_table{"softirq-$_"}, $softirq, &fix_cmd($softirq_table{$_}), $cpu;
    }

    for(sort {$a <=> $b} keys %proc_table){
        my $j=0;
        while($proc_table{$_}{cpus}>>$j){
            if($proc_table{$_}{cpus} & (1<<$j)){
                #$proc_table{$_}{"tag-$j"} = &gentag($i++);
                $tag_table{"proc-$_-$j"} = &gentag($i++);
                if($proc_table{$_}{prio} <100){
                    printf $fout "\$var wire 1 %s %d-%s[%03d]_RT=%d \$end\n",
                    $tag_table{"proc-$_-$j"},
                    $_,
                    &fix_cmd($proc_table{$_}{name}, $_),
                    $j,
                    99-$proc_table{$_}{prio};
                }else{
                    printf $fout "\$var wire 1 %s %d-%s[%03d]_nc=%d \$end\n",
                    $tag_table{"proc-$_-$j"},
                    $_,
                    &fix_cmd($proc_table{$_}{name}, $_),
                    $j,
                    $proc_table{$_}{prio}-120;
                }
                #print "$_: $proc_table{$_}{name}, $proc_table{$_}{prio}, $proc_table{$_}{\"tag-$j\"}";
                #printf ", [%d]\n", $j;
            }
            $j++;
        }
    }

    printf $fout <<'HEADER_END', $first_timestamp;
$upscope $end
$enddefinitions $end
#%d
HEADER_END
# initialize all processes
    for(my $j = 0; $j < $i; ++$j){
        print $fout "Z@{[&gentag($j)]}\n";
    }
}

# ------------
# main parsing subroutine
# ------------
sub parse {
    local $_;
    my $tracing_on = 1;
    my @cpu_online;
    my ($fout, $ref) = @_;
    my $nested_irq_warn_already = 0;

    for(@{$ref}){
        my ($orig_pid, $cpu, $timestamp);
        if((($orig_pid, $cpu, $timestamp) = m/^
                .{16}\-(\d+)\s*
                \[(\d+)\]\s+(?:\S{4}\s+)?
                (\d+\.\d+)\:
                /xso)<3){
            # skip un-recognized strings
            next;
        }

        $timestamp =~ s/\.//go;
        $timestamp = int($timestamp);
        $cpu = int($cpu);
        $cpu_online[$cpu] = 1;

# state characters used internally
#
# R:            running
# r:            runable
# w:            runable, and waked up from state not running or runnable
# m:            waiting for mutex
# d:            waiting for I/O
# s:            sleeping (including interruptible & uninterruptible state)
# (align with ftrace_cputime.pl to represent waking up as 'w'
        if(m/\s
            sched_switch\:\s+
            prev_comm=.+\s
            prev_pid=(\d+)\s
            prev_prio=(\d+)\s
            prev_state=(\S+)\s
            ==>\s
            next_comm=.+\s
            next_pid=(\d+)\s
            next_prio=(\d+)
            (?:\sextra_prev_state=(\S+))?
            /xso)
        {
            my ($prev_pid, $prev_prio, $prev_state, $next_pid, $next_prio) =
            ($1, $2, ((defined $6)?"$3|$6":$3), $4, $5);

            print $fout "#$timestamp\n";

            if($next_pid != 0 && defined($proc_table{$next_pid}{cpu}) &&
                ($proc_table{$next_pid}{cpu} ne $cpu) && defined($proc_table{$next_pid}{state}) &&
                (($proc_table{$next_pid}{state} eq 'waking' || $proc_table{$next_pid}{state} eq 'runable' || $proc_table{$next_pid}{state} eq 'io') ||
                    $next_prio < 100)
            ){
                # handle migration
                # note: IDLE process DOESN'T migrate!!
                printf $fout "%s%s\n",
                # $next_prio<100?"W":"Z",
                "Z",
                $tag_table{"proc-$next_pid-$proc_table{$next_pid}{cpu}"};
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

            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                if($type ne 'proc' or $id != $prev_pid){
                    # should not parse anymore because it seems certain ftrace events lost
                    die "$0: scheduling event not matched, ts=$timestamp, pid_to_schedule_out=$prev_pid, pid_in_exec=$id\n";
                }else{
                    pop @{$exec_stack[$cpu]};
                }
            }
            push @{$exec_stack[$cpu]}, "proc-$next_pid-$next_prio";

            printf $fout <<SCHED_SWITCH, &from_proc_char($prev_prio, $prev_state), $tag_table{"proc-$prev_pid-$cpu"}, to_proc_char($next_prio), $tag_table{"proc-$next_pid-$cpu"};
%s%s
%s%s
SCHED_SWITCH

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
            my $output;

            if(!defined $cpu_online[$target_cpu] || $cpu_online[$target_cpu] == 0){
                # since the target cpu is not active, skip it
                # (maybe that cpu is running out of ring buffer or offline)
                next;
            }

            $output = sprintf <<SCHED_WAKEUP;
#$timestamp
SCHED_WAKEUP

            # waked up on another cpu, we have to make it sleep on current cpu
            if(exists($proc_table{$pid}{cpu}) && ($proc_table{$pid}{cpu} ne $target_cpu) && 
                exists($proc_table{$pid}{state}) && 
                ($proc_table{$pid}{state} eq 'waking' || $proc_table{$pid}{state} eq 'runable' || $proc_table{$pid}{state} eq 'io' || $prio < 100)){
                # for process suddenly migrated to another core
                $output .= sprintf "Z%s\n", $tag_table{"proc-$pid-$proc_table{$pid}{cpu}"};
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
                $output .= sprintf "X%s\n", $tag_table{"proc-$pid-$target_cpu"};
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
                printf $fout <<SCHED_MIGRATE_TASK, $tag_table{"proc-$pid-$orig_cpu"};
#$timestamp
Z%s
SCHED_MIGRATE_TASK
                if(defined $proc_table{$pid}{cpu} and ($proc_table{$pid}{cpu} ne $orig_cpu)){
                    print $fout "Z".$tag_table{"proc-$pid-$proc_table{$pid}{cpu}"}."\n";
                }

                if(defined($proc_table{$pid}{state})){
                    if($proc_table{$pid}{state} eq 'runable'){
                        if($prio < 100){
                            printf $fout "L%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                        }else{
                            printf $fout "0%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                        }
                    }elsif($proc_table{$pid}{state} eq 'waking'){
                        printf $fout "X%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                    }elsif($proc_table{$pid}{state} eq 'io'){
                        printf $fout "-%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                    }elsif($proc_table{$pid}{state} eq 'running'){
                        # migrate during running, which is weird
                        if($prio < 100){
                            printf $fout "H%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                        }else{
                            printf $fout "1%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                        }
                    }else{
                        if($prio < 100){
                            printf $fout "W%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                        }else{
                            printf $fout "Z%s\n", $tag_table{"proc-$pid-$dest_cpu"};
                        }
                    }
                }
            }
            $proc_table{$pid}{cpu} = int($dest_cpu);
        }elsif(m/\s
            (?:irq|ipi)_handler_entry\:\s+
            (?:irq|ipi)=(\d+)\s
            name=(.+)
            /xso){
            no warnings 'uninitialized';

            my $irq = $1;
            my ($type, $id, $prio);

            if ($irq_count[$cpu]>0 && $nested_irq_warn_already == 0){
                my $last_irq = (&find_last_event($exec_stack[$cpu], qr!^irq\-!o) =~ m/^irq\-(\d+)/o);
                $nested_irq_warn_already++;
                die "$0: nested irq handler detected, ts=$timestamp cpu=$cpu irq=$irq-$2 in_irq=@{[$last_irq || 'unknown']}";
            }
            $irq_count[$cpu]++;

            printf $fout <<IRQ_HANDLER_ENTRY, $tag_table{"irq-$irq-$cpu"};
#$timestamp
1%s
IRQ_HANDLER_ENTRY

            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
            }
            printf $fout <<IRQ_HANDLER_ENTRY, &from_proc_char((defined($prio)?$prio:'120'), 'R'), $tag_table{"@{[defined($type)?$type:'proc']}-@{[defined($id)?$id:$orig_pid]}-$cpu"};
%s%s
IRQ_HANDLER_ENTRY
            push @{$exec_stack[$cpu]}, "irq-$irq";

        }elsif(m/\s
            (?:irq|ipi)_handler_exit\:\s+
            (?:irq|ipi)=(\d+)
            (?:\sret=handled)?
            /xso){

            my $irq = $1;
            $irq_count[$cpu]-- if(defined($irq_count[$cpu]) && $irq_count[$cpu] > 0);

            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                if($type ne 'irq' or $id != $irq){
                    # should not parse anymore because it seems certain ftrace events lost
                    die "$0: irq entry/exit event not matched, ts=$timestamp, irq_to_exit=$irq, in_irq=$id\n";
                }else{
                    pop @{$exec_stack[$cpu]} ;
                }
            }

            printf $fout <<IRQ_HANDLER_EXIT, $tag_table{"irq-$irq-$cpu"};
#$timestamp
0%s
IRQ_HANDLER_EXIT
            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                if(!defined $prio && $type eq 'softirq'){
                    $prio = 120;
                }
                printf $fout <<IRQ_HANDLER_EXIT, &to_proc_char($prio), $tag_table{"$type-$id-$cpu"};
%s%s
IRQ_HANDLER_EXIT
            }
        }elsif(m/\s
            softirq_raise\:\s
            vec=(\d+)\s
            \[action=.+\]
            /xso){
            printf $fout <<SOFTIRQ_RAISE, $tag_table{"softirq-$1-$cpu"};
#$timestamp
X%s
SOFTIRQ_RAISE
        }elsif(m/\s
            softirq_entry\:\s
            vec=(\d+)\s
            \[action=.+\]
            /xso){

            my $softirq = $1;
            my ($type, $id, $prio);

            printf $fout <<SOFTIRQ_ENTRY, $tag_table{"softirq-$softirq-$cpu"};
#$timestamp
1%s
SOFTIRQ_ENTRY

            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
            }
            printf $fout <<SOFTIRQ_ENTRY, &from_proc_char((defined($prio)?$prio:'120'), 'R'), $tag_table{"@{[defined($type)?$type:'proc']}-@{[defined($id)?$id:$orig_pid]}-$cpu"};
%s%s
SOFTIRQ_ENTRY
            push @{$exec_stack[$cpu]}, "softirq-$softirq";
        }elsif(m/\s
            softirq_exit\:\s
            vec=(\d+)\s
            \[action=.+\]
            /xso){

            my $softirq = $1;
            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                if($type ne 'softirq' or $id != $softirq){
                    # should not parse anymore because it seems certain ftrace events lost
                    die "$0: softirq entry/exit event not matched, ts=$timestamp, softirq_to_exit: $softirq, in_softirq=$id\n";
                }else{
                    pop @{$exec_stack[$cpu]} ;
                }
            }

            printf $fout <<SOFTIRQ_EXIT, $tag_table{"softirq-$softirq-$cpu"};
#$timestamp
Z%s
SOFTIRQ_EXIT
            if(defined $exec_stack[$cpu] and scalar(@{$exec_stack[$cpu]}) > 0){
                my ($type, $id, $prio) = (${$exec_stack[$cpu]}[-1] =~ m/^(\w+)\-(\d+)(?:\-(\d+))?$/o);
                #@print "timestamp = $timestamp, last exec=${$exec_stack[$cpu]}[-1]\n";
                printf $fout <<SOFTIRQ_EXIT, to_proc_char(($type eq 'softirq')?120:$prio), $tag_table{"$type-@{[defined($id)?$id:$orig_pid]}-$cpu"};
%s%s
SOFTIRQ_EXIT
            }
        }elsif(m/\s
            tracing_on\:\s
            ftrace\s is\s ((?:dis|en)abled)$
            /oxs)
        {
            my $enabled = $1;
            my $output;

            if($enabled eq 'disabled' && $tracing_on == 1){
                $tracing_on = 0;
                $output = sprintf <<FTRACE_DISABLED;
#$timestamp
FTRACE_DISABLED
                for(my $j = 0; $j <= $#exec_stack ; ++$j){
                    $output .= &cpu_offline_str($j);
                }
            }elsif($enabled eq 'enabled' && $tracing_on == 0){
                $tracing_on = 1;
                $output = sprintf <<FTRACE_ENABLED;
#$timestamp
FTRACE_ENABLED

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
            my $output;
            my $cpu_id = int($1);

            if($state eq 'online'){
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
            warn "$0: unknown irq=$1, ts=$timestamp\n";
        }

    }
}

# tag_table index: proc-<pid>-<cpu>, irq-<id>-<cpu>, softirq-<id>-<cpu>
# exec_stack value: proc-<pid>-<prio>, irq-<irq>, softirq-<irq>
sub main{
    my ($input_file, $output_file) = ($_[0]||"SYS_FTRACE", $_[1]||"trace.vcd");
    my ($fout, @inputs);
    my $event_count = 0;

    if(-e $input_file and !defined $c){
        warn "$0: input=$input_file, output=$output_file\n";
        open my $fin, '<', $input_file or die "$0: unable to open $input_file\n";
        @inputs = @{ &filter_events(<$fin>) };
        close $fin;
        open $fout, '>', $output_file or die "$0: unable to open $output_file\n";
    }else{
        warn "$0: $input_file not exist and read from stdin instead\n";
        @inputs = @{ &filter_events(<STDIN>) };
        open $fout, ">&=STDOUT" or die "$0: unable to alias STDOUT: $!\n"
    }

    $event_count = &collect_runtime_info(\@inputs);
    if($event_count == 0){
        die "$0: no recognized events, exit\n";
    }
    &print_vcd_header($fout);
    &parse($fout, \@inputs);

    close $fout;
}

if($h){
    &usage();
}else{
    &main(@ARGV);
}
