#!/usr/bin/env bash
#
# Installs the one dependency that is not on Maven Central.
#
# jcoord 1.0 is not published anywhere reachable; the release ships the JAR in
# admin-maven-repository/ instead. Without this, the build cannot resolve it.
# See docs/not-fixed-yet/NF-001-jcoord-not-on-maven-central.md
#
# Idempotent: re-running just overwrites the same artifact in the volume.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR=admin-maven-repository/uk/org/mygrid/resources/jcoord/jcoord/1.0/jcoord-1.0.jar

if [ ! -f "$REPO_ROOT/$JAR" ]; then
	echo "Expected bundled JAR is missing: $JAR" >&2
	exit 1
fi

exec "$REPO_ROOT/tools/mvn.sh" install:install-file \
	-Dfile="/usr/src/eva-admin/$JAR" \
	-DgroupId=uk.org.mygrid.resources.jcoord \
	-DartifactId=jcoord \
	-Dversion=1.0 \
	-Dpackaging=jar \
	-DgeneratePom=true
