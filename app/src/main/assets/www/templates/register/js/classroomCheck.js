/**
 * 按分类查看选择教室
 * Created by wangl on 2017/1/9 16:12
 *
 */
angular.module('starter').controller('classroomCheckController', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "tkStorageService", "$ionicLoading",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, tkStorageService, $ionicLoading) {
        /**
         * 路由信息
         * @type {string}
         */
        $scope.basePath = originBaseUrl;

        var tkBasicInfo = tkStorageService.getItemNoDel("tkBasicInfo");

        $scope.param = tkStorageService.getItemNoDel("tkClassType");

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

        // 获取教室集
        var getClassRoom = function () {
            $scope.tips = initTips();
            $http.post($scope.basePath + "/third/tkOutsideTaskRegisterApp/getClassRoomByType.htm",{code: $scope.param.dictCode}).success(function (data) {
                if (data.status == 0) {
                    $scope.classRoomList = data.result;
                    if (!(data.result && data.result.length > 0)) {
                        $scope.tips.isNoData = true;
                    }
                } else {
                    $scope.tips.isError = true;
                }
            }).error(function () {
                $scope.classRoomList = [];
                $scope.tips.isError = true;
            });
        };
        getClassRoom();

        /**刷新试试*/
        $scope.refreshAndRetry = function () {
            getClassRoom();
        };

        /**取消*/
        $scope.cancle = function () {
            tkStorageService.clearKey("tkClassType");
            $location.path("/classroomClassify").search({"isChooseTeacher":false});
            $location.path("/basic_msg").search({"isFromChooseClassRoom":true});
        };

        /**选中教室*/
        $scope.selectClassRoom = function(item){
            tkBasicInfo.selectedClassRoomName = item.name;
            tkBasicInfo.selectedClassRoomId = item.id;
            tkStorageService.clearKey("tkClassType");
            tkStorageService.setItem("tkBasicInfo", tkBasicInfo);
            $location.path("/basic_msg").search({"isChooseTeacher":false});
            $location.path("/basic_msg").search({"isFromChooseClassRoom":true});
        };


    }]);
