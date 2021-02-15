const fs = require('fs');
const chokidar = require('chokidar');
const path = require('path');
const fg = require('fast-glob');
const bs = require('browser-sync').create();
const { createDslEntry, singleDslToPng } = require('./dsl-to-png');
const { workDir, outputDir } = require('./opts.js');

const createIndexPage = (images, filename) => {
  const imgTags = [];
  const navbar = [];
  images.forEach((img) => {
    const imageName = path.basename(img);
    imgTags.push(`<div id="${imageName}"><img src="${imageName}"></div>`);
    navbar.push(`<li><a id="nav.${imageName}" href="#${imageName}" onClick="showImage('${imageName}')">${imageName}</a></li>`);
  });

  const html = fs.readFileSync(path.join(__dirname, 'index.tpl.html'), 'utf8')
    .replace('@IMAGES@', imgTags.join('\n'))
    .replace('@NAVBAR@', navbar.join('\n'));

  fs.writeFileSync(filename, html, 'utf8');
};

const createWebroot = () => {
  const webroot = path.join(workDir, 'www');
  if (fs.existsSync(webroot)) {
    fs.rmdirSync(webroot, { recursive: true });
  }
  fs.mkdirSync(webroot, { recursive: true })
  return webroot;
};

const changeHandler = (event, filename) => {
  if (filename && event === 'change') {
    // TODO Log something
    console.log('Process', event, filename);
    singleDslToPng(filename).catch((err) => {
      console.error(err.message);
    })
  }
};

const watch = (dslFiles) => {
  const images = [];
  const visitedDirs = new Set();

  const watcher = chokidar.watch(dslFiles, { persistent: true });
  watcher.on('change', (path) => {
    singleDslToPng(path).catch((err) => {
      console.error(err.message);
    });
  });

  dslFiles.forEach((dslFile) => {
    const { imageOutDir } = createDslEntry(dslFile);
    if (!visitedDirs.has(imageOutDir)) {
      visitedDirs.add(imageOutDir);
      images.push(...fg.sync(path.join(imageOutDir, '*.png')));
    }
  });

  const webroot = createWebroot();

  // Create symlinks for images in www.
  images.forEach((img) => {
    fs.symlinkSync(img, path.join(webroot, path.basename(img)));
  });
  createIndexPage(images, path.join(webroot, 'index.html'));

  bs.init({
    ui: false,
    server: webroot,
    watch: true,
    open: false,
    localOnly: true,
  });
};

module.exports = watch;
