/**
 * project:     yineng-corpSysLand
 * title:      view_other.controller
 * author:      xiechangwei
 * date:        2016/12/5 9:26
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:其他费用查看控制器
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('viewOtherController', viewOtherController);

    viewOtherController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function viewOtherController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout){
        //时间和id
        $scope.date = $location.search().backDate;//获得主页面返回的日期数据
        $scope.id = $location.search().id;

        //默认不展示修改和删除按钮
        $scope.showEdit = false;
        $scope.showDel = false;
        //获得当前系统日期并查询当前日期数据
        $http.get(basePath + "/third/businessjournal/getOtherFeeDateTime?").success(function (data) {
            if(data.status == 0){
                $scope.queryDate = data.result[0];//获取当前系统日期
                var editDay = parseInt($scope.compareDate($scope.date,$scope.queryDate));
                if(parseInt(editDay) <3){
                    $scope.showEdit = true;
                }
                var delDay = parseInt($scope.compareDate($scope.date,$scope.queryDate));
                if(parseInt(delDay) <1){
                    $scope.showDel =true;
                }
            }
        });
        //加载数据
        $scope.refresh = function () {
            $http.post(basePath + "/third/businessjournal/findDataById?id=" + $scope.id).success(function (data) {
                if(data.status =="0"){
                    $scope.other = data.result;
                }
            })
        }



        //返回
        $scope.goBack = function(){
            $location.path("/view_fee").search({date:$scope.date});
        }

        //删除
        $scope.del = function () {
            var confirmPopup = $ionicPopup.confirm({
                title : '提示',
                template : '<h5 class="margin-bottom-0">确定要删除此项费用吗?删除后将无法恢复?</h5>',
                buttons : [
                    {
                        text : '取消',
                        type : 'button-theme button-local button-outline'
                    },
                    {
                        text : '确认',
                        type : 'button-theme button-local',
                        onTap: function (e){
                            $http.post(basePath + "/third/commresource/deleteJSTXAddHotelFee?id=" + $scope.id + "&index=2").success(function (data) {
                                if (data.status == "0") {
                                    if($scope.dateBusin){
                                        $location.path("/view_fee").search({date:$scope.dateBusin});
                                    }else{
                                        $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
                                            if(data.status == "0"){
                                                $location.path("/view_fee").search({date:data.result});
                                            }
                                        });
                                    }
                                } else {
                                    var alertPopup = $ionicPopup.alert({
                                        title: '删除失败',
                                        template:'<h5 class="margin-bottom-0">data.message</h5>' ,
                                        buttons : [
                                            {
                                                text : '确定',
                                                type : 'button-theme button-local'
                                            }
                                        ]

                                    });
                                }
                            });
                        }


                    }
                ]

            });

        }
        //编辑
        $scope.edit = function () {
            $location.path("/add_other").search({id:$scope.id,backDate: $scope.date});//其他费
        }

        //加载数据
        $scope.refresh();

        //根据两个时间判断返回相差天数
        $scope.compareDate = function(startDate,endDate) {
            var day ="";
            var arr=startDate.split("-");
            var starttime=new Date(arr[0],arr[1]-1,arr[2]);
            var starttimes=starttime.getTime();

            var arrs=endDate.split("-");
            var lktime=new Date(arrs[0],arrs[1]-1,arrs[2]);
            var lktimes=lktime.getTime();
            day = (lktimes-starttimes)/86400000
            return day;
        }


    }
})();
