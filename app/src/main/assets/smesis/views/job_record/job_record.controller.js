(function () {
    'use strict';
    angular.module('myApp').controller('jobRecordController', jobRecordController);

    jobRecordController.$inject = ['$scope', '$ionicPopup', '$http', '$timeout', '$ionicLoading', '$ionicModal', '$filter'];

    function jobRecordController($scope, $ionicPopup, $http, $timeout, $ionicLoading, $ionicModal, $filter) {

        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function () {
            // $scope.add_task.remove();
            // $scope.add_task_modal.remove();
            // $scope.view_task_modal.remove();
        });

        $scope.pageData = {
            rule: null,
            allDateList: [],
            beforeTask: [],
            afterTask: [],
            selectedTaskDetail: null,
            selectedTaskIndex: null,
            selectedAttribute: null,
            selectedIsModify: false
        };

        $scope.conditions = {
            lastDate: ""
        };

        $scope.clickRow = clickRow;     // 点击行事件处理
        $scope.getDayStr = getDayStr;   // 处理 今、明、昨天的显示
        $scope.getMonthAndDay = getMonthAndDay;   // 处理日期显示
        $scope.getTaskCountStr = getTaskCountStr; // 处理计划项显示
        $scope.getAllTask = getAllTask; // 获取所有的计划数据
        $scope.loadMore = loadMore;     // 划到底部刷新
        $scope.findTaskDetail = findTaskDetail;   // 根据单个日期查询计划详细（用于添加）
        $scope.findRule = findRule;     // 获取填写规则
        $scope.saveData = saveData;     // 添加、修改保存数据
        $scope.getTask = getTask;       // 从交办和项目管理中获取任务
        $scope.showAddTask = showAddTask;         // 显示添加任务
        $scope.closeAddTask = closeAddTask;       // 关闭添加任务
        $scope.deleteTask = deleteTask;           // 删除整个计划
        $scope.deleteTaskDetail = deleteTaskDetail;     // 删除单个任务
        $scope.showAddTaskDetail = showAddTaskDetail;   // 显示添加、修改单个任务
        $scope.closeAddTaskDetail = closeAddTaskDetail; // 关闭添加、修改单个任务
        $scope.addOneTaskDetail = addOneTaskDetail;     // 增加1项任务按钮
        $scope.addConsumptionTime = addConsumptionTime; // 加减工时
        $scope.confirmDetail = confirmDetail;     // 添加修改确定
        $scope.submitPlan = submitPlan; // 提交任务
        $scope.findTask = findTask;     // 查询任务详细（用于查看、修改）
        $scope.showViewTask = showViewTask;    // 显示查看、修改modal
        $scope.closeViewTask = closeViewTask;  // 隐藏查看、修改modal
        $scope.modifyPageAddTask = modifyPageAddTask;  // 修改页面增加一项任务
        $scope.showConclusionTask = showConclusionTask;  // 显示总结任务modal
        $scope.closeConclusionTask = closeConclusionTask;  // 关闭总结任务modal
        $scope.submitConclusion = submitConclusion;  // 提交总结
        $scope.formatNumber = formatNumber;  // 格式化输入小时那

        // 注册Android的物理返回键
        // $rootScope.registerBackButtonAction($scope.closeAddTask, 'closeAddTask');
        // 默认获取一次规则
        $scope.findRule();

        // 添加任务modal框
        $ionicModal.fromTemplateUrl('add-task.html', {
            scope: $scope,
            animation: 'slide-in-right'
        }).then(function (modal) {
            $scope.add_task = modal;
        });

        $ionicModal.fromTemplateUrl('add-task-modal.html', {
            scope: $scope,
            animation: 'slide-in-right',
            backdropClickToClose: false
        }).then(function (modal) {
            $scope.add_task_modal = modal;
        });

        $ionicModal.fromTemplateUrl('task-view.html', {
            scope: $scope,
            animation: 'slide-in-right'
        }).then(function (modal) {
            $scope.view_task_modal = modal;
        });

        $ionicModal.fromTemplateUrl('task-conclusion.html', {
            scope: $scope,
            animation: 'slide-in-right'
        }).then(function (modal) {
            $scope.conclusion_task_modal = modal;
        });

        function clickRow(rowData) {
            $scope.delectedTask = [];
            $scope.currentRowData = angular.copy(rowData);
            // 格式化日期
            $scope.currentRowData.formatShowDate = $scope.currentRowData.showDate.split('-')[1] + '月' +
                $scope.currentRowData.showDate.split('-')[2] + '日';
            if (rowData.teskQuantity === 0 && rowData.showDay > 1) {
                $ionicPopup.show({
                    title: '提示',
                    template: '<h5>只允许昨天、今天、明天添加工作任务</h5>',
                    buttons: [{
                        text: '确定',
                        type: 'button-theme button-local'
                    }]
                });
            } else if (rowData.showDay <= 1 && rowData.teskQuantity > 0) {
                // 修改
                $scope.showViewTask(rowData);
            } else if (rowData.showDay <= 1 && rowData.teskQuantity <= 0) {
                // 添加
                $scope.showAddTask(rowData);
            } else {
                // 查看
                $scope.showViewTask(rowData);
            }
        }

        function getDayStr(type) {
            if (type === '-1') {
                return "[明天]";
            } else if (type === '0') {
                return "[今天]";
            } else if (type === '1') {
                return "[昨天]";
            } else {
                return "";
            }
        }

        function getMonthAndDay(date) {
            if (date) {
                var ol = date.split("-");
                if (ol.length > 2) {
                    return ol[1] + "-" + ol[2];
                }
            }
            return date;
        }

        function getTaskCountStr(taskCount) {
            if (taskCount > 0) {
                return "共" + taskCount + "项任务";
            } else {
                return "未计划";
            }
        }

        function getAllTask(params) {
            return $http.get(basePath + '/third/jobrecord/findJSTXJobRecordDTOByConditions', {
                params: params
            }).then(function (response) {
                if (params.showDate) {
                    $scope.pageData.allDateList = $scope.pageData.allDateList.concat(response.data);
                } else {
                    $scope.pageData.allDateList = response.data;
                }
                return response;
            })
        }

        function findTaskDetail(params) {
            return $http.get(basePath + '/third/jobrecord/findJSTXJObRecordDetailDTOByShowDateAndAll', {
                params: params
            }).then(function (response) {
                return response;
            })
        }

        function findRule() {
            return $http.get(basePath + '/third/jobrecord/findWorkLogSet').then(function (response) {
                $scope.pageData.rule = response.data.result;
                return response;
            })
        }

        function saveData(params, postParams) {
            return $http.post(basePath + '/third/jobrecord/saveOrUpdateJSTXJObRecordDetail', postParams, {
                params: params
            }).then(function (response) {
                return response;
            })
        }

        function getTask(params) {
            return $http.get(basePath + '/third/jobrecord/findJSTXJObRecordDetailDTOByShowDateAndGet', {
                params: params
            }).then(function (response) {
                return response;
            })
        }

        //------------------------------------ 翻页加载 组件添加 start ------------------------------

        function loadMore() {
            $scope.isLoadData = true;
            if ($scope.showMore) {
                return null;
            }
            $scope.getAllTask({showDate: $scope.conditions.lastDate}).then(function () {
                $scope.isLoadData = false;
                if (!$scope.pageData.allDateList || $scope.pageData.allDateList.length === 0) {
                    $scope.showMore = true;
                }
                if ($scope.pageData.allDateList.length * 7 >= 100) {
                    $scope.showMore = true;
                }
                $scope.conditions.lastDate = $scope.pageData.allDateList.getAttr().getAttr('showDate');
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }, 1000);
            });
        }

        //------------------------------------ 翻页加载 组件添加 end --------------------------------

        function showAddTask(rowData) {
            $scope.pageData.showTip = true;
            $scope.pageData.beforeTask = [];
            $scope.pageData.afterTask = [];
            $scope.add_task.show().then(function () {
                // 根据日期查询已有的pdca
                $scope.findTaskDetail({showDate: rowData.showDate}).then(function (response) {
                    // 判断当前时间是否超过事先计划时间
                    var planTime = new Date($scope.currentRowData.showDate.replace(/-/g, '/') + ' ' + $scope.pageData.rule.planTime);
                    var currentTime = new Date();
                    if (planTime > currentTime) { // 事先计划
                        $scope.pageData.beforeTask = [];
                        $scope.pageData.beforeTask = $scope.pageData.beforeTask.concat(response.data)
                            .goAir().goHeavy().suppFun();
                    } else { // 延后登记
                        $scope.pageData.afterTask = [];
                        $scope.pageData.afterTask = $scope.pageData.afterTask.concat(response.data)
                            .goAir().goHeavy().suppFun();
                    }
                });
            });
        }

        function closeAddTask() {
            $ionicPopup.show({
                title: '提示',
                template: '<h5>确定要放弃当前填写的计划吗？</h5>',
                buttons: [
                    {text: '继续编辑', type: 'button-outline button-theme button-local'},
                    {
                        text: '确定返回', type: 'button-theme button-local',
                        onTap: function () {
                            $scope.add_task.hide();
                            // $rootScope.deregisterBackButtonAction('closeAddTask');
                        }
                    }
                ]
            });
        }

        $scope.getTaskByProject = getTaskByProject;

        function getTaskByProject() {
            $ionicLoading.show({
                template: '<h4>正在获取...</h4>',
                scope: $scope
            });
            $scope.getTask({
                showDate: $scope.currentRowData.showDate,
                isObtain: true
            }).then(function (response) {
                $ionicLoading.hide();
                var count = 0, i, j, tasks = [];
                for (i = 0; i < response.data.length; i++) {
                    if (response.data[i].isAdd) {
                        continue;
                    }
                    var index = null;
                    for (j = 0; j < $scope.delectedTask.length; j++) {
                        if (response.data[i].taskType === $scope.delectedTask[j].taskType &&
                            response.data[i].taskName === $scope.delectedTask[j].taskName) {
                            // tasks.push($scope.delectedTask[i]);
                            index = j;
                            break;
                        }
                    }
                    // 移除
                    if (null !== index) {
                        $scope.delectedTask.splice(index, 1);
                    }
                    tasks.push(response.data[i]);
                }
                var beforeCount = 0, afterCount = 0;
                if ($scope.pageData.beforeTask.length > 0) {
                    // 获取之前的任务条数
                    beforeCount = $scope.pageData.beforeTask.goAir().length;
                    $scope.pageData.beforeTask = $scope.pageData.beforeTask.concat(tasks);
                    $scope.pageData.beforeTask = $scope.pageData.beforeTask.goAir().goHeavy().suppFun();
                    // 获取之后的任务条数
                    afterCount = $scope.pageData.beforeTask.goAir().length;
                } else {
                    // 获取之前的任务条数
                    beforeCount = $scope.pageData.afterTask.goAir().length;
                    $scope.pageData.afterTask = $scope.pageData.afterTask.concat(tasks);
                    $scope.pageData.afterTask = $scope.pageData.afterTask.goAir().goHeavy().suppFun();
                    // 获取之后的任务条数
                    afterCount = $scope.pageData.afterTask.goAir().length;
                }
                $ionicPopup.show({
                    title: '提示',
                    template: afterCount - beforeCount === 0 ? '<h5>没有获取到任务</h5>' : '<h5>成功获取' + (afterCount - beforeCount) + '条任务</h5>',
                    buttons: [{
                        text: '确定',
                        type: 'button-theme button-local'
                    }]
                });
            })
        }

        function deleteTask() {
            $ionicPopup.confirm({
                title: '提示',
                template: '<h5>删除工作计划将删除里面所有的任务，确定要删除吗？</h5>',
                buttons: [{
                    text: '取消',
                    type: 'button-outline button-theme button-local'
                }, {
                    text: '确定',
                    type: 'button-theme button-local',
                    onTap: function () {
                        $http.get(basePath + '/third/jobrecord/deleteWorkingLogByShowDate', {
                            params: {
                                showDate: $scope.currentRowData.showDate
                            }
                        }).then(function () {
                            $ionicLoading.show({
                                template: '<h4>删除成功</h4>',
                                scope: $scope,
                                duration: 1000
                            }).then(function () {
                                $scope.getAllTask({showData: ''}).then(function () {
                                    $scope.conditions.lastDate = $scope.pageData.allDateList.getAttr().getAttr('showDate');
                                    $scope.view_task_modal.hide();
                                });
                            });
                        });
                    }
                }]
            });
        }

        function deleteTaskDetail(attribute, index) {
            var msg;
            if ($scope.pageData.canModify) {
                if ($scope.pageData.beforeTask.length + $scope.pageData.afterTask.length === 1) {
                    msg = '<h5>删除最后一条任务后，整个' + $scope.currentRowData.formatShowDate + '的计划都将被删除</h5>';
                } else {
                    msg = '<h5>删除后，任务无法恢复</h5>';
                }
            } else {
                msg = '<h5>删除后，当前添加的任务无法恢复</h5>';
            }
            $ionicPopup.show({
                title: '提示',
                template: msg,
                buttons: [
                    {text: '取消', type: 'button-outline button-theme button-local'},
                    {
                        text: '确定删除', type: 'button-theme button-local',
                        onTap: function () {
                            // 代表是修改页面的删除，要去数据库删除（不能修改的逻辑删除
                            if ($scope.pageData.canModify) {
                                $http.get(basePath + '/third/jobrecord/deleteDetailsById', {
                                    params: {
                                        id: $scope.pageData[attribute][index].id
                                    }
                                }).then(function () {
                                    $ionicLoading.show({
                                        template: '<h4>删除成功</h4>',
                                        duration: 1000
                                    }).then(function () {
                                        $scope.getAllTask({showData: ''}).then(function () {
                                            $scope.conditions.lastDate = $scope.pageData.allDateList.getAttr().getAttr('showDate');
                                        });
                                        // 需要返回主页面
                                        if ($scope.pageData.beforeTask.length + $scope.pageData.afterTask.length === 0) {
                                            $scope.view_task_modal.hide();
                                        }
                                    })
                                })
                            }
                            if (!$scope.pageData[attribute][index].modify) {
                                $scope.pageData[attribute][index].del = true;
                                var flag = false;
                                for (var i = 0; i < $scope.delectedTask.length; i++) {
                                    if ($scope.delectedTask[i].taskName === $scope.pageData[attribute][index].taskName &&
                                        $scope.delectedTask[i].taskType === $scope.pageData[attribute][index].taskType) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if (!flag) {
                                    $scope.delectedTask.push($scope.pageData[attribute][index]);
                                }
                            }
                            $scope.pageData[attribute].splice(index, 1);
                        }
                    }
                ]
            });
        }

        function showAddTaskDetail(rowData, attribute, index) {
            $scope.pageData.selectedTaskDetail = angular.copy(rowData);
            // 处理工时
            if (isNaN(parseFloat($scope.pageData.selectedTaskDetail.consumptionTime))) {
                $scope.pageData.selectedTaskDetail.consumptionTime = 0;
            }
            // 添加的需要清空taskName
            if (rowData.isAdd) {
                $scope.pageData.selectedTaskDetail.taskName = null;
                // 如果是添加的，需要去取最小的需要添加的任务
                for (var i = 0; i < $scope.pageData[attribute].length; i++) {
                    if ($scope.pageData[attribute][i].isAdd) {
                        index = i;
                        break;
                    }
                }
            }
            $scope.pageData.selectedAllTaskIndex = index;
            if (attribute === 'afterTask') {
                $scope.pageData.selectedAllTaskIndex += $scope.pageData.beforeTask.length
            }
            $scope.pageData.selectedTaskIndex = index;
            $scope.pageData.selectedAttribute = attribute;
            if (null === $scope.pageData.selectedTaskDetail.remindTime) {
                $scope.pageData.selectedTaskDetail.remindTime = '00:00';
            }
            $scope.add_task_modal.show();
            // $rootScope.registerBackButtonAction($scope.closeAddTaskDetail, 'closeAddTaskDetail');
        }

        function closeAddTaskDetail() {
            if ($scope.pageData.selectedTaskDetail.taskName) {
                $ionicPopup.show({
                    title: '提示',
                    template: '<h5>取消后编辑的内容将丢失，确定取消吗？</h5>',
                    buttons: [
                        {text: '继续编辑', type: 'button-outline button-theme button-local'},
                        {
                            text: '确定返回', type: 'button-theme button-local',
                            onTap: function () {
                                $scope.add_task_modal.hide().then(function () {
                                    $scope.add_task_modal.remove().then(function () {
                                        $ionicModal.fromTemplateUrl('add-task-modal.html', {
                                            scope: $scope,
                                            animation: 'slide-in-right',
                                            backdropClickToClose: false
                                        }).then(function (modal) {
                                            $scope.add_task_modal = modal;
                                        });
                                    });
                                });
                                $scope.pageData.selectedTaskDetail = null;
                                // $rootScope.deregisterBackButtonAction('closeAddTaskDetail');
                                // $rootScope.registerBackButtonAction($scope.closeAddTask, 'closeAddTask');
                            }
                        }
                    ]
                });
            } else {
                $scope.add_task_modal.hide().then(function () {
                    $scope.add_task_modal.remove().then(function () {
                        $ionicModal.fromTemplateUrl('add-task-modal.html', {
                            scope: $scope,
                            animation: 'slide-in-right',
                            backdropClickToClose: false
                        }).then(function (modal) {
                            $scope.add_task_modal = modal;
                        });
                    });
                });
                $scope.pageData.selectedTaskDetail = null;
            }
        }

        function addOneTaskDetail() {
            if ($scope.pageData.beforeTask.length + $scope.pageData.afterTask.length >= 10) {
                $ionicPopup.show({
                    title: '提示',
                    template: '<h5>最多只能添加10项任务</h5>',
                    buttons: [{
                        text: '确定',
                        type: 'button-theme button-local'
                    }]
                });
                return false;
            }
            // 取当前时间，判断事前计划或延后登记
            var planTime = new Date($scope.currentRowData.showDate.replace(/-/g, '/') + ' ' + $scope.pageData.rule.planTime);
            var currentTime = new Date();
            if (planTime > currentTime) { // 事先计划
                $scope.pageData.beforeTask.push({
                    taskName: '',
                    isAdd: true,
                    taskType: 1,
                    remind: false,
                    modify: true
                })
            } else { // 延后登记
                $scope.pageData.afterTask.push({
                    taskName: '',
                    isAdd: true,
                    taskType: 1,
                    remind: false,
                    modify: true
                })
            }
        }

        function addConsumptionTime(step, obj, type) {
            if (type) {
                if (!angular.isNumber(obj.actualConsumptionTime)) {
                    obj.actualConsumptionTime = 0;
                } else {
                    obj.actualConsumptionTime += step;
                    if (obj.actualConsumptionTime < 0) {
                        obj.actualConsumptionTime = 0;
                    }
                }
            } else {
                if (obj) {
                    if (!angular.isNumber(obj.consumptionTime)) {
                        obj.consumptionTime = 0;
                    }
                    obj.consumptionTime += step;
                    if (obj.consumptionTime < 0) {
                        obj.consumptionTime = 0;
                    }
                } else {
                    if (!angular.isNumber($scope.pageData.selectedTaskDetail.consumptionTime)) {
                        $scope.pageData.selectedTaskDetail.consumptionTime = 0;
                    }
                    $scope.pageData.selectedTaskDetail.consumptionTime += step;
                    if ($scope.pageData.selectedTaskDetail.consumptionTime < 0) {
                        $scope.pageData.selectedTaskDetail.consumptionTime = 0;
                    }
                }
            }
        }

        function confirmDetail() {
            // 验证
            if (!$scope.pageData.selectedTaskDetail.taskName) {
                $ionicPopup.show({
                    title: '提示',
                    template: '<h5>请输入任务名称</h5>',
                    buttons: [
                        {text: '确定', type: 'button-theme button-local'}
                    ]
                });
            } else {
                var regex = new RegExp(/^(\d+\.\d|\d+)$/);
                // 计划工时
                if ($scope.pageData.selectedTaskDetail.consumptionTime) {
                    if (!regex.test($scope.pageData.selectedTaskDetail.consumptionTime)) {
                        $ionicPopup.show({
                            title: '提示',
                            template: '<h5>工时格式不正确</h5>',
                            buttons: [
                                {text: '确定', type: 'button-theme button-local'}
                            ]
                        });
                        return false;
                    }
                    // 格式化一下数字
                    $scope.pageData.selectedTaskDetail.consumptionTime =
                        parseFloat($scope.pageData.selectedTaskDetail.consumptionTime) + '';
                }
                // 实际工时
                if ($scope.pageData.selectedTaskDetail.actualConsumptionTime) {
                    if (!regex.test($scope.pageData.selectedTaskDetail.actualConsumptionTime)) {
                        $ionicPopup.show({
                            title: '提示',
                            template: '<h5>工时格式不正确</h5>',
                            buttons: [
                                {text: '确定', type: 'button-theme button-local'}
                            ]
                        });
                        return false;
                    }
                    $scope.pageData.selectedTaskDetail.actualConsumptionTime =
                        parseFloat($scope.pageData.selectedTaskDetail.actualConsumptionTime) + '';
                }
                if (undefined === $scope.pageData.selectedTaskDetail.completeState) {
                    $scope.pageData.selectedTaskDetail.completeState = 1;
                }
                if ($scope.pageData.selectedTaskDetail.remind && undefined === $scope.pageData.selectedTaskDetail.remindTime) {
                    $scope.pageData.selectedTaskDetail.remindTime = '00:00';
                }
                // 更新对应的任务
                $scope.pageData.selectedTaskDetail.isAdd = false;
                $scope.pageData[$scope.pageData.selectedAttribute][$scope.pageData.selectedTaskIndex] =
                    $scope.pageData.selectedTaskDetail;
                // 大于0代表是修改进来的那时候点击确定才保存
                if ($scope.currentRowData.teskQuantity > 0) {
                    var type = angular.isUndefined($scope.pageData.selectedTaskDetail.id) ? 'add' : 'modify';
                    var arr = buildSaveTask();
                    $scope.saveData({showDate: $scope.currentRowData.showDate}, arr).then(function () {
                        $ionicLoading.show({
                            template: type === 'add' ? '<h4>添加成功</h4>' : '<h4>修改成功</h4>',
                            scope: $scope,
                            duration: 1000
                        }).then(function () {
                            $scope.getAllTask({showData: ''}).then(function () {
                                $scope.conditions.lastDate = $scope.pageData.allDateList.getAttr().getAttr('showDate');
                                saveTaskAfter();
                            });
                        });
                    });
                }
                $scope.add_task_modal.hide().then(function () {
                    $scope.add_task_modal.remove().then(function () {
                        $ionicModal.fromTemplateUrl('add-task-modal.html', {
                            scope: $scope,
                            animation: 'slide-in-right',
                            backdropClickToClose: false
                        }).then(function (modal) {
                            $scope.add_task_modal = modal;
                        });
                    });
                });
            }
        }

        /**
         * 组装计划中的任务数据
         * @returns {Array}
         */
        function buildSaveTask() {
            var arr = [], i;
            for (i = 0; i < $scope.pageData.beforeTask.length; i++) {
                if ($scope.pageData.beforeTask[i].taskName && !$scope.pageData.beforeTask[i].isAdd) {
                    $scope.pageData.beforeTask[i].planning = 1;
                    arr.push($scope.pageData.beforeTask[i]);
                }
            }
            for (i = 0; i < $scope.pageData.afterTask.length; i++) {
                if ($scope.pageData.afterTask[i].taskName && !$scope.pageData.afterTask[i].isAdd) {
                    $scope.pageData.afterTask[i].planning = 2;
                    arr.push($scope.pageData.afterTask[i]);
                }
            }
            return arr.concat($scope.delectedTask);
        }

        function checkDataAndSave(type) {
            var arr = buildSaveTask();
            $scope.saveData({showDate: $scope.currentRowData.showDate}, arr).then(function (response) {
                if (response.data.status === '1') {
                    $ionicLoading.show({
                        template: type === 'add' ? '<h4>添加成功</h4>' : '<h4>修改成功</h4>',
                        scope: $scope,
                        duration: 1000
                    }).then(function () {
                        $scope.getAllTask({showData: ''}).then(function () {
                            $scope.conditions.lastDate = $scope.pageData.allDateList.getAttr().getAttr('showDate');
                            if (type === 'add') {
                                $scope.add_task.hide();
                            } else {
                                saveTaskAfter();
                            }
                        });
                    });
                }
            })
        }

        /**
         * 保存了任务之后需要做的处理
         */
        function saveTaskAfter() {
            $scope.add_task_modal.hide().then(function () {
                $scope.add_task_modal.remove().then(function () {
                    $ionicModal.fromTemplateUrl('add-task-modal.html', {
                        scope: $scope,
                        animation: 'slide-in-right',
                        backdropClickToClose: false
                    }).then(function (modal) {
                        $scope.add_task_modal = modal;
                    });
                });
            });
            $scope.findTask({showDate: $scope.currentRowData.showDate}).then(function (response) {
                if (response.data.length > 0) {
                    $scope.currentRowData.summaryExplain = response.data[0].summaryExplain;
                    var count = 0, cancel = 0, beforeTask = [], afterTask = [];
                    for (var i = 0; i < response.data.length; i++) {
                        if (response.data[i].del) {
                            $scope.delectedTask.push(response.data[i]);
                            continue;
                        }
                        if (response.data[i].completeState === 2) {
                            count += 1;
                        }
                        if (response.data[i].completeState === 3) {
                            cancel += 1;
                        }
                        if (response.data[i].planning === 1) { // 事先计划
                            beforeTask.push(response.data[i]);
                        } else { // 延后登记
                            afterTask.push(response.data[i]);
                        }
                    }
                    $scope.pageData.beforeTask = beforeTask;
                    $scope.pageData.afterTask = afterTask;
                    var length = beforeTask.length + afterTask.length;
                    $scope.pageData.taskCountMsg = '共' + (length) + '项任务、已完成' +
                        count + '项、未完成' + (length - count - cancel) + '项、任务取消' +
                        (cancel) + '项';
                }
            });
        }

        function submitPlan() {
            var i, count = 0;
            for (i = 0; i < $scope.pageData.beforeTask.length; i++) {
                if ($scope.pageData.beforeTask[i].taskName) {
                    count += 1;
                }
            }
            for (i = 0; i < $scope.pageData.afterTask.length; i++) {
                if ($scope.pageData.afterTask[i].taskName) {
                    count += 1;
                }
            }
            if (count === 0) {
                $ionicPopup.show({
                    title: '提示',
                    template: '<h5>请至少填写一项工作任务</h5>',
                    buttons: [
                        {text: '确定', type: 'button-theme button-local'}
                    ]
                });
            } else {
                if ($scope.currentRowData.showDay === '1') { // 是昨天的添加需要给出提示
                    $ionicPopup.show({
                        title: '提示',
                        template: '<h5>您添加的是昨天的计划，提交后将无法修改</h5>',
                        buttons: [{
                            text: '继续编辑',
                            type: 'button-theme button-local button-outline'
                        }, {
                            text: '确定提交',
                            type: 'button-theme button-local',
                            onTap: function () {
                                checkDataAndSave('add');
                            }
                        }]
                    });
                } else {
                    checkDataAndSave('add');
                }
            }
        }

        function findTask(params) {
            return $http.get(basePath + '/third/jobrecord/findJSTXJObRecordDetailDTOByShowDate', {
                params: params
            }).then(function (response) {
                return response;
            })
        }

        function showViewTask(rowData) {
            $scope.pageData.showTip = true;
            $scope.pageData.beforeTask = [];
            $scope.pageData.afterTask = [];
            $scope.pageData.viewTips = '';
            $scope.pageData.taskCountMsg = '';
            $scope.view_task_modal.show().then(function () {
                $scope.currentRowData = angular.copy(rowData);
                $scope.currentRowData.formatShowDate = $scope.currentRowData.showDate.split('-')[1] + '月' +
                    $scope.currentRowData.showDate.split('-')[2] + '日';
                var now = new Date();
                // 最后可修改时间
                var lastModifyTime = new Date(
                    $scope.currentRowData.showDate.replace(/-/g, '/') + ' ' + $scope.pageData.rule.planTimeEnd + ' +0800');
                $scope.pageData.canModify = now < lastModifyTime;
                // 最后可总结时间
                var lastSummaryTime = new Date(
                    new Date($scope.currentRowData.showDate.replace(/-/g, '/') + ' ' + $scope.pageData.rule.sumaryTimeEnd + ' +0800')
                        .setSeconds(60 * 60 * 24));
                $scope.pageData.canSummary = now < lastSummaryTime;
                // 显示提示信息
                if ($scope.pageData.canModify && $scope.pageData.canSummary) {
                    $scope.pageData.viewTips = '请在' + $filter('date')(lastSummaryTime, 'MM月dd号HH:mm')
                        + '前完成总结，如需修改任务请在' + $scope.currentRowData.formatShowDate + $scope.pageData.rule.planTimeEnd
                        + '前操作';
                } else if (!$scope.pageData.canModify && $scope.pageData.canSummary) {
                    $scope.pageData.viewTips = '已超时不能修改任务，请在' + $filter('date')(lastSummaryTime, 'MM月dd号HH:mm')
                        + '前完成总结';
                } else {
                    $scope.pageData.viewTips = '超时不允许修改任务和总结计划';
                }
                // 查询
                $scope.findTask({showDate: $scope.currentRowData.showDate}).then(function (response) {
                    if (response.data.length > 0) {
                        $scope.currentRowData.summaryExplain = response.data[0].summaryExplain;
                        var count = 0, cancel = 0;
                        for (var i = 0; i < response.data.length; i++) {
                            if (response.data[i].del) {
                                $scope.delectedTask.push(response.data[i]);
                                continue;
                            }
                            if (response.data[i].completeState === 2) {
                                count += 1;
                            }
                            if (response.data[i].completeState === 3) {
                                cancel += 1;
                            }
                            if (response.data[i].planning === 1) { // 事先计划
                                $scope.pageData.beforeTask.push(response.data[i]);
                            } else { // 延后登记
                                $scope.pageData.afterTask.push(response.data[i]);
                            }
                        }
                        var length = $scope.pageData.beforeTask.length + $scope.pageData.afterTask.length;
                        $scope.pageData.taskCountMsg = '共' + (length) + '项任务、已完成' +
                            count + '项、未完成' + (length - count - cancel) + '项、任务取消' +
                            (cancel) + '项';
                    }
                });
            });
        }

        function closeViewTask() {
            $scope.view_task_modal.hide();
        }

        function modifyPageAddTask() {
            $scope.pageData.selectedTaskDetail = {
                taskName: '',
                isAdd: true,
                taskType: 1,
                remind: false,
                modify: true
            };
            var planTime = new Date($scope.currentRowData.showDate + ' ' + $scope.pageData.rule.planTime);
            var currentTime = new Date();
            if (currentTime > planTime) { // 延后登记
                $scope.pageData.selectedAttribute = 'afterTask';
                $scope.pageData.selectedTaskDetail.planning = 2;
            } else { // 事先计划
                $scope.pageData.selectedAttribute = 'beforeTask';
                $scope.pageData.selectedTaskDetail.planning = 1;
            }
            $scope.pageData.selectedAllTaskIndex = $scope.pageData.beforeTask.length + $scope.pageData.afterTask.length;
            $scope.pageData.selectedTaskIndex = $scope.pageData[$scope.pageData.selectedAttribute].length;
            $scope.pageData.selectedTaskDetail.taskType = 1; // 通常任务
            $scope.add_task_modal.show();
        }

        function showConclusionTask() {
            $scope.pageData.showTip = true;
            var now = new Date();
            // 最后可总结时间
            var lastSummaryTime = new Date(
                new Date($scope.currentRowData.showDate.replace(/-/g, '/') + ' ' + $scope.pageData.rule.sumaryTimeEnd + ' +0800')
                    .setSeconds(60 * 60 * 24));
            // 按时总结时间
            var firstSummaryTime = new Date(
                new Date($scope.currentRowData.showDate.replace(/-/g, '/') + ' ' + $scope.pageData.rule.sumaryTime + ' +0800')
                    .setSeconds(60 * 60 * 24));
            // 判断是不是按时总结
            $scope.pageData.summaryState = now > firstSummaryTime ? 3 : 2;
            // 显示提示信息
            $scope.pageData.conclusionTips = '请在' + $filter('date')(lastSummaryTime, 'MM月dd号HH:mm') + '前完成总结';
            $scope.conclusion_task_modal.show();
        }

        function closeConclusionTask() {
            $ionicPopup.show({
                title: '提示',
                template: '<h5>返回后，此次编辑的总结内容将丢失</h5>',
                buttons: [{
                    text: '继续编辑',
                    type: 'button-theme button-local button-outline'
                }, {
                    text: '确定返回',
                    type: 'button-theme button-local',
                    onTap: function () {
                        $scope.currentRowData.summaryExplain = '';
                        $scope.conclusion_task_modal.hide().then(function () {
                            $scope.conclusion_task_modal.remove().then(function () {
                                $ionicModal.fromTemplateUrl('task-conclusion.html', {
                                    scope: $scope,
                                    animation: 'slide-in-right'
                                }).then(function (modal) {
                                    $scope.conclusion_task_modal = modal;
                                }).then(function () {
                                    $scope.getAllTask({showDate: null}).then(function () {
                                        for (var i = 0; i < $scope.pageData.allDateList.length; i++) {
                                            for (var j = 0; j < $scope.pageData.allDateList[i].length; j++) {
                                                if ($scope.pageData.allDateList[i][j].showDate === $scope.currentRowData.showDate) {
                                                    $scope.currentRowData = angular.copy($scope.pageData.allDateList[i][j]);
                                                    break;
                                                }
                                            }
                                        }
                                        $scope.showViewTask($scope.currentRowData);
                                    });
                                })
                            })
                        });
                    }
                }]
            });
        }

        function submitConclusion() {
            var arr = [], i, msg, complete = 0, error = 0, cancel = 0;
            var regex = new RegExp(/^(\d+\.\d|\d+)$/);
            for (i = 0; i < $scope.pageData.beforeTask.length; i++) {
                if ($scope.pageData.beforeTask[i].taskName) {
                    if ($scope.pageData.beforeTask[i].consumptionTime &&
                        !regex.test($scope.pageData.beforeTask[i].consumptionTime)) {
                        error += 1;
                        break;
                    }
                    if ($scope.pageData.beforeTask[i].actualConsumptionTime &&
                        !regex.test($scope.pageData.beforeTask[i].actualConsumptionTime)) {
                        error += 1;
                        break;
                    }
                    $scope.pageData.beforeTask[i].planning = 1;
                    arr.push($scope.pageData.beforeTask[i]);
                    if ($scope.pageData.beforeTask[i].completeState === 2) {
                        complete += 1;
                    } else if ($scope.pageData.beforeTask[i].completeState === 3) {
                        cancel += 1;
                    }
                }
            }
            for (i = 0; i < $scope.pageData.afterTask.length; i++) {
                if ($scope.pageData.afterTask[i].taskName) {
                    if ($scope.pageData.afterTask[i].consumptionTime &&
                        !regex.test($scope.pageData.afterTask[i].consumptionTime)) {
                        error += 1;
                        break;
                    }
                    if ($scope.pageData.afterTask[i].actualConsumptionTime &&
                        !regex.test($scope.pageData.afterTask[i].actualConsumptionTime)) {
                        error += 1;
                        break;
                    }
                    $scope.pageData.afterTask[i].planning = 2;
                    arr.push($scope.pageData.afterTask[i]);
                    if ($scope.pageData.afterTask[i].completeState === 2) {
                        complete += 1;
                    } else if ($scope.pageData.afterTask[i].completeState === 3) {
                        cancel += 1;
                    }
                }
            }
            if (error) {
                $ionicPopup.show({
                    title: '提示',
                    template: '<h5>工时格式不正确</h5>',
                    buttons: [{
                        text: '确定',
                        type: 'button-theme button-local'
                    }]
                });
                return false;
            }
            msg = '共' + arr.length + '项任务，已完成' + complete + '项，未完成' +
                (arr.length - complete - cancel) + '项，任务取消' + cancel + '项';
            arr = arr.concat($scope.delectedTask);
            $ionicPopup.show({
                title: '提示',
                template: '<h5>' + msg + '</h5>',
                buttons: [{
                    text: '继续编辑',
                    type: 'button-theme button-local button-outline'
                }, {
                    text: '确定提交',
                    type: 'button-theme button-local',
                    onTap: function () {
                        $scope.saveData({
                            showDate: $scope.currentRowData.showDate,
                            summaryExplain: $scope.currentRowData.summaryExplain,
                            summaryState: $scope.pageData.summaryState
                        }, arr).then(function () {
                            $ionicLoading.show({
                                template: '<h4>提交成功</h4>',
                                scope: $scope,
                                duration: 1000
                            }).then(function () {
                                // $scope.showViewTask($scope.currentRowData);
                                $scope.conclusion_task_modal.hide().then(function () {
                                    $scope.conclusion_task_modal.remove().then(function () {
                                        $ionicModal.fromTemplateUrl('task-conclusion.html', {
                                            scope: $scope,
                                            animation: 'slide-in-right'
                                        }).then(function (modal) {
                                            $scope.conclusion_task_modal = modal;
                                        }).then(function () {
                                            $scope.getAllTask({showDate: null}).then(function () {
                                                for (var i = 0; i < $scope.pageData.allDateList.length; i++) {
                                                    for (var j = 0; j < $scope.pageData.allDateList[i].length; j++) {
                                                        if ($scope.pageData.allDateList[i][j].showDate === $scope.currentRowData.showDate) {
                                                            $scope.currentRowData = angular.copy($scope.pageData.allDateList[i][j]);
                                                            break;
                                                        }
                                                    }
                                                }
                                                $scope.showViewTask($scope.currentRowData);
                                            });
                                        })
                                    })
                                });
                            });
                        });
                    }
                }]
            });
        }

        $scope.showRemindTime = showRemindTime;

        function showRemindTime(detail) {
            var msg = $scope.currentRowData.formatShowDate + detail.remindTime + '提醒我';
            $ionicLoading.show({
                template: '<h5>' + msg + '</h5>',
                scope: $scope
            }).then(function () {
                $timeout(function () {
                    $ionicLoading.hide();
                }, 2000);
            })
        }

        function formatNumber(obj) {
            var newChar = obj.consumptionTime.substr(obj.consumptionTime.length - 1);
            var regex = new RegExp(/[^0-9|.]/);
            if (regex.test(newChar)) {
                obj.consumptionTime = obj.consumptionTime.substr(0, obj.consumptionTime.indexOf(newChar));
            }
            // obj.consumptionTime = parseFloat(obj.consumptionTime).toFixed(1);
        }

        // 获取 指定属性
        Array.prototype.getAttr = function (code) {
            if (this && this.length > 0) {
                if (code) {
                    return this[this.length - 1][code];
                } else {
                    return this[this.length - 1];
                }
            } else {
                return {};
            }
        };

        // 补充集合 数据
        Array.prototype.suppFun = function () {
            if (this.length < 6) {
                for (var i = this.length; i < 6; i++) {
                    this.push({taskName: '添加第' + i + '项新任务', isAdd: true, taskType: 1, remind: false, modify: true});
                }
            }
            // else if (this.length < 10) {
            //     this.push({taskName: '添加一项新任务'});
            // }
            return this;
        };

        // 判断id
        function checkId(info) {
            if (info.isAdd) {
                return false;
            } else {
                if (info.id) {
                    return true;
                } else if (info.xtgbAssId) {
                    return true;
                } else if (info.xtbgProjectId) {
                    return true;
                }
            }
            return false;
        }

        // 去除集合空 数据
        Array.prototype.goAir = function () {
            var res = [];
            for (var i = 0; i < this.length; i++) {
                var obj = this[i];
                if (checkId(obj)) {
                    res.push(this[i]);
                }
            }
            return res;
        };

        // 去重
        Array.prototype.goHeavy = function () {
            var res = [];
            var json = {};
            for (var i = 0; i < this.length; i++) {
                var obj = this[i];
                if (obj.taskType && obj.taskType === 1) {
                    if (obj.id) {
                        if (!json[obj.id]) {
                            res.push(obj);
                            json[obj.id] = true;
                        }
                    } else {
                        res.push(obj);
                    }
                } else {
                    var xtId = obj.xtgbAssId || obj.xtbgProjectId;
                    if (xtId) {
                        if (!json[xtId+i]) {
                            json[xtId+i] = true;
                            res.push(obj);
                        }
                    } else {
                        res.push(obj);
                    }
                }
            }
            return res;
        };
    }
})();
