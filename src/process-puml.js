const fs = require('fs');
const path = require('path');
const util = require('util');
const readDir = util.promisify(fs.readdir);
const readFile = util.promisify(fs.readFile);
const writeFile = util.promisify(fs.writeFile);

const modifyPuml = (data) => data.replace('@enduml', 'SHOW_DYNAMIC_LEGEND()\n@enduml');

const processPuml = async (dslEntry) => {
  const { uniqueWorkDir } = dslEntry;
  return readDir(uniqueWorkDir)
    .then((files) => files.filter(file => file.endsWith('.puml'))
      .map(file => path.resolve(uniqueWorkDir, file))
      .map(file => readFile(file, 'utf8').then(data => ({
        file,
        data: modifyPuml(data),
      })).then(({ file, data }) => writeFile(file, data, 'utf8'))))
    .then(() => dslEntry);
};

module.exports = processPuml;
