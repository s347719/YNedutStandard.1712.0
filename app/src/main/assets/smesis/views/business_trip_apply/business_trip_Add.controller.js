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
        .controller('businessTripAddController', businessTripAddController);
    businessTripAddController.$inject = ['$scope', '$ionicModal', '$http', '$ionicPopup', '$rootScope', '$ionicLoading', '$filter', '$timeout', '$location'];
    function businessTripAddController($scope, $ionicModal, $http, $ionicPopup, $rootScope, $ionicLoading, $filter, $timeout, $location) {

        //数据id
        $scope.id = $location.search().id;

        //流程状态
        $scope.processScheduleStates = $location.search().processScheduleStates;
        //流程实例Id
        $scope.actExecutionProcinstId = $location.search().actExecutionProcinstId;
        //申请人Id
        $scope.applyId = $location.search().applyId;
        //当前节点id
        $scope.currentTaskId = $location.search().currentTaskId;


        /**
         * 自定义时间样式
         * @param dataList
         */
        var entityToShowDataFun = function (dataList) {
            angular.forEach(dataList, function (data) {
                /**
                 * 重写申请时间样式
                 */
                if (data.beginTime.indexOf(" ") != -1) {

                    var applyTimeArr = data.beginTime.split(" "), applyDate = [];
                    if (applyTimeArr[0] && applyTimeArr[0].indexOf("-" != -1)) {
                        applyDate = applyTimeArr[0].split("-");
                        if (applyDate.length == 3) {

                            data.beginShowTime = applyDate[1] + "月" + applyDate[2] + "日";
                        }
                    }
                }
                /**
                 * 重写出差时间
                 */
                if (data.beginTime.indexOf("-") != -1) {
                    data.beginTimeStr = data.beginTime.replace(/-/g, "/");
                }
                if (data.endTime.indexOf("-") != -1) {
                    data.endTimeStr = data.endTime.replace(/-/g, "/");
                }
            });
        }
        /**
         * 初始化方法
         */
            //审批状态码表
        $scope.processScheduleList = ["保存草稿", "审批通过", "返回修改", "审批中"];
        $scope.addSecurityFlag = false;//出差申请流程 权限

        // 判断是否有出差申请流程 权限
        $http.post(basePath + "/third/leaveapply/checkAddSecurity?name=出差申请").success(function (data) {
            $scope.addSecurityFlag = data;
        });
        /**
         * 流程相关初始化
         * index 0 添加 1 编辑
         */
        $scope.initActive = function (index) {
            if (index == 0) {
                //根据流程Key获取流程id
                $scope.flowKey = {actKey: "YNBusinessTravel", Id: ""};
                $http.post(basePath + "/third/leaveapply/getXTBGActivityByActKeyForProcessDefinition?actKey=" + $scope.flowKey.actKey).success(function (data) {
                    if (data.status == 0) {
                        // 流程ID
                        $scope.editBusiness.processDefinitionId = data.result.id;

                    }
                });
                //获取申请单号
                $http.post(basePath + "/third/businessjournal/getXTBGMissionManageApplyNumber").success(function (data) {
                    $scope.editBusiness.applyNumber = data.result;
                });
            }


        }

        /**
         * 添加
         */
        if (!$scope.id) {
            $scope.editBusiness = {
                isAdvanceCost: false,
                beginTime: $filter('date')(new Date, "yyyy-MM-dd HH:mm")
            };
            /**
             * 获取流程定义
             */
            $scope.initActive(0);
        } else {
            $http.get(basePath + "/third/workflowmanage/findJSTXMissionMangeById?id="+$scope.id).success(function(data) {
                if(data.status=="0"){
                    $scope.editBusiness = data.result;
                    //根据实例id获取当前任务节点Id
                    $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProcessInstanceIdByEveryOneForCurrentTask?processInstanceId=" + $scope.actExecutionProcinstId).success(function (data) {
                        if (data.status == 0) {
                            //当前任务节点ID
                            $scope.editBusiness.currentTaskId = data.result;
                        }
                    });

                }
            });

            /**
             * 获取流程定义
             */
            $scope.initActive(1);
        }


        /**
         * 金额格式验证
         * @param cost
         * @param editBusiness
         */
        $scope.checkNumber = function (cost, editBusiness) {

            if (!/^\d*\.?\d{0,2}$/.test(cost) && cost) {
                editBusiness.cost = "";
                return $scope.notifyMsg("金额格式不正确！");
            }
        }
        /**
         * 日期操作(指定日期加上天数)
         * @param date 旧的日期
         * @param days 需要加上的天数
         * @returns {string} 新的日期
         * @constructor
         */
        var AddDays = function (date, days) {
            var nd = new Date(date);
            nd = nd.valueOf();
            nd = nd + days * 24 * 60 * 60 * 1000;
            nd = new Date(nd);
            var y = nd.getFullYear();
            var m = nd.getMonth() + 1;
            var d = nd.getDate();
            if (m <= 9) m = "0" + m;
            if (d <= 9) d = "0" + d;
            var cdate = y + "-" + m + "-" + d;
            return cdate;
        }
        /**
         * 回填日期方法
         * @param time 时间
         * @param status 1开始 2结束
         */
        $scope.timeSelect = function (time, status) {
            $timeout(function () {
                if (status) {
                    $scope.beginTime = {
                        minDate: new Date(time)
                    }
                } else {
                    $scope.endTime = {
                        minDate: new Date(time)
                    }
                }

            })
        };
        /**
         * 日期天数加1
         */
        $scope.addOneDay = function () {
            var startDate = $scope.editBusiness.beginTime;
            var str = AddDays(startDate.replace("-", "/").replace("-", "/"), 1);
            str = str + " " + $scope.editBusiness.beginTime.split(" ")[1];
            $scope.editBusiness.endTime = str;
        }
        /**
         * 人员选择器
         */
        $scope.selectUser = function (item) {
            // 下一节点人ID
            $scope.editBusiness.nextTaskUserId = item.id;
            // 下一节点人的名字
            $scope.editBusiness.approvalName = item.name;
        }
        /*取消提示弹框*/
        $scope.cancelTipsFun = function () {
            $ionicPopup.show({
                title: '取消提示',
                template: '<h5> 返回后，当前编辑内容将丢失，确定要返回吗？</h5>',
                buttons: [
                    {
                        text: '继续编辑',
                        type: 'button-outline button-positive button-local'
                    },
                    {
                        text: '确定返回',
                        type: 'button-positive button-local',
                        onTap: function () {
                            $scope.backHome();
                        }
                    }
                ]
            });
        };
        /**
         * 信息提示框
         * @param msg 需要显示的信息
         */
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }
        //节点和审批人对象
        $scope.nodeData ={};
        /**
         * 保存状态
         */
        var flag=true;
        $scope.submit = function () {
            if(!flag)return;
            /*********表单验证Sta*/
            // 请假类型
            if (!$scope.editBusiness.instruction) {
                $scope.notifyMsg("出差事由不能为空！");
                return;
            }
            // 请假天数
            if (!$scope.editBusiness.beginTime) {
                $scope.notifyMsg("出差时间(开始)不能为空！");
                return;
            }
            // 开始时间
            if (!$scope.editBusiness.endTime) {
                $scope.notifyMsg("出差时间(结束)不能为空！");
                return;
            }

            if ($scope.editBusiness.beginTime > $scope.editBusiness.endTime) {
                $scope.notifyMsg("请假开始时间不能大于结束时间！");
                return;
            }
            // 预支金额为真则 预支，否则不预支
            if ($scope.editBusiness.cost) {
                $scope.editBusiness.isAdvanceCost = true;
            }
            //判断是否有下一节点和审批人
            if($scope.nodeData.hasNode){
                if(!$scope.nodeData.nextUserTaskActId){
                    $scope.notifyMsg("请选择下一节点！");
                    return ;
                }else{
                    $scope.editBusiness.nextUserTaskActId = $scope.nodeData.nextUserTaskActId;
                }

            }
            if($scope.nodeData.hasUser){
                if($scope.nodeData.nextTaskUserId){
                    $scope.editBusiness.nextTaskUserId = $scope.nodeData.nextTaskUserId;
                }
            }
            /*********表单验证End*/

            // 发起流程    -------------------------------------  // 返回修改后同意
            var url = "updateXTBGMissionManageBySubmitProcess";
            //提交审批
            if ($scope.editBusiness.processScheduleStates != 2) {
                url = "saveOrUpdateXTBGMissionManageByDraftboxTurnProcess";
            }
            flag=false;
            $http.post(basePath + "/third/businessjournal/" + url, $scope.editBusiness).success(function (data) {
                flag=true;
                if (data.status == 0) {
                    $scope.notifyMsg("提交成功");
                    $scope.backHome();

                } else {
                    $scope.notifyMsg(data.message);
                    $timeout(function(){
                        $scope.backHome();
                    },500);
                }
            })
        }
        //删除流程
        $scope.deleteflow = function () {
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
                                    $scope.backHome();
                                    $scope.notifyMsg(data.message);
                                } else {
                                    $scope.notifyMsg(data.message);
                                }
                            })
                        }
                    }
                ]

            });
        }

        //节点和下一节点审批人配置
        $scope.nodeOptions = {
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNBusinessTravel",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            routingStatusType:"YNBusinessTravel",
            userTypeApp:1
        }

        //手机端流程组件初始化
        $scope.options ={
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNBusinessTravel",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            button: {
                //同意
                agree: $scope.submit,
                //交办
                assign: $scope.choiceUser,
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
                deleteProcess: $scope.deleteflow,
                //流程图
                chart: $scope.chart,
                //审批历史
                approveHistory: $scope.approve
            }
        }

        //返回发起申请主页面
        $scope.backHome = function () {
            $location.path("/flow_apply").search({id: null});
        }
    }
})();
