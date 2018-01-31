/**
 * project:     yineng-corpSysLand
 * title:      add_other_fee.controller.js
 * author:      xiechangwei
 * date:        2016/12/1 15:30
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:添加其他费用页面控制
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('addOtherController', addOtherController);

    addOtherController.$inject = ['$scope', '$ionicPopup', '$http', '$filter', '$ionicLoading', '$ionicScrollDelegate', '$location', '$timeout'];
    function addOtherController($scope, $ionicPopup, $http, $filter, $ionicLoading, $ionicScrollDelegate, $location, $timeout) {
        $scope.initData = function () {
            $scope.otherFeeInfo = {
                money: "",//金额
                feeReason: "",//费用事由
                feeSpendDate: ""//费用发生日期
            };
            $scope.today = {};//今天时间
            $scope.yesterday = {};//昨天时间‘
            $scope.anteayer = {};//前天时间
            $scope.selectedDate = "today";//默认选中当前时间
        };
        $scope.backDate = $location.search().backDate;
        $scope.dateBusin = $location.search().backDate;//获得主页面返回的日期数据
        $scope.showTime = $location.search().showTime;//获得主页面返回的日期数据
        $scope.id = $location.search().id;
        if ($scope.id) {
            $http.post(basePath + "/third/businessjournal/findDataById?id=" + $scope.id).success(function (data) {
                if (data.status == "0") {
                    $scope.otherFeeInfo = data.result;
                }
            })
        }
        $scope.initData();

        $scope.changSelectedDate = function (str) {
            $scope.selectedDate = str;//改变选中的费用日期
            $scope.today.showStr = "今天";
            $scope.yesterday.showStr = "昨天";
            $scope.anteayer.showStr = "前天";
            if ($scope.selectedDate == "today") {
                $scope.today.showStr = "今天(" + $scope.today.showDateStr + ")";
            }
            if ($scope.selectedDate == "yesterday") {
                $scope.yesterday.showStr = "昨天(" + $scope.yesterday.showDateStr + ")";
            }
            if ($scope.selectedDate == "anteayer") {
                $scope.anteayer.showStr = "前天(" + $scope.anteayer.showDateStr + ")";
            }
            setTimeout(function () {
                $scope.$apply();
            })

        };
        $scope.getShowDateStr = function (dateStr) {
            var str = dateStr.split("-");
            return str[1] + "/" + str[2];
        };
        $scope.getDate = function (func) {
            //获取最近3天日期
            $http.get(basePath + "/third/businessjournal/getOtherFeeDateTime?").success(function (data) {
                if (data.status == 0) {
                    $scope.today = {
                        dateStr: data.result[0],
                        showDateStr: $scope.getShowDateStr(data.result[0]),
                        showStr: "今天(" + $scope.getShowDateStr(data.result[0]) + ")"
                    };
                    $scope.yesterday = {
                        dateStr: data.result[1],
                        showDateStr: $scope.getShowDateStr(data.result[1]),
                        showStr: "昨天"
                    };
                    $scope.anteayer = {
                        dateStr: data.result[2],
                        showDateStr: $scope.getShowDateStr(data.result[2]),
                        showStr: "前天"
                    }
                }
                if($scope.showTime){
                    if($scope.today.dateStr.indexOf($scope.showTime)>-1){
                        $scope.changSelectedDate("today");
                    }
                    if($scope.yesterday.dateStr.indexOf($scope.showTime)>-1){
                        $scope.changSelectedDate("yesterday");
                    }
                    if($scope.anteayer.dateStr.indexOf($scope.showTime)>-1){
                        $scope.changSelectedDate("anteayer");
                    }
                }
                if (func) {
                    func();
                }
            }).error(function () {
                if (func) {
                    func();
                }
                $scope.emptyInfo = true;
                $scope.dataErrorMsg = '加载失败，请重试！';
            });
        };

        $scope.doRefresh = function () {
            $ionicLoading.show({
                template: '正在刷新...'
            });
            $scope.initData();
            $scope.getDate(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };
        $scope.doRefresh();
        //isBack 保存后是否返回上一级
        $scope.submitData = function (isBack, func) {

            $http.post(basePath + "/third/businessjournal/saveJSTXOtherFee?", $scope.otherFeeInfo).success(function (data) {
                if (func) {
                    func();
                }
                if (data.status == 0) {
                    $ionicLoading.show({
                        template: '提交成功'
                    });
                    setTimeout(function () {
                        $ionicLoading.hide();
                    }, 500);
                    if (isBack) {
                        setTimeout(function () {
                            if ($scope.otherFeeInfo.feeSpendDate) {
                                $location.path("/view_fee").search({date: $scope.otherFeeInfo.feeSpendDate});
                            } else {
                                $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                                    if (data.status == "0") {
                                        $location.path("/view_fee").search({date: data.result});
                                    }
                                });
                            }

                        }, 500);
                    } else {
                        $scope.otherFeeInfo.money = 0;
                        $scope.otherFeeInfo.feeReason ="";
                    }
                } else {
                    var alertPopup = $ionicPopup.alert({
                        title: "提交失败",
                        template: '<h5 class="margin-bottom-0">'+data.message+'</h5>',
                        buttons: [
                            {
                                text: '确定',
                                type: 'button-theme button-local'

                            }
                        ]
                    });
                }

            }).error(function () {
                if (func) {
                    func();
                }
                $ionicLoading.show({
                    template: '提交失败，请重试！'
                });
                setTimeout(function () {
                    $ionicLoading.hide();
                }, 1500);
            });
        };
        $scope.submit = function (isBack) {
            if ($scope.otherFeeInfo.money > 999999999 || !/^\d*\.?\d{0,2}$/.test($scope.otherFeeInfo.money)) {//验证金额格式，长度（2位小数 && < 9位）
                var alertPopup = $ionicPopup.alert({
                    title: "提交失败",
                    template: '<h5 class="margin-bottom-0">金额只能由小于9位的整数或1到2位小数组成!</h5>',
                    buttons: [
                        {
                            text: '确定',
                            type: 'button-theme button-local'

                        }
                    ]
                });
                return false;
            }
            if($scope.id){
                $scope.otherFeeInfo.feeSpendDate =$scope.dateBusin
            }else{
                if ($scope.selectedDate == "today") {
                    $scope.otherFeeInfo.feeSpendDate = $scope.today.dateStr;
                } else if ($scope.selectedDate == "yesterday") {
                    $scope.otherFeeInfo.feeSpendDate = $scope.yesterday.dateStr;
                } else if ($scope.selectedDate == "anteayer") {
                    $scope.otherFeeInfo.feeSpendDate = $scope.anteayer.dateStr;
                }
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
        //返回按钮
        $scope.cancel = function () {
            if($scope.otherFeeInfo.feeSpendDate){
                $location.path("/view_fee").search({date: $scope.otherFeeInfo.feeSpendDate});
            }else{
                if ($scope.backDate) {
                    $location.path("/view_fee").search({date: $scope.backDate});
                } else {
                    $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                        if (data.status == "0") {
                            $location.path("/view_fee").search({date: data.result});
                        }
                    });
                }
            }

        };
        $scope.showConfirm = function () {
            var confirmPopup = $ionicPopup.confirm({
                title: "返回提示",
                template: '<h5 class="margin-bottom-0">返回后当前编辑数据将丢失,确定要返回吗?</h5>',
                buttons: [
                    {
                        text: '继续编辑',
                        type: 'button-theme button-local button-outline'
                    },
                    {
                        text: '确定返回',
                        type: 'button-theme button-local',
                        onTap: function (e) {
                            $scope.cancel();
                        }
                    }
                ]
            });
        }

        //返回按钮
        $scope.doBack = function () {
            //点击返回编辑过数据 需要提示 您已编辑过内容，是要放弃？
            if ($scope.otherFeeInfo.money != 0 || $scope.selectedDate != "today" || $scope.otherFeeInfo.feeReason) {
                $scope.showConfirm();
            } else {
                $scope.cancel();
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
