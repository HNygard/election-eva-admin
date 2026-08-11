# WildFly 13.0.0.Final -- the last Java EE 7 full server, matching the release's
# javaee-api 7.0 and the jboss-ejb-client 2.x style lookups that 73 frontend
# producers depend on. See docs/decisions/D009-wildfly-13-for-java-ee-7.md

FROM eclipse-temurin:8-jdk-jammy

ENV WILDFLY_VERSION 13.0.0.Final
ENV POSTGRES_DRIVER_VERSION 42.2.5
ENV JBOSS_HOME /opt/jboss/wildfly

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/jboss \
    && curl -fL "https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz" \
       -o /tmp/wildfly.tar.gz \
    && tar xf /tmp/wildfly.tar.gz -C /opt/jboss \
    && mv "/opt/jboss/wildfly-$WILDFLY_VERSION" "$JBOSS_HOME" \
    && rm /tmp/wildfly.tar.gz

# Version pinned to the release's own postgresql dependency, so the driver in the
# server matches the one the build resolved.
RUN curl -fL "https://repo1.maven.org/maven2/org/postgresql/postgresql/$POSTGRES_DRIVER_VERSION/postgresql-$POSTGRES_DRIVER_VERSION.jar" \
    -o /tmp/postgresql.jar

# Install the driver as a proper module rather than dropping it in deployments/,
# so the datasource can be defined up front instead of depending on deployment
# ordering.
RUN $JBOSS_HOME/bin/jboss-cli.sh --commands="module add --name=org.postgresql --resources=/tmp/postgresql.jar --dependencies=javax.api\,javax.transaction.api" \
    && rm /tmp/postgresql.jar

COPY standalone-eva.cli /opt/jboss/standalone-eva.cli
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=/opt/jboss/standalone-eva.cli \
    && rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history

# Management user, so the console and CLI are usable from outside.
RUN $JBOSS_HOME/bin/add-user.sh admin Admin#70365 --silent

COPY start.sh /opt/jboss/start.sh
RUN chmod +x /opt/jboss/start.sh

# 8080 application, 9990 management
EXPOSE 8080 9990

CMD ["/opt/jboss/start.sh"]
