/**
 *  批量审批流程
 * Created by wangl on 2017/12/13 09:39
 *
 */
angular.module('starter')
    .config(function ($stateProvider) {
        $stateProvider.state(
            'batchApproval',
            {
                url: '/batchApproval',
                templateUrl: 'templates/lcapproval/batch-approval.html',
                controller: 'batchApprovalCtrl',
                cache: false
            }
        );
    })
    .service('batchApprovalService', function ($http, $q) {
        var version = 'V1.0';
        return {
            getOANodes: function (defKey) {
                var deferred = $q.defer();
                var promise = deferred.promise;
                $http.post(originBaseUrl + "/third/mobileProc/queryMobileToDoOANodes.htm", {
                    defKey: defKey,
                    version: version
                }).success(function (data) {
                    deferred.resolve(data);
                });
                return promise;
            },
            getMyToDo: function (formSource, proId, nodeId, defKey) {
                var deferred = $q.defer();
                var promise = deferred.promise;
                $http.post(originBaseUrl + "/third/mobileProc/queryMobileProcToDoTasks.htm", {
                    formSource: formSource,
                    procId: proId,
                    nodeId: nodeId,
                    defKey: defKey,
                    version: version
                }).success(function (data) {
                    deferred.resolve(data);
                });
                return promise;

            },
            batchSumbitCheck: function (formSource, procId, nodeId, procinstIdAndTaskIds, taskIds, checkComment) {
                var deferred = $q.defer();
                var promise = deferred.promise;
                $http.post(originBaseUrl + "/third/mobileProc/batchSumbitCheck.htm", {
                    formSource: formSource,
                    procId: procId,
                    nodeId: nodeId,
                    procinstIdAndTaskIds: procinstIdAndTaskIds,
                    taskIds: taskIds,
                    checkComment: checkComment,
                    version: version
                }).success(function (data) {
                    deferred.resolve(data);
                });
                return promise;
            }
        };
    })
    .controller(
        'batchApprovalCtrl',
        [
            '$http',
            '$scope',
            '$rootScope',
            '$location',
            '$ionicModal',
            'ynuiNotification',
            'batchApprovalService',
            function ($http, $scope, $rootScope, $location, $ionicModal, ynuiNotification, batchApprovalService) {
                // 获取url上的参数,外部传入的参数
                $scope.externalParam = {
                    formSource: $location.search().formSource,   // 流程来源：0 宏天OA   1：V8 2：SMEsis
                    proId: $location.search().proId,        // 流程ID
                    procDefName: $location.search().procDefName,       // 流程名
                    isFromOA: $location.search().formSource == 0,
                    defKey: $location.search().proId // 流程定义key
                };

                // 页面参数
                $scope.pageParam = {
                    isShowOANode: false,    // 是否显示OA流程节点
                    selectedNode: null,     // 选中的节点
                    checkedAll: false,      // 是否全选
                };


                // 提示信息
                $scope.tips = new Tips(false);


                // 宏天OA流程所关联的流程节点
                $scope.nodes = [];

                // 数据
                $scope.items = [];

                $scope.init = function () {
                    $scope.nodes = [];
                    // 数据
                    $scope.items = [];
                    // 如果为宏天OA
                    if ($scope.externalParam.formSource == 0) {
                        getOANodes();
                    } else {
                        getMyToDo();
                    }
                }

                $scope.init();


                /**
                 * 点击流程节点
                 * @param node OA流程节点
                 */
                $scope.clickNode = function (nodeId) {
                    $scope.pageParam.selectedNode = nodeId;
                    getMyToDo();
                };

                /**
                 * 点击重试
                 */
                $scope.reTry = function () {
                    $scope.init();
                };

                /**
                 * 批量审核
                 */
                $scope.batchApproval = function () {
                    var checkedLCIds = [];
                    angular.forEach($scope.items, function (item) {
                        if (item.isChecked) {
                            checkedLCIds.push(item.id);
                        }
                    });
                    if (checkedLCIds.length == 0) {
                        ynuiNotification.warning({msg: "请至少选中一项!"});
                        return;
                    }


                };


                /**
                 * 页面提示信息
                 * @param isShow 是否显示
                 * @param isError 是否有错误
                 * @param isNoData 是否有数据
                 * @constructor
                 */
                function Tips(isShow, isError, isNoData) {
                    this.isShow = isShow;
                    this.isError = isError;
                    this.isNoData = isNoData;
                }

                /**
                 * 获取宏天OA流程所关联的流程节点
                 */
                function getOANodes() {
                    $scope.pageParam.selectedNode = null;
                    $scope.tips = new Tips(false);
                    batchApprovalService.getOANodes($scope.externalParam.defKey).then(function (data) {
                        if (data.status == 0) {
                            $scope.nodes = data.result;
                            if ($scope.nodes.length == 0) {
                                $scope.tips = new Tips(true, false, true);
                            } else {
                                // 如果只有一个节点，则不显示
                                $scope.pageParam.isShowOANode = $scope.nodes.length != 1;
                                $scope.pageParam.selectedNode = $scope.nodes[0].nodeId;
                                getMyToDo();
                            }
                        } else {
                            $scope.tips = new Tips(true, true, false);
                        }

                    });
                }


                // 已选中的数量
                $scope.checkedNum = 0;

                /**
                 * 获取待审核任务
                 * @param proId 流程ID
                 * @param nodeId 流程节点ID
                 */
                function getMyToDo() {
                    $scope.tips = new Tips(false);
                    $scope.items = [];
                    $scope.pageParam.checkedAll = true;
                    $scope.checkedNum = 0;
                    batchApprovalService.getMyToDo(
                        $scope.externalParam.formSource,
                        $scope.externalParam.proId,
                        $scope.pageParam.selectedNode,
                        $scope.externalParam.defKey
                    ).then(function (data) {
                        if (data.status == 0) {
                            angular.forEach(data.result, function (item) {
                                item.isChecked = true;
                                $scope.items.push(item);
                            })
                            $scope.checkedNum = $scope.items.length;
                            if ($scope.items.length == 0) {
                                $scope.tips = new Tips(true, false, true);
                            }
                        } else {
                            $scope.tips = new Tips(true, true, false);
                        }

                    });
                }

                /****************************************全选、全不选处理开始*******************************************/

                /**
                 * 全选/全不选
                 */
                $scope.checkOrUnCheckAll = function () {
                    angular.forEach($scope.items, function (item) {
                        item.isChecked = $scope.pageParam.checkedAll;
                        $scope.checkedNum = $scope.pageParam.checkedAll ? $scope.items.length : 0;
                    });
                };

                /**
                 * 选中、不选中每条数据
                 * @param isCheck 是否选中
                 */
                $scope.checkOrUnCheckEach = function (isChecked) {
                    // 如果为选中状态，则已选中的数量加1，否则减一
                    $scope.checkedNum = isChecked ? ++$scope.checkedNum : --$scope.checkedNum;
                    // 如果则已选中的数量等于总数，则选中全选按钮，否则不选中
                    $scope.pageParam.checkedAll = ($scope.checkedNum == $scope.items.length);
                };
                /****************************************全选、全不选处理结束*******************************************/

            }
        ]
    )
    .directive("batchApproval", function ($ionicPopup, $ionicLoading, ynuiNotification, batchApprovalService) {
        return {
            restrict: 'A',
            link: function ($scope, ele, attrs) {
                // 参数信息
                $scope.popParam = {
                    checkComment: null, // 审核意见
                    checkedItemIds: [],  // 选中的项目ID
                    taskIds: [] // 任务实例ID
                };

                // 点击“批量审批”
                ele.click(function () {
                    if (haveCheckedItems()) {
                        $ionicPopup.show({
                            title: "批量审批",
                            templateUrl: 'batch-approval-modal.html',
                            scope: $scope,
                            buttons: [
                                {text: '取消'},
                                {
                                    text: '<b>提交</b>',
                                    type: 'button-positive',
                                    onTap: function (e) {
                                            return true;
                                    }
                                }
                            ]
                        }).then(function (res) {
                            $ionicLoading.hide();
                            if (res) {
                                submit();
                            }
                        });
                    }
                });

                /**
                 * 是否有选中的项目
                 * @returns {boolean}
                 */
                function haveCheckedItems() {
                    $scope.popParam.checkedItemIds = [];
                    $scope.popParam.taskIds = [];
                    angular.forEach($scope.items, function (item) {
                        if (item.isChecked) {
                            $scope.popParam.checkedItemIds.push(item.id + ":" + item.taskId);
                            $scope.popParam.taskIds.push(item.taskId);
                        }
                    });
                    if ($scope.popParam.checkedItemIds.length == 0) {
                        ynuiNotification.warning({msg: "请至少选中一项!"});
                        return false;
                    }
                    return true;
                }


                /**
                 * 提交
                 */
                function submit() {

                    batchApprovalService.batchSumbitCheck(
                        $scope.externalParam.formSource,
                        $scope.externalParam.proId,
                        $scope.pageParam.selectedNode,
                        $scope.popParam.checkedItemIds,
                        $scope.popParam.taskIds.join(","),
                        $scope.popParam.checkComment
                    ).then(function (data) {
                        if (data.status == 0) {
                            clearData();
                            // 调用控制器中的初始数据方法
                            $scope.init();
                        } else {
                            ynuiNotification.error({msg: data.message});
                        }
                    });
                }

                /**
                 * 清空数据
                 */
                function clearData() {
                    $scope.popParam.checkComment = null;
                    $scope.popParam.checkedItemIds = [];
                }
            }
        };
    })


