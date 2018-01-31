/**
 * 操行分考核选择考核项目控制器
 * Created by wangl on 2017/08/09 10:33
 *
 */
angular.module('starter').controller('selectCheckItemCtrl', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "creditStorageService", "$ionicLoading", "$ionicScrollDelegate",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, creditStorageService, $ionicLoading, $ionicScrollDelegate) {
        // 已选中的学生
        $scope.selectedStuList = creditStorageService.getItemNoDel("credit_selectedStuList");

        $scope.locationStr = "right";
        // 考核项目类别和项目集
        $scope.checkTypeAndItemList = [];
        //  查询值
        $scope.queryValue = null;
        // 项目类别集
        $scope.itemTypeList = [];
        // 考核项目
        $scope.itemList = [];
        // 临时展示的考核项目（用于展示筛选结果）
        $scope.itemListTemp = [];
        // 选中的考核类别
        $scope.selectedItemType = null;

        // 过滤条件
        $scope.filterInfo = {
            selectedCreditType: 0, // 选中的过滤条件
            selectedCreditTypeCache: null // 选中的过滤条件缓存
        };

        /**
         * 过滤
         * @param type null 全部，0 扣分项，1 加分项
         */
        $scope.filter = function (type) {
            $scope.filterInfo.selectedCreditType = type;
            if (type != null) {
                $scope.itemListTemp = $filter('filter')($scope.itemList, {"creditType": type});
            } else {
                $scope.itemListTemp = angular.copy($scope.itemList);
            }
        };

        /**
         * 选中考核类别
         * @param item
         */
        $scope.selectItemType = function (item) {
            $scope.selectedItemType = item;
            $scope.itemList = [];
            // 如果选中某个类别，则展示该类别下的项目
            if (item) {
                $scope.itemList = $scope.itemList.concat($scope.selectedItemType.creditExamineItemVOs);
            }
            // 如果选中全部，展示所有项目
            else {
                angular.forEach($scope.itemTypeList, function (itemType) {
                    $scope.itemList = $scope.itemList.concat(itemType.creditExamineItemVOs);
                });
            }
            $scope.filter($scope.filterInfo.selectedCreditType);
        };

        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };
        $scope.tips = initTips();
        /**
         *  获取考核项目类别和项目
         */
        var searchExamineItem = function () {
            $http.post(originBaseUrl + '/third/behavioRassess/searchExamineItem.htm', {creditType: null, queryValue: $scope.queryValue}).success(function (data) {
                $scope.tips = initTips();
                $scope.itemTypeList = [];
                if (data.status == 0) {
                    if (!(data.result && data.result.length > 0)) {
                        $scope.tips.isNoData = true;
                    } else {
                        $scope.itemTypeList = data.result;
                    }
                    $scope.selectItemType(null);
                } else {
                    $scope.tips.isError = true;
                }
            }).error(function () {
                $scope.tips = initTips();
                $scope.itemTypeList = [];
                $scope.tips.isError = true;
            });
        };
        searchExamineItem();



        var timer = null;
        $scope.$watch('queryValue', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                if (timer) {
                    $timeout.cancel(timer);
                }
                timer = $timeout(function () {
                    if (newVal == null) {
                        $scope.filterInfo.selectedCreditType = $scope.filterInfo.selectedCreditTypeCache;
                    } else {
                        $scope.filterInfo.selectedCreditTypeCache = angular.copy($scope.filterInfo.selectedCreditType);
                        $scope.filterInfo.selectedCreditType = null;
                    }
                    searchExamineItem();
                }, 700);
            }
        });

        /**
         * 清空查询条件
         */
        $scope.cleanVal = function () {
            $scope.queryValue = null;
        };

        /**
         * 重试
         */
        $scope.retry = function () {
            searchExamineItem();
        };

        /**
         * 选中考核项
         * @param item
         */
        $scope.selectCheckItem = function (item) {
            // 放入缓存
            if (item.convertClassScore) {
                item.convertClassScore = Math.abs(item.convertClassScore);
            }
            creditStorageService.setItem("credit_checkItem", item);
            $location.path("/fillInCheckInfo");
        };

        /**
         * 上一步
         */
        $scope.previousStep = function () {
            var fromSrc = $location.search().fromSrc;
            if (fromSrc) {
                // 来源班级学生选择页面
                if (fromSrc == "byClass") {
                    $location.path("/searchStuByClass").search("isFromItemPage", true);
                } else if (fromSrc == "byCondition"){
                    $location.path("/searchStuByCondition");
                }
            } else {
                $location.path("/search_student");
            }
        };
    }]);


