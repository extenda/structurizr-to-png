const chalk = require('chalk');

const LOG_NAME = `[${chalk.blue('dsl2png')}]`;

const format = (msg, dslFile = '') => {
  if (dslFile) {
    return `${LOG_NAME} ${chalk.green(dslFile)} - ${msg}`;
  }
  return `${LOG_NAME} ${msg}`;
};

module.exports = {
  error: (msg) => console.error(format(chalk.red(msg))),
  info: (msg) => console.log(format(msg)),
  logDsl: (dslFile, msg) => console.log(format(msg, dslFile)),
  raw: (msg) => console.log(msg),
};
