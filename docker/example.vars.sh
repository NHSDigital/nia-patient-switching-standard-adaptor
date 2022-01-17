#!/usr/bin/env bash

export PS_DB_URL="jdbc:postgresql://localhost:5436/patient_switching"
export POSTGRES_PASSWORD=
export PS_DB_OWNER_NAME="db_owner"
export PS_DB_OWNER_PASSWORD=
export PS_FROM_ODS_CODE="PSS_001"

export GPC_FACADE_USER_DB_PASSWORD=
export GPC_FACADE_SERVER_PORT="8081"

export GP2GP_TRANSLATOR_USER_DB_PASSWORD=
export GP2GP_TRANSLATOR_SERVER_PORT="8085"

export PS_QUEUE_NAME="pssQueue"
export PS_AMQP_MAX_REDELIVERIES="3"

export MHS_AMQP_MAX_REDELIVERIES="3"
export MHS_QUEUE_NAME="mhsQueue"
export MHS_OUTBOUND_URL="http://localhost:8080/mhs/outbound"
