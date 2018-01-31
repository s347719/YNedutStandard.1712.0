angular.module('starter')
    .config(function ($stateProvider) {
        $stateProvider
            //教室课表
            .state('schedule_classroom', {
                url: '/schedule_classroom',
                templateUrl: 'templates/schedule/schedule_classroom.html',
                controller:'scheduleClassroomCrl'
            })
            .state('classroom_classify', {
                url: '/classroom_classify',
                templateUrl: 'templates/schedule/classroom_classify.html',
                controller:'scheduleClassifyCrl'
            })
            .state('classroom_search', {
                url: '/classroom_search',
                templateUrl: 'templates/schedule/classroom_search.html',
                controller:'scheduleSearchCrl'
            })
            .state('classroom_check', {
                url: '/classroom_check',
                templateUrl: 'templates/schedule/classroom_check.html',
                controller:'scheduleCheckCrl'
            });
    })

    /*教室分类*/
    .controller('scheduleClassifyCrl', ['$scope', '$state', '$http', '$location', function ($scope, $state, $http, $location) {
        /*获取教室类型*/
        $scope.classroomTypeList = [];
        $scope.getClassroomType = function () {
            $http.get(originBaseUrl + '/third/schedules/queryClassroomType.htm?timestamp='+new Date().getTime()).success(function (res) {
                if(res.status == 0){
                    if (res.result.classroomType) {
                        $scope.classroomTypeList = res.result.classroomType;
                        $scope.isClassroomType = true;
                    } else {
                        $scope.isClassroomType = false;
                        $scope.noClassroomType = true
                    }
                }else{
                    $scope.loadErrorMsg = res.message;
                }
            }).error(function () {
                $scope.loadErrorMsg = '加载分类失败';
                $scope.noClassroomType = false;
            })
        };
        $scope.getClassroomType();

        /*点击切换页面*/
        $scope.classifyInputClick = function () {
            $location.path('/classroom_search');

        };
        /*教室分类页面 跳转到 教室列表页面*/
        $scope.classroomClassifyClick = function (type) {
            $location.path('/classroom_check').search('type', angular.toJson(type));
        };
    }])
    /*查看教室列表*/
    .controller('scheduleCheckCrl', ['$scope', '$location', '$http', function ($scope, $location, $http){
        /*获取教室*/
        var type = angular.fromJson($location.search().type);
        $scope.classroomName = type.name;
        $scope.classroomList = [];
        $scope.getClassroom = function (typeId) {
            $http.get(originBaseUrl + '/third/schedules/queryClassroom.htm?timestamp='+new Date().getTime()+'&typeId=' + typeId).success(function (res) {
                if(res.status == 0){
                    if (res.result) {
                        $scope.classroomList = res.result.classroom;
                        $scope.isClassroomType = true;
                        $scope.loadErrorMsg ='';
                    } else {
                        $scope.isClassroomType = false;
                        $scope.noClassroomType = true
                    }
                }else{
                    $scope.loadErrorMsg = res.message;
                }
            }).error(function () {
                $scope.loadErrorMsg = '加载分类失败';
                $scope.noClassroomType = false;
            })
        };
        $scope.getClassroom(type.id, type.name);
        $scope.searchIconClear = function () {
            $location.path('/classroom_classify');
        };
        $scope.checkReturnClick = function () {
            $location.path('/classroom_classify');
        };
        /*点击进入课表*/
        $scope.classroomListClick = function (classroomByObj) {
            $location.path('/schedule_classroom').search('classroomByObj', angular.toJson(classroomByObj));
        };

    }])
    /*搜索教室*/
    .controller('scheduleSearchCrl', ['$scope', '$location', '$http','$timeout', function ($scope, $location, $http,$timeout) {
        /*搜索框获取焦点*/
        $timeout(function(){
            $("#inputFocus").focus();
        },1000);
        /*获取教室*/
        $scope.classroomList = [];
        var intervalSearch;
        $scope.getClassroom = function (name) {
            $http.get(originBaseUrl + '/third/schedules/queryClassroom.htm?timestamp='+new Date().getTime()+'&name=' + name).success(function (res) {
                if (res.result.classroom){
                    $scope.classroomList = res.result.classroom;
                    $scope.isClassroomType = true;
                    $scope.isRefreshIcon = false;/*数据加载完成图标隐藏*/
                    $scope.loadErrorMsg ='';
                }else{
                    $scope.isClassroomType = false;
                    $scope.noClassroomType = true;
                }
            }).error(function () {
                $scope.loadErrorMsg = '加载分类失败';
                $scope.noClassroomType = false;
            })
        };
        $scope.searchIconClear = function () {
            $location.path('/classroom_classify');
        };
        /*点击进入课表*/
        $scope.classroomListClick = function (classroomByObj) {
            $location.path('/schedule_classroom').search('classroomByObj', angular.toJson(classroomByObj));
        };
        /*搜索教室名称*/
        $scope.searchNameLike = "";
        var getClassroom = function () {
            $scope.getClassroom($scope.searchNameLike);
            $scope.isRefreshIcon = true;/*数据加载中图标显示*/
        };
        $scope.searchClassroomByName = function () {
            if ((typeof intervalSearch) != undefined) {
                clearTimeout(intervalSearch);
            }
            if ($scope.searchNameLike != '') {
                intervalSearch = setTimeout(getClassroom, 1000);
            } else {
                $scope.classroomList = [];
                $scope.isRefreshIcon = false;
            }
        };
    }])
    /*教室课表*/
    .controller('scheduleClassroomCrl', ['$http', '$scope', '$rootScope', '$timeout', '$ionicLoading', '$ionicScrollDelegate', '$location', function ($http, $scope, $rootScope, $timeout, $ionicLoading, $ionicScrollDelegate, $location) {
        $scope.adminClassList = [];
        $scope.selectedAdminClass = {};
        $scope.selectedWeek = {};
        $scope.options = {};
        $scope.options.queryTimeTableByWeek = {};
        /*获取周*/
        $scope.getWeekInfo = function () {
            $http.get(originBaseUrl + '/third/schedules/getWeekInfo.htm').success(function (res) {
                if(res.status == 0){
                    if (res.result && res.result.weekList) {
                        $scope.curWeekAndDay = res.result.curWeekAndDay;
                        $scope.weekList = res.result.weekList;
                        $scope.adminClassList = res.result.adminClassList;
                        // 默认选择 当前周
                        angular.forEach($scope.weekList, function (week) {
                            if ($scope.curWeekAndDay.week == week.activityWeek) {
                                $scope.selectedWeek = week;
                            }
                        });
                        $scope.options = {
                            weekNum:$scope.selectedWeek.activityWeek,
                            userId:$scope.classroomByObj.id,
                            userType:5,
                            errorCallAction:function(){
                                $scope.loadErrorMsg = "课表加载失败！";
                                $scope.dataContent = false;
                            }
                        };
                        $scope.initDataTag = true;
                        $scope.dataContent = true;
                        $scope.loadErrorMsg ='';
                    } else {
                        //没有数据
                        $scope.loadErrorMsg = true;
                        $scope.dataContent = false
                    }
                }else{
                    $scope.loadErrorMsg = res.message;
                }
            }).error(function () {
                $scope.loadErrorMsg = '加载失败，请重试！';
            });
        };
        $scope.getWeekInfo();

        // 切换 周
        $scope.changeWeek = function (week) {
            var weekNum = week.activityWeek;
            if (weekNum == 0) {
                // 弹框 已经是第一周！
                $scope.showWarning("已经是第一周！");
            } else if (weekNum == $scope.weekList.length + 1) {
                // 弹框 已经是最后一周！
                $scope.showWarning("已经是最后一周！");
            } else {
                $scope.selectedWeek = week;
                $scope.options.queryTimeTableByWeek($scope.selectedWeek.activityWeek,$scope.classroomByObj.id,5);
                $scope.isClickWeek = weekNum;
            }
        };
        /*进重新请求课表*/
        $scope.reloadData = function () {
            $ionicLoading.show({
                template: '正在刷新...'
            });
            $timeout(function () {
                $scope.options.queryTimeTableByWeek();
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            },500)
        };
        /*弹框提示*/
        $scope.showWarning = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            $timeout(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            }, 700);
        };
        /*接收教室类型下的班级 参数*/
        var classroomByObj = angular.fromJson($location.search().classroomByObj);
        $scope.classroomByObj = classroomByObj;
        /*返回上一页*/
        $scope.goBack = function () {
            history.back();
        }
    }]);

