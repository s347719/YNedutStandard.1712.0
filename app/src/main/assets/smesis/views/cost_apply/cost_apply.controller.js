
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('costApplyCtrl',costApplyCtrl);

    costApplyCtrl.$inject = ['$scope', '$http', '$filter', '$ionicLoading',"$location","$ionicPopup","$timeout","$rootScope","$ionicModal"];

    function costApplyCtrl($scope, $http, $filter, $ionicLoading,$location,$ionicPopup,$timeout,$rootScope,$ionicModal) {
        //数据id
        $scope.id = $location.search().id;
        //数据对象
        $scope.applyData ={
            jyhsMyExpensesInfoDTOList:[]
        };
        //费用明细对象
        $scope.detail ={};

        //最大下标
        $scope.index =0;

        //是否显示更多
        $scope.showMore = false;
        $scope.showDataList =true;
        //页面显示明细
        $scope.detailList =[];
        $scope.countMoney =0;

        //获取当前人的上级
        $scope.getLeader = function () {
            $http.post(basePath + "/third/feeCostApply/findUserLeader").success(function (data) {
                if(data.status=='0'){
                    $scope.applyData.approvalUserId = data.result.id;
                    $scope.applyData.approvalUserName = data.result.name;
                    if($scope.applyData.approvalUserName ==null){
                        $scope.applyData.approvalUserName ="无";
                    }
                }
            });
        }

        //收费显示删除按钮
        $scope.showIsDel =false;
        if($scope.id){
            $http.post(basePath + "/third/feeCostApply/findFeeCostApplyDataById?id="+$scope.id).success(function (data) {
                if(data.status=='0'){
                    $scope.applyData= data.result;
                    $scope.applyData.feeTypeName = $scope.applyData.feeTypeName;
                    $scope.getLeader();
                    angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
                        $scope.countMoney += parseFloat(newData.applyMoney?newData.applyMoney:0);
                    })
                    var length = $scope.applyData.jyhsMyExpensesInfoDTOList.length;
                    var newList = angular.copy($scope.applyData.jyhsMyExpensesInfoDTOList);
                    //默认展示三项
                    if(length >3){
                        //显示更多
                        $scope.showMore = true;
                        $scope.detailList = newList.splice(length-3,length);
                    }else{
                        $scope.detailList = $scope.applyData.jyhsMyExpensesInfoDTOList;
                    }
                }
            });
        }else{
            $http.post(basePath + "/third/feeCostApply/findUserInfoData").success(function (data) {
                if(data.status=='0'){
                    $scope.applyData.applicationUserName = data.result.applicationUserName;
                    $scope.applyData.applyDate = data.result.applyDate;
                    $scope.applyData.applicationUserId = data.result.applicationUserId;
                    $scope.applyData.platformSysOrganizationId = data.result.platformSysOrganizationId;
                    $scope.getLeader();
                }
            });
        }



        //查询费用类别
        $http.post(basePath + "/third/feeCostApply/findFeeTypeListByKey").success(function (data) {
            $scope.feeTypeList = data;
            var newList = [];
            angular.forEach($scope.feeTypeList, function (newData) {
                if(newData.dictCode !="02"){
                    newList.push(newData);
                }
            })
            $scope.feeTypeList = newList;
        });



        /*选择费用申请类别 模态*/
        $ionicModal.fromTemplateUrl('select-cost-apply.html',{
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function(modal){
            $scope.modalOne = modal;
        });
        $scope.showCostApply = function(){
            $scope.modalOne.show();
        };
        $scope.closeCostApply = function(){
            $scope.modalOne.hide();
        };
        /*增加费用项 模态*/
        $ionicModal.fromTemplateUrl('add-cost.html',{
            scope: $scope,
            animation: 'slide-in-left'
        }).then(function(modal){
            $scope.modalOne1 = modal;
        });
        $scope.showAddCost = function(){
            $scope.detail ={};
            $scope.index = $scope.getMaxNumber();
            $scope.modalOne1.show();
        };
        $scope.closeAddCost = function(){
            $scope.modalOne1.hide();
        };
        //编辑费用项
        $ionicModal.fromTemplateUrl('edit-cost.html',{
            scope: $scope,
            animation: 'slide-in-left'
        }).then(function(modal){
            $scope.modalEdit = modal;
        });
        $scope.showEditCost = function(){
            $scope.modalEdit.show();
        };
        $scope.closeEditCost = function(){
            $scope.modalEdit.hide();
        };
        /*提示信息*/
        /*费用项 说明*/
        $scope.showCostWarning = function(){
            var confirmPopup = $ionicPopup.confirm({
                title : '帮助',
                template : '<p class="text-left">例如填写费用类别：差旅交通费、投标履约保证金、客户招待费等；若是采购资产或是领用礼品等，可直接填入资产名称或礼品名称</p>',
                buttons : [
                    {
                        text : '确定',
                        type : ' button-theme button-local',
                        onTap: function (e) {

                        }
                    }
                ]

            });
        };
        /*费用详细说明 提示*/
        $scope.showCostMsgWarning = function(){
            var confirmPopup = $ionicPopup.confirm({
                title : '帮助',
                template : '<p class="text-left">如投标费用说明转账方式转账要求；资产采购说明采购数量、型号；领取礼品写明礼品赠送客户的单位、接受人；客户接待写明来访客户单位名称、最高领导；出差写明出差地点及事由等等</p>',
                buttons : [
                    {
                        text : '确定',
                        type : ' button-theme button-local',
                        onTap: function (e) {

                        }
                    }
                ]

            });
        };

        //编辑费用项的时候赋值给原来的值
        $scope.editDetail = function (item) {
            var re = /^[0-9]+.?[0-9]{0,2}$/;
            if(!re.test(item.applyMoney)){
                $scope.notifyMsg("申请金额必须为数字，且最多两位小数!");
                return false;
            }
            $scope.countMoney =0;
            angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
                if(newData.index==item.index){
                    newData.applyMoney = item.applyMoney;
                    newData.column = item.column;
                    newData.describe = item.describe;
                }
                $scope.countMoney += parseFloat(newData.applyMoney?newData.applyMoney:0);
            })
           angular.forEach($scope.detailList, function (newData) {
               if(newData.index==item.index){
                   newData.applyMoney = item.applyMoney;
                   newData.column = item.column;
                   newData.describe = item.describe;
               }
           })
            $scope.closeEditCost();
        }

        //字符截取
        $scope.getNewText = function (item) {
            var text="";
            if(item && item.length >10){
                text = item.substr(0,10)+"..."
            }else{
                text = item;
            }
            return text;
        }

        //数据处理
        $scope.onSelect = function(item){
            // 页面显示名字
            $scope.applyData.feeTypeName = item.dictName;
            // 请假类型id
            $scope.applyData.feeTypeCode = item.dictCode;
            $scope.closeCostApply();
        }

        //选择人员
        $scope.applyUser = function (item) {
            $scope.applyData.applicationUserName = item.name+"("+item.jobNumber+")";
            $scope.applyData.applicationUserId = item.id;
        }

        //显示帮助
        $scope.showHelp = function () {
            var confirmPopup = $ionicPopup.confirm({
                title : '帮助',
                template : '<p class="text-left">1、根据学校制度规定，申请人可通过“提交审批”提交到下一级审批人审批。</p>' +
                '<p class="text-left">2、审批结束后费用方可发生，执行时费用应控制在“核准金额”范围内，避免超支。</p>',
                buttons : [
                    {
                        text : '确定',
                        type : ' button-theme button-local',
                        onTap: function (e) {

                        }
                    }
                ]

            });
        }

        //返回
        $scope.back =function(){
            $location.path("/flow_apply").search({});
        }

        //明细确定
        $scope.sureDetail = function () {
            //判断费用项是否填写完整
            if(!$scope.detail.column){
                $scope.notifyMsg("费用项名称不能为空!")
                return false;
            }
            if($scope.detail.applyMoney ==null){
                $scope.notifyMsg("申请金额不能为空!")
                return false;
            }
            var re = /^[0-9]+.?[0-9]{0,2}$/;
            if(!re.test($scope.detail.applyMoney)){
              $scope.notifyMsg("申请金额必须为数字，且最多两位小数!");
                return false;
            }
            $scope.countMoney =0;
            $scope.index = $scope.getMaxNumber();
            $scope.detail.index =$scope.index;
            //判断费用项是否已经存在
            var flag = true;
            if($scope.applyData.jyhsMyExpensesInfoDTOList !=null &&$scope.applyData.jyhsMyExpensesInfoDTOList.length >0){
                angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
                    if(newData.column ==$scope.detail.column){
                        flag = false;
                    }
                })
            }
            if(!flag){
                $scope.notifyMsg("费用项名称已经存在!")
                return false;
            }
            $scope.applyData.jyhsMyExpensesInfoDTOList.push($scope.detail);
            var newList = angular.copy($scope.applyData.jyhsMyExpensesInfoDTOList);
            //默认展示三项
            if($scope.index >3){
                //显示更多
                $scope.showMore = true;
                var length = newList.length;
                $scope.detailList = newList.splice(length-3,length);
            }else{
                $scope.detailList =angular.copy(newList);

            }
           angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
               $scope.countMoney += parseFloat(newData.applyMoney);
           })
            $scope.closeAddCost();
        }

        //显示更多
        $scope.clickMore = function () {
            $scope.showMoreData = true;
            $scope.showMore = false;
            $scope.showDataList = false;

        }

        //赋值给明细
        $scope.setDetail = function (item,type) {
            if(type){
                $scope.showIsDel = true;
            }
            $scope.showEditCost();
            $scope.detail = angular.copy(item);
            $scope.index = item.index;
        }
        //删除明细
        $scope.delDetail = function (item) {
            var newList =[];
            if($scope.applyData.jyhsMyExpensesInfoDTOList != null && $scope.applyData.jyhsMyExpensesInfoDTOList.length ==1){
                $scope.notifyMsg("一条费用项不允许删除!")
                return;
            }else{
                var index =1;
                angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
                    if(newData.index !=item.index){
                        newData.index=index;
                        newList.push(newData);
                        index ++;
                    }
                })
                $scope.countMoney =0;
                $scope.applyData.jyhsMyExpensesInfoDTOList=[];
                $scope.applyData.jyhsMyExpensesInfoDTOList = newList;
                if(newList.length <=3){
                    $scope.showMore = false;
                }
                $scope.detailList = newList;
                angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
                    $scope.countMoney += parseFloat(newData.applyMoney?newData.applyMoney:0);
                })
                $scope.closeEditCost();
            }


        }

        //提交审批
        $scope.approve = function(){
            //验证数据必填项
            if($scope.applyData.jyhsMyExpensesInfoDTOList != null && $scope.applyData.jyhsMyExpensesInfoDTOList.length ==0){
                $scope.notifyMsg("请至少添加一条费用项!")
                return;
            }
            if(!$scope.applyData.feeTypeCode){
                $scope.notifyMsg("费用类别不能为空!")
                return;
            }
            if(!$scope.applyData.applyDate){
                $scope.notifyMsg("费用申请时间不能为空!")
                return;
            }
            if(!$scope.applyData.expensesUse){
                $scope.notifyMsg("费用用途不能为空!")
                return;
            }
            if($scope.applyData.expendTimeStart != null &&null !=$scope.applyData.expendTimeEnd){
                if($scope.applyData.expendTimeStart > $scope.applyData.expendTimeEnd){
                    $scope.notifyMsg("预计支出开始时间不能大于结束时间！");
                    return false;
                }
            }

            $scope.approveModal.show();

        }
        //提交审批
        $scope.approveDataOk = function () {
            if(!$scope.applyData.approvalUserId){
                $scope.notifyMsg("请选择审批人!")
                return;
            }
            $scope.closeApproveModal();
            $http.post(basePath + "/third/feeCostApply/saveOrUpdateFeeCostData",$scope.applyData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("提交成功!");
                    $timeout(function(){
                        $scope.back();
                    },500);
                }else{
                    $scope.notifyMsg(data.message);
                    $timeout(function(){
                        $scope.back();
                    },500);
                }
            })
        }

        //审批的时候 选择审批人模板
        var approveTemplate = ' <ion-modal-view class="auto bar-up-shadow">' +
            '<div class="bottom-50">' +
            '<div class="padding">' +
            '<div class="approve-flow-wrap flow-title"> ' +
            '<div class="title more">请选择审批人</div>' +
            '<div class="pos-right-text">'+
            '<input type="text" placeholder="请选择" class="text-left" user-selector onclick="approveUser" inServiceStates = "1" title="审批人" routingStatusType="YNCostApply" userTypeApp="1" ng-model="applyData.approvalUserName"  on-select="selectUser(item)"  readonly>' +
            '<a class="text-right right-text" href="" style="text-decoration:none" ng-if="applyData.approvalUserName" ng-click="reSelectUser(item)">修改<a>' +
            '<a class="text-right right-text" href="" style="text-decoration:none" ng-if="!applyData.approvalUserName" ng-click="reSelectUser(item)">选择<a>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '<ion-footer-bar>' +
            '<div class="row">' +
            '<div class="col text-center btn-group-wrap">' +
            '<button class="button button-theme button-local button-outline" ng-click="closeApproveModal()">取消</button>' +
            '<button class="button button-theme button-local" ng-click="approveDataOk()">确定</button>' +
            '</div>' +
            '</div>' +
            '</ion-footer-bar>' +
            '</ion-modal-view>';

        $scope.approveModal = $ionicModal.fromTemplate(approveTemplate, {
            scope: $scope,
            animation: 'slide-in-up'
        });

        //关闭审批人模态框
        $scope.closeApproveModal = function () {
            $scope.approveModal.hide();
        }

        //选择审批人
        $scope.selectUser = function (item) {
            $scope.applyData.approvalUserName = item.name;
            $scope.applyData.approvalUserId = item.id;
        };
        //重新选择人员
        $scope.reSelectUser = function (item) {
            $scope.$broadcast("approveUser",item);
            if(item){
                $scope.feeApplyData.approvalUserName = item.name;
                $scope.feeApplyData.approvalUserId = item.id;
            }

        };


        //获取最大的项的
        $scope.getMaxNumber = function () {
            var max =0;
            if($scope.applyData.jyhsMyExpensesInfoDTOList != null && $scope.applyData.jyhsMyExpensesInfoDTOList.length >0){
                angular.forEach($scope.applyData.jyhsMyExpensesInfoDTOList, function (newData) {
                    if(parseInt(newData.index) >= parseInt(max)){
                        max =  parseInt(newData.index) +1;
                    }
                })
                return max;

            }else{
                return 1;
            }
        }

        //消息提示
        $scope.notifyMsg = function(msg){
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        }

    }
})();
