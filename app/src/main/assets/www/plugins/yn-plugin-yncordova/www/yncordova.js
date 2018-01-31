cordova.define("yn-plugin-yncordova.yncordova", function(require, exports, module) {

    var exec = require('cordova/exec');

    // 关闭 cordova
    exports.close = function(successFn, failureFn) {
        exec(successFn, failureFn, "YNCordova","close", []);
    }

});
