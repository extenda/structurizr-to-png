FROM adoptopenjdk:11-jre-hotspot-bionic
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN curl -sL https://deb.nodesource.com/setup_14.x | bash -
RUN apt-get update -qq && apt-get install -qq --no-install-recommends \
  nodejs \
  graphviz \
  unzip \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY src ./src/
COPY install-tools.sh .
COPY package*.json ./

EXPOSE 3000

RUN npm ci \
  && npm run postinstall \
  && npm cache clean --force

WORKDIR /docs

ENTRYPOINT ["node", "/app/src/index.js"]
