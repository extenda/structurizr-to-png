const path = require('path');
const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');

const argv = yargs(hideBin(process.argv))
  .usage('$0 [options]', 'Create PNG files from Structurizr DSL.')
  .option('output', {
    alias: 'o',
    type: 'string',
    default: 'images',
    description: 'Image output directory. A relative path is resolved from the DSL file.'
  }).option('watch', {
    alias: 'w',
    type: 'boolean',
    default: false,
    description: 'Watch for changed DSL files.'
  }).argv;

module.exports = {
  workDir: path.resolve(__dirname, '..', '.work'),
  outputDir: argv.output,
  watch: argv.watch,
}
