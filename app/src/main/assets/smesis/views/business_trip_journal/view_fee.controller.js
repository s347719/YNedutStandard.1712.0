/**
 * project:     yineng-corpSysLand
 * title:      view_fee.controller
 * author:      xiechangwei
 * date:        2016/12/2 15:52
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:出差费用日记账查看页面
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('viewFeeController', viewFeeController);

    viewFeeController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function viewFeeController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout){

        //根据数据id和人取查询数据
        $scope.time = $location.search().date;

        //是否显示 添加费用按钮  默认显示
        $scope.showFeiYong = true;

        //获得当前系统日期并查询当前日期数据
        $http.get(basePath + "/third/businessjournal/getOtherFeeDateTime?").success(function (data) {
            if(data.status == 0){
                $scope.queryDate = data.result[0];//获取当前系统日期
                var editDay = parseInt($scope.compareDate($scope.time,$scope.queryDate));
                if(editDay>=3){
                    $scope.showFeiYong = false;
                }
            }
        });
        //是否显示数据
        $scope.showData = false;
        //总共费用
        $scope.countMoney =0;
        //是否显示点击按钮
        $scope.showClickButton = true;

        var timeList = [];
        if($scope.time){
            timeList =  $scope.time.split("-");

        }else{
            timeList =  $scope.queryDate.split("-");
        }
        $scope.showTime = timeList[1]+"月"+timeList[2]+"日";

        $scope.loadData = function(){
            $http.post(basePath + "/third/businessjournal/findDataByDataIdAndUserId?time="+$scope.time).success(function (data) {
                if(data.status == "0"){
                    if(data.result != null){
                        $scope.data = data.result;
                        $scope.showData = true;
                        $scope.countMoney = $scope.data.countMoney;
                    }
                }
            });
        }

        //冒泡提示
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 800);
        }

        //点击按钮显示修改和删除按钮
        $scope.clickButton = function(item){
            //item.showButton = true;
            var editDay = parseInt($scope.compareDate($scope.time,$scope.queryDate));
            if(parseInt(editDay) <3){
                item.showEdit = true;
                item.showClickButton = false;
            }
            var delDay = parseInt($scope.compareDate($scope.time,$scope.queryDate));
            if(parseInt(delDay) <1){
                item.showDel =true;
                item.showClickButton = false;
            }
            if(!item.showEdit && !item.showDel){
                $scope.notifyMsg("该数据不能修改和删除!");
                item.showButton = true;
            }
            $scope.test_1 = item.showButton;

        }
        //点击修改按钮跳转
        $scope.clickEdit = function(item,type){
            if(type == 0){
                $location.path("/add_hotel").search({id:item.id,backDate: $scope.time});//酒店费
            }
            if(type == 1){
                $location.path("/add_trans").search({id:item.id,backDate: $scope.time});//交通费
            }
            if(type == 2){
                $location.path("/add_other").search({id:item.id,backDate: $scope.time});//其他费
            }

        }
        //点击删除按钮跳转
        //删除记录
        $scope.deleteFun = function(item,index){
            var confirmPopup = $ionicPopup.confirm({
                title : '提示',
                template : '<h5 class="margin-bottom-0">确定要删除此项费用吗?删除后将无法恢复!',
                buttons : [
                    {
                        text : '取消',
                        type : 'button-theme button-local button-outline'
                    },
                    {
                        text : '确认',
                        type : 'button-theme button-local',
                        onTap: function (e) {
                            $http.post(basePath + "/third/commresource/deleteJSTXAddHotelFee?id=" + item.id + "&index=" + index).success(function (data) {
                                if (data.status =="0") {
                                    window.location.reload();

                                } else {
                                    var alertPopup = $ionicPopup.alert({
                                        title: '删除失败',
                                        template : '<h5 class="margin-bottom-0">data.message</h5>',
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
        //查看详细
        $scope.showDetail = function(item,type){
            if(type == 0){
                $location.path("/view_hotel").search({id:item.id,backDate: $scope.time});//酒店费
            }
            if(type == 1){
                $location.path("/view_trans").search({id:item.id,backDate: $scope.time});//交通费
            }
            if(type == 2){
                $location.path("/view_other").search({id:item.id,backDate: $scope.time});//其他费
            }

        }



        //切换日期
        $scope.nextDay = function(item){
            if(item==1){
                if ($scope.queryDate == $scope.time) {
                    $scope.notifyMsg("已经是最新一天!");
                    return false;
                }else{
                    //获取后一天
                    $scope.time =  $scope.dateChange($scope.time,1);
                    var editDay = parseInt($scope.compareDate($scope.time,$scope.queryDate));
                    if(editDay>=3){
                        $scope.showFeiYong = false;
                    }else{
                        $scope.showFeiYong = true;
                    }
                    var timeList =[];
                    timeList =  $scope.time.split("-");
                    $scope.showTime = timeList[1]+"月"+timeList[2]+"日";
                    $http.post(basePath + "/third/businessjournal/findDataByDataIdAndUserId?time="+$scope.time).success(function (data) {
                        if(data.status == "0"){
                            if(data.result != null){
                                $scope.data = data.result;
                                $scope.showData = true;
                                $scope.countMoney = $scope.data.countMoney;
                            }else{
                                //是否显示数据
                                $scope.showData = false;
                                $scope.countMoney =0;
                            }
                        }
                    });

                }
            }else{
                //获取前一天
                $scope.time =  $scope.dateChange($scope.time,2);
                var editDay = parseInt($scope.compareDate($scope.time,$scope.queryDate));
                if(editDay>=3){
                    $scope.showFeiYong = false;
                }else{
                    $scope.showFeiYong = true;
                }
                var timeList =[];
                timeList =  $scope.time.split("-");
                $scope.showTime = timeList[1]+"月"+timeList[2]+"日";
                $http.post(basePath + "/third/businessjournal/findDataByDataIdAndUserId?time="+$scope.time).success(function (data) {
                    if(data.status == "0"){
                        if(data.result != null){
                            $scope.data = data.result;
                            $scope.showData = true;
                            $scope.countMoney = $scope.data.countMoney;
                        }else{
                            //是否显示数据
                            $scope.showData = false;
                            $scope.countMoney =0;
                        }
                    }
                });
            }
            //重新加载数据
            $scope.loadData();

        }

        //按钮控制阻止冒泡
        $scope.goTo = {
            isGoTo: true,
            gotoFun: function(data ,item ,type){
                if(this.isGoTo){
                    if(data == 1){
                        this.isGoTo = !this.isGoTo;
                        $scope.showDetail(item,type)
                    }else if(data == 2){
                        this.isGoTo = !this.isGoTo;
                        $scope.clickButton(item);

                    }else if(data ==3){
                        this.isGoTo = !this.isGoTo;
                        $scope.clickEdit(item,type);

                    }else if(data==4){
                        this.isGoTo = !this.isGoTo;
                        $scope.deleteFun(item,type);
                    }
                    setTimeout(function(){
                        $scope.goTo.isGoTo = true;
                    },100);

                }
            }
        };

        //返回到主页面
        $scope.goBack = function(){
            $location.path("/business_journal").search({});
        }
        //添加酒店费
        $scope.addTrans = function(){
            $location.path("/add_trans").search({id:null,backDate:$scope.time,date: null,showTime:$scope.showTime.replace("月","-").replace("日","")});

        }

        //添加住宿费
        $scope.addHotel = function(){
            $http.post(basePath + "/third/commresource/findHotelDateAndUserId?date1="+$scope.time).success(function (data) {
                if(data.status=='0'){
                    $location.path("/add_hotel").search({id:null,backDate:$scope.time,date: null,showTime:$scope.showTime.replace("月","-").replace("日","")});
                }else{
                    $scope.showMessage(data.message);

                }
            });


        }
        //添加其他费
        $scope.addOther = function(){
            $location.path("/add_other").search({id:null,backDate:$scope.time,date: null,showTime:$scope.showTime.replace("月","-").replace("日","")});

        }

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


        //默认加载数据
        $scope.loadData();

        //把时间加一天或者减一天转换
        $scope.dateChange = function(date,type){
            var translateDate = date.replace("-", "/").replace("-", "/");
            var newDate = new Date(translateDate);
            var nextDate ="";
            if(type==1){
                nextDate = new Date(newDate.getTime() + 24*60*60*1000);
            }else{
                nextDate = new Date(newDate.getTime() - 24*60*60*1000);
            }

            var  year=nextDate.getFullYear();
            var  month=nextDate.getMonth()+1;
            if(parseInt(month) <10){
                month ="0"+month;
            }
            var  date=nextDate.getDate();
            if(parseInt(date) <10){
                date ="0"+date;
            }
            var time =year+"-"+month+"-"+date;
            return time;
        }

        //修改时提示不允许修改
        $scope.showMessage = function(msg){
            $ionicLoading.show({
                template:msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }





    }
})();
