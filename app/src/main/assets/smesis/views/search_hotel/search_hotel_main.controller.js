/**
 * project:     yineng-corpSysLand
 * title:      search_hotel_main.controller
 * author:      xiechangwei
 * date:        2016/12/6 14:19
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:出差酒店查询前端控制js
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('searchHotelMainController', searchHotelMainController);

    searchHotelMainController.$inject = ['$ionicPlatform','$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout','$ionicModal'];
    function searchHotelMainController($ionicPlatform,$scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout,$ionicModal){
        /**
        * 酒店详细信息弹层操作
        */
        var template =
            '<ion-modal-view class="bottom-half min-modal-height"> ' +
            '<ion-header-bar class="bar-stable"> ' +
            '<h1 class="title">&nbsp;</h1> ' +
            '<button class="button button-clear button-custom" ng-click="closeModal()">取消</button>' +
            '</ion-header-bar>' +
            ' <ion-content delegate-handle="mainScroll"> ' +
            '<div class="hotel-detail-infor text-center">' +
            ' <h3 class="margin-top-10" ng-bind="hotelDetail.name"></h3>' +
            '<h3 ng-bind="hotelDetail.tel"></h3>' +
            '<div class="margin-top-20">' +
            '<a href="tel:{{hotelDetail.tel}}"><button class="button button-positive btn-icon"><i class="call"></i>拨打电话</button></a>' +
            '</div>' +
            '<h4 ng-click="introduceFun()">酒店介绍</h4>' +
            ' <div><i class="ion ion-chevron-down" ng-if="!isShowIntroduce"></i><i class="ion ion-chevron-up" ng-if="isShowIntroduce"></i></div>' +
            '<p ng-bind="hotelDetail.hotelDes" ng-if="isShowIntroduce"></p>' +
            '</div>' +
            '</ion-content> ' +
            '</ion-modal-view>';

        //根据经纬度获取当前的定位点
        //var signLongAndLat_arr =[30.743508,103.971393];
        $scope.searchId = $location.search().searchId;

        $scope.reLoad = function () {
            $scope.cityCode  = '';
            $scope.address = '定位中...';
            navigator.geolocation.getCurrentPosition(function(position){
                if(position.timestamp){
                    $http.jsonp('http://api.map.baidu.com/geocoder/v2/?ak=kdGPrRp7Ufrv5thTKbSCDEnkSiNFkRg8&callback=JSON_CALLBACK&output=json', {
                        params: {
                            location: position.coords.latitude+','+position.coords.longitude
                        }
                    }).success(function (data) {
                        $scope.cityCode  = data.result.addressComponent.adcode;
                        $scope.address = data.result.addressComponent.province +","+data.result.addressComponent.city+","+data.result.addressComponent.district;
                    })
                }else{
                    $scope.cityCode  = '';
                    // 设置延迟，视觉缓冲
                    setTimeout(function(){
                        $scope.address = '获取当前位置失败，请重试！';
                        $scope.$digest();
                    },500)
                }
            });
        };
        $ionicPlatform.ready(function(){
            $scope.reLoad();
        });

        $scope.provinceName ="请选择省";
        $scope.cityName ="请选择市";
        $scope.showCity = false;
        $scope.areaName ="请选择区";
        $scope.showArea = false;

        //跳转到酒店查询页面
        $scope.serachHotel = function (item) {
            if(item){
                $location.path("/search_hotel").search({cityCode:item,searchId:item});
            }else{
                return;
            }
        }

        //获取最近的选择的地区
        $scope.addrHistory =[];
        $scope.showAddress = false;
        $http.get(basePath + "/third/searchhotel/findHistoryAddressByUserId?index=4&ownedBusiness=mobile").success(function (data) {
            if (data.result.length != 0) {
                $scope.showAddress = true;

                if(data.result.length ==1){
                    $scope.address1 = true;

                }else if(data.result.length ==2){
                    $scope.address1 = true;
                    $scope.address2 = true;
                }else if(data.result.length ==3){
                    $scope.address1 = true;
                    $scope.address2 = true;
                    $scope.address3 = true;
                }else{
                    $scope.address1 = true;
                    $scope.address2 = true;
                    $scope.address3 = true;
                    $scope.address4 = true;
                }
                angular.forEach(data.result,function(newData){
                    if(newData.platCommonAreaId != null){
                        newData.platCommonCityId =newData.platCommonAreaId;
                        newData.platCommonCityName =newData.platCommonAreaName;
                    }
                    $scope.addrHistory.push(newData);


                })

            }
        });
        $scope.newName ="";
        var getShowDateFun = function(){
            return  {
                index:0,
                showName: [{name:"请选择省"}],
                setShowName: function(info, size,type){
                    $scope.newName =info.id;
                    if(type==1){
                        this.showName =[];
                        if(info.parent){
                            this.showName[0] = info.parent.name;
                            this.showName[this.index] = info.parent;
                            this.showName[this.index+1] = info.name;
                            this.showName[this.index+1] = info;
                            this.showName[this.index+2] = {name:"请选择区"};
                        }else{
                            this.showName[this.index] = info.name;
                            this.showName[this.index] = info;
                            this.showName[this.index+1] = {name:"请选择市"};
                            this.showName[this.index+2] = {name:"请选择区"};
                        }
                        if(size > 0 ){
                            if(this.index == 1){
                                this.showName[this.index] = {name:"请选择市"};
                            }else if(this.index == 2){
                                this.showName[this.index] = {name:"请选择区"};
                            }
                        }else{
                            $location.path("/search_hotel").search({cityCode:info.code,searchId:info.parent.id});

                        }
                        return;

                    }
                    this.showName[this.index] = info.name;
                    this.showName[this.index] = info;
                    this.index ++;
                    if(size > 0 ){
                        if(this.index == 1){
                            this.showName[this.index] = {name:"请选择市"};
                            this.showName[this.index] = info;
                        }else if(this.index == 2){
                            this.showName[this.index] = {name:"请选择区"};
                            this.showName[this.index] = info;
                        }
                    }else{
                        $location.path("/search_hotel").search({cityCode:info.code,searchId:info.parent.id});

                    }

                }
            };
        };

        //查询所有的一级城市
        $scope.allCity = function () {
            $scope.showData = getShowDateFun();
            $http.post(basePath + "/third/searchhotel/findAreaDataAndParentIdIsNull?").success(function (data) {
                if (data.status=="0") {
                    $scope.parentAddress = data.result;
                }
            });
        }
        $scope.allCity();
        $scope.showData = getShowDateFun();



       //根据父级id查询子级数据
        $scope.newClass ="";
        $scope.getAddress = function (item,type) {
            $ionicScrollDelegate.resize();
            $scope.newClass = item.id;
            $http.post(basePath + "/third/searchhotel/findAreaDataListByParentId?id="+item.id).success(function (data) {
                if (data.status=="0") {
                    $scope.parentAddress = data.result;
                    $http.post(basePath + "/third/searchhotel/findParentAddressById?id="+item.id).success(function (newData) {
                        if (newData.status=="0") {
                            //重新计算页面大小 填充页面
                            $ionicScrollDelegate.resize();
                            $scope.showData.setShowName(newData.result,data.result.length,1);

                        }
                    });


                }
            });

        }
        if($scope.searchId){
            //根据code查询数据
            $http.post(basePath + "/third/searchhotel/findPlatAreaByCityCode?cityCode="+$scope.searchId).success(function (data) {
                if (data.status=="0") {
                    $scope.newClass = data.result.id;
                    $scope.showData.showName=data.result.objectList;
                    $scope.testShowName = data.result.name;
                    $http.post(basePath + "/third/searchhotel/findAreaDataListByParentId?id="+data.result.parentId).success(function (data) {
                        if (data.status=="0") {
                            $scope.parentAddress = data.result;

                        }
                    });
                }
            });


        }




    }
})();

