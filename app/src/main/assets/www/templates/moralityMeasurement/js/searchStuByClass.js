/**
 * 操行分考核按班级查找学生控制器
 * Created by wangl on 2017/08/09 10:33
 *
 */
angular.module('starter').controller('searchStuByClassCtrl', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "$timeout", "ynuiNotification", "creditStorageService", "$ionicLoading", "$ionicScrollDelegate",
    function ($scope, $http, $location, $state, $ionicModal, $filter, $timeout, ynuiNotification, creditStorageService, $ionicLoading, $ionicScrollDelegate) {
        // 已选中的学生
        $scope.selectedStuList = creditStorageService.getItemNoDel("credit_selectedStuList");
        // 之前的过滤条件
        var oldParam;
        // 如果从考核项目页面跳转至此页面
        var isFromItemPage = $location.search().isFromItemPage;
        if (isFromItemPage) {
            oldParam = creditStorageService.getItemNoDel("credit_oldSearchByClassParam");
        }

        $scope.locationStr = "center";
        // 模态框控制
        $ionicModal.fromTemplateUrl('select_grade.html', {
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function (modal) {
            $scope.modal = modal;
        });
        $scope.selectGradeClick = function () {
            $scope.modal.show();
        };
        $scope.closeSelectGrade = function () {
            $scope.modal.hide();
        };



        // 近5年年级
        $scope.gradeList = [];

        // 机构集
        $scope.orgList = [];

        // 是否为第一次加载
        var isFirstLoad = true;

        // 选中的年级
        $scope.selectedGrade = {};
        $scope.selectedGradeTemp = null;

        // 选中的机构
        $scope.selectedOrg = {};

        // 行政班集
        $scope.adminClassList = [];

        // 选中的行政班
        $scope.selectedAdminClass = null;

        // 行政班下的学生集
        $scope.searchStuList = [];

        $scope.page = {};

        var param = {
            gradeId: null,
            orgNo: null,
            pageNumber: 0,
            pageSize: 20
        };

        /**
         *  获取年级、机构下的行政班
         */
        var getAdminClassUnderGradeAndOrg = function () {
            $scope.page = {};
            param.gradeId = $scope.selectedGrade.id;
            param.orgNo = $scope.selectedOrg.id;
            $scope.selectedAdminClass = null;
            $scope.searchStuList = [];
            $http.post(originBaseUrl + '/third/behavioRassess/getAdminClassUnderGradeAndOrg.htm', param).success(function (data) {
                $scope.page = null;
                if (data.status == 0) {
                    $scope.page = data.result;
                    // 缓存的有滚动之前的内容
                    if ($scope.adminClassList.length > 0) {
                        angular.forEach($scope.page.content, function(item){
                            $scope.adminClassList.push(item);
                        });
                    } else {
                        angular.forEach($scope.page.content, function(item){
                            $scope.adminClassList.push(item);
                        });
                    }
                }
                $scope.$broadcast('scroll.infiniteScrollComplete');
            }).error(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
            });
        };

        /**
         * 选中机构
         * @param grade
         */
        $scope.selectOrg = function (org) {
            $scope.selectedOrg = org;
            $scope.selectedGrade = angular.copy($scope.selectedGradeTemp);
            $scope.adminClassList = [];
            param.pageNumber = 0;
            getAdminClassUnderGradeAndOrg();
            $scope.closeSelectGrade();
        };

        /**
         * 汇总年级下在读班级的机构
         */
        var getAdminClassOrgs = function () {
            $http.post(originBaseUrl + '/third/behavioRassess/getAdminClassOrgs.htm', {gradeId: $scope.selectedGradeTemp.id}).success(function (data) {
                if (data.status == 0) {
                    $scope.orgList = data.result;
                    if (!isFromItemPage) {
                        if(data.result && data.result.length > 0) {
                            if (isFirstLoad) {
                                // 默认选中第一个
                                $scope.selectOrg(data.result[0]);
                            }
                        } else {
                            $scope.selectOrg({});
                        }
                    } else {
                        $scope.selectOrg(oldParam.selectedOrg);

                    }
                    isFromItemPage = false;
                }
                isFirstLoad = false;
            });
        };

        /**
         * 选中年级
         * @param grade
         */
        $scope.selectGrade = function (grade) {
            $scope.selectedGradeTemp = grade;
            getAdminClassOrgs();
        };

        /**
         * 获得近5年有效的年级
         */
        var getNearlyFiveYearsGrades = function () {
            $http.get(originBaseUrl + '/third/behavioRassess/getNearlyFiveYearsGrades.htm').success(function (data) {
                if (data.status == 0) {
                    $scope.gradeList = data.result;
                    if(data.result && data.result.length > 0) {
                        // 如果有缓存
                        if (isFromItemPage) {
                            $scope.selectGrade(oldParam.selectedGrade);
                        } else {
                            // 默认选中第一个
                            $scope.selectGrade(data.result[0]);
                        }
                    }
                }
            });
        };
        getNearlyFiveYearsGrades();

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

        /**
         * 获取行政班下的学生
         */
        var getStuUnderAminClass = function () {
            $http.post(originBaseUrl + '/third/behavioRassess/getStuUnderAminClass.htm', {adminClassId:$scope.selectedAdminClass.id}).success(function (data) {
                if (data.status == 0) {
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
                    $scope.searchStuList = data.result;
                    initSelectedFlag($scope.selectedStuList, $scope.searchStuList);
                }
            });
        };

        /**
         * 选中行政班
         * @param adminClass
         */
        $scope.selectAdminClass = function (adminClass) {
            $scope.selectedAdminClass = adminClass;
            getStuUnderAminClass();
        };

        // 是否显示已选中学生
        $scope.showSelectedStu = false;
        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };
        $scope.tips = initTips();

        /**
         * 加载更多
         */
        $scope.loadMore = function () {
            param.pageNumber++;
            if (param.pageNumber >= $scope.page.totalPages) {
                return false;
            }
            getAdminClassUnderGradeAndOrg();
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
            $location.search("isFromItemPage", false);
            creditStorageService.clearKey("credit_oldSearchByClassParam");
            $location.path("/search_student").search("fromInternal", true);;
        };

        /**
         * 开始考核
         */
        $scope.startCheck = function () {
            if ($scope.selectedStuList.length == 0) {
                ynuiNotification.warning({msg: "请先添加学生！"});
                return;
            }
            $location.path("/selectCheckItem").search({"fromSrc": "byClass"});
            oldParam = {};
            oldParam.selectedGrade = $scope.selectedGrade;
            oldParam.selectedOrg = $scope.selectedOrg;
            creditStorageService.setItem("credit_oldSearchByClassParam", oldParam);
        };
    }]);


