#!/bin/bash

# Updates the github-pages repo with the latest docs.
# Expects to be executed from the blog root dir.
# Expects the github-pages repo to be in the following dir:
# be sure to run from sbt/sbt ghpages, since this depends on sbt html task
GHPAGES_DIR=../bayesianlogic.github.io

set -e

COMMITHASH=$(git rev-parse HEAD)
# rm -r $GHPAGES_DIR/*
mkdir -p target/pelican/download
cp target/universal/*.zip target/pelican/download/
cp target/*.deb target/pelican/download/
cp tools/blog.el target/pelican/download/
cp tools/blog.vim target/pelican/download/
(cd tools ; zip -r blog-for-sublime blog-for-sublime)
mv tools/blog-for-sublime.zip target/pelican/download/
(cd tools ; zip -r blog_py_lexer blog_py_lexer)
mv tools/blog_py_lexer.zip target/pelican/download/
cp -r target/pelican/* $GHPAGES_DIR/
cd $GHPAGES_DIR
git add --all
git commit -m "generated docs from blog commit ${COMMITHASH}"
git push origin master
