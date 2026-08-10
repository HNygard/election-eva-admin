#!/usr/bin/env bash
#
# Runs Maven inside Docker. Nothing is installed on the host (D003).
#
#   tools/mvn.sh -Dmaven.test.skip clean install
#   tools/mvn.sh -pl admin/admin-common test
#
# Pinned to JDK 8 on purpose: the poms target 1.8, and Surefire 2.20.1 throws a
# NullPointerException under JDK 11 (docs/findings/2026-08-10-baseline-survey.md).
#
# Maven runs as the invoking user so that target/ directories in the bind mount
# do not come back owned by root.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAVEN_IMAGE="${MAVEN_IMAGE:-maven:3-jdk-8}"
MAVEN_VOLUME="${MAVEN_VOLUME:-eva-admin-maven-repo}"
CONTAINER_HOME=/var/maven

if ! docker volume inspect "$MAVEN_VOLUME" >/dev/null 2>&1; then
	echo "Creating Maven repository volume '$MAVEN_VOLUME'"
	docker volume create --name "$MAVEN_VOLUME" >/dev/null
	# A fresh volume is root-owned; hand it to the invoking user once.
	docker run --rm -v "$MAVEN_VOLUME:$CONTAINER_HOME/.m2" "$MAVEN_IMAGE" \
		chown -R "$(id -u):$(id -g)" "$CONTAINER_HOME/.m2"
fi

TTY_FLAGS=()
[ -t 0 ] && TTY_FLAGS=(-t)

exec docker run --rm -i "${TTY_FLAGS[@]}" \
	--user "$(id -u):$(id -g)" \
	--volume "$MAVEN_VOLUME:$CONTAINER_HOME/.m2" \
	--volume "$REPO_ROOT:/usr/src/eva-admin" \
	--workdir /usr/src/eva-admin \
	--env "MAVEN_CONFIG=$CONTAINER_HOME/.m2" \
	"$MAVEN_IMAGE" \
	mvn -Duser.home="$CONTAINER_HOME" "$@"
