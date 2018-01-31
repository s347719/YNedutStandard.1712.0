angular.module('starter')
    .config(function ($stateProvider) {
        $stateProvider
            //班级课表
            .state('schedule_class', {
                url: '/schedule_class',
                templateUrl: 'templates/schedule/schedule_class.html',
                controller:'scheduleClassCtrl'
            });
    })
.controller('scheduleClassCtrl', ['$http', '$scope', '$rootScope','$timeout','$ionicLoading','$ionicScrollDelegate', function ($http, $scope, $rootScope,$timeout,$ionicLoading,$ionicScrollDelegate) {
    $scope.adminClassList = [];
    $scope.selectedAdminClass = {};
    $scope.selectedWeek = {};
    /*获取周*/
    $scope.getWeekInfo = function(){
        $http.get(originBaseUrl + '/third/schedules/getWeekInfo.htm').success(function (res) {
            if(res.status == 0){
                if (res.result && res.result.weekList) {
                    $scope.curWeekAndDay = res.result.curWeekAndDay;
                    $scope.weekList = res.result.weekList;
                    $scope.adminClassList = res.result.adminClassList;
                    /*没有班级*/
                    if($scope.adminClassList.length<=0){
                        $scope.dataContent = false;
                        $scope.loadErrorMsg = "没有可查看的课表"
                    }else{
                        // 默认选择 第一个班级
                        if($scope.adminClassList && $scope.adminClassList.length){
                            $scope.selectedAdminClass = $scope.adminClassList[0];
                        }
                        // 默认选择 当前周
                        angular.forEach($scope.weekList, function (week) {
                            if ($scope.curWeekAndDay.week == week.activityWeek) {
                                $scope.selectedWeek = week;
                            }
                        });
                        $scope.options = {
                            weekNum:$scope.selectedWeek.activityWeek,
                            userId:$scope.selectedAdminClass.id,
                            userType:4,
                            errorCallAction:function(){
                                $scope.loadErrorMsg = "课表加载失败！";
                                $scope.dataContent = false;
                            }
                        };
                        $scope.initDataTag = true;
                        $scope.dataContent = true;
                        $scope.loadErrorMsg = '';
                    }
                } else {
                    //没有数据
                    $scope.loadErrorMsg = true;
                    $scope.dataContent = false
                }
            }else{
                $scope.loadErrorMsg = res.message;
            }
        }).error(function(){
            $scope.loadErrorMsg = '加载失败，请重试！';
        });
    };
    $scope.getWeekInfo();

    // 切换 班级
    $scope.changeAdminClass = function(adminClass){
        $scope.selectedAdminClass = adminClass;
        $scope.options.queryTimeTableByWeek($scope.selectedWeek.activityWeek,$scope.selectedAdminClass.id,4);
    };

    // 切换 周
    $scope.changeWeek = function(week){
        var weekNum = week.activityWeek;
        if(weekNum == 0){
            // 弹框 已经是第一周！
            $scope.showWarning("已经是第一周！");
        }else if(weekNum == $scope.weekList.length+1){
            // 弹框 已经是最后一周！
            $scope.showWarning("已经是最后一周！");
        }else{
            $scope.selectedWeek = week;
            $scope.options.queryTimeTableByWeek($scope.selectedWeek.activityWeek,$scope.selectedAdminClass.id,4);
            $scope.isClickWeek = weekNum;
        }
    };

    /*弹框提示*/
    $scope.showWarning = function () {
        $ionicLoading.show({
            template: "加载中..."
        });
        $timeout(function () {
            $scope.getWeekInfo();
            $scope.options.queryTimeTableByWeek();
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        },700);
    };
}]);

