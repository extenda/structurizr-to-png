const util = require('util');
const fs = require('fs');
const readFile = util.promisify(fs.readFile);
const writeFile = util.promisify(fs.writeFile);

const EXTERNAL_TAGS = ['external', 'existing system', 'external system'];
const DATABASE_TAGS = ['database'];

const hasTagsFrom = (element, tags) => element.tags.toLowerCase()
  .split(',')
  .some((t) => tags.includes(t));

const enhanceElement = (element) => {
  if (hasTagsFrom(element, EXTERNAL_TAGS)) {
    element.location = 'External';
  }
  if (hasTagsFrom(element, DATABASE_TAGS)) {
    if (!element.properties) {
      element.properties = {};
    }
    element.properties['c4:element:type'] = 'Db';
  }
};

const processJson = async (dslEntry) => {
  const { jsonFile } = dslEntry;
  return readFile(jsonFile, 'utf8')
    .then(JSON.parse)
    .then(data => {
      Object.keys(data.model).forEach((type) => {
        const elements = data.model[type];
        if (Array.isArray(elements)) {
          elements.forEach((element) => {
            enhanceElement(element);
            if (element.containers) {
              element.containers.forEach(enhanceElement)
            }
          });
        }
      });
      return JSON.stringify(data, null, 2);
    }).then(json => writeFile(jsonFile, json, 'utf8'))
    .then(() => dslEntry)
};

module.exports = processJson;
