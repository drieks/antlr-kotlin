#!/bin/bash

set -eu

# JS tests will fail because no headless chrome can be found, so we skip tests
ignore="-x jsBrowserTest"
extra=""
if [ -n "$*" ]; then
  extra="$extra $*"
fi

echo "INFO: $(date) running clean"
./gradlew clean

echo "INFO: $(date) running test"
./gradlew test $extra $ignore

echo "INFO: $(date) running publishToMavenLocal"
./gradlew publishToMavenLocal $extra $ignore

echo "INFO: $(date) running check for antlr-kotlin-examples-jvm"
cd antlr-kotlin-examples-jvm
../gradlew --info clean check $extra
cd ..

echo "INFO: $(date) running check for antlr-kotlin-examples-mpp"
cd antlr-kotlin-examples-mpp
../gradlew --info clean check $extra
cd ..

if [ -n "${MAVEN_PASSWORD:-}" ]; then
  echo "INFO: $(date) running publish"
  ./gradlew publish $extra $ignore
fi

#  - cd antlr-kotlin-examples-js && ../gradlew --info clean check $extra && cd ..
