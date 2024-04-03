#!/usr/bin/env bash

export MHS_INBOUND_VERSION="nhsdev/nia-mhs-inbound:1.3.0"
export MHS_OUTBOUND_VERSION="nhsdev/nia-mhs-outbound:1.3.0"

export PS_TRANSLATOR_VERSION="docker-gp2gp_translator:latest"
export PS_FACADE_VERSION="docker-gpc_facade:latest"
export PS_DB_MIGRATION_VERSION="docker-db_migration:latest"

export ACTIVEMQ_VERSION="nhsdev/nia-ps-activemq:0.1-amd64" # has an arm64 and amd64 version
export POSTGRES_VERSION="postgres:14.0"
export DYNAMODB_VERSION="nhsdev/nia-dynamodb-local:1.0.3"
export REDIS_VERSION="redis"
