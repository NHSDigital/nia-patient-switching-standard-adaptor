#!/usr/bin/env bash
export PS_DB_HOST="ps_db"
export PS_DB_PORT="5432"
export PS_DB_URL="jdbc:postgresql://${PS_DB_HOST}:${PS_DB_PORT}"
export PS_DB_OWNER_NAME="postgres"
export POSTGRES_PASSWORD="pass_test"
export GPC_FACADE_USER_DB_PASSWORD="pass_test"
export GP2GP_TRANSLATOR_USER_DB_PASSWORD="pass_test"
export GP2GP_TRANSLATOR_SERVER_PORT="8085"
export GPC_FACADE_SERVER_PORT="8081"
export PS_QUEUE_NAME="pssQueue"
export MHS_QUEUE_NAME="mhsQueue"
export PS_AMQP_MAX_REDELIVERIES="3"
export MHS_AMQP_MAX_REDELIVERIES="3"
export PS_AMQP_BROKER="amqp://activemq:5672"
export MHS_AMQP_BROKER="amqp://activemq:5672"
export MHS_BASE_URL="http://mhs-adaptor-mock:8080/"
export PS_LOGGING_LEVEL="DEBUG"

export STORAGE_TYPE="LocalMock";
export STORAGE_REGION="";
export STORAGE_CONTAINER_NAME=""; #Local Bucket or Azure Container name
export STORAGE_REFERENCE=""; #Azure blob name or AWS user reference”
export STORAGE_SECRET=""; #Secret Key

export SDS_API_key=""
export SDS_BASE_URL="https://int.api.service.nhs.uk/spine-directory/FHIR/R4/"
