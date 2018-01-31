/**
 * Project: yineng-corpSysLand
 * Package
 * Title: fileName
 * author xiechangwei
 * date 2017/5/5 9:31
 * Copyright: 2017 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
angular.module('myApp')
    .directive('activityApprove', ['$http','$timeout', function ($http,$timeout) {
        var template = '<ion-footer-bar class="bar-up-shadow">' +
            '<div class="row">' +
            ' <div class="col text-center btn-group-wrap"> ' +
            '<button class="button button-theme button-local button-outline" type="button" ng-click="options.button.cancel()">返回</button>' +
            '<button class="button button-theme button-local" type="button" subbutt ng-click="approve(options.params)" ng-if="auth.agree" >通过</button>' +
            '<button class="button button-theme button-local" type="button" subbutt ng-click="assignData(options.params)" ng-if="auth.assign" >交办</button>' +
            ' <button class="button button-theme button-local button-outline button-more" ng-click="showMoreButton()">' +
            '<i class="ion-more"></i>' +
            '<ul class="more" ng-if="moreButton">' +
            '<li ng-click="rejectData(options.params)" ng-if="auth.reject">驳回发起人</li>' +
            '<li ng-click="rejectPreNodeData(options.params)" ng-if="auth.rejectPreNode">驳回上一步</li>' +
            '</ul>' +
            '</button>' +
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

                //点击更多按钮
                $scope.moreButton = false;
                $scope.showMoreButton = function () {
                    $scope.moreButton = !$scope.moreButton;
                };

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
                //声明传回页面的参数
                $scope.pageData = {
                    //下一节点审批人
                    nextTaskUserId: "",
                    //下一节点
                    nextUserTaskActId: "",
                    //审批意见
                    message: ""
                };

                //提交审批的时候 按钮模板
                var approveTemplate = ' <ion-modal-view class="auto bar-up-shadow">' +
                    '<div class="bottom-50">' +
                    '<div class="padding">' +
                    '<div class="approve-flow-wrap flow-title" ng-show="nodeNameList && nodeNameList.length > 0 ">' +
                    '<div class="title">下一审批节点</div>' +
                    '<label class="radio-inline" ng-repeat="node in nodeNameList"><input type="radio" name="1" ng-click="selectNextNode(node)"/>{{node.userTaskName}}</label>' +
                    ' </div>' +
                    '<div class="approve-flow-wrap flow-title" ng-show="auth.nextUser">' +
                    '<div class="title more">下一节点审批人</div>' +
                    //'<span class="text-gray" >请选择</span>' +
                    ' <input type="text" placeholder="请选择"  user-selector inServiceStates = "1" title="下一节点审批人" routingStatusType="{{routingStatusType}}" userTypeApp="{{nextNodeApprover}}"  on-select="selectUser(item)"  readonly ng-model="pageData.nextUserName"/>' +
                    '</div>' +
                    '</div>' +
                    '<div class="padding button-modal">' +
                    '<input class="form-control" type="input" readonly placeholder="输入处理意见，最多50个字" ng-model="pageData.message" ng-click="showHandleIdea()"/>' +
                    '</div>' +
                    '</div>' +
                    '<ion-footer-bar>' +
                    '<div class="row">' +
                    '<div class="col text-center btn-group-wrap">' +
                    '<button class="button button-theme button-local button-outline" ng-click="closePassApproveModal()">取消</button>' +
                    '<button class="button button-theme button-local" ng-click="checkData()">确定</button>' +
                    '</div>' +
                    '</div>' +
                    '</ion-footer-bar>' +
                    '</ion-modal-view>';

                $scope.modal = $ionicModal.fromTemplate(approveTemplate, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });

                //展开模板
                $scope.openModal = function () {
                    $scope.modal.show();
                };
                $scope.closePassApproveModal = function () {
                    $scope.modal.hide();
                };

                //交办的时候 展开模板
                var assignedTemplate = ' <ion-modal-view class="auto bar-up-shadow">' +
                    '<div class="bottom-50">' +
                    '<div class="padding">' +
                    '<div class="approve-flow-wrap flow-title"> ' +
                    '<div class="title more">请选择交办人</div>' +
                    ' <input type="text" placeholder="请选择"  user-selector inServiceStates = "1" title="交办人" routingStatusType="{{routingStatusType}}" userTypeApp="{{assignedPerson}}"  on-select="selectUser(item)"  readonly ng-model="pageData.nextUserName"/>' +
                    '</div>' +
                    '</div>' +
                    '<div class="padding button-modal">' +
                    '<input class="form-control" type="input" readonly placeholder="输入处理意见，最多50个字" ng-model="pageData.message" ng-click="showHandleIdea()"/>' +
                    '</div>' +
                    '</div>' +
                    '<ion-footer-bar>' +
                    '<div class="row">' +
                    '<div class="col text-center btn-group-wrap">' +
                    '<button class="button button-theme button-local button-outline" ng-click="closeAssignedModal()">取消</button>' +
                    '<button class="button button-theme button-local" ng-click="checkAssignedData()">确定</button>' +
                    '</div>' +
                    '</div>' +
                    '</ion-footer-bar>' +
                    '</ion-modal-view>';

                $scope.assignedModal = $ionicModal.fromTemplate(assignedTemplate, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });

                //交办展开模板
                $scope.openAssignedModal = function () {
                    $scope.assignedModal.show();
                };
                $scope.closeAssignedModal = function () {
                    $scope.assignedModal.hide();
                };


                var rejectTemplate = ' <ion-modal-view class="auto bar-up-shadow">' +
                    '<div class="bottom-50">' +
                    '<div class="padding button-modal">' +
                    '<input class="form-control" type="input" readonly placeholder="输入处理意见，最多50个字" ng-model="pageData.message" ng-click="showHandleIdea()"/>' +
                    '</div>' +
                    '</div>' +
                    '<ion-footer-bar>' +
                    '<div class="row">' +
                    '<div class="col text-center btn-group-wrap">' +
                    '<button class="button button-theme button-local button-outline" ng-click="closeRejectModal()">取消</button>' +
                    '<button class="button button-theme button-local" ng-click="sure(opreaTionType)">确定</button>' +
                    '</div>' +
                    '</div>' +
                    '</ion-footer-bar>' +
                    '</ion-modal-view>';

                $scope.rejectModal = $ionicModal.fromTemplate(rejectTemplate, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });

                //驳回发起人和驳回上一步展开模板
                //判断是驳回发起人还是驳回上一步
                $scope.opreaTionType ="";
                $scope.openRejectModal = function (type) {
                    $scope.opreaTionType = type;
                    $scope.rejectModal.show();
                }

                $scope.closeRejectModal = function () {
                    $scope.rejectModal.hide();
                };

                //处理意见弹框
                $scope.showHandleIdea = function () {
                    $scope.show ={}
                    var myPopup = $ionicPopup.show({
                        template: '<textarea type="input" rows="5" class="form-control"  placeholder="输入处理意见，最多50个字"  maxlength="50" ng-model="pageData.message"></textarea>',
                        title: '处理意见(非必填)',
                        scope: $scope,
                        buttons: [
                            {
                                text: '取消',
                                type: 'button-local button-theme button-outline',
                                onTap: function (e) {
                                    $scope.pageData.message = ""

                                }

                            },
                            {
                                text: '确定',
                                type: 'button-local button-theme',
                                onTap: function (e) {
                                    $scope.pageData.message = $scope.pageData.message;
                                }
                            }
                        ]
                    })
                    //$timeout(function () {
                    //    myPopup.close(); //close the popup after 3 seconds for some reason
                    //}, 3000);
                };
                //验证是否选择下一节点
                $scope.checkData = function () {
                    if($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                        if(!$scope.pageData.nextUserTaskActId){
                            $scope.notifyErrorMsg("请选择下一审批节点!")
                            return;
                        }
                    }
                    //if($scope.auth.nextUser && !$scope.pageData.nextTaskUserId){
                    //    $scope.notifyErrorMsg("请选择下一审批人!")
                    //    return;
                    //}
                    $scope.closePassApproveModal();
                    $scope.agree($scope.pageData);
                }

                //验证交办的时候 是否选择了交办人
                $scope.checkAssignedData = function () {
                    if(!$scope.pageData.nextTaskUserId){
                        $scope.notifyErrorMsg("请选择下一审批人!")
                        return;
                    }
                    $scope.closeAssignedModal();
                    $scope.assign($scope.pageData);
                }

                //驳回发起人和驳回上一步
                $scope.sure = function (type) {
                    $scope.closeRejectModal();
                    if(type==1){
                        //驳回发起人
                        $scope.reject($scope.pageData);
                    }else{
                        //驳回上一步
                        $scope.rejectPreNode($scope.pageData);
                    }


                }

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
                    buttonAction: null,
                    //取得当前的用户有哪些按钮点击权限
                    getUserAuth: getUserAuth
                };

                //将使用者转入的参数对象与默认参数合并
                $scope.options = $.extend(true, defaultOptions, $scope.options);
                //人员单选时 传入 流程key、人员类型
                $scope.routingStatusType = $scope.options.routingStatusType;
                $scope.nextNodeApprover = $scope.options.nextNodeApprover;//下一节点审批人
                $scope.assignedPerson = $scope.options.assignedPerson;//交办人
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
                        if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                            if (!params.nextNode) {
                                $scope.notifyErrorMsg("请选择下一节点!")
                                return;
                            }
                        }
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
                $scope.selectUser = function (item) {
                    $scope.pageData.nextUserName = item.name;
                    $scope.pageData.nextTaskUserId = item.id;
                };


                //选择交下一个节点
                $scope.selectNextNode = function (item) {
                    $scope.pageData.nextUserTaskActId = item.usertTaskId;
                };

                //同意
                $scope.approve = function () {
                    $scope.pageData.message = "";
                    $scope.pageData.nextTaskUserId = null;
                    $scope.pageData.nextUserName = null;
                    $scope.openModal();
                }

                //交办的时候展开模板
                $scope.assignData = function () {
                    $scope.pageData.message = "";
                    $scope.pageData.nextTaskUserId = null;
                    $scope.pageData.nextUserName = null;
                    $scope.openAssignedModal();

                }
                //驳回发起人的时候展开模板
                $scope.rejectData = function () {
                    $scope.pageData.message = ""
                    $scope.openRejectModal(1);

                }
                //驳回上一步的时候展开模板
                $scope.rejectPreNodeData = function () {
                    $scope.pageData.message = ""
                    $scope.openRejectModal(2);
                }


                //同意
                $scope.agree = function (params) {
                    $scope.options.button.agree(params);
                };

                //交办
                $scope.assign = function (params) {
                    $scope.options.button.assign(params);
                };

                //驳回发起人
                $scope.reject = function (params) {
                    $scope.options.button.reject($scope.pageData);
                };

                //驳回上一步
                $scope.rejectPreNode = function (params) {
                    $scope.options.button.rejectPreNode($scope.pageData);
                };

                //撤销
                $scope.repeal = function () {
                    repeal();
                };

            }]
        }
    }]);
