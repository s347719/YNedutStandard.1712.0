cordova.define("custom-plugin-file-chooser.fileChooser", function(require, exports, module) {

    var exec = require('cordova/exec');

    var fileChooser = {
        open: function(successFn, failureFn) {
            exec(successFn, failureFn, "FileChooser","open", []);
        }
    };

    module.exports = fileChooser;

});
