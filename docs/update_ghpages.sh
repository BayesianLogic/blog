#!/bin/bash

# Updates the github-pages repo with the latest docs.
# Expects to be executed from the blog root dir.
# Expects the github-pages repo to be in the following dir:
GHPAGES_DIR=../bayesianlogic.github.io

set -e

COMMITHASH=$(git rev-parse HEAD)
rm -r $GHPAGES_DIR/*
(cd docs; make html)
cp -r docs/output/* $GHPAGES_DIR/
cd $GHPAGES_DIR
git add --all
git commit -m "generated docs from blog commit ${COMMITHASH}"
git push origin master
