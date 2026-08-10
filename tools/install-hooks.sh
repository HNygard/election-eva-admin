#!/usr/bin/env bash
#
# Points git at tools/githooks so the manifest check runs on every commit.
# Hooks cannot live in .git/hooks and be version controlled, so this has to be
# run once per clone.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

git config core.hooksPath tools/githooks
chmod +x tools/githooks/* tools/*.sh

echo "core.hooksPath = $(git config core.hooksPath)"
echo "Manifest check will now run on every commit."
