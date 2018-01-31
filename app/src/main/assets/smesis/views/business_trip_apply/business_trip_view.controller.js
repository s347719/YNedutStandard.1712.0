/**
 * Project: yineng-corpSysLand
 * Package
 * Title: fileName
 * author xiechangwei
 * date 2017/5/4 15:53
 * Copyright: 2017 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('businessTripViewController', businessTripViewController);
    businessTripViewController.$inject = ['$scope', '$ionicModal', '$http', '$ionicPopup', '$rootScope', '$ionicLoading', '$filter', '$timeout', '$location'];
    function businessTripViewController($scope, $ionicModal, $http, $ionicPopup, $rootScope, $ionicLoading, $filter, $timeout, $location) {

        //表单tab
        $scope.isTab = $location.search().isTab;
        //数据id
        $scope.id = $location.search().id;
        //类型
        $scope.type = $location.search().type;
        //流程实例Id
        $scope.actExecutionProcinstId = $location.search().actExecutionProcinstId;
        //流程状态
        $scope.processScheduleStates = $location.search().processScheduleStates;
        //申请人id
        $scope.applyId = $location.search().applyId;
        //根据数据id获取数据
        $http.get(basePath + "/third/workflowmanage/findJSTXMissionMangeById?id=" + $scope.id).success(function (data) {
            if (data.status == "0") {
                $scope.businessData = data.result;
                //根据实例id获取当前任务节点Id
                $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProcessInstanceIdByEveryOneForCurrentTask?processInstanceId=" + $scope.actExecutionProcinstId).success(function (data) {
                    if (data.status == 0) {
                        //当前任务节点ID
                        $scope.businessData.currentTaskId = data.result;
                    }
                });
            }
        });



        //返回发起申请主页面
        $scope.backHome = function () {
            if (window.history.back()) {
                window.history.back();
            } else {
                window.close();
            }
        }

        //修改
        $scope.editData = function () {
            $location.path("/business_trip_add").search({
                id: $scope.id,
                actExecutionProcinstId: $scope.actExecutionProcinstId,
                processScheduleStates:$scope.processScheduleStates,
                applyId:$scope.businessData.userId
            });
        }

        //删除流程
        $scope.delete = function (item) {
            var confirmPopup = $ionicPopup.confirm({
                title: '提示',
                template: '<h5 class="margin-bottom-0">确定要删除吗？</h5>',
                buttons: [
                    {
                        text: '取消',
                        type: 'button-outline button-theme button-local'
                    },
                    {
                        text: '确定删除',
                        type: ' button-theme button-local',
                        onTap: function (e) {
                            $http.post(basePath + "/third/workflowmanage/deleteJSTXMissionManageById?id="+$scope.id+"&processInstanceId="+$scope.actExecutionProcinstId).success(function (data) {
                                if (data.status == "0") {
                                    $scope.notifyMsg(data.message);
                                    $scope.backHome();
                                } else {
                                    $scope.notifyMsg(data.message);
                                }
                            })

                        }
                    }
                ]

            });

        }
        //撤销流程
        $scope.revertData = function (item) {
            $scope.saveData = {id:$scope.id};
            var confirmPopup = $ionicPopup.confirm({
                title: '提示',
                template: '<h5 class="margin-bottom-0">确定要撤销吗？</h5>',
                buttons: [
                    {
                        text: '取消',
                        type: 'button-outline button-theme button-local'
                    },
                    {
                        text: '确定撤销',
                        type: ' button-theme button-local',
                        onTap: function (e) {
                            $http.post(basePath + "/third/workflowmanage/backJSTXMissionManageByRevert", $scope.saveData).success(function (data) {
                                if (data.status == "0") {
                                    $scope.notifyMsg(data.message);
                                    $scope.backHome();
                                } else {
                                    $scope.notifyMsg(data.message);
                                }
                            })

                        }
                    }
                ]

            });

        }
        //催办
        $scope.urgeData = function (item) {
            $http.get(basePath + "/third/xtbgactivity/sendActivityRemindersMessage", {params: {taskId: $scope.businessData.currentTaskId}}).success(function (data) {
                $scope.notifyMsg(data.message);
            });

        }

        //消息提示
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            },2000);
        }

        //流程配置
        $scope.options = {
            actExecutionProcinstId: $scope.actExecutionProcinstId,
            processScheduleStates: $scope.processScheduleStates

        }


        //流程审批
        $scope.agree = function (params) {
            //审批意见
            $scope.businessData.message = params.message;
            //下一节点审批人
            $scope.businessData.nextTaskUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.businessData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/businesstrip/updateJSTXMissionManageBySubmitProcess",$scope.businessData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("提交成功");
                    $timeout(function(){
                        $scope.backHome();
                    },2000);
                }else{
                    $scope.notifyMsg(data.message);
                }
            })
        }

        //交办
        $scope.assigned = function (params) {
            //审批意见
            $scope.businessData.message = params.message;
            //下一节点审批人
            $scope.businessData.assignedUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.businessData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/businesstrip/updateJSTXMissionManageByTransferAssignee",$scope.businessData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("提交成功");
                    $timeout(function(){
                        $scope.backHome();
                    },500);
                }else{
                    $scope.notifyMsg(data.message);
                }
            })
        }

        //驳回发起人
        $scope.returnApply = function (params) {
            //审批意见
            $scope.businessData.message = params.message;
            //下一节点审批人
            $scope.businessData.nextTaskUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.businessData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/businesstrip/backJSTXMissionManageByStartUser",$scope.businessData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("提交成功");
                    $timeout(function(){
                        $scope.backHome();
                    },500);
                }else{
                    $scope.notifyMsg(data.message);
                }
            })
        }
        //驳回上一步
        $scope.returnLastStep = function (params) {
            //审批意见
            $scope.businessData.message = params.message;
            //下一节点审批人
            $scope.businessData.nextTaskUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.businessData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/businesstrip/backJSTXMissionManageByPreNode",$scope.businessData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg(data.message);
                    $timeout(function(){
                        $scope.backHome();
                    },500);
                }else{
                    $scope.notifyMsg(data.message);
                }
            })
        }

        //手机端流程组件初始化
        $scope.flowOptions ={
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNBusinessTravel",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            button: {
                //同意
                agree: $scope.agree,
                //交办
                assign: $scope.assigned,
                //催办
                urge: true,
                //驳回发起人
                reject: $scope.returnApply,
                //驳回上一步
                rejectPreNode: $scope.returnLastStep,
                //提交审批
                startProcess: $scope.submit,
                //保存草稿
                saveDraft: $scope.save,
                //撤销
                repeal: $scope.revert,
                //删除草稿
                deleteDraft: $scope.delete,
                //删除流程
                deleteProcess: $scope.deleteflow
            },
            routingStatusType:"YNBusinessTravel",
            nextNodeApprover:5,//下一节点审批人
            assignedPerson:6//交办人
        }

    }
})();
