cordova.define("cordova-plugin-baidulocation.baidulocation", function(require, exports, module) {

    var exec = require('cordova/exec');

    var baiduLocation = {
      getCurrentPosition: function(successFn, failureFn) {
        exec(successFn, failureFn, 'BaiduLocation', 'getCurrentPosition', []);
      }
    };

    module.exports = baiduLocation
});
