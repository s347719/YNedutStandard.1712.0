/**
 * Created by wuhaiying on 2017/3/13.
 */
(function(){
    'use strict';

    angular
        .module('myApp')
        .controller('outworkSignCtrl', outworkSignCtrl);
    outworkSignCtrl.$inject = ['$scope','$ionicModal','$timeout','$http','$filter','$ionicLoading','$ionicPlatform','stringsConstant','$cordovaFileTransfer','$cordovaFile','$ionicPopup'];
    function outworkSignCtrl($scope,$ionicModal,$timeout,$http,$filter,$ionicLoading,$ionicPlatform,stringsConstant,$cordovaFileTransfer,$cordovaFile,$ionicPopup){
        //外勤打卡成功状态
        $ionicModal.fromTemplateUrl('sign-success.html',{
            scope: $scope
        }).then(function(modal){
            $scope.modalOne = modal;
        });

        $scope.showLoading = function () {
            $ionicLoading.show({
                template:"<ion-spinner class='spinner-theme'></ion-spinner><h4 class='text-theme'>加载中</h4>"
            });
        }
        $scope.hideLoading = function () {
            $ionicLoading.hide();
        }

        $scope.openModalOne = function(){
            $scope.modalOne.show();
            $timeout(function() {
                $scope.modalOne.hide(); //hide the modal after 3 seconds for some reason
            }, 3000);
        };
        $scope.closeModalOne = function(){
            $scope.modalOne.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalOne.remove();
        });

        $scope.record = 'record';
        $scope.signIn = 'signIn';
        $scope.pageContent = [];
        $scope.item = {};
        ($scope.changeTab = function (type) {
            $scope.type = type;
            type == $scope.signIn ? $("#main").removeClass("outwork-sign-history") : $("#main").addClass("outwork-sign-history");
        })($scope.signIn);



        /**
         *  获取 数据方法
         * @param url  路径
         * @param params get数据
         * @param data post 数据
         * @param fun 访问成功 方法
         * @param errFun 访问失败方法
         */
        var httpVisit = function (url,params,data,fun,errFun){
            console.log(JSON.stringify(data));
            params = params || angular.noop;
            data = data || angular.noop;
            fun = fun || angular.noop;
            errFun = errFun || angular.noop;
            var config= {
                url: basePath + "/third/fieldSignin/" + url,
                method:"post",
                params:params, //后台用 get接收
                data: data //后台用@RequestBody 接收
            };
            $http(config).success(function(resultData){
                fun && fun(resultData);
            }).error(function (resultData) {
                $scope.isError = true;
                errFun && errFun(resultData);
            });
        };

        var successFun = function (resultData) {
            $scope.pageData = resultData.result;
            $scope.pageContent = $scope.pageContent.concat(resultData.result.content);
            $scope.isLoadData = false;
            $timeout(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
            },1000);
        };

        var errorFun = function (resultData) {
            $scope.errorFlag = true;
            if ($scope.page.pageNumber>0)
                $scope.page.pageNumber--;
        };

        $scope.page = {pageSize:20,pageNumber:0};

        ($scope.loadData = function () {
            $scope.errorFlag = false;
            httpVisit('findFieldSigninRecord',$scope.page,{}, successFun,errorFun);
        })();

        $scope.saveSignIn = function () {
            if (!$scope.item.currTime || $scope.item.currTime.length==0) {
                $scope.notifyMsg("打卡时间无效，请刷新时间");
                return ;
            }
            if ($scope.addressFlag==1) {
                $scope.notifyMsg(stringsConstant.gps.progress);
                return ;
            }
            $scope.showLoading();

            // 提交gps数据至 云端  临时方案
            console.log(JSON.stringify($scope.gpsData));
            if($scope.gpsData){
                var serverAddress = JSON.parse(window.localStorage.getItem('authentication')).originUrl;
                $http
                    .get(serverAddress + '/third/version/getCurVersion.htm')
                    .success(function(serverInfoData){
                        $http.post(serverInfoData.result.cloudHostAndPost + '/sfjc/rydwformsg/collectLocation.htm?'+$scope.gpsData)
                            .success(function(resultData){
                                console.log(JSON.stringify(resultData))
                            })
                            .error(function (errorData) {
                                console.log(JSON.stringify(errorData))
                            });
                    })
                    .error(function(serverInfoError){
                        console.log(JSON.stringify(serverInfoError))
                    });
            }

            httpVisit('signIn',null,$scope.item, function (data) {
                if (data.status==0) {
                    $scope.hideLoading();
                    setTimeout(function () {
                        $scope.openModalOne();
                        $scope.pageContent.unshift(data.result);
                        $scope.pageData.totalElements = parseInt($scope.pageData.totalElements) + 1;
                        $scope.item = {
                            userId:$scope.item.userId,
                            userName:$scope.item.userName,
                            gpsAddress:$scope.address
                        };
                        $scope.changeTab($scope.record);
                        //数据提交成功后，清除之前的打卡数据
                        $scope.photos = [];
                        $scope.buildFillPhotos();
                        $scope.getFsIds();

                        $scope.$apply();
                    },200);
                }
            }, function (data) {
                $scope.notifyMsg("打卡失败，服务器异常");
                console.log(JSON.stringify(data));
            });
        };

        var intervalId;
        ($scope.getCurrTime = function(){
            $scope.item.currTime = null;
            var base = 1000;
            intervalId && clearInterval(intervalId);
            httpVisit('getCurrTimeAndUser',{},{}, function (data) {
                var currTime = new Date(data.result.signinTime);
                $scope.serverTime = currTime;
                $scope.item.userName = data.result.userName;
                $scope.item.userId = data.result.userId;
                intervalId = window.setInterval(function () {
                    currTime.setTime(currTime.getTime()+1000);
                    $scope.item.currTime = $filter('date')(currTime,'HH:mm:ss');
                    $scope.$apply();
                },base);
            });
        })();

        $scope.imgUrl = function (fsId) {
            return stringsConstant.file.downloadServer() + fsId;
        };

        $scope.loadDataMore = {
            flag: true,
            loadMore: function () {
                $scope.isLoadData = true;
                if ($scope.pageData && $scope.pageContent.length!=$scope.pageData.totalElements) {
                    $scope.page.pageNumber++;
                    $scope.loadData();
                }
            }
        };

        /**
         * 信息提示框
         * @param msg 需要显示的信息
         */
        $scope.notifyMsg = function(msg){
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 800);
        };

        $scope.loadGPSAddress = function () {
            $scope.cityCode  = '';
            $scope.addressFlag = 1;//定位中
            $scope.address = stringsConstant.gps.progress;
            navigator.geolocation.getCurrentPosition(function(position){
                console.log('定位成功geolocation：' + JSON.stringify(position));
                if(position.timestamp){
                    $http.jsonp('http://api.map.baidu.com/geocoder/v2/?ak=kdGPrRp7Ufrv5thTKbSCDEnkSiNFkRg8&callback=JSON_CALLBACK&output=json', {
                        params: {
                            location: position.coords.latitude+','+position.coords.longitude
                        }
                    }).success(function (data) {
                        var addressComponent = data.result.addressComponent;
                        $scope.cityCode  = addressComponent.adcode;
                        $scope.item.gpsAddress = $scope.address = data.result.formatted_address + data.result.sematic_description;
                        $scope.distance = addressComponent.distance;
                        $scope.addressFlag = 2;//定位成功

                               // 将数据提交至 cloud  临时处理方案
                               var userId = JSON.parse(window.localStorage.getItem('authentication')).platformSysUserId;
                               $scope.gpsData =
                               'errorRange='+position.coords.accuracy+
                               '&address='+$scope.address+
                               '&lastCollectTime='+$filter('date')(new Date(position.timestamp),'yyyy-MM-dd HH:mm:ss')+
                               '&longAndLat='+position.coords.longitude+','+position.coords.latitude+
                               '&gpsIsOpen=0'+
                               '&rsglSysUserId='+userId;

                    }).error(function(geocoderError){
                        console.error('定位失败geocoder: ' + JSON.stringify(geocoderError));
                        gpsError();
                    });

                }else{
                    console.error('定位失败: timestamp is null');
                    gpsError();
                }
            },function(getCurrentPositionError){
                console.error('定位失败geolocation:' + JSON.stringify(getCurrentPositionError));
                gpsError();
            });
        };
        function gpsError(){
            $scope.cityCode  = '';
            // 设置延迟，视觉缓冲
            setTimeout(function(){
                $scope.item.gpsAddress = $scope.address = stringsConstant.gps.error;
                $scope.addressFlag = 3;//定位失败
                $scope.$digest();
            },500)
        }
        $ionicPlatform.ready(function(){
            $scope.loadGPSAddress();
        });


        document.addEventListener("deviceready", onDeviceReady, false);
        function onDeviceReady() {
            console.log("deviceready");

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
            $scope.buildFillPhotos = function(){
                $scope.fillPhotos = [];
                for(var i=0;i<5-$scope.photos.length;i++){
                    $scope.fillPhotos.push(i);
                }
            };
            $scope.buildFillPhotos();

            // 得到照片字符串
            $scope.getFsIds = function(){
                var photosFsIds = [];
                for(var i = 0; i < $scope.photos.length; i++) {
                    if($scope.photos[i].fsId){
                        photosFsIds.push($scope.photos[i].fsId);
                    }
                }
                $scope.item.photoList = photosFsIds;
                console.log(JSON.stringify($scope.item.photoList));
            };

            // 拍照上传
            $scope.takePhoto = function(){
                if (!$scope.item.currTime || $scope.item.currTime.length==0) {
                    $scope.notifyMsg("打卡时间无效，请刷新时间");
                    return ;
                }
                if($scope.photos.length==5){
                    $ionicPopup.alert({
                        title: '拍照提示',
                        template: '最多只能拍摄5张照片'
                    });
                    return;
                }
                //成功拍照 组装photo对象 push到 photos
                var customcamera = yn.plugin.customcamera;
                var opts = [{
                    name: $scope.item.userName,
                    address: $scope.address,
                    time: $filter('date')($scope.serverTime,'yyyy/MM/dd 星期' + ['日','一','二','三','四','五','六'][$scope.serverTime.getDay()] + ' HH:mm:ss')
                }];
                customcamera.startCamera(opts,takeSuccess,takeError);
            };
            // 拍照完成
            function takeSuccess(data){
                var photo = {
                    uri: data,
                    path: data.substr(0,data.lastIndexOf('/')+1),
                    name: data.substr(data.lastIndexOf('/')+1),
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
            function takeError(data){
                console.error('拍照失败');
                console.error(JSON.stringify(data));
            }
            // 删除照片
            $scope.deletePhoto = function(photo){
                $ionicPopup.confirm({
                    title: '删除确认',
                    template: '确认要删除当前照片吗?',
                    cancelText:'取消',
                    okText:'删除'
                }).then(function(res) {
                    if(res) {
                        if (photo.fsId) {
                            // 删除服务端文件
                            $http({
                                method: 'GET',
                                url: stringsConstant.file.deleteServer() + photo.fsId
                            })
                                .success(function (deletePhotoRes) {
                                    if (deletePhotoRes.result && deletePhotoRes.result == 'success') {
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
            $scope.deleteLocalPhoto = function(photo) {
                $cordovaFile
                    .removeFile(photo.path, photo.name)
                    .then(function (removeFileSuccess) {
                        // 已从本地删除文件
                        for(var i = 0; i < $scope.photos.length; i++) {
                            if($scope.photos[i].uri == photo.uri) {
                                $scope.photos.splice(i,1);
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
            $scope.previewPhoto = function(photo){
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
                            template: '预览时发生了未知错误 #'+code
                        });
                    }
                }
            };
            // 单个照片失败重试
            $scope.retry = function(photo){
                console.log('失败文件重试: ' + photo.uri);
                if(photo.status == '-1'){
                    $scope.upload(photo);
                }
            };
            // 单个上传
            $scope.upload = function(photo){
                photo.status = '0';
                // 参数
                var mac = stringsConstant.utils.uuid();
                var platformSysUserId = JSON.parse(window.localStorage.getItem('authentication')).platformSysUserId;
                var options = new FileUploadOptions();
                options.params = {
                    name: photo.name,
                    platformSysUserId: platformSysUserId,
                    mac: mac
                };
                options.headers = {
                    authorization: JSON.parse(window.localStorage.getItem('authentication')).access_token
                };
                $cordovaFileTransfer.upload(stringsConstant.file.uploadServer(), photo.uri, options)
                    .then(function(uploadSuccess) {
                        // 上传完成，开始合并文件
                        var data = {
                            "name": photo.name,
                            "uploadToFDFS": "uploadToFDFS",
                            "platformSysUserId": platformSysUserId,
                            "mac": mac
                        };
                        $http({
                            method: 'POST',
                            url: stringsConstant.file.mergeServer(),
                            data: data
                        })
                            .success(function (mergeRes) {
                                // 合并完成
                                photo.fsId = mergeRes.result;
                                photo.status = '1';
                                // 允许继续拍照
                                $scope.getFsIds();
                            })
                            .error(function (mergeErr) {
                                console.error('文件合并失败');
                                console.error(JSON.stringify(mergeErr));
                                photo.status = '-1';
                                // 允许继续拍照
                            });
                    }, function(uploadError) {
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



    }
})();
