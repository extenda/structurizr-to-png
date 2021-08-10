const util = require('util');
const fs = require('fs');
const path = require('path');
const readFile = util.promisify(fs.readFile);
const writeFile = util.promisify(fs.writeFile);

const EXTERNAL_TAGS = ['external', 'existing system', 'external system'];

const hasTagsFrom = (element, tags) => element.tags.toLowerCase()
  .split(',')
  .some((t) => tags.includes(t));

const setProperty = (element, name, value) => {
  if (!element.properties) {
    element.properties = {};
  }
  element.properties[name] = value;
}

const enhanceElement = (element) => {
  if (hasTagsFrom(element, EXTERNAL_TAGS)) {
    element.location = 'External';
  }
};

const processModel = (data) => {
  const { model } = data;
  Object.keys(model).forEach((type) => {
    const elements = model[type];
    if (Array.isArray(elements)) {
      elements.forEach((element) => {
        enhanceElement(element);
        if (element.containers) {
          element.containers.forEach(enhanceElement)
        }
      });
    }
  });
  return {
    ...data,
    model,
  };
};

const addStyles = async (data) => {
  const { views: { configuration: { styles: { elements = [] } } } } = data;

  const dslTheme = await readFile(path.resolve(__dirname, 'theme.json'), 'utf8');

  return readFile(path.resolve(__dirname, 'theme.json'), 'utf8')
    .then(JSON.parse)
    .then((theme) => {
      const result = { ...data };
      result.views.configuration.styles = {
        elements: [
          ...theme.elements,
          ...elements,
        ],
      };
      return result;
    });
};

const processJson = async (dslEntry) => {
  const { jsonFile } = dslEntry;
  return readFile(jsonFile, 'utf8')
    .then(JSON.parse)
    .then(processModel)
    .then(addStyles)
    .then(data => JSON.stringify(data, null, 2))
    .then(json => writeFile(jsonFile, json, 'utf8'))
    .then(() => dslEntry)
};

module.exports = processJson;
