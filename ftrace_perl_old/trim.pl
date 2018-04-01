#!/usr/bin/perl -s
use strict;
use warnings;


# trim space away
sub trim{
    (local $_) = @_;
    s/\s+//g;
    return $_;
}

sub main{
    my $input_file  = $_[0]||"SYS_FTRACE";
    my $output_file = $input_file;
    my $start_time = 0;
    my $end_time = 0;
    my $filter_time = $_[1];

	open (FILE, $input_file) or die "$!";
	my @arr=<FILE>;;
	close FILE;
	
	for(@arr){
    	if(m/^.*?\[.*?\].*?(\d+)\.\d+\:.*?/){
    		$start_time = $1;
    		last;
    	}
    }
	
	if($arr[$#arr] =~ m/^.*?\[.*?\].*?(\d+)\.\d+\:.*?/){
		$end_time = $1;
	}

#	print "$start_time\n";
#	print "$end_time\n";

	if($end_time > $start_time + ($filter_time * 2)){
		open my $fout, '>', $output_file or die "$0: unable to open $output_file\n";
    	for(@arr){
    		if(m/^.*?\[.*?\].*?(\d+)\.\d+\:.*?/){
    			if ($1 > ($start_time + $filter_time) && $1 < ($end_time - $filter_time)){
    				print $fout "$_";
    			}
    		}
    		else{
    			print $fout "$_";
    		}        
		}
    	close $fout;
    }
    else{
    	print "Log period < 20s, don't trim it."
    }
}

&main(@ARGV);