# Getting started with Postman tests

## Pre-reqs before running Postman tests
1. Ensure you have built the docker containers by following the build process on the first README.md
2. Postman installed
3. Import the scripts into Postman `nia-patient-switching-standard-adaptor\test-suite\postman\Medicus Test Collection PSS Adaptor.postman_collection.json`
4. Certificates have been added to postman
    1. Open Postman / Settings (cog symbol)
    2. Go to Certificates tab inside Settings menu
    3. Turn on CA Certificates and add the `rootCA.pem` file from `nia-patient-switching-standard-adaptor\test-suite\postman\localhost-certificates`
    4. Click on Add certificate next to Client Certificates
    5. Add the `spineClient` file
    6. Add the `spintClient.key`
    7. Change the Host location to `localhost` : `443`
    8. Click on Add
    9. Close Settings menu

## Setting up test-suite adaptors to run Postman tests
1. Open project in IDE
2. Use the terminal and CD into folder `\nia-patient-switching-standard-adaptor\test-suite`
3. Run the code `sudo ./start-test-environment.sh`
4. Allow the system to build adaptors
5. After building your docker suite should look something like this (containers can appear in any order):

<details>
    ```
    - test-suite (expand the folder)
		- ps_gp2gp_translator-1
		- gpc_facade-1
		- mock-spine-mhs-1
		- inbound-1
		- ps_db-1
		- outbound-1
		- activemq-1
		- redis-1
		- dynamodb-1
    ```
</details>

## Running the tests (If you are intending run the postman scripts locally then follow these steps)
1. Ensure test-suite environment is setup from steps above
2. Turn off both the `ps_gp2gp_transaltor-1` and `gpc_facade-1` in dock desktop
3. Open the project:`nia-patient-switching-standard-adaptor`
4. Navigate to `nia-patient-switching-standard-adaptor\gp2gp-translator\src\main\java\uk\nhs\adaptors\pss\translator\Gp2gpTranslatorApplication.java`
5. Run `Gp2gpTranslatorApplication.java` with the environment variables below:

<details>

   ```   
   GP2GP_TRANSLATOR_USER_DB_PASSWORD=123456;
   MHS_AMQP_PASSWORD=admin;
   MHS_AMQP_USERNAME=admin;
   PS_AMQP_PASSWORD=admin;
   PS_AMQP_USERNAME=admin;
   SUPPORTED_FILE_TYPES=application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/msoutlook,text/rtf,text/plain,image/bmp,image/gif,image/jpeg,image/png,image/tiff,application/xml,audio/x-aiff,audio/x-mpegurl,audio/mpeg,audio/x-wav,audio/x-ms-wma,video/3gpp2,video/3gpp,video/x-ms-asf,video/x-ms-asf,video/x-msvideo,video/x-flv,video/quicktime,video/mp4,video/mpeg,audio/vnd.rn-realaudio,application/x-shockwave-flash,video/x-ms-vob,video/x-ms-wmv,application/postscript,application/postscript,image/svg+xml,image/x-pict,application/pdf,application/vnd.openxmlformats-package.relationships+xml,text/css,text/html,application/xhtml+xml,text/plain,application/json,text/xml,application/xml,application/pdf,audio/basic,audio/mpeg,image/png,image/gif,image/jpeg,image/tiff,video/mpeg,application/msword,application/octet-stream,text/csv,application/dicom,application/zip,application/x-rar-compressed,application/x-gzip,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/x-mplayer2,audio/x-au,application/x-troff-msvideo,video/msvideo,image/x-windows-bmp,application/pkix-cert,application/x-x509-ca-cert,application/cdf,application/x-cdf,application/x-netcdf,application/x-x509-user-cert,application/EDIFACT,application/EDI-X12,application/EDI-Consent,application/hl7-v2,application/hl7-v2+xml,video/x-mpeg,application/pkcs10,application/x-pkcs10,application/pkcs-12,application/x-pkcs12,application/x-pkcs7-signature,application/pkcs7-mime,application/x-pkcs7-mime,application/pkcs7-mime,application/x-pkcs7-mime,application/x-pkcs7-certreqresp,application/pkcs7-signature,application/x-rtf,application/x-compressed,application/x-zip-compressed,multipart/x-zip,application/pgp,application/pgp-keys,application/pgp-signature,application/x-pgp-plugin,application/pgp-encrypted,audio/wav,audio/wave,audio/x-pn-wav,chemical/x-mdl-sdfile,chemical/x-mdl-molfile,chemical/x-pdb,application/x-hl7

   DB_PORT: '5436'
   PS_DB_PORT: '5436'
   HOSTNAME: 'localhost'
   
   PS_DB_URL: "jdbc:postgresql://localhost:5436"
   PS_DB_OWNER_NAME: "postgres"
   PS_FROM_ODS_CODE: "PSS_001"
   PS_DB_OWNER_PASSWORD: "123456"
   PS_DB_HOST: "localhost"
   POSTGRES_PASSWORD: "123456"
   GPC_FACADE_USER_DB_PASSWORD: "123456"
   GP2GP_TRANSLATOR_USER_DB_PASSWORD: "123456"
   GP2GP_TRANSLATOR_SERVER_PORT: "8085"
   GPC_FACADE_SERVER_PORT: "8081"
   PS_QUEUE_NAME: "pssQueue"
   MHS_QUEUE_NAME: "mhsQueue"
   PS_AMQP_MAX_REDELIVERIES: "3"
   MHS_AMQP_MAX_REDELIVERIES: "3"
   MHS_BASE_URL: "http://localhost:8084/"
   PS_LOGGING_LEVEL: "DEBUG"
   
   PS_AMQP_USERNAME: "admin"
   PS_AMQP_PASSWORD: "admin"
   
   MHS_AMQP_USERNAME: "admin"
   MHS_AMQP_PASSWORD: "admin"
   
   SSL_ENABLED: 'false'
   KEY_STORE: '/certs/keystore.jks'
   TRUST_STORE: '/certs/truststore.jks'
   
   SDS_API_KEY: "Nn8Y3sofb3B2PuhFA7EOzr8wQguoMTMG"
   SDS_BASE_URL: "https://int.api.service.nhs.uk/spine-directory/FHIR/R4/"
   
   TIMEOUT_CRON_TIME: '0 0 */6 * * *'
   #TIMEOUT_CRON_TIME: '*/30 * * * * *'
   TIMEOUT_SDS_POLL_FREQUENCY: "3"
   
   #changepathforsnomedFilepath
   SNOMED_CT_TERMINOLOGY_FILE: "/mnt/c/Users/BenjaminHession/Documents/NIA/uk_sct2mo_36.0.0_20230412000001Z.zip"
   
   SUPPORTED_FILE_TYPES: "application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/msoutlook,text/rtf,text/plain,image/bmp,image/gif,image/jpeg,image/png,image/tiff,application/xml,audio/x-aiff,audio/x-mpegurl,audio/mpeg,audio/x-wav,audio/x-ms-wma,video/3gpp2,video/3gpp,video/x-ms-asf,video/x-ms-asf,video/x-msvideo,video/x-flv,video/quicktime,video/mp4,video/mpeg,audio/vnd.rn-realaudio,application/x-shockwave-flash,video/x-ms-vob,video/x-ms-wmv,application/postscript,application/postscript,image/svg+xml,image/x-pict,application/pdf,application/vnd.openxmlformats-package.relationships+xml,text/css,text/html,application/xhtml+xml,text/plain,application/json,text/xml,application/xml,application/pdf,audio/basic,audio/mpeg,image/png,image/gif,image/jpeg,image/tiff,video/mpeg,application/msword,application/octet-stream,text/csv,application/dicom,application/zip,application/x-rar-compressed,application/x-gzip,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/x-mplayer2,audio/x-au,application/x-troff-msvideo,video/msvideo,image/x-windows-bmp,application/pkix-cert,application/x-x509-ca-cert,application/cdf,application/x-cdf,application/x-netcdf,application/x-x509-user-cert,application/EDIFACT,application/EDI-X12,application/EDI-Consent,application/hl7-v2,application/hl7-v2+xml,video/x-mpeg,application/pkcs10,application/x-pkcs10,application/pkcs-12,application/x-pkcs12,application/x-pkcs7-signature,application/pkcs7-mime,application/x-pkcs7-mime,application/pkcs7-mime,application/x-pkcs7-mime,application/x-pkcs7-certreqresp,application/pkcs7-signature,application/x-rtf,application/x-compressed,application/x-zip-compressed,multipart/x-zip,application/pgp,application/pgp-keys,application/pgp-signature,application/x-pgp-plugin,application/pgp-encrypted,audio/wav,audio/wave,audio/x-pn-wav,chemical/x-mdl-sdfile,chemical/x-mdl-molfile,chemical/x-pdb,application/x-hl7"
   
   #PS_DAISY_CHAINING_ACTIVE: "true"
   
   #STORAGE_TYPE: S3
   #STORAGE_REGION: eu-west-2
   #STORAGE_CONTAINER_NAME: ps-adaptor-storage
   #STORAGE_SECRET: +BkMFhS8BBaMf7C0nyrJu5OddCoOu4E7xxltK262
   #STORAGE_REFERENCE: AKIAVGJUWOQEZSQILVGL
   
   #STORAGE_SECRET: +BkMFhS8BBaMf7C0nyrJu5OddCoOu4E7xxltK # this is wrong for testing
   
   ```

</details>

6. Navigate to `nia-patient-switching-standard-adaptor\gpc-api-facade\src\main\java\uk\nhs\adaptors\pss\gpc\GpcFacadeApplication.java`
7. Run `GpcFacadeApplication.java` with the environment variables below:

<details>
```
GPC_FACADE_USER_DB_PASSWORD=123456
```
</details>

8. Open Postman and run through the tests


### Troubleshooting:
- Check that both `Gp2gpTranslatorApplication.java` + `GpcFacadeApplication.java` are running locally
- Check the environment variables
- Check that both the `ps_gp2gp_translator-1` + `gpc_facade-1` have stopped running
- Check the certificates in postman
