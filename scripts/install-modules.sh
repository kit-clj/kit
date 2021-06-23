#!/bin/bash

## Installs all modules in one script

set -e

for ext in \
  wake-core \
  wake-crux \
  wake-hato \
  wake-metrics \
  wake-quartz \
  wake-redis \
  wake-repl \
  wake-selmer \
  wake-sql \
  wake-template \
  wake-undertow; do
  cd modules/$ext; lein install; cd ../..;
done