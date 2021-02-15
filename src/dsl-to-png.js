const path = require('path');
const { exportDslToJson, exportJsonToPuml, plantUml } = require('./tools');
const processJson = require('./process-json')
const processPuml = require('./process-puml');
const { workDir, outputDir } = require('./opts.js');

const createDslEntry = (dslFile) => {
  const dslDir = path.dirname(dslFile);
  const imageOutDir = path.isAbsolute(outputDir) ? path.resolve(outputDir) : path.resolve(dslDir, outputDir);
  const uniqueWorkDir = path.resolve(workDir, dslDir);
  const jsonFile = path.join(uniqueWorkDir, `${path.basename(dslFile, '.dsl')}.json`);
  return {
    dslFile,
    jsonFile,
    uniqueWorkDir,
    imageOutDir,
  };
};

const dslToPuml = async (dslEntry) => exportDslToJson(dslEntry)
  .then(processJson)
  .then(exportJsonToPuml);

const singleDslToPng = async (dslFile) => {
  const dslEntry = typeof dslFile == 'string' ? createDslEntry(dslFile) : dslFile;
  return dslToPuml(dslEntry)
    .then(processPuml)
    .then(plantUml)
};

const allDslToPng = async (dslFiles) => {
  let promises = [];
  const visitedDirs = new Set();
  dslFiles.forEach(dslFile => {
    promises.push(dslToPuml(createDslEntry(dslFile))
      .then((dslEntry) => {
        // PUML files are processed per directory so we
        // only process each working directory once.
        if (!visitedDirs.has(dslEntry.uniqueWorkDir)) {
          visitedDirs.add(dslEntry.uniqueWorkDir);
          return processPuml(dslEntry).then(plantUml);
        }
        return dslEntry;
      }));
  });
  return Promise.all(promises);
};

module.exports = {
  createDslEntry,
  singleDslToPng,
  allDslToPng,
};
