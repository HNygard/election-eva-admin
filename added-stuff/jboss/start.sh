#!/bin/bash

echo "-------------------------------"
echo "Listing all EVA WAR files in /.m2/ (our Maven repo)."
find /.m2/ |grep eva |grep war

echo "-------------------------------"
echo "Copy admin-backend and admin-frontend to JBoss deployment folder."
cp /.m2/repository/no/valg/eva/admin-backend/2019.22-SNAPSHOT/admin-backend-2019.22-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/
#cp /.m2/repository/no/valg/eva/admin-frontend/2019.22-SNAPSHOT/admin-frontend-2019.22-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/
echo "... copied"

echo ""
echo "-------------------------------"
echo "Result in /opt/jboss/wildfly/standalone/deployments/ after copy."
ls -la /opt/jboss/wildfly/standalone/deployments/

echo "-------------------------------"
echo "Starting JBoss standalone."
/opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0