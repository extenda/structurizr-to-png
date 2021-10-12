const glob = require('fast-glob');
const chalk = require('chalk');
const { allDslToPng, setOutputDir } = require('./dsl-to-png');
const opts = require('./opts');
const watch = require('./watch');

// Handle SIGINT to gracefully exit on CTRL+C in Docker.
process.once('SIGINT', () => {
  process.exit(0);
});

(async () => {
  // Find all dsl files from the working directory.
  const dslFiles = glob.sync([opts.path, '!**/node_modules']);

  if (dslFiles.length === 0) {
    console.log(chalk.red('No DSL files found matching *.dsl'));
    process.exit(0);
  }

  await allDslToPng(dslFiles);

  if (opts.watch) {
    watch(dslFiles);
  }
})();
