/**
 * Project: yineng-corpSysLand
 * Package
 * Title: 事务交办查看页面控制器
 * author xiechangwei
 * date 2016/12/30 11:09
 * Copyright: 2016 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('viewAffairController', viewAffairController);

    viewAffairController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout','$ionicModal','Attachment'];
    function viewAffairController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location, $timeout,$ionicModal,Attachment){

        //数据Id
        $scope.dataId = $location.search().id;
        //1待办 2交办 3 关注 4 已办结
        $scope.pageType = $location.search().pageType;
        //控制选项卡
        $scope.showIsTab = $location.search().showIsTab;

        $http.post(basePath + "/third/affairsassigned/checkInstructionsStarus?assignedId=" + $scope.dataId).success(function (data) {
            //判断选中那个选项卡
            $scope.isTab =data;
            if($scope.showIsTab){
                $scope.isTab = $scope.showIsTab;
            }
            //判断加载那个页面的数据
            if(data !=1){
                $scope.getAppovalData(1);
                $scope.getAppovalData(2);
                //$scope.getAppoval(1);
                //$scope.getAppoval(2);
            }else{
                $scope.loadData();
            }

        })

        //反馈按钮
        $scope.showfeedBack =false;
        //批示
        $scope.showapproval = false;
        //显示更多
        $scope.showMoreButton = false;
        //显示审批
        $scope.showSubmit = false;
        //显示修改
        $scope.showEdit = false;
        //显示删除
        $scope.showDel = false;
        $scope.getInintData = function(){
            $http.post(basePath + "/third/affairsassigned/findBaseInfoByAssignedIdToApp?assignedId=" + $scope.dataId).success(function (data) {
                if(data.status =="0"){
                    $scope.assign = data.result;
                    //判断哪些按钮该显示
                    $http.post(basePath + "/third/affairsassigned/findAssignedStatus?assignedId=" + $scope.dataId).success(function (data) {
                        if(data.status =="0"){
                            $scope.flagList = data.result;
                            //如果是已办结 和关注的 不能进行其他操作
                            if($scope.pageType ==3 || $scope.pageType ==4 ){

                            }else{
                                angular.forEach($scope.flagList,function(item){
                                    if(item.feed==1){
                                        //反馈按钮
                                        $scope.showfeedBack =true;
                                    }
                                    if(item.sub==1){
                                        //批示
                                        $scope.showapproval = true;
                                    }
                                    if(item.more==1){
                                        //显示更多
                                        $scope.showMoreButton = true;
                                    }
                                    if(item.mit==1 && parseInt($scope.assign.transactionSchedule)==100){
                                        //显示审批
                                        $scope.showSubmit = true;
                                    }
                                    if(item.edit==1){
                                        //显示修改
                                        $scope.showEdit = true;
                                    }
                                    if(item.del==1 && parseInt($scope.assign.transactionSchedule)<=100){
                                        //显示删除
                                        $scope.showDel = true;
                                    }

                                    //如果更多下面的按钮一个都没有的话 那么连更多都不用显示
                                    if(!$scope.showSubmit && !$scope.showEdit && !$scope.showDel){
                                        $scope.showMoreButton = false;
                                    }



                                })
                            }


                        }
                    })

                }
            })
        }
        $scope.getInintData();



        //查看的基本信息findXTBGAffairsAssignedByAssignedIdToApp
        $scope.loadData = function(){
            //重新计算页面大小
            $ionicScrollDelegate.resize();
            $scope.upload.show =false;

            $scope.show = false;
            $scope.isTab =1;
            $http.post(basePath + "/third/affairsassigned/findXTBGAffairsAssignedByAssignedIdToApp?assignedId=" + $scope.dataId).success(function (data) {
                if(data.status =="0"){
                    $scope.baseInfo = data.result;
                    $scope.showdata ="";
                    $scope.baseInfo.instructionShow = $scope.getTitle($scope.baseInfo.instruction);
                    if($scope.baseInfo.isImportant){
                        $scope.showdata ="重要,";
                    }else{
                        $scope.showdata ="不重要,";
                    }
                    if($scope.baseInfo.isUrgent){
                        $scope.showdata +="紧急";
                    }else{
                        $scope.showdata +="不紧急";
                    }


                }
            })
        };

        /**
         *  查看附件
         */
        $scope.viewFileFun = function (fsIds) {
            if(fsIds && fsIds.length > 0){
                Attachment.show(fsIds.toString());
            }
        };

        //反馈批示 getXTBGRelatedOperationListToApp
        $scope.appovalData =[];
        $scope.submitList =[];
        $scope.getAppoval =function(item){
            //重新计算页面大小
            $ionicScrollDelegate.resize();
            $scope.upload.show =false;
            $scope.show = false;
            if(item==1){
                $scope.isTab = 2;
            }else{
                $scope.isTab = 3;
            }
            $http.post(basePath + "/third/affairsassigned/getXTBGRelatedOperationListToApp?assignedId=" + $scope.dataId+"&type="+item).success(function (data) {
                if(data.status =="0"){
                    if(item ==1){
                        $scope.appovalData = data.result;
                        if($scope.appovalData.length >0){
                            angular.forEach($scope.appovalData,function(item){
                                item.instructionShow =$scope.getTitle(item.instructions);
                            });
                        }
                    }else{
                        $scope.submitList =data.result;
                        if($scope.submitList.length >0){
                            angular.forEach($scope.submitList,function(item){
                                item.instructionShow =$scope.getTitle(item.instructions);
                                if(item.auditStatus == 1){
                                    $scope.assign.auditStatus = 1;
                                }
                            })
                        }

                    }

                }
            })
        }


        //返回主页面
        $scope.back = function(){
            $location.path('/affair_assign').search({pageType:$scope.pageType});;
        }

        //审批
        $scope.submit = function(){
            $scope.sub = angular.copy($scope.assign);
            var confirmPopup = $ionicPopup.show({
                title : '审核',
                template : '<textarea type="input" rows="5" class="form-control"  placeholder="输入审核意见" maxlength="1000" ng-model="sub.instructions"></textarea>',
                scope: $scope,
                buttons : [
                    {
                        text : '取消',
                        type : 'button-local button-outline button-theme button-min-width'
                    },
                    {
                        text : '不通过',
                        type : 'button-local button-theme',
                        onTap: function (e) {
                            //审核意见 改为非必填 变更2017年3月13日09:15:04
                            //if (!$scope.sub.instructions) {
                            //    $scope.error ("审核意见不能为空!");
                            //    e.preventDefault();
                            //}else{
                                $scope.sub.instructionsStarus =1;
                                $scope.subFunction(2);

                            //}


                        }
                    },
                    {
                        text : '通过',
                        type : 'button-local button-theme button-min-width',
                        onTap: function (e) {
                            //if (!$scope.sub.instructions) {
                            //    $scope.error ("审核意见不能为空!");
                            //    e.preventDefault();
                            //}else{
                                $scope.sub.instructionsStarus =1;
                                $scope.subFunction(1);
                            //}

                        }
                    }
                ]
            });

        }
        //修改
        $scope.edit = function(){
            $location.path('/add_assign').search({id:$scope.dataId,showIsTab:$scope.isTab})

        }
        //删除
        $scope.del = function(){
            var confirmPopup = $ionicPopup.confirm({
                title : '提示',
                template : '<h5 class="margin-bottom-0">删除后将无法恢复,确定要删除吗?',
                buttons : [
                    {
                        text : '取消',
                        type : 'button-theme button-local button-outline'
                    },
                    {
                        text : '确认',
                        type : 'button-theme button-local',
                        onTap: function (e) {
                            $http.post(basePath + "/third/affairsassigned/deleteJSTXAffairsAssignedList?assignedId=" + $scope.dataId).success(function (data) {
                                if (data.successCount >0) {
                                    $location.path('/affair_assign');

                                } else {
                                    var alertPopup = $ionicPopup.alert({
                                        title: '删除失败',
                                        template : '<h5 class="margin-bottom-0">'+data.message+'</h5>',
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

        //反馈
        $scope.feedBack = function(){
            $location.path('/affair_retroaction').search({id:$scope.dataId})

        }

        //批示
        $scope.approval = function(){
            $scope.appr = angular.copy($scope.assign)
            var confirmPopup = $ionicPopup.show({
                title : '批示',
                template : '<textarea rows="5" class="form-control"  placeholder="请输入批示内容" maxlength="1000" ng-model="appr.instructions"></textarea>',
                scope: $scope,
                buttons : [
                    {
                        text : '取消',
                        type : 'button-theme button-local button-outline'
                    },
                    {
                        text : '确认',
                        type : 'button-theme button-local',
                        onTap: function (e) {
                            if (!$scope.appr.instructions) {
                                $scope.notifyMsg("批示内容不能为空!")
                                e.preventDefault();
                            }else{
                                var dataInfo = {
                                    instructionsStarus : 3,
                                    affairsassignedUserId : $scope.appr.instructionsId,
                                    instructions : $scope.appr.instructions
                                }
                                $http.post(basePath + "/third/affairsassigned/saveJSTXRelatedOperation?", dataInfo).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.getAppoval(1);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }

                                })
                            }

                        }
                    }
                ]
            });
        }

        //展开按钮
        $scope.moreButton = function(){
            $scope.show = !$scope.show;
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

        //审核访问后台方法
        $scope.subFunction = function(item){
            var dataInfo = {
                instructionsStarus : 1,
                affairsassignedUserId : $scope.sub.toExamineId,
                instructions : $scope.sub.instructions,
                auditStatus:item

            }
            $http.post(basePath + "/third/affairsassigned/saveJSTXRelatedOperation?", dataInfo).success(function (data) {
                if (data.status == "0") {
                    $scope.getInintData();
                    //如果是审核通过的话 值显示返回按钮
                    if(item ==1){
                        //反馈按钮
                        $scope.showfeedBack =false;
                        //显示更多
                        $scope.showMoreButton = false;
                        //显示审批
                        $scope.showSubmit = false;
                        //显示修改
                        $scope.showEdit = false;
                        //显示删除
                        $scope.showDel = false;
                    }
                    $scope.getAppoval(2);
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
        $scope.upload = {show:false};
        //只显示有附件的记录
        $scope.showUpload = function (item) {
            $scope.appovalData =[];
            var newDataList =[];
            if($scope.upload.show){
                angular.forEach(item, function (newData) {
                    if(newData.uploadVoList != null &&newData.uploadVoList.length >0 ){
                        newDataList.push(newData);
                    }
                })
                $scope.appovalData = newDataList;
            }else{
                $scope.appovalData =[];
                //重新查询反馈批示
                $scope.getAppoval(1);

            }

        }

        //按时间倒序排列
        $scope.timedesc = true;
        $scope.timeDesc = function(){
            $scope.timedesc = !$scope.timedesc;

        }

        //对字符串进行截取
        $scope.getTitle = function(data){
            var title ="";
            if(data != null && data.length >50){
                $scope.showTitle =true;
                title = data.substring(0,50)+"...";
            }else{
                title = data;
            }
            return  title;

        }

        //查看更多
        $scope.showMore = function(item,type){
            if(item.instructionShow.length > 53){
                if(type==2){
                    item.instructionShow = $scope.getTitle(item.instructions);
                }else{
                    item.instructionShow = $scope.getTitle(item.instruction);
                }

            }else{
                if(type==2){
                    item.instructionShow = item.instructions;
                }else{
                    item.instructionShow = item.instruction;
                }

            }


        }

        //反馈批示
        //反馈批示 getXTBGRelatedOperationListToApp
        $scope.appovalData =[];
        $scope.submitList =[];
        $scope.getAppovalData =function(item){
            $http.post(basePath + "/third/affairsassigned/getXTBGRelatedOperationListToApp?assignedId=" + $scope.dataId+"&type="+item).success(function (data) {
                if(data.status =="0"){
                    if(item ==1){
                        $scope.appovalData = data.result;
                        if($scope.appovalData.length >0){
                            angular.forEach($scope.appovalData,function(item){
                                item.instructionShow =$scope.getTitle(item.instructions);
                            });
                        }
                    }else{
                        $scope.submitList =data.result;
                        if($scope.submitList.length >0){
                            angular.forEach($scope.submitList,function(item){
                                item.instructionShow =$scope.getTitle(item.instructions);
                                if(item.auditStatus == 1){
                                    $scope.assign.auditStatus = 1;
                                }
                            })
                        }

                    }

                }
            })
        }



    }
})();
