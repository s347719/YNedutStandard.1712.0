(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('staffLocusController', staffLocusController);

    staffLocusController.$inject = ['$scope', '$http',"$location","$timeout"];
    function staffLocusController($scope, $http,$location,$timeout){

        //获取当前人id
        $http.post( basePath + "/third/stafflocus/getCurrentUser" ).success(function(data){
            $scope.userCurrentId = data;
        });
        function getCookie(name) {
            return window.localStorage.getItem(name);
        }
        function setCookie(name,value) {
            window.localStorage.setItem(name,JSON.stringify(value));
        }

        $scope.tadayFlag = true;//默认显示 今日正常
        $scope.tadayFlagAll = true;//默认显示 今日正常
        $scope.searghAllFlag = false;//默认 不显示 查询数据
        $scope.searchFlag = true;//默认显示 查询按钮 false 旋转图标

        $scope.searchDataErrorFlag = false;
        $scope.normalFlag = true;//默认不显示 搜索时没有数据情况
        $scope.abnormalFlag = true;//默认不显示 搜索报错情况

        $scope.searchData = {};//搜索条件

        $scope.normal = 0;
        $scope.abnormal = 0;

        var oldData = null;
        $scope.isHoldBack = true;
        var getData = function(){
            setTimeout(function () {
                var  dat = getCookie("data"+$scope.userCurrentId);

                if(dat){

                    $scope.isHoldBack = false;

                    // 人员信息
                    $scope.listItem  = JSON.parse(dat);
                    oldData = angular.copy($scope.listItem)

                    var yes=0,noo=0;
                    angular.forEach($scope.listItem,function(info){
                        var normal= 0,abnormal=0;
                        if(info.jstxPositionBakDetailDTOList){//正常
                            normal = info.jstxPositionBakDetailDTOList.length;
                        }
                        if(info.jstxPositionBakDetailDTOAbnormalList){//异常
                            abnormal = info.jstxPositionBakDetailDTOAbnormalList.length;
                        }
                        info.total = normal + abnormal;
                        info.normal = normal;
                        info.abnormal = abnormal;

                        yes += normal;
                        noo += abnormal;
                    });
                    $scope.normal = yes;
                    $scope.abnormal = noo;
                    if(yes==0)$scope.normalFlag = false;
                    if(noo==0)$scope.abnormalFlag = false;

                    // 获得页面显示人员
                    $http.post( basePath + "/third/stafflocus/getAlluserByuserIds" ).success(function(data){
                        if(data.status=="0"){
                            setCookie("data"+$scope.userCurrentId,data.result);
                        }
                    })

                }else {
                    $scope.loadDataShow();
                }
            },100)

        }

        getData();

        $scope.loadData = function(){

        }

        $scope.loadDataShow = function(){
            // 获得页面显示人员
            $http.post( basePath + "/third/stafflocus/getAlluserByuserIds" ).success(function(data){

                $scope.$broadcast('scroll.infiniteScrollComplete');
                if(data.status=="0"){
                    $scope.isHoldBack = false;
                    setCookie("data"+$scope.userCurrentId,data.result);
                    // 人员信息
                    $scope.listItem  = data.result;
                    oldData = angular.copy($scope.listItem)
                    if($scope.listItem){
                        var yes=0,noo=0;
                        angular.forEach($scope.listItem,function(info){
                            var normal= 0,abnormal=0;
                            if(info.jstxPositionBakDetailDTOList){//正常
                                normal = info.jstxPositionBakDetailDTOList.length;
                            }
                            if(info.jstxPositionBakDetailDTOAbnormalList){//异常
                                abnormal = info.jstxPositionBakDetailDTOAbnormalList.length;
                            }
                            info.total = normal + abnormal;
                            info.normal = normal;
                            info.abnormal = abnormal;

                            yes += normal;
                            noo += abnormal;
                        });
                        $scope.normal = yes;
                        $scope.abnormal = noo;
                        if(yes==0)$scope.normalFlag = false;
                        if(noo==0)$scope.abnormalFlag = false;

                    }else{
                        $scope.normalFlag = false;
                        $scope.abnormalFlag = false;
                        $scope.searchDataErrorFlag = true;
                    }

                }else{
                    $scope.normalFlag = false;
                    $scope.abnormalFlag = false;
                    $scope.searchDataErrorFlag = true;
                }

            }).error(function(){
                $scope.searchDataFlag = false;
                $scope.searchDataErrorFlag = true;
            });
        }

        /**
         * 刷新试试
         */
        $scope.refreshModal = function () {
            getData();
        };
        /**
         * 跳转到个人轨迹明细页面
         * @param item页面人员对象
         */
        $scope.toPersonDetail = function(item,type){
            if(item.address){//正常时候，才跳转
                $location.path("/staff_locus_detail").search({id:item.userId,name:item.userName,orgName:item.orgName});
            }
        }

        /**
         * 数据切换
         * @param type 1 今日正常 2 今日异常
         */
        $scope.checkData = function (type) {
            if(type==1){
                $scope.tadayFlag = true;//默认显示 今日正常
            }else{
                $scope.tadayFlag = false;//默认显示 今日正常
            }
        };

        /**
         * 数据切换
         * @param type 1 今日正常 2 查询
         */
        $scope.checkSearch = function (type) {
            $scope.searchData = {};//搜索条件
            $scope.searchFlag = true;
            if(type==1){
                $scope.tadayFlagAll = false;
                $scope.searghAllFlag = true;
                $scope.search();
            }else{
                $scope.tadayFlagAll = true;
                $scope.searghAllFlag = false;
                getData();
            }


        };



        /**
         * 清除查询数据
         */
        $scope.delete = function () {
            $scope.searchData = {};//搜索条件
            $scope.searchFlag = true;
        };

        /**
         * 根据姓名查询数据
         */
        $scope.search = function (data) {
            $scope.searchFlag = false;
            //当获取数据成功 显示查询按钮
            if(oldData!=null){
                angular.forEach(oldData,function(item){
                    var newList = [];
                    angular.forEach($scope.listItem,function(vo){
                        if(vo.orgOrPersonCount==item.orgOrPersonCount){
                            if(item.jstxPositionBakDetailDTOList){
                                angular.forEach(item.jstxPositionBakDetailDTOList,function(val){
                                    if(data==undefined || data=="undefined"){
                                        newList.push(val);
                                    }else
                                    if(val.userName.indexOf(data)!=-1){
                                        newList.push(val);
                                    }
                                });
                            }
                            if(item.jstxPositionBakDetailDTOAbnormalList){
                                angular.forEach(item.jstxPositionBakDetailDTOAbnormalList,function(va){
                                    if(data==undefined || data=="undefined"){
                                        newList.push(va);
                                    }else if(va.userName.indexOf(data)!=-1){
                                        newList.push(va);
                                    }
                                });
                            }
                            vo.all = 0;
                            vo.allList = newList;
                        }
                    })

                });
                $scope.searchFlag = true;
                if($scope.listItem){
                    var f = false;
                    angular.forEach($scope.listItem,function(info){
                        if(info.allList && info.allList.length){
                            info.allList = info.allList.goHeavy();
                            info.all = info.allList.length;
                            if(info.all>0){
                                f = true;
                            }
                        }
                    });
                    if(f)
                        $scope.searchDataFlag = true;
                    else
                        $scope.searchDataFlag = false;

                }
            }
        };

        /**
         * 即时 搜索数据
         */
        $scope.$watch("searchData.name",function(newVal,oldVal){
            if(newVal && newVal != oldVal){
                //延迟加载数据
                $timeout(function () {
                    $scope.search(newVal);
                }, 200);
                $scope.searchFlag = false;
            }else if(!newVal){
                $scope.searchFlag = false;
                //延迟加载数据
                $timeout(function () {
                    $scope.search(newVal);
                    $scope.searchFlag = true;
                }, 200);
            }
        })


        //去重
        Array.prototype.goHeavy = function () {
            var res = [];
            var json = {};
            for(var i = 0; i < this.length; i++){
                if(this[i].userId){
                    if(!json[this[i].userId]){
                        res.push(this[i]);
                        json[this[i].userId] = true;
                    }
                }else{
                    res.push(this[i]);
                }
            }
            return res;
        };
    }
})();
