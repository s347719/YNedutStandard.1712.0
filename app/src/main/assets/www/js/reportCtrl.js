/**
 * Created by YN on 2016/4/26.
 */
angular.module('starter').controller('reportCtrl', ['$scope', '$timeout', 'receivePromiseService', 'reportUtilService', 'dataService', '$location',
    function ($scope, $timeout, receivePromiseService, reportUtilService, dataService, $location) {

        $scope.report = {
            queryValue: '',
            table_serach: false,
            table_content: [],
            isAdjust : true,
            pageNumber: 0,
            totalElements: 0
        };

        function initAttr() {
            $scope.currentTask = dataService.getcurrentData('currentTask');
            $scope.currentClass = dataService.getcurrentData('currentClass');
            $scope.report.table_content = [];
            $scope.report.totalElements = 0;
            $scope.report.pageNumber = 0;
        }

        function init() {
            initAttr();
            receivePromiseService.queryTasks($scope.report, 'tasks', reportUtilService.init)
                .then(function () {
                    if ($scope.report.tasks!= undefined && $scope.report.tasks.length > 0 && $scope.report.tasks[0].adminClassList.length > 0) {
                        receivePromiseService.queryStudents({
                            pageSize: 20,
                            pageNumber: 0,
                            reportTaskId: $scope.currentTask ? $scope.currentTask.id : $scope.report.tasks[0].id,
                            reportAdminClassId: $scope.currentClass ? $scope.currentClass.id : $scope.report.tasks[0].adminClassList[0].id
                        }).then(
                            function (data) {
                                $scope.report.totalElements = data.result.reportStudent.totalElements;
                                reportUtilService.initAttr(data.result.reportStudent.content, 'isChange', false);
                                $scope.report.table_content = _.union($scope.report.table_content, data.result.reportStudent.content);
                                $scope.report.statistic = data.result.statisticReport;
                                $timeout(function () {
                                    $scope.$broadcast('scroll.infiniteScrollComplete');
                                });
                            }
                        );
                        $scope.report.isAdjust = $scope.report.tasks[0].isAdjust;
                        $scope.currentTask = $scope.currentTask ? $scope.currentTask : $scope.report.tasks[0];
                        $scope.currentClass = $scope.currentClass ? $scope.currentClass : $scope.report.tasks[0].adminClassList[0];
                        dataService.setcurrentData('currentTask', $scope.currentTask);
                        dataService.setcurrentData('currentClass', $scope.currentClass);
                    }
                });
        };

        init();

        $scope.selectClass = function (t, c, aTask) {
            initAttr();
            if (Object.prototype.toString.apply(c) === "[object Object]") {
                dataService.setcurrentData('currentTask', t);
                dataService.setcurrentData('currentClass', c);
                reportUtilService.init(aTask);
                c.isSelect = true;
            }
            receivePromiseService.queryStudents({
                pageSize: 20,
                pageNumber: 0,
                reportTaskId: t.id,
                reportAdminClassId: c.id
            }).then(
                function (data) {
                    if (data.status === 0) {
                        $scope.currentTask = t;
                        $scope.currentClass = c;
                        $scope.report.table_content = [];
                        $scope.report.isAdjust = t.isAdjust;
                        $scope.report.totalElements = data.result.reportStudent.totalElements;
                        reportUtilService.initAttr(data.result.reportStudent.content, 'isChange', false);
                        $scope.report.table_content = _.union($scope.report.table_content, data.result.reportStudent.content);
                        statisticReport();
                        $timeout(function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                        });
                        $scope.$apply();
                    }
                }
            );
        };


        function refresh(number) {
            var reportTaskId = dataService.getcurrentData('currentTask').id,
                reportAdminClassId = dataService.getcurrentData('currentClass').id;
            return receivePromiseService.queryStudents({
                pageSize: 20,
                pageNumber: number ? number : 0,
                reportTaskId: reportTaskId,
                reportAdminClassId: reportAdminClassId,
                userInfo: $scope.report.queryValue
            });
        }

        function statisticReport() {
            var reportTaskId = dataService.getcurrentData('currentTask').id,
                reportAdminClassId = dataService.getcurrentData('currentClass').id;
            receivePromiseService.statisticReport({
                reportTaskId: reportTaskId,
                reportAdminClassId: reportAdminClassId
            }, $scope.report, 'statistic');
        }

        $scope._updateStatus = function (s, n) {
            if (n == 2) {
                receivePromiseService.updateStatus({
                    userIds: s.id,
                    status: n,
                    remark: ''
                })
                    .then(
                    function (data) {
                        if (data.status === 0) {
                            s.reportStatus = n;
                            s.isChange = !s.isChange;
                            statisticReport();
                        }
                    }
                );
            }
            if (n == 3) {
                $scope.report.remark = s.remark ? s.remark : '';
                reportUtilService.showPopup($scope, function (remark) {
                    receivePromiseService.updateStatus({
                        userIds: s.id,
                        status: n,
                        remark: remark
                    })
                        .then(
                        function (data) {
                            if (data.status === 0) {
                                s.reportStatus = n;
                                s.isChange = !s.isChange;
                                statisticReport();
                            }
                        }
                    )
                });

            }
        };

        $scope.updateAllStatus = function (arr) {
            var users = reportUtilService.filterArrByAttr(arr, 'reportStatus', '1');
            var userIds = reportUtilService.getValuesByAttr(users, 'id');

            receivePromiseService.updateStatus({
                userIds: userIds,
                status: 2,
                remark: ''
            })
                .then(
                function (data) {
                    if (data.status === 0) {
                        refresh();
                        reportUtilService.initAttr(users, 'reportStatus', '2');
                        statisticReport();
                    }
                }
            );

        }



        $scope.changePanel = function (s, arr) {
            reportUtilService.initAttr(arr, 'isChange', false);
            s.isChange = true;
        };


        $scope.to_serach = function () {
            $scope.report.table_serach = true;
            $scope.report.queryValue = '';
            initAttr();
        };

        $scope.to_add_stu = function () {
            $location.path("/add_report_stus");
        }

        $scope.serachByInfo = function () {
            initAttr();
            var promise = refresh();
            promise.then(
                function (data) {
                    $scope.report.totalElements = data.result.reportStudent.totalElements;
                    reportUtilService.initAttr(data.result.reportStudent.content, 'isChange', false);
                    $scope.report.table_content = _.union($scope.report.table_content, data.result.reportStudent.content);
                    $scope.report.statistic = data.result.statisticReport;
                    $timeout(function () {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                    });
                }
            )
            return promise;
        };

        $scope.clearInfo = function (attr) {
            $scope.report[attr] = '';

        };

        $scope.cancel = function () {
            $scope.report.queryValue = '';
            $scope.report.table_serach = false;
            $scope.report.table_content = [];
            $scope.report.pageNumber = 0;
            $scope.report.totalElements = 0;
            init();
        };

        $scope.doFresh = function () {
            dataService.setcurrentData('currentTask', '');
            dataService.setcurrentData('currentClass', '');
            init();
        }


        $scope.loadMore = function () {
            if ($scope.report.totalElements && $scope.report.totalElements !== $scope.report.table_content.length) {
                $scope.report.pageNumber = $scope.report.pageNumber + 1;
                refresh($scope.report.pageNumber).then(
                    function (data) {
                        if (data.status === 0) {
                            $scope.report.totalElements = data.result.reportStudent.totalElements;
                            $scope.report.table_content = _.union($scope.report.table_content, data.result.reportStudent.content);
                            $timeout(function () {
                                $scope.$broadcast('scroll.infiniteScrollComplete');
                            });
                        }
                    }
                );
            }
        };

        $scope.$on('$stateChangeSuccess', function () {
            $scope.loadMore();
        })

    }
]).
    directive('inputTimerSerach',['$ionicLoading',
        function ($ionicLoading) {
            return {
                restrict : 'A',
                scope : {
                    onSend : '&',
                    times : '@',
                    bshade : '@',
                    shadetimes : '@'
                },
                link : function (scope, element, attrs) {
                    var last;
                    scope.times = scope.times || 1000;
                    scope.shadetimes = scope.shadetimes || 500;
                    $(element[0]).keyup(function (event) {
                        last = event.timeStamp;
                        setTimeout(function () {
                            if (last - event.timeStamp == 0) {
                                if (!scope.bshade) {
                                    scope.onSend();
                                }else{
                                    $ionicLoading.show({
                                        content: 'Loading',
                                        animation: 'fade-in',
                                        showBackdrop: true,
                                        maxWidth: 200,
                                        showDelay: 0
                                    });
                                    scope.onSend().then(
                                        function (data) {
                                            setTimeout(function () {
                                                $ionicLoading.hide();
                                            },scope.shadetimes);
                                        }
                                    );
                                }
                            }
                        },scope.times);
                    });
                }
            }
        }
    ]);