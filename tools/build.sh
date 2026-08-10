#!/usr/bin/env bash
#
# Milestone 1 build: seed the non-Central dependency, then build everything.
#
#   tools/build.sh              # build without running tests (current M1 default)
#   tools/build.sh --with-tests # also run them -- expected to fail, see NF-002
#
# Tests are not run by default because EvoteProperties aborts during class
# initialisation when EVOTE_PROPERTIES is unset, which takes down unrelated
# tests. That is NF-002, not something to paper over permanently.
#
# -DskipTests, NOT -Dmaven.test.skip. The difference matters here: 13 modules
# depend on test-jars produced by 11 others, and -Dmaven.test.skip skips test
# *compilation*, so those test-jars never exist and the reactor fails to resolve
# them. -DskipTests compiles tests and simply does not run them.
#
# This is what origin/attempt worked around by deleting test-scoped dependencies
# from 13 poms (13fba0b). The flag does the same job and changes no files.

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

exec "$REPO_ROOT/tools/mvn.sh" -DskipTests clean install
