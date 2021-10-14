const path = require('path');
const glob = require('fast-glob');
const chalk = require('chalk');
const fs = require('fs');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const mkdir = util.promisify(fs.mkdir);
const { renderPng } = require('./plantuml');
const log = require('./logger');

const libDir = path.resolve(__dirname, '..', 'lib');

const structurizr = async (format, outputDir, inputFile) => {
  const args = [
    'java',
    '-cp', path.join(libDir, 'structurizr') + path.delimiter + path.join(libDir, 'structurizr', '*'),
    'com.structurizr.cli.StructurizrCliApplication',
    'export',
    '-f', format,
    '-o', `"${outputDir}"`,
    '-w', `"${inputFile}"`,
  ];
  return exec(args.join(' ')).catch((err) => {
    throw new Error(`!! Error in ${inputFile}.\n${err.message}`);
  });
};

const exportDslToJson = async (dslEntry) => {
  const {dslFile, uniqueWorkDir} = dslEntry;
  log.logDsl(dslFile, 'Export JSON');
  return structurizr('json', uniqueWorkDir, dslFile)
    .then(() => dslEntry);
};

const exportJsonToPuml = async (dslEntry) => {
  const { dslFile, jsonFile, uniqueWorkDir } = dslEntry;
  log.logDsl(dslFile, 'Export PlantUML');
  return structurizr('plantuml/c4plantuml', uniqueWorkDir, jsonFile)
    .then(() => dslEntry);
}



const plantUml = async (dslEntry, removeImages = false) => {
  const { dslFile, uniqueWorkDir, imageOutDir } = dslEntry;
  log.logDsl(dslFile, `Create PNGs ${chalk.cyan(imageOutDir)}`);

  if (removeImages) {
    glob.sync(`${imageOutDir}/structurizr-*.png`)
      .forEach(fs.unlinkSync);
  }

  return mkdir(imageOutDir, { recursive: true }).then(
    () => {
      const writeFile = util.promisify(fs.writeFile);
      return Promise.all(glob.sync(`${uniqueWorkDir}/*.puml`)
        .map((pumlFile) => renderPng(pumlFile)
          .then(({ imageName, data }) => {
            const filename = path.join(imageOutDir, imageName);
            return writeFile(filename, data, 'binary').then(() => {
              log.logDsl(dslFile, chalk.cyan(filename));
              return filename;
            });
          }).catch((err) => {
            log.logDsl(dslFile, `Failed to render PNG. Reason: ${chalk.red(err.message)}`);
          })));
      });
};

module.exports = {
  exportDslToJson,
  exportJsonToPuml,
  plantUml,
}
