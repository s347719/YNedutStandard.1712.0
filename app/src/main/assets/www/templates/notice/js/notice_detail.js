(function () {
    'use strict';

    angular
        .module('starter')
        .config(stateConfig)
        .controller('notice_detailController', notice_detailController);

    stateConfig.$inject = ['$stateProvider'];
    function stateConfig($stateProvider) {
        $stateProvider
        /*通知公告*/
            .state('notice_detail', {
                firstPage: true,
                url: '/notice_detail',
                templateUrl: 'templates/notice/notice_detail.html',
                controller: 'notice_detailController'
            })
    }

    notice_detailController.$inject = ['$scope', '$http', '$sce', '$location', '$cordovaFile', '$cordovaFileTransfer','$timeout','$ionicPopup'];
    function notice_detailController($scope, $http, $sce, $location, $cordovaFile, $cordovaFileTransfer, $timeout, $ionicPopup) {

        // format Size
        function formatSize(bytes) {
            if (bytes === 0) return '0 B';
            var k = 1024,
                sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
                i = Math.floor(Math.log(bytes) / Math.log(k));
            return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
        }

        ionic.Platform.ready(function () {
            // 下载接口及存储路径
            var downloadServer = originBaseUrl + '/third/file/download.htm?fileId=';
            var downLoadPath = cordova.file.documentsDirectory + 'ynDownloads/';
            if (ionic.Platform.isAndroid()) {
                downLoadPath = cordova.file.externalRootDirectory + 'ynDownloads/';
            }

            // 查询通知公告详情
            var queryNoticeUrl = originBaseUrl + '/third/notice/queryNotice.htm' +
                '?isUpdate=false' +
                '&isReceiver=false' +
                '&requestSource=1' +
                '&id=' + $location.search().id;
            $http
                .get(queryNoticeUrl)
                .success(function (noticeResult) {
                    if (noticeResult && noticeResult.result) {
                        var noticeDetail = noticeResult.result;
                        var attachmentList = noticeDetail.messageNoticeAttachmentList;
                        // 处理通知 内容
                        noticeDetail.noticeContent = noticeDetail.noticeContent.replace(/"\/fs\/preview\/preview\.htm/g, '"' + originBaseUrl.replace('/ynedut','') + '/fs/preview/preview.htm');
                        // 处理通知 落款
                        var signedContent = (noticeDetail.messageNoticeSignedTemplateVO && noticeDetail.messageNoticeSignedTemplateVO.signedContent) || '';
                        noticeDetail.messageNoticeSignedTemplateVO.signedContent = $sce.trustAsHtml(signedContent);
                        // 处理附件
                        if (attachmentList.length) {
                            angular.forEach(attachmentList, function (value, key) {
                                attachmentList[key].size = formatSize(value.size);
                                attachmentList[key].download = '下载打开';
                                // 检测本地文件
                                $cordovaFile.checkFile(downLoadPath, value.attachmentName)
                                    .then(function (success) {
                                        console.log(value.attachmentName + ' is downloaded!');
                                        attachmentList[key].download = '打开';
                                    }, function (error) {
                                        console.log('文件未下载：'+JSON.stringify(error));
                                        attachmentList[key].download = '下载打开';
                                    });
                            });
                        }

                        $scope.noticeDetail = noticeDetail;
                        $scope.attachmentList = attachmentList;
                    }
                })
                .error(function (noticeError) {
                    console.log('获取通知明细失败：' + JSON.stringify(noticeError))
                });

            // 附件下载
            $scope.downloadAttachment = function (attachment) {
                var fsId = attachment.attachmentId;
                var options = {};
                // 检测本地文件
                $cordovaFile.checkFile(downLoadPath, attachment.attachmentName)
                    .then(function (success) {
                        $scope.viewAttachment(attachment);
                        attachment.download = '打开';
                    }, function (error) {
                        attachment.download = "下载中";
                        $cordovaFileTransfer.download(downloadServer + attachment.attachmentId, encodeURI(downLoadPath + attachment.attachmentName), options, true)
                            .then(function (result) {
                                console.log('下载完成：' + JSON.stringify(result));
                                // 下载完成 默认打开
                                $scope.viewAttachment(attachment);
                            }, function (err) {
                                console.error('下载失败：' + JSON.stringify(err));
                                $ionicPopup.alert({
                                    title: '文件下载提示',
                                    template: '文件下载失败 #' + err.code
                                });
                                attachment.download = "下载打开";
                            }, function (progress) {
                                $timeout(function () {
                                    attachment.download = formatSize(progress.loaded);
                                });
                            });
                    });
            };

            $scope.viewAttachment = function (attachment) {
                var cordovaFileOpen = cordova.plugins.disusered.open;
                cordovaFileOpen(downLoadPath + attachment.attachmentName, cordovaFileOpenSuccess, cordovaFileOpenError);
                function cordovaFileOpenSuccess() {
                    console.log('Open success! ' + downLoadPath + attachment.attachmentName);
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
                $timeout(function () {
                    attachment.download = "打开";
                })
            }


        });

    }
})();
