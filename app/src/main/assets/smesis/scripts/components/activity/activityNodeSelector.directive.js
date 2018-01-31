/**
 * Project: yineng-corpSysLand
 * Package
 * Title: fileName
 * author xiechangwei
 * date 2017/5/9 13:32
 * Copyright: 2017 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
angular.module('myApp')
    .directive('nodeSelector', ['$http', function ($http) {
        var template = '<div class="item item-input"  ng-show="nodeNameList && nodeNameList.length > 0  && auth.nextNode" >' +
            '<span class="input-label">下一审批节点</span>' +
            '<input slide-select init-data="nodeNameList" name-field="userTaskName" ng-model="node.nextNodeName"  selected-obj="usertTaskId" on-select="selectNextNode(item)" readonly="readonly" type="text" placeholder="请选择">' +
            '</div>' +
            '<div class="item item-input" ng-show="auth.nextUser">' +
            '<span class="input-label">审批人</span>' +
            ' <input type="text" placeholder="请选择"  user-selector inServiceStates = "1" title="审批人" routingStatusType="{{routingStatusType}}" userTypeApp="{{userTypeApp}}" on-select="selectUser(item)"  readonly ng-model="node.nextUserName"/>' +
            '</div>';
        return {
            scope: {
                options: '=',
                node: '='
            },
            template: template,
            controller: ['$scope', '$ionicModal', '$ionicLoading', '$ionicPopup',function ($scope, $ionicModal, $ionicLoading, $ionicPopup) {
                /**
                 * 根据身份，流程状态的不同设置按钮的控制
                 * @param coordinate
                 */
                    $scope.auth ={}
                //选择交下一个节点
               //$scope.node ={}
                var initAuthButton = function (coordinate) {
                    //根据type的不同控制按钮的隐藏，展示
                    //用户身份:0发起人，1:当前审批人(当前任务执行人)，2:发起人和当前审批人(当前任务执行人)
                    //流程进度:0为未提交，1为结束，2为返回修改，3为进行中
                    //特殊处理:-1新增有权限，-2新增无权限/未提交无权限，-3未提交有权限，1不显示审批意见文本域，2非发起人和非当前审批人，c值优先级高于a,b
                    //是否显示下一节点处理人按钮:1是，0否
                    //是否显示下个节点选择:1是，0否
                    //如果coordinate为空，则 非发起人或审批人在查看流程时，只有：流程图、审批历史

                    //除了1,0 为无，0,0，2,0没有审批历史，其他都有流程图和审批历史
                    //$scope.auth.comment = true;

                    if (!coordinate) {
                        return;
                    }
                    if (coordinate.length != 4) {

                    }
                    var identity = coordinate[0];
                    var status = coordinate[1];
                    var specialty = coordinate[2];
                    $scope.auth.nextUser = coordinate[3];
                    //判断是否可以选择下一审批人
                    $scope.node.hasUser =coordinate[3];
                    $scope.auth.nextNode = coordinate[4];

                    //0,0(未提交) 提交审批、保存草稿、删除草稿、流程图、取消
                    if (identity == 0 && status == 0) {
                        $scope.auth.startProcess = true;
                        $scope.auth.saveDraft = true;
                        $scope.auth.deleteDraft = true;
                        $scope.auth.approveHistory = false;
                        $scope.auth.cancel = true;
                        $http.post(basePath + "/third/xtbgactivity/getUserTaskIdsByActivityKey?", null, {params: {processDefinitionKey: $scope.options.definitionKey}}).success(function (data) {
                            if(data.status=='0'){
                                $scope.nodeNameList = data.result;
                                if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                                    $scope.node.hasNode =true;
                                }
                            }

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
                            if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                                $scope.node.hasNode =true;
                            }
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
                            if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                                $scope.node.hasNode =true;
                            }
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
                            if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                                $scope.node.hasNode =true;
                            }
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
                                        if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                                            $scope.node.hasNode =true;
                                        }
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
                                        if ($scope.nodeNameList && $scope.nodeNameList.length > 0) {
                                            $scope.node.hasNode =true;
                                        }
                                    });
                                }
                            } else {
                                $scope.notifyErrorMsg("获取数据失败！")
                            }
                        });
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
                var initData = function () {
                    //若未配置自定义获取权限，则使用默认获取
                    if (!$scope.options.getUserAuth) {
                        getUserAuth(initAuthButton)
                    } else {
                        $scope.options.getUserAuth(initAuthButton);
                    }

                };

                initData();

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
                    getUserAuth: getUserAuth
                };

                //将使用者转入的参数对象与默认参数合并
                $scope.options = angular.extend(defaultOptions, $scope.options);


                $scope.selectNextNode = function (item) {
                    $scope.node.nextNodeName = item.userTaskName;
                    $scope.node.nextUserTaskActId = item.usertTaskId;
                };

                //选择审批人
                $scope.selectUser = function (item) {
                    $scope.node.nextUserName = item.name;
                    $scope.node.nextTaskUserId = item.id;
                }

                //人员单选时 传入 流程key、人员类型
                $scope.routingStatusType = $scope.options.routingStatusType;
                $scope.userTypeApp = $scope.options.userTypeApp;


            }



            ]
        }
    }]);
