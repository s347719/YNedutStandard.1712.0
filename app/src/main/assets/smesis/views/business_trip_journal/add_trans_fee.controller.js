/**
 * project:     yineng-corpSysLand
 * title:      add_trans_fee.controller.js
 * author:      xiechangwei
 * date:        2016/12/1 15:30
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:添加交通费页面控制
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('addTransController', addTransController);

    addTransController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function addTransController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout){
        $scope.dateBusin = $location.search().backDate;//获得主页面返回的日期数据
        $scope.showTime = $location.search().showTime;//获得主页面返回的日期数据
        $scope.id = $location.search().id;//获得主页面返回的id
        $scope.backDate = $location.search().backDate;
        var compare = {};//装载比较数据
        $scope.transFeeFun = function(){
            $scope.departArea = {provinceId : -1,cityId : -1,areaId : -1};
            $scope.terminiArea = {provinceId : -1,cityId : -1,areaId : -1};
            $scope.selectModel = {};

            $scope.JSTXAddTransFeeDTO = {
                transDate: "",//初始化日期
                departParentId: "",//出发地省
                departParentName: "",//出发地省Name
                departCityId: "",//出发地市
                departCityName: "",//出发地市Name
                departAreaId: "",//出发地县
                departAreaName: "",//出发地县Name
                depart: "",//出发地字串

                terminiParentId: "",//目的地省
                terminiParentName: "",//目的地省Name
                terminiCityId: "",//目的地市
                terminiCityName: "",//目的地市Name
                terminiAreaId: "",//目的地县
                terminiAreaName: "",//目的地县Name
                termini: "",//目的地县字串
                areaName: "",//所在地区县Name
                vehicle: "",//出行方式CODE
                vehicleName: "",//出行方式code Name
                money: "",//金额
                moneyStr: "",
                message:""
            };
            compare = angular.copy($scope.JSTXAddTransFeeDTO);//用于比较数据是否修改
        };
        $scope.transFeeFun();//初始化信息
        /**
         * 初始化出行方式 ，出发地，目的地
         * @type {{}}
         */
            //获得出行方式码表值
        $http.get(basePath + "/third/businessjournal/findVehicleCodeTable?").success(function (value) {
            $scope.vehicleCodeData = value;
        });
        //获得今天出差酒店费用信息
        $scope.refreshGrid = function (func) {
            if($scope.id){//修改时加载，，，
                //根据id查询数据
                $http.get(basePath + "/third/businessjournal/findJSTXAddTransFeeById?id="+$scope.id).success(function (data) {
                    if (data.length != 0) {
                        //$scope.totalList = data.result;
                        $scope.JSTXAddTransFeeDTO = data[0];
                        compare = angular.copy($scope.JSTXAddTransFeeDTO);//用于比较数据是否修改
                        /***********数据回填*************/
                        $scope.departArea = {provinceId : $scope.JSTXAddTransFeeDTO.departParentId,
                            cityId : $scope.JSTXAddTransFeeDTO.departCityId,areaId : $scope.JSTXAddTransFeeDTO.departAreaId};
                        $scope.terminiArea = {provinceId : $scope.JSTXAddTransFeeDTO.terminiParentId,
                            cityId : $scope.JSTXAddTransFeeDTO.terminiCityId,areaId : $scope.JSTXAddTransFeeDTO.terminiAreaId};

                        var start = $scope.JSTXAddTransFeeDTO.departParentName+","+$scope.JSTXAddTransFeeDTO.departCityName;
                        if($scope.JSTXAddTransFeeDTO.departAreaName != null){
                            start +","+$scope.JSTXAddTransFeeDTO.departAreaName;
                        }
                        $scope.departArea.name =start; //出发地区
                        var endName = $scope.JSTXAddTransFeeDTO.terminiParentName+","+ $scope.JSTXAddTransFeeDTO.terminiCityName;
                        if($scope.JSTXAddTransFeeDTO.terminAreaName != null){
                            endName +","+$scope.JSTXAddTransFeeDTO.terminAreaName;
                        }
                        $scope.terminiArea.name = endName;//目的地区
                        $scope.selectModel.dictName = $scope.JSTXAddTransFeeDTO.vehicleName;//交通工具
                    }
                    if (func) { func();}
                }).error(function () {
                    if (func) {func();}
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            }else{
                //获得前天，昨天，今天日期
                $http.get(basePath + "/third/businessjournal/getDateTime?").success(function (value) {
                    $scope.dateTime = value;
                    $scope.dateTimeList = [];//用于不同日期 样式装载
                    for (var i = 0; i < value.length; i ++) {
                        var dateTime = {time: "", name: "", check: ""};
                        dateTime.time = value[i].date;//具体日期
                        dateTime.name = (i == 0 ? "前天" : (i == 1 ? "昨天" : "今天(" + value[i].monthDay + ")"))//显示值
                        if (i == value.length - 1) {//初始化赋值样式参数
                            dateTime.check = true;
                        } else {
                            dateTime.check = false;
                        }
                        $scope.dateTimeList.push(dateTime);
                    };
                    if($scope.showTime){
                        angular.forEach($scope.dateTimeList,function(val){
                            if(val.time.indexOf($scope.showTime)>-1){
                                $scope.refresh(val);
                            }
                        });
                    }
                    $scope.transFeeFun();
                    if (func) {func();}
                }).error(function () {
                    if (func) {func();}
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            }

        };
        $scope.refresh = function (item) {//点击查询前天，昨天，今天
            for (var i = 0; i < $scope.dateTimeList.length; i++) {
                if (angular.equals($scope.dateTimeList[i], item)) {
                    $scope.dateTimeList[i].check = true;//动态样式控制
                    if ("前天" == $scope.dateTimeList[i].name || "前天(" + $scope.dateTime[0].monthDay + ")" == $scope.dateTimeList[i].name) {
                        $scope.dateTimeList[0].name = "前天(" + $scope.dateTime[0].monthDay + ")";
                        $scope.dateTimeList[1].name = "昨天";
                        $scope.dateTimeList[2].name = "今天";
                    } else if ("昨天" == $scope.dateTimeList[i].name || "昨天(" + $scope.dateTime[1].monthDay + ")" == $scope.dateTimeList[i].name) {
                        $scope.dateTimeList[0].name = "前天";
                        $scope.dateTimeList[1].name = "昨天(" + $scope.dateTime[1].monthDay + ")";
                        $scope.dateTimeList[2].name = "今天";
                    } else {
                        $scope.dateTimeList[0].name = "前天";
                        $scope.dateTimeList[1].name = "昨天";
                        $scope.dateTimeList[2].name = "今天(" + $scope.dateTime[2].monthDay + ")";
                    }
                } else {
                    $scope.dateTimeList[i].check = false;
                }
            }
        };

        //强制刷新页面
        $scope.refreshEdit = function () {
            $ionicLoading.show({
                template: '正在刷新...'
            });
            $scope.refreshGrid(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };
        //自动加载...............
        $scope.refreshEdit();

        /*
         * 出发地地区选择组件
         */
        $scope.areaDepartSelect = function (item) {
            if (item.length == 3) {
                $scope.JSTXAddTransFeeDTO.departAreaId = item[2].id;
                //取得所选对象
                $scope.departName = item[2].name + "(" + item[1].name + ")";//县（市）
            } else {
                $scope.JSTXAddTransFeeDTO.departAreaId = "";
                $scope.departName = item[1].name + "(" + item[0].name + ")";//直辖市 区（市）
            }
            $scope.JSTXAddTransFeeDTO.departParentId = item[0].id;//省
            $scope.JSTXAddTransFeeDTO.departCityId = item[1].id;//市


        };
        /*
         * 目的地地区选择组件
         */
        $scope.areaTerminiSelect = function (item) {
            //取得所选对象
            if (item.length == 3) {
                $scope.JSTXAddTransFeeDTO.terminiAreaId = item[2].id;//县
                //取得所选对象
                $scope.terminiName = item[2].name + "(" + item[1].name + ")";//县（市）
            } else {
                $scope.JSTXAddTransFeeDTO.terminiAreaId = "";
                $scope.terminiName = item[1].name + "(" + item[0].name + ")";//直辖市 区（市）
            }
            $scope.JSTXAddTransFeeDTO.terminiParentId = item[0].id;//省
            $scope.JSTXAddTransFeeDTO.terminiCityId = item[1].id;//市
        };
        //出发地与目的地切换
        $scope.chooseClick = function () {
            //暂存后者数据
            var temporaryList = {
                name: $scope.terminiArea.name,
                parentId: $scope.JSTXAddTransFeeDTO.terminiParentId,
                cityId: $scope.JSTXAddTransFeeDTO.terminiCityId,
                areaId: $scope.JSTXAddTransFeeDTO.terminiAreaId
            };

            //将前者赋值给后者
            //$scope.terminiArea.name = $scope.departArea.name
            $scope.terminiArea.name = $scope.departArea.name;
            $scope.JSTXAddTransFeeDTO.terminiParentId = $scope.JSTXAddTransFeeDTO.departParentId;//省
            $scope.JSTXAddTransFeeDTO.terminiCityId = $scope.JSTXAddTransFeeDTO.departCityId;//市
            $scope.JSTXAddTransFeeDTO.terminiAreaId = $scope.JSTXAddTransFeeDTO.departAreaId;//县

            //将暂存后者赋值给前者
            $scope.departArea.name =temporaryList.name;
            //$scope.departName = temporaryList.name;
            $scope.JSTXAddTransFeeDTO.departParentId = temporaryList.parentId;//省
            $scope.JSTXAddTransFeeDTO.departCityId = temporaryList.cityId;//市
            $scope.JSTXAddTransFeeDTO.departAreaId = temporaryList.areaId;//县
        };
        //交通工具选择
        $scope.vehicleSelect = function (item) {
            //取得所选对象
            $scope.JSTXAddTransFeeDTO.vehicle = item.dictCode;
        };
        //isBack 保存后是否返回上一级
        $scope.submitData = function(isBack,func){
            //提交交通费用信息
            $http.post(basePath + "/third/businessjournal/saveJSTXAddTransFee?", $scope.JSTXAddTransFeeDTO).success(function (data) {
                if (func) {func();}
                if (data.status == 0) {
                    $ionicLoading.show({
                        template: '提交成功'
                    });
                    setTimeout(function () {
                        $ionicLoading.hide();
                    }, 500);
                    if (isBack == 1) {
                        setTimeout(function () {
                            if($scope.JSTXAddTransFeeDTO.transDate){
                                $location.path("/view_fee").search({date:$scope.JSTXAddTransFeeDTO.transDate});
                            }else{
                                $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                                    if(data.status == "0"){
                                        $location.path("/view_fee").search({date:data.result});
                                    }
                                });
                            }
                        }, 500);
                    } else {
                        $scope.JSTXAddTransFeeDTO.money = 0;
                        $scope.JSTXAddTransFeeDTO.message ="";
                    }
                }else{
                    var alertPopup = $ionicPopup.alert({
                        title : '提交失败',
                        template : '<h5 class="margin-bottom-0">'+data.message+'</h5>',
                        buttons : [
                            {
                                text : '确定',
                                type : 'button-theme button-local'


                            }
                        ]
                    });
                }

            }).error(function () {
                if (func) {func();}
                $scope.emptyInfo = true;
                $scope.dataErrorMsg = '加载失败，请重试！';
            });
        };
        //必填验证
        $scope.dateBusinFun = function(data){
            var alertPopup = $ionicPopup.alert({
                title : '提示',
                template : '<h5 class="margin-bottom-0">'+data+'不能为空!</h5>',
                buttons : [
                    {
                        text : '确定',
                        type : 'button-theme button-local'


                    }
                ]
            });
        };
        //保存数据
        $scope.submit = function (isBack) {
            if(!$scope.id) {
                for (var i = 0; i < $scope.dateTimeList.length; i++) {
                    var date = $scope.dateTimeList[i];
                    if (date.check == true) {
                        $scope.JSTXAddTransFeeDTO.transDate = date.time;//初始化赋值
                    }
                }
            }else{
                $scope.JSTXAddTransFeeDTO.transDate = $scope.dateBusin;
            }
            if ($scope.JSTXAddTransFeeDTO.money > 999999999 ||!/^\d*\.?\d{0,2}$/.test($scope.JSTXAddTransFeeDTO.money)) {//验证金额格式，长度（2位小数 && < 9位）
                $scope.notifyMsg("金额只能由小于9位的整数或1到2位小数组成!")
                return false;
            }
            //验证必填效果(费用日期，出行方式，出发地省市县，目的地省市县，金额)
            if (!$scope.JSTXAddTransFeeDTO.money) {
                $scope.notifyMsg("费用金额不能为空!")
                return false;
            }

            if (!$scope.JSTXAddTransFeeDTO.transDate) {
                $scope.notifyMsg("费用日期不能为空!")
                return false;
            }
            if (!$scope.JSTXAddTransFeeDTO.vehicle) {
                $scope.notifyMsg("出行方式不能为空!")
                return false;
            }
            if (!( $scope.JSTXAddTransFeeDTO.departParentId && $scope.JSTXAddTransFeeDTO.departCityId)) {
                $scope.notifyMsg("出发地不能为空!")
                return false;
            }
            if (!($scope.JSTXAddTransFeeDTO.terminiParentId && $scope.JSTXAddTransFeeDTO.terminiCityId)) {
                $scope.notifyMsg("目的地不能为空!")
                return false;
            }


            $ionicLoading.show({
                template: '正在提交...'
            });
            $scope.submitData(isBack,function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };

        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 800);
        }

        //返回按钮
        $scope.cancel = function(){
            if($scope.JSTXAddTransFeeDTO.transDate){
                $location.path("/view_fee").search({date:$scope.JSTXAddTransFeeDTO.transDate});
            }else{
                if($scope.backDate){
                    $location.path("/view_fee").search({date:$scope.backDate});
                }else{
                    $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                        if(data.status == "0"){
                            $location.path("/view_fee").search({date:data.result});
                        }
                    });
                }
            }


        };

        //弹框提示
        $scope.showConfirm = function() {
            var confirmPopup = $ionicPopup.confirm({
                title : '返回提示',
                template : '<h5 class="margin-bottom-0">返回后当前编辑数据将丢失,确定要返回吗?</h5>',
                buttons : [
                    {
                        text : '继续编辑',
                        type : 'button-theme button-local button-outline'
                    },
                    {
                        text : '确定返回',
                        type : 'button-theme button-local',
                        onTap: function (e){
                            $scope.cancel();
                        }


                    }
                ]
            });
        };
        //返回按钮
        $scope.doBack = function () {
            //点击返回编辑过数据 需要提示 您已编辑过内容，是要放弃？
            if (!angular.equals($scope.JSTXAddTransFeeDTO, compare)) {
                $scope.showConfirm();
            } else {
                $scope.cancel();
            }

        };
        //模拟点击事件
        $scope.selectArea = function(index){
            if(1 == index){
                angular.element('#departArea').click();
            }else{
                angular.element('#terminiArea').click();
            }
        }

        //修改时提示不允许修改
        $scope.clickDateBusin = function(){
            $ionicLoading.show({
                template: "<p>不允许修改日期！</p>"
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }


    }
})();
