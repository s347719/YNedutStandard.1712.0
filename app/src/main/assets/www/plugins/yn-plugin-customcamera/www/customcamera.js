cordova.define("yn-plugin-customcamera.customcamera", function(require, exports, module) {

var exec = require('cordova/exec');

exports.startCamera = function(opts,successFun, failFun) {
    exec(successFun,failFun,'YNCamera','startCamera',opts)
};

});
