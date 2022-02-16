#!/usr/bin/env bash

cd "$GITHUB_WORKSPACE" || exit 1

echo "==== DECLARE PLUGINS VERSION ===="
export PLUGINS_VERSION
export RELEASE

if [[ "$GITHUB_REF" =~ [^v[0-9]+\.[0-9]+\.[0-9]] ]]; then
  echo "tag set -> set version to tag version"
  RELEASE=1
  PLUGINS_VERSION=$(echo "$GITHUB_REF" | cut -d "v" -f 2)
else
  RELEASE=0
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

if [ $RELEASE -eq 1 ]; then

  export LAST_RELEASE_TAG=$(curl -H "Accept: application/vnd.github.v3+json" "https://api.github.com/repos/EXXETA/correomqtt-plugins/releases" | python2 -c "import sys, json; print(json.load(sys.stdin)[0]['tag_name'])")
  export LAST_RELEASE_COMMIT=$(git rev-list -n 1 $LAST_RELEASE_TAG)

  for i in $(find . -maxdepth 1 -not -path '*/\.*' -type d -printf '%P ')
  do
    cd $i
    export COMMIT_OF_FOLDER=$(git log --pretty=tformat:"%H" -n1 . | cat)
    if [ $COMMIT_OF_FOLDER != $LAST_RELEASE_COMMIT ]; then
      mvn clean install
    fi
  done
else
    mvn clean install
fi

