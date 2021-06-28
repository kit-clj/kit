#!/bin/bash

## Installs all modules in one script

set -e

VERSION=$(<version.txt)

echo "Installing with version '${VERSION}'"

for ext in \
  wake-core \
  wake-crux \
  wake-hato \
  wake-metrics \
  wake-postgres \
  wake-quartz \
  wake-redis \
  wake-repl \
  wake-selmer \
  wake-sql \
  wake-template \
  wake-undertow; do
  cd modules/$ext; clojure -X:jar :sync-pom true :version "\"${VERSION}\""; clojure -X:install; cd ../..;
done