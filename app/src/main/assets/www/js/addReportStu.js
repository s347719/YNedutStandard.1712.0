/**
 * Created by YN on 2016/4/28.
 */
angular.module('starter').controller('addReportStu', ['$scope', '$location', '$state', '$timeout', 'receivePromiseService', 'reportUtilService', 'dataService',
    function ($scope, $location, $state, $timeout, receivePromiseService, reportUtilService, dataService) {

        $scope.report = {
            table_serach: false,
            table_content: [],
            selected: [],
            enrolls: [],
            pageNumber: 0,
            totalElements: 0,
            gradeId: '',
            enrollType: '',
            queryValue: ''
        };

        function initAttr() {
            $scope.report.table_content = [];
            $scope.report.selected = [];
            $scope.report.totalElements = 0;
            $scope.report.pageNumber = 0;
        }

        function getGrade() {
            receivePromiseService.getGrade($scope.report, 'grades')
                .then(
                function () {
                    reportUtilService.initAttr($scope.report.grades, 'isClick', false);
                    dataService.getEnrolls($scope.report);
                    $scope.report.currentEnroll = $scope.report.enrolls[0];
                }
            );
        };

        getGrade();

        function queryNoClassStu(num) {
            initAttr();
            return receivePromiseService.queryNoClassStu({
                pageSize: 20,
                pageNumber: num ? num : 0,
                gradeId: $scope.report.gradeId,
                enrollType: $scope.report.enrollType,
                queryValue: $scope.report.queryValue
            }).then(
                function (data) {
                    if (data.status == 0) {
                        $scope.report.totalElements = data.result.totalElements;
                        $scope.report.table_content = _.union($scope.report.table_content, data.result.content);
                        reportUtilService.initAttr($scope.report.table_content, 'isClick', false);
                        $timeout(function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                        });
                    }
                }
            );
        };

        queryNoClassStu();

        function refresh(num) {
            return receivePromiseService.queryNoClassStu({
                pageSize: 20,
                pageNumber: num ? num : 0,
                gradeId: $scope.report.gradeId,
                enrollType: $scope.report.enrollType,
                queryValue: $scope.report.queryValue
            });
        };

        $scope.loadNoClassMore = function () {
            if ($scope.report.totalElements && $scope.report.totalElements !== $scope.report.table_content.length) {
                $scope.report.pageNumber = $scope.report.pageNumber + 1;
                refresh($scope.report.pageNumber).then(
                    function (data) {
                        if (data && data.status === 0) {
                            $scope.report.totalElements = data.result.totalElements;
                            $scope.report.table_content = _.union($scope.report.table_content, data.result.content);
                            $timeout(function () {
                                $scope.$broadcast('scroll.infiniteScrollComplete');
                            });
                        }
                    }
                );
            }
        };

        $scope.$on('$stateChangeSuccess', function () {
            $scope.loadNoClassMore();
        })

        $scope.changeEnroll = function () {
            reportUtilService.rotateEnrollView($scope.report.enrolls);
            $scope.report.enrollType = $scope.report.enrolls[0].id;
            queryNoClassStu();
        }

        $scope.changeGrade = function (g) {
            reportUtilService.initAttr($scope.report.grades, 'isClick', false);
            if (Object.prototype.toString.apply(g) === "[object Object]") {
                g.isClick = true;
                $scope.report.gradebar = g.name + '未分班学生';
                $scope.report.gradeId = g.id;
            } else {
                $scope.report.gradeId = '';
                $scope.report.gradebar = '所有未分班学生';
            }
            queryNoClassStu();
        };

        $scope.selectNoClassStu = function (s) {
            s.isClick = !s.isClick;
            $scope.report.selected = reportUtilService.filterArrByAttr($scope.report.table_content, 'isClick', true);
        }

        $scope.changeAll = function (b) {
            if (b) {
                reportUtilService.initAttr($scope.report.table_content, 'isClick', false);
            } else {
                reportUtilService.initAttr($scope.report.table_content, 'isClick', true);
            }
            $scope.report.selected = reportUtilService.filterArrByAttr($scope.report.table_content, 'isClick', true);
        };

        $scope.to_serach = function () {
            $scope.report.table_serach = true;
            $scope.report.queryValue = '';
            initAttr();
        };

        $scope.clearInfo = function (attr) {
            $scope.report[attr] = '';
        };

        $scope.doFresh = function () {
            getGrade();
            queryNoClassStu();
        }

        $scope.serachByInfo = function () {
            initAttr();
            var promise = refresh();
            promise.then(
                function (data) {
                    if (data.status === 0) {
                        $scope.report.totalElements = data.result.totalElements;
                        $scope.report.table_content = _.union($scope.report.table_content, data.result.content);
                        $timeout(function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                        });
                    }
                }
            )
            return promise;
        }

        $scope.cancel = function () {
            $scope.report.queryValue = '';
            $scope.report.table_serach = false;
            $scope.report.table_content = [];
            $scope.report.pageNumber = 0;
            $scope.report.totalElements = 0;
            queryNoClassStu();
        };


        $scope.to_last_page = function () {
            $state.go("report_detail",{},{ reload: true });
        }

        $scope.addStudent = function () {
            $scope.report.selected = reportUtilService.filterArrByAttr($scope.report.table_content, 'isClick', true);
            if ($scope.report.selected.length === 0) {
                $location.path("/report_detail");
            } else {
                var userIds = reportUtilService.getValuesByAttr($scope.report.selected, 'id');
                var reportTaskId = dataService.getcurrentData('currentTask').id,
                    reportAdminClassId = dataService.getcurrentData('currentClass').id;
                receivePromiseService.addStudent({
                    userIds: userIds,
                    moraleduReportAdminClassId: reportAdminClassId,
                    moraleduReportTaskId: reportTaskId
                }).then(
                    function (data) {
                        if (data.status == 0) {
                            reportUtilService.showMsg('添加成功！');
                            $timeout(function () {
                                $state.go("report_detail",{},{ reload: true });
                            },500);
                        } else {
                            reportUtilService.showMsg("添加失败！");
                        }
                    }
                );
            }
        };

    }
]);