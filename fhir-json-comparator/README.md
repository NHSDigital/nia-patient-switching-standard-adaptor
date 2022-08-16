# New Raw Compare process as of 25/07/2022

### Raw-Compare

Our first attempt at simplifying the comparison process was not completely successful. 
A new script raw-compare.js can be found in this directory which will pre format and compare two json objects. 
The script can be run with the following paramaters...

node raw-compare.json "location of provider file" "location of PS file" "output folder to save the results e.g.."

e.g. 

node raw-compare.js "./unprocessed-files/provider-PWPT2-output.json" "./unprocessed-files/ps-PWPT2-output.json" "./raw-compare-results"


The script has three process...

1. The files are removed of any fields we know will not be in the PSAdaptor Output. These can be configured in the Config Section 2 & 3 of the script and are customisable for each entry resource type. 
2. Secondly, we cannot compare items easily with non-matching ID's, therefore we attempt to replace ID's on the PS file side with the Provider record IDs where given matching conditions are found. The matching conditions can be found and modified under Config Section 4 
3. Thirdly we compare and output as much data as possible about the two files looking primarily for differences. We still look for matching Conditions in Config section 4 but we also try to order and match arrays with given conditions under Config Section 5 too. The result of the compare will be saved to the given output folder with the original files, our processed files and a report.txt. 

By default the script will only output differences but the paramater const displayMatches = false; can be set to true to output all field comparisons. 

*NOTE: There is also a multi-compare.sh script that will run multiple comparisons.

_________


# OLD as of 25/07/2022 Scripts to compare JSON fhir objects

NOTE: before staring, scripts and json-diff may not work in IDE command line. It has to be OS command line.

in the fhir-json-comparator there are 2 scripts that compare fhir JSON files.
create-entry-reports.js and process-and-diff.js

in order for the files to be compared, there should be a file called actual-FILENAME.json,
and the file to be compared should be called expected-FILENAME.json. 
Many files with the same name format can be placed in the unprocessed-files folder, and they will all be compared.
the process-and-diff.js uses 'json-diff' tool against the two files. create-entry-reports.js opens both files and uses 
'json-diff' tool against each property


###process-and-diff.js

the 'process-and-diff.js' script will:
- grab the 2 files in the unprocessed-files folder,
- removes generated fields and place them in the 'processed-files' directory
- it will grab them from 'processed-files' directory, and compares the files using a tool called 'json-diff' and place 
them in files-diff


note: it will do the process with each file which names contain actual and expected, inside unprocessed-files directory

###create-entry-reports.js

the 'create-entry-reports.js' script will:
- grab the 2 files in the unprocessed-files folder,
- removes generated fields and place them in the 'processed-files' directory
- it will grab them from 'processed-files' directory
- it will loop through each property value of the JSON object from 'actual', and use the tool to compare the 
properties with the same name property in the 'expected' file (using the 'json-diff' tool) 
- it will then create a directory for each file in 'comparison-reports' directory.

## Installation
to be able to use this tool, NPM should already be installed 

to begin with, open terminal in the computer, change directory to 'fhir-json-comparator' folder and run the following 
command:

```
   npm install
```

## Running the scripts

to begin with, open terminal in the computer, change directory to 'fhir-json-comparator' folder and run the following 
command:

```
   node process-and-diff.js
```
or
```
   node create-entry-reports.js
```