const chalk = require('chalk');
const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const axios = require('axios');
const plantUmlEncoder = require('plantuml-encoder');
const log = require('./logger');

const libDir = path.resolve(__dirname, '..', 'lib');

let resolveServerReady;

const serverReady = new Promise((resolve, reject) => {
  resolveServerReady = resolve;
});

const startServer = () => {
  log.info('Start PlantUML server');
  const server = spawn('java', ['-jar', path.join(libDir, 'plantuml.jar'), '-picoweb:8888']);
  server.on('error', (err) => {
    log.error(`PlantUML error.\n${err.message}`);
  });

  server.stderr.once('data', () => {
    log.info('PlantUML server listening on port 8888');
    resolveServerReady();
  })

  server.once('close', () => {
    log.info('Close PlantUML server');
  });

  return () => server.kill('SIGKILL');
};

const renderPng = async (pumlFile) => {
  // Wait for PlantUML to be ready for requests.
  await serverReady;

  const data = plantUmlEncoder.encode(fs.readFileSync(pumlFile, 'utf8'));
  return axios.get(
    `http://127.0.0.1:8888/plantuml/png/${data}`,
    { responseType: 'arraybuffer' },
    ).then((response) => ({
      imageName: path.basename(pumlFile, '.puml') + '.png',
      data: response.data
    }));
}

module.exports = {
  startServer,
  renderPng,
}
