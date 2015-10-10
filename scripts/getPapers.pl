#!/usr/bin/perl

use strict;
use warnings;
use WWW::Mechanize;
use File::Fetch;

my $url = "http://research.google.com/pubs/ArtificialIntelligenceandMachineLearning.html";
my $static_base_url = "http://research.google.com";
my $directory = "pdf_downloads/";

my $mech = WWW::Mechanize->new();

$mech->get($url);

my @links = $mech->links();

my $count = 0;
for my $link (@links)
{
	if($link->url() =~ /.pdf$/ && $link->url() =~ /^\/pubs/)
	{
		my $resource_url = $static_base_url.$link->url();
		print "Downloading ".$resource_url."...";
		my $ff = File::Fetch->new(uri => $resource_url);
		$ff->fetch(to => $directory);
		print "DONE\n"
		
	}
	
}
print "Downloaded $count articles, for SCIENCE!\n";


