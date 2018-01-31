/**
 * Project: yineng-corpSysLand
 * Package
 * Title: 事务反馈页面控制
 * author xiechangwei
 * date 2016/12/30 11:08
 * Copyright: 2016 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('affairRetroactionController', affairRetroactionController);

    affairRetroactionController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function affairRetroactionController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout){

        //数据Id
        $scope.dataId = $location.search().id;
        $scope.assign = {};
        //默认显示 事务进度 反馈人为协办人时隐藏
        $scope.transactionScheduleFlag = true;

        $http.post(basePath + "/third/affairsassigned/findBaseInfoByAssignedIdToApp?assignedId=" + $scope.dataId+"&type=1").success(function (data) {
            if(data.status =="0"){
                $scope.assign = data.result;
                //设置附件信息
                if($scope.assign){
                    //判断反馈人是否为协办人
                    if($scope.assign.isCoOrganizer)$scope.transactionScheduleFlag = false;
                    setFsAnnexIDsFun($scope.assign, $scope.assign.uploadVoList);
                }
                $scope.transactionSchedule = $scope.assign.transactionSchedule;
            }
        });
        //附件处理方法
        var setFsAnnexIDsFun = function ( info , uploadVoList) {
            //附件处理
            if(uploadVoList && uploadVoList.length){
                var fsAnnexIDs = uploadVoList.toString();
                info.fsAnnexIDs = fsAnnexIDs;
                $scope.$broadcast('filesDone');
            }
        };
        //返回上一级
        $scope.back = function(){
            var confirmPopup = $ionicPopup.confirm({
                title : '返回提示',
                template : '<h5 class="margin-bottom-0">返回后当前编辑内容将丢失,确定要返回吗？</h5>',
                buttons : [
                    {
                        text : '继续编辑',
                        type : 'button-outline button-theme button-local'
                    },
                    {
                        text : '确定返回',
                        type : 'button-theme button-local',
                        onTap: function (e) {
                            $scope.cancle();
                        }
                    }
                ]
            });
        }

        //返回
        $scope.cancle = function(){
            $location.path("/view_affair").search({id:$scope.dataId});
        }


        //验证必填项
        $scope.error = function(data){
            var alertPopup = $ionicPopup.alert({
                title : '提示',
                template : '<h5 class="margin-bottom-0">'+data+'</h5>',
                buttons : [
                    {
                        text : '确认',
                        type : 'button-theme button-local'

                    }
                ]

            });
        };

        $scope.checkData = function(){
            //验证必填效果
            var speed =$scope.assign.transactionSchedule;
            var re = /^[0-9]*[1-9][0-9]*$/ ;
            //显示的时候才验证
            if($scope.transactionScheduleFlag){
                if(re.test(speed)){

                }else{
                    $scope.notifyMsg("事务进度输入不合法,最小为1,最大100");
                    $scope.assign.transactionSchedule =1;
                    return false;
                }

                if(parseInt($scope.assign.transactionSchedule) >100){
                    $scope.notifyMsg("事务进度填写不合理,最大为100%!");
                    $scope.assign.transactionSchedule =100;
                    return false;
                }
                if(parseInt($scope.transactionSchedule) > parseInt($scope.assign.transactionSchedule)){
                    $scope.notifyMsg("事务进度不能小于当前事务进度！当前进度：" +parseInt( $scope.transactionSchedule) + "%");
                    return false;
                }

            }

            if (!$scope.assign.instructions) {
                $scope.notifyMsg("反馈内容不能为空!");
                return false;
            }
            //处理附件问题
            if( $scope.assign &&  $scope.assign.fsAnnexIDs){
                $scope.assign.uploadVoList = [];
                var upList = $scope.assign.fsAnnexIDs.split(",");
                angular.forEach(upList, function (u) {
                    if(u){
                        $scope.assign.uploadVoList.push(u);
                    }
                });
            }
            var dataInfo = {
                instructionsStarus : 2,
                affairsassignedUserId : $scope.assign.affairsassignedUserId,
                instructions : $scope.assign.instructions,
                transactionSchedule : $scope.assign.transactionSchedule,
                uploadVoList: $scope.assign.uploadVoList
            }
            $http.post(basePath + "/third/affairsassigned/saveJSTXRelatedOperation?", dataInfo).success(function (data) {
                if (data.status == "0") {
                    $location.path("/view_affair").search({id:$scope.dataId});
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


            })

        }

        //验证字数
        $scope.min =0;
        $scope.checkText = function(text){
            $scope.min = text.length;

        }

        //add
        $scope.changeSpeed = function(item){
            if(item ==1){
                if(!$scope.assign.transactionSchedule){
                    $scope.assign.transactionSchedule =0;
                }
                $scope.assign.transactionSchedule =parseInt($scope.assign.transactionSchedule) +5;
                if(parseInt($scope.assign.transactionSchedule)>100){
                    $scope.notifyMsg("事务进度最大为100%!");
                    $scope.assign.transactionSchedule =100;
                    return false;
                }
            }else{
                if(!$scope.assign.transactionSchedule){
                    $scope.assign.transactionSchedule =0;
                }
                $scope.assign.transactionSchedule =parseInt($scope.assign.transactionSchedule) -5;
                if(parseInt($scope.assign.transactionSchedule) <$scope.transactionSchedule){
                    $scope.notifyMsg("事务进度不能不能小于当前进度!")
                    $scope.assign.transactionSchedule =$scope.transactionSchedule;
                    return false;
                }
                if(parseInt($scope.assign.transactionSchedule) <0 ){
                    $scope.notifyMsg("事务进度不能为负!");
                    $scope.assign.transactionSchedule =0;
                    return false;
                }


            }

        }

        //显示提示信息
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }



    }
})();
