#!/usr/bin/env bash

cd "$GITHUB_WORKSPACE" || exit 1

echo "==== DECLARE PLUGINS VERSION ===="
export PLUGINS_VERSION
export RELEASE

if [[ "$GITHUB_REF_NAME" =~ [^v[0-9]+\.[0-9]+\.[0-9]] ]]; then
  echo "tag set -> set version to tag version"
  PLUGINS_VERSION=$(echo "$GITHUB_REF_NAME" | cut -d "v" -f 2)
else
  PLUGINS_VERSION=99.99.99
fi

echo "PLUGINS_VERSION is $PLUGINS_VERSION"

echo "==== INSTALL DEPENDENCIES ===="

echo -n "Downloading Java 13 ..."
wget -q https://cdn.azul.com/zulu/bin/zulu13.29.9-ca-jdk13.0.2-linux_x64.tar.gz
echo " done"
echo -n "Extracting Java 13 ..."
tar zxvf zulu13.29.9-ca-jdk13.0.2-linux_x64.tar.gz >/dev/null 2>&1
echo " done"
export JAVA_HOME=$GITHUB_WORKSPACE/zulu13.29.9-ca-jdk13.0.2-linux_x64
export PATH=$JAVA_HOME/bin:$PATH

echo "JAVA_HOME=$JAVA_HOME"
echo "PATH=$PATH"
echo "JAVA_VERSION"
java -version
echo "MVN_VERSION"
mvn -version

echo "==== SET PLUGIN VERSION ===="
mvn versions:set -DnewVersion="$PLUGINS_VERSION"

echo "==== BUILD PLUGINS ===="
mvn clean install
