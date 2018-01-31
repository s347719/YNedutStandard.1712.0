/**
 * Created by wuhaiying on 2017/4/24.
 */
(function(){
    'use strict';
    angular
        .module('myApp')
        .controller('approveController', approveController);
    approveController.$inject = ['$scope','$ionicLoading','$ionicModal','$ionicPopup','$http','$timeout','$location','$ionicScrollDelegate'];
    function approveController($scope,$ionicLoading,$ionicModal,$ionicPopup,$http,$timeout,$location,$ionicScrollDelegate){
        //点击更多按钮
        $scope.moreButton =false;
        //默认选中待审批
        $scope.isTab = false;
        if($location.search().isTab==1){
            $scope.isTab =true;
        }else{
            $scope.isTab = false;
        }
        //是否显示无数据
        $scope.showNoData = false;

        //是否显示没有更多数据了
        $scope.isNoMoreData = false;
        $scope.isNoMoreAlreadyData = false;


        //是否默认加载数据
        $scope.areadyData =false;
        $scope.approveData = true;
        //加载数据
        $scope.loadData = function (type) {
            //把数据清空
            $scope.dataList =[];
            $scope.alreadyDataList =[];
            //是否显示没有数据
            $scope.showNoData = false;
            $scope.showNoAlreadyData = false;
            ////显示提示信息
            //$scope.showInfo = true;
            $scope.isTab = type;
            $scope.page = 0;
            $scope.isMoreData =true;
            if(type==true){
                $scope.alreadyApproveData = true;
                $scope.doAlreadyRefresh();
            }else{
                $scope.approveData = true;
                $scope.doRefresh($scope.isTab);
            }
        }
        $scope.showMoreButton = function(){
            $scope.moreButton = !$scope.moreButton;
        };
        $scope.page = 0;
        $scope.dataList = [];
        //显示错误
        $scope.showError = false;
        //doRefresh 只加载第一页数据
        $scope.doRefresh = function (type) {
            $http.post(basePath + "/third/xtbgactivity/findJSTXActivityByCondtionForUndertakeProcess?pageNumber=0&&pageSize=20"+"&isHandled=false", $scope.conditions).success(function (data) {
                if (data.status == "0") {
                    //数据请求结束 关闭提示
                    //$ionicLoading.hide();
                    $scope.dataList = data.result;
                    //如果页面数据没有填充慢 则说明没有数据了 则不再继续请求了
                    if(data.result.length <20){
                        $scope.isNoMoreData = true;
                        $scope.approveData = false;
                    }
                    //如果请求数据为空 就不再请求了
                    if($scope.dataList.length ==0){
                        $scope.showNoData = true;
                        $scope.isNoMoreData = true;
                    }else{
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                    }
                    //重新计算页面大小 填充页面
                    $ionicScrollDelegate.resize();
                } else {
                    $scope.showError = true;
                }
            })
        };
        //下拉刷新已处理tab
        $scope.alreadyDataList =[];
        $scope.doAlreadyRefresh = function () {
            $http.post(basePath + "/third/xtbgactivity/findJSTXActivityByCondtionForUndertakeProcess?pageNumber=0&&pageSize=20"+"&isHandled=true", $scope.conditions).success(function (data) {
                if (data.status == "0") {
                    //数据请求结束 关闭提示
                    $scope.alreadyDataList = data.result;
                    //如果页面数据没有填充慢 则说明没有数据了 则不再继续请求了
                    if(data.result.length <20){
                        $scope.isNoMoreAlreadyData = true;
                        $scope.alreadyApproveData = false;
                    }
                    //如果请求数据为空 就不再请求了
                    if($scope.alreadyDataList.length ==0){
                        $scope.isNoMoreAlreadyData = true;
                        $scope.showNoData = true;
                    }else{
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                    }
                    //重新计算页面大小 填充页面
                    $ionicScrollDelegate.resize();
                } else {
                    $scope.showError = true;
                }
            })
        };

        //加载已处理数据
        $scope.loadAlreadyMore = function(){
            //判断是不是同一种类型，如果不是 则要把页数重置和数据重置
            $http.post(basePath + "/third/xtbgactivity/findJSTXActivityByCondtionForUndertakeProcess?pageNumber="+$scope.page+"&pageSize=20"+"&isHandled=true", $scope.conditions).success(function (data) {
                if (data.status == "0") {
                    $scope.page++;
                    $scope.alreadyDataList = $scope.alreadyDataList.concat(data.result);
                    if($scope.alreadyDataList.length ==0){
                        $scope.showNoAlreadyData = true;
                        $scope.isNoMoreAlreadyData = true;
                    }
                    //如果不足20条则不加载
                    if(data.result.length <20){
                        $scope.alreadyApproveData = false;
                        $scope.isNoMoreAlreadyData = true;
                    }
                    if (data.result.length != 0) {
                        $scope.alreadyApproveData = true;
                        $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                    } else {
                        $scope.alreadyApproveData = false;
                    }
                } else {
                    $scope.showError = true;
                }
            })
        }

        //加载数据
        $scope.loadMore = function () {
            //判断是不是同一种类型，如果不是 则要把页数重置和数据重置
            $http.post(basePath + "/third/xtbgactivity/findJSTXActivityByCondtionForUndertakeProcess?pageNumber="+$scope.page+"&pageSize=20"+"&isHandled=false", $scope.conditions).success(function (data) {
                if (data.status == "0") {
                    $scope.page++;
                    $scope.dataList = $scope.dataList.concat(data.result);
                    if($scope.dataList.length ==0){
                        $scope.approveData = true;
                        $scope.isNoMoreData = true;
                    }
                    //如果不足20条则不加载
                    if(data.result.length <20){
                        $scope.approveData = false;
                        $scope.isNoMoreData = true;
                    }
                    if (data.result.length != 0) {
                        $scope.approveData = true;
                        $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                    } else {
                        $scope.approveData = false;
                    }
                } else {
                    $scope.showError = true;
                }
            })
        };
        // 上滑事件状态改变时，执行加载更多
        $scope.$on('stateChangeSuccess', function () {
            if($scope.isTab){
                $scope.alreadyApproveData =true;
                $scope.loadAlreadyMore();
            }else{
                $scope.approveData = true;
                $scope.loadMore();
            }
        });



        //审批进行时
        //$ionicModal.fromTemplateUrl('approving.html',{
        //    scope:$scope,
        //    animation:'slide-in-right'
        //}).then(function(modal){
        //    $scope.modalOne = modal;
        //});
        //待处理流程审批
        $scope.showApproveModal = function(item){
            //待审批
            if(!$scope.isTab){
                //判断是否可以在手机端审批
                if(!item.isPcOrApp){
                    $scope.showWarning();
                }else{
                    //请假申请
                    if (item.actKey == 'YNLeave') {
                        $location.path("/leave_apply_view").search({
                            id: item.bussinessId,
                            actExecutionProcinstId: item.actExecutionProcinstId,
                            processScheduleStates:item.processScheduleStates,
                            applyId:item.applyUserId,
                            type:"approve",
                            isTab:2
                        });
                    }
                    //出差申请
                    if (item.actKey == 'YNBusinessTravel') {
                        $location.path("/business_trip_view").search({
                            id: item.bussinessId,
                            actExecutionProcinstId: item.actExecutionProcinstId,
                            processScheduleStates:item.processScheduleStates,
                            applyId:item.applyUserId,
                            type:"approve",
                            isTab:2
                        });
                    }
                    //外勤申请
                    if (item.actKey == 'YNFieldPersonnel') {
                        $location.path("/outwork_apply_view").search({
                            id: item.bussinessId,
                            actExecutionProcinstId: item.actExecutionProcinstId,
                            processScheduleStates:item.processScheduleStates,
                            applyId:item.applyUserId,
                            type:"approve",
                            currentTaskId:item.currentTaskId,
                            isTab:2
                        });
                    }
                    //超标费用申请
                    if (item.actKey == 'YNExcessivCost') {
                        $location.path("/excessiv_cost_apply").search({
                            id: item.bussinessId,
                            actExecutionProcinstId: item.actExecutionProcinstId,
                            processScheduleStates:item.processScheduleStates,
                            applyId:item.applyUserId,
                            type:"approve",
                            isTab:2
                        });
                    }
                    //费用申请
                    if(item.actKey =="YNCostApply"){
                        $location.path("/cost_apply_view").search({id:item.bussinessId,pageType:'approve'});
                    }
                }
            }else{
                $scope.showDetail(item);
            }


        };
        //已处理的流程查看详情
        $scope.showDetail = function (item) {
            //判断是否可以在手机端审批
            if(!item.isPcOrApp){
                $scope.showWarning();
            }else{
                //请假申请
                if (item.actKey == 'YNLeave') {
                    $location.path("/leave_apply_view").search({
                        id: item.bussinessId,
                        actExecutionProcinstId: item.actExecutionProcinstId,
                        processScheduleStates:item.processScheduleStates,
                        applyId:item.applyUserId,
                        type:"already",
                        isTab:1
                    });
                }
                //出差申请
                if (item.actKey == 'YNBusinessTravel') {
                    $location.path("/business_trip_view").search({
                        id: item.bussinessId,
                        actExecutionProcinstId: item.actExecutionProcinstId,
                        processScheduleStates:item.processScheduleStates,
                        applyId:item.applyUserId,
                        type:"already",
                        isTab:1
                    });
                }
                //超标费用申请
                if (item.actKey == 'YNExcessivCost') {
                    $location.path("/excessiv_cost_apply").search({
                        id: item.bussinessId,
                        actExecutionProcinstId: item.actExecutionProcinstId,
                        processScheduleStates:item.processScheduleStates,
                        applyId:item.applyUserId,
                        type:"already",
                        isTab:1
                    });
                }
                //外勤申请
                if (item.actKey == 'YNFieldPersonnel') {
                    $location.path("/outwork_apply_view").search({
                        id: item.bussinessId,
                        actExecutionProcinstId: item.actExecutionProcinstId,
                        processScheduleStates:item.processScheduleStates,
                        applyId:item.applyUserId,
                        type:"already",
                        currentTaskId:item.currentTaskId,
                        isTab:1
                    });
                }
                //费用申请
                if(item.actKey =="YNCostApply"){
                    $location.path("/cost_apply_view").search({id:item.bussinessId,pageType:'approve'});
                }
            }

        }
        //$scope.closeApproveModal = function(){
        //    $scope.modalOne.hide();
        //};
        //// Cleanup the modal when we're done with it!
        //$scope.$on('$destroy', function() {
        //    $scope.modalOne.remove();
        //});

        //默认显示提示 点击关闭
        $scope.showInfo = true;
        $scope.closeInfo = function () {
            $scope.showInfo = false;

        }
        //提示信息
        $scope.showWarning = function(){
            $ionicLoading.show({
                template:'此申请只能在电脑上进行审批'
            });
            setTimeout(function(){
                $ionicLoading.hide();
            },2000)
        };
        //处理意见弹框
        $scope.showHandleIdea = function(){
            var myPopup = $ionicPopup.show({
                template:'<textarea class="form-control" placeholder="输入处理意见，最多X个字"></textarea>',
                title:'处理意见(非必填)',
                scope:$scope,
                buttons:[
                    {
                        text:'取消',
                        type:'button-local button-theme button-outline'
                    },
                    {
                        text:'确定',
                        type:'button-local button-theme'
                    }
                ]
            })
            $timeout(function() {
                myPopup.close(); //close the popup after 3 seconds for some reason
            }, 3000);
        };

    }
})();
