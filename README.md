# VisSearch: Framework for Visual Search
Implementation for the Best Student Paper for MAICS2016: http://cslab.valpo.edu/maics2016/paper20.pdf
Live Demonstration (Temporary): http://www.citegraph.xyz:8080/vis


## Installation
This document will assist you in successfully installing the DocVis web application for development.

### PREREQUISITES:
	You have the following: Perl, IntelliJ IDEA, a distribution of the repository.

-----------

### INSTALLATION:

1. Install necessary Perl modules.
   
   This includes `Text::CSV`, `Text::Unidecode`, and `WWW::Mechanize`.
   This operation is completed easiest by using cpanm to download the necessary modules.
   If you have issues completing this step, Google any errors which may arise.
   
   Download cpan, then run the following:
   * `cpan App::cpanminus`
   * `cpanm Text::CSV`
   * `cpanm Text::Unidecode`
   * `cpanm WWW::Mechanize`

2. Download the papers
   
   You may make up any directories of your choosing, but the recommended setup is as follows:
   1. Create a directory named `RESOURCE_FOLDER` in the base `docvis` directory.
   2. Inside `RESOURCE_FOLDER` create the path `resources/pdfs`.
   3. Inside `RESOURCE_FOLDER` create a directory `db`
   4. Run `scripts/getPapersCsv.pl` with the following parameters:
      * `./getPapersCsv.pl INPUT_CSV OUTPUT_DIRECTORY DOCUMENT_DATABASE_NAME`
          - `INPUT_CSV` is `scripts/pdf-links.csv`
          - `OUTPUT_DIRECTORY` is the path to the directory where you will be dumping all of your PDFs (recommended `RESOURCE_FOLDER/resources/pdfs`)
          - `DOCUMENT_DATABASE_NAME` is `RESOURCE_FOLDER/db/pdf-info.csv`
          + **NOTE:** In the vanilla implementation of DocVis, this parameter is *REQUIRED* to be the path to the `RESOURCE_FOLDER/db/pdf-info.csv`
      + **NOTE:** The script will take a good while to complete; it has to download a lot of data.

3. Create stopwords file.
   
   The stopwords file is a file containing blacklisted words that are NOT to be indexed.
   Any word contained within will be skipped over when indexing.
   
   You may use whatever stopwords you wish, but we recommend using our stopwords file, which you can find in the relevant-documents directory.
   
   Regardless of what you have for your stopwords file, place it at
   * `RESOURCE_FOLDER/db/stopwords.txt`

4. Create dictionary file
   
   The dictionary file is a whitelist text file containing words that are to be recognized by the tier 2 utilities: the words in this file are the only words to appear in tier 2 as synonyms, related terms, etc. 
   
   Like the stopwords.txt, you may use whatever stopwords you wish, but we recommend our dictionary file, which can also be found in the relevant-documents directory.
   
   Regardless of what you have for your dictionary file, place it at
   * `RESOURCE_FOLDER/db/dictionary.txt`

5. Set up server run configuration
   
   If you are not using IntelliJ, you will have to set up a configuration to run a tomcat server with `RESOURCE_FOLDER` defined as described earlier.
   
   For IntelliJ:
   1. Open up a project.
   2. Choose `Edit Configurations` from the *Run* menu.
   3. Select the `+` and choose `Maven`.
   4. Name it something descriptive, like "Run Tomcat"
   5. Where it says *Command Line* enter `tomcat7:run`
   6. Click on the `Runner` tab.
   7. Under *Environment Variables* click on the `...` button.
   8. Click on the `+` and add an entry for
     * `name:	   RESOURCE_FOLDER`
     * `value: {wherever you parked the RESOURCE_FOLDER directory earlier on.}`
   9. Accept changes.
   10. Accept changes again.
   11. Now select your new configuration from the drop-down *Run* menu.
   12. Click on the green 'run' arrow.

------------

### POST-INSTALLATION NOTES:

 If you see the terminal spin up the indexer and start indexing your PDFs, then
   CONGRATULATIONS!!!  You have successfully set up the DocVis project.
 
 To make future instances of the server start up more rapidly, you may add a config file titled
   `RESOURCE_FOLDER/config/index-config.cfg`
 Where RESOURCE_FOLDER is what you specified earlier.  Add the following line to the file:
  `INDEX_DOCS=false`

 This will tell the application to skip indexing when it starts the server, making things much quicker for you in the long run.
 If you ever want it to index again, simply edit the line to read `true` instead of `false`.

----------------
