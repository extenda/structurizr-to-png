#!/bin/sh
set -e
docker pull --quiet extenda/structurizr-to-png
exec docker run --rm -it -v "$(pwd)":/docs -p 3000:3000 extenda/structurizr-to-png "$@"
