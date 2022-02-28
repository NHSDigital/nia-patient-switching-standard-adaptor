#!/bin/bash
set -e;

basedir="$(pwd)"
dbName=patient_switching
releasePath=$1
snomedCtSchema=snomedct

if [ -z ${releasePath} ]
then
  echo "Please set the path to zipped SnomedCT RF2 release as the first argument, e.g. ./load_release-postgresql.sh uk_sct2cl_32.0.0_20210512000001Z.zip"
	exit -1
fi

if [ -z ${DB_OWNER_USERNAME} ]
then
  echo "Please set the following env var: DB_OWNER_USERNAME, e.g. \"export DB_OWNER_USERNAME='postgres'\""
	exit -1
fi

if [ -z ${DB_HOSTNAME} ]
then
  echo "Please set the following env var: DB_HOSTNAME, e.g. \"export DB_HOSTNAME='localhost'\""
	exit -1
fi

if [ -z ${DB_PORT} ]
then
  echo "Please set the following env var: DB_PORT, e.g. \"export DB_PORT='5432'\""
	exit -1
fi

if [ -z ${PGPASSWORD} ]
then
  echo "Please set the following env var: PGPASSWORD, e.g. \"export PGPASSWORD='********'\""
	exit -1
fi


#Unzip the files here, junking the structure
localExtract="tmp_extracted"
generatedLoadScript="tmp_loader.sh"
generatedEnvScript="tmp_environment-postgresql.sql"

fileTypes=(UKEDSnapshot)
unzip -j ${releasePath} "*UKEDSnapshot*" -d ${localExtract}
	
#Determine the release date from the filenames
releaseDate=`ls -1 ${localExtract}/*.txt | head -1 | egrep -o '[0-9]{8}'`	

function addLoadScript() {
	for fileType in ${fileTypes[@]}; do
		fileName=${1/TYPE/${fileType}}
		fileName=${fileName/DATE/${releaseDate}}

		#Check file exists - try beta version if not
		if [ ! -f ${localExtract}/${fileName} ]; then
			origFilename=${fileName}
			fileName="x${fileName}"
			if [ ! -f ${localExtract}/${fileName} ]; then
				echo "Unable to find ${origFilename} or beta version"
				exit -1
			fi
		fi

		tableName=${2}_s

		echo -e "psql -h ${DB_HOSTNAME} -p ${DB_PORT} -d ${dbName} -U ${DB_OWNER_USERNAME} -c \"\\\copy ${snomedCtSchema}.${tableName} FROM '${basedir}/${localExtract}/${fileName}' DELIMITER E'	' CSV HEADER QUOTE E'\b'\"\n" >> ${generatedLoadScript}
	done
}

echo -e "\nGenerating loading script for $releaseDate"
echo "#!/bin/bash" >> ${generatedLoadScript}
echo "# Generated Loader Script" >  ${generatedLoadScript}
chmod +x ${generatedLoadScript}

addLoadScript sct2_Description_TYPE-en_GB_DATE.txt description
addLoadScript der2_cRefset_LanguageTYPE-en_GB_DATE.txt langrefset

#create schema, tables, indexes
psql -h ${DB_HOSTNAME} -U ${DB_OWNER_USERNAME} -p ${DB_PORT} -d ${dbName} << EOF
	\ir create-database-postgres.sql;
EOF

#load data
./${generatedLoadScript}

#cleanup
rm -rf $localExtract
rm ${generatedLoadScript}
