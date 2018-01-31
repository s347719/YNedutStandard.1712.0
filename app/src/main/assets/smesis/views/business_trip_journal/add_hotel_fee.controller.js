/**
 * project:     yineng-corpSysLand
 * title:      add_hotel_fee.controller.js
 * author:      xiechangwei
 * date:        2016/12/1 15:31
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:添加住宿费页面控制
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('addHotelController', addHotelController);

    addHotelController.$inject = ['$scope', '$ionicModal', '$http', '$filter', '$ionicLoading', '$ionicScrollDelegate', "$ionicPopup", "$location"];
    function addHotelController($scope,$ionicModal,$http,$filter,$ionicLoading,$ionicScrollDelegate,$ionicPopup, $location){
        $scope.indexCount = 0;//计数存在记录条数
        $scope.dateBusin = $location.search().backDate;//获得主页面返回的日期数据
        $scope.backTime = $location.search().backDate;
        $scope.showTime = $location.search().showTime;//获得主页面返回的日期数据
        //数据id
        $scope.id = $location.search().id;
        $scope.area = {};
        $scope.hotel = {};
        $scope.selectModel = {};
        $scope.jstxAddHotelFeeDTO = {
            traDate: "",//初始化当前日期
            xzglHotelTraId: "",//出差酒店ID
            xzglHotelTraName: "",//出差酒店Name
            xzglHotelTraCode: "",//出差酒店类型
            xzglHotelTraAddress: "",//出差酒店地址
            provinceId: "",//所在地区省id
            provinceName: "",//所在地区省name
            cityId: "",//所在地区市
            cityName: "",//所在地区市Name
            areaId: "",//所在地区县
            areaName: "",//所在地区县Name
            travelHotelType: "",//房型code
            travelHotelName: "",//房型codeName
            money: ""//金额
        };
        var compare = {};//装载比较数据
        //获得房型码表值
        $http.get(basePath + "/third/businessjournal/findTravelHotRoomTypeCodeTable?").success(function (value) {
            $scope.travelHotelData = value;
        });
        $scope.dateTimeList = [];
        $scope.backDate = $scope.dateBusin;
        //获得今天出差酒店费用信息
        $scope.refreshGrid = function (func) {
            if ($scope.dateBusin &&$scope.id) {//修改默认加载数据
                //查询将修改数据
                $http.get(basePath + "/third/businessjournal/findJSTXAddHotelFeeByTime?date1=" + $scope.dateBusin + "&index=0").success(function (data) {
                    if (data.result.length != 0) {
                        $scope.totalList = data.result;
                        $scope.jstxAddHotelFeeDTO = data.result[data.result.length - 1];
                        compare = angular.copy($scope.jstxAddHotelFeeDTO);//用于比较数据是否修改
                        /***********数据回填*************/
                        $scope.area = {provinceId: $scope.jstxAddHotelFeeDTO.provinceId,cityId: $scope.jstxAddHotelFeeDTO.cityId,
                            areaId: $scope.jstxAddHotelFeeDTO.areaId};
                        $scope.area.name = $scope.jstxAddHotelFeeDTO.areaId ? $scope.jstxAddHotelFeeDTO.areaName + "(" + $scope.jstxAddHotelFeeDTO.cityName + ")" :
                        $scope.jstxAddHotelFeeDTO.cityName + "(" + $scope.jstxAddHotelFeeDTO.provinceName + ")";//地区
                        $scope.hotel.name = $scope.jstxAddHotelFeeDTO.xzglHotelTraName;//所住酒店
                        $scope.selectModel.dictName = $scope.jstxAddHotelFeeDTO.travelHotelName;//房型
                    }
                    if (func) {func();}
                }).error(function () {
                    if (func) {func();}
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            } else {
                //获得前天，昨天，今天日期
                $http.get(basePath + "/third/businessjournal/getDateTime?").success(function (value) {
                    $scope.dateTime = value;
                    $scope.dateTimeList = [];//用于不同日期 样式装载
                    for (var i = 0; i < value.length; i++) {
                        var dateTime = {time: "", name: "", check: "", isDis: ""};
                        dateTime.time = value[i].date;//具体日期
                        dateTime.name = (i == 0 ? "前天" : (i == 1 ? "昨天" : "今天(" + value[i].monthDay + ")"))//显示值
                        if (i == value.length - 1) {//初始化赋值样式参数
                            dateTime.check = true;
                        } else {
                            dateTime.check = false;
                        }
                        $scope.dateTimeList.push(dateTime);
                    };
                    //动态获得费用日期是否存在记录
                    $http.get(basePath + "/third/businessjournal/findJSTXAddHotelFeeByIsTimeTure?date1=" + $scope.dateTimeList[0].time + "&date2=" + $scope.dateTimeList[1].time
                    + "&date3=" + $scope.dateTimeList[2].time).success(function (data) {
                        $scope.dateStrList = data;
                        //动态获得费用日期是否存在记录 数据处理
                        $scope.indexCount =0;
                        for (var timeI = 0; timeI < $scope.dateTimeList.length; timeI++) {
                            var dateTimeIsStr = $scope.dateTimeList[timeI], dateIsStr = $scope.dateStrList[timeI];
                            if (dateIsStr == "0") {
                                dateTimeIsStr.isDis = false;
                            } else {
                                $scope.indexCount += 1;//计数存在记录条数
                                dateTimeIsStr.isDis = true;
                            }
                        }
                        /*************************动态日期样式监听********************/
                        var is = 0, os = 0, ds = 0;
                        for (var i = 0; i < $scope.dateTimeList.length; i++) {
                            var y = $scope.dateTimeList[i];
                            if (y.isDis == true) {
                                y.check = false;
                                if (i == 0) is++;
                                if (i == 1) os++;
                                if (i == 2) {
                                    y.name = "今天";
                                    ds++;
                                }
                            }
                        }
                        if (ds == 0) {
                        } else if (os == 0) {
                            $scope.dateTimeList[1].check = true;
                            $scope.dateTimeList[1].name = "昨天(" + $scope.dateTime[1].monthDay + ")";
                        } else if (is == 0) {
                            $scope.dateTimeList[0].check = true;
                            $scope.dateTimeList[0].name = "前天(" + $scope.dateTime[0].monthDay + ")";
                        }
                        //if($scope.showTime){
                        //    angular.forEach($scope.dateTimeList,function(val){
                        //        if(val.time.indexOf($scope.showTime)>-1){
                        //            $scope.refresh(val);
                        //        }
                        //    });
                        //}
                        if (func) {func();}
                    }).error(function () {
                        if (func) { func();}
                        $scope.emptyInfo = true;
                        $scope.dataErrorMsg = '加载失败，请重试！';
                    });

                    //查询展示最新一条数据
                    $http.get(basePath + "/third/businessjournal/findJSTXAddHotelFeeByTime?date1=" + $scope.dateTimeList[0].time + "&date2=" + $scope.dateTimeList[1].time
                    + "&date3=" + $scope.dateTimeList[2].time + "&index=1").success(function (data) {
                        if (data.result.length != 0) {
                            $scope.totalList = data.result;
                            $scope.jstxAddHotelFeeDTO = data.result[data.result.length - 1];
                            compare = angular.copy($scope.jstxAddHotelFeeDTO);//用于比较数据是否修改
                            /***********数据回填*************/
                            $scope.area = {provinceId : $scope.jstxAddHotelFeeDTO.provinceId,cityId : $scope.jstxAddHotelFeeDTO.cityId,
                                areaId : $scope.jstxAddHotelFeeDTO.areaId};
                            $scope.area.name = $scope.jstxAddHotelFeeDTO.areaId ? $scope.jstxAddHotelFeeDTO.areaName + "(" + $scope.jstxAddHotelFeeDTO.cityName + ")" :
                            $scope.jstxAddHotelFeeDTO.cityName + "(" + $scope.jstxAddHotelFeeDTO.provinceName + ")";//地区
                            $scope.hotel.name = $scope.jstxAddHotelFeeDTO.xzglHotelTraName;//所住酒店
                            $scope.selectModel.dictName = $scope.jstxAddHotelFeeDTO.travelHotelName;//房型
                        }
                        if (func) {func();}
                    }).error(function () {
                        if (func) { func();}
                        $scope.emptyInfo = true;
                        $scope.dataErrorMsg = '加载失败，请重试！';
                    });
                });
            }

        };

        $scope.refresh = function (item) {//点击查询前天，昨天，今天
            if (item.isDis == true) {
                var alertPopup = $ionicPopup.alert({
                    title : '提示',
                    template : '<h5 class="margin-bottom-0">'+item.name+'已有酒店费用记录,不能再添加！</h5>',
                    buttons : [
                        {
                            text : '确认',
                            type : 'button-theme button-local'

                        }
                    ]
                });
            } else {
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
         * 地区选择组件
         */
        $scope.areaSelect = function (item) {
            //取得所选对象
            $scope.jstxAddHotelFeeDTO.provinceId = item[0].id;//省
            $scope.jstxAddHotelFeeDTO.cityId = item[1].id;//市
            if (item.length > 2) {
                $scope.jstxAddHotelFeeDTO.areaId = item[2].id;//县
            }else{
                $scope.jstxAddHotelFeeDTO.areaId = "";
            }
            $scope.area.name = item.length > 2 ? item[2].name + "(" + item[1].name + ")" :
            item[1].name + "(" + item[0].name + ")";//地区
        };
        /*
         * 酒店选择组件
         */
        $scope.hotelSelect = function (item) {
            $scope.jstxAddHotelFeeDTO.provinceId = item.provinceId;//省
            $scope.jstxAddHotelFeeDTO.cityId = item.cityId;//市
            $scope.jstxAddHotelFeeDTO.areaId = item.areaId;//县
            $scope.area.name = item.area;
            //取得所选对象
            $scope.jstxAddHotelFeeDTO.xzglHotelTraId = item.id;
            $scope.jstxAddHotelFeeDTO.xzglHotelTraName = item.name;
            $scope.jstxAddHotelFeeDTO.xzglHotelTraAddress = item.address;
            //酒店类型
            $scope.jstxAddHotelFeeDTO.xzglHotelTraCode = item.hotelTypeCode;
        };
        //房型选择
        $scope.diySelect = function (item) {
            //房型
            $scope.jstxAddHotelFeeDTO.travelHotelType = item.dictCode;
        };
        //isBack 保存后是否返回上一级
        $scope.submitData = function (isBack, func) {
            //提交酒店费用信息
            $http.post(basePath + "/third/businessjournal/saveJSTXAddHotelFee?", $scope.jstxAddHotelFeeDTO).success(function (data) {
                if (func) {
                    func();
                }
                if (data.status == "0") {
                    $ionicLoading.show({
                        template: '提交成功'
                    });
                    setTimeout(function () {
                        $ionicLoading.hide();
                    }, 500);
                    if (isBack == 1) {
                        setTimeout(function () {
                            if($scope.jstxAddHotelFeeDTO.traDate){
                                $location.path("/view_fee").search({date:$scope.jstxAddHotelFeeDTO.traDate});
                            }else{
                                $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                                    if(data.status == "0"){
                                        $location.path("/view_fee").search({date:data.result});
                                    }
                                });
                            }


                        }, 500);
                    }else{
                        $scope.refreshEdit();//刷新
                    }
                } else {
                    var alertPopup = $ionicPopup.alert({
                        title : '提交失败',
                        template : '<h5 class="margin-bottom-0">'+data.message+'</h5>',
                        buttons : [
                            {
                                text : '确认',
                                type : 'button-theme button-local'

                            }
                        ]
                    });
                }

            }).error(function () {
                if (func) {
                    func();
                }
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
                        text : '确认',
                        type : 'button-theme button-local'

                    }
                ]

            });
        };
        //保存数据
        $scope.submit = function (isBack) {
            if(!$scope.id) {//添加时获得选择日期
                for (var i = 0; i < $scope.dateTimeList.length; i++) {
                    var date = $scope.dateTimeList[i];
                    if (date.check == true) {
                        $scope.jstxAddHotelFeeDTO.traDate = date.time;//初始化赋值
                    }
                }
            } else {
                $scope.jstxAddHotelFeeDTO.traDate = $scope.dateBusin;//修改时赋值
            }
            if ($scope.jstxAddHotelFeeDTO.money > 999999999 || !/^\d*\.?\d{0,2}$/.test($scope.jstxAddHotelFeeDTO.money)) {//验证金额格式，长度（2位小数 && < 9位）
                $scope.notifyMsg("金额只能由小于9位的整数或1到2位小数组成!")
                return false;
            }
            //验证必填效果(地区，酒店，省市县，房型，金额)
            if (!$scope.jstxAddHotelFeeDTO.money) {
                $scope.notifyMsg("金额不能为空!")
                return false;
            }
            if (!$scope.jstxAddHotelFeeDTO.traDate) {
                $scope.notifyMsg("费用日期不能为空!")
                return false;
            }
            if (!($scope.jstxAddHotelFeeDTO.provinceId && $scope.jstxAddHotelFeeDTO.cityId)) {
                $scope.notifyMsg("所在地区不能为空!")
                return false;
            }
            if (!$scope.jstxAddHotelFeeDTO.xzglHotelTraName) {
                $scope.notifyMsg("所住酒店不能为空!")
                return false;
            }
            if (!$scope.jstxAddHotelFeeDTO.travelHotelType) {
                $scope.notifyMsg("房型不能为空!")
                return false;
            }
            $ionicLoading.show({
                template: '正在提交...'
            });
            $scope.submitData(isBack, function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };

        //冒泡提示
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 800);
        }
        //返回
        $scope.cancel = function () {
            if($scope.jstxAddHotelFeeDTO.traDate){
                $location.path("/view_fee").search({date:$scope.jstxAddHotelFeeDTO.traDate});
            }else{
                if($scope.backTime){
                    $location.path("/view_fee").search({date:$scope.backTime});
                }else{
                    $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                        if(data.status == "0"){
                            $location.path("/view_fee").search({date:data.result});
                        }
                    });
                }
            }

        };
        $scope.showConfirm = function () {
            var confirmPopup = $ionicPopup.confirm({
                title : '返回提示',
                template : '<h5 class="margin-bottom-0">返回后当前编辑数据将丢失,确定要返回吗？</h5>',
                buttons : [
                    {
                        text : '继续编辑',
                        type : 'button-outline button-theme button-local'
                    },
                    {
                        text : '确定返回',
                        type : 'button-theme button-local',
                        onTap: function (e) {
                            $scope.cancel();
                        }
                    }
                ]

            });
        };
        //返回按钮
        $scope.doBack = function () {
            //点击返回编辑过数据 需要提示 您已编辑过内容，是要放弃？
            if (!angular.equals($scope.jstxAddHotelFeeDTO, compare)) {
                $scope.showConfirm();
            } else {
                $scope.cancel();
            }
        };
        //模拟点击事件
        $scope.selectArea = function(){
            angular.element('#area').click();
        };
        //修改时提示不允许修改
        $scope.clickDateBusin = function(){
            $ionicLoading.show({
                template: "不允许修改日期！"
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }

    }
})();
