/**
 * Project: yineng-corpSysLand
 * Title: 费用申请查询页面控制器
 * author xiechangwei
 * date 2017/7/26 17:09
 * Copyright: 2017 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('costApplyViewController', costApplyViewController);

    costApplyViewController.$inject = ['$scope', '$http', '$filter', '$ionicLoading',"$location","$ionicPopup","$timeout","$rootScope","$ionicModal"];
    function costApplyViewController($scope, $http, $filter, $ionicLoading,$location,$ionicPopup,$timeout,$rootScope,$ionicModal){
        //数据id
        $scope.id = $location.search().id;
        //判断是从哪个页面跳转过来的
        $scope.pageType = $location.search().pageType;
        //申请金额合计
        $scope.countApplyMoney =0;
        //核准金额合计
        $scope.countApproveMoney =0;

        $scope.feeApplyData ={};
        //根据数据id查询数据
        $http.post(basePath + "/third/feeCostApply/findFeeCostApplyDataById?id="+$scope.id).success(function (data) {
            if (data.status == "0") {
                $scope.feeApplyData = data.result;
                $scope.approveUserName = data.result.approvalUserName;
                $scope.approveMoney();
                //获取当前人的上级
                $http.post(basePath + "/third/feeCostApply/findUserLeader").success(function (data) {
                    if(data.status=='0'){
                        $scope.feeApplyData.approvalUserName = data.result.name;
                        $scope.feeApplyData.approvalUserId = data.result.id;
                        if($scope.feeApplyData.approvalUserName==null){
                            $scope.feeApplyData.approvalUserName ="无";
                        }
                    }
                });
            } else {
                $scope.notifyMsg("获取数据失败!");
            }
        })


        //返回发起申请主页面
        $scope.back =function(){
            if (window.history.back()) {
                window.history.back();
            } else {
                window.close();
            }
        }

        //编辑
        $scope.edit = function (item) {
            $location.path("/cost_apply").search({id:item.id});
        }

        //动态计算核准金额
        $scope.approveMoney = function () {
            //申请金额合计
            $scope.countApplyMoney =0;
            //核准金额合计
            $scope.countApproveMoney =0;
            angular.forEach($scope.feeApplyData.jyhsMyExpensesInfoDTOList, function (newData) {
                $scope.countApproveMoney +=newData.approvalMoney;
                $scope.countApplyMoney +=newData.applyMoney
            })
        }

        //删除
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
                            $http.post(basePath + "/third/feeCostApply/deleteFeeCostApplyById?id="+item.id).success(function (data) {
                                if (data.status == "0") {
                                    $scope.notifyMsg("操作成功");
                                    $scope.back();
                                } else {
                                    $scope.notifyMsg(data.message);
                                }
                            })
                        }
                    }
                ]

            });
        }

        //显示更多
        $scope.showMore =false;
        $scope.showMoreButton = function () {
            $scope.showMore = !$scope.showMore;
        }

        //填写审批意见
        $scope.pageData ={};
        $scope.showMessage = function () {
            $scope.show ={}
            var myPopup = $ionicPopup.show({
                template: '<textarea type="input" rows="5" class="form-control"  placeholder="输入处理意见，最多50个字"  maxlength="50" ng-model="pageData.message"></textarea>',
                title: '处理意见(非必填)',
                scope: $scope,
                buttons: [
                    {
                        text: '取消',
                        type: 'button-local button-theme button-outline',
                        onTap: function (e) {
                            $scope.pageData.message = ""

                        }

                    },
                    {
                        text: '确定',
                        type: 'button-local button-theme',
                        onTap: function (e) {

                        }
                    }
                ]
            })
        }

        //审批不通过
        $scope.approvePass = function (type) {
            if(type==1){
                $scope.feeApplyData.isPass =false;
            }else{
                $scope.feeApplyData.type ='APPROVALED';
            }
            $scope.feeApplyData.checkOpinion = $scope.pageData.message;
            $http.post(basePath + "/third/feeCostApply/approveCostApplyData",$scope.feeApplyData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("审批成功!");
                    $timeout(function(){
                        $scope.back();
                    },2000);
                }else{
                    $scope.notifyMsg(data.message);
                    $timeout(function(){
                        $scope.back();
                    },2000);
                }
            })

        }

        //审批通过并提交给下一个节点的人
        $scope.passOthrer = function () {
            $scope.approveModal.show();
        }
        //审批人模态框
        //审批的时候 选择审批人模板
        var approveTemplate = ' <ion-modal-view class="auto bar-up-shadow">' +
            '<div class="bottom-50">' +
            '<div class="padding">' +
            '<div class="approve-flow-wrap flow-title"> ' +
            '<div class="title more">请选择审批人</div>' +
            '<div class="pos-right-text">'+
            '<input type="text" class="text-left" placeholder="请选择" user-selector inServiceStates = "1" title="审批人" onclick="approveUser" routingStatusType="YNCostApply" userTypeApp="1" ng-model="feeApplyData.approvalUserName"  on-select="selectUser(item)"  readonly/>' +
            '<a class="text-right right-text" href="" style="text-decoration:none" ng-if="feeApplyData.approvalUserName" ng-click="reSelectUser(item)">修改<a>' +
            '<a class="text-right right-text" href="" style="text-decoration:none" ng-if="!feeApplyData.approvalUserName" ng-click="reSelectUser(item)">选择<a>' +
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
            $scope.feeApplyData.approvalUserName = item.name;
            $scope.feeApplyData.approvalUserId = item.id;
        };
        //重新选择人员
        $scope.reSelectUser = function (item) {
            $scope.$broadcast("approveUser",item);
            if(item){
                $scope.feeApplyData.approvalUserName = item.name;
                $scope.feeApplyData.approvalUserId = item.id;
            }

        };

        $scope.approveDataOk = function () {
            $scope.feeApplyData.isPass =true;
            $scope.feeApplyData.checkOpinion = $scope.pageData.message;
            $http.post(basePath + "/third/feeCostApply/approveCostApplyData",$scope.feeApplyData).success(function(data){
                if(data.status==0){
                    $scope.notifyMsg("审批成功!");
                    $scope.closeApproveModal();
                    $timeout(function(){
                        $scope.back();
                    },2000);
                }else{
                    $scope.notifyMsg(data.message);
                    $timeout(function(){
                        $scope.back();
                    },2000);
                }
            })
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
