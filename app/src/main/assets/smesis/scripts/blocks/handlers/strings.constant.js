(function () {
    'use strict';

    /* 全局常量字符串定义 fanjiaben
     * eg. stringsConstant.gps.error
     */
    angular
        .module('myApp')
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
                    return basePath + '/third/outuploader/queryFileMetaDataByFdfsIds?fastdfsIds=';
                },
                // 文件上传接口
                uploadServer: function(){
                    return basePath + '/third/outuploader/upload';
                },
                // 文件合并接口
                mergeServer: function(){
                    return basePath + '/third/outuploader/mergeFile'
                },
                // 文件下载接口
                downloadServer: function(){
                    return basePath + '/outuploader/downloadStream?fastDFSId=';
                },
                // 文件删除接口
                deleteServer: function(){
                    return basePath + '/third/outuploader/delete?fastDFSId=';
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