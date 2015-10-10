#!/usr/bin/perl

# Small script used to remove unnessesary new lines from pdf-links csv file.

use strict;
use warnings;
use utf8;
use Text::CSV;
use Text::Unidecode;
use File::Copy;

my $csv = Text::CSV->new( { binary => 1 } );

open( my $input, "<", "pdf-links.csv" ) or die $!;
open( my $output, ">", "pdf-links.tmp") or die $!;

while ( my $row = $csv->getline($input) ) {
    for (@$row) {
        #remove linefeeds in each 'element'. 
        s/\n/ /g;
        #remove repeated spaces in each 'element'
        s/ +/ /g;
        #fix shitty unicode characters fucking up my day
        $_ =~ s/([^[:ascii:]]+)/unidecode($1)/ge;
        #print this specific element WITH QUOTES 
        print $output "\"$_\"";
        print $output ",";
    }
    print $output "\n";
}
close($input);
close($output);
copy "pdf-links.tmp", "pdf-links.csv" or die "Copy Failed: $!";
unlink "pdf-links.tmp";