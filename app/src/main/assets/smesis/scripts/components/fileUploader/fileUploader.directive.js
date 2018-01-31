(function () {
    'use strict';

    angular
        .module('myApp')
        .factory('Attachment', function ($rootScope, $ionicModal, $cordovaFileTransfer, $cordovaFile, $ionicPopup, $timeout, $http) {
            var service = {
                show: show
            };

            var $scope = $rootScope.$new(true);

            // format Size
            function formatSize(bytes) {
                if (bytes === 0) return '0 B';
                var k = 1024,
                    sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
                    i = Math.floor(Math.log(bytes) / Math.log(k));
                return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
            }

            function show(files) {
                var fileInfoServer = basePath + '/third/outuploader/queryFileMetaDataByFdfsIds?fastdfsIds=';
                var downloadServer = basePath + '/outuploader/downloadStream?fastDFSId=';
                var downLoadPath = cordova.file.documentsDirectory + 'Data/Downloads/';
                if(ionic.Platform.isAndroid()){
                    downLoadPath = cordova.file.externalDataDirectory + 'Downloads/';
                }
                files = files.split(',');
                $scope.files = [];
                $http({
                    method: 'GET',
                    url: fileInfoServer + files
                }).success(function(resFilesInfo){
                    if(resFilesInfo && resFilesInfo.result && resFilesInfo.result.length){
                        angular.forEach(resFilesInfo.result, function (file) {
                            var fileExt = (file.fileName).substring(file.fileName.lastIndexOf('.'));
                            var defaultIcon = 'other';
                            if(/\.(doc|docx)$/i.test(fileExt)){
                                defaultIcon = 'word';
                            }else if(/\.(ppt|pptx)$/i.test(fileExt)){
                                defaultIcon = 'ppt';
                            }else if(/\.(xls|xlsx)$/i.test(fileExt)){
                                defaultIcon = 'excel';
                            }else if(/\.(rar|zip|gzip)$/i.test(fileExt)){
                                defaultIcon = 'rar';
                            }else if(/\.(pdf)$/i.test(fileExt)){
                                defaultIcon = 'pdf';
                            }
                            var previewIcon = 'assets/images/ext/'+defaultIcon+'.png';
                            if(/\.(gif|jpg|jpeg|png|bmp)$/i.test(fileExt)){
                                previewIcon = downloadServer + file.fileURLMappingId;
                            }
                            var formatFile = {
                                fsId: file.fileURLMappingId,
                                uri: downloadServer + file.fileURLMappingId,
                                previewIcon: previewIcon,
                                name: file.fileName,
                                size: formatSize(file.size),
                                download : "下载"
                            };
                            $cordovaFile.checkFile(downLoadPath, file.fileName)
                                .then(function (success) {
                                    console.log(file.fileName + ' is downloaded!');
                                    formatFile.download = "打开";
                                }, function (error) {
                                    console.log(JSON.stringify(error));
                                });
                            $scope.files.push(formatFile);
                        });

                        $scope.openModal();
                    }else{
                        $ionicPopup.alert({
                            title: '提示',
                            template: '文件丢失了'
                        });
                    }
                })
                    .error(function(){
                        $ionicPopup.alert({
                            title: '提示',
                            template: '服务器错误'
                        });
                    });


                document.addEventListener('deviceready', function () {
                    var options = {};

                    $scope.openFile = function (e, file) {
                        e.preventDefault();
                        e.stopPropagation();

                        var cordovaFileOpen = cordova.plugins.disusered.open;
                        cordovaFileOpen(downLoadPath + file.name, cordovaFileOpenSuccess, cordovaFileOpenError);
                        function cordovaFileOpenSuccess() {
                            console.log('Open success! ' + downLoadPath + file.name);
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
                    };

                    $scope.download = function (e, file) {
                        e.preventDefault();
                        e.stopPropagation();

                        // 检查文件是否存在
                        $cordovaFile.checkFile(downLoadPath, file.name)
                            .then(function (success) {
                                console.log(JSON.stringify(success));

                                $scope.openFile(e, file);

                            }, function (error) {
                                console.log(JSON.stringify(error));
                                // 不存在 则进行下载
                                file.download = "下载中";
                                $cordovaFileTransfer.download(file.uri, encodeURI(downLoadPath + file.name), options, true)
                                    .then(function (result) {
                                        console.log(JSON.stringify(result));

                                        // 下载完成 默认打开
                                        $scope.openFile(e, file);
                                        file.download = "打开";
                                    }, function (err) {
                                        console.error(JSON.stringify(err));

                                    }, function (progress) {
                                        $timeout(function () {
                                            $scope.downloadProgress = (progress.loaded / progress.total) * 100;
                                        });
                                    });
                            });
                    };

                }, false);

                $scope.modal = $ionicModal.fromTemplate(
                    '<ion-modal-view class="bottom-half attachmentViewList">' +
                    '<ion-header-bar class="bar-positive">' +
                    '<h1 class="title" ng-bind="\'附件(\'+files.length+\')\'"></h1>' +
                    '<button class="button button-clear" ng-click="closeModal()">取消</button>' +
                    '</ion-header-bar>' +
                    '<ion-content>' +
                    '<div class="list">' +
                    '<a ng-repeat="file in files" ng-click="download($event,file)" class="item item-thumbnail-left" href="javascript:;">' +
                    '<img ng-src="{{file.previewIcon}}">' +
                    '<h2 ng-bind="file.name"></h2>' +
                    '<p>' +
                    '<span ng-bind="file.size" ng-if="file.size" class="padding-right"></span>' +
                    '</p>' +
                    '<button class="download button button-clear button-dark" ng-bind="file.download">下载</button>' +
                    '</a>' +
                    '</div>' +
                    '</ion-content>' +
                    '</ion-modal-view>',
                    {
                        scope: $scope,
                        animation: 'slide-in-up'
                    });
                $scope.openModal = function () {
                    $scope.modal.show();
                };
                $scope.closeModal = function () {
                    $scope.modal.hide();
                };
                // Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function () {
                    $scope.modal.remove();
                });
                // Execute action on hide modal
                $scope.$on('modal.hidden', function () {
                    $scope.modal.remove();
                });
                // Execute action on remove modal
                $scope.$on('modal.removed', function () {
                    // Execute action
                });
            }

            return service;
        })
        .directive('fileUploader', function ($cordovaFile,$cordovaFileTransfer, $cordovaImagePicker, $http, $timeout, $ionicPopup,$ionicScrollDelegate) {
            return {
                restrict: 'A',
                scope: {
                    initFiles: '='
                },
                template: '<div class="list fileUploadList">' +
                '<div class="item item-button-right">' +
                '附件(图片)' +
                '<button ng-click="getPictures()" class="button filePicker">' +
                '<i class="icon ion-plus-round"></i>' +
                '</button>' +
                '</div>' +
                '<a ng-repeat="file in files" class="item item-thumbnail-left" ng-click="preview(file)">' +
                '<img ng-src="{{file.previewIcon}}">' +
                '<h2 ng-bind="file.name"></h2>' +
                '<p>' +
                '<span ng-bind="file.size" ng-if="file.size" class="padding-right"></span>' +
                '<span ng-bind="(file.progress)"></span>' +
                '</p>' +
                '<button ng-if="file.state==-1" ng-click="retry($event,file)" class="retry button button-icon icon ion-arrow-up-a"></button>' +
                '<button ng-if="file.state==-1||file.state==1" ng-click="deleteFile($event,file)" class="delete button button-icon icon ion-close-circled"></button>' +
                '</a>' +
                '</div>',
                link: function (scope, element, attrs) {
                    // format Size
                    function formatSize(bytes) {
                        if (bytes === 0) return '0 B';
                        var k = 1024,
                            sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
                            i = Math.floor(Math.log(bytes) / Math.log(k));
                        return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
                    }

                    // create uuid
                    function uuid() {
                        var s = [];
                        var hexDigits = "0123456789abcdef";
                        for (var i = 0; i < 36; i++) {
                            s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
                        }
                        s[14] = "4";
                        s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
                        s[8] = s[13] = s[18] = s[23] = "-";
                        var domId = s.join("");
                        return domId;
                    }

                    var uuid = uuid();

                    scope.queue = {
                        index: 0,
                        done: true
                    };
                    scope.files = [];


                    var transferServer = basePath + '/third/outuploader/upload?platformSysUserId=15&mac=' + uuid;
                    var mergeServer = basePath + '/third/outuploader/mergeFile';
                    var deleteServer = basePath + '/third/outuploader/delete?fastDFSId=';
                    var fileInfoServer = basePath + '/third/outuploader/queryFileMetaDataByFdfsIds?fastdfsIds=';
                    var downloadServer = basePath + '/outuploader/downloadStream?fastDFSId=';

                    var downLoadPath = cordova.file.documentsDirectory + 'Data/Downloads/';
                    if(ionic.Platform.isAndroid()){
                        downLoadPath = cordova.file.externalDataDirectory + 'Downloads/';
                    }

                    // Listens on event of file
                    scope.$on(attrs.initBroadcast, function () {
                        scope.files = [];
                        $timeout(function () {
                            $http({
                                method: 'GET',
                                url: fileInfoServer + scope.initFiles
                            }).success(function (resFilesInfo) {
                                if(resFilesInfo && resFilesInfo.result){
                                    angular.forEach(resFilesInfo.result, function (file) {
                                        var fileExt = (file.fileName).substring(file.fileName.lastIndexOf('.'));
                                        var defaultIcon = 'other';
                                        if(/\.(doc|docx)$/i.test(fileExt)){
                                            defaultIcon = 'word';
                                        }else if(/\.(ppt|pptx)$/i.test(fileExt)){
                                            defaultIcon = 'ppt';
                                        }else if(/\.(xls|xlsx)$/i.test(fileExt)){
                                            defaultIcon = 'excel';
                                        }else if(/\.(rar|zip|gzip)$/i.test(fileExt)){
                                            defaultIcon = 'rar';
                                        }else if(/\.(pdf)$/i.test(fileExt)){
                                            defaultIcon = 'pdf';
                                        }
                                        var previewIcon = 'assets/images/ext/'+defaultIcon+'.png';
                                        if(/\.(gif|jpg|jpeg|png|bmp)$/i.test(fileExt)){
                                            previewIcon = downloadServer + file.fileURLMappingId;
                                        }
                                        scope.files.push({
                                            fsId: file.fileURLMappingId,
                                            uri: downloadServer + file.fileURLMappingId,
                                            previewIcon: previewIcon,
                                            name: file.fileName,
                                            size: formatSize(file.size),
                                            progress: '已上传',
                                            state: 1
                                        });
                                    });
                                    scope.queue.index = scope.files.length;
                                }
                            });
                        });
                    });

                    document.addEventListener("deviceready", function () {
                        scope.getPictures = function () {
                            var options = {
                                maximumImagesCount: 10,
                                width: 800,
                                height: 800,
                                quality: 80
                            };

                            $cordovaImagePicker.getPictures(options).then(getPicturesSuc,getPicturesErr);

                            function getPicturesSuc(results) {
                                console.log(JSON.stringify(results));
                                // 存放已选文件
                                for (var i = 0; i < results.length; i++) {
                                    var fileName = results[i].substr(results[i].lastIndexOf('/') + 1);
                                    var fileExt = (fileName).substring(fileName.lastIndexOf('.'));
                                    var defaultIcon = 'other';
                                    if(/\.(doc|docx)$/i.test(fileExt)){
                                        defaultIcon = 'word';
                                    }else if(/\.(ppt|pptx)$/i.test(fileExt)){
                                        defaultIcon = 'ppt';
                                    }else if(/\.(xls|xlsx)$/i.test(fileExt)){
                                        defaultIcon = 'excel';
                                    }else if(/\.(rar|zip|gzip)$/i.test(fileExt)){
                                        defaultIcon = 'rar';
                                    }else if(/\.(pdf)$/i.test(fileExt)){
                                        defaultIcon = 'pdf';
                                    }
                                    var previewIcon = 'assets/images/ext/'+defaultIcon+'.png';
                                    if(/\.(gif|jpg|jpeg|png|bmp)$/i.test(fileExt)){
                                        previewIcon = results[i];
                                    }
                                    scope.files.push({
                                        uri: results[i],
                                        previewIcon: previewIcon,
                                        name: fileName,
                                        progress: '等待上传...',
                                        state: 0
                                    });
                                }
                                if (scope.queue.done) {
                                    scope.queue.done = false;
                                    scope.start();
                                }
                            }
                            function getPicturesErr(imagePickerErr) {
                                console.error(JSON.stringify(imagePickerErr));

                            }

                        };
                        scope.start = function () {
                            if (!scope.queue.done) {
                                if (scope.queue.index <= scope.files.length - 1) {
                                    scope.fileTransfer(scope.files[scope.queue.index]);
                                    scope.queue.index++;
                                } else {
                                    scope.queue.done = true;
                                }
                            }
                        };
                        scope.retry = function (e, file) {
                            e.preventDefault();
                            e.stopPropagation();
                            scope.fileTransfer(file);
                        };
                        scope.fileTransfer = function (file) {
                            // 参数
                            var options = new FileUploadOptions();
                            options.params = {
                                name: file.name
                            };
                            options.headers = {
                                authorization: JSON.parse(window.localStorage.getItem('authentication')).access_token
                            };

                            // 开始上传
                            $cordovaFileTransfer
                                .upload(encodeURI(transferServer), file.uri, options)
                                .then(function (transferRes) {
                                    console.log(JSON.stringify(transferRes));

                                    file.size = formatSize(transferRes.bytesSent);
                                    // 上传完成 开始合并
                                    var data = {
                                        "name": file.name,
                                        "uploadToFDFS": "uploadToFDFS",
                                        "platformSysUserId": 15,
                                        "mac": uuid
                                    };
                                    $http({
                                        method: 'POST',
                                        url: mergeServer,
                                        data: data
                                    })
                                        .success(function (mergeRes) {
                                            // 合并完成
                                            console.log(JSON.stringify(mergeRes));

                                            scope.start();
                                            file.progress = '已上传';
                                            file.state = 1;
                                            file.fsId = mergeRes.result;

                                            scope.getFsIds();
                                        })
                                        .error(function (mergeErr) {
                                            // 合并失败
                                            console.error(JSON.stringify(mergeErr));

                                            scope.start();
                                            file.progress = '上传失败';
                                            file.state = -1;
                                        });
                                }, function (transferErr) {
                                    console.error(JSON.stringify(transferErr));

                                    scope.start();
                                }, function (progress) {
                                    setTimeout(function () {
                                        file.progress = ((progress.loaded / progress.total) * 100).toFixed(0) - 15 + '%';
                                        file.state = 0;
                                    });
                                });
                        };
                        scope.deleteFile = function (e, file) {
                            e.preventDefault();
                            e.stopPropagation();
                            if (file.fsId) {
                                $http({
                                    method: 'GET',
                                    url: deleteServer + file.fsId
                                })
                                    .success(function (res) {
                                        if (res.result && res.result == 'success') {
                                            scope.removeFile(file)
                                        } else {
                                            console.error('delete error');
                                        }
                                    })
                                    .error(function (err) {
                                        console.error(JSON.stringify(err));
                                    });
                            } else {
                                console.log('remove local file');
                                scope.removeFile(file);
                            }
                        };
                        scope.removeFile = function (picture) {
                            angular.forEach(scope.files, function (file, key) {
                                if (angular.equals(file, picture)) {
                                    scope.files.splice(key, 1);
                                    scope.queue.index <= 0 ? scope.queue.index = 0 : scope.queue.index -= 1;

                                    scope.getFsIds();
                                }
                            })
                        };
                        scope.getFsIds = function () {
                            var fsIds = [];
                            console.log(JSON.stringify(scope.files));
                            angular.forEach(scope.files, function (file) {
                                if (file.fsId) {
                                    fsIds.push(file.fsId);
                                }
                            });
                            scope.initFiles = fsIds.join(',');
                        };

                        scope.preview = function (file) {
                            var options = {};

                            console.log(JSON.stringify(file));
                            // 检查文件是否存在
                            $cordovaFile.checkFile(downLoadPath, file.name)
                                .then(function (success) {
                                    console.log(JSON.stringify(success));

                                    // 存在，则直接打开
                                    scope.openFile(file);
                                }, function (error) {
                                    console.log(JSON.stringify(error));

                                    // 不存在 则进行下载
                                    $cordovaFileTransfer.download(file.uri, encodeURI(downLoadPath + file.name), options, true)
                                        .then(function (result) {
                                            console.log(JSON.stringify(result));

                                            // 下载完成 默认打开
                                            scope.openFile(file);
                                        }, function (err) {
                                            console.error(JSON.stringify(err));

                                        }, function (progress) {

                                        });
                                });
                            $ionicScrollDelegate.resize();
                        };

                        scope.openFile = function (file) {
                            var cordovaFileOpen = cordova.plugins.disusered.open;
                            cordovaFileOpen(downLoadPath + file.name, cordovaFileOpenSuccess, cordovaFileOpenError);
                            function cordovaFileOpenSuccess() {
                                console.log('Open success! ' + downLoadPath + file.name);
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
                        };

                    }, false);
                }
            }
        })
})();
