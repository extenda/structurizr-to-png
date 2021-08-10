#!/usr/bin/env bash
set -e
SCRIPT_PATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
mkdir -p "$SCRIPT_PATH/lib"

plantUmlVersion="1.2021.9"
curl -sS "https://repo.maven.apache.org/maven2/net/sourceforge/plantuml/plantuml/$plantUmlVersion/plantuml-${plantUmlVersion}.jar" -o "$SCRIPT_PATH/lib/plantuml.jar"

structurizrVersion="1.12.0"
curl -sSL "https://github.com/structurizr/cli/releases/download/v$structurizrVersion/structurizr-cli-$structurizrVersion.zip" -o structurizr.zip
unzip -q -p structurizr.zip "structurizr-cli-$structurizrVersion.jar" > "$SCRIPT_PATH/lib/structurizr.jar"
rm structurizr.zip
