(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('excessivCostApplyCtrl', excessivCostApplyCtrl);
    excessivCostApplyCtrl.$inject = ['$scope', '$ionicModal', '$http', '$location', '$ionicPopup', '$rootScope', '$ionicLoading', '$filter', '$timeout', '$ionicPlatform'];
    function excessivCostApplyCtrl($scope, $ionicModal, $http, $location, $ionicPopup, $rootScope, $ionicLoading, $filter, $timeout, $ionicPlatform) {

        //格式化 时间方法
        Date.prototype.Format = function (fmt) { //author: meizz
            var o = {
                "M+": this.getMonth() + 1, //月份
                "d+": this.getDate(), //日
                "h+": this.getHours(), //小时
                "m+": this.getMinutes(), //分
                "s+": this.getSeconds(), //秒
                "q+": Math.floor((this.getMonth() + 3) / 3), //季度
                "S": this.getMilliseconds() //毫秒
            };
            if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        };

        /** -----------------业务 数据对象------**/
        $scope.excessivCost = {
            costRsglSysUser: {},     //费用发生人
            approvalName: "",        //费用发生人名称  -----页面展示
            costOccurDate: new Date().Format('yyyy-MM-dd'),       //费用发生日期
            excessiveAmount: '',      //超标金额
            overproofReason: ""      //超标原因
        };
        $scope.nodeData = {};    //选择按钮
        $scope.showModel;
        /** -----------------流程相关参数 start------------------------------------------------**/
        //数据id
        $scope.id = $location.search().id;
        //流程状态
        $scope.processScheduleStates = $location.search().processScheduleStates;
        //按钮状态
        var type = $location.search().type;
        if(type!='edit'){$scope.type = type;}   //区分 是否是点击修改按钮进入的页面
        //流程实例Id
        $scope.actExecutionProcinstId = $location.search().actExecutionProcinstId;
        $scope.excessivCost.processDefinitionId = $location.search().processDefinitionId;
        //申请人Id
        $scope.applyId = $location.search().applyId;
        if($scope.actExecutionProcinstId){
            //根据实例id获取当前节点Id
            $http.post(basePath + "/third/businessjournal/getXTBGActivityByProInstdForCurTask?processInstanceId=" + $scope.actExecutionProcinstId).success(function (data) {
                if (data.status == 0) {
                    //当前节点ID
                    $scope.excessivCost.currentTaskId = data.result;
                }
            });
        };

        //判断 展示页面 方法
        var showHtml = function(type){
            var html = type=='view'?'excessiv-view.html':'excessiv-apply.html';
            $ionicModal.fromTemplateUrl(html,{
                scope:$scope,
                animation:'slide-in-right'
            }).then(function(modal){
                $scope.showModel = modal;
                $scope.showModel.show();
            });
        };

        //当我们需要 关闭模态框时 注 主要是物理返回时 使用此方法进行处理，运行此方法
        $ionicPlatform.onHardwareBackButton(function(){
            $scope.backHome();
        });

        //点击编辑时 展示编辑页面 关闭查看页面
        $scope.editData = function(){
            $scope.showModel.remove();
            showHtml("apply");
        };

        /** -----------------流程相关参数 end------------------------------------------------**/

        /**
         * 金额格式验证
         * @param cost
         * @param editBusiness
         */
        $scope.checkNumber = function (cost, index) {
            if (!/^\d{1,13}\.?\d{0,2}$/.test(cost[index])) {
                cost[index] = '';
                return $scope.notifyMsg("金额格式不正确，且最多只能输入两位小数！");
            }
        };
        /**
         * 信息提示框
         * @param msg 需要显示的信息
         */
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg,
                duration: 2000
            });
        };
        //返回发起申请主页面
        $scope.backHome = function () {
            $scope.showModel.remove();   //先关闭拟态框 然后跳转回首页
            $location.path($scope.type?"/flow_approve":"/flow_apply").search({id:null});
        };
        //流程提交后 提示信息
        $scope.htmlMessage = function(data){
            $scope.notifyMsg(data.message);
            if(data.status==0){
                $timeout(function(){
                    $scope.backHome();
                },2000);
            }else{
                $timeout(function(){
                    $scope.backHome();
                },500);
            }
        };
        /* 费用发生人 选择器*/
        $scope.selectUser = function(data){
            $scope.excessivCost.costRsglSysUser = data;
            $scope.excessivCost.approvalName = data.name;
            $scope.excessivCost.approvalNameAndJobNumber = data.name + "(" + (data.orgName || data.mainOrganizatName) +")";
            $scope.excessivCost.costRsglSysUserDTOId = data.id;
        };
        //验证数据是否完整
        var checkDataFun = function(data){
            if(!data.overproofReason){
                $scope.notifyMsg("超标原因不能为空！"); return false;
            }
            if(!data.excessiveAmount){
                $scope.notifyMsg("超标金额不能为空！"); return false;
            }
            if(!data.approvalName){
                $scope.notifyMsg("费用发生人不能为空！"); return false;
            }
            if(!data.costOccurDate){
                $scope.notifyMsg("费用发生日期不能为空！"); return false;
            }
            return true;
        }

        //催办流程
        $scope.urgeData = function (item) {
            $http.get(basePath + "/third/xtbgactivity/sendActivityRemindersMessage", {
                params: {taskId: item.currentTaskId}
            }).success(function (data) {$scope.notifyMsg(data.message);});
        }
        //同意
        $scope.assign = function(data){
            if(!checkDataFun($scope.excessivCost)){
                return false;
            }
            if($scope.type && $scope.type=='approve'){
                $scope.excessivCost.nextUserTaskActId = data.nextUserTaskActId;
                $scope.excessivCost.nextTaskUserId = data.nextTaskUserId;
                $scope.excessivCost.message = data.message;
            }else{
                $scope.excessivCost.nextTaskUserId = $scope.nodeData.nextTaskUserId;
                if($scope.nodeData.hasNode){
                    if($scope.nodeData.nextUserTaskActId){
                        $scope.excessivCost.nextUserTaskActId = $scope.nodeData.nextUserTaskActId;
                    }else{
                        $scope.notifyMsg("请选择下一级节点！"); return false;
                    }
                }
            }
            httpVisit("saveXTBGStandardCostApplyByDraftboxTurnProcess",null, $scope.excessivCost, function(val){
                $scope.htmlMessage(val);
            });
        };

        //
        //交办
        $scope.choiceUser = function(data){
            $scope.excessivCost.assignedUserId = data.nextTaskUserId;
            $scope.excessivCost.nextUserTaskActId = data.nextUserTaskActId;
            $scope.excessivCost.message = data.message;
            httpVisit("updateXTBGStandardCostApplyByTransferAssignee",null, $scope.excessivCost, function(val){
                $scope.htmlMessage(val);
            });
        };
        //驳回发起人
        $scope.returnApply = function(data){
            $scope.excessivCost.processState = 4;
            $scope.excessivCost.assignedUserId = data.nextTaskUserId;
            $scope.excessivCost.nextUserTaskActId = data.nextUserTaskActId;
            $scope.excessivCost.message = data.message;
            httpVisit("backXTBGStandardCostApplyByStartUser",null, $scope.excessivCost, function(val){
                $scope.htmlMessage(val);
            });
        };
        //驳回上一步
        $scope.returnLastStep = function(data){
            $scope.excessivCost.assignedUserId = data.nextTaskUserId;
            $scope.excessivCost.nextUserTaskActId = data.nextUserTaskActId;
            $scope.excessivCost.message = data.message;
            httpVisit("backXTBGStandardCostApplyByPreNode",null, $scope.excessivCost, function(val){
                $scope.htmlMessage(val);
            });
        };
        /*删除和撤销 弹出窗确认信息*/
        var revertOrdeleteFun = function(mess,mes,fun){
           $ionicPopup.confirm({
                title: '提示',
                template: '<h5 class="margin-bottom-0">' + mess + '</h5>',
                buttons: [
                    {
                        text: '取消',
                        type: 'button-outline button-theme button-local'
                    },
                    {
                        text: mes,
                        type: ' button-theme button-local',
                        onTap: function (e) {
                            fun && fun();
                        }
                    }
                ]

            });
        }
        //撤销
        $scope.revert = function(data){
            $scope.excessivCost.processState = 7;
            revertOrdeleteFun("确定要撤销吗？","确定撤销",function(){
                httpVisit("backXTBGStandardCostApplyByStartUser",null, $scope.excessivCost, function(val){
                    $scope.htmlMessage(val);
                });
            })
        };
        //删除流程
        $scope.deleteflow = function(data){
            revertOrdeleteFun("确定要删除吗？","确定删除",function(){
                httpVisit("deleteXTBGStandardCostApplyByIdAndProcessInstanceId",{id: $scope.excessivCost.id, processInstanceId: $scope.actExecutionProcinstId}, null, function(val){
                    $scope.htmlMessage(val);
                });
            });
        };
        //节点和下一节点审批人配置
        $scope.nodeOptions = {
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNExcessivCost",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            routingStatusType:"YNExcessivCost",
            userTypeApp:1
        };
        //流程配置
        $scope.optionsView = {
            actExecutionProcinstId: $scope.actExecutionProcinstId,
            processScheduleStates: $scope.processScheduleStates
        }
        //手机端流程组件初始化
        $scope.options ={
            instanceId: $scope.actExecutionProcinstId,
            definitionKey: "YNExcessivCost",
            //申请人Id
            applyId: $scope.applyId,
            //流程状态
            processStatus: $scope.processScheduleStates,
            button: {
                //同意
                agree: $scope.assign || angular.noop,
                //交办
                assign: $scope.choiceUser || angular.noop,
                //催办
                urge: true,
                //驳回发起人
                reject: $scope.returnApply || angular.noop,
                //驳回上一步
                rejectPreNode: $scope.returnLastStep || angular.noop,
                //提交审批
                startProcess: $scope.assign || angular.noop,
                //保存草稿
                saveDraft: $scope.save || angular.noop,
                //撤销
                repeal: $scope.revert || angular.noop,
                //删除草稿
                deleteDraft: $scope.delete || angular.noop,
                //删除流程
                deleteProcess: $scope.deleteflow || angular.noop,
                //流程图
                chart: $scope.chart,
                //审批历史
                approveHistory: $scope.approve,
                cancel: function(){
                    $scope.backHome();
                }
            },
            routingStatusType:"YNExcessivCost",
            nextNodeApprover:5,//下一节点审批人
            assignedPerson:6//交办人
        }

        /**
         *  访问后台方法
         * @param url  路径
         * @param params get数据
         * @param data post 数据
         * @param fun 访问成功 方法
         * @param errFun 访问失败方法
         */
        var httpVisit = function (url,params,data,fun,errFun){
            params = params || angular.noop;
            data = data || angular.noop;
            fun = fun || angular.noop;
            errFun = errFun || angular.noop;
            var config= {
                url: basePath + "/third/excessivCostApply/" + url,
                method:"post",
                params:params, //后台用 get接收
                data: data //后台用@RequestBody 接收
            };
            $http(config).success(function(resultData){
                fun && fun(resultData);
            }).error(function (resultData) {
                $scope.isError = true;
                errFun && errFun(resultData);
            });
        };


        //初始状态 判断id 如果有id 就查询数据
        if($scope.id){
            showHtml('edit' == type?"apply":"view");  //展示页面
            httpVisit("findXTBGStandardCostApplyById", "", $scope.id, function(data){
                $scope.excessivCost = angular.copy(data);
                $scope.selectUser(data.costRsglSysUserDTO);
            });
        }else{
            //获取 申请单号信息
            showHtml("apply");  //展示页面
            httpVisit("getApplyNumber", "", "", function(data){
                $scope.excessivCost.applyNumber = data.applyNumber;
                $scope.excessivCost.costOccurDate = data.costOccurDate;
                var userDTO = data.costRsglSysUserDTO;
                $scope.excessivCost.costRsglSysUser = userDTO;
                $scope.excessivCost.approvalName =userDTO.name;
                $scope.excessivCost.approvalNameAndJobNumber = userDTO.name + "(" + userDTO.mainOrganizatName +")";
                $scope.excessivCost.costRsglSysUserDTOId = userDTO.id;
            });
        }
    }
})();
