/**
 * project:     yineng-corpSysLand
 * title:      inform_indexController
 * author:      qiucheng Lu
 * date:        2017年3月30日09:58:26
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description: 通知公告页面控制器
 */
(function () {
    'use strict';
    angular
        .module('myApp')
        .controller('inform_indexController', inform_indexController);

    inform_indexController.$inject = ['$scope','$http','$location','$cordovaFileTransfer','$cordovaFile','$ionicPopup'];
    function inform_indexController($scope,$http,$location,$cordovaFileTransfer,$cordovaFile,$ionicPopup){
        document.addEventListener("deviceready", function () {
            var downloadServer = basePath + '/outuploader/downloadStream?fastDFSId=';
            var downLoadPath = cordova.file.documentsDirectory + 'Data/Downloads/';
            if(ionic.Platform.isAndroid()){
                downLoadPath = cordova.file.externalDataDirectory + 'Downloads/';
            }

            // format Size
            function formatSize(bytes) {
                if (bytes === 0) return '0 B';
                var k = 1024,
                    sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
                    i = Math.floor(Math.log(bytes) / Math.log(k));
                return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
            }

            var id = $location.search().id;
            var platformSysUserId = $location.search().platformSysUserId;
            $http.get(basePath + "/third/businessjournal/findByNoticeId?id="+id).success(function(data){
                $scope.sysNoticeDTO = data.result;
                if($scope.sysNoticeDTO.szglFileAttachmentDTOList.length){
                    angular.forEach($scope.sysNoticeDTO.szglFileAttachmentDTOList,function(value,key){
                        // format size
                        $scope.sysNoticeDTO.szglFileAttachmentDTOList[key].size = formatSize(value.size);
                        // check file
                        $cordovaFile.checkFile(downLoadPath, value.attachmentName)
                            .then(function (success) {
                                console.log(value.attachmentName + ' is downloaded!');
                                $scope.sysNoticeDTO.szglFileAttachmentDTOList[key].download = "打开";
                            }, function (error) {
                                console.log(JSON.stringify(error));
                                $scope.sysNoticeDTO.szglFileAttachmentDTOList[key].download = "下载";
                            });
                    })
                }
            });

            $scope.downloadFile = function(file) {
                var options = {};

                // check file
                $cordovaFile.checkFile(downLoadPath, file.attachmentName)
                    .then(function (success) {
                        console.log(file.attachmentName + ' is downloaded!');
                        $scope.openFile(file);
                        file.download = "打开";
                    }, function (error) {
                        console.log(JSON.stringify(error));

                        file.download = "下载中";
                        $cordovaFileTransfer.download(downloadServer+file.attachmentId, encodeURI(downLoadPath + file.attachmentName), options, true)
                            .then(function (result) {
                                console.log(JSON.stringify(result));

                                // 下载完成 默认打开
                                $scope.openFile(file);
                            }, function (err) {
                                console.error(JSON.stringify(err));
                                $ionicPopup.alert({
                                    title: '文件下载提示',
                                    template: '文件下载失败 #' + err.code
                                });
                            }, function (progress) {
                                $timeout(function () {
                                    file.downloadProgress = (progress.loaded / progress.total) * 100;
                                });
                            });
                    });


            };

            $scope.openFile = function(file){
                var cordovaFileOpen = cordova.plugins.disusered.open;
                cordovaFileOpen(downLoadPath + file.attachmentName, cordovaFileOpenSuccess, cordovaFileOpenError);
                function cordovaFileOpenSuccess() {
                    console.log('Open success! ' + downLoadPath + file.attachmentName);
                }
                function cordovaFileOpenError(code) {
                    if (code === 1) {
                        $ionicPopup.alert({
                            title: '文件预览提示',
                            template: '未能在手机已安装APP中找到可预览此文件的程序'
                        });
                    } else {
                        $ionicPopup.alert({
                            title: '文件预览提示',
                            template: '预览时发生了未知错误 #0'
                        });
                    }
                }
                file.download = "打开";
            }
        });
    }
})();
