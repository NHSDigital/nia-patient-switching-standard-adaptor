const fs = require('fs');
const jsonDiff = require('json-diff');
const inputFolder = './unprocessed-files';
const outputFolder = './processed-files';
const difFolder = './files-diff';

compareFilesInFolder(outputFolder, difFolder);

function compareFilesInFolder(outputFolder, difFolder) {

	let filesToCompare = fs.readdirSync(outputFolder);

	for (var y = 0; y < filesToCompare.length; y++) {

		let typeIndex = filesToCompare[y].indexOf('-');
		let type = filesToCompare[y].substring(0, typeIndex);

		if(type == "actual") {

			let fileName = filesToCompare[y].substring(typeIndex, filesToCompare[y].length);
			let comparisonFile = "expected" + fileName;
			
			let outputReport = "\n";
			outputReport = outputReport + "-----------------------------\n";
			outputReport = outputReport + "Comparing the following files\n";
			outputReport = outputReport + outputFolder + '/' + filesToCompare[y] + "----" + outputFolder + '/' + comparisonFile + "\n";

			let actualPSAdapterOutput = JSON.parse(fs.readFileSync(outputFolder + '/' + filesToCompare[y], 'utf8'));
			let emisOutput = JSON.parse(fs.readFileSync(outputFolder + '/' + comparisonFile, 'utf8'));
			
			let entriesByResource = resourceTypeCount(actualPSAdapterOutput, emisOutput);
			
			// go through each resourceType and analyse
			for (var resourceTypeIndex = 0; resourceTypeIndex < entriesByResource.resourceTypes.length; resourceTypeIndex++) { //entriesByResource.resourceTypes.length
				
				let resourceType = entriesByResource.resourceTypes[resourceTypeIndex];
				outputReport = outputReport + "\n"
				outputReport = outputReport + "---------------------------------------------------------------------------------\n";
				outputReport = outputReport + "Analysing Entries of type " + resourceType + "\n"
				outputReport = outputReport + "---------------------------------------------------------------------------------\n";
				outputReport = outputReport + "PS Entries = " + entriesByResource.psEntries[resourceTypeIndex].length + " vs EMIS Entires = " + entriesByResource.emisEntries[resourceTypeIndex].length + "\n";
				if (entriesByResource.psEntries[resourceTypeIndex].length !== entriesByResource.emisEntries[resourceTypeIndex].length) {
					outputReport = outputReport + "ALERT: There is a mismatch in the number of entries of type " + resourceType + "\n"
				}
			}

			const baseFolderLocation = "./comparison-reports";
			const comparisonName = "report-" + fileName;
			const reportOutput = baseFolderLocation + "/" + comparisonName;

			if (!fs.existsSync(reportOutput)){
				fs.mkdirSync(reportOutput, { recursive: true });
			}

			fs.writeFile(reportOutput + "/report.txt", outputReport, 
				err => {
		  			if (err) throw err;               
		  			console.log(reportOutput + 'file created successfully');
				}
		    );

			const entriesSaveLocation = reportOutput + "/entires";
			if (!fs.existsSync(entriesSaveLocation)){
				fs.mkdirSync(entriesSaveLocation, { recursive: true });
			}

			
			for (var resourceTypeIndex = 0; resourceTypeIndex < entriesByResource.resourceTypes.length; resourceTypeIndex++) {
				
				let resourceType = entriesByResource.resourceTypes[resourceTypeIndex];

				let emisEntries = entriesByResource.emisEntries[resourceTypeIndex];
				let psEntries = entriesByResource.psEntries[resourceTypeIndex];

				sortResourcesByMatchingId(emisEntries, psEntries);				
				
				if(resourceType === "List"){
					sortResourcesByTitleAndReference(emisEntries, psEntries);
				}

				var emisEntriesAsString = JSON.stringify({ entry : emisEntries} )
				var psEntriesAsString = JSON.stringify({ entry: psEntries} )





				fs.writeFile(entriesSaveLocation + "/emis-" + resourceType + "-entries.json", emisEntriesAsString, 
				err => {
		  			if (err) throw err;               
		  			console.log(reportOutput + 'file created successfully');
				});

				fs.writeFile(entriesSaveLocation + "/ps-" + resourceType + "-entries.json", psEntriesAsString, 
				err => {
		  			if (err) throw err;               
		  			console.log(reportOutput + 'file created successfully');
				});
				
				let diff = jsonDiff.diffString({ entry : emisEntries}, { entry: psEntries}, {full:true, color:true}).replaceAll(/(\[31m|\[32m|39m|)+/g, "");
				
				fs.writeFile(entriesSaveLocation + "/diff-" + resourceType + "-report.txt", diff, 
				err => {
		  			if (err) throw err;               
		  			console.log(reportOutput + 'file created successfully');
				});
			}
		}
	}
}

function resourceTypeCount(actualPSAdapterOutput, emisOutput) {

	let resourceTypePSOutput = []
	let resourceTypeEMISOutput = []
	let resourceEntriesByTypePS = []
	let resourceEntriesByTypeEMIS = []

	// count resources on the PS json files .. entry.resource
	actualPSAdapterOutput.entry.forEach(entry => {
		if (resourceTypePSOutput.indexOf(entry.resource.resourceType) !== -1) {
			let indexOfResource = resourceTypePSOutput.indexOf(entry.resource.resourceType)
			resourceEntriesByTypePS[indexOfResource].push(entry)
		} else {
			resourceTypePSOutput.push(entry.resource.resourceType)
			resourceTypeEMISOutput.push(entry.resource.resourceType)
			resourceEntriesByTypePS.push([entry])
			resourceEntriesByTypeEMIS.push([])
		}
	});

	// console.log(resourceTypeEMISOutput)
	// console.log(resourceTypePSOutput)

	// count resources on the EMIS json file.. entry.resource
	emisOutput.entry.forEach(entry => {
		if (resourceTypeEMISOutput.indexOf(entry.resource.resourceType) !== -1) {
			let indexOfResource = resourceTypeEMISOutput.indexOf(entry.resource.resourceType)
			resourceEntriesByTypeEMIS[indexOfResource].push(entry)
		} else {
			resourceTypeEMISOutput.push(entry.resource.resourceType)
			resourceTypePSOutput.push(entry.resource.resourceType)
			resourceEntriesByTypeEMIS.push([entry])
			resourceEntriesByTypePS.push([])
		}
	});

	// for(var resourceIndex = 0; resourceIndex < resourceTypeEMISOutput.length; resourceIndex++){
	// 	let alertMessage = "                <<<<<<<<<<<<<<<< MISMATCH"
	// 	if (resourceTypePSOutput.indexOf(resourceTypeEMISOutput[resourceIndex] !== -1)){
			
	// 		if (resourceEntriesByTypeEMIS[resourceIndex].length === resourceEntriesByTypePS[resourceIndex].length){
	// 			alertMessage = ""
	// 		}
	// 		console.log(resourceTypeEMISOutput[resourceIndex] + " EMIS " + resourceEntriesByTypeEMIS[resourceIndex].length + ":" + resourceEntriesByTypePS[resourceIndex].length + " PS Adapter " + alertMessage)
	// 	} else {
	// 		console.log(resourceTypeEMISOutput[resourceIndex] + " EMIS " + resourceEntriesByTypeEMIS[resourceIndex].length + ":0" + " PS Adapter" + alertMessage)
	// 	}
	// }

	return {
		resourceTypes : resourceTypePSOutput,
		psEntries : resourceEntriesByTypePS,
		emisEntries : resourceEntriesByTypeEMIS
	}
}

function sortResourcesByMatchingId(jsonEMIS, jsonPs){
	let matchIndex = 0;

	for (var y = 0; y < jsonEMIS.length; y++) {
		for (var x = 0; x < jsonPs.length; x++) {
			if(jsonEMIS[y].resource.id === jsonPs[x].resource.id){
				let oldPositionPs = jsonPs[x] ;
				let newPositionPs = jsonPs[matchIndex];
				jsonPs[x] = newPositionPs;
				jsonPs[matchIndex] = oldPositionPs; 

				let oldPositionEmis = jsonEMIS[y] ;
				let newPositionEmis = jsonEMIS[matchIndex];
				jsonEMIS[y] = newPositionEmis;
				jsonEMIS[matchIndex] = oldPositionEmis; 
				matchIndex++;
			}
		}
	}
}


function sortResourcesByTitleAndReference(jsonEMIS, jsonPs){
	let matchIndex = 0;

	for (var y = 0; y < jsonEMIS.length; y++) {

		for (var x = 0; x < jsonPs.length; x++) {
			if(jsonEMIS[y].resource.title === jsonPs[x].resource.title && jsonEMIS[y].resource.encounter.reference === jsonPs[x].resource.encounter.reference){
				let oldPositionPs = jsonPs[x] ;
				let newPositionPs = jsonPs[matchIndex];
				jsonPs[x] = newPositionPs;
				jsonPs[matchIndex] = oldPositionPs; 

				let oldPositionEmis = jsonEMIS[y] ;
				let newPositionEmis = jsonEMIS[matchIndex];
				jsonEMIS[y] = newPositionEmis;
				jsonEMIS[matchIndex] = oldPositionEmis; 
				matchIndex++;
			}
		}
	}
}