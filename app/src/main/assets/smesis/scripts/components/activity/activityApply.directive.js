/**
 * Project: yineng-corpSysLand
 * Package
 * Title: fileName
 * author xiechangwei
 * date 2017/5/9 16:31
 * Copyright: 2017 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
angular.module('myApp')
    .directive('activityApply', ['$http', function ($http) {
        var template = '<ion-footer-bar class="bar-up-shadow">' +
            '<div class="row">' +
            ' <div class="col text-center btn-group-wrap"> ' +
            '<button class="button button-theme button-local button-outline" type="button" ng-click="options.button.cancel()">返回</button>' +
            '<button class="button button-theme button-local" type="button" subbutt ng-click="urge()" ng-if="auth.urge">催办</button>' +
            '<button class="button button-theme button-local" type="button" subbutt ng-click="startProcess(options.params)" ng-if="auth.startProcess" >提交审批</button>' +
                //'<button class="button button-theme button-local" type="button" ng-click="options.button.saveDraft()" ng-if="auth.saveDraft">保存草稿</button>' +
            '<button class="button button-theme button-local" type="button" ng-click="repeal()" ng-if="auth.repeal" ng-if="auth.repeal">撤销</button>' +
                //'<button class="button button-theme button-local" type="button" ng-click="options.button.deleteDraft()" ng-if="auth.deleteDraft">删除草稿</button>' +
            '<button class="button button-theme button-local" type="button" ng-click="options.button.deleteProcess()" ng-if="auth.deleteProcess" >删除</button>' +
            '</div>' +
            '</div>' +
            '</ion-footer-bar>';
        return {
            scope: {
                options: '='
            },
            template: template,
            controller: ['$scope', '$ionicModal', '$ionicLoading', '$ionicPopup', function ($scope, $ionicModal, $ionicLoading, $ionicPopup) {

                //根据实例id获取当前任务节点Id
                $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProcessInstanceIdByEveryOneForCurrentTask?processInstanceId=" + $scope.options.instanceId).success(function (data) {
                    if (data.status == 0) {
                        //当前任务节点ID
                        $scope.currentTaskId = data.result;
                    }
                });

                /******************* 默认按钮事件 START******************************/

                //1-提交审批,2--同意,3--反对,4--驳回发起人,5--交办,6--驳回上一步,7--撤销
                //这几个按钮的公用方法
                var backProcess = function (isToStart, processState, callback) {

                    var params = $.extend(
                        {}, $scope.options.params,
                        {
                            businessId: $scope.options.businessId,
                            instanceId: $scope.options.instanceId,
                            isToStart: isToStart,
                            processState: processState
                        }, $scope.options.globalParams
                    );

                    if (callback) {
                        callback(params);
                    } else {
                        //$http.post(basePath + "/khglActivity/backProcess", null, {
                        //    params: params
                        //}).success(function (data) {
                        //    $scope.options.buttonAction();
                        //});
                    }
                };


                /**
                 * 默认的驳回方法，主要是大客户那块
                 * @param params
                 */
                var defaultBackProcess = function (params) {
                    //$http.post(basePath + "/khglActivity/backProcess", null, {
                    //    params: params
                    //}).success(function (data) {
                    //    $scope.options.buttonAction();
                    //});
                };

                //交办
                var assign = function () {

                    ynuiSelectorService.userSelector(function (item) {

                        var params = $.extend({
                            businessId: $scope.options.businessId,
                            instanceId: $scope.options.instanceId,
                            nextAssignee: item.id
                        }, $scope.options.buttonParams.assign);
                        //$http.post(basePath + "/khglActivity/transferAssigneeBusiness", null, {
                        //    params: params
                        //}).success(function (data) {
                        //    $scope.options.buttonAction();
                        //});
                    }, {gridOptions: {useSingleChoice: true}});

                };

                //消息提示
                $scope.notifyErrorMsg = function (msg) {
                    $ionicLoading.show({
                        template: msg
                    });
                    setTimeout(function () {
                        $ionicLoading.hide();
                    }, 2000);
                }

                //催办
                $scope.urge = function () {
                    $http.get(basePath + "/third/xtbgactivity/sendActivityRemindersMessage", {
                        params: {
                            taskId: $scope.currentTaskId
                        }
                    }).success(function (data) {
                        $scope.notifyErrorMsg(data.message);
                    });
                };
                //驳回发起人
                var reject = function () {
                    backProcess(true, 4, $scope.options.button.reject);
                };
                //驳回上一步
                var rejectPreNode = function () {
                    backProcess(false, 6, $scope.options.button.rejectPreNode);
                };
                //保存草稿
                var saveDraft = function () {

                };
                //撤销
                var repeal = function () {
                    backProcess(true, 7, $scope.options.button.repeal);
                };
                //删除草稿
                var deleteDraft = function () {

                };
                //删除流程
                var deleteProcess = function () {

                };


                /******************* 默认按钮事件 END******************************/
                //默认配置
                var defaultOptions = {

                    //如果此3个状态都存在，则会自动判断用户的按钮权限
                    //当前流程实例Id
                    instanceId: null,
                    //申请人Id
                    applyId: null,
                    //流程状态
                    processStatus: null,
                    //业务表Id
                    businessId: null,
                    //流程定义Key
                    definitionKey: null,
                    //取得当前的用户有哪些按钮点击权限
                    getUserAuth: getUserAuth,
                    //当前任务节点Id
                    currentTaskId: null,
                    //按钮
                    button: {
                        reject: defaultBackProcess,
                        rejectPreNode: defaultBackProcess,
                        saveDraft: saveDraft,
                        repeal: defaultBackProcess,
                        deleteDraft: deleteDraft,
                        deleteProcess: deleteProcess,
                        //approveHistory: approveHistory,
                        assign: assign,
                        cancel: function () {
                            if ($scope.options.buttonAction) {
                                $scope.options.buttonAction();
                            } else {
                                yn.plugin.yncordova.close();
                            }
                        }
                    },
                    //采用默认按钮，对应按钮的默认参数
                    buttonParams: {},
                    //页面的 审批意见这些参数
                    params: {},
                    //使用默认点击事件，点击后的操作
                    buttonAction: null
                };

                //将使用者转入的参数对象与默认参数合并
                $scope.options = $.extend(true, defaultOptions, $scope.options);

                /**
                 * 根据身份，流程状态的不同设置按钮的控制
                 * @param coordinate
                 */
                var initAuthButton = function (coordinate) {
                    //根据type的不同控制按钮的隐藏，展示
                    //用户身份:0发起人，1:当前审批人(当前任务执行人)，2:发起人和当前审批人(当前任务执行人)
                    //流程进度:0为未提交，1为结束，2为返回修改，3为进行中
                    //特殊处理:-1新增有权限，-2新增无权限/未提交无权限，-3未提交有权限，1不显示审批意见文本域，2非发起人和非当前审批人，c值优先级高于a,b
                    //是否显示下一节点处理人按钮:1是，0否
                    //是否显示下个节点选择:1是，0否
                    //如果coordinate为空，则 非发起人或审批人在查看流程时，只有：流程图、审批历史

                    //除了1,0 为无，0,0，2,0没有审批历史，其他都有流程图和审批历史
                    //流程图，和审批历史 默认是开启
                    $scope.auth.chart = true;
                    $scope.auth.approveHistory = true;
                    $scope.auth.comment = true;

                    if (!coordinate) {
                        return;
                    }
                    if (coordinate.length != 4) {

                    }
                    var identity = coordinate[0];
                    var status = coordinate[1];
                    var specialty = coordinate[2];
                    $scope.auth.nextUser = coordinate[3];
                    $scope.auth.nextNode = coordinate[4];

                    //0,0(未提交) 提交审批、保存草稿、删除草稿、流程图、取消
                    if (identity == 0 && status == 0) {
                        $scope.auth.startProcess = true;
                        $scope.auth.saveDraft = true;
                        $scope.auth.deleteDraft = true;
                        $scope.auth.approveHistory = false;
                        $scope.auth.cancel = true;
                        $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByActivityKey?", null, {params: {processDefinitionKey: $scope.options.definitionKey}}).success(function (data) {
                            $scope.nodeNameList = data.result;
                        });
                    }
                    //1,0(未提交) 无
                    if (identity == 1 && status == 0) {
                        $scope.auth.chart = false;
                        $scope.auth.approveHistory = false;
                    }
                    //2,0(未提交) 提交审批、保存草稿、删除草稿、流程图
                    if (identity == 2 && status == 0) {
                        $scope.auth.startProcess = true;
                        $scope.auth.saveDraft = true;
                        $scope.auth.deleteDraft = true;
                        $scope.auth.approveHistory = false;
                        $scope.auth.cancel = true;
                        $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByActivityKey?", null, {params: {processDefinitionKey: $scope.options.definitionKey}}).success(function (data) {
                            $scope.nodeNameList = data.result;
                        });
                    }
                    //0,1(结束)  流程图、审批历史、转发(未设置则没有)
                    if (identity == 0 && status == 1) {
                    }
                    //1,1(结束) 流程图、审批历史、转发(未设置则没有)
                    if (identity == 1 && status == 1) {
                    }
                    //2,1(结束) 流程图、审批历史、转发(未设置则没有)
                    if (identity == 2 && status == 1) {
                    }
                    //0,2(为返回修改) 提交审批、删除流程、流程图、审批历史
                    if (identity == 0 && status == 2) {
                        $scope.auth.startProcess = true;
                        $scope.auth.deleteProcess = true;
                        $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByActivityKey?", null, {params: {processDefinitionKey: $scope.options.definitionKey}}).success(function (data) {
                            $scope.nodeNameList = data.result;
                        });
                    }
                    //1,2(为返回修改) 流程图、审批历史
                    if (identity == 1 && status == 2) {

                    }
                    //2,2(为返回修改) 提交审批、删除流程、流程图、审批历史
                    if (identity == 2 && status == 2) {
                        $scope.auth.startProcess = true;
                        $scope.auth.deleteProcess = true;
                        $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByActivityKey?", null, {params: {processDefinitionKey: $scope.options.definitionKey}}).success(function (data) {
                            $scope.nodeNameList = data.result;
                        });
                    }
                    //0,3(为进行中) 撤消、催办、流程图、审批历史
                    if (identity == 0 && status == 3) {
                        $scope.auth.repeal = true;
                        $scope.auth.urge = true;
                    }
                    //1,3(为进行中) 同意、交办、驳回发起人、驳回上一步、流程图、审批历史
                    if (identity == 1 && status == 3) {
                        $scope.auth.agree = true;
                        $scope.auth.assign = true;
                        $scope.auth.reject = true;
                        $scope.auth.rejectPreNode = true;
                        //根据实例id获取当前任务节点Id
                        $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProInstdForCurTask?processInstanceId=" + $scope.options.instanceId).success(function (data) {
                            if (data.status == 0) {
                                //当前任务节点ID
                                $scope.currentTaskId = data.result;
                                //根据当前任务节点ID获取可以选择的下一节点名称
                                if ($scope.currentTaskId != null) {
                                    $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByTaskId?", null, {params: {taskId: $scope.currentTaskId}}).success(function (data) {
                                        $scope.nodeNameList = data.result;
                                    });
                                }
                            } else {
                                $scope.notifyErrorMsg("获取数据失败！")
                            }
                        });
                    }
                    //2,3(为进行中) 撤消、催办、同意、交办、驳回发起人、驳回上一步、流程图、审批历史
                    if (identity == 2 && status == 3) {
                        $scope.auth.repeal = true;
                        $scope.auth.urge = true;
                        $scope.auth.agree = true;
                        $scope.auth.assign = true;
                        $scope.auth.reject = true;
                        $scope.auth.rejectPreNode = true;
                        //根据实例id获取当前任务节点Id
                        $http.post(basePath + "/third/xtbgactivity/getXTBGActivityByProInstdForCurTask?processInstanceId=" + $scope.options.instanceId).success(function (data) {
                            if (data.status == 0) {
                                //当前任务节点ID
                                $scope.currentTaskId = data.result;
                                //根据当前任务节点ID获取可以选择的下一节点名称
                                if ($scope.currentTaskId != null) {
                                    $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByTaskId?", null, {params: {taskId: $scope.currentTaskId}}).success(function (data) {
                                        $scope.nodeNameList = data.result;
                                    });
                                }
                            } else {
                                $scope.notifyErrorMsg("获取数据失败！")
                            }
                        });
                    }


                    //根据状态的不同，提交审批的方法可能是同意操作
                    //$scope.startProcess = status == 2 && $scope.options.instanceId ? $scope.options.button.agree : $scope.options.button.startProcess;
                    $scope.startProcess = function (params) {
                        //if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                        //    if (!params.nextNode) {
                        //        $scope.notifyErrorMsg("请选择下一节点!")
                        //        return;
                        //    }
                        //}
                        if (status == 2 && $scope.options.instanceId) {
                            $scope.options.button.agree(params);
                        } else {
                            $scope.options.button.startProcess(params);
                        }
                    };

                    //特殊处理:-1新增有权限，-2新增无权限/未提交无权限，-3未提交有权限，1不显示审批意见文本域，2非发起人和非当前审批人，c值优先级高于a,b
                    if (specialty == -1) {
                        $scope.auth.startProcess = true;
                        $scope.auth.saveDraft = true;
                        $scope.auth.deleteDraft = false;
                        $scope.auth.approveHistory = false;
                        $scope.auth.cancel = true;
                    } else if (specialty == -2) {
                        $scope.auth.approveHistory = false;
                    } else if (specialty == -3) {
                        $scope.auth.approveHistory = false;
                    } else if (specialty == 1) {
                        $scope.auth.comment = false;
                    } else if (specialty == 2) {
                        $scope.auth.nextUser = false;
                    } else if (specialty == 3) {
                        $scope.auth.startProcess = false;
                        $scope.auth.deleteProcess = false;
                    }

                    //流程被禁用时，隐藏所有按钮
                    var hideAll = coordinate.every(function (item) {
                        return item == null;
                    });
                    if (hideAll) {
                        $scope.auth.agree = false;
                        $scope.auth.assign = false;
                        $scope.auth.urge = false;
                        $scope.auth.reject = false;
                        $scope.auth.rejectPreNode = false;
                        $scope.auth.startProcess = false;
                        $scope.auth.saveDraft = false;
                        $scope.auth.repeal = false;
                        $scope.auth.deleteDraft = false;
                        $scope.auth.chart = false;
                        $scope.auth.approveHistory = false;
                        $scope.auth.cancel = false;
                        $scope.auth.nextUser = false;
                    }
                };

                //默认获取全选的请求
                var getUserAuth = function (callback) {
                    $http.get(basePath + "/third/xtbgactivity/getXTBGActivityCoordinate", {
                        params: {
                            processDefinitionId: $scope.options.definitionKey,
                            processInstanceId: $scope.options.instanceId,
                            applyId: $scope.options.applyId,
                            processStatus: $scope.options.processStatus
                        }
                    }).success(function (data) {
                        if (data.status == 0) {
                            callback(data.result);
                        }
                    });

                };


                //流程进度 0为未提交，1为结束，2为返回修改，3为进行中
                $scope.auth = {
                    //同意
                    agree: false,
                    //交办
                    assign: false,
                    //催办
                    urge: false,
                    //驳回发起人
                    reject: false,
                    //驳回上一步
                    rejectPreNode: false,
                    //提交审批
                    startProcess: false,
                    //保存草稿
                    saveDraft: false,
                    //撤销
                    repeal: false,
                    //删除草稿
                    deleteDraft: false,
                    //删除流程
                    deleteProcess: false,
                    //流程图
                    chart: false,
                    //审批历史
                    approveHistory: false,
                    //取消
                    cancel: false
                };

                /**
                 *
                 */
                var initData = function () {
                    //若未配置自定义获取权限，则使用默认获取
                    if (!$scope.options.getUserAuth) {
                        getUserAuth(initAuthButton)
                    } else {
                        $scope.options.getUserAuth(initAuthButton);
                    }

                };

                initData();

                //选择交下一个节点审核人
                $scope.selectUser = function () {
                    ynuiSelectorService.userSelector(function (item) {
                        $scope.options.params.nextTaskUserStr = item.name;
                        $scope.options.params.nextTaskUserId = item.id;

                        $scope.$apply();
                    }, {gridOptions: {useSingleChoice: true}});
                };


                //选择交下一个节点
                $scope.selectNextNode = function (item) {
                    $scope.options.params.nextNode = item.usertTaskId;
                };


                ////////////Add By ZengXiangyong/////////////
                //加载表单设置权限
                //activitiService.setAuth({
                //        processDefinitionKey: $scope.options.definitionKey,
                //        processInstanceId: $scope.options.instanceId,
                //        processStatus: $scope.options.processStatus,
                //        applyId: $scope.options.applyId
                //    }
                //);

                //同意
                $scope.agree = function (params) {
                    //if (!params.comment) {
                    //    ynNotification.notify("error", "审批意见不能为空！");
                    //    return;
                    //}
                    //if ($scope.nodeNameList && $scope.nodeNameList.length > 0 && $scope.auth.nextNode == 1) {
                    //    if (!params.nextNode) {
                    //        $scope.notifyErrorMsg("请选择下一节点!");
                    //        return;
                    //    }
                    //}
                    $scope.options.button.agree(params);
                };

                //交办
                $scope.assign = function (params) {
                    //if (!params.comment) {
                    //    ynNotification.notify("error", "审批意见不能为空！");
                    //    return;
                    //}
                    $scope.options.button.assign(params);
                };

                //驳回发起人
                $scope.reject = function (params) {
                    //if (!params.comment) {
                    //    ynNotification.notify("error", "审批意见不能为空！");
                    //    return;
                    //}
                    reject();
//                $scope.options.button.reject(params);
                };

                //驳回上一步
                $scope.rejectPreNode = function (params) {
                    //if (!params.comment) {
                    //    ynNotification.notify("error", "审批意见不能为空！");
                    //    return;
                    //}
                    rejectPreNode();
//                $scope.options.button.rejectPreNode(params);
                };

                //撤销
                $scope.repeal = function () {
                    repeal();
                };

            }],

        }
    }]);
