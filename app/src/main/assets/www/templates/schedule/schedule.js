angular.module('starter')
    .config(function ($stateProvider) {
        $stateProvider
            //学生教师课表
            .state('schedule', {
                url: '/schedule',
                templateUrl: 'templates/schedule/schedule.html',
                controller: 'scheduleNewCtrl'
            });
    })

    .controller('scheduleNewCtrl', ['$http', '$scope', '$rootScope', '$timeout', '$ionicLoading', '$ionicScrollDelegate', 'ynuiNotification',
        function ($http, $scope, $rootScope, $timeout, $ionicLoading, $ionicScrollDelegate, ynuiNotification) {
            $scope.curWeekAndDay = {};
            $scope.weekList = [];
            $scope.selectedWeek = {};
            $scope.options = {};
            $scope.getWeekInfo = function () {
                $http.get(originBaseUrl + '/third/schedules/getWeekInfo.htm').success(function (res) {
                    if (res.status == 0) {
                        if (res.result && res.result.weekList) {
                            $scope.curWeekAndDay = res.result.curWeekAndDay;
                            $scope.weekList = res.result.weekList;
                            /*默认选中当前周*/
                            $scope.selectedWeek = $scope.getWeekByWeekNum($scope.curWeekAndDay.week);
                            $scope.options = {
                                weekNum: $scope.selectedWeek.activityWeek,
                                userId: $rootScope.authorizationStr.userId,
                                userType: $rootScope.authorizationStr.userType,
                                errorCallAction: function () {
                                    $scope.loadErrorMsg = "课表加载失败！";
                                    $scope.dataContent = false;
                                }
                            };
                            $scope.initDataTag = true;
                            $scope.dataContent = true;
                            $scope.loadErrorMsg = '';
                        } else {
                            $scope.loadErrorMsg = true;
                            $scope.dataContent = false;
                        }
                    } else {
                        $scope.loadErrorMsg = res.message;
                    }
                }).error(function () {
                    $scope.loadErrorMsg = '加载失败，请重试！';
                });
            };
            $scope.getWeekInfo();
            $scope.getWeekByWeekNum = function (weekNum) {
                var selectedWeek = {};
                angular.forEach($scope.weekList, function (week) {
                    if (week.activityWeek == weekNum) {
                        selectedWeek = week;
                    }
                });
                return selectedWeek;
            };
            $scope.changeWeek = function (weekNum) {
                if (weekNum == 0) {
                    // 弹框 已经是第一周！
                    $scope.showWarning("已经是第一周！");
                    return false;
                } else if (weekNum == $scope.weekList.length + 1) {
                    // 弹框 已经是最后一周！
                    $scope.showWarning("已经是最后一周！");
                    return false;
                }else{
                    $scope.selectedWeek = $scope.getWeekByWeekNum(weekNum);
                    $scope.options.queryTimeTableByWeek($scope.selectedWeek.activityWeek, $rootScope.authorizationStr.userId, $rootScope.authorizationStr.userType);
                    $scope.isClickWeek = weekNum;
                }
            };

            /**
             * 刷新数据
             */
            $scope.refreshList = function () {
                $ionicLoading.show({
                    template: "加载中..."
                });
                $timeout(function () {
                    $scope.getWeekInfo();
                    $scope.options.queryTimeTableByWeek();
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 700);
            };
            /*弹框提示*/
            $scope.showWarning = function (message) {
                ynuiNotification.error({"msg": message});
            };
        }]);



