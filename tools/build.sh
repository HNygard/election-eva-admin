#!/usr/bin/env bash
#
# Milestone 1 build: seed the non-Central dependency, then build everything.
#
#   tools/build.sh              # skip tests (current M1 default)
#   tools/build.sh --with-tests # attempt tests -- expected to fail, see NF-002
#
# Tests are skipped by default because EvoteProperties aborts during class
# initialisation when EVOTE_PROPERTIES is unset, which takes down unrelated
# tests. That is NF-002, not something to paper over permanently.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WITH_TESTS=0

for arg in "$@"; do
	case "$arg" in
	--with-tests) WITH_TESTS=1 ;;
	*)
		echo "Unknown argument: $arg" >&2
		exit 2
		;;
	esac
done

"$REPO_ROOT/tools/seed-local-repo.sh"

if [ "$WITH_TESTS" -eq 1 ]; then
	exec "$REPO_ROOT/tools/mvn.sh" clean install
fi

exec "$REPO_ROOT/tools/mvn.sh" -Dmaven.test.skip clean install
