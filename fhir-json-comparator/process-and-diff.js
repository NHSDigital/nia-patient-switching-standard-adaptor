const fs = require('fs');
const jsonDiff = require('json-diff');
const inputFolder = './unprocessed-files';
const outputFolder = './processed-files';
const difFolder = './files-diff';

processFilesToBeCompared(inputFolder, outputFolder);

setTimeout(()=>{compareFilesInFolder(outputFolder, difFolder)}, 1000)

function processFilesToBeCompared(inputFolder, outputFolder) {
	let files = fs.readdirSync(inputFolder);

	for (var i = 0; i < files.length; i++) {
		console.log("processing file = " + files[i]);
		let file = JSON.parse(fs.readFileSync(inputFolder + '/' + files[i], 'utf8'));



		let processedFile = removeFieldsFromObject(file);

		fs.writeFile(outputFolder + '/' + files[i], JSON.stringify(processedFile), 
			err => {
	  			if (err) throw err;               
	  			console.log('saved successfully');
			}
	   );
	}
}

function compareFilesInFolder(outputFolder, difFolder) {

	let filesToCompare = fs.readdirSync(outputFolder);

	console.log("ComparingFiles");


	for (var y = 0; y < filesToCompare.length; y++) {

		let typeIndex = filesToCompare[y].indexOf('-');
		let type = filesToCompare[y].substring(0, typeIndex);

		if(type = "actual"){
			let fileName = filesToCompare[y].substring(typeIndex, filesToCompare[y].length);
			let comparisonFile = "expected" + fileName;

			console.log("Comparing the following files");
			console.log(outputFolder + '/' + filesToCompare[y]);
			console.log(outputFolder + '/' + comparisonFile + "\n");

			let object1 = JSON.parse(fs.readFileSync(outputFolder + '/' + filesToCompare[y], 'utf8'));

			let object2 = JSON.parse(fs.readFileSync(outputFolder + '/' + comparisonFile, 'utf8'));
			
			let dif = jsonDiff.diffString(object1, object2, {full:true, color:true}).replaceAll(/(\[31m|\[32m|39m|)+/g, ""); //here


			let difFilePath = difFolder + '/dif-' + fileName.replace('json', 'txt');

			fs.writeFile(difFilePath, dif, 
				err => {
		  			if (err) throw err;               
		  			console.log(difFilePath + 'file created successfully');
				}
		    );
		}
	}
}

function removeFieldsFromObject(jsonObject) {

	if('id' in jsonObject){
		jsonObject.id = "";
	}
	if('identifier' in jsonObject.entry[0].resource){
		jsonObject.entry[0].resource.identifier[0].value = "";
	}

	console.log('*******************************************************************');
	console.log('*******************************************************************');

	for (let i = 0; i < jsonObject.entry.length; i++) 
	{


		jsonObject.entry[i] = recursive(jsonObject.entry[i]);


/*		if('resource' in jsonObject.entry[i]){
			jsonObject.entry[i].resource.id = "";
				
			if('medicationReference' in jsonObject.entry[i].resource){											
				if('reference' in jsonObject.entry[i].resource.medicationReference){
					let indexOfSlash = jsonObject.entry[i].resource.medicationReference.reference.indexOf('/') + 1; //
					let newString = jsonObject.entry[i].resource.medicationReference.reference.substring( 0, indexOfSlash);
					jsonObject.entry[i].resource.medicationReference.reference = newString;
				}
			}

			if('subject' in jsonObject.entry[i].resource){
				if('reference' in jsonObject.entry[i].resource.subject){
					jsonObject.entry[i].resource.subject.reference = "";	
				}
			}

			if('patient' in jsonObject.entry[i].resource){
				if('reference' in jsonObject.entry[i].resource.patient){
					jsonObject.entry[i].resource.patient.reference = "";
				}
			}

			if('custodian' in jsonObject.entry[i].resource){											
				if('reference' in jsonObject.entry[i].resource.custodian){
					let indexOfSlash = jsonObject.entry[i].resource.custodian.reference.indexOf('/') + 1; //
					let newString = jsonObject.entry[i].resource.custodian.reference.substring( 0, indexOfSlash);
					jsonObject.entry[i].resource.custodian.reference = newString;
				}
			}

			if('entry' in jsonObject.entry[i].resource){
				for (var index = 0; index < jsonObject.entry[i].resource.entry.length; index++) {

					if('item' in jsonObject.entry[i].resource.entry[index]){											
						if('reference' in jsonObject.entry[i].resource.entry[index].item){
							let indexOfSlash = jsonObject.entry[i].resource.entry[index].item.reference.indexOf('/') + 1; //
							let newString = jsonObject.entry[i].resource.entry[index].item.reference.substring( 0, indexOfSlash);
							jsonObject.entry[i].resource.entry[index].item.reference = newString;
						}
					}
				}
			}
		}*/
	}
	return jsonObject;
}




function recursive(entry){


    var properties = Object.keys(entry);
	console.log("properties = " + properties);

    for(let i = 0; i < properties.length; i++){

		console.log("entry[properties[i]] = " + entry[properties[i]]);

    		console.log(" Array.isArray(entry) === false //// " +  Array.isArray(entry) === false);

        if( typeof entry[properties[i]] === 'object' && Array.isArray(entry) === false){
    		console.log("entering object recursion");
            entry[properties[i]] = recursive(entry[properties[i]]);
        }
        else{
            if(Array.isArray(entry)){
                
                for(let y = 0; y < entry.length; y++){

        	        if( typeof entry[properties[i]] === 'object'){
    					console.log("entering object recursion");
			            entry[properties[i]] = recursive(entry[properties[i]]);
			        }
                }
            }else {
				if('reference' === properties[i]){
					console.log("entering reference.value recursion = " +  entry[properties[i]] );
					let indexOfSlash = entry[properties[i]].indexOf('/') + 1;
					let newString = entry[properties[i]].substring( 0, indexOfSlash);
					entry[properties[i]] = newString;
				}
            }
        }
    }

    console.log(entry);
    return entry;
}



