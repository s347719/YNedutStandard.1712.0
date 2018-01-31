(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('addApplyController', addApplyController);

    addApplyController.$inject = ['$scope', '$http', '$filter', '$ionicLoading',"$location","$ionicPopup","$timeout","$rootScope"];
    function addApplyController($scope, $http, $filter, $ionicLoading,$location,$ionicPopup,$timeout,$rootScope){
        /*****页面初始化Sta*/
            // 页面对象
        $scope.update = {};
        //下一节点和下一节点审批人对象
        $scope.nodeData ={};
        // 开始时间
        $scope.update.startDate = $filter('date')(new Date,"yyyy-MM-dd HH:mm");
        // 获取请假类型
        $http.post(basePath + "/third/leaveapply/getLeaveTypeIsEnabled").success(function(data){
            if(data.status == 0){
                $scope.leaveList = data.result;
            }
        })
        /*****页面初始化End*/

        //流程相关参数
        //数据id
        $scope.id = $location.search().id;
        //流程状态
        $scope.processScheduleStates = $location.search().processScheduleStates;
        //流程实例Id
        $scope.actExecutionProcinstId = $location.search().actExecutionProcinstId;
        //申请人Id
        $scope.applyId = $location.search().applyId;
        if($scope.actExecutionProcinstId){
            //根据实例id获取当前节点Id
            $http.post(basePath + "/third/businessjournal/getXTBGActivityByProInstdForCurTask?processInstanceId=" + $scope.actExecutionProcinstId).success(function (data) {
                if (data.status == 0) {
                    //当前节点ID
                    $scope.update.currentTaskId = data.result;
                }
            });
        }

        $scope.checkNuber = function (day , info) {
            if(!isNaN(day) && day){
                info.days = "";
                return getMessageFun("错误","请填写正确的请假天数");
            }
        }
        var isNaN = function (info) {
            var re = /^[0-9]+.?[0-9]{0,1}$/;
            return re.test(info);
        }

        var getMessageFun = function ( title, message) {
            $ionicPopup.show({
                title: title,
                template: '<h5> ' + message + '</h5>',
                buttons : [
                    {text : '确认', type : 'button-local button-positive'}
                ]
            });
        }

        /**
         * 请假类型选择回调函数
         * @param item
         */
        $scope.diySelect = function(item){
            // 页面显示名字
            $scope.leaveTypeName = item.name;
            // 请假类型id
            $scope.update.typeId = item.id;
        }

        /**
         * 流程相关初始化
         */
        $scope.initActive = function(){
            //根据流程Key获取流程id
            $scope.flowKey = {actKey: "YNLeave", Id: ""};
            $http.post(basePath + "/third/leaveapply/getXTBGActivityByActKeyForProcessDefinition?actKey=" + $scope.flowKey.actKey).success(function (data) {
                if (data.status == 0) {
                    // 流程ID
                    $scope.update.processDefinitionId = data.result.id;
                }
            })
        }

        /**
         * 日期操作(指定日期加上天数)
         * @param date 旧的日期
         * @param days 需要加上的天数
         * @returns {string} 新的日期
         * @constructor
         */
        var  AddDays = function(date,days){
            var nd = new Date(date);
            nd = nd.valueOf();
            nd = nd + days * 24 * 60 * 60 * 1000;
            nd = new Date(nd);
            //alert(nd.getFullYear() + "年" + (nd.getMonth() + 1) + "月" + nd.getDate() + "日");
            var y = nd.getFullYear();
            var m = nd.getMonth()+1;
            var d = nd.getDate();
            if(m <= 9) m = "0"+m;
            if(d <= 9) d = "0"+d;
            var cdate = y+"-"+m+"-"+d;
            return cdate;
        }

        /**
         * 日期天数加1
         */
        $scope.addOndeDay = function(){
            var startDate = $scope.update.startDate;
            var str = AddDays(startDate.replace("-","/").replace("-","/"),1);
            str = str+" "+$scope.update.startDate.split(" ")[1];
            $scope.update.endDate = str;
        }

        /*取消提示弹框*/
        $scope.cancelTips = function(){
            $ionicPopup.show({
                title : '取消提示',
                template : '<h5> 确定要取消当前编辑内容吗？</h5>',
                buttons : [
                    {
                        text : '继续编辑',
                        type : 'button-outline button-positive button-local'
                    },
                    {
                        text : '确定',
                        type : 'button-positive button-local',
                        onTap: function () {
                            $scope.backHome();
                        }
                    }
                ]
            });
        };


        // 判断是否修改操作
        if($scope.id){
            //根据数据id查询数据
            $http.post(basePath + "/third/leaveapply/findJSTXLeaveManageById?id="+$scope.id).success(function (data) {
                if (data.status == "0") {
                    $scope.update = data.result;
                    //根据实例id获取当前任务节点Id
                    $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProcessInstanceIdByEveryOneForCurrentTask?processInstanceId=" + $scope.actExecutionProcinstId).success(function (data) {
                        if (data.status == 0) {
                            //当前任务节点ID
                            $scope.update.currentTaskId = data.result;
                        }
                    });
                    $scope.leaveTypeName =  $scope.update.type;
                    // 请假类型id
                    $scope.update.typeId =  $scope.update.typeId;
                } else {
                    $scope.notifyMsg("获取数据失败!");
                }
            })
        };


        /**
         * 保存状态
         */
        $scope.assign = function(){
            if($scope.update.startDate>$scope.update.endDate){
                $scope.notifyMsg("请假开始时间不能大于结束时间！");
                return ;
            }
            // 备注
            if(!$scope.update.instruction){
                $scope.notifyMsg("请假原因不能为空！");
                return ;
            }
            /*********表单验证Sta*/
            // 请假类型
            if(!$scope.update.typeId){
                $scope.notifyMsg("请假类型不能为空！");
                return ;
            }
            // 请假天数
            if(!$scope.update.days){
                $scope.notifyMsg("请假天数不能为空！");
                return ;
            }
            // 开始时间
            if(!$scope.update.startDate){
                $scope.notifyMsg("开始时间不能为空！");
                return ;
            }
            // 结束时间
            if(!$scope.update.endDate){
                $scope.notifyMsg("结束时间不能为空！");
                return ;
            }

            //判断是否有下一节点和审批人
            if($scope.nodeData.hasNode){
                if(!$scope.nodeData.nextUserTaskActId){
                    $scope.notifyMsg("请选择下一节点！");
                    return ;
                }else{
                    $scope.update.nextUserTaskActId = $scope.nodeData.nextUserTaskActId;
                }

            }
            if($scope.nodeData.hasUser){
                if($scope.nodeData.nextTaskUserId){
                    $scope.update.nextTaskUserId = $scope.nodeData.nextTaskUserId;
                }
            }
            /*********表单验证End*/
                // 处理多余的字段，防止springmvc 注入对象参数值出错
            delete $scope.update.$$hashKey;
            delete $scope.update.platformSysUserId;
            delete $scope.update.access_token;
            // 发起流程    -------------------------------------  // 返回修改后同意
            var url =  $scope.update.processScheduleStates != 2 ? "saveXTBGLeaveManageByDraftboxTurnProcessMobile" : "agree";
            $http.post(basePath + "/third/leaveapply/"+url,$scope.update).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("提交成功");
                    $timeout(function(){
                        $scope.backHome();
                    },500);
                }else{
                    $scope.notifyMsg(data.message);
                    $timeout(function(){
                        $scope.backHome();
                    },500);
                }
            })

        }

        /**
         * 人员选择器
         */
        $scope.selectUser = function(item){
            // 下一节点人ID
            $scope.update.nextTaskUserId = item.id;
            // 下一节点人的名字
            $scope.update.approvalName = item.name;
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

        /**
         * 信息提示框
         * @param msg 需要显示的信息
         */
        $scope.notifyMsg = function(msg){
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }


        //延迟加载数据
        $timeout(function () {
            // 初始化日期加1天
            $scope.addOndeDay();
        }, 300);

        // 流程初始化
        $scope.initActive();
        //返回发起申请主页面
        $scope.backHome = function () {
            yn.plugin.yncordova.close();
            // $location.path("/flow_apply").search({id:null});
        }
        //节点和下一节点审批人配置
        $scope.nodeOptions = {
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNLeave",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            routingStatusType:"YNLeave",
            userTypeApp:1
        }

        //手机端流程组件初始化
        $scope.options ={
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNLeave",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            button: {
                //同意
                agree: $scope.assign,
                //交办
                assign: $scope.choiceUser,
                //催办
                urge: true,
                //驳回发起人
                reject: $scope.returnApply,
                //驳回上一步
                rejectPreNode: $scope.returnLastStep,
                //提交审批
                startProcess: $scope.assign,
                //保存草稿
                saveDraft: $scope.save,
                //撤销
                repeal: $scope.revert,
                //删除草稿
                deleteDraft: $scope.delete,
                //删除流程
                deleteProcess: $scope.delete,
                //流程图
                chart: $scope.chart,
                //审批历史
                approveHistory: $scope.approve
            }
        }
    }

})();
