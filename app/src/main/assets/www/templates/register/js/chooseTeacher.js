/**
 * 选择教师
 * Created by yineng on 2017/1/5 10:33
 *
 */
angular.module('starter').controller('chooseTeacherController', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "tkStorageService", "$ionicLoading", "$ionicScrollDelegate",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, tkStorageService, $ionicLoading, $ionicScrollDelegate) {
        /**
         * 路由信息
         * @type {string}
         */
        $scope.basePath = originBaseUrl;
        var param = tkStorageService.getItemNoDel("tkBasicInfo");

        $scope.queryConditions = {
            queryTeacherVal: null, //查询教师条件值
            character: "不限", // 姓名首字母
            pageNumber: 0,
            pageSize: 20

        };
        $scope.queryConditions = angular.extend($scope.queryConditions, param);

        $scope.characterList = ["不限", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];


        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };

        $scope.isLoading = false;
        $scope.tips = initTips();
        $scope.page = {};
        $scope.content = [];
        /***
         * 获取分页查询结果
         * @param isInstant 是否是搜索框即时搜索
         */
        var getSKTeacherInfoByPage = function (isInstant) {
            if (isInstant) {
                $scope.isLoading = true;
            }
            $scope.tips = initTips();
            var paramObj = angular.copy($scope.queryConditions);
            if (paramObj.character == "不限") {
                paramObj.character = null;
            }
            $http.post($scope.basePath + "/third/tkOutsideTaskRegisterApp/getSKTeacherInfoByPage.htm", paramObj
            ).success(function (data) {
                    if (isInstant) {
                        $scope.isLoading = false;
                    }
                    $scope.page = null;
                    if (data.status == 0) {
                        $scope.page = data.result;
                        if (!(data.result.content && data.result.content.length > 0)) {
                            $scope.tips.isNoData = true;
                        } else {
                            // 缓存的有滚动之前的内容
                            if ( $scope.content.length > 0) {
                                angular.forEach($scope.page.content, function(item){
                                    $scope.content.push(item);
                                });
                            } else {
                                angular.forEach($scope.page.content, function(item){
                                    $scope.content.push(item);
                                });
                            }
                        }
                    } else {
                        $scope.tips.isError = true;
                    }
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }).error(function () {
                    if (isInstant) {
                        $scope.isLoading = false;
                    }
                    $scope.tips.isError = true;
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                });
        };
        getSKTeacherInfoByPage(false);

        /**刷新试试*/
        $scope.refreshAndRetry = function () {
            getSKTeacherInfoByPage(false);
        };


        /**按首字母搜索*/
        $scope.clickCharacter = function (itme) {
            $scope.content = [];
            $scope.page = {};
            //$ionicScrollDelegate.scrollTop();
            $scope.queryConditions.character = itme;
            $scope.queryConditions.queryTeacherVal = null;
            $scope.queryConditions.pageNumber = 0;
            getSKTeacherInfoByPage(false);
        };


        var timeout;
        /**即时搜索值改变*/
        $scope.changeQueryVal = function (val) {
            $scope.queryConditions.pageNumber = 0;
            $scope.content = [];
            $scope.page = {};
            $ionicScrollDelegate.scrollTop();
            if (val) {
                // 清空按字母检索条件值
                $scope.queryConditions.character = "不限";
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(function () {
                    getSKTeacherInfoByPage(true);
                }, 700);
            } else {
                getSKTeacherInfoByPage(true);
            }
        };

        /**清除即时搜索值*/
        $scope.cleanQueryVal = function () {
            $scope.content = [];
            $scope.page = {};
            $ionicScrollDelegate.scrollTop();
            $scope.queryConditions.pageNumber = 0;
            $scope.queryConditions.queryTeacherVal = null;
            getSKTeacherInfoByPage(false);
        };

        /**选中教师*/
        $scope.chooseTeacher = function (item) {
            var isChangeTeacher = false;
            if (param.teacherId != item.id) {
                param.teacherId = item.id;
                param.teacherName = item.displayName;
                isChangeTeacher = true;
                tkStorageService.setItem("tkBasicInfo", param);
            }
            $location.path("/basic_msg").search({"isChooseTeacher": true, isChangeTeacher: isChangeTeacher});
        };

        /**取消*/
        $scope.cancle = function () {
            $location.path("/basic_msg").search({"isChooseTeacher": true, isChangeTeacher: false});
        };

        /**
         * 加载更多
         */
        $scope.loadMore = function () {
            $scope.queryConditions.pageNumber++;
            if ($scope.queryConditions.pageNumber >= $scope.page.totalPages) {
                return false;
            }
            getSKTeacherInfoByPage(false);
        };

    }]);


