FROM symptoma/activemq:5.16.3
COPY --chown=activemq:activemq ./activemq.xml /conf/activemq.xml
COPY --chown=activemq:activemq ./jetty.xml conf/jetty.xml


EXPOSE 5672
EXPOSE 8161