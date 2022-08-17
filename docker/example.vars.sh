#!/usr/bin/env bash

export DB_PORT='5436';
export PS_DB_PORT='5436';
export HOSTNAME='localhost';

export PS_DB_URL="jdbc:postgresql://$HOSTNAME:$DB_PORT";
export PS_DB_OWNER_NAME="postgres";
export PS_FROM_ODS_CODE="PSS_001";
export PS_DB_OWNER_PASSWORD=;
export PS_DB_HOST="$HOSTNAME";
export POSTGRES_PASSWORD=;
export GPC_FACADE_USER_DB_PASSWORD=;
export GP2GP_TRANSLATOR_USER_DB_PASSWORD=;
export GP2GP_TRANSLATOR_SERVER_PORT="8085";
export GPC_FACADE_SERVER_PORT="8081";
export PS_QUEUE_NAME="pssQueue";
export MHS_QUEUE_NAME="mhsQueue";
export PS_AMQP_MAX_REDELIVERIES="3";
export MHS_AMQP_MAX_REDELIVERIES="3";
export MHS_BASE_URL="http://$HOSTNAME:8080/";
export PS_LOGGING_LEVEL="DEBUG";

export PS_AMQP_USERNAME="admin";
export PS_AMQP_PASSWORD="admin";

export MHS_AMQP_USERNAME="admin";
export MHS_AMQP_PASSWORD="admin";

export SSL_ENABLED=false;
export KEY_STORE=/certs/keystore.jks;
export TRUST_STORE=/certs/truststore.jks;

export STORAGE_TYPE="LocalMock";
export STORAGE_REGION="";
export STORAGE_CONTAINER_NAME=""; #Local Bucket or Azure Container name
export STORAGE_REFERENCE=""; #Azure blob name or AWS user reference”
export STORAGE_SECRET=""; #Secret Key

export SDS_API_key=""
export SDS_BASE_URL="https://int.api.service.nhs.uk/spine-directory/FHIR/R4/"

export SUPPORTED_FILE_TYPES="application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/msoutlook,text/rtf,text/plain,image/bmp,image/gif,image/jpeg,image/png,image/tiff,application/xml,audio/x-aiff,audio/x-mpegurl,audio/mpeg,audio/x-wav,audio/x-ms-wma,video/3gpp2,video/3gpp,video/x-ms-asf,video/x-ms-asf,video/x-msvideo,video/x-flv,video/quicktime,video/mp4,video/mpeg,audio/vnd.rn-realaudio,application/x-shockwave-flash,video/x-ms-vob,video/x-ms-wmv,application/postscript,application/postscript,image/svg+xml,image/x-pict,application/pdf,application/vnd.openxmlformats-package.relationships+xml,text/css,text/html,application/xhtml+xml,text/plain,application/json,text/xml,application/xml,application/pdf,audio/basic,audio/mpeg,image/png,image/gif,image/jpeg,image/tiff,video/mpeg,application/msword,application/octet-stream,text/csv,application/dicom,application/zip,application/x-rar-compressed,application/x-gzip,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/x-mplayer2,audio/x-au,application/x-troff-msvideo,video/msvideo,image/x-windows-bmp,application/pkix-cert,application/x-x509-ca-cert,application/cdf,application/x-cdf,application/x-netcdf,application/x-x509-user-cert,application/EDIFACT,application/EDI-X12,application/EDI-Consent,application/hl7-v2,application/hl7-v2+xml,video/x-mpeg,application/pkcs10,application/x-pkcs10,application/pkcs-12,application/x-pkcs12,application/x-pkcs7-signature,application/pkcs7-mime,application/x-pkcs7-mime,application/pkcs7-mime,application/x-pkcs7-mime,application/x-pkcs7-certreqresp,application/pkcs7-signature,application/x-rtf,application/x-compressed,application/x-zip-compressed,multipart/x-zip,application/pgp,application/pgp-keys,application/pgp-signature,application/x-pgp-plugin,application/pgp-encrypted,audio/wav,audio/wave,audio/x-pn-wav,chemical/x-mdl-sdfile,chemical/x-mdl-molfile,chemical/x-pdb,application/x-hl7"

#change path for snomedFile fath
#export SNOMED_CT_TERMINOLOGY_FILE="/mnt/c/Users/yourUserName/yourPath/uk_sct2cl_32.10.0_20220216000001Z.zip";