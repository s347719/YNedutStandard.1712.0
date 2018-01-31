/**
 * Created by liulijia on 2017/8/24.
 */
(function () {
    'use strict';

    angular
        .module('starter')
        .config(stateConfig)
        .controller('commentExcellentCtrl',commentExcellentCtrl);

    stateConfig.$inject = ['$stateProvider'];
    commentExcellentCtrl.$inject = ['$scope','$ionicPopup', '$http','ynuiNotification','$ionicLoading','$ionicScrollDelegate'];
    function stateConfig($stateProvider) {
        $stateProvider
            /*评优申请*/
            .state('taskApply_list', {
                firstPage: true,
                url: '/taskApply_list',
                templateUrl: 'templates/commentExcellent_apply/taskApply_list.html',
                controller:'commentExcellentCtrl'
            })
    }
    function commentExcellentCtrl($scope,$ionicPopup,$http,ynuiNotification,$ionicLoading,$ionicScrollDelegate){
        $scope.pageInfo = {
            totalPage:0,
            pageSize:20,
            pageNumber:0,
            itemSize:0
        };
        $scope.showOwnRecommend = false;//显示自荐理由页面
        $scope.MobaileTaskCheckVOList = [];
        $scope.getData = function (func) {
                $http.post(originBaseUrl+"/third/studentRewardTaskMobile/queryTaskInfoPage.htm?",{
                    pageNumber:$scope.pageInfo.pageNumber,
                    pageSize:$scope.pageInfo.pageSize
                }).success(function(data){
                    if(data.status==0){
                        $scope.MobaileTaskCheckVOList.push.apply($scope.MobaileTaskCheckVOList,data.result.content);
                        $scope.pageInfo = {
                            totalPage:data.result.totalPages,
                            pageSize:data.result.size,
                            pageNumber:data.result.number,
                            itemSize:data.result.totalElements
                        };
                        if(data.result.totalPages>0){
                            $scope.pageInfo.pageNumber = data.result.number + 1;
                        }
                        if(! $scope.MobaileTaskCheckVOList|| $scope.MobaileTaskCheckVOList.length<1){
                            $scope.emptyInfo = true;
                            $scope.dataErrorMsg ='没有可参与的评优活动';
                        }else{
                            $scope.emptyInfo = false;
                        }
                        if (func) {
                            func();
                        }
                    }else{
                        if (func) {
                            func();
                        }
                        $scope.emptyInfo = true;
                        $scope.dataErrorMsg ='加载失败，请重试';
                    }

                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg ='加载失败，请重试';
                });
        };
        $scope.getData();
        //刷新
        $scope.doRefresh = function () {
            $ionicLoading.show({
                template: '正在刷新...'
            });
            //置空数据，在获取时候叠加数据
            $scope.MobaileTaskCheckVOList = [];
            $scope.pageInfo = {
                totalPage:0,
                pageSize:20,
                pageNumber:0,
                itemSize:0
            };
            $scope.getData(function () {
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };

        $scope.$on('stateChangeSuccess', function () {
            $scope.loadMore();
        });
        /**
         * 加载更多
         */
        $scope.loadMore = function () {
            if ($scope.pageInfo.pageNumber >= $scope.pageInfo.totalPage) {
                return false;
            }
            $scope.getData(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicLoading.hide();
            });
        };
        $scope.submitVO = {
            taskId:"",
            adminClassId:"",
            ownRecommendDesc:""
        };
        $scope.$watch(function(){return  $scope.submitVO.ownRecommendDesc},function(newVal,oldVal){
           if(newVal && newVal!=oldVal){
               if($scope.submitVO.ownRecommendDesc.length>500){
                   $scope.submitVO.ownRecommendDesc = $scope.submitVO.ownRecommendDesc.substring(0,500);
               }
           }
        });
        $scope.back = function(){//返回
            if($scope.submitVO.ownRecommendDesc && $scope.submitVO.ownRecommendDesc.length>0){
                $ionicPopup.show({
                    title: '提示',
                    template: '<span class="help-block text-left">返回后填写内容将丢失，确定要返回吗?</span>',
                    buttons:[
                        {text:'继续编辑',onTap:function(){
                            $scope.showOwnRecommend = true;
                        }},
                        {text:"确定返回",onTap:function(){
                            $scope.showOwnRecommend = false;
                            $scope.submitVO = {
                                taskId:"",
                                adminClassId:"",
                                ownRecommendDesc:""
                            };
                        }
                        }
                    ]
                });
            }else{
                $scope.showOwnRecommend = false;
                $scope.submitVO = {
                    taskId:"",
                    adminClassId:"",
                    ownRecommendDesc:""
                };
            }

        };
        $scope.submit = function(){//提交申请
            if(!$scope.submitVO.ownRecommendDesc){
                ynuiNotification.error({msg: '请输入自荐理由'});
                return false;
            }
            if($scope.submitVO.ownRecommendDesc.length <20){
                ynuiNotification.error({msg: '自荐理由不能少于20个字!'});
                return false;
            }
            $ionicPopup.show({
                title: '提示',
                template: '<span class="help-block text-left">请确认已填写完成，提交后无法修改</span>',
                buttons:[
                    {text:'确定',onTap:function(){
                        $http.post(originBaseUrl+"/third/studentRewardTaskMobile/submitOwnRecommendInfo.htm?", $scope.submitVO).success(function(data){
                            if(data.status == 0){
                                $scope.showOwnRecommend = false;
                                ynuiNotification.success({msg: "已提交请耐心等待，祝你好运!"});
                                $scope.doRefresh();
                            }else{
                                ynuiNotification.error({msg: data.message});
                            }
                        }).error(function () {
                            ynuiNotification.error({msg: '服务器出错啦!'});
                        });
                    }},
                    {text:"取消",onTap:function(){

                    }
                    }
                ]
            });

        };
        /*查看详情*/
        $scope.showParticulars = function(item) {
            $scope.checkItem = item;
            var template = "<div class=\"apply-particulars text-left\">"
            +"<strong>●&nbsp;评优起止日期：</strong>"
            +"<span>"+$scope.checkItem.rewardDate+"</span>"
            +"<strong>●&nbsp;班级限定名额：</strong>"
            +"<span>"+$scope.checkItem.tatolNum+"人</span>"
            +($scope.checkItem.realityEndDate?"<strong>●&nbsp;实际结束日期：</strong>"
            +"<span>"+$scope.checkItem.realityEndDate+"</span>":"")
            +"<strong>●&nbsp;评优要求：</strong>"
            +"<span>"+$scope.checkItem.rewardDesc+"</span>"
            +($scope.checkItem.headRecommendDesc?"<div> <strong>●&nbsp;班主任推荐理由：</strong>"
            +"<span>"+$scope.checkItem.headRecommendDesc+"</span>"
            +"</div>":"")
            +($scope.checkItem.ownRecommendDate?"<div ><strong>●&nbsp;自荐日期：</strong>"
            +"<span>"+$scope.checkItem.ownRecommendDate+"</span>"
            +"</div>":"")
            +($scope.checkItem.ownRecommendDesc?"<div> <strong>●&nbsp;自荐理由：</strong>"
            +"<span>"+$scope.checkItem.ownRecommendDesc+"</span>"
            +"</div>":"")+" </div>";
            var buttons = [
                item.status == 0?{
                    text:'申请',
                    onTap:function(){
                        $scope.showOwnRecommend = true;
                        $scope.submitVO = {
                            taskId:item.taskId,
                            adminClassId:item.adminClassId,
                            ownRecommendDesc:""
                        };
                    }
                }:{text:"关闭"}
            ];
            if(item.status == 0){
                buttons.unshift({text:'取消'});
            }
            $ionicPopup.show({
                title: '详情',
                template: template,
                 buttons:buttons
            });
        };
        /*评优要求*/
        $scope.showApply = function(item) {
            $ionicPopup.show({
                title: '评优要求',
                template: "<span class='help-block text-left'>"+item.rewardDesc+"</span>",
                buttons:[{
                  text:'取消'
                },
                    {text:'继续',onTap:function(){
                        $scope.showOwnRecommend = true;
                        $scope.submitVO = {
                            taskId:item.taskId,
                            adminClassId:item.adminClassId,
                            ownRecommendDesc:""
                        };
                    }}
                ]
            });
        };
    }

})();