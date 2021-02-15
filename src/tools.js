const path = require('path');
const chalk = require('chalk');
const util = require('util');
const exec = util.promisify(require('child_process').exec);
const mkdir = util.promisify(require('fs').mkdir);

const libDir = path.resolve(__dirname, '..', 'lib');

const logDsl = (dslFile, message) => {
  console.log(`[${chalk.blue('dsl2png')}] ${chalk.green(dslFile)} - ${message}`)
}

const structurizr = async (format, outputDir, inputFile) => {
  const args = [
    `java -jar "${path.join(libDir, 'structurizr.jar')}"`,
    'export',
    '-f', format,
    '-o', `"${outputDir}"`,
    '-w', `"${inputFile}"`,
  ];
  return exec(args.join(' '));
};

const exportDslToJson = async (dslEntry) => {
  const {dslFile, uniqueWorkDir} = dslEntry;
  logDsl(dslFile, 'Export JSON');
  return structurizr('json', uniqueWorkDir, dslFile)
    .then(() => dslEntry);
};

const exportJsonToPuml = async (dslEntry) => {
  const { dslFile, jsonFile, uniqueWorkDir } = dslEntry;
  logDsl(dslFile, 'Export PlantUML');
  return structurizr('plantuml/c4plantuml', uniqueWorkDir, jsonFile)
    .then(() => dslEntry);
}

const plantUml = async (dslEntry) => {
  const { dslFile, uniqueWorkDir, imageOutDir } = dslEntry;
  logDsl(dslFile, `Create PNGs ${chalk.cyan(imageOutDir)}`);
  return mkdir(imageOutDir, { recursive: true }).then(
    () => exec(`java -jar "${path.join(libDir, 'plantuml.jar')}" "${uniqueWorkDir}"/*.puml -o "${imageOutDir}"`),
  );
};

module.exports = {
  exportDslToJson,
  exportJsonToPuml,
  plantUml,
}
