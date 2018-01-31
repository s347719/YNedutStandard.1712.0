(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('leaveApplyViewController', leaveApplyViewController);

    leaveApplyViewController.$inject = ['$scope', '$http', '$filter', '$ionicLoading',"$location","$ionicPopup","$timeout","$rootScope"];
    function leaveApplyViewController($scope, $http, $filter, $ionicLoading,$location,$ionicPopup,$timeout,$rootScope){
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
        $scope.leaveData ={};
        //根据数据id查询数据
        $http.post(basePath + "/third/leaveapply/findJSTXLeaveManageById?id="+$scope.id).success(function (data) {
            if (data.status == "0") {
                $scope.leaveData = data.result;
                //根据实例id获取当前任务节点Id
                $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProcessInstanceIdByEveryOneForCurrentTask?processInstanceId=" + $scope.actExecutionProcinstId).success(function (data) {
                    if (data.status == 0) {
                        //当前任务节点ID
                        $scope.leaveData.currentTaskId = data.result;
                    }
                });

            } else {
                $scope.notifyMsg("获取数据失败!");
            }
        })

        //返回发起申请主页面
        $scope.backHome = function () {
            // if (window.history.back()) {
            //     window.history.back();
            // } else {
            //     window.close();
            // }
            yn.plugin.yncordova.close();
        }
        //修改
        $scope.editData = function () {
            $location.path("/add_apply").search({
                id: $scope.id,
                actExecutionProcinstId: $scope.actExecutionProcinstId,
                processScheduleStates:$scope.processScheduleStates,
                applyId:$scope.leaveData.userId
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
                            $http.post(basePath + "/third/workflowmanage/deleteJSTXLeaveManageById?id="+$scope.id+"&processInstanceId="+$scope.actExecutionProcinstId).success(function (data) {
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
                            $http.post(basePath + "/third/workflowmanage/backJSTXLeaveManageByRevert", $scope.saveData).success(function (data) {
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
            $http.get(basePath + "/third/xtbgactivity/sendActivityRemindersMessage", {params: {taskId:  $scope.leaveData.currentTaskId}}).success(function (data) {
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
            }, 2000);
        }

        //流程配置
        $scope.options = {
            actExecutionProcinstId: $scope.actExecutionProcinstId,
            processScheduleStates: $scope.processScheduleStates

        }

        //流程审批
        $scope.agree = function (params) {
            //审批意见
            $scope.leaveData.message = params.message;
            //下一节点审批人
            $scope.leaveData.nextTaskUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.leaveData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/leaveapply/agree",$scope.leaveData).success(function(data){
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

        //交办
        $scope.assigned = function (params) {
            //审批意见
            $scope.leaveData.message = params.message;
            //下一节点审批人
            $scope.leaveData.assignedUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.leaveData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/leaveapply/updateJSTXLeaveManageByTransferAssignee",$scope.leaveData).success(function(data){
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
            $scope.leaveData.message = params.message;
            //下一节点审批人
            $scope.leaveData.nextTaskUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.leaveData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/leaveapply/backJSTXLeaveManageByStartUser",$scope.leaveData).success(function(data){
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
            $scope.leaveData.message = params.message;
            //下一节点审批人
            $scope.leaveData.nextTaskUserId = params.nextTaskUserId;
            //下一节点ID
            $scope.leaveData.nextUserTaskActId = params.nextUserTaskActId;
            $http.post(basePath + "/third/leaveapply/backJSTXLeaveManageByPreNode",$scope.leaveData).success(function(data){
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

        //手机端流程组件初始化
        $scope.flowOptions ={
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNLeave",
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
            routingStatusType:"YNLeave",
            nextNodeApprover:5,//下一节点审批人
            assignedPerson:6//交办人
        }
    }

})();
