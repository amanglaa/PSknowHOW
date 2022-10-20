// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html



module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      clearContext: true, // leave Jasmine Spec Runner output visible in browser,
      jasmine: {
        // timeoutInterval: 2147483647  // maximum value a 32 bit int can hold
        timeoutInterval: 6000,
        random: false
      }

    },
    coverageIstanbulReporter: {
      dir: require('path').join(__dirname, '../coverage'),
      reports: ['html', 'lcovonly'],
      fixWebpackSourcePaths: true
    },
    reporters: ['progress', 'kjhtml'],
    port: 8000,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['ChromeHeadlessNoSandbox'],//Chrome
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: [
          '--disable-gpu',
          '--no-sandbox',
          '--disable-translate',
          '--disable-extensions',
          '--no-proxy-server']
      }
    },
    singleRun: true,
    browserDisconnectTolerance: 3,
    browserNoActivityTimeout: 200000,
    concurrency: Infinity,
    reportSlowerThan: 5000
  });
};
