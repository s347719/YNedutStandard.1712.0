/**
 * 操行分考核首页控制器
 * Created by wangl on 2017/08/09 10:33
 *
 */
angular.module('starter').controller('searchByConditionCtlr', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "creditStorageService", "$ionicLoading", "$ionicScrollDelegate",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, creditStorageService, $ionicLoading, $ionicScrollDelegate) {
        $scope.numberOrName = null;
        // 已选中的学生
        $scope.selectedStuList = creditStorageService.getItemNoDel("credit_selectedStuList");
        // 是否查询完成
        $scope.loadFinished = true;
        // 是否显示已选中学生
        $scope.showSelectedStu = false;
        $scope.locationStr = "center";
        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };

        /**
         * 初始化已选中学生标识
         * @param selectedStuList
         * @param searchStuList
         */
        var initSelectedFlag = function (selectedStuList, searchStuList) {
            angular.forEach(selectedStuList , function (selectedStu) {
                angular.forEach(searchStuList , function (stu) {
                    if (selectedStu.platformSysUserId == stu.platformSysUserId) {
                        stu.isSelected = true;
                    }
                });
            });
        };


        $scope.tips = initTips();
        $scope.searchStuList = [];
        $scope.query = function () {
            if (!$scope.loadFinished) {
                return;
            }
            $scope.tips = initTips();
            $scope.loadFinished = false;
            $http.post(originBaseUrl + '/third/behavioRassess/searchStuByNumberOrName.htm', {numberOrName: $scope.numberOrName}).success(function (data) {
                $scope.loadFinished = true;
                if (data.status == 0) {
                    if (!(data.result && data.result.length > 0)) {
                        $scope.tips.isNoData = true;
                    } else {
                        angular.forEach(data.result, function (item) {
                            item.isSelected = false;
                            if (item.headUrl) {
                                var tempArr = item.headUrl.split(".");

                                var artworkUrl = tempArr[0].substring(0, tempArr[0].length - 2);
                                item.artworkUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + artworkUrl + "." + tempArr[1];
                                item.headUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + item.headUrl;

                            } else {
                                if (item.gender != 2) {
                                    item.headUrl = "img/userface_0.png";
                                } else {
                                    item.headUrl = "img/userface_1.png";
                                }
                            }
                        });
                    }
                    $scope.searchStuList = data.result;
                    initSelectedFlag($scope.selectedStuList, $scope.searchStuList);
                } else {
                    $scope.tips.isError = true;
                }
            }).error(function () {
                $scope.loadFinished = true;
                $scope.tips.isError = true;
            });
        };

        /**
         * 选中学生
         * @param item
         */
        $scope.selectStu = function (item) {
            if (item.isSelected) {
                item.isSelected = false;
                var index = 0, delIndex = 0;

                angular.forEach($scope.selectedStuList, function (stu) {
                    if (stu.platformSysUserId == item.platformSysUserId) {
                        delIndex = index;
                    }
                    index++;
                });
                $scope.selectedStuList.splice(delIndex, 1);
            } else {
                item.isSelected = true;
                $scope.selectedStuList.unshift(item);
            }

            // 及时放入缓存
            creditStorageService.setItem("credit_selectedStuList", $scope.selectedStuList);
        };


        /**
         * 返回到首页
         */
        $scope.back = function () {
            $location.path("/search_student").search("fromInternal", true);
        };

        /**
         * 开始考核
         */
        $scope.startCheck = function () {
            if ($scope.selectedStuList.length == 0) {
                ynuiNotification.error({msg: "请先添加学生！"});
                return;
            }
            $location.path("/selectCheckItem").search({"fromSrc": "byCondition", "numberOrName": $scope.numberOrName});
        };
    }]);


