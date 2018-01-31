/**
 * Created by wuhaiying on 2017/4/24.
 */
(function () {
    'use strict';
    angular
        .module('myApp')
        .controller('applyController', applyController);
    applyController.$inject = ['$scope', '$ionicModal', '$http', '$timeout', '$location','$ionicPopup','$ionicLoading','$ionicScrollDelegate'];
    function applyController($scope, $ionicModal, $http, $timeout, $location,$ionicPopup,$ionicLoading,$ionicScrollDelegate) {

        //声明查询对象条件参数
        $scope.conditions = {};

        //获取手机端可以操作的流程类型
        $http.post(basePath + "/third/xtbgactivity/findworkFlowTypeToMoblie").success(function (data) {
            if (data.status == 0) {
                $scope.flowTypeList = data.result;
            }
        })

        //根据当前人获取有权限的流程
        $http.post(basePath + "/third/xtbgactivity/getworkFlowTypeList").success(function (data) {
            if (data.status == 0) {
                $scope.workFlowList = data.result;
            }
        })


        //审批状态
        $scope.approveTypeList = [{code: "", name: "全部", isClick: true},
            {code: "1", name: "进行中", isClick: false},
            {code: "2", name: "已结束", isClick: false},
        ]

        //是否显示没有数据
        $scope.showNoData =false;
        $scope.isMoreData = true;
        $scope.page = 0;
        $scope.dataList = [];
        //显示错误
        $scope.showError = false;
        //查询数据
        $scope.clickType = function (type, item) {
            $scope.isMoreData = false;
            $scope.showNoData = false;
            if (type == 1) {
                $scope.page = 0;
                //流程名称
                $scope.conditions.actKey = item.flowKey;
                $scope.doRefresh();
                item.isClick = !item.isClick;
                angular.forEach($scope.flowTypeList, function (val) {
                    if (item.flowName != val.flowName) {
                        val.isClick = false;
                    } else {
                        item.isClick = true;
                    }
                })
            } else {
                //流程状态
                $scope.conditions.flowStates = item.code;
                $scope.page = 0;
                $scope.doRefresh();
                item.isClick = !item.isClick;
                angular.forEach($scope.approveTypeList, function (val) {
                    if (item.name != val.name) {
                        val.isClick = false;
                    } else {
                        item.isClick = true;
                    }
                })

            }
        }
        //doRefresh 只加载第一页数据
        $scope.doRefresh = function () {
            $http.post(basePath + "/third/xtbgactivity/findMyStartworkFolwByConditions?pageNumber=0&&pageSize=20", $scope.conditions).success(function (data) {
                if (data.status == "0") {
                    //重新计算页面大小 填充页面
                    $ionicScrollDelegate.resize();
                    $scope.dataList = data.result;
                    if($scope.dataList.length ==0){
                        $scope.showNoData =true;
                    }
                    //如果页面数据没有填充完 则不继续加载数据了
                    if($scope.dataList.length <20){
                        $scope.isMoreData = false;
                    }
                    $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                } else {
                    $scope.showError = true;
                }
            })
        };

        //加载数据
        $scope.loadMore = function (type) {
            if (type) {
                $scope.page = 0;
            } else {
                type = "";
            }
            $http.post(basePath + "/third/xtbgactivity/findMyStartworkFolwByConditions?pageNumber=" + $scope.page + "&pageSize=20", $scope.conditions).success(function (data) {
                if (data.status == "0") {
                    $scope.page++;
                    if(type && type == 1){
                        $scope.dataList =[];
                        $scope.dataList = $scope.dataList.concat(data.result);
                    }else{
                        $scope.dataList = $scope.dataList.concat(data.result);
                    }
                    if($scope.dataList.length ==0){
                        $scope.showNoData =true;
                    }
                    if (type && type == 1) {
                        if (data.result.length == 0) {
                            $scope.dataList = [];
                        }
                    }
                    if (data.result.length != 0) {
                        $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                    } else {
                        $scope.isMoreData = false;
                    }
                } else {
                    $scope.showError = true;
                }
            })
        };
        // 上滑事件状态改变时，执行加载更多
        $scope.$on('stateChangeSuccess', function () {
            $scope.loadMore();
        });

        //删除流程
        $scope.deleteData = function (item) {
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
                            //请假申请
                            if (item.actKey == 'YNLeave') {
                                $http.post(basePath + "/third/workflowmanage/deleteJSTXLeaveManageById?id="+item.bussinessId+"&processInstanceId="+item.actExecutionProcinstId).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.notifyMsg(data.message);
                                        $scope.loadMore(1);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })

                            }
                            //出差申请
                            if (item.actKey == 'YNBusinessTravel') {
                                $http.post(basePath + "/third/workflowmanage/deleteJSTXMissionManageById?id="+item.bussinessId+"&processInstanceId="+item.actExecutionProcinstId).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })

                            }
                            //外勤申请
                            if (item.actKey == 'YNFieldPersonnel') {
                                $http.post(basePath + "/third/workflowmanage/deleteJSTXOutSideManageById?id="+item.bussinessId+"&processInstanceId="+item.actExecutionProcinstId).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })
                            }
                            //超标费用
                            if (item.actKey == 'YNExcessivCost') {
                                $http.post(basePath + "/third/excessivCostApply/deleteXTBGStandardCostApplyByIdAndProcessInstanceId?id="+item.bussinessId+"&processInstanceId="+item.actExecutionProcinstId).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })
                            }
                            //费用申请
                            if (item.actKey == 'YNCostApply') {
                                $http.post(basePath + "/third/feeCostApply/deleteFeeCostApplyById?id="+item.bussinessId).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg("操作成功");
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
        //撤销流程
        $scope.revertData = function (item) {
            $scope.saveData = {id: item.bussinessId, processState: 7};  //processState:   4 驳回发起人 7 撤销流程
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
                            //请假申请
                            if (item.actKey == 'YNLeave') {
                                $http.post(basePath + "/third/workflowmanage/backJSTXLeaveManageByRevert", $scope.saveData).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);

                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })

                            }
                            //出差申请
                            if (item.actKey == 'YNBusinessTravel') {
                                $http.post(basePath + "/third/workflowmanage/backJSTXMissionManageByRevert", $scope.saveData).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })

                            }
                            //外勤申请
                            if (item.actKey == 'YNFieldPersonnel') {
                                $http.post(basePath + "/third/workflowmanage/backJSTXOutSideManageByRevert", $scope.saveData).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);
                                    } else {
                                        $scope.notifyMsg(data.message);
                                    }
                                })
                            }
                            //超标费用申请
                            if (item.actKey == 'YNExcessivCost') {
                                $http.post(basePath + "/third/excessivCostApply/backXTBGStandardCostApplyByStartUser", $scope.saveData).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.loadMore(1);
                                        $scope.notifyMsg(data.message);
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
        //修改流程
        $scope.editData = function (item) {
            //请假申请
            if (item.actKey == 'YNLeave') {
                $location.path("/add_apply").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId,
                    currentTaskId:item.currentTaskId
                });
            }
            //出差申请
            if (item.actKey == 'YNBusinessTravel') {
                $location.path("/business_trip_add").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId,
                    currentTaskId:item.currentTaskId
                });
            }

            //外勤申请
            if (item.actKey == 'YNFieldPersonnel') {
                $location.path("/outwork_apply_add").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId,
                    currentTaskId:item.currentTaskId
                });
            }
            //超标费用申请
            if (item.actKey == 'YNExcessivCost') {
                $location.path("/excessiv_cost_apply").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId,
                    currentTaskId:item.currentTaskId,
                    type: 'edit'
                });
            }

            //费用申请修改
            if(item.actKey =="YNCostApply"){
                $location.path("/cost_apply").search({id:item.bussinessId});
            }

        }
        //催办流程
        $scope.urgeData = function (item) {
            $http.get(basePath + "/third/xtbgactivity/sendActivityRemindersMessage", {
                params: {
                    taskId: item.currentTaskId
                }
            }).success(function (data) {
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

        //发起流程
        $scope.applyFlow = function (item) {
            //流程 跳转对象
            var actKeyApply = {
                'YNLeave': "/add_apply",  //请假申请
                'YNBusinessTravel': "/business_trip_add", //出差申请
                'YNFieldPersonnel': "/outwork_apply_add", //外勤申请
                'YNExcessivCost': "/excessiv_cost_apply",   //超标费用
                'YNWorkOverTime': ""
            };
            if(actKeyApply[item.flowKey]){
                $location.path(actKeyApply[item.flowKey]).search({id: null, actExecutionProcinstId: item.actExecutionProcinstId,processDefinitionId: item.processDefinitionId });
            }
            //费用申请
            if(item.flowKey =="YNCostApply"){
                $location.path("/cost_apply").search({id:null});
            }
        }
        /*选择申请类型*/
        $ionicModal.fromTemplateUrl('apply-style.html', {
            scope: $scope
        }).then(function (modal) {
            $scope.modalOne = modal;
        });
        $scope.chooseApplyStyle = function () {
            $scope.modalOne.show();
        };
        $scope.closeApplyStyle = function () {
            $scope.modalOne.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function () {
            $scope.modalOne.remove();
        });

        ///*申请详情*/
        //$ionicModal.fromTemplateUrl('apply-detail.html', {
        //    scope: $scope,
        //    animation: 'slide-in-right'
        //}).then(function (modal) {
        //    $scope.modalTwo = modal;
        //});
        //查看详情
        $scope.showApplyDetail = function (item) {
            //请假申请
            if (item.actKey == 'YNLeave') {
                $location.path("/leave_apply_view").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId
                });
            }
            //出差申请
            if (item.actKey == 'YNBusinessTravel') {
                $location.path("/business_trip_view").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId
                });
            }

            //外勤申请
            if (item.actKey == 'YNFieldPersonnel') {
                $location.path("/outwork_apply_view").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId
                });
            }

            //超标费用
            if (item.actKey == 'YNExcessivCost') {
                $location.path("/excessiv_cost_apply").search({
                    id: item.bussinessId,
                    actExecutionProcinstId: item.actExecutionProcinstId,
                    processScheduleStates:item.processScheduleStates,
                    applyId:item.applyUserId
                });
            }
            //费用申请
            if(item.actKey =="YNCostApply"){
                $location.path("/cost_apply_view").search({id:item.bussinessId});
            }
        };
        //$scope.closeApplyDetail = function () {
        //    $scope.modalTwo.hide();
        //};
        //// Cleanup the modal when we're done with it!
        //$scope.$on('$destroy', function () {
        //    $scope.modalTwo.remove();
        //});


    }
})();
