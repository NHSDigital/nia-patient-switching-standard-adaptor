const fs = require('fs');
let filePath = "../comparison-reports/report--PWTP6-output.json/entires/";
let fileName1 = "ps-Observation-entries.json";
let fileName2 = "emis-Observation-entries.json";
let fileAsString1 = fs.readFileSync(filePath + fileName1, 'utf8');
let fileAsString2 = fs.readFileSync(filePath + fileName2, 'utf8');


let jsonPs = JSON.parse(fileAsString1);   
let jsonEMIS = JSON.parse(fileAsString2); 


jsonEMIS = sortResourcesId(jsonPs, jsonEMIS);

console.log(JSON.stringify(jsonEMIS));

//printResourceIds(jsonEMIS);
printResourceIds(jsonPs);



function sortResourcesId(jsonEMIS, jsonPs){
	for (var i = 0; i < jsonEMIS.entry.length; i++) {//97
		jsonPs = { ... changeResourcePosition(i, jsonEMIS.entry[i].resource.id, jsonPs)};
	}
	return jsonPs;
}

function changeResourcePosition(index, id, json){
	for (var i = 0; i < json.entry.length; i++) { //105
		if(id === json.entry[i].resource.id){

			let oldPosition = { ...json.entry[i] } ;
			let newPosition = { ...json.entry[index]};
			json.entry[i] = newPosition;
			json.entry[index] = oldPosition; 
			return json;
		}
	}
	return json;
}

function printResourceIds(json){
	for (var i = 0; i < json.entry.length; i++) {//97
		console.log((i+1) + " = " + json.entry[i].resource.id);
	}
}