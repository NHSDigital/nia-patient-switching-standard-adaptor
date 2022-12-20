export PS_DAISY_CHAINING_ACTIVE="true"

####################################
# GP2GP VARS

export GP2GP_SERVER_PORT="8183"
export GP2GP_MHS_OUTBOUND_URL="http://outbound:80"

export GP2GP_MONGO_URI="mongodb://mongodb:27017"
export GP2GP_MONGO_DATABASE_NAME="gp2gp"

export GP2GP_LARGE_ATTACHMENT_THRESHOLD="31216"
export GP2GP_LARGE_EHR_EXTRACT_THRESHOLD="31216"

export GP2GP_GPC_GET_URL="http://gpcc:8090/@ODS_CODE@/STU3/1/gpconnect"

export GP2GP_AMQP_BROKERS="amqp://activemq:5672";
export GP2GP_MHS_INBOUND_QUEUE="gp2gpInboundQueue";
export GP2GP_AMQP_USERNAME="admin";
export GP2GP_AMQP_PASSWORD="admin";

####################################
#GP CONNECT CONSUMER ADAPTOR VARS
export GPC_CONSUMER_SERVER_PORT="8090"
export GPC_CONSUMER_SDS_URL="http://wiremock:8080/spine-directory/"
export GPC_CONSUMER_SDS_APIKEY="anykey"