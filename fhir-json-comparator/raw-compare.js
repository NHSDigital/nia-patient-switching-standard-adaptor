const fs = require('fs');

// # Config section 1
// set to true to display matching values as well as differences

const displayMatches = false;

// # Config section 2
// fields that correlate to data that will not be in a GP2GP message,  
// anything in these arrays will be stripped from the data before comparing

const patientFieldsToIgnore = ["resource.meta", "resource.extension", "resource.identifier.extension", "resource.name", "resource.contact", "resource.gender", "resource.birthDate", "resource.address", "resource.generalPractitioner", "resource.managingOrganization"];
const organizationFieldsToIgnore = ["resource.meta.versionId", "resource.extension", "resource.type", "resource.name", "resource.telecom", "resource.address"];
const allergyIntoleranceFieldsToIgnore = [];
const conditionFieldsToIgnore = [];
const encounterFieldsToIgnore = [];
const listFieldsToIgnore = [];
const locationFieldsToIgnore = ["resource.meta.versionId", "resource.telecom", "resource.address", "resource.managingOrganization"];
const medicationFieldsToIgnore = [];
const medicationRequestFieldsToIgnore = ["resource.dispenseRequest.expectedSupplyDuration"];
const medicationStatementFieldsToIgnore = [];
const observationFieldsToIgnore = [];
const practitionerFieldsToIgnore = ["resource.meta.versionId", "resource.gender"];
const procedureRequestFieldsToIgnore = [];

// # Config section 3
// object removers remove the whole of a parent object if a condition occurs 
// during the data stripping process

const allergyIntoleranceObjectRemovers = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["http://read.info/readv2"] },
	//{"parentName": "resource.code.coding.extension.extension", "field": "url", "checkType" : "equals", "checkValues" : ["descriptionDisplay"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["https://fhir.hl7.org.uk/Id/egton-codes"] }
	// {"parentName": "resource.type.code.coding", "field": "code", "checkType" : "contains", "checkValues" : ["000000"] }
]

const conditionObjectRemovers = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["http://read.info/readv2"] },
	//{"parentName": "resource.code.coding.extension.extension", "field": "url", "checkType" : "equals", "checkValues" : ["descriptionDisplay"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["https://fhir.hl7.org.uk/Id/egton-codes"] }
	// {"parentName": "resource.type.code.coding", "field": "code", "checkType" : "contains", "checkValues" : ["000000"] }
]

const locationObjectRemovers = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
]

const organizationObjectRemovers = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
]

const encounterObjectRemover = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["http://read.info/readv2"] },
	//{"parentName": "resource.code.coding.extension.extension", "field": "url", "checkType" : "equals", "checkValues" : ["descriptionDisplay"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["https://fhir.hl7.org.uk/Id/egton-codes"] },
	{ "parentName": "resource.type.coding", "field": "code", "checkType": "contains", "checkValues": ["000000"] }
];

const medicationObjectRemover = [
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["https://fhir.hl7.org.uk/Id/emis-drug-codes"] }
]

const medicationRequestObjectRemover = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] }
]

const medicationStatementObjectRemover = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] }
]

const observationObjectRemover = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
	{ "parentName": "resource.code.coding", "field": "system", "checkType": "equals", "checkValues": ["http://read.info/readv2"] }
]

const procedureRequestObjectRemover = [
	{ "parentName": "resource.identifier", "field": "system", "checkType": "contains", "checkValues": ["https://EMISWeb", "https://PSSAdaptor"] },
]

// # Config section 4
// Matchers - these arrays define how to identify one item with another across files
// a first past the post method is used, if more than 1 item exists after the final check or 
// we don;t find a match, potential matches will be displayed

const organizationMatchFields = ["id"];
const allergyIntoleranceMatchFields = ["assertedDate", "onsetDateTime"];
const practionerMatchFields = ["name.[0].family", "name.[0].given.[0]"];
const encounterMatchFields = ["id"];
const listMatchFields = ["title", "encounter.reference", "date", "id"]
const locationMatchFields = ["name"]
const conditionMatchFields = ["id"]
const procedureRequestMatchFields = ["id"]
const medicationMatchFields = ["code.coding.[0].display"];
const medicationRequestMatchFields = ["id"];
const medicationStatementMatchFields = ["id"]
const observationMatchFields = ["id"]

// # Config section 5 
// array sorters manage matching on nested arrays, for example paticiant array fields will be 
// value matched to order known matches near the start of an array with non matches at the bottom

const arraySorters = [
  { arrayReference : "participant", sortOn : ["type.[0].coding.[0].system", "type.[0].coding.[0].code"] },
  { arrayReference : "entry", sortOn : ["item.reference"] },
// 	{ arrayReference : "extension", sortOn : ["url", "valueCodeableConcept.reference"] },
 	{ arrayReference : "basedOn", sortOn : ["reference"] },
 	{ arrayReference : "category", sortOn : [""] },
 	{ arrayReference : "note", sortOn : ["text"] }
]

// global storage items that need to be tracked
const removedKeys = [];
const unsupportedResourceTypes = [];


// MAIN ENTRY POINT
setTimeout(() => { processApplication() }, 1000)

function processApplication() {
	
	var providerOutputFileLocation = process.argv[2];
	var psOutputFileLocation = process.argv[3];
	var reportOutputFolder = process.argv[4];

	let preProcessedFiles = preProcessFiles(providerOutputFileLocation, psOutputFileLocation);
	preProcessedFiles.psFile = preProccessIdMismatches(preProcessedFiles.providerFile, preProcessedFiles.psFile)
	let scanResults = scanFilesAndCompareOnMatches(preProcessedFiles.providerFile, providerOutputFileLocation, preProcessedFiles.psFile, psOutputFileLocation)

	const providerFileName = providerOutputFileLocation.substring(providerOutputFileLocation.lastIndexOf("/") +1, providerOutputFileLocation.lastIndexOf("."))
	const psFileName = psOutputFileLocation.substring(providerOutputFileLocation.lastIndexOf("/") +1, psOutputFileLocation.lastIndexOf("."))
	fullOutputLocationFolder = reportOutputFolder + "/" + new Date().toISOString().replaceAll("-", "").replaceAll(":", "").replaceAll(".", "") + "-(" + providerFileName + " : " + psFileName + ")";
	
	if (!fs.existsSync(fullOutputLocationFolder )) {
		fs.mkdirSync(fullOutputLocationFolder , { recursive: true });
	}

	fs.writeFile(fullOutputLocationFolder + "/report.txt", scanResults,
		err => {
			if (err) throw err;
		}
	);

	fs.writeFile(fullOutputLocationFolder + "/original_provider_file(" + providerFileName + ").json", fs.readFileSync(providerOutputFileLocation),
		err => {
			if (err) throw err;
		}
	)

	fs.writeFile(fullOutputLocationFolder + "/original_ps_file(" + psFileName + ").json", fs.readFileSync(psOutputFileLocation),
		err => {
			if (err) throw err;
		}
	)

	fs.writeFile(fullOutputLocationFolder + "/processed_provider_file(" + providerFileName + ").json", JSON.stringify(preProcessedFiles.providerFile),
		err => {
			if (err) throw err;
		}
	)

	fs.writeFile(fullOutputLocationFolder + "/processed_ps_file(" + psFileName + ").json", JSON.stringify(preProcessedFiles.psFile),
		err => {
			if (err) throw err;
		}
	)
}

// Entry point to strip out all known properties that the PS Adapter will not contain
function preProcessFiles(providerFile, psFile) {

	let providerJsonFile = JSON.parse(fs.readFileSync(providerFile), 'utf8');
	let psJsonFile = JSON.parse(fs.readFileSync(psFile), 'utf8');

	return {
		providerFile : preProcessFile(providerJsonFile),
		psFile : preProcessFile(psJsonFile)
	}
}

function preProccessIdMismatches(providerFile, psFile) {

	let psIdReplacedJson = scanForIdsAndReplace(providerFile, psFile);
	return psIdReplacedJson;

}

function scanFilesAndCompareOnMatches(providerFile, providerFileName, psFile, psFileName) {

	let outputReport = "\n";
	outputReport = outputReport + "-----------------------------\n";
	outputReport = outputReport + "Comparing the following files\n";
	outputReport = outputReport +  providerFileName + "----" + psFileName + "\n";

	outputReport = outputReport + compareFiles(providerFile, psFile);

	return outputReport;
}


function preProcessFile(jsonObject) {

	if ('id' in jsonObject) {
		jsonObject.id = "";
	}

	var newEntryArray = []
	for (let i = 0; i < jsonObject.entry.length; i++) {
		let entry = jsonObject.entry[i];
		switch (entry.resource.resourceType) {
			case "Patient": newEntryArray.push(recursiveIdScan(entry, patientFieldsToIgnore, null, "", entry.resource.id)); break;
			case "Organization": newEntryArray.push(recursiveIdScan(entry, organizationFieldsToIgnore, organizationObjectRemovers, "", entry.resource.id)); break;
			case "AllergyIntolerance": newEntryArray.push(recursiveIdScan(entry, allergyIntoleranceFieldsToIgnore, allergyIntoleranceObjectRemovers, "", entry.resource.id)); break;
			case "Condition": newEntryArray.push(recursiveIdScan(entry, conditionFieldsToIgnore, conditionObjectRemovers, "", entry.resource.id)); break;
			case "Encounter": newEntryArray.push(recursiveIdScan(entry, encounterFieldsToIgnore, encounterObjectRemover, "", entry.resource.id)); break;
			case "List": newEntryArray.push(recursiveIdScan(entry, listFieldsToIgnore, null, "", entry.resource.id)); break;
			case "Location": newEntryArray.push(recursiveIdScan(entry, locationFieldsToIgnore, locationObjectRemovers, "", entry.resource.id)); break;
			case "Medication": newEntryArray.push(recursiveIdScan(entry, medicationFieldsToIgnore, medicationObjectRemover, "", entry.resource.id)); break;
			case "MedicationRequest": newEntryArray.push(recursiveIdScan(entry, medicationRequestFieldsToIgnore, medicationRequestObjectRemover, "", entry.resource.id)); break;
			case "MedicationStatement": newEntryArray.push(recursiveIdScan(entry, medicationStatementFieldsToIgnore, medicationStatementObjectRemover, "", entry.resource.id)); break;
			case "Observation": newEntryArray.push(recursiveIdScan(entry, observationFieldsToIgnore, observationObjectRemover, "", entry.resource.id)); break;
			case "Practitioner": newEntryArray.push(recursiveIdScan(entry, practitionerFieldsToIgnore, null, "", entry.resource.id)); break;
			case "ProcedureRequest": newEntryArray.push(recursiveIdScan(entry, procedureRequestFieldsToIgnore, procedureRequestObjectRemover, "", entry.resource.id)); break;
			default: processUnsupportedResourceType(entry.resource.resourceType);
		}
	}

	jsonObject.entry = newEntryArray;
	return jsonObject;
}

function processUnsupportedResourceType(type) {
	if (unsupportedResourceTypes.indexOf(type) === -1) {
		unsupportedResourceTypes.push(type);
	}
}

function recursiveIdScan(resource, ignoreParams, ignoreParents, parentId, entryId, ignoreOveride = false) {

	var properties = Object.keys(resource);
	var responseObject = {};

	for (let i = 0; i < properties.length; i++) {

		// if the parameter is in our ignore path, return our current object build
		const propertyWithParent = parentId + properties[i];

		if (ignoreParams.indexOf(propertyWithParent) !== -1 && ignoreOveride === false) {
			removedKeys.push(entryId + ": " + propertyWithParent);
			continue;
		}

		// if the property we are at is an object and not an array, lets dive in recursivly
		if (typeof resource[properties[i]] === 'object' && Array.isArray(resource[properties[i]]) === false) {
			responseObject[properties[i]] = recursiveIdScan(resource[properties[i]], ignoreParams, ignoreParents, parentId + properties[i] + ".", entryId);
			continue;
		}

		// if we're dealing with an array manage the internal properties.
		if (Array.isArray(resource[properties[i]])) {
			var arrayResponse = []
			for (let y = 0; y < resource[properties[i]].length; y++) {
				if (typeof resource[properties[i]][y] === 'object') {
					let response = recursiveIdScan(resource[properties[i]][y], ignoreParams, ignoreParents, parentId + properties[i] + ".", entryId)
					if (response !== null) {
						arrayResponse.push(response);
					}
				} else {
					arrayResponse.push(resource[properties[i]][y]);
				}
			}

			if (arrayResponse.length !== 0) {
				responseObject[properties[i]] = arrayResponse;
			}
			continue;
		}

		// run through the ignore object checkers, if we meet the conditions to ignore the full object, return null
		if (ignoreParents !== null) {
			for (let j = 0; j < ignoreParents.length; j++) {
				let ignoreObject = ignoreParents[j];
				if (ignoreObject.parentName + "." === parentId) {
					if (ignoreObject.field === properties[i]) {
						for (z = 0; z < ignoreObject.checkValues.length; z++) {
							switch (ignoreObject.checkType) {
								case "contains": {
									if (resource[properties[i]].indexOf(ignoreObject.checkValues[z]) !== -1) {
										return null;
									}
									break;
								}
								case "equals": {
									if (resource[properties[i]] === ignoreObject.checkValues[z]) {
										return null;
									}
									break;
								}
								default: { break; }
							}

						}
					}
				}
			}
		}

		// // we are now intrested in this object so lets add it into our new object after checking for date cut offs
		if (properties[i].toLowerCase().indexOf("date") !== -1
			|| properties[i].toLowerCase().indexOf("authoredon") !== -1
			|| properties[i].toLowerCase().indexOf("issued") !== -1) {

			if (resource[properties[i]].length >= 25) {
				responseObject[properties[i]] = resource[properties[i]].substring(0, 19);
			} else {
				responseObject[properties[i]] = resource[properties[i]];
			}
		} else {
			responseObject[properties[i]] = resource[properties[i]];
		}
	}

	return responseObject;
}

function scanFilesAndCompareOnMatches(providerFile, providerFileName, psFile, psFileName) {

	// begin our output report, it's a string of multiline information
	let outputReport = "\n";
	outputReport = outputReport + "-----------------------------\n";
	outputReport = outputReport + "Comparing the following files\n";
	outputReport = outputReport +  providerFileName + "----" + psFileName + "\n";

	// once we have loaded our two files, begin our compare
	outputReport = outputReport + compareFiles(providerFile, psFile);

	return outputReport;
}

function compareFiles(emisOutputFile, psOutputFile) {

	// sort the two files by their respective output types
	let entriesByResource = resourceTypeCount(emisOutputFile, psOutputFile);
	let allPsEntires = psOutputFile.entry;
	let outputReport = "";

	// go through each resourceType and analyse
	for (var resourceTypeIndex = 0; resourceTypeIndex < entriesByResource.resourceTypes.length; resourceTypeIndex++) { //entriesByResource.resourceTypes.length

		let resourceType = entriesByResource.resourceTypes[resourceTypeIndex];
		outputReport = outputReport + "\n"
		outputReport = outputReport + "---------------------------------------------------------------------------------\n";
		outputReport = outputReport + "Analysing Entries of type " + resourceType + "\n"
		outputReport = outputReport + "---------------------------------------------------------------------------------\n";
		outputReport = outputReport + "EMIS Entires = " + entriesByResource.emisEntries[resourceTypeIndex].length + " vs PS Entries = " + entriesByResource.psEntries[resourceTypeIndex].length + "";
		if (entriesByResource.psEntries[resourceTypeIndex].length !== entriesByResource.emisEntries[resourceTypeIndex].length) {
			outputReport = outputReport + "\nALERT: There is a mismatch in the number of entries of type " + resourceType + ""
		}

		outputReport = outputReport + "\n\n"

		switch (resourceType) {

			// only ever one instance of Patient and the ID's will never match by default
			case "Patient": {
				outputReport = outputReport + "Matches will be found via the following fields: "
				outputReport = outputReport + allergyIntoleranceMatchFields + "\n\n";
				const emisEntry = entriesByResource.emisEntries[resourceTypeIndex][0].resource;
				const psEntry = entriesByResource.psEntries[resourceTypeIndex][0].resource;
				outputReport = outputReport + "EMIS Patient Entry 1 \n"
				outputReport = outputReport + "EMIS Entry " + emisEntry["id"] + " vs PS Entry " + psEntry["id"] + "\n";
				outputReport = outputReport + compareJsonEntry(emisEntry, psEntry, "resource");
				break;
			}
			case "AllergyIntolerance": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, allergyIntoleranceMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Location": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, locationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Condition": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, conditionMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Encounter": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, encounterMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "MedicationRequest": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, medicationRequestMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "MedicationStatement": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, medicationStatementMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Observation": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, observationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "ProcedureRequest": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, procedureRequestMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "List": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, listMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Medication": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, medicationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Practitioner": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, practionerMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				break;
			}
			case "Organization": {
				outputReport = outputReport + resourceTypeCompareBuilder(resourceType, resourceTypeIndex, organizationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries, allPsEntires)
				outputReport = outputReport + "NOTE: Given the limited Details in Organisation, there is no automatic way to compare \n"
				break;
			}
		}
	}

	return outputReport
}

function resourceTypeCompareBuilder(resourceType, resourceTypeIndex, resourceMatchFields, resourceEmisEntries, resourcePsEntries, allPSEntires) {

	let outputReport = "Matches will be found via the following fields: "
	outputReport = outputReport + resourceMatchFields + "\n\n";
	outputReport = outputReport + setupCompare(resourceEmisEntries, resourcePsEntries, resourceType, resourceTypeIndex, allPSEntires, resourceMatchFields);
	return outputReport;
}

function setupCompare(emisEntries, psEntries, resourceName, resourceTypeIndex, allPSEntires, matchFields) {

	let foundPsEntries = [];
	let outputReport = "";

	for (var emisEntryCount = 0; emisEntryCount < emisEntries[resourceTypeIndex].length; emisEntryCount++) {
		const emisEntry = emisEntries[resourceTypeIndex][emisEntryCount].resource;
		let matchingFieldIndex = 0;

		var searchEntries = psEntries[resourceTypeIndex];
		var matchingEntries = [];
		var potentialMatchingEntries = [];

		// if there is more than one match, continue looking through the match properties 
		// until we are left with 0, 1 or no more matchFields
		// NOTE: if we only have 1 ps entry at the start, we should still compare

		while (
			searchEntries.length > 1
			|| (psEntries[resourceTypeIndex].length == 1 && searchEntries.length == 1)
		) {

			// if there are no more match options, we have not found our single match
			if (typeof matchFields[matchingFieldIndex] === 'undefined') {
				searchEntries = [];
			}

			for (var psEntryCount = 0; psEntryCount < searchEntries.length; psEntryCount++) {
				psEntry = searchEntries[psEntryCount];

				// grab the match field property values
				emisEntryMatchPropertyValue = getPropertyMatchByField(emisEntry, matchFields[matchingFieldIndex]);
				psEntryMatchPropertyValue = getPropertyMatchByField(psEntry.resource, matchFields[matchingFieldIndex]);

				//if we've found a match, log it.
				if (emisEntryMatchPropertyValue === psEntryMatchPropertyValue) {
					matchingEntries.push(psEntry);
				}
			}

			
			matchingFieldIndex = matchingFieldIndex + 1;
			searchEntries = [...matchingEntries];

			if (matchingEntries.length !== 0) {
				potentialMatchingEntries = [...searchEntries];
				matchingEntries = [];
			}

			// under the condition that we are only checking against one ps record, 
			// if we still have 1 remaining after all checks then we have found our match and 
			// need to exit our loop, due to the loop clause an escape/break is required
			if (psEntries[resourceTypeIndex].length == 1 && matchingFieldIndex === matchFields.length) {
				break;
			}
		}

		const emisEntryId = getPropertyMatchByField(emisEntry, "id");

		// if we have found a maatching resource entry, compare the objects
		if (searchEntries.length == 1) {

			const psEntry = searchEntries[0].resource;
			// console.log(psEntry)
			const psEntryId = psEntry["id"];
			foundPsEntries.push(psEntryId)
			outputReport = outputReport + "EMIS " + resourceName + " Entry " + (emisEntryCount + 1) + "\n";
			outputReport = outputReport + "EMIS Entry " + emisEntryId + " vs PS Entry " + psEntryId + "\n";
			outputReport = outputReport + compareJsonEntry(emisEntry, psEntry, "resource") + "\n"

			// if (resourceName === "List" && emisEntryCount == 29){
			// 	console.log("Emis Count " +emisEntryCount + " " + emisEntry.id)
			// 	console.log(emisEntry)
			// 	console.log(emisEntryMatchPropertyValue)
			// }

		} else {

			// if we did not find a match...
			outputReport = outputReport + "EMIS " + resourceName + " Entry " + (emisEntryCount + 1) + "\n";
			outputReport = outputReport + "MISSING Entry: EMIS entry with ID " + emisEntryId + " has not been found in the PS enties \n"

			// output any potential matches at the previous stage in the search process
			for (var k = 0; k < potentialMatchingEntries.length; k++) {
				outputReport = outputReport + "    -Potential " + resourceName + " match " + potentialMatchingEntries[k].resource.id + "\n";
			}

			// also check to see if the ID exists under a different resource type 
			// incase it's been incorrectly mapped
			allPSEntires.forEach((entry) => {
				if (entry.resource.id === emisEntryId) {
					outputReport = outputReport + "RESOURCE MISMATCH: Id " + emisEntryId + " has been found against anohter PS Resource type " + entry.resource.resourceType + "\n";
				}
			});

			outputReport = outputReport + "\n"
		}

	}
	// check for additonal records in ps entries
	for (var psEntryCount = 0; psEntryCount < psEntries[resourceTypeIndex].length; psEntryCount++) {
		psEntry = psEntries[resourceTypeIndex][psEntryCount].resource;
		psEntryId = psEntry["id"]
		if (foundPsEntries.indexOf(psEntryId) === -1) {
			outputReport = outputReport + "ADDITIONAL ENTRY: PS Entries have an addition entry with ID " + psEntryId + "\n";
		}
	}

	return outputReport;

}

// drills down on either property or an array index
function getPropertyMatchByField(jsonObject, propertySearch) {

	const diveSteps = propertySearch.split(".")
	let searchProperty = jsonObject;

	for (var step = 0; step < diveSteps.length; step++) {
		propertyId = diveSteps[step];
		// manage array step

		if (propertyId.indexOf("[") === 0 && propertyId.indexOf("]") === propertyId.length - 1) {
			var arrayPos = propertyId.substring(1, propertyId.length - 1);
			searchProperty = searchProperty[arrayPos];
		} else {

			// if (propertyId === "id"){
			// 	if (typeof searchProperty[propertyId] !== 'undefined') { 
			// 		searchProperty = searchProperty[propertyId].substring(0,36);
			// 	}
			// } else {
			// manage property step
				searchProperty = searchProperty[propertyId];
			// }
		}

		if (typeof searchProperty === 'undefined') {
			return null;
		}
	}

	return searchProperty;
}

function compareJsonEntry(emisEntry, psEntry, parentId) {

	const emisProperties = Object.keys(emisEntry);
	const psProperties = Object.keys(psEntry);
	let foundPsProperties = [];

	let compareResponse = "";

	for (let i = 0; i < emisProperties.length; i++) {

		const emisProperty = emisProperties[i];
		const emisPropertyValue = emisEntry[emisProperty];

		// NO PROPERTY: if property does not exist, log and continue to the next iternation
		if (psProperties.indexOf(emisProperty) === -1) {
			compareResponse = compareResponse + "MISSING: Property " + emisProperty + " is missing in PS Entry (Emis Value : " + emisPropertyValue + ") \n";
			continue;
		}

		foundPsProperties.push(parentId + "." + emisProperty);
		const psPropertyValue = psEntry[emisProperty];

		// PROPERTY IS OBJECT: object handling 
		if (typeof emisPropertyValue === 'object' && Array.isArray(emisPropertyValue) === false) {
			compareResponse = compareResponse + compareJsonEntry(emisPropertyValue, psPropertyValue, parentId + "." + emisProperty);
			continue;
		}

		// PROPERTY IS AN ARRAY: array handling 
		if (Array.isArray(emisPropertyValue)) {

			if (emisPropertyValue.length === psPropertyValue.length) {
				if (displayMatches) {
					compareResponse = compareResponse + "MATCH: Array Property " + parentId + "." + emisProperty + " has equal length with PS " + emisProperty + "(" + emisPropertyValue.length + " = " + psPropertyValue.length + ")\n";
				}
			} else {
				compareResponse = compareResponse + "DIFF: Array Property " + parentId + "." + emisProperty + " does not have equal length with PS property " + parentId + "." + emisProperty + " (" + emisPropertyValue.length + " != " + psPropertyValue.length + ")\n";
			}
			
			sortedArrays = sortArrays(emisPropertyValue, psPropertyValue, emisProperty, emisEntry.id );
			const emisSortedArray = sortedArrays.emisSortedArray;
			const psSortedArray = sortedArrays.psSortedArray;

			if(emisEntry.id === "9B69EBA5-F541-4119-9142-0AA0F87E3758-PG0-HD1"){
				console.log(emisSortedArray.length + " " + psSortedArray.length)
			}

			for (let y = 0; y < emisSortedArray.length; y++) {

				emisPropertyValueArrayEntry = emisSortedArray[y];
				psPropertyValueArrayEntry = psSortedArray[y];
		
				if (typeof emisPropertyValue[y] === 'object') {

					if (psPropertyValueArrayEntry !== null && typeof psPropertyValueArrayEntry !== 'undefined') {
						compareResponse = compareResponse + compareJsonEntry(emisPropertyValueArrayEntry, psPropertyValueArrayEntry, parentId + "." + emisProperty + "." + y)
					} else {
						// TODO display emis entry better 
						compareResponse = compareResponse + "MISSING EMIS has an additional array entry in " + parentId + "." + emisPropertyValueArrayEntry + "\n";
					}

				} else {
					compareResponse = compareResponse + valueComparer(emisPropertyValueArrayEntry, psPropertyValueArrayEntry, emisProperty, parentId + "." + emisProperty + "." + y);
				}
			}

			if(emisSortedArray.length < psSortedArray.length){
				compareResponse = compareResponse + "ADDITIONAL: The PS array "+ parentId + "." + emisPropertyValueArrayEntry + " has an additional " + (psSortedArray.length - emisSortedArray.length) + " reosurces \n";
			}

			continue;
		}

		// PROPERTY IS A VALUE: otherwise we have a valid value to compar eof type string
		compareResponse = compareResponse + valueComparer(emisPropertyValue, psPropertyValue, emisProperty, parentId);

	}

	// if any additional properties have been found in the PS entries, log them as the final step
	for (let psPropertyCount = 0; psPropertyCount < psProperties.length; psPropertyCount++) {
		const psProperty = psProperties[psPropertyCount];
		if (foundPsProperties.indexOf(parentId + "." + psProperty) === -1) {
			const psPropertyValue = psEntry[psProperty];
			compareResponse = compareResponse + "NEW: Property " + psProperty + " is not present in the EMIS data (PS Value : " + psPropertyValue + ") \n";
		}
	}

	return compareResponse;
}

function sortArrays(emisArray, psArray, propertyName, id) {

	let canSortArray = false;
	let arraySorterIndex = -1;

	for (var arraySortDetailsIndex = 0; arraySortDetailsIndex < arraySorters.length; arraySortDetailsIndex++) {
		const arraySorter = arraySorters[arraySortDetailsIndex];
		if (arraySorter.arrayReference === propertyName) {
			canSortArray = true;
			arraySorterIndex = arraySortDetailsIndex;
		}
	}

	if (canSortArray === false) {
		return { emisSortedArray: emisArray, psSortedArray: psArray }
	}

	const arraySorter = arraySorters[arraySorterIndex];
	let unfoundEmisItems = [];
	let unfoundPsItems = []
	let emisSortedArray = [];
	let psSortedArray = [];
	let foundPsEntryIndexes = [];

	// if(id === "9B69EBA5-F541-4119-9142-0AA0F87E3758-PG0-HD1"){
	// 	console.log(emisArray.length+ " " +psArray.length)
	// }

	for (var emisItemIndex = 0; emisItemIndex < emisArray.length; emisItemIndex++){
		
		const emisItem = emisArray[emisItemIndex];

		for (var psItemIndex = 0; psItemIndex < psArray.length; psItemIndex++){

			// if(id === "9B69EBA5-F541-4119-9142-0AA0F87E3758-PG0-HD1"){
			// 	console.log(psItemIndex + " " + emisSortedArray.length)
			// }

			const psItem = psArray[psItemIndex];
			let matchingPropertyCount = 0;

			for (var arraySortItemIndex = 0; arraySortItemIndex < arraySorter.sortOn.length; arraySortItemIndex++) {
				var emisPropertyToCheck = emisItem;
				var psPropertyToCheck = psItem;

				
				if (arraySorter.sortOn[0] !== "") {
					const diveIdSteps = arraySorter.sortOn[arraySortItemIndex].split(".");
					for (var diveStepIndex = 0; diveStepIndex < diveIdSteps.length; diveStepIndex++) {
						
						const diveStep = diveIdSteps[diveStepIndex];

						if (diveStep.indexOf("[") === 0 && diveStep.indexOf("]") === diveStep.length - 1) {
							var arrayPos = diveStep.substring(1, diveStep.length - 1);
							emisPropertyToCheck = emisPropertyToCheck[arrayPos];
							psPropertyToCheck = psPropertyToCheck[arrayPos];
						} else {
							emisPropertyToCheck = emisPropertyToCheck[diveIdSteps[diveStepIndex]];
							psPropertyToCheck = psPropertyToCheck[diveIdSteps[diveStepIndex]];
						}
					}
				}

				if (emisPropertyToCheck === psPropertyToCheck){
					matchingPropertyCount = matchingPropertyCount + 1;
				}

			}

			if (matchingPropertyCount === arraySorter.sortOn.length) {
				emisSortedArray.push(emisItem)

				if (foundPsEntryIndexes.indexOf(psItemIndex) === -1){
					psSortedArray.push(psItem);
					foundPsEntryIndexes.push(psItemIndex);
				}
			} 
			// else {
			// 	if (unfoundEmisItems.indexOf(emisItem) === -1){
			// 		unfoundEmisItems.push(emisItem)
			// 	}
			// }
		}
	}

	if(id === "9B69EBA5-F541-4119-9142-0AA0F87E3758-PG0-HD1"){
		console.log(emisSortedArray.length)
		console.log(psSortedArray.length)
		console.log(unfoundEmisItems.length)
		console.log(unfoundPsItems.length)
	}

	for (var emisItemIndex = 0; emisItemIndex < emisArray.length; emisItemIndex++) {
		const emisItem = emisArray[emisItemIndex];
		if (emisSortedArray.indexOf(emisItem) === -1) {
			unfoundEmisItems.push(emisItem);
		} 
	}

	for (var psItemIndex = 0; psItemIndex < psArray.length; psItemIndex++) {
		const psItem = psArray[psItemIndex];
		if (psSortedArray.indexOf(psItem) === -1) {
			unfoundPsItems.push(psItem);
		} 
	}

	if(id === "9B69EBA5-F541-4119-9142-0AA0F87E3758-PG0-HD1"){
		console.log(emisSortedArray.length)
		console.log(psSortedArray.length)
		console.log(unfoundEmisItems.length)
		console.log(unfoundPsItems.length)
	}

	emisSortedArray = emisSortedArray.concat(unfoundEmisItems);
	psSortedArray = psSortedArray.concat(unfoundPsItems);

	return({emisSortedArray, psSortedArray})
}

function valueComparer(emisEntry, psEntry, emisProperty, parentId) {

	let compareResponse = "";
	if (emisEntry === psEntry) {
		if (displayMatches) {
			compareResponse = compareResponse + "MATCH: Emis Value of " + parentId + "." + emisProperty + " matches PS Property (" + emisEntry + ":" + psEntry + ")\n";
		}
	} else {
		compareResponse = compareResponse + "DIFF: Emis Value of " + parentId + "." + emisProperty + " does not match PS Property (" + emisEntry + " != " + psEntry + ")\n";
	}

	return compareResponse;
}

function resourceTypeCount(emisOutput, actualPSAdapterOutput) {

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

	return {
		resourceTypes: resourceTypePSOutput,
		psEntries: resourceEntriesByTypePS,
		emisEntries: resourceEntriesByTypeEMIS
	}
}


function scanForIdsAndReplace(emisOutputFileJson, psOutputFileJson) {

	
	let entriesByResource = resourceTypeCount(emisOutputFileJson, psOutputFileJson);
	let responsePsOutputFile = JSON.stringify(psOutputFileJson);
	let idsToReplace = [];

	for (var resourceTypeIndex = 0; resourceTypeIndex < entriesByResource.resourceTypes.length; resourceTypeIndex++) { //entriesByResource.resourceTypes.length

		let resourceType = entriesByResource.resourceTypes[resourceTypeIndex];

		switch (resourceType) {

			// only ever one instance of Patient and the ID's will never match by default
			case "Patient": {
				const emisEntry = entriesByResource.emisEntries[resourceTypeIndex][0].resource;
				const psEntry = entriesByResource.psEntries[resourceTypeIndex][0].resource;
				idsToReplace.push({ resourceType: resourceType, emisEntryId: emisEntry.id, psEntryId: psEntry.id }) 
				break;
			}
			case "AllergyIntolerance": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, allergyIntoleranceMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Location": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, locationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Condition": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, conditionMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Encounter": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, encounterMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "MedicationRequest": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, medicationRequestMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "MedicationStatement": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, medicationStatementMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Observation": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, observationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "ProcedureRequest": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, procedureRequestMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "List": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, listMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Medication": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, medicationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Practitioner": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, practionerMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
			case "Organization": {
				idsToReplace = idsToReplace.concat(findIdsToReplace(resourceType, resourceTypeIndex, organizationMatchFields, entriesByResource.emisEntries, entriesByResource.psEntries));
				break;
			}
		}
	}

	for (var idReplaceIndex = 0; idReplaceIndex < idsToReplace.length; idReplaceIndex++) {
		oldResponsefile = responsePsOutputFile;
		const regexSearch = new RegExp(idsToReplace[idReplaceIndex].psEntryId, 'g')
		responsePsOutputFile = responsePsOutputFile.replace(regexSearch, idsToReplace[idReplaceIndex].emisEntryId)
	}

	return JSON.parse(responsePsOutputFile);
}

function findIdsToReplace(resourceType, resourceTypeIndex, matchFields, emisEntries, psEntries) {

	let idsToReplace = []; // {resourceType, emisId, psId}

	for (var emisEntryCount = 0; emisEntryCount < emisEntries[resourceTypeIndex].length; emisEntryCount++) {
		const emisEntry = emisEntries[resourceTypeIndex][emisEntryCount].resource;
		let matchingFieldIndex = 0;

		var searchEntries = psEntries[resourceTypeIndex];
		var matchingEntries = [];
		var potentialMatchingEntries = [];

		// if there is more than one match, continue looking through the match properties 
		// until we are left with 0, 1 or no more matchFields
		// NOTE: if we only have 1 ps entry at the start, we should still compare
		while (
			searchEntries.length > 1
			|| (psEntries[resourceTypeIndex].length == 1 && searchEntries.length == 1)
		) {

			// if there ar eno more match options, we have not found our single match
			if (typeof matchFields[matchingFieldIndex] === 'undefined') {
				searchEntries = [];
			}

			for (var psEntryCount = 0; psEntryCount < searchEntries.length; psEntryCount++) {
				psEntry = searchEntries[psEntryCount];

				// grab the match field property values
				emisEntryMatchPropertyValue = getPropertyMatchByField(emisEntry, matchFields[matchingFieldIndex]);
				psEntryMatchPropertyValue = getPropertyMatchByField(psEntry.resource, matchFields[matchingFieldIndex]);

				//if we've found a match, log it.
				if (emisEntryMatchPropertyValue === psEntryMatchPropertyValue) {
					matchingEntries.push(psEntry);
				}
			}

			matchingFieldIndex = matchingFieldIndex + 1;
			searchEntries = [...matchingEntries];

			if (matchingEntries.length !== 0) {
				potentialMatchingEntries = [...searchEntries];
				matchingEntries = [];
			}

			// under the condition that we are only checking against one ps record, 
			// if we still have 1 remaining after all checks then we have found our match and 
			// need to exit our loop, due to the loop clause an escape/break is required
			if (psEntries[resourceTypeIndex].length == 1 && matchingFieldIndex === matchFields.length) {
				break;
			}
		}
		// if we have found a matching resource entry, compare the objects
		if (searchEntries.length == 1) {
			let emisEntryId = emisEntry.id
			const psEntryId = searchEntries[0].resource.id
			idsToReplace.push({ resourceType: resourceType, emisEntryId: emisEntryId, psEntryId: psEntryId })
		}
	}

	return idsToReplace;
}
