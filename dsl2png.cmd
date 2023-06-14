@echo off
docker pull --quiet extenda/structurizr-to-png
docker run --rm -it -v "%CD%":/docs -p 3000:3000 extenda/structurizr-to-png %*
