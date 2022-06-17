# Scripts to compare JSON fhir objects

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