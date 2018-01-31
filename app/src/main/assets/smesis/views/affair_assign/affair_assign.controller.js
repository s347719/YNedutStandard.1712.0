/**
 * Project: yineng-corpSysLand
 * Package
 * Title: 事务交办主页面控制
 * author xiechangwei
 * date 2016/12/30 11:01
 * Copyright: 2016 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('affairAssignlController', affairAssignlController);

    affairAssignlController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function affairAssignlController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout){

        //控制选项卡
        $scope.showIsTab = $location.search().pageType;

        //数据
       $scope.dataList =[];
        //是否显示数据
        $scope.showData = false;
        //加载数据
        var i = 0,type= 1,flag=true,timer=null;
        if($scope.showIsTab){
            type =$scope.showIsTab;
        }
        //默认选中我的待办



        //更改选项卡
        $scope.changeStatus = function(item){
            $scope.showInfo = false;
            $scope.isTab = item;
            $scope.loadData(item);
        }

        //加载数据
        $scope.showInfo = false;
        $scope.loadData = function(item){
            $scope.isTab = item;
            if(type!=item){
                $scope.dataList =[];
                i =0;
            }
            type = item;
            var pageNumber =i;
            i++;
            var pageSize =20;
            var searchObj ={pageNumber:pageNumber,pageSize:pageSize,status:item};
            $http.get(basePath + "/third/affairsassigned/findJSTXAssignedByConditions",{params: searchObj}).success(function (data) {
                if(data.status == "0"){
                    if(data.result.length <20){
                        $scope.showInfo =true;
                    }
                    //$scope.showInfo = true;
                    flag = false;
                    var dataFlag =true;
                    var obj =[];
                    //获取时间
                    angular.forEach(data.result,function(newData){
                        angular.forEach($scope.dataList,function(oldData){
                            if(newData.id != oldData.id){
                                dataFlag =false;
                            }
                        })
                        if(dataFlag){
                            obj.push(newData)
                        }
                    })

                    $scope.dataList = $scope.dataList.concat(obj);
                    if(obj.length <20){
                        $scope.showInfo =true;
                        return;
                    }else{
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                    }
                    //$scope.$broadcast('scroll.infiniteScrollComplete');



                }
            });

        }

        if(!$scope.showIsTab){
            $scope.loadData(1);
            $scope.isTab =1;
        }else{
            $scope.isTab=$scope.showIsTab;
            $scope.loadData($scope.showIsTab);
        }

        //跳转到添加页面
        $scope.goToAdd = function(){
            $location.path('/add_assign').search({});
        }

        //跳转到查看页面
        $scope.goToShow = function(item){
            $location.path("/view_affair").search({id:item.id,pageType:$scope.isTab});
        }



    }
})();
