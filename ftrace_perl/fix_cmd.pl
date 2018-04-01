#!/usr/bin/perl -s
use strict;
use warnings;

# author: mtk04259
#   patch process cmd such as <...> by post-processing

use vars qw($h $c $b);

sub usage{

    if($h){
        print <<USAGE
Usage: $0 <input_file>
        fix process cmd in trace raw data
        -h: show usage
        -c: console mode, input from stdin and output to stdout
        -b: backup original version with .bak
USAGE
        ;
        exit 0;
    }

}

# trim space away
sub trim{
    (local $_) = @_;
    s/\s+//g;
    return $_;
}

sub main{
    my $input_file  = $_[0]||"SYS_FTRACE" ;
    my $output_file = $input_file;
    my %proc_table;
    my ($fout, @inputs);
    my $event_count = 0;

    if( -e $input_file and !defined $c){
        #warn "$0: input=$input_file, output=$output_file\n";
        open my $fin, '<', $input_file or die "$0: unable to open $input_file\n";
        @inputs = <$fin>;
        close $fin;
        open $fout, '>', $output_file or die "$0: unable to open $output_file\n";

        if($b){
            # backup original version
            open my $backup, '>', "$input_file.bak" or die "$0: unable to open $input_file.bak\n";
            print $backup @inputs;
            close $backup;
        }
    }else{
        warn "$0: $input_file not exist and read from stdin instead\n";
        @inputs = <STDIN>;
        open $fout, ">&=STDOUT" or die "$0: unable to alias STDOUT: $!\n"
    }

    for(@inputs){

        if(!m/^.{16}\-\d+\s*\[\d+\]\s+(?:\S{4}\s+)?\d+\.\d+\:/xso){
            next;
        }

        if(m/\s
            sched_switch\:\s+
            prev_comm=(.+)\s
            prev_pid=(\d+)\s
            prev_prio=\d+\s
            prev_state=\S+\s
            ==>\s
            next_comm=(.+)\s
            next_pid=(\d+)\s
            next_prio=\d+
            /xs)
        {

            $proc_table{$2}{name} = $1;
            $proc_table{$4}{name} = $3;

        }elsif(m/\s
            sched_wakeup(?:_new)?\:\s+
            comm=(.+)\s
            pid=(\d+)\s
            prio=\d+\s
            success=1\s
            target_cpu=\d+
            /xs)
        {

            $proc_table{$2}{name} = $1;
        }elsif(m/\s
            sched_migrate_task\:\s+
            comm=(.+)\s
            pid=(\d+)\s
            prio=\d+\s
            orig_cpu=\d+\s
            dest_cpu=\d+
            /xs)
        {
            $proc_table{$2}{name} = $1;
        }

    }

    for(@inputs){
        if(m/^\s{11}\<\.\.\.\>\-([\d\s]{5})/xso){
            my $pid = &trim($1);
            if(defined $proc_table{$pid}{name}){
                s/^\s{11}\<\.\.\.\>\-[\d\s]{5}/
                    sprintf("%16s-%-5d", $proc_table{$pid}{name}, $pid)/xse;
            }
        }
        print $fout "$_";
    }
    close $fout;
}

&usage();
&main(@ARGV);
