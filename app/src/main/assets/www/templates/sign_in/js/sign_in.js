/**
 * 打卡
 *
 * @author Jin Tan
 * @date 2017/12/19
 */

(function () {
    'use strict';

    angular.module('starter').config(stateConfig).controller('signInCtrl', signInCtrl);

    stateConfig.$inject = ['$stateProvider'];

    /**
     * 路由配置
     *
     * @param $stateProvider
     */
    function stateConfig($stateProvider) {
        $stateProvider.state('sign_in', {
            firstPage: true,
            url: '/sign_in',
            templateUrl: 'templates/sign_in/sign_in.html',
            controller: 'signInCtrl',
            controllerAs: 'vm'
        });
    }

    signInCtrl.$inject = ['$scope', '$http', '$interval', '$filter', 'ynuiNotification', 'stringsConstant', '$cordovaFileTransfer', '$cordovaFile', '$timeout', '$ionicPopup'];

    function signInCtrl($scope, $http, $interval, $filter, ynuiNotification, stringsConstant, $cordovaFileTransfer, $cordovaFile, $timeout, $ionicPopup) {

        var vm = this;

        vm.findCurrentTimeAndUser = findCurrentTimeAndUser;
        vm.findAddressByLongAndLat = findAddressByLongAndLat;
        vm.findTaskPointByConditions = findTaskPointByConditions;
        vm.initPageData = initPageData;
        vm.findSignIn = findSignIn;
        vm.submit = submit;

        // 默认tab_1，tab = '2' 为 tab_2
        vm.tab = '1';
        // 默认不显示更多
        vm.showMore = false;
        // 获取数据状态，0失败，1获取中，2获取成功
        vm.dataStatus = '1';
        // 获取时间状态，0失败，1获取中，2获取成功
        vm.timeStatus = '1';
        // 页面显示数据
        vm.pageData = {
            address: ''
        };
        // 保存的打卡点
        vm.dataVO = {
            longAndLat: '',
            gpsIsOpen: '1',
            address: '',
            // 手机号码取不到
            phoneNumber: '',
            errorRange: '',
            fastDFSIds: '',
            remark: ''
        };
        vm.monthList = [];
        vm.pageSize = 0;
        vm.pageNumber = 0;
        vm.signInStatus = 1;

        vm.initPageData();
        vm.findSignIn();

        /**
         * 获取系统时间和当前的登陆人员
         */
        function findCurrentTimeAndUser() {
            return $http.get(originBaseUrl + '/third/rydwForMsg/findCurrentTimeAndUser.htm').then(function (response) {
                if (response.status === 200 && response.data.status === 0) {
                    vm.timeStatus = '2';
                } else {
                    vm.timeStatus = '0';
                }
                return response;
            });
        }

        /**
         * 根据经纬度查询实际地址
         *
         * @param latitude  纬度
         * @param longitude 经度
         */
        function findAddressByLongAndLat(latitude, longitude) {
            var url = 'http://api.map.baidu.com/geocoder/v2/?ak=kdGPrRp7Ufrv5thTKbSCDEnkSiNFkRg8' +
                '&callback=JSON_CALLBACK&output=json';
            return $http.jsonp(url, {
                params: {
                    location: latitude + ',' + longitude
                }
            }).success(function (data) {
                vm.dataStatus = '2';
                vm.pageData.address = data.result.formatted_address + data.result.sematic_description;
                vm.dataVO.address = vm.pageData.address;
            }).error(function (error) {
                vm.dataStatus = '1';
                console.error('error: ' + JSON.stringify(error));
            });
        }

        /**
         * 根据人员id查询需要打卡的点
         *
         * @param longitudeAndLatitude 经纬度
         */
        function findTaskPointByConditions(longitudeAndLatitude) {
            return $http.get(originBaseUrl + '/third/rydwForMsg/findTaskPointByConditions.htm', {
                params: {
                    longitudeAndLatitude: longitudeAndLatitude
                }
            }).then(function (response) {
                return response;
            })
        }

        /**
         * 初始化页面数据
         */
        function initPageData() {
            // 获取当前时间和打卡人员信息
            vm.findCurrentTimeAndUser().then(function (response) {
                var currentTime = new Date(response.data.result.currentTime);
                vm.pageData.currentTime = $filter('date')(currentTime, 'HH:mm:ss');
                vm.pageData.userName = response.data.result.platformSysUserVO.userName;
                $interval(function () {
                    // + 1s
                    currentTime.setTime(currentTime.getTime() + 1000);
                    vm.pageData.currentTime = $filter('date')(currentTime, 'HH:mm:ss');
                    $scope.currentTime = currentTime;
                }, 1000);
            }).then(function () {
                if ('2' === vm.timeStatus) {
                    // 获取当前定位
                    navigator.geolocation.getCurrentPosition(function (position) {
                        if (position.timestamp) {
                            vm.dataVO.longAndLat = position.coords.longitude + ',' + position.coords.latitude;
                            vm.dataVO.gpsIsOpen = '1';
                            // 取真实地址
                            vm.findAddressByLongAndLat(position.coords.latitude, position.coords.longitude);
                            var param = position.coords.longitude + ',' + position.coords.latitude;
                            // 取任务打卡点
                            vm.findTaskPointByConditions(param).then(
                                function (response) {
                                    vm.pageData.taskPointVOList = response.data.result;
                                }
                            )
                        } else {
                            vm.dataVO.gpsIsOpen = '0';
                            vm.dataStatus = '0';
                            console.error('error: timestamp is null');
                        }
                    }, function (error) {
                        vm.dataStatus = '0';
                        console.log(error);
                    });
                }
            });
        }

        /**
         * 查询一个人的打卡记录
         */
        function findSignIn() {
            vm.pageSize += 20;
            return $http.get(originBaseUrl + '/third/rydwForMsg/findSignIn.htm', {
                params: {
                    pageNumber: vm.pageNumber,
                    pageSize: vm.pageSize
                }
            }).then(function (response) {
                if (response.data.result.length === vm.monthList.length) {
                    vm.signInStatus = 1;
                } else {
                    vm.signInStatus = 0;
                }
                vm.monthList = response.data.result;
                angular.forEach(vm.monthList, function (month) {
                    angular.forEach(month.signInList, function (sign) {
                        if (null !== sign.fastDFSIds) {
                            var fastDFSIds = sign.fastDFSIds.split(',');
                            var photoSrcList = [];
                            angular.forEach(fastDFSIds, function (fastDFSId) {
                                photoSrcList.push(stringsConstant.file.downloadServer() + fastDFSId);
                            });
                            sign.photoSrcList = photoSrcList;
                        }
                    })
                })
            })
        }

        // ====================拍照====================
        /*
         * 照片对象
         * {
         *   uri: ''  // 本地全路径
         *   path: ''    // 本地路径
         *   name: ''    // 文件名
         *   fsId: ''    // 文件服务器id
         *   status: 0  // 状态
         *   progress: 0    // 上传进度
         * }
         * */
        $scope.photos = [];    //照片
        $scope.fillPhotos = [];     //空格子填充

        // 构造空格子
        $scope.buildFillPhotos = function () {
            $scope.fillPhotos = [];
            for (var i = 0; i < 5 - $scope.photos.length; i++) {
                $scope.fillPhotos.push(i);
            }
        };
        $scope.buildFillPhotos();

        // 得到照片字符串
        $scope.getFsIds = function () {
            var photosFsIds = [];
            for (var i = 0; i < $scope.photos.length; i++) {
                if ($scope.photos[i].fsId) {
                    photosFsIds.push($scope.photos[i].fsId);
                }
            }
            vm.dataVO.fastDFSIds = photosFsIds.join(',');
            console.log(JSON.stringify(vm.dataVO.fastDFSIds));
        };

        document.addEventListener("deviceready", onDeviceReady, false);

        function onDeviceReady() {
            console.log("deviceready");

            vm.initPageData();

            // 拍照上传
            $scope.takePhoto = function () {
                if (vm.timeStatus === '0' || vm.timeStatus === '1') {
                    ynuiNotification.error({'msg': '打卡时间无效，无法拍照'});
                    return;
                }
                //成功拍照 组装photo对象 push到 photos
                var customcamera = yn.plugin.customcamera;
                var opts = [{
                    name: $scope.vm.pageData.userName,
                    address: $scope.vm.pageData.address,
                    time: $filter('date')($scope.currentTime, 'yyyy/MM/dd 星期' + ['日', '一', '二', '三', '四', '五', '六'][$scope.currentTime.getDay()] + ' HH:mm:ss')
                }];
                customcamera.startCamera(opts, takeSuccess, takeError);
            };

            // 拍照完成
            function takeSuccess(data) {
                var photo = {
                    uri: data,
                    path: data.substr(0, data.lastIndexOf('/') + 1),
                    name: data.substr(data.lastIndexOf('/') + 1),
                    fsId: '',
                    status: 0,
                    progress: 0
                };
                // 拍照完成
                $scope.photos.push(photo);
                $scope.buildFillPhotos();
                // 执行上传
                $scope.upload(photo);
            }

            // 拍照失败
            function takeError(data) {
                console.error('拍照失败');
                console.error(JSON.stringify(data));
            }

            // 删除照片
            $scope.deletePhoto = function (photo) {
                $ionicPopup.confirm({
                    title: '删除确认',
                    template: '确认要删除当前照片吗?',
                    cancelText: '取消',
                    okText: '删除'
                }).then(function (res) {
                    if (res) {
                        if (photo.fsId) {
                            // 删除服务端文件
                            $http({
                                method: 'GET',
                                url: stringsConstant.file.deleteServer() + photo.fsId
                            })
                                .success(function (deletePhotoRes) {
                                    console.log(JSON.stringify(deletePhotoRes));
                                    if (deletePhotoRes.status == 0) {
                                        // 已从文件服务器删除文件，执行本地删除
                                        $scope.deleteLocalPhoto(photo);
                                    }
                                })
                                .error(function (deletePhotoErr) {
                                    console.error('从文件服务器删除文件失败');
                                    console.error(JSON.stringify(deletePhotoErr));
                                });
                        } else {
                            // 删除本地文件
                            $scope.deleteLocalPhoto(photo);
                        }
                    }
                });

            };
            // 删除本地照片
            $scope.deleteLocalPhoto = function (photo) {
                $cordovaFile
                    .removeFile(photo.path, photo.name)
                    .then(function (removeFileSuccess) {
                        // 已从本地删除文件
                        for (var i = 0; i < $scope.photos.length; i++) {
                            if ($scope.photos[i].uri == photo.uri) {
                                $scope.photos.splice(i, 1);
                                $scope.buildFillPhotos();
                                $scope.getFsIds();
                            }
                        }
                    }, function (removeFileError) {
                        // error
                        console.error('从本地删除文件失败');
                        console.error(JSON.stringify(removeFileError));
                    });
            };
            // 预览照片
            $scope.previewPhoto = function (photo) {
                var cordovaFileOpen = cordova.plugins.disusered.open;
                cordovaFileOpen(photo.uri, cordovaFileOpenSuccess, cordovaFileOpenError);

                function cordovaFileOpenSuccess() {
                    // 从已安装程序中打开文件完成
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
                            template: '预览时发生了未知错误 #' + code
                        });
                    }
                }
            };
            // 单个照片失败重试
            $scope.retry = function (photo) {
                console.log('失败文件重试: ' + photo.uri);
                if (photo.status == '-1') {
                    $scope.upload(photo);
                }
            };
            // 单个上传
            $scope.upload = function (photo) {
                photo.status = '0';
                // 参数
                var mac = stringsConstant.utils.uuid();
                var platformSysUserId = JSON.parse(window.localStorage.getItem('authentication')).platformSysUserId;
                var options = new FileUploadOptions();
                // options.params = {
                //     name: photo.name,
                //     platformSysUserId: platformSysUserId,
                //     mac: mac
                // };
                options.headers = {
                    authorization: JSON.parse(window.localStorage.getItem("authorizationStr")).access_token
                };
                $cordovaFileTransfer.upload(stringsConstant.file.uploadServer(), photo.uri, options)
                    .then(function (uploadSuccessData) {
                        console.log(JSON.stringify(uploadSuccessData.response));
                        // 上传完成
                        photo.fsId = JSON.parse(uploadSuccessData.response).result.fileId;
                        photo.status = '1';
                        // 允许继续拍照
                        $scope.getFsIds();
                    }, function (uploadError) {
                        console.error('上传失败');
                        console.error(JSON.stringify(uploadError));
                        photo.status = '-1';
                        // 允许继续拍照
                    }, function (progress) {
                        $timeout(function () {
                            photo.progress = parseInt((progress.loaded / progress.total) * 100) + '%';
                            console.log('上传进度: ' + photo.progress);
                        });
                    });
            }
        }

        function submit() {
            // 验证时间是否获取成功
            if (vm.timeStatus === '0') {
                ynuiNotification.error({'msg': '打卡时间无效，请刷新时间'});
                return;
            }
            // 验证定位
            if (vm.dataStatus === '1') {
                ynuiNotification.error({'msg': '正在获取定位信息，请稍后'});
                return;
            }
            // 验证照片
            var photosCount = $scope.photos.length, uploaded = vm.dataVO.fastDFSIds.split(',').length;
            if (photosCount > uploaded) {
                ynuiNotification.error({'msg': '还有' + (photosCount - uploaded) + '张照片没有提交'});
                return;
            }
            // 提交数据
            var dataVO = angular.copy(vm.dataVO);
            $http.post(originBaseUrl + '/third/rydwForMsg/saveSignIn.htm',
                dataVO
            ).then(function (response) {
                if (response.status === 200) {
                    // 后端返回异常
                    if (response.data.status === 1) {
                        ynuiNotification.error({'msg': '打卡失败，' + response.data.message})
                    } else {
                        ynuiNotification.success({'msg': '打卡成功'});
                        vm.tab = '2';
                        vm.findSignIn();
                        // 清空数据
                        $scope.photos = [];
                        vm.dataVO.remark = '';
                    }
                } else {
                    ynuiNotification.error({'msg': '服务器错误，请稍后再试'});
                }
            }, function () {
                ynuiNotification.error({'msg': '服务器错误，请稍后再试'});
            })
        }
    }
})();