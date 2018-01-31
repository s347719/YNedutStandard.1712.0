/**
 * project:     yineng-corpSysLand
 * title:      search_hotel.controller
 * author:      xiechangwei
 * date:        2016/12/6 17:19
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:查询酒店页面控制器
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('searchHotelController', searchHotelController);

    searchHotelController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout','$ionicModal'];
    function searchHotelController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout,$ionicModal){
        /**
         * 酒店详细信息弹层操作
         */
        var template =
            '<ion-modal-view class="bottom-half hotel-detail min-modal-height"> ' +
            '<ion-header-bar class="bar-stable"> ' +
            '<h1 class="title">酒店信息</h1> ' +
            '<button class="button button-clear button-custom" ng-click="closeModal()">取消</button>' +
            '</ion-header-bar>' +
            ' <ion-content delegate-handle="mainScroll"> ' +
            '<div class="text-center">' +
            ' <h4 ng-bind="hotelDetail.name"></h4>' +
            '<h3 ng-bind="hotelDetail.tel"></h3>' +
            '<a href="tel:{{hotelDetail.tel}}" class="telephone"><button class="button button-theme button-local icon-left ion-ios-telephone-outline">拨打电话</button></a>' +
            '<h6 class="text-gray" ng-click="introduceFun()">酒店介绍</h6>' +
            '<p ng-bind="hotelDetail.hotelDes" ng-if="isShowIntroduce"></p>' +
            '</div>' +
            '</ion-content> ' +
            '</ion-modal-view>';
        $scope.cityCode = $location.search().cityCode;
        $scope.searchId = $location.search().searchId;//获得主页面返回的日期数据

        $scope.hotelFun = function(){
            $scope.travelHotelType ="酒店类型";//页面显示内容
            $scope.hotelStar = "推荐星级";
            $scope.agreementStr = "是否协议";
            //初始化参数
            $scope.indexArr = {type:null,star:null,isAgr:null}//用于记录已选择的下拉值（酒店类型，推荐星级，是否协议酒店）
        }
        $scope.isReturnArea = false;//是否回传第三级
        $scope.defaultFun = function(){
            $scope.isHomeShow = true;//是否显示主页
            $scope.isShowDetail = false;//是否显示详细信息浮框
            $scope.isShowIntroduce = false;//是否显示酒店介绍
            $scope.hotelDetail = {};//用于存储酒店详细信息
            $scope.addrHistory = {};//初始化历史选择地址
            $scope.platCommonAraeName = "";//选择地址 历史字串(区级)显示
            $scope.platCommonCityName = "";//选择地址 历史字串(市级)显示
            $scope.dufCommonName = "请选择酒店所在地区";//home选择地址 历史字串显示
            $scope.conditions = {
                name:"",//酒店名称
                isAgreement:"",//是否协议酒店
                hotelStarCode:"",//推荐指数
                provinceId:"",//所在地区省
                cityId:"",//所在地区市
                areaId:"",//所在地区县
                hotelTypeCode:"",//酒店类型
                isPhone:"1"//是否手机端
            }//初始化查询参数
            $scope.hotelTraListCopyDefulat =[];
            $scope.hotelTraListCopy = [];//中转存储数据
            $scope.isShowSelect = false;//是否显示查询区域
            $scope.searchLoding = false;
            $scope.isShowState = true;//省市级选择效果切换
            //初始已选省市区
            $scope.area ={provinceId : -1,cityId : -1,areaId : -1};
            //初始化前端过滤信息
            $scope.hotelFun();
        }

        $scope.defaultFun();//初始化调用

        $scope.dictTypeCode = {dictCode: "",dictName: "不限",dictTypeKey: "",dictTypeName: "",id: -1,isDel: false,maintainable: 0,managerUserId: null,
            remark: "",sortFieldMap: null,status: 1,statusName: null,userDictCode: "",isShowTravel:true,isHotelStar:true}//初始化酒店类型
        $scope.agreementList = [{dictCode:"-1",dictName:"不限",isAgree:true},{dictCode:true,dictName:"协议酒店",isAgree:false},
            {dictCode:false,dictName:"非协议酒店",isAgree:false}]

        //根据城市code查询数据
        $http.post(basePath + "/third/searchhotel/findAreaAddressByCityId?cityCode="+$scope.cityCode).success(function (data) {
            if(data.status=="0"){
                $scope.addrHistory = data.result;//获得初始化历史选择地址
                $scope.area ={provinceId : $scope.addrHistory.platCommonParentId,cityId :$scope.addrHistory.platCommonCityId,areaId : $scope.addrHistory.platCommonAreaId};
                if ($scope.addrHistory.platCommonAreaName) {
                    $scope.dufCommonName = $scope.addrHistory.platCommonAreaName +
                    "(" + $scope.addrHistory.platCommonCityName + "," + $scope.addrHistory.platCommonParentName + ")";//home回显
                    $scope.platCommonAraeName = $scope.addrHistory.platCommonAreaName;//select回显
                    $scope.platCommonCityName = $scope.addrHistory.platCommonCityName;
                    $scope.isReturnArea = true;
                } else {
                    $scope.dufCommonName = $scope.addrHistory.platCommonCityName + "(" + $scope.addrHistory.platCommonParentName + ")";//home回显
                    $scope.platCommonAraeName =  $scope.addrHistory.platCommonCityName;//select回显
                    $scope.platCommonCityName = $scope.addrHistory.platCommonParentName;
                    $scope.isReturnArea = false;
                }
                //查询赋值
                $scope.conditions.provinceId = $scope.addrHistory.platCommonParentId;//所在地区省
                $scope.conditions.cityId = $scope.addrHistory.platCommonCityId;//所在地区市
                if ($scope.addrHistory.platCommonAreaId) {
                    $scope.conditions.areaId = $scope.addrHistory.platCommonAreaId;//所在地区县
                    $scope.areaIdCopy = angular.copy($scope.conditions.areaId);//所在地区县备份
                }
                $scope.refreshEdit();//存在地区历史时自动加载数据
            }

        });
        //获取出差酒店类型码表
        $http.post(basePath + "/third/searchhotel/findHotelTypeCode").success(function (data) {
            $scope.travelHotelTypeList = data;//获得酒店类型
            $scope.travelHotelTypeList.push(angular.copy($scope.dictTypeCode));//添加初始化参数
        });

        //获取酒店星级码表
        $http.post(basePath + "/third/searchhotel/findHotelStarCode").success(function (data) {
            $scope.hotelStarList = data;//获得酒店推荐星级
            $scope.hotelStarList.push(angular.copy($scope.dictTypeCode));//添加初始化参数
        });


        //根据条件获取出差酒店查询数据
        $scope.refreshGrid = function (func) {
            $http.post(basePath + "/third/searchhotel/findHotelByCondition?",$scope.conditions).success(function (data) {
                if (data.result.content.length != 0) {
                    $scope.hotelTraList = data.result.content;
                    $scope.hotelTraListCopyDefulat = angular.copy($scope.hotelTraList);//前端筛选时防止污染，作为数据源
                }else{
                    $scope.hotelTraList = [];
                    $scope.hotelTraListCopyDefulat = [];
                }

                if (func) { func();}
            }).error(function () {
                if (func) {func();}
                $scope.emptyInfo = true;
                $scope.dataErrorMsg = '加载失败，请重试！';
            });
        }

            //强制刷新页面
        $scope.refreshEdit = function () {
            $ionicLoading.show({
                template: '正在加载...'
            });
            $scope.refreshGrid(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        }

        //查询酒店按钮click事件
        $scope.findHotel = function(){
            if(!angular.equals($scope.dufCommonName,"请选择酒店所在地区")){
                $scope.isHomeShow = !$scope.isHomeShow;//页面切换
            }else{
                var alertPopup = $ionicPopup.alert({
                    title: '提示',
                    template: "请选择酒店所在地区！",
                    okText: '确定'
                });
            }
        }

        //酒店类型，推荐星级,是否协议酒店 选中事件处理
        $scope.clickHotel = function (item,index) {
            if(index == 0){
                if(item.dictCode){
                    $scope.conditions.hotelTypeCode = item.dictCode;
                    $scope.travelHotelType = item.dictName;
                    $scope.indexArr.type = item.dictCode;
                }else{
                    $scope.conditions.hotelTypeCode = '';
                    $scope.travelHotelType ="酒店类型";//页面显示内容
                    $scope.indexArr.type = null;
                }
                item.isShowTravel = !item.isShowTravel;
                angular.forEach($scope.travelHotelTypeList,function(travel){
                    if(!angular.equals(item,travel)){
                        travel.isShowTravel = false;
                    }
                })
            }
            if(index == 1){
                if(item.dictCode){
                    $scope.conditions.hotelStarCode = item.dictCode;
                    $scope.hotelStar = item.dictName;
                    $scope.indexArr.star = item.dictCode;
                }else{
                    $scope.conditions.hotelStarCode = "";
                    $scope.hotelStar = "推荐星级";//页面显示内容
                    $scope.indexArr.star = null;
                }
                item.isHotelStar = !item.isHotelStar;
                angular.forEach($scope.hotelStarList,function(hotel){
                    if(!angular.equals(item,hotel)){
                        hotel.isHotelStar = false;
                    }
                })
            }
            if(index == 2){
                if(item.dictCode != "-1"){
                    $scope.conditions.isAgreement = item.dictCode;
                    $scope.agreementStr = item.dictName;
                    $scope.indexArr.isAgr = item.dictCode;
                }else{
                    $scope.conditions.isAgreement = '';
                    $scope.agreementStr = "是否协议";
                    $scope.indexArr.isAgr = null;
                }
                item.isAgree = !item.isAgree;
                angular.forEach($scope.agreementList,function(agree){
                    if(!angular.equals(item,agree)){
                        agree.isAgree = false;
                    }
                })
            }
            $scope.hotelTraSelectFun($scope.indexArr);//过滤数据

        }

        //重置
        $scope.ueset = function(){
            $scope.hotelFun();//初始化前端过滤显示信息
            //重置查询参数
            $scope.conditions.hotelTypeCode = '';
            $scope.conditions.hotelStarCode = "";
            $scope.conditions.isAgreement = "";
            angular.forEach($scope.travelHotelTypeList,function(travel){
                if(!angular.equals('不限',travel.dictName)){
                    travel.isShowTravel = false;
                }else{
                    travel.isShowTravel = false;
                }
            })
            angular.forEach($scope.hotelStarList,function(hotel){
                if(!angular.equals('不限',hotel.dictName)){
                    hotel.isHotelStar = false;
                }else{
                    hotel.isHotelStar = false;
                }
            })
            angular.forEach($scope.agreementList,function(agree){
                if(!angular.equals('不限',agree.dictName)){
                    agree.isAgree = false;
                }else{
                    agree.isAgree = false;
                }
            })
            //清空界面渲染List
            $scope.hotelTraList = [];
            $scope.hotelTraList = $scope.hotelTraListCopyDefulat;//还原数据
        }

        $scope.selectArea = function(){
            $location.path("/search_hotel_main").search({searchId:$scope.cityCode});
            //angular.element('#area').click();
        }

        //选择器事件
        $scope.areaSelect = function (item) {
            $scope.conditions.provinceId = item[0].id;//所在地区省
            $scope.conditions.cityId = item[1].id;//所在地区市
            if (item.length > 2) {
                $scope.dufCommonName = item[2].name +"(" + item[1].name + "," + item[0].name + ")";//home回显
                $scope.platCommonAraeName =  item[2].name;//select回显
                $scope.platCommonCityName = item[1].name;
                $scope.conditions.areaId = item[2].id;//所在地区县
                $scope.areaIdCopy = angular.copy( $scope.conditions.areaId);//所在地区县
                $scope.isReturnArea = true;
                //地区选择器为三级，选到叶子节点时加载查询
                if($scope.conditions.areaId){
                    $scope.refreshEdit();//查询
                }
            }else{
                $scope.dufCommonName = item[1].name +"(" + item[0].name+")";//home回显
                $scope.platCommonAraeName =  item[1].name;//select回显
                $scope.platCommonCityName = item[0].name;
                $scope.conditions.areaId = "";//所在地区县
                $scope.isReturnArea = false;
                //地区选择器为2级，选到叶子节点时加载查询
                if(item[1].id){
                    $scope.refreshEdit();//查询
                }
            }
        }

        //前端过滤数据
        $scope.hotelTraSelectFun = function (indexArr) {
            $scope.hotelTraListCopy = angular.copy($scope.hotelTraListCopyDefulat);//前端筛选时防止污染，作为数据源
            //清空界面渲染List
            $scope.hotelTraList = [];
            angular.forEach($scope.hotelTraListCopy, function (hotelTra) {
                if ((indexArr.type != null ? indexArr.type == hotelTra.hotelTypeCode : 1 == 1) && (indexArr.star != null ? indexArr.star == hotelTra.hotelStarCode : 2 == 2)
                    && (indexArr.isAgr != null ? indexArr.isAgr == hotelTra.isAgreement : 3 == 3)) {
                    $scope.hotelTraList.push(hotelTra);
                }
            })
        }

        //是否显示酒店介绍
        $scope.introduceFun = function () {
            $scope.isShowIntroduce = !$scope.isShowIntroduce;
        }

        //查询输入
        $scope.clickInputSearch = function (index) {
            if(index == 0){
                $scope.getClass(1);
                $scope.isShowSelect = true;
            }else{
                $scope.getClass(3);
                $scope.conditions.name = "";
                $scope.searchLoding = false;
                $scope.isShowSelect = !$scope.isShowSelect;
                if($scope.isReturnArea){
                    $scope.conditions.areaId = $scope.areaIdCopy;//返还叶子节点地区过滤条件
                }
                $scope.isShowState = true;//重置更新城市显示
                $scope.refreshEdit();//查询
            }
        }

        //动态获取样式
        $scope.returnClass = "fixed-header search-hotel ";
        $scope.getClass = function(item){
            if(item ==1){
                $scope.returnClass = 'fixed-header search-hotel search-only ' ;
            }else if(item==2){
                $scope.returnClass = 'fixed-header search-hotel search-only more-tips' ;
            }else if(item==3){
                $scope.returnClass = "fixed-header search-hotel ";
            }
        }

        //清空数据
        $scope.cancleSearch = function () {
            $scope.conditions.name = "";
        }

        //查询数据
        $scope.search = function () {
            $scope.getClass(2);
            if($scope.isShowSelect){//未点击搜索酒店输入框之前不允许调用查询
                $scope.searchLoding = true;
                $scope.refreshEdit();
            }
        }

        //更改城市
        $scope.clickStateSearch = function(index){
            $scope.isShowState = !$scope.isShowState;
            if(index == 0){
                $scope.conditions.areaId = "";//所在地区县
            }else{
                $scope.conditions.areaId = $scope.areaIdCopy
            }
            $scope.refreshEdit();
        }
        //强制刷新页面
        $scope.refreshSearchFun = function(){
            $scope.defaultFun();//重置参数
        }
        /**
         * 酒店详情显示方式设置
         */
        $scope.modal = $ionicModal.fromTemplate(template, {
            scope: $scope,
            animation: 'slide-in-up'
        });
        $scope.openModal = function (item) {
            $scope.modal.show();
            $scope.isShowIntroduce = false;
            $scope.hotelDetail = item;
        };
        $scope.closeModal = function () {
            $scope.modal.hide();
        };
        //Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function () {
            $scope.modal.remove();
        });

        //返回
        $scope.goBack = function () {
            $location.path("/search_hotel_main").search({});

        }




    }
})();

