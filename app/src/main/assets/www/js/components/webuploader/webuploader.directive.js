angular.module('starter')
    .directive('webUploader', ['$compile', '$http', '$ionicModal','$rootScope','$ionicPopup', function ($compile, $http, $ionicModal,$rootScope,$ionicPopup) {
        return {
            restrict: 'A',
            scope: {
                initFiles: '=',
                onUploaderChange: '&onUploaderChange',
                "options":"="
            },
            link: function (scope, element, attrs) {

                function createID() {
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

                var uploaderID = createID();

                var defaultOpts = {};

                var $imagesUpload = angular.element(
                    '<div class="image-upload-wrap clearfix">' +
                    '<ul class="image-list">' +
                    '<li class="picker-wrap">' +
                    '<div class="imagePicker" id="' + uploaderID + '">+</div>' +
                    '</li>' +
                    '</ul>' +
                    '</div>'
                );
                var $imageList = $imagesUpload.find('.image-list');
                var $pickerWrap = $imagesUpload.find('.picker-wrap');
                element.append($imagesUpload);

                scope.uploadedRes = {
                    msg: '',
                    data: []
                };


                function generateList(file, isInitFiles) {
                    if (!isInitFiles) {
                        var $li = angular.element(
                            '<li id="' + file.id + '">' +
                            '<div class="image">' +
                            '<img src="img/face.bmp"/>' +
                            '<div class="progress">' +
                            '<div class="progress-bar"><span></span></div>' +
                            '</div>' +
                            '</div>' +
                            '<span class="delete">-</span>' +
                            '</li>'
                        );
                    } else {
                        var $li = angular.element(
                            '<li id="' + new Date().getTime() + '">' +
                            '<div class="image">' +
                            '<img src="' + file.src + '" id="' + file.fsId + '"/>' +
                            '<div class="progress">' +
                            '<div class="progress-bar" style="width: 100%"><span>已上传</span></div>' +
                            '</div>' +
                            '</div>' +
                            '<span class="delete">-</span>' +
                            '</li>'
                        );
                    }

                    var $img = $li.find('img'),
                        $del = $li.find('.delete');
                    $pickerWrap.before($li);
                    $del.on('click', function () {
                        var confirmPopup = $ionicPopup.confirm({
                            title:"提示",
                            template: '确定要删除图片吗？',
                            cancelText:"取消",
                            okText:"确认"
                        });
                        confirmPopup.then(function(res) {
                            if(res) {
                                if ($img.attr('id')) {
                                    var fsId = $img.attr('id');
                                    angular.forEach(scope.uploadedRes.data, function (v, k) {
                                        if (v == fsId) {
                                            scope.uploadedRes.data.splice(k, 1);
                                        }
                                    });
                                    scope.onUploaderChange({data: scope.uploadedRes});
                                    if (!isInitFiles) {
                                        uploader.removeFile(file, true);
                                    }
                                    $li.remove();
                                    //$http.post(originBaseUrl + '/file/delete.htm', {
                                    //    fastDFSId: fsId
                                    //})
                                    //    .success(function (res) {
                                    //        if (res == 'success') {
                                    //            angular.forEach(scope.uploadedRes.data, function (v, k) {
                                    //                if (v == fsId) {
                                    //                    scope.uploadedRes.data.splice(k, 1);
                                    //                }
                                    //            });
                                    //            scope.onUploaderChange({data: scope.uploadedRes});
                                    //            if (!isInitFiles) {
                                    //                uploader.removeFile(file, true);
                                    //            }
                                    //            $li.remove();
                                    //        } else {
                                    //            console.log('delete - response nothing');
                                    //        }
                                    //    })
                                    //    .error(function (err) {
                                    //        console.log(err);
                                    //        //do something ...
                                    //    });
                                } else {
                                    if (!isInitFiles) {
                                        uploader.removeFile(file, true);
                                    }
                                    $li.remove();
                                }
                            }
                        });

                    });
                    $img.on('click', function () {
                        if(!$img.attr('id')){
                            uploader.retry(file);
                        }else{
                            scope.picUrl = $img.attr('src');
                            if($img.attr('src') == 'img/file.png'){
                                scope.picUrl = 'img/file_1.png'
                            }
                            scope.openModal();
                        }
                    });
                    // 创建缩略图
                    if (!isInitFiles) {
                        uploader.makeThumb(file, function (error, src) {
                            if (error) {
                                $img.replaceWith('<span>不能预览</span>');
                                return;
                            }
                            $img.attr('src', src);
                        },1,1);
                    }
                }

                // 初始化
                var uploader = WebUploader.create({
                    auto: true,
                    server: originBaseUrl + '/file/thirdUpload.htm?userId=' + $rootScope.authorizationStr.userId,
                    pick: '#' + uploaderID,
                    chunked: false,
                    accept: {
                        title: 'Images',
                        extensions: 'gif,jpg,jpeg,bmp,png',
                        mimeTypes: 'image/*'
                    }
                });

                // 当有文件被添加进队列的时候
                uploader.on('fileQueued', function (file) {
                    if(/image./.test(file.name)){
                        file.name = file.id.toLowerCase() + '.' + file.ext;
                    }
                    generateList(file);
                });
                // 文件上传过程中创建进度条实时显示。
                uploader.on('uploadProgress', function (file, percentage) {
                    //scope.uploadedRes.msg = 'uploading';
                    var $percentBar = angular.element('#' + file.id).find('.progress-bar');
                    $percentBar.css('width', (percentage * 100) - 25 + '%');
                });
                // 文件上传成功，给item添加成功class, 用样式标记上传成功。
                uploader.on('uploadSuccess', function (file) {
                    var $li = angular.element('#' + file.id),
                        $img = $li.find('img'),
                        $progress = $li.find('.progress'),
                        $percentBar = $li.find('.progress-bar'),
                        $percent = $percentBar.find('span');
                        $progress.removeClass('error');
                        $percent.text('');
                    $http.post(originBaseUrl + '/file/thirdMergeFile.htm', {
                        "id": file.id,
                        "name": file.name,
                        "size": file.size,
                        "type": file.type,
                        "uploadToFDFS": "uploadToFDFS"
                    })
                        .success(function (res) {
                            var $li = angular.element('#' + file.id),
                                $img = $li.find('img'),
                                $progress = $li.find('.progress'),
                                $percentBar = $li.find('.progress-bar'),
                                $percent = $percentBar.find('span');
                            if (res) {
                                $img.prop('id', res);
                                scope.uploadedRes.data.push(res);
                                $percentBar.css('width', '100%');
                                $progress.removeClass('error');
                                $percent.text('已上传');
                                //if ((uploader.getStats().successNum - uploader.getStats().cancelNum) == scope.uploadedRes.data.length) {
                                //    scope.uploadedRes.msg = 'success';
                                //} else {
                                //    scope.uploadedRes.msg = 'uploading';
                                //}
                                scope.onUploaderChange({data: scope.uploadedRes})
                            } else {
                                $percent = $percentBar.find('span');
                                $progress.addClass('error');
                                $percentBar.css('width', '100%');
                                $percent.text('重试');
                            }
                        })
                        .error(function (err) {
                            console.log(err);
                        });
                });
                // 文件上传失败，显示上传出错。
                uploader.on('uploadError', function (file) {
                    var $li = angular.element('#' + file.id),
                        $progress = $li.find('.progress'),
                        $percentBar = $progress.find('.progress-bar');
                    $percent = $percentBar.find('span');
                    $progress.addClass('error');
                    $percentBar.css('width', '100%');
                    $percent.text('重试');
                });
                // 完成上传完了，成功或者失败
                uploader.on('uploadComplete', function (file) {
                    // do something
                });


                // 预览
                scope.viewHeight = angular.element(window).height() - 43;
                var template = '<ion-modal-view class="images-view">' +
                    '<ion-header-bar align-title="center">' +
                    '<button class="button button-clear button-dark" ng-click="closeModal()"><i class="ion-chevron-left"></i>返回</button>' +
                    '<h1 class="title">预览</h1>' +
                    '</ion-header-bar>' +
                    '<ion-content>' +
                    '<ion-spinner class="screen-center" ng-if="spinner"></ion-spinner>' +
                    '<img ng-src="{{picUrl}}" style="opacity: 0" />' +
                    '</ion-content>' +
                    '</ion-modal-view>';
                scope.pictureModel = $ionicModal.fromTemplate(template, {
                    scope: scope,
                    animation: 'slide-in-up'
                });
                scope.openModal = function () {
                    if (scope.modelIsRemove) {
                        scope.pictureModel = $ionicModal.fromTemplate(template, {
                            scope: scope,
                            animation: 'slide-in-up'
                        });
                    }
                    scope.pictureModel.show().then(function () {
                        scope.spinner = true;
                        var $img = angular.element('ion-modal-view').find('img');
                        var pinchOpts = {};
                        if($img.attr('src') == 'img/file_1.png'){
                            pinchOpts = {
                                maxZoom : 1,
                                minZoom : 1
                            }
                        }
                        $img.imagesLoaded( function() {
                            new RTP.PinchZoom($img, pinchOpts);
                            $img.css('opacity','1');
                            scope.spinner = false;
                            scope.$apply();
                        });
                        scope.modelIsRemove = false;
                    });
                };
                scope.closeModal = function () {
                    scope.pictureModel.remove().then(function () {
                        scope.modelIsRemove = true;
                    });
                };
                // Cleanup the modal when we're done with it!
                scope.$on('$destroy', function () {
                    scope.pictureModel.remove();
                });

                /**
                 * 初始化条件
                 */
                scope.initLinkFiles = function(){
                    if (scope.initFiles && scope.initFiles.length) {
                        scope.uploadedRes.data = scope.initFiles;
                        angular.forEach(scope.initFiles, function (v, k) {
                            var _src = originBaseUrl + '/file/downloadStream.htm?fastDFSId=' + v;
                            if(!(/\.(?:jpg|png|bmp|gif|jpeg)$/i).test(v)){
                                _src = 'img/file.png'
                            }
                            generateList({
                                src: _src,
                                fsId: v
                            }, true);
                        });
                        scope.onUploaderChange({data: scope.uploadedRes})
                    }
                };

                /**
                 * 回调函数
                 */
                if(scope.options){
                    scope.options.initBackFile = function(fileids){
                        if(angular.isArray(fileids)){
                            scope.initFiles = angular.copy(fileids);
                        }
                        scope.initLinkFiles();
                    };
                }else{
                    scope.initLinkFiles();
                }
            }
        }
    }])





    .config(function($stateProvider){
        $stateProvider
            .state('webuploader', {
                url: '/webuploader',
                templateUrl: 'js/components/webuploader/demo.html',
                controller: 'WebuploaderCtrl'
            })
    })
    .controller('WebuploaderCtrl', function ($scope, $ionicPopover) {
        /* Demo1 */
        $scope.msg1 = '';
        $scope.fsIds1 = [];
        $scope.getDfsIds1 = function (data) {
            $scope.msg1 = data.msg;
            $scope.fsIds1 = data.data;
        };

        /* Demo2 */
        //初始化已有图片 - 编辑的时候会用到
        $scope.initFiles2 = [
            'M00/09/8C/CgYAwVeRmiOARSokAALo0CI1c4U130.jpg',
            'M00/09/8C/CgYAwVeRmiOAc_HSAANzTyFzOjw461.jpg',
            'M00/09/8C/CgYAwVeRmgaAW6onAANTma8hdxA687.jpg',
            'M00/09/90/CgYAwVekMMuAFlVRAAL7NToGu9k026.png'
        ];
        $scope.msg2 = '';
        $scope.fsIds2 = [];
        $scope.getDfsIds2 = function (data) {
            $scope.msg2 = data.msg;
            $scope.fsIds2 = data.data;
        };

    });
