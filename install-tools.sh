#!/usr/bin/env bash
set -e
SCRIPT_PATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
rm -rf "$SCRIPT_PATH/lib"
mkdir -p "$SCRIPT_PATH/lib"

plantUmlVersion="1.2021.12"
curl -sS "https://repo.maven.apache.org/maven2/net/sourceforge/plantuml/plantuml/$plantUmlVersion/plantuml-${plantUmlVersion}.jar" -o "$SCRIPT_PATH/lib/plantuml.jar"

structurizrVersion="1.15.0"
curl -sSL "https://github.com/structurizr/cli/releases/download/v${structurizrVersion}/structurizr-cli-${structurizrVersion}.zip" -o structurizr.zip
unzip -j structurizr.zip "lib/*.jar" -d "$SCRIPT_PATH/lib/structurizr"
rm structurizr.zip
