#!/usr/bin/env bash

export DB_PORT='5436';
export PS_DB_PORT='5436';
export HOSTNAME='localhost';

export PS_DB_URL="jdbc:postgresql://$HOSTNAME:$DB_PORT";
export PS_DB_OWNER_NAME="postgres";
export PS_FROM_ODS_CODE="PSS_001";
export PS_DB_OWNER_PASSWORD="123456";
export PS_DB_HOST="$HOSTNAME";
export POSTGRES_PASSWORD="$PS_DB_OWNER_PASSWORD";
export GPC_FACADE_USER_DB_PASSWORD="123456";
export GP2GP_TRANSLATOR_USER_DB_PASSWORD="123456";
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

#change path for snomedFile fath
export SNOMED_CT_TERMINOLOGY_FILE="/mnt/c/Users/yourUserName/yourPath/uk_sct2cl_32.10.0_20220216000001Z.zip";