FROM rmohr/activemq:5.15.9-alpine
COPY --chown=activemq:activemq activemq.xml /opt/activemq/config
COPY --chown=activemq:activemq jetty.xml /opt/jetty.xml

EXPOSE 5672
EXPOSE 8161
