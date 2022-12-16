#!/usr/bin/env bash

export MHS_INBOUND_VERSION="nhsdev/nia-mhs-inbound:1.2.4-arm64"
export MHS_OUTBOUND_VERSION="nhsdev/nia-mhs-outbound:1.2.4-arm64"
export MHS_ROUTE_VERSION="nhsdev/nia-mhs-route:1.2.2" #Not Used in test version

export PS_TRANSLATOR_VERSION="nhsdev/nia-ps-adaptor:0.4-arm64"
export PS_FACADE_VERSION="nhsdev/nia-ps-facade:0.3-arm64"
export PS_DB_MIGRATION_VERSION="nhsdev/nia-ps-db-migration:0.2"

export ACTIVEMQ_VERSION="nhsdev/nia-ps-activemq:0.1"
export POSTGRES_VERSION="postgres:14.0"
export DYNAMODB_VERSION="nhsdev/nia-dynamodb-local:1.0.3"
export REDIS_VERSION="redis"
export MONGODB_VERSION="mongo"
