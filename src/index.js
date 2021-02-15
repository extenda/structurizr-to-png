const glob = require('fast-glob');
const { allDslToPng, setOutputDir } = require('./dsl-to-png');
const opts = require('./opts');
const watch = require('./watch');

// Handle SIGINT to gracefully exit on CTRL+C in Docker.
process.once('SIGINT', () => {
  process.exit(0);
});

(async () => {
  // Find all dsl files from the working directory.
  const dslFiles = glob.sync(['**/*.dsl', '!**/node_modules']);

  await allDslToPng(dslFiles);

  if (opts.watch) {
    watch(dslFiles);
  }
})();
