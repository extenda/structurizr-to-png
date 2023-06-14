#!/bin/sh

dsl2png() {
  docker run --rm -it -v "$(pwd)":/docs -p 3000:3000 extenda/structurizr-to-png "$@"
}

dsl2png -o images/c4plantuml -r c4plantuml
dsl2png -o images/graphviz -r graphviz
dsl2png -o images/structurizr -r structurizr
