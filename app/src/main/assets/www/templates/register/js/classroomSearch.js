/**
 * 按分类查看选择教室
 * Created by wangl on 2017/1/9 16:12
 *
 */
angular.module('starter').controller('classroomSearchController', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "tkStorageService", "$ionicLoading",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, tkStorageService, $ionicLoading) {
        /**
         * 路由信息
         * @type {string}
         */
        $scope.basePath = originBaseUrl;

        var tkBasicInfo = tkStorageService.getItemNoDel("tkBasicInfo");

        $scope.conditions = {
            className: null
        };

        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };

        $scope.tips = initTips();
        /**
         * 教室集
         * @type {Array}
         */
        $scope.classRoomList = [];

        $scope.isLoading = false;

        // 获取教室集
        var getClassRoom = function (isInstant) {
            if (isInstant) {
                $scope.isLoading = true;
            }
            $scope.tips = initTips();
            $scope.classRoomList = [];
            $http.post($scope.basePath + "/third/tkOutsideTaskRegisterApp/queryAvailableClassRoom.htm",{name: $scope.conditions.className}).success(function (data) {
                if (isInstant) {
                    $scope.isLoading = false;
                }
                if (data.status == 0) {
                    $scope.classRoomList = data.result;
                    if (!(data.result && data.result.length > 0)) {
                        $scope.tips.isNoData = true;
                    }
                } else {
                    $scope.tips.isError = true;
                }
            }).error(function () {
                if (isInstant) {
                    $scope.isLoading = false;
                }
                $scope.classRoomList = [];
                $scope.tips.isError = true;
            });
        };
        getClassRoom(false);

        /**刷新试试*/
        $scope.refreshAndRetry = function () {
            getClassRoom(false);
        };

        /**取消*/
        $scope.cancle = function () {
            $location.path("/classroomClassify");
        };

        /**选中教室*/
        $scope.selectClassRoom = function(item){
            tkBasicInfo.selectedClassRoomName = item.name;
            tkBasicInfo.selectedClassRoomId = item.id;
            tkBasicInfo.classRoomList = [];
            tkBasicInfo.classRoomList.push(item);
            tkStorageService.setItem("tkBasicInfo", tkBasicInfo);
            $location.path("/basic_msg").search({"isChooseTeacher":false});
            $location.path("/basic_msg").search({"isFromChooseClassRoom":true});
        };

        var timeout;
        /**即时搜索值改变*/
        $scope.changeQueryVal = function (val) {
            if (val) {
                if (timeout) {
                    $timeout.cancel(timeout);
                }
                timeout = $timeout(function () {
                    getClassRoom(true);
                }, 700);
            } else {
                getClassRoom(true);
            }
        };

        /**清除即时搜索值*/
        $scope.cleanQueryVal = function () {
            $location.path("/classroomClassify");
        };


    }]);
