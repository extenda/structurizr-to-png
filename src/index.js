const glob = require('fast-glob');
const chalk = require('chalk');
const { allDslToPng, setOutputDir } = require('./dsl-to-png');
const opts = require('./opts');
const watch = require('./watch');
const { startServer } = require('./plantuml');
const log = require('./logger');

// Handle SIGINT to gracefully exit on CTRL+C in Docker.
process.once('SIGINT', () => {
  process.exit(0);
});

(async () => {
  // Find all dsl files from the working directory.
  const dslFiles = glob.sync([opts.path, '!**/node_modules']);

  if (dslFiles.length === 0) {
    log.raw(chalk.red('No DSL files found matching ' + opts.path));
    process.exit(0);
  }

  // Start PlantUML now.
  const stopServer = startServer();

  await allDslToPng(dslFiles)
    .catch((err) => {
      log.error(err.message);
    });

  if (opts.watch) {
    watch(dslFiles);
  } else {
    stopServer();
  }
})();
