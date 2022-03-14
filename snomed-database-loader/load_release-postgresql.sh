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

if [ -z ${PS_DB_OWNER_NAME} ]
then
  echo "Please set the following env var: PS_DB_OWNER_NAME, e.g. \"export PS_DB_OWNER_NAME='postgres'\""
	exit -1
fi

if [ -z ${PS_DB_HOST} ]
then
  echo "Please set the following env var: PS_DB_HOST, e.g. \"export PS_DB_HOST='localhost'\""
	exit -1
fi

if [ -z ${PS_DB_PORT} ]
then
  echo "Please set the following env var: PS_DB_PORT, e.g. \"export PS_DB_PORT='5432'\""
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

fileTypes=(Snapshot)
unzip -j ${releasePath} "*Snapshot*" -d ${localExtract}

#Determine the release date from the filenames
releaseDateINT=`ls -1 ${localExtract}/*INT*.txt | head -1 | egrep -o '[0-9]{8}'`
releaseDateUK=`ls -1 ${localExtract}/*UKEDSnapshot*.txt | head -1 | egrep -o '[0-9]{8}'`

function addLoadScript() {
	fileName=${1/TYPE/${2}}
	fileName=${fileName/DATE/${4}}
	#Check file exists - try beta version if not
	if [ ! -f ${localExtract}/${fileName} ]; then
		origFilename=${fileName}
		fileName="x${fileName}"
		if [ ! -f ${localExtract}/${fileName} ]; then
			echo "Unable to find ${origFilename} or beta version"
			exit -1
		fi
	fi
	tableName=${3}_s
	echo -e "psql -h ${PS_DB_HOST} -p ${PS_DB_PORT} -d ${dbName} -U ${PS_DB_OWNER_NAME} -c \"\\\copy ${snomedCtSchema}.${tableName} FROM '${basedir}/${localExtract}/${fileName}' DELIMITER E'	' CSV HEADER QUOTE E'\b'\"\n" >> ${generatedLoadScript}
}

echo -e "\nGenerating loading script for releaseDateINT"
echo "#!/bin/bash" >> ${generatedLoadScript}
echo "# Generated Loader Script" >  ${generatedLoadScript}
chmod +x ${generatedLoadScript}

addLoadScript sct2_Description_TYPE-en_GB_DATE.txt UKEDSnapshot description $releaseDateUK
addLoadScript sct2_Description_TYPE-en_INT_DATE.txt Snapshot description $releaseDateINT
addLoadScript der2_cRefset_LanguageTYPE-en_GB_DATE.txt UKEDSnapshot langrefset $releaseDateUK

#create schema, tables, indexes
psql -h ${PS_DB_HOST} -U ${PS_DB_OWNER_NAME} -p ${PS_DB_PORT} -d ${dbName} << EOF
	\ir create-database-postgres.sql;
EOF

#load data
./${generatedLoadScript}

#cleanup
rm -rf $localExtract
rm ${generatedLoadScript}
