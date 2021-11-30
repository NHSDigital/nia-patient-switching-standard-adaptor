#!/usr/bin/env bash

export PS_DB_URL="jdbc:postgresql://localhost:5436/patient_switching"
export POSTGRES_PASSWORD=
export PS_DB_OWNER_NAME="db_owner"
export PS_DB_OWNER_PASSWORD=
export GPC_USER_DB_PASSWORD=
export GP2GP_USER_DB_PASSWORD=
export GP2GP_SERVER_PORT="8085"
export GPC_SERVER_PORT="8080"
export PSS_QUEUE_NAME="pssQueue"
export MHS_QUEUE_NAME="mhsQueue"
export PSS_AMQP_BROKER="amqp://activemq:5672"
export MHS_AMQP_BROKER="amqp://activemq:5672"
export PSS_AMQP_MAX_REDELIVERIES="3"
export MHS_AMQP_MAX_REDELIVERIES="3"
