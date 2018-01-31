/**
 * 搜索教师
 * Created by wangl on 2017/1/9 15:30
 *
 */
angular.module('starter').controller('classroomClassifyController', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "tkStorageService", "$ionicLoading",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, tkStorageService, $ionicLoading) {
        /**
         * 路由信息
         * @type {string}
         */
        $scope.basePath = originBaseUrl;

        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };

        $scope.tips = initTips();
        /**
         * 教室分类集
         * @type {Array}
         */
        $scope.classRoomTypeList = [];

        // 获取教室分类集
        var getClassRoomType = function(){
            $scope.tips = initTips();
            $http.post($scope.basePath + "/third/tkTaskRegisterApp/findAllClassRoomType.htm").success(function (data) {
                if (data.status == 0) {
                    $scope.classRoomTypeList = data.result;
                    if (!(data.result && data.result.length > 0)) {
                        $scope.tips.isNoData = true;
                    }
                } else {
                    $scope.tips.isError = true;
                }
            }).error(function () {
                $scope.classRoomTypeList = [];
                $scope.tips.isError = true;
            });
        };
        getClassRoomType();
        /**刷新试试*/
        $scope.refreshAndRetry = function () {
            getClassRoomType();
        };

        var timeout;
        /**即时搜索值改变*/
        $scope.changeQueryVal = function (val) {
            $scope.queryConditions.pageNumber = 0;
            if (val) {
                // 清空按字母检索条件值
                $scope.queryConditions.character = "不限";
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(function () {
                    $scope.content = [];
                }, 700);
            } else {
                $scope.content = [];
            }
        };

        /**
         * 按分类搜索
         * @param item 分类对象
         */
        $scope.searchByType = function(item){
            tkStorageService.setItem("tkClassType", item);
            $location.path("/classroomCheck");
        };


        /**按名称搜索*/
        $scope.searchByName = function(){
            $location.path("/classroomSearch");
        };

        /**取消*/
        $scope.cancle = function () {
            $location.path("/basic_msg");
        };


    }]);


