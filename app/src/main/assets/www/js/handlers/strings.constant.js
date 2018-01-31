(function () {
    'use strict';

    /* 全局常量字符串定义 fanjiaben
     * eg. stringsConstant.gps.error
     */
    angular
        .module('starter')
        .constant('stringsConstant', {
            gps: {
                progress: '定位中...',
                error: '定位失败(未开启GPS或网络异常)'
            },
            http: {
                progress: '正在加载...',
                timeout:'请求超时，请重试',
                error:'加载失败，请重试',
                timeoutNum:60000
            },
            file: {
                // 文件信息接口
                infoServer: function(){
                    return originBaseUrl + '/third/file/xxxxxxxx.htm?fileId=';
                },
                // 文件上传接口
                uploadServer: function(){
                    return originBaseUrl + '/third/file/upload.htm';
                },
                // 文件下载接口
                downloadServer: function(){
                    return originBaseUrl + '/third/file/download.htm?fileId=';
                },
                // 文件删除接口
                deleteServer: function(){
                    return originBaseUrl + '/third/file/delete.htm?fileId=';
                },
                // 文件下载本地路径
                downLoadPath: function(){
                    if(ionic.Platform.isAndroid()){
                        return cordova.file.externalDataDirectory + 'Downloads/';
                    }else {
                        return cordova.file.documentsDirectory + 'Data/Downloads/';
                    }
                }
            },
            utils: {
                uuid: function() {
                    var s = [];
                    var hexDigits = "0123456789abcdef";
                    for (var i = 0; i < 36; i++) {
                        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
                    }
                    s[14] = "4";
                    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
                    s[8] = s[13] = s[18] = s[23] = "-";
                    return s.join("");
                }
            }
        });
})();