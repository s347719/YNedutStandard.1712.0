(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('outworkApplyAddController', outworkApplyAddController);
    outworkApplyAddController.$inject = ['$scope','$http','$ionicLoading',"$location","$ionicPopup","$timeout","$rootScope","$filter"];
    function outworkApplyAddController($scope,$http, $ionicLoading,$location,$ionicPopup,$timeout,$rootScope,$filter){
        /*****页面初始化Sta*/

        // 页面对象
        $scope.update = {};

        //下一节点和下一节点审批人对象
        $scope.nodeData ={};

        //流程状态
        $scope.processScheduleStates = $location.search().processScheduleStates;
        //流程实例Id
        $scope.actExecutionProcinstId = $location.search().actExecutionProcinstId;
        //申请人Id
        $scope.applyId = $location.search().applyId;

        // 判断是否修改操作
        if($location.search().id){
            $http.get(basePath + "/third/jstxOutSideManageResource/findXTBGOutSideManageById?id="+$location.search().id).success(function(data) {
                $scope.update = data.result;
            });
        }else{
            // 初始化开始时间
            $scope.update.beginTime = $filter('date')(new Date,"yyyy-MM-dd HH:mm");
        }

        if($location.search().id){
            /**
             * 时间下拉值自动绑定
             * @param time 时间
             */
            $scope.goBackTime = function (time) {
                $timeout(function () {
                    return new Date(time);
                })
            };
        }


        /*****页面初始化End*/

        /**
         * 流程相关初始化
         */
        $scope.initActive = function() {
            //根据流程Key获取流程id
            $scope.flowKey = {actKey: "YNFieldPersonnel", Id: ""};
            $http.post(basePath + "/third/leaveapply/getXTBGActivityByActKeyForProcessDefinition?actKey=" + $scope.flowKey.actKey).success(function (data) {
                if (data.status == 0) {
                    // 流程ID
                    $scope.update.processDefinitionId = data.result.id;
                }
            })
        }


        /*取消提示弹框*/
        $scope.cancelTips = function(){
            $ionicPopup.show({
                title : '取消提示',
                template : '<h5> 返回后，当前编辑内容将丢失，确定要返回吗？</h5>',
                buttons : [
                    {
                        text : '继续编辑',
                        type : 'button-outline button-positive button-local'
                    },
                    {
                        text : '确定返回',
                        type : 'button-positive button-local',
                        onTap: function () {
                            //$location.path('/outwork_apply').search({});
                            $scope.backHome();
                        }
                    }
                ]
            });
        };
        var num = 0;
        /*********表单验证Start*/
        $scope.assign = function(){
            if($scope.update.beginTime>$scope.update.endTime){
                $scope.notifyMsg("开始时间不能大于结束时间！");
                return ;
            }

            // 外勤事由
            if(!$scope.update.instruction){
                $scope.notifyMsg("外勤事由不能为空！");
                return ;
            }
            // 开始时间
            if(!$scope.update.beginTime){
                $scope.notifyMsg("开始时间不能为空！");
                return ;
            }
            // 结束时间
            if(!$scope.update.endTime){
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
            // 处理多余的字段，防止springMvc 注入对象参数值出错
            delete $scope.update.$$hashKey;
            delete $scope.update.platformSysUserId;
            delete $scope.update.access_token;
            // 发起流程    -------------------------------------  // 被驳回--第一次添加
            var url = "";
            // var url =  $scope.update.processScheduleStates != 2? "saveOrUpdateXTBGOutSideManageByDraftboxTurnProcess" : "agree";
            if($scope.update.processScheduleStates ==2) {
                url = "agree";
            }
            else{
                url = "saveOrUpdateXTBGOutSideManageByDraftboxTurnProcess";
            }

            num ++;
            if(num <=1){
                $http.post(basePath + "/third/jstxOutSideManageResource/"+url,$scope.update).success(function(data){
                    if(data.status==0){
                        $scope.notifyMsg("提交成功");
                        num = 0;
                        $timeout(function(){
                            //$location.path("/outwork_apply").search({});
                            $scope.backHome();
                        },500);
                    }else{
                        $scope.notifyMsg(data.message);
                        $timeout(function(){
                            //$location.path("/outwork_apply").search({});
                            $scope.backHome();
                            num = 0;
                        },500);
                        num --;
                    }
                })
            }else {
                num--;
            }
        }

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
                            $http.post(basePath + "/third/jstxOutSideManageResource/deleteXTBGOutSideManageByIdAndProcessInstanceId?id="+$location.search().id+"&processInstanceId="+$scope.actExecutionProcinstId).success(function (data) {
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
        };

        /**
         * 人员选择器
         */
        $scope.selectUser = function(item){
            // 下一节点人ID
            $scope.update.nextTaskUserId = item.id;
            // 下一节点人的名字
            $scope.update.approvalName = item.name;
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
        };

        // 流程初始化
        $scope.initActive();

        //节点和下一节点审批人配置
        $scope.nodeOptions = {
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNFieldPersonnel",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            routingStatusType:"YNFieldPersonnel",
            userTypeApp:1
        }

        //手机端流程组件初始化
        $scope.options ={
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNFieldPersonnel",
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
                deleteProcess: $scope.deleteflow,
                //流程图
                chart: $scope.chart,
                //审批历史
                approveHistory: $scope.approve
            }
        }

        //返回发起申请主页面
        $scope.backHome = function () {
            $location.path("/flow_apply").search({id:null});
        }

    }
})();
