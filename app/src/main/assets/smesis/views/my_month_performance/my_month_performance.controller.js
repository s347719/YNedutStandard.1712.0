/**
 * xiaobowen
 * 2017年11月16日14:58:13
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('myMonthPerformanceCtrl', performanceReviewCtrl);

    performanceReviewCtrl.$inject = ['$scope','$ionicPopup', '$http', '$timeout', '$ionicLoading', '$ionicModal', '$filter'];
    function performanceReviewCtrl($scope, $ionicPopup,$http, $timeout, $ionicLoading, $ionicModal, $filter){
        $scope.noResultFlag = false;
        $scope.errorFlag = false;
        $scope.nowPage = 1;
        $scope.indexFlag = false;
        //主页面查询
        ($scope.mainSearchFun = function () {
            $scope.noResultFlag = false;
            $scope.errorFlag = false;
            $http.post(basePath + "/third/jxglPerformInterview/findMyPerformanInterview?pageNumber=0&pageSize=12").success(function(data){
                if(data.status == 0){
                    if(null != data.result && data.result.content.length>0){
                        $scope.items = data.result;
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                    }else{
                        //没结果
                        $scope.noResultFlag = true;
                    }
                }else{
                    //错误
                    $scope.errorFlag = true;
                }
            })
        })();

        //切换tab
        $scope.changePage = function (type) {
            $scope.nowPage = type;
        };
        
        //展开收缩flag
        $scope.performFlag = true;
        $scope.workFlag = true;
        //展开收缩
        $scope.checkFlagFun = function (type) {
            if(type == 1){
                $scope.performFlag = !$scope.performFlag;
            }else if (type == 2){
                $scope.workFlag = !$scope.workFlag;
            }
        };

        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modal.remove();
        });

        var getDetail = function (item) {
            if(item != null){
                $scope.planId = item.planId;
                $scope.userId = item.userId;
                $http.post(basePath + "/third/jxglPerformInterview/findMyPerformanInterviewDetail",item).success(function(data){
                    if(data.status == "0"){
                        $scope.detailData = data.result;
                    }
                })
            }
        };

        //查看页面
        $ionicModal.fromTemplateUrl('view.html',{
            scope: $scope,
            animation: 'slide-in-right'
        }).then(function(modal){
            $scope.modalOne = modal;
        });
        $scope.goDetail = function(item) {
            if(item.interviewStatus == 1)return;
            $scope.nowPage = 1;
            $scope.modalOne.show();
            getDetail(item)
        };
        $scope.closeModalOne = function() {
            $scope.modalOne.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalOne.remove();
        });
        $ionicModal.fromTemplateUrl('notify-info.html',{
            scope: $scope
        }).then(function(modal){
            $scope.notifyModel = modal;
        });
        $scope.$on('$destroy', function() {
            $scope.notifyModel.remove();
        });
        
        $scope.save = function(item){
            item.planId = $scope.planId;
            item.userId = $scope.userId;
            $http.post(basePath+"/third/jxglPerformInterview/saveMyPerformanInterview",item).success(function(data){
                //TODO 冒泡提示
                if(data.status == "0"){
                    $scope.errorFlag = false;
                    $scope.notifyModel.show();
                    $timeout(function() {
                        $scope.notifyModel.hide(); //hide the modal after 3 seconds for some reason
                        $scope.closeModalOne();
                        $scope.mainSearchFun();
                    }, 3000);
                }else{
                    $scope.errorFlag = true;
                }
            })
        };
    }
})();
