#!/bin/bash
#
# Copies whatever WARs are mounted at /deployments into WildFly and starts it.
#
# The WARs are bind-mounted from the build output rather than baked into the
# image, so a rebuild is picked up by restarting the container.

set -e

DEPLOY_DIR=/opt/jboss/wildfly/standalone/deployments

if [ -d /deployments ]; then
	echo "--- WARs offered for deployment ---"
	ls -la /deployments/*.war 2>/dev/null || echo "(none)"
	cp -f /deployments/*.war "$DEPLOY_DIR/" 2>/dev/null || true
fi

echo "--- deployments directory ---"
ls -la "$DEPLOY_DIR/"

echo "--- starting WildFly ---"
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
