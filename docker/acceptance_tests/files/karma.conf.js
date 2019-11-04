/*
 * This file is part of symfinder.
 *
 * symfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * symfinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with symfinder. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
 * Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
 * Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
 */

// Karma configuration

module.exports = function (config) {
    config.set({

        client: {
            projects: config.projects
        },

        // base path, that will be used to resolve files and exclude
        basePath: '',

        // frameworks to use
        frameworks: ['jasmine', 'jquery-3.2.1'],

        plugins: [
            'karma-jasmine',
            'karma-chrome-launcher',
            'karma-jquery',
            'karma-spec-reporter'
        ],

        // list of files / patterns to load in the browser
        files: [
            'tests/*.js',
            {pattern: 'tests/data/*.json', watched: false, served: true, included: false},
            {pattern: 'scripts/*.js', type: "module", watched: false, served: true, included: false}
        ],

        customContextFile: "pages/context.html",

        proxies: {
            '/tests/': '/base/tests/',
            '/scripts/': '/base/scripts/',
            '/context.js': '/base/context.js'
        },

        // list of files to exclude
        exclude: [],

        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['spec'],

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['ChromeNoSandboxHeadless'],
        customLaunchers: {
            ChromeNoSandboxHeadless: {
                base: 'Chromium',
                flags: [
                    '--no-sandbox',
                    // See https://chromium.googlesource.com/chromium/src/+/lkgr/headless/README.md
                    '--headless',
                    '--disable-gpu',
                    // Without a remote debugging port, Google Chrome exits immediately.
                    ' --remote-debugging-port=9222'
                ]
            }
        },

        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,

        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: true
    });
};
