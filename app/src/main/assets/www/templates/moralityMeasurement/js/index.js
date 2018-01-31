/**
 * 操行分考核首页控制器
 * Created by wangl on 2017/08/09 10:33
 *
 */
angular.module('starter')
.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
        //搜索学生
        .state('search_student', {
            url: '/search_student',
            templateUrl: 'templates/moralityMeasurement/index.html',
            controller: 'creditIndexController',
            cache:false
        })
        //按学号、姓名搜索学生
        .state('searchStuByCondition', {
            url: '/searchStuByCondition',
            templateUrl: 'templates/moralityMeasurement/searchStuByCondition.html',
            controller: 'searchByConditionCtlr',
            cache:false
        })
        //从班级中选择
        .state('searchStuByClass', {
            url: '/searchStuByClass',
            templateUrl: 'templates/moralityMeasurement/searchStuByClass.html',
            controller:'searchStuByClassCtrl',
            cache:false
        })
        //选择考核项目
        .state('selectCheckItem', {
            url: '/selectCheckItem',
            templateUrl: 'templates/moralityMeasurement/selectCheckItem.html',
            // templateUrl: 'templates/moralityMeasurement/select_examine_project.html',
            controller:'selectCheckItemCtrl',
            cache:false
        })
        //填写考核信息
        .state('fillInCheckInfo', {
            url: '/fillInCheckInfo',
            templateUrl: 'templates/moralityMeasurement/fillInCheckInfo.html',
            controller:'fillInCheckInfoCtrl',
            cache:false
        });

    $urlRouterProvider.otherwise('/search_student');
})
.factory("creditStorageService", function () {
        /**
         * 可以插入undefined  null 或者是对象
         * 对key值的保护，只返回string
         * @param key
         */
        var translateKeyToStr = function (key) {
            if (angular.isUndefined(key)) {
                return $rootScope.DEFAULT_KEY;
            } else if (angular.isObject(key) || angular.isArray(key) || angular.isNumber(+key || key)) {
                return angular.toJson(key) + $rootScope.path;
            }
            /*增加对空字符串的支持*/
            else if (angular.isString(key)) {
                if (key.length > 0) {
                    return key;
                } else {
                    return $rootScope.DEFAULT_KEY;
                }
            }
            /*增加对布尔值的支持*/
            else if (angular.isBoolean(key)) {
                return key + "";
            }
            return $rootScope.DEFAULT_KEY;
        };
        /**
         * 根据 key删除缓存的数据
         * @param key
         */
        var removeValue = function (key) {
            if (window && window.localStorage) {
                window.localStorage.removeItem(translateKeyToStr(key));
            }
        };
        /***
         * 保存数据和删除数据
         */
        return {
            getItem: function (key) {
                if (window && window.localStorage) {
                    var _$ = window.localStorage.getItem(translateKeyToStr(key));
                    removeValue(translateKeyToStr(key));
                    if (_$) {
                        return angular.copy(JSON.parse(_$));
                    }
                    return null;
                }
            },
            getItemNoDel: function (key) {
                if (window && window.localStorage) {
                    var _$ = window.localStorage.getItem(translateKeyToStr(key));
                    if (_$) {
                        return angular.copy(JSON.parse(_$));
                    }
                    return null;
                }
            },
            clearKey: function (key) {
                removeValue(translateKeyToStr(key));
            },
            setItem: function (key, value) {
                if (window && window.localStorage) {
                    if (value) {
                        window.localStorage.setItem(translateKeyToStr(key), JSON.stringify(value));
                    }
                }
            }
        }
    })
.directive("selectedStuViewer", function () {
        return {
            restrict: "AE",
            // scope: {
            //     param: "=",
            //     tkInfo: "=" /**听课信息*/
            // },
            // 设置模板路径
            templateUrl: function (tElement, tAttrs) {
                return "templates/moralityMeasurement/selectedStuViewer.html";
            },
            controller: ["$scope", "$http", "$filter", "creditStorageService", function ($scope, $http, $filter, creditStorageService) {
                $scope.selectedStuList = creditStorageService.getItemNoDel("credit_selectedStuList");
                if (!$scope.selectedStuList) {
                    $scope.selectedStuList = [];
                    creditStorageService.setItem("credit_selectedStuList", $scope.selectedStuList);
                }

                /**
                 * 移除已选中的学生
                 * @param id
                 */
                $scope.removeStu = function (platformSysUserId) {
                    var index = 0;
                    // 移除已选中学生
                    angular.forEach($scope.selectedStuList, function (selectedStu) {
                        if (selectedStu.platformSysUserId == platformSysUserId) {
                            $scope.selectedStuList.splice(index, 1);
                        }
                        index++;
                    });
                    // 如果有查询出来的学生
                    if ($scope.searchStuList) {
                        // 将移除的学生标记为未选中
                        angular.forEach($scope.searchStuList , function (stu) {
                            if (stu.platformSysUserId == platformSysUserId) {
                                stu.isSelected = false;
                            }
                        });
                    }
                    // 及时放入缓存
                    creditStorageService.setItem("credit_selectedStuList", $scope.selectedStuList);
                };
            }]
        };
    })
.controller('creditIndexController', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "creditStorageService", "$ionicLoading", "$ionicScrollDelegate","$cordovaBarcodeScanner",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, creditStorageService, $ionicLoading, $ionicScrollDelegate, $cordovaBarcodeScanner) {
        /**
         * 路由信息
         * @type {string}
         */
        $scope.basePath = originBaseUrl;
        // 是否显示已选中学生
        $scope.showSelectedStu = false;
        $scope.conditions = {
            numberOrName: null // 学号或姓名
        };
        $scope.locationStr = "left";
        $scope.selectedStuList = [];
        // 如果从菜单进入页面，则清空缓存
        if(!$location.search().fromInternal){
            creditStorageService.clearKey("credit_selectedStuList");
            creditStorageService.clearKey("credit_checkInfo");
            creditStorageService.clearKey("credit_checkItem");
            // 及时放入缓存
            creditStorageService.setItem("credit_selectedStuList", $scope.selectedStuList);
        }
        // 否则查询缓存
        else {
            $scope.selectedStuList = creditStorageService.getItemNoDel("credit_selectedStuList");
        }

        /**
         * 按学号或姓名查询
         * @param numberOrName
         */
        $scope.query = function (stuInfo) {
            $ionicLoading.show({
                template: "正在查询学生..."
            });
            $http.post(originBaseUrl + '/third/behavioRassess/searchStuByStuId.htm', {stuId: stuInfo["USERID"]}).success(function (data) {
                $ionicLoading.hide();
                if (data.status == 0) {
                    if (!data.result) {
                        ynuiNotification.warning({msg: "没有找到对应学生，请重新扫描学生牌！"});
                    } else {
                        var item = data.result;
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
                        var isExists = false;
                        angular.forEach($scope.selectedStuList, function (stu) {
                            if (stu.platformSysUserId == item.platformSysUserId) {
                                isExists = true;
                            }
                        });
                        if (!isExists) {
                            $scope.selectedStuList.unshift(item);
                            // 及时放入缓存
                            creditStorageService.setItem("credit_selectedStuList", $scope.selectedStuList);
                            $scope.showSelectedStu = true;
                        }
                    }
                } else {
                    ynuiNotification.error({msg: "查询学生失败！"});
                }
            }).error(function () {
                $ionicLoading.hide();
                ynuiNotification.error({msg: "查询学生失败！"});
            });
        };

        //扫描二维码
        $scope.scanCode = function () {
            $cordovaBarcodeScanner.scan().then(function (barcodeData) {
                var stuInfo = JSON.parse(barcodeData.text);
                $scope.query(stuInfo);
            }, function (error) {
                ynuiNotification.error({msg: "扫描失败！"});
            });
        };

        /**
         * 按条件查找
         */
        $scope.searchByCondition = function () {
            $location.path("/searchStuByCondition");

        };
        /**
         * 从班级中查找学生
         */
        $scope.searchStuByClass = function () {
            $location.path("/searchStuByClass");
        };

        /**
         * 开始考核
         */
        $scope.startCheck = function () {
            if ($scope.selectedStuList.length == 0) {
                ynuiNotification.warning({msg: "请先添加学生！"});
                return;
            }
            $location.path("/selectCheckItem").search("fromSrc", null);
        };


    }]);


