(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('outworkApplyViewController', outworkApplyViewController);
    outworkApplyViewController.$inject = ['$scope','$http','$ionicLoading',"$location","$ionicPopup","$timeout","$rootScope"];
    function outworkApplyViewController($scope,$http, $ionicLoading,$location,$ionicPopup,$timeout,$rootScope){

        //流程实例Id
        var actExecutionProcinstId = $location.search().actExecutionProcinstId;
        //流程状态
        $scope.processScheduleStates = $location.search().processScheduleStates;
        //数据id
        var id = $location.search().id;
        //申请人id
        $scope.applyId = $location.search().applyId;
        //类型
        $scope.type = $location.search().type;

        $scope.currentTaskId = {};

        $http.get(basePath + "/third/jstxOutSideManageResource/findXTBGOutSideManageById?id="+id).success(function(data) {
            $scope.update = data.result;
            //根据实例id获取当前任务节点Id
            $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProcessInstanceIdByEveryOneForCurrentTask?processInstanceId=" + actExecutionProcinstId).success(function (data) {
                if (data.status == 0) {
                    //当前任务节点ID
                    $scope.currentTaskId = data.result;
                    $scope.update.currentTaskId = data.result;
                }
            });
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
            $location.path("/outwork_apply_add").search({
                id: id,
                actExecutionProcinstId: actExecutionProcinstId,
                processScheduleStates:$scope.processScheduleStates,
                applyId:$scope.applyId
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
                            $http.post(basePath + "/third/jstxOutSideManageResource/deleteXTBGOutSideManageByIdAndProcessInstanceId?id="+id+"&processInstanceId="+actExecutionProcinstId).success(function (data) {
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
            $scope.saveData = {id:id};
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
                            $http.post(basePath + "/third/jstxOutSideManageResource/backXTBGOutSideManageByRevert", $scope.saveData).success(function (data) {
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
            $http.get(basePath + "/third/xtbgactivity/sendActivityRemindersMessage", {params: {taskId: $scope.currentTaskId}}).success(function (data) {
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

        $scope.options = {
            actExecutionProcinstId : actExecutionProcinstId,
            processScheduleStates : $scope.processScheduleStates
        }

        //流程审批
        $scope.agree = function (params) {
            $scope.update.message =params.message;
            $scope.update.nextTaskUserId =params.nextTaskUserId;
            $scope.update.nextUserTaskActId =params.nextUserTaskActId;
            $http.post(basePath + "/third/jstxOutSideManageResource/updateXTBGOutSideManageBySubmitProcess",$scope.update).success(function(data){
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
            $scope.update.message =params.message;
            $scope.update.assignedUserId =params.nextTaskUserId;
            $scope.update.nextUserTaskActId =params.nextUserTaskActId;
            $http.post(basePath + "/third/jstxOutSideManageResource/updateXTBGOutSideManageByTransferAssignee",$scope.update).success(function(data){
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
            $scope.update.message =params.message;
            $scope.update.nextTaskUserId =params.nextTaskUserId;
            $scope.update.nextUserTaskActId =params.nextUserTaskActId;
            $http.post(basePath + "/third/jstxOutSideManageResource/backXTBGOutSideManageByStartUser",$scope.update).success(function(data){
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
            $scope.update.message =params.message;
            $scope.update.nextTaskUserId =params.nextTaskUserId;
            $scope.update.nextUserTaskActId =params.nextUserTaskActId;
            $http.post(basePath + "/third/jstxOutSideManageResource/backXTBGOutSideManageByPreNode",$scope.update).success(function(data){
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
            instanceId: actExecutionProcinstId,
            definitionKey: "YNFieldPersonnel",
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
            routingStatusType:"YNFieldPersonnel",
            nextNodeApprover:5,//下一节点审批人
            assignedPerson:6//交办人
        }

    }
})();
