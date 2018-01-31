/**
 * project:     yineng-corpSysLand
 * title:      business_journal.controller.js
 * author:      xiechangwei
 * date:        2016/12/1 10:23
 * copyright:   2016 www.yineng.com.cn Inc. All rights reserved.
 * description:出差费用日记账页面控制器
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('businessJournalController', businessJournalController);

    businessJournalController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function businessJournalController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout){

        //跳转到查看页面
        $scope.goTo = function(item,list){
            angular.forEach(list,function(val){
                item.selecter = false;
                if(item.monthDay == val.monthDay){
                    item.selecter = true;
                }

            })
            $location.path("/view_fee").search({date:item.dataTime});

        }
        $scope.dataList =[];
        $scope.showInfo = false;

        //获取最近的20天的数据
        var i =3;
        $scope.loadData = function(item){
            if(!item){
                item ="";
            }
            i+=3;
            var pageNumber =3;
            var pageSize =i;
            if(i >15){
                $scope.showInfo = true;
                return;
            }

            var searchObj ={pageNumber:pageNumber,pageSize:pageSize,showDate:item};
            $http.get(basePath + "/third/businessjournal/findLatelyTimeAndDate",{params: searchObj}).success(function (data) {
                if(data.status == "0"){
                    //获取时间
                    $scope.dataList = $scope.dataList.concat(data.result);
                    $scope.showDate =$scope.dataList[$scope.dataList.length-1];
                    $scope.searchDate = $scope.showDate[$scope.showDate.length-1].dataTime
                   $scope.$broadcast('scroll.infiniteScrollComplete');
                }
            });
        }

        //$scope.loadData();


        //动态获取样式

        $scope.getClass = function (item) {
            if(item.dateDay =='[今天]'){
                return 'text-green';
            }else if(item.dateDay =='[昨天]'){
                return 'text-theme';
            }else{
                return 'text-gray';
            }


        }


        //$scope.$on('$stateChangeSuccess', function(item) {
        //    console.log("jjjjjjjjjjjjjjjjjjjjj");
        //    $scope.loadData(2,item);
        //});



    }
})();
