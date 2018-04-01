#!/usr/bin/perl -s

# author: mtk04259
#   check ftrace usability
#
# 2014/12/08    initial release

use strict;
use warnings;
use IO::Uncompress::Gunzip qw(gunzip $GunzipError);
use IO::File;

sub check_adb{
    local $_ = `adb devices`;
    if(!m/(\S+)\s+device\b/xsgo){
        die "check_adb: device not available, please check if adb connected\n";
    }else{
        return $1;
    }
}

sub _is_debugfs_mounted{
    local $_ = `adb shell mount`;
    if (m/\bdebugfs\s(\S+)\sdebugfs\b/xsgo){
        return $1;
    }else{
        return undef;
    }
}

sub check_software_version{
    local $_;
    my $props = `adb shell getprop`;
    my ($build_type, $project, $branch_label);

    ($build_type) = ($props =~ m/\[ro\.build\.type\]\:\s*\[(.*)\]/o);
    ($project) = ($props =~ m/\[ro\.build\.product\]\:\s*\[(.*)\]/o);
    ($branch_label) = ($props =~ m/\[ro.mediatek\.version\.release\]\:\s*\[(.*)\]/o);

    print "check_software_version: build_type: $build_type, project: $project, branch version: $branch_label\n";
}

sub check_mount{
    local $_ = &_is_debugfs_mounted();

    if (defined $_){
        print "check_mount: debugfs mounted in $_\n";
    } else {
        print "check_mount: debugfs not mounted, try to mount in /sys/kernel/debug...\n";
        system("adb shell \"mount -t debugfs debugfs /sys/kernel/debug\"");
        if(!defined(&_is_debugfs_mounted())){
            die "check_mount: unable to mount debugfs\n";
        }
    }
}

sub check_file_exist{
    local $_ = `adb shell ls $_[0]`;
    
    if(m/\bNo such file or directory\b/o){
        return undef;
    }else{
        return 1;
    }
}

sub check_kernel_config{
    local $_;
    my $config;

    if(&check_file_exist("/proc/config.gz")) {
        system('adb pull /proc/config.gz');
        my $input = new IO::File "<config.gz"
            or die "cannot open config.gz\n";
        gunzip "config.gz" => \$config
            or die "gunzip failed $GunzipError\n";
        if($config =~ m/CONFIG_EVENT_TRACING=y/o){
            print "CONFIG_EVENT_TRACING=y in kernel\n";
        }else{
            die "CONFIG_EVENT_TRACING not set, please enable ftrace kernel options\n";
        }
    } else {
        print "check_kernel_config: /proc/config.gz not available\n";
        print "check_kernel_config: enable CONFIG_IKCONFIG to check kernel options at run-time\n";
    }
}

sub check_selinux{
    local $_;
    my $selinux_status = `adb shell getenforce`;
    $selinux_status =~ s#[\s\r\n]+##g;

    if($selinux_status =~ m/^Enforcing$/o){
        my $disable_enforcing = `adb shell setenforce 0`;
        if($disable_enforcing =~ m/Could not set enforcing status\:\s*\(.*\)/io){
            print "check_selinux: unable to disable selinux: $1\n";
        } else {
            print "check_selinux: change enforcing to permissive\n";
        }
    }
    print "check_selinux: selinux=$selinux_status\n";
}

# check if file writable
# file: target file on device
# value: value to write to that file
# regex: regular expression to match its content
# func: function to transform original value to write back
sub _writable_check{
    local $_;
    my ($file, $value, $regex, $func) = @_;
    my $escaped_file = $file;

    my $orig_value = `adb shell cat $file`;
    if(defined $func and ref($func) eq 'CODE'){
        $orig_value = &${func}($orig_value);
    } else {
        $orig_value =~ s/[\n\r]+$//o;
    }
    print "writable_check: $file orig_value='$orig_value', try to write '$value'\n";

    # to convert the path in double quote
    $escaped_file  =~ s/([\\|\/])/\\$1/g;

    #print "command=adb shell 'echo $value > $file'\n";
    system("adb shell \"echo $value \> $escaped_file \"");
    $_ = `adb shell cat $file`;
    # print "_writable_check: write back '$orig_value' to $file, ret=$_\n";
    if(!m/$regex/){
        system("adb shell \"echo $orig_value > $escaped_file \"");
        return undef;
    }else{
        system("adb shell \"echo $orig_value > $escaped_file \"");
        return 1;
    }
}

sub check_tracing{
    local $_;

    if(!&_writable_check('/sys/kernel/debug/tracing/buffer_size_kb', '1',
            qr/1[\n\r]*/,
            sub{
                local $_ = $_[0];
                if(s/^(\d+).*/$1/go){
                    return $1;
                } else {
                    return '4096';
                }
            }))
    {
        die "check_tracing: unable to change buffer_size\n";
    }
    if(!&_writable_check('/sys/kernel/debug/tracing/set_event', 'sched_switch',
            qr/sched\:sched_switch[\n\r]*/, sub {local $_ = $_[0]; s/[\s\n\r]+/ /go; return $_;}))
    {
        die "check_tracing: unable to change set_event\n";
    }
    if(!&_writable_check('/sys/kernel/debug/tracing/tracing_on', '0', qr/0[\n\r]*/)){
        die "check_tracing: unable to stop tracing\n";
    }
}

sub main{
    local $_;

    my $tracing_on = "/sys/kernel/debug/tracing/tracing_on";

    my $serial = &check_adb();
    &check_software_version();
    &check_kernel_config();
    &check_mount();
    if (!defined(&check_file_exist($tracing_on))){
        die "check_tracing_on: $tracing_on is not available, ftrace may be not built in kernel\n";
    }
    &check_selinux();
    &check_tracing();

    print "tracing_checker: all pass, ftrace is ok\n";
    unlink("config.gz") if(-e "config.gz");
    exit 0;
}

&main();
