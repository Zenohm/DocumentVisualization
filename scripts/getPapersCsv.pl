#!/usr/bin/perl
use strict;
use warnings;
use Text::CSV;
use File::Basename;
use File::Fetch;
use File::Copy;
use File::Spec;

my $USAGE = "USAGE: getPapersCsv.pl inputFile [outputDirectory [documentDatabase]]
inputFile         - CSV containing pdf locations and metadata.
outputDirectory   - Directory to output the pdfs to
documentDatabase  - The location to write the CSV that contains the downloaded PDFs and metadata.\n";

my $base_url = "http://research.google.com";

if(!@ARGV) { print $USAGE; die "Cannot run without an input file"; }
if (scalar(@ARGV) < 1) { print $USAGE; die "Cannot run without an input file"; }

# Get the input and the output files
my $inputFile = $ARGV[0];
my $outputDirectory = "pdf_downloads/";
my $documentDatabaseName = "docDb.txt";
if(defined($ARGV[1]))
{
	$outputDirectory = $ARGV[1];
    if(substr($outputDirectory,-1) ne "/")
    {
        $outputDirectory .= "/";
    }
}
if(defined($ARGV[2]))
{
    $documentDatabaseName = $ARGV[2];
}

# Create the directory if it doesnt exist already
mkdir $outputDirectory unless -d $outputDirectory;

open my $fh, "<", $inputFile or die "Cannot open $inputFile, $!";
open my $ddb, ">", $documentDatabaseName 
    or die "Cannot Open Document Database, $documentDatabaseName, $!";

my $csv = Text::CSV->new({binary => 1}) or die "Cannot use CSV: ".Text::CSV->error_diag();

# Get the column names and get the CSV data
$csv->column_names($csv->getline($fh)) or die "Error with CSV, ".$csv->error_diag();
my $csv_data = $csv->getline_hr_all($fh);
close $fh;

my $count = 0;
my $read_count = 0;
my @column_names = qw(file title author conference);
$csv->print($ddb, \@column_names);
print $ddb "\n";

for my $doc (@{$csv_data})
{
    if($doc->{link})
    {
        my $resource_url = $base_url.$doc->{link};
        my $filename = $outputDirectory.basename($doc->{link});
        if(! -e $filename)
        {
            print "Downloading $resource_url to $outputDirectory...";
            my $ff = File::Fetch->new(uri => $resource_url);
            $ff->fetch(to => $outputDirectory);
            my @csvLine = (File::Spec->rel2abs($filename), $doc->{title}, $doc->{authors}, $doc->{conference});
            $csv->print($ddb, \@csvLine);
            print $ddb "\n";
            print "DONE\n";
        } else
        {
            print "File from $resource_url has already been downloaded at $filename\n";    
        }
        $count++;
    }
    $read_count++;
}
print "There are $count downloadable documents out of $read_count\n";
close $ddb;