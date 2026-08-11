#!/usr/bin/env bash
#
# Deploys the built WARs to WildFly.
#
#   tools/wildfly.sh backend    # backend only (M2)
#   tools/wildfly.sh both       # backend and frontend (M3)
#   tools/wildfly.sh logs       # follow the server log
#   tools/wildfly.sh down       # stop everything, keeping the database
#
# Requires tools/build.sh to have produced the WARs first.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

COMPOSE=(docker compose -f docker/docker-compose.yml)
DEPLOYMENTS=docker/deployments
BACKEND_WAR=admin/admin-backend/target/admin-backend-2019.22-SNAPSHOT.war
FRONTEND_WAR=admin/admin-frontend/target/admin-frontend-2019.22-SNAPSHOT.war

mkdir -p "$DEPLOYMENTS"

case "${1:-backend}" in
backend | both)
	rm -f "$DEPLOYMENTS"/*.war

	[ -f "$BACKEND_WAR" ] || {
		echo "Missing $BACKEND_WAR -- run tools/build.sh first" >&2
		exit 1
	}
	# Deployed WITHOUT the version suffix, deliberately. An EJB's module name is
	# the deployment filename, and ServiceLookupUtil builds lookups of the form
	#   ejb:/admin-backend//<Service>!<FQCN>
	# so admin-backend-2019.22-SNAPSHOT.war would bind every one of the 190 EJBs
	# under a name the frontend's 73 producers cannot find. Renaming on the way
	# in fixes that without touching a release file. See NF-019.
	cp "$BACKEND_WAR" "$DEPLOYMENTS/admin-backend.war"

	if [ "$1" = "both" ]; then
		[ -f "$FRONTEND_WAR" ] || {
			echo "Missing $FRONTEND_WAR -- run tools/build.sh first" >&2
			exit 1
		}
		# Deployed as ROOT.war, so the frontend answers on "/" rather than
		# "/admin-frontend". Not a preference: the application generates
		# context-less absolute links. welcome.xhtml renders
		#   <a href="/tmpLogin?scanning=false">
		# and TmpLoginFilter does response.sendRedirect("/tmpLogin?..."), both of
		# which only resolve if the frontend is the root context. See NF-023.
		cp "$FRONTEND_WAR" "$DEPLOYMENTS/ROOT.war"
	fi

	echo "Deploying:"
	ls -la "$DEPLOYMENTS"/*.war

	"${COMPOSE[@]}" up -d --build
	echo
	echo "Follow the log with: tools/wildfly.sh logs"
	;;
logs)
	exec "${COMPOSE[@]}" logs -f eva-wildfly
	;;
down)
	exec "${COMPOSE[@]}" down
	;;
*)
	echo "usage: tools/wildfly.sh [backend|both|logs|down]" >&2
	exit 2
	;;
esac
