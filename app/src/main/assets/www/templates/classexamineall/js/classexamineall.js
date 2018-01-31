/**
 * Created by YN on 2016/9/28.
 */
angular.module('starter').config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
    //任务管理
        .state('class_examine', {
            url: '/class_examine',
            templateUrl: 'templates/classexamineall/class_examine.html',
            cache: false
        })
        //添加任务
        .state('add_task', {
            url: '/add_task',
            templateUrl: 'templates/classexamineall/add_task.html',
            cache: false
        })
        //打分 班级考核
        .state('class_grade', {
            url: '/class_grade',
            templateUrl: 'templates/classexamineall/class_grade.html',
            cache: false
        })
        //填写表单
        .state('class_form', {
            url: '/class_form',
            templateUrl: 'templates/classexamineall/class_form.html',
            cache: false
        })
        /*==========班级巡查考核 德育检查人=============*/
        /*查找班级*/
        .state('class_search', {
            url: '/class_search',
            templateUrl: 'templates/classexamineall/class_search.html',
            cache: false
        })
        /*开始查找班级*/
        .state('search_classList', {
            url: '/search_classList',
            templateUrl: 'templates/classexamineall/search_classList.html',
            cache: false
        })
        /*选择考核项目*/
        .state('selectClass_examine_project', {
            url: '/selectClass_examine_project',
            templateUrl: 'templates/classexamineall/selectClass_examine_project.html',
            cache: false
        })
        /*选择班级*/
        .state('select_class', {
            url: '/select_class',
            templateUrl: 'templates/classexamineall/select_class.html',
            cache: false
        })/*巡查打分*/
        .state('class_patrol', {
            url: '/class_patrol',
            templateUrl: 'templates/classexamineall/class_patrol.html',
            cache: false
        })

})		//班级考核 任务管理
    .controller('classExamineCtrl', ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location',"ynuiNotification",
        function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location,ynuiNotification) {

            $scope.getData = function (func) {
                $scope.MoraleduChScoreCheckVOList = [];
                $http.get(originBaseUrl + '/third/classExamine/queryMyCheckTaskByCheckUser.htm?time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.MoraleduChScoreCheckVOList = data.result;
                        if ($scope.MoraleduChScoreCheckVOList.length > 0) {
                            $scope.emptyInfo = false;
                        } else {
                            $scope.emptyInfo = true;              //展示空数据提示
                            $scope.dataErrorMsg = "没有可选择的考核任务，请先添加！";
                        }
                        if (func) {
                            func();
                        }
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            };
            $scope.getData();
            //到 考核打分页面
            $scope.toClassGrade = function (item) {
                if(item.isLock){
                    ynuiNotification.error({msg:"该打分表的"+item.checkDate+"考核日期在操行分锁定范围内，不能进行打分！" });
                    return false;
                }
                $location.path("/class_grade").search("item", JSON.stringify({
                    "checkScoreId": item.id,
                    "noOverClassNum": item.noOverClassNum,
                    "lastIsClassForm": false
                }));
            };
            //新增考核任务
            $scope.toAddTask = function () {
                $location.path("/add_task");
            };

            //刷新
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                //置空数据，在获取时候叠加数据
                $scope.MoraleduChScoreCheckVOList = [];
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
        }])
    //新增考核任务
    .controller('addTaskCtrl', ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', 'ynuiNotification', '$location',
        function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, ynuiNotification, $location) {
            $scope.firstObj = {
                alreadyScore: false,     //是否是已经查询（防止请求数据还没返回就显示  没有数据）
                isOneScore: true,        //是否是第一次查询
                alreadyRegion: false
            };
            $scope.addTaskVo = {
                tableId: "",
                tableName: "请选择",
                checkType: 1,
                regionIdOrGroupId: "",
                groupName: "请先选择打分表",
                checkTimes: "",
                termId: "",
                termName: "",
                checkDate: ""
            };
            var submitErrorTitle = {
                tableId: "打分表",
                regionIdOrGroupId: "考核区域",
                checkTimes: "考核次数",
                termId: "学期",
                checkDate: "考核日期"
            };
            //查询打分表下拉
            var getScoreTable = function () {
                $scope.firstObj.alreadyScore = false;
                if ($scope.firstObj.isOneScore) {
                    $scope.scoreTableList = [];
                    $http.get(originBaseUrl + "/third/classExamine/queryMyMoraleduChScoreTable.htm?status=true" + "&_datatime=" + new Date().getTime()).success(function (data) {
                        if (data.status == 0) {
                            $scope.scoreTableList = data.result;
                            $scope.firstObj.alreadyScore = true;
                        }
                    });
                }
            };
            $scope.checkRegionList = [];
            var checkRegionListAdmin = [];
            var checkRegionListMy = [];
            //管理员考核区域
            $scope.getRegionList = function (tableId) {
                if (!tableId) {
                    return false;
                }
                checkRegionListAdmin = [];
                $scope.addTaskVo.regionIdOrGroupId = "";
                $scope.firstObj.alreadyRegion = false;
                $http.get(originBaseUrl + "/third/classExamine/queryAdminRegionUsedTableRelationUsed.htm?tableId=" + tableId + "&_datatime=" + new Date().getTime()).success(function (data) {
                    if (data.status == 0) {
                        checkRegionListAdmin = data.result;
                        $scope.checkRegionList = data.result;
                        $scope.firstObj.alreadyRegion = true;
                    }
                });
            }
            //我的考核区域
            $scope.getTableGroupList = function (tableId) {
                if (!tableId) {
                    return false;
                }
                checkRegionListMy = [];
                $scope.addTaskVo.regionIdOrGroupId = "";
                $scope.firstObj.alreadyRegion = false;
                $http.get(originBaseUrl + "/third/classExamine/queryMyTableGroupList.htm?tableId=" + tableId + "&_datatime=" + new Date().getTime()).success(function (data) {
                    if (data.status == 0) {
                        checkRegionListMy = data.result;
                        $scope.checkRegionList = data.result;
                        $scope.firstObj.alreadyRegion = true;
                    }
                });
            };
            //考核次数下拉
            var getCheckTimeList = function (totality) {
                $scope.checkTimeList = [];
                for (var i = 1; i <= totality; i++) {
                    $scope.checkTimeList.push({
                        id: i,
                        name: "第" + i + "次"
                    });
                }
                $scope.addTaskVo.checkTimes = $scope.checkTimeList[0].id;
            };
            $scope.$watch("addTaskVo.checkType", function () {
                if ($scope.addTaskVo.checkType == 0) {
                    $scope.checkRegionList = checkRegionListAdmin;
                    $scope.addTaskVo.regionIdOrGroupId = "";
                } else {
                    $scope.checkRegionList = checkRegionListMy;
                    $scope.addTaskVo.regionIdOrGroupId = "";
                }
            });
            //点击选择打分表
            $scope.selectedScore = function (item) {
                if (item.id == $scope.addTaskVo.tableId) {
                    return;
                }
                $scope.addTaskVo.tableId = item.id;
                $scope.addTaskVo.tableName = item.name;
                $scope.addTaskVo.regionIdOrGroupId = "";
                $scope.addTaskVo.groupName = "请选择";
                if ($scope.addTaskVo.checkType == 0) {
                    $scope.getRegionList(item.id);
                } else {
                    $scope.getTableGroupList(item.id);
                }
                ;
                getCheckTimeList(item.gender);
                $scope.closeModal();
            };
            //获取学期下拉
            var getTermList = function () {
                $scope.termList = [];
                $http.get(originBaseUrl + "/third/classExamine/getCurrentTermAndListSelect.htm?isASC=false" + "&_datatime=" + new Date().getTime()).success(function (data) {
                    if (data.status == 0) {
                        if (data.result.termList) {
                            $scope.termList = data.result.termList;
                        } else {
                            $scope.termList = [];
                        }
                        if (data.result.currentTerm) {
                            $scope.addTaskVo.termId = data.result.currentTerm.id;
                            $scope.addTaskVo.termName = data.result.currentTerm.name;
                        } else {
                            if ($scope.termList.length > 0) {
                                $scope.addTaskVo.termId = $scope.termList[0].id;
                                $scope.addTaskVo.termName = $scope.termList[0].name;
                            } else {
                                $scope.addTaskVo.termId = "";
                                $scope.addTaskVo.termName = "";
                            }
                        }
                        $scope.nowTerm = {
                            id: $scope.addTaskVo.termId,
                            name: $scope.addTaskVo.termName
                        }
                        getTermDateRegion();
                    }
                });
            };
            getTermList();
            //获取某个学期的对应日期区间及默认日期
            var getTermDateRegion = function () {
                var startDate = "";
                var endDate = "";
                var nowDate = "";
                $http.get(originBaseUrl + '/third/classExamine/queryDefaultCheckDateByTermId.htm?termId=' + $scope.addTaskVo.termId + '&time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        startDate = data.result[0];
                        endDate = data.result[1];
                        nowDate = data.result[2];
                        var startDateArr = startDate.split('-');
                        var endDateArr = endDate.split('-');
                        $scope.checkDate = nowDate;
                        $scope.addTaskVo.checkDate = nowDate;
                        //限制日期组件的开始结束日期
                        $scope.addTaskKHRQSettings = {
                            minDate: new Date(parseInt(startDateArr[0]), parseInt(startDateArr[1]) - 1, parseInt(startDateArr[2])),
                            maxDate: new Date(parseInt(endDateArr[0]), parseInt(endDateArr[1]) - 1, parseInt(endDateArr[2]))
                        };
                    }
                });

            };
            //回填选中的日期
            $scope.selectedDate = function (item) {
                $scope.addTaskVo.checkDate = item;
                $scope.$apply();
            };
            //选择学期
            $scope.selectedTerm = function (item) {
                $scope.addTaskVo.termId = item.id;
                $scope.addTaskVo.termName = item.name;
                getTermDateRegion();
                $scope.closeModal3()
            };
            //生成考核任务
            $scope.addScoreCheckTask = function () {
                var obj = angular.copy($scope.addTaskVo);
                delete obj.tableName;
                delete obj.groupName;
                delete obj.termName;
                var isNoNull = true;
                var goOn = true;
                angular.forEach(obj, function (value, key) {
                    if (value == "" && value != "0" && goOn) {
                        ynuiNotification.error({msg: submitErrorTitle[key] + "不能为空！"});
                        isNoNull = false;
                        goOn = false;
                    }
                });
                if (!isNoNull) {
                    return false;
                }
                $http.post(originBaseUrl + '/third/classExamine/addChScoreCheckTaskByCheckUser.htm?', obj).success(function (data) {
                    if (data.status == 0) {
                        ynuiNotification.success({msg: "添加任务成功！"});
                        $location.path("/class_examine");
                    } else {
                        ynuiNotification.error({msg: "提交失败！" + data.message});
                    }
                })
            };
            $scope.doBack = function () {
                history.back(-1);
            };
            //刷新
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                //置空数据，在获取时候叠加数据
                $scope.addTaskVo = {
                    tableId: "",
                    tableName: "请选择",
                    checkType: 1,
                    regionIdOrGroupId: "",
                    groupName: "请先选择打分表",
                    checkTimes: "",
                    termId: $scope.nowTerm.id,
                    termName: $scope.nowTerm.name,
                    checkDate: $scope.checkDate
                };
                $scope.checkTimeList = [];
                setTimeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 200);
            };
            $ionicModal.fromTemplateUrl('template.html', {
                scope: $scope,
                animation: 'slide-in-up'
            }).then(function (modal) {
                $scope.modal = modal;
            });
            $scope.openModal = function () {
                $scope.modal.show();
                getScoreTable();
            };
            $scope.closeModal = function () {
                $scope.modal.hide();
            };


            $ionicModal.fromTemplateUrl('template1.html', {
                scope: $scope,
                animation: 'slide-in-up'
            }).then(function (modal) {
                $scope.modal1 = modal;
            });
            $scope.openModal1 = function () {
                $scope.modal1.show();
            };
            $scope.closeModal1 = function () {
                $scope.modal1.hide();
            };


            $ionicModal.fromTemplateUrl('template2.html', {
                scope: $scope,
                animation: 'slide-in-up'
            }).then(function (modal) {
                $scope.modal2 = modal;
            });
            $scope.openModal2 = function () {
                $scope.modal2.show();
            };
            $scope.closeModal2 = function () {
                $scope.modal2.hide();
            };

            $ionicModal.fromTemplateUrl('template3.html', {
                scope: $scope,
                animation: 'slide-in-up'
            }).then(function (modal) {
                $scope.modal3 = modal;
            });
            $scope.openModal3 = function () {
                $scope.modal3.show();
            };
            $scope.closeModal3 = function () {
                $scope.modal3.hide();
            };
        }])
    //班级考核 打分
    .controller('classGradeCtrl', ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', 'ynuiNotification', '$location', '$filter',
        function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, ynuiNotification, $location, $filter) {

            $ionicModal.fromTemplateUrl('grouping.html', {
                scope: $scope,
                animation: 'slide-in-up'
            }).then(function (modal) {
                $scope.modal1 = modal;
            });
            $scope.openGrouping = function () {
                $scope.modal1.show();
                $scope.someOneShowName.searchClassName = "";
            };
            $scope.closeGrouping = function (item) {
                console.log(item);
                if(item.isLock == "true"){
                    ynuiNotification.error({msg: item.checkDate});
                    return false;
                }
                $scope.modal1.hide();
                $scope.someOneShowName.searchItemScoreName = "";
                someOneCondition.classRoomMidId = item.classRoomMidId;
                $scope.selectedClassInfo = item;
                for (var i in $scope.nowMoraleduMobileChClassDetailVOsListCopy) {
                    if ($scope.nowMoraleduMobileChClassDetailVOsListCopy[i].classRoomMidId == item.classRoomMidId) {
                        someOneCondition.classRoomIndex = parseInt(i);
                        break;
                    }
                }
                getClassCheckItemScore();
            };
            $scope.isOver = false;
            var lastItem = JSON.parse($location.search().item);
            var checkScoreId = lastItem.checkScoreId;
            var noOverClassNum = lastItem.noOverClassNum;
            var groupOrRegionIndex = null;//有则代表是 从 考核项打分页面跳转过来
            var classRoomIndex = null;//有则代表是 从 考核项打分页面跳转过来
            var lastIsClassForm = lastItem.lastIsClassForm;//等于true，则代表是打分页面的保存状态过来，需要原样返回，等于false就代表是首页过来或者打分的取消过来，用于返回到首页
            if (lastItem.groupOrRegionIndex || lastItem.groupOrRegionIndex == 0) {
                groupOrRegionIndex = lastItem.groupOrRegionIndex;
            }
            ;
            if (lastItem.classRoomIndex || lastItem.classRoomIndex == 0) {
                classRoomIndex = lastItem.classRoomIndex;
            }
            ;
            $scope.someOneShowName = {
                groupOrRegionName: "",//考核任务的考核区域下的分组名称+考核任务的考核区域名称
                searchClassName: "",//搜索班级的输入框的输入值
                searchItemScoreName: ""//搜索考核项输入框的输入值
            };
            var someOneCondition = {
                groupOrRegionId: "",//考核任务的考核区域下的分组或考核任务的考核区域的id
                groupOrRegionIndex: 0,//考核任务的考核区域下的分组或考核任务的考核区域在所在list的位置
                classRoomIndex: 0,//班级在所在list的位置
                classRoomMidId: ""//班级场地主键ID
            };
            $scope.canSubmit = {
                one: false
            }
            $scope.getData = function (func) {
                $scope.someOneShowName = {
                    groupOrRegionName: "",//考核任务的考核区域下的分组名称+考核任务的考核区域名称
                    searchClassName: ""//搜索班级的输入框的输入值
                };
                someOneCondition = {
                    groupOrRegionId: "",//考核任务的考核区域下的分组或考核任务的考核区域的id
                    groupOrRegionIndex: 0,//考核任务的考核区域下的分组或考核任务的考核区域在所在list的位置
                    classRoomIndex: 0,//班级在所在list的位置
                    classRoomMidId: ""//班级场地主键ID
                }
                $scope.MoraleduMobileChGroupClassVOList = [];
                $scope.nowMoraleduMobileChClassDetailVOsList = [];
                $scope.nowMoraleduMobileChClassDetailVOsListCopy = [];
                $scope.selectedClassInfo = {};
                $scope.closeIcon = true;
                $http.get(originBaseUrl + '/third/classExamine/queryChGroupClassInfoList.htm?checkScoreId=' + checkScoreId + '&time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.MoraleduMobileChGroupClassVOList = data.result;
                        if ($scope.MoraleduMobileChGroupClassVOList.length > 0) {
                            $scope.emptyInfo = false;
                            if (groupOrRegionIndex || groupOrRegionIndex == 0) {
                                $scope.someOneShowName.groupOrRegionName = $scope.MoraleduMobileChGroupClassVOList[groupOrRegionIndex].name;
                                someOneCondition.groupOrRegionId = $scope.MoraleduMobileChGroupClassVOList[groupOrRegionIndex].groupIdOrRegionId;
                                someOneCondition.groupOrRegionIndex = groupOrRegionIndex;
                                $scope.nowMoraleduMobileChClassDetailVOsList = $scope.MoraleduMobileChGroupClassVOList[groupOrRegionIndex].moraleduMobileChClassDetailVOs;
                                $scope.nowMoraleduMobileChClassDetailVOsListCopy = $scope.MoraleduMobileChGroupClassVOList[groupOrRegionIndex].moraleduMobileChClassDetailVOs;
                            } else {
                                $scope.someOneShowName.groupOrRegionName = $scope.MoraleduMobileChGroupClassVOList[0].name;
                                someOneCondition.groupOrRegionId = $scope.MoraleduMobileChGroupClassVOList[0].groupIdOrRegionId;
                                someOneCondition.groupOrRegionIndex = 0;
                                $scope.nowMoraleduMobileChClassDetailVOsList = $scope.MoraleduMobileChGroupClassVOList[0].moraleduMobileChClassDetailVOs;
                                $scope.nowMoraleduMobileChClassDetailVOsListCopy = $scope.MoraleduMobileChGroupClassVOList[0].moraleduMobileChClassDetailVOs;
                            }
                            if (noOverClassNum > 0) {
                                if ($scope.nowMoraleduMobileChClassDetailVOsListCopy && $scope.nowMoraleduMobileChClassDetailVOsListCopy.length > 0) {
                                    if (classRoomIndex || classRoomIndex == 0) {
                                        someOneCondition.classRoomMidId = $scope.nowMoraleduMobileChClassDetailVOsListCopy[classRoomIndex].classRoomMidId;
                                        someOneCondition.classRoomIndex = classRoomIndex;
                                        $scope.selectedClassInfo = $scope.nowMoraleduMobileChClassDetailVOsListCopy[classRoomIndex];
                                    } else {
                                        for (var i in $scope.nowMoraleduMobileChClassDetailVOsListCopy) {
                                            if (!$scope.nowMoraleduMobileChClassDetailVOsListCopy[i].isRecode) {
                                                someOneCondition.classRoomMidId = $scope.nowMoraleduMobileChClassDetailVOsListCopy[i].classRoomMidId;
                                                someOneCondition.classRoomIndex = parseInt(i);
                                                $scope.selectedClassInfo = $scope.nowMoraleduMobileChClassDetailVOsListCopy[i];
                                                break;
                                            }
                                        }
                                    }
                                    if (someOneCondition.classRoomMidId) {
                                        getClassCheckItemScore();
                                    } else {
                                        $scope.openGrouping();
                                        $scope.isOver = true;
                                        $scope.canSubmit.one = true;
                                    }
                                } else {
                                    $scope.openGrouping();
                                    $scope.isOver = true;
                                    $scope.canSubmit.one = true;
                                }
                            } else {
                                $scope.openGrouping();
                                $scope.isOver = true;
                                $scope.canSubmit.one = true;
                            }
                            if ($scope.nowMoraleduMobileChClassDetailVOsListCopy.length < 1) {
                                $scope.emptyInfo = true;              //展示空数据提示
                                $scope.dataErrorMsg = "没有可选择的班级!";
                            }
                        }
                        if (func) {
                            func();
                        }
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.canSubmit.one = true;
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            };
            $scope.getData();
            //上 下 一区域分组
            $scope.toLastOrNextTerm = function (isNext) {
                var canLastOrNext = false;
                if (isNext > 0) {
                    if (someOneCondition.groupOrRegionIndex < $scope.MoraleduMobileChGroupClassVOList.length - 1) {
                        someOneCondition.groupOrRegionIndex += 1;
                        groupOrRegionIndex = someOneCondition.groupOrRegionIndex;
                        canLastOrNext = true;
                    } else {
                        canLastOrNext = false;
                        ynuiNotification.error({msg: "已经是最后1个区域或分组！"});
                    }
                } else {
                    if (someOneCondition.groupOrRegionIndex > 0) {
                        someOneCondition.groupOrRegionIndex -= 1;
                        groupOrRegionIndex = someOneCondition.groupOrRegionIndex;
                        canLastOrNext = true;
                    } else {
                        canLastOrNext = false;
                        ynuiNotification.error({msg: "已经是第1个区域或分组！"});
                    }
                }
                if (canLastOrNext) {
                    noOverClassNum = 0;
                    $scope.someOneShowName.searchClassName = "";
                    $scope.someOneShowName.groupOrRegionName = $scope.MoraleduMobileChGroupClassVOList[(someOneCondition.groupOrRegionIndex)].name;
                    someOneCondition.groupOrRegionId = $scope.MoraleduMobileChGroupClassVOList[(someOneCondition.groupOrRegionIndex)].groupIdOrRegionId;
                    $scope.nowMoraleduMobileChClassDetailVOsList = $scope.MoraleduMobileChGroupClassVOList[(someOneCondition.groupOrRegionIndex)].moraleduMobileChClassDetailVOs;
                    $scope.nowMoraleduMobileChClassDetailVOsListCopy = $scope.MoraleduMobileChGroupClassVOList[(someOneCondition.groupOrRegionIndex)].moraleduMobileChClassDetailVOs;
                    if ($scope.nowMoraleduMobileChClassDetailVOsList.length < 1) {
                        $scope.emptyInfo = true;              //展示空数据提示
                        $scope.dataErrorMsg = "没有可选择的班级!";
                    } else {
                        $scope.emptyInfo = false;
                    }
                }
            };
            $scope.$watch("someOneShowName.searchClassName", function (newVal, oldVal) {
                if ($scope.someOneShowName.searchClassName == "") {
                    $scope.isSearchClassName = false;
                } else {
                    $scope.isSearchClassName = true;
                }
                if (newVal != oldVal) {
                    $scope.nowMoraleduMobileChClassDetailVOsList = $filter('filter')($scope.nowMoraleduMobileChClassDetailVOsListCopy, {platformSysAdminClassName: $scope.someOneShowName.searchClassName});
                    if ($scope.nowMoraleduMobileChClassDetailVOsList.length < 1) {
                        $scope.emptyInfo = true;              //展示空数据提示
                        $scope.dataErrorMsg = "没有符合条件的班级!";
                    } else {
                        $scope.emptyInfo = false;
                    }
                }
            }, true);
            $scope.closeIconClick = function (item) {
                if (item == $scope.someOneShowName.searchItemScoreName) {
                    $scope.someOneShowName.searchItemScoreName = "";
                } else if (item == $scope.someOneShowName.searchClassName) {
                    $scope.someOneShowName.searchClassName = "";
                }
            };
            //查询班级下考核项
            var getClassCheckItemScore = function () {
                $scope.MoraleduMobileChItemScoreVOList = [];
                $scope.MoraleduMobileChItemScoreVOListCopy = [];
                $scope.someOneShowName.searchItemScoreName = "";
                $http.get(originBaseUrl + '/third/classExamine/queryChItemScoreInfoListByMidId.htm?scoreCheckMidId=' + someOneCondition.classRoomMidId+"&checkDate="+ + '&time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.MoraleduMobileChItemScoreVOList = data.result;
                        $scope.MoraleduMobileChItemScoreVOListCopy = data.result;
                        if ($scope.MoraleduMobileChItemScoreVOList.length > 0) {
                            $scope.emptyInfoClass = false;
                        } else {
                            $scope.emptyInfoClass = true;              //展示空数据提示
                            $scope.dataErrorMsgClass = "没有考核项目！";
                        }
                        $scope.isOver = true;
                        $scope.canSubmit.one = true;
                    }
                }).error(function () {
                    $scope.canSubmit.one = true;
                    $scope.emptyInfoClass = true;
                    $scope.dataErrorMsgClass = '加载失败，请重试！';
                });
            };
            //上  下 一班
            $scope.isLastOrNextClass = function (isNext) {
                var canLastOrNext = false;
                if (isNext > 0) {
                    if (someOneCondition.classRoomIndex < $scope.nowMoraleduMobileChClassDetailVOsListCopy.length - 1) {
                        someOneCondition.classRoomIndex += 1;
                        canLastOrNext = true;
                    } else {
                        canLastOrNext = false;
                        ynuiNotification.error({msg: "没有了！"});
                    }
                } else {
                    if (someOneCondition.classRoomIndex > 0) {
                        someOneCondition.classRoomIndex -= 1;
                        canLastOrNext = true;
                    } else {
                        canLastOrNext = false;
                        ynuiNotification.error({msg: "没有了！"});
                    }
                }
                if (canLastOrNext) {
                    $scope.someOneShowName.searchItemScoreName = "";
                    someOneCondition.classRoomMidId = $scope.nowMoraleduMobileChClassDetailVOsListCopy[(someOneCondition.classRoomIndex)].classRoomMidId;
                    $scope.selectedClassInfo = $scope.nowMoraleduMobileChClassDetailVOsListCopy[(someOneCondition.classRoomIndex)];
                    getClassCheckItemScore();
                }
            };
            $scope.$watch("someOneShowName.searchItemScoreName", function (newVal, oldVal) {
                if ($scope.someOneShowName.searchItemScoreName == "") {
                    $scope.isSearchItemScoreName = false
                } else {
                    $scope.isSearchItemScoreName = true;
                }
                if (newVal != oldVal) {
                    $scope.MoraleduMobileChItemScoreVOList = $filter('filter')($scope.MoraleduMobileChItemScoreVOListCopy, {itemName: $scope.someOneShowName.searchItemScoreName});
                    if ($scope.MoraleduMobileChItemScoreVOList.length < 1) {
                        $scope.emptyInfoClass = true;              //展示空数据提示
                        $scope.dataErrorMsgClass = "没有符合条件的考核项!";
                    } else {
                        $scope.emptyInfoClass = false;
                    }
                }

            }, true);
            //js浮点数计算不准确计算解决
            var mul = function (a, b) {
                if (!a) {
                    a = 0;
                }
                if (!b) {
                    b = 0;
                }
                var c = 0,
                    d = a.toString(),
                    e = b.toString();
                try {
                    c += d.split(".")[1].length;
                } catch (f) {
                }
                try {
                    c += e.split(".")[1].length;
                } catch (f) {
                }
                return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
            }
            var accAdd = function (a, b) {
                if (!a) {
                    a = 0;
                }
                if (!b) {
                    b = 0;
                }
                var c, d, e;
                try {
                    c = a.toString().split(".")[1].length;
                } catch (f) {
                    c = 0;
                }
                try {
                    d = b.toString().split(".")[1].length;
                } catch (f) {
                    d = 0;
                }
                return e = Math.pow(10, Math.max(c, d)), (mul(a, e) + mul(b, e)) / e;
            };
            //所有考核项总分
            $scope.returnTotalScore = function () {
                var sum = 0;
                angular.forEach($scope.MoraleduMobileChItemScoreVOListCopy, function (item) {
                    sum = accAdd(sum, item.itemScore);
                });
                return sum;
            };
            //到填写单个考核项打分页面
            $scope.toClassForm = function (item) {
                if(item.isLock == "true"){
                    ynuiNotification.error({msg: item.checkDate});
                    return false;
                }
                $location.path("/class_form").search("item", JSON.stringify({
                    "checkScoreId": checkScoreId,
                    "noOverClassNum": 1,
                    "groupOrRegionIndex": someOneCondition.groupOrRegionIndex,
                    "classRoomIndex": someOneCondition.classRoomIndex,
                    "itemId": item.itemId,
                    "scoreCheckMidId": someOneCondition.classRoomMidId,
                    "classId": $scope.selectedClassInfo.platformSysAdminClassId
                }));
            };
            //提交
            $scope.submitScore = function () {
                if (!$scope.canSubmit.one) {
                    return false;
                }
                $scope.canSubmit.one = false;
                var obj = {
                    isSubmit: true,
                    scoreCheckMidId: someOneCondition.classRoomMidId,
                    classTotalScore: $scope.returnTotalScore(),
                    checkUserIdStr: $scope.selectedClassInfo.checkUserIdStr,
                    checkUserNameStr: $scope.selectedClassInfo.checkUserNameStr,
                    time: (new Date()).getTime()
                };
                $http.post(originBaseUrl + '/third/classExamine/saveOrUpdateScoreCheckDetailWithMobile.htm?', obj).success(function (data) {
                    if (data.status == 0) {
                        ynuiNotification.success({msg: $scope.selectedClassInfo.platformSysAdminClassName + "考核完成！"});
                        if (someOneCondition.classRoomIndex < $scope.nowMoraleduMobileChClassDetailVOsListCopy.length - 1) {
                            $scope.nowMoraleduMobileChClassDetailVOsList[(someOneCondition.classRoomIndex)].totalScore = $scope.returnTotalScore();
                            $scope.nowMoraleduMobileChClassDetailVOsList[(someOneCondition.classRoomIndex)].isRecode = true;
                            someOneCondition.classRoomIndex += 1;
                            someOneCondition.classRoomMidId = $scope.nowMoraleduMobileChClassDetailVOsListCopy[(someOneCondition.classRoomIndex)].classRoomMidId;
                            $scope.selectedClassInfo = $scope.nowMoraleduMobileChClassDetailVOsListCopy[(someOneCondition.classRoomIndex)];
                            $scope.nowMoraleduMobileChClassDetailVOsListCopy[(someOneCondition.classRoomIndex - 1)].isRecode = true;
                            getClassCheckItemScore();
                        } else {
                            noOverClassNum = 0;
                            $scope.getData(function () {
                                $scope.$broadcast('scroll.infiniteScrollComplete');
                                $ionicScrollDelegate.scrollTop();
                                $ionicLoading.hide();
                                $scope.isOver = true;
                            });
                        }
                    } else {
                        ynuiNotification.error({msg: data.message});
                    }
                }).error(function () {
                    $scope.canSubmit.one = true;
                    ynuiNotification.error({msg: "请求失败！"});
                })
            };
            //返回到任务管理页面
            $scope.doBack = function () {
                $scope.modal1.hide();
                if (lastIsClassForm) {
                    history.back(-1);
                } else {
                    $location.path("/class_examine");
                }
            };
            //刷新
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.isOver = false;
                //置空数据，在获取时候叠加数据
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                    $scope.isOver = true;
                });
            };
        }])
    //班级考核 打分  填写表单
    .controller('classFormCtrl', ['$scope', '$http', '$ionicModal', 'ynuiNotification', 'viewsParameterService', '$ionicPopup', '$location', '$filter', '$ionicLoading', '$ionicScrollDelegate',
        function ($scope, $http, $ionicModal, ynuiNotification, viewsParameterService, $ionicPopup, $location, $filter, $ionicLoading, $ionicScrollDelegate) {
            $ionicModal.fromTemplateUrl('student.html', {
                scope: $scope,
                animation: 'slide-in-left'
            }).then(function (modal) {
                $scope.modal = modal;
            });
            $scope.openName = function () {
                getClassStu();
                $scope.modal.show();
            };
            $scope.closeName = function () {
                $scope.modal.hide();
            };
            $scope.isOver = false;
            var lastItem = JSON.parse($location.search().item);
            var checkScoreId = lastItem.checkScoreId;
            var noOverClassNum = lastItem.noOverClassNum;
            var groupOrRegionIndex = lastItem.groupOrRegionIndex;
            var classRoomIndex = lastItem.classRoomIndex;
            var itemId = lastItem.itemId;
            var scoreCheckMidId = lastItem.scoreCheckMidId;
            var classId = lastItem.classId;

            $scope.options = {};
            $scope.isOpen = false;
            $scope.someOneShowName = {
                searchClassStu: ""
            };
            $scope.isHaveChange = false;
            $scope.submitObj = {};
            $scope.oldSubmitObj = {};
            $scope.getDfsIds = function (data) {
                if (!$scope.isHaveChange) {
                    if ($scope.submitObj.fastDfsIds.length != data.data.length) {
                        $scope.isHaveChange = true;
                    } else {
                        for (var i in $scope.submitObj.fastDfsIds) {
                            if ($scope.submitObj.fastDfsIds[i] != data.data[i]) {
                                $scope.isHaveChange = true;
                                break;
                            }
                        }
                    }
                }
                ;
                $scope.submitObj.fastDfsIds = angular.copy(data.data);
            };
            $scope.getData = function (func) {
                $http.get(originBaseUrl + '/third/classExamine/queryChItemScoreInfo.htm?itemId=' + itemId +'&checkDate='+ '&scoreCheckMidId=' + scoreCheckMidId + '&time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.submitObj = data.result;
                        $scope.submitObj.stuIdList = [];
                        if ($scope.submitObj.stuInfoList) {
                            angular.forEach($scope.submitObj.stuInfoList, function (item) {
                                $scope.submitObj.stuIdList.push(item.id);
                            })
                        }
                        $scope.oldSubmitObj = angular.copy(data.result);
                        if (viewsParameterService.getParameter().isReturn && viewsParameterService.getParameter().itemId && viewsParameterService.getParameter().itemId == itemId) {
                            $scope.submitObj = viewsParameterService.getParameter().submitObj;
                            $scope.isHaveChange = viewsParameterService.getParameter().isHaveChange;
                        }
                        $scope.options.initBackFile($scope.submitObj.fastDfsIds);
                        $scope.emptyInfo = false;
                        $scope.returnItemScore();
                        $scope.isOver = true;
                        if (func) {
                            func();
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                        history.go(-1);
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            };
            $scope.getData();


            //返回打分页面（保存当前页面数据）
            $scope.returnClassGrade = function () {
                viewsParameterService.setParameter({
                    itemId: itemId,
                    submitObj: $scope.submitObj,
                    isHaveChange: $scope.isHaveChange,
                    isReturn: true
                });
                $location.path("/class_grade").search("item", JSON.stringify({
                    "checkScoreId": checkScoreId,
                    "noOverClassNum": noOverClassNum,
                    "groupOrRegionIndex": groupOrRegionIndex,
                    "classRoomIndex": classRoomIndex,
                    "lastIsClassForm": true
                }));
            };
            //js浮点数计算不准确计算解决
            var mul = function (a, b) {
                var c = 0,
                    d = a.toString(),
                    e = b.toString();
                try {
                    c += d.split(".")[1].length;
                } catch (f) {
                }
                try {
                    c += e.split(".")[1].length;
                } catch (f) {
                }
                return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
            }
            var accAdd = function (a, b) {
                var c, d, e;
                try {
                    c = a.toString().split(".")[1].length;
                } catch (f) {
                    c = 0;
                }
                try {
                    d = b.toString().split(".")[1].length;
                } catch (f) {
                    d = 0;
                }
                return e = Math.pow(10, Math.max(c, d)), (mul(a, e) + mul(b, e)) / e;
            }
            var accSub = function (a, b) {
                var c, d, e;
                try {
                    c = a.toString().split(".")[1].length;
                } catch (f) {
                    c = 0;
                }
                try {
                    d = b.toString().split(".")[1].length;
                } catch (f) {
                    d = 0;
                }
                return e = Math.pow(10, Math.max(c, d)), (mul(a, e) - mul(b, e)) / e;
            }
            $scope.isAddScore = function (isAdd) {
                if ($scope.submitObj.score == "") {
                    $scope.submitObj.score = 0;
                }
                if (isAdd > 0) {
                    $scope.submitObj.score = accAdd($scope.submitObj.score, 0.1);
                } else {
                    $scope.submitObj.score = accSub($scope.submitObj.score, 0.1);
                }
                if ($scope.submitObj.scoreType == 0) {
                    if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.addBeginScore) {
                        ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                        $scope.submitObj.score = $scope.submitObj.addBeginScore;
                    } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.addEndScore) {
                        ynuiNotification.error({msg: "不能大于" + $scope.submitObj.addEndScore});
                        $scope.submitObj.score = $scope.submitObj.addEndScore;
                    }
                } else {
                    if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.delEndScore) {
                        ynuiNotification.error({msg: "不能大于" + $scope.submitObj.delEndScore});
                        $scope.submitObj.score = $scope.submitObj.delEndScore;
                    } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.delBeginScore) {
                        ynuiNotification.error({msg: "不能小于" + $scope.submitObj.delBeginScore});
                        $scope.submitObj.score = $scope.submitObj.delBeginScore;
                    }
                }
                $scope.returnItemScore();
            };
            $scope.$watch(function () {
                return $scope.submitObj.score;
            }, function (newVal, oldVal) {
                var reg = /^\d+\.?\d{0,2}$/;
                if (newVal && !reg.test(newVal)) {
                    $scope.submitObj.score = oldVal;
                }
            });
            $scope.changeScore = function () {
                var canGoOn = false;
                if ($scope.submitObj.scoreType == 0) {
                    if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.addBeginScore) {
                        ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                        $scope.submitObj.score = $scope.submitObj.addBeginScore;
                        canGoOn = true;
                    } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.addEndScore) {
                        ynuiNotification.error({msg: "不能大于" + $scope.submitObj.addEndScore});
                        $scope.submitObj.score = $scope.submitObj.addEndScore;
                        canGoOn = true;
                    }
                } else {
                    if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.delEndScore) {
                        ynuiNotification.error({msg: "不能大于" + $scope.submitObj.delEndScore});
                        $scope.submitObj.score = $scope.submitObj.delEndScore;
                        canGoOn = true;
                    } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.delBeginScore) {
                        ynuiNotification.error({msg: "不能小于" + $scope.submitObj.delBeginScore});
                        $scope.submitObj.score = $scope.submitObj.delBeginScore;
                        canGoOn = true;
                    }
                }
                $scope.returnItemScore();
                if (canGoOn) {
                    return false;
                } else {
                    return true;
                }
            };
            $scope.returnItemScore = function () {
                if ($scope.submitObj.scoreType == 0) {
                    $scope.submitObj.itemScore = accAdd($scope.submitObj.baseScore, $scope.submitObj.score);
                } else {
                    $scope.submitObj.itemScore = accSub($scope.submitObj.baseScore, $scope.submitObj.score);
                }
            };
            $scope.submitScore = function () {
                if (!$scope.changeScore()) {
                    return false;
                }
                $scope.submitObj.checkScoreDetailId = checkScoreId;
                $scope.submitObj.scoreCheckMidId = scoreCheckMidId;
                var obj = angular.copy($scope.submitObj);
                delete  obj.stuInfoList;
                delete  obj.attachmentList;
                obj.isSubmit = false;
                $http.post(originBaseUrl + '/third/classExamine/saveOrUpdateScoreCheckDetailWithMobile.htm?', obj).success(function (data) {
                    if (data.status != 0) {
                        ynuiNotification.error({msg: data.message});
                    } else {
                        viewsParameterService.setParameter({
                            itemId: itemId,
                            submitObj: $scope.submitObj,
                            isHaveChange: $scope.isHaveChange,
                            isReturn: false
                        });
                        $location.path("/class_grade").search("item", JSON.stringify({
                            "checkScoreId": checkScoreId,
                            "noOverClassNum": noOverClassNum,
                            "groupOrRegionIndex": groupOrRegionIndex,
                            "classRoomIndex": classRoomIndex
                        }));
                    }
                });
            };
            $scope.returnLastPage = function () {
                if ($scope.submitObj.scoreType != $scope.oldSubmitObj.scoreType) {
                    $scope.isHaveChange = true;
                } else if ($scope.submitObj.score != $scope.oldSubmitObj.score) {
                    $scope.isHaveChange = true;
                } else if ($scope.submitObj.scoreDesc != $scope.oldSubmitObj.scoreDesc) {
                    $scope.isHaveChange = true;
                }
                ;
                if ($scope.isHaveChange) {
                    var confirmPopup = $ionicPopup.confirm({
                        title: "提示",
                        template: '已编辑考核内容确定要放弃吗？',
                        cancelText: "取消",
                        okText: "确认"
                    });
                    confirmPopup.then(function (res) {
                        if (res) {
                            viewsParameterService.setParameter({
                                itemId: itemId,
                                submitObj: $scope.submitObj,
                                isHaveChange: $scope.isHaveChange,
                                isReturn: false
                            });
                            $location.path("/class_grade").search("item", JSON.stringify({
                                "checkScoreId": checkScoreId,
                                "noOverClassNum": noOverClassNum,
                                "groupOrRegionIndex": groupOrRegionIndex,
                                "classRoomIndex": classRoomIndex,
                                "lastIsClassForm": false
                            }));
                        }
                    });
                } else {
                    viewsParameterService.setParameter({
                        itemId: itemId,
                        submitObj: $scope.submitObj,
                        isHaveChange: $scope.isHaveChange,
                        isReturn: false
                    });
                    $location.path("/class_grade").search("item", JSON.stringify({
                        "checkScoreId": checkScoreId,
                        "noOverClassNum": noOverClassNum,
                        "groupOrRegionIndex": groupOrRegionIndex,
                        "classRoomIndex": classRoomIndex,
                        "lastIsClassForm": false
                    }));
                }
            };
            //刷新
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                //置空数据，在获取时候叠加数据
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            /**
             * 添加学生
             */
            var getClassStu = function () {
                $scope.someOneShowName.searchClassStu = "";
                $scope.stuCheckedNum = 0;
                $scope.classStudent = [];
                $http.get(originBaseUrl + '/third/classExamine/queryClassStudentByClassIdByMobile.htm?classId=' + classId + '&time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.classStudent = data.result;
                        $scope.classStudentCopy = angular.copy(data.result);
                        angular.forEach($scope.classStudent, function (item, index) {
                            item.checked = false;
                            $scope.classStudentCopy[index].checked = false;
                            angular.forEach($scope.submitObj.stuIdList, function (ite) {
                                if (item.id == ite) {
                                    item.checked = true;
                                    $scope.classStudentCopy[index].checked = true;
                                    $scope.stuCheckedNum += 1;
                                }
                            })
                        });
                    }
                });
            };
            $scope.closeIconClick = function () {
                $scope.someOneShowName.searchClassStu = "";
            };
            $scope.changeChecked = function (index) {
                $scope.classStudent[index].checked = !$scope.classStudent[index].checked;
                $scope.isHaveChange = true;
                $scope.stuCheckedNum = 0;
                angular.forEach($scope.classStudentCopy, function (item) {
                    if ($scope.classStudent[index].id == item.id) {
                        item.checked = $scope.classStudent[index].checked;
                        if (item.checked) {
                            $scope.stuCheckedNum += 1;
                        }
                    } else {
                        if (item.checked) {
                            $scope.stuCheckedNum += 1;
                        }
                    }

                })
            }
            $scope.$watch("someOneShowName.searchClassStu", function (newVal, oldVal) {
                if ($scope.someOneShowName.searchClassStu == "") {
                    $scope.isCloseIcon = false;
                } else {
                    $scope.isCloseIcon = true;
                }
                if (newVal != oldVal) {
                    $scope.classStudent = $filter('filter')($scope.classStudentCopy, {number: $scope.someOneShowName.searchClassStu});
                    $ionicScrollDelegate.scrollTop();
                    if (!$scope.classStudent || $scope.classStudent.length < 1) {
                        $scope.emptyInfoStu = true;              //展示空数据提示
                        $scope.dataErrorMsgStu = "没有符合条件的学生!";
                    } else {
                        $scope.emptyInfoStu = false;
                    }
                }
            }, true);

            /**
             * 完成学生的选择
             */
            $scope.submitClassStu = function () {
                $scope.submitObj.stuIdList = [];
                $scope.submitObj.stuInfoList = [];
                angular.forEach($scope.classStudentCopy, function (item) {
                    if (item.checked) {
                        $scope.submitObj.stuIdList.push(item.id);
                        $scope.submitObj.stuInfoList.push(item);
                    }
                });
                $scope.closeName();
            };
            $scope.doRefresh2 = function () {
                $scope.someOneShowName.searchClassStu = ""
            };
        }])
    //选择班级
    .controller("classSearch", ['classListService', '$scope', '$http', '$ionicModal', 'ynuiNotification', '$ionicPopup', '$location','$cordovaBarcodeScanner', function (classListService, $scope, $http, $ionicModal, ynuiNotification, $ionicPopup, $location,$cordovaBarcodeScanner) {

        $scope.classListShow = false;

        $scope.selectedClass = classListService.getAllList();
        /**
         * 开始考核
         */
        $scope.startClassPatrol = function () {
            if($scope.selectedClass.length == 0){
                ynuiNotification.error({msg: "请选择巡查的班级"});
                return false;
            }
            classListService.save($scope.selectedClass);
            $location.path("/selectClass_examine_project");
        };

        /**
         * 显示浮动窗口
         */
        $scope.showClassListModal = function () {
            $scope.classListShow = !$scope.classListShow;
        };

        /**
         * 查询全校班级
         */
        $scope.toSearchAllClass = function () {
            classListService.save($scope.selectedClass);
            $location.path("/select_class");
        };

        /**
         * 输入查询
         */
        $scope.inputClassInfoToSearch = function () {
            classListService.save($scope.selectedClass);
            $location.path("/search_classList")
        };

        /**
         * 扫描班级好牌
         */
        $scope.scanClassCode =  function () {
            $cordovaBarcodeScanner.scan().then(function (barcodeData) {
                $scope.number = barcodeData.text;
                if(!$scope.number){return false;}
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $http.get(originBaseUrl + "/third/classPatrol/queryUserSecurityClass.htm?classCode="+  encodeURIComponent($scope.number)).success(function (data) {
                    $ionicLoading.hide();
                    if (data.status == 0) {
                        var arr = data.result.content;
                        if(arr.length > 0){
                            angular.forEach(arr,function (a) {
                                if(!$scope.selectedClass.some(function(s){
                                        return  s.id == a.id;
                                    })){
                                    $scope.selectedClass.push(a);
                                }
                            });
                            $scope.classListShow = false;
                        }else{
                            ynuiNotification.error({msg: '没有班级到学生信息！'});
                        }
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                    } else {
                        ynuiNotification.error({msg: '获取班级信息失败！'});
                    }
                }).error(function (data) {
                    ynuiNotification.error({msg: '获取班级信息失败！'});
                });
            }, function (error) {
                ynuiNotification.error({msg: "扫描失败！"});
            });
        };
        /**
         *  选择或者是不行选择
         * @param index
         */
        $scope.unSelected = function(index){
            $scope.selectedClass.splice(index,1);
        };
    }])
    //查询班级
    .controller("searchClassList", ['classListService', '$scope', '$http', '$ionicModal', 'ynuiNotification', '$ionicPopup', '$location', '$filter', '$ionicLoading', '$ionicScrollDelegate','$cordovaBarcodeScanner', function (classListService, $scope, $http, $ionicModal, ynuiNotification, $ionicPopup, $location, $filter, $ionicLoading, $ionicScrollDelegate,$cordovaBarcodeScanner) {
        $scope.classListShow = false;
        $scope.search = false;
        /**
         * 全部的班级
         * @type {Array}
         */
        $scope.classList = [];
        /**
         * 被选中的班级
         * @type {Array}
         */
        $scope.selectedClassList = classListService.getAllList();

        $scope.className = "";
        /**
         * 上一步
         */
        $scope.lastStep = function () {
            classListService.save($scope.selectedClassList);
            window.history.go(-1);
        };
        /**
         * 显示浮动窗口
         */
        $scope.showClassListModal = function () {
            $scope.classListShow = !$scope.classListShow;
        };
        /**
         * 开始考核
         */
        $scope.startClassPatrol = function () {
            if(!$scope.selectedClassList || $scope.selectedClassList.length == 0){
                ynuiNotification.error({msg: "请选择巡查的班级"});
                return false;
            }
            classListService.save($scope.selectedClassList);
            $location.path("/selectClass_examine_project");
        };

        /**
         * 获取全部的数据
         */
        $scope.getAllClassList = function () {
            $http.get(originBaseUrl + "/third/classPatrol/queryUserSecurityClass.htm?className="+$scope.className).success(function (data) {
                if(data.status == 0){
                    $scope.classList = data.result.content;
                    var tempArr = classListService.getAllList();
                    angular.forEach($scope.classList,function (item) {
                        item.selected = tempArr.length > 0 ? tempArr.some(function (t) {
                            return t.id == item.id;
                        })  : false;
                    });
                }else{
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                }
            });
        };

        /**
         * 选中班级
         * @param item
         */
        $scope.selectItemed = function (item) {
            item.selected = !item.selected;
            angular.forEach($scope.classList,function(c){
                if(!$scope.selectedClassList.some(function (t) {
                        return t.id == c.id;
                    }) &&  c.selected ){
                    $scope.selectedClassList.push(c);
                }
            });
        };
        /**
         *  删除选中的数据
         * @param index
         */
        $scope.unSelectedItem = function (index) {
            $scope.selectedClassList.splice(index,1);
            angular.forEach($scope.classList,function(c){
                c.selected = $scope.selectedClassList.some(function (t) {
                    return t.id == c.id;
                });
            })
        };

        $scope.$watch(function () {
            return $scope.className;
        },function (newVal) {
            var timeout = window.setTimeout(function () {
                if($scope.className){
                    $scope.search = true;
                    $scope.getAllClassList();
                }else{
                   $scope.$apply(function () {
                       $scope.classList = [];
                   })
                }
                window.clearTimeout(timeout);
            })
        });

    }])

    .controller("selectClassExamineProject", ['classListService', '$scope', '$http', '$ionicModal', 'ynuiNotification', '$ionicPopup', '$location', '$filter', '$ionicLoading', '$ionicScrollDelegate', function (classListService, $scope, $http, $ionicModal, ynuiNotification, $ionicPopup, $location, $filter, $ionicLoading, $ionicScrollDelegate) {

        $scope.classListShow = false;
        $scope.selectedClass = classListService.getAllList();
        $scope.classItems = [];
        $scope.classCheckItem = [];
        $scope.itemCode = "";
        /**
         * 上一步
         */
        $scope.lastStep = function () {
            classListService.save( $scope.selectedClass);
            window.history.go(-1);
        };
        /**
         * 显示浮动窗口
         */
        $scope.showClassListModal = function () {
            $scope.classListShow = !$scope.classListShow;
        };

        /**
         *  删除数据
         * @param index
         */
        $scope.unSelected = function(index){
            $scope.selectedClass.splice(index,1);
        };

        /**
         * 获取全部的打分项目
         */
        $scope.queryAllClassChItem = function () {
            $http.get(originBaseUrl + "/third/classPatrol/queryAllClassChItem.htm").success(function (data) {
                if(data.status == 0){
                    $scope.classItems = data.result;
                    angular.forEach($scope.classItems,function (item) {
                       item.selected = false;
                    });
                    $scope.classItems.unshift({
                        id:"",
                        typeNumber:"",
                        checkItemName:"全部"
                    });
                    $scope.item=$scope.classItems[0];
                    // $scope.queryPatrolClassItem($scope.item);
                }else{
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                }
            });
        };

        /**
         * 获取全部的打分详列
         */

        $scope.queryPatrolClassItem = function (item) {
            angular.forEach($scope.classItems,function (item) {
                item.selected = false;
            });
            item.selected =  true;
            $scope.item = item;
            $scope.getPatrolClassItemList(item);
        };

        /**
         * 获取数据列
         * @param item
         */
        $scope.getPatrolClassItemList = function(item){
            $http.get(originBaseUrl + "/third/classPatrol/queryPatrolClassItem.htm?patrolItemId="+(item ? item.id : ""+"&itemCode="+$scope.itemCode)).success(function (data) {
                if(data.status == 0){
                    $scope.classCheckItem = data.result;
                }else{
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                }
            });
        };
        /**
         * 监视选择
         */
        $scope.$watch("itemCode",function(){
            if($scope.itemCode){
                angular.forEach($scope.classItems,function (item) {
                    item.selected = false;
                });
                $scope.classItems[0].selected= true;
                $scope.getPatrolClassItemList({});
            }else{
                angular.forEach($scope.classItems,function (item) {
                    item.selected = item.id == $scope.item.id;
                });
                $scope.getPatrolClassItemList($scope.item);
            }
        });
        /**
         * 刷新
         */
        $scope.refreshAll = function(){
            var arr = $scope.classItems.filter(function (t) {
                return t.selected;
            });
            $scope.queryPatrolClassItem(arr.length > 0  ? arr[0]:{})
        };

        /**
         * 进入打分界面
         * @param item
         */
        $scope.classPatrolAct = function(item){
            if($scope.selectedClass.length == 0){
                ynuiNotification.error({msg: "请选择巡查的班级"});
                return false;
            }
            $location.path("/class_patrol").search({item:JSON.stringify(item)});
        };
        $scope.queryAllClassChItem();
    }])

    .controller("selectClass", ['classListService', '$scope', '$http', '$ionicModal', 'ynuiNotification', '$ionicPopup', '$location', '$filter', '$ionicLoading', '$ionicScrollDelegate', function (classListService, $scope, $http, $ionicModal, ynuiNotification, $ionicPopup, $location, $filter, $ionicLoading, $ionicScrollDelegate) {

        /**
         * 上一步
         */
        $scope.classList = [];
        $scope.gradeList = [];
        $scope.selectedClass = classListService.getAllList();
        $scope.lastStep = function () {
            $scope.selectedClass = $scope.classList.filter(function (a) {
                return a.selected;
            });
            classListService.save( $scope.selectedClass);
            window.history.go(-1);
        };
        /**
         * 显示浮动窗口
         */
        $scope.showClassListModal = function () {
            $scope.classListShow = !$scope.classListShow;
        };
        /**
         * 开始考核
         */
        $scope.startClassPatrol = function () {
            $scope.selectedClass = $scope.classList.filter(function (a) {
                return a.selected;
            });
            if($scope.selectedClass.length == 0){
                ynuiNotification.error({msg: "请选择巡查的班级"});
                return false;
            }
            classListService.save( $scope.selectedClass);
            $location.path("/selectClass_examine_project");
        };

        /**
         * 获得近5年有效的年级
         */
        $scope.getNearlyFiveYearsGrades = function(){
            $http.get(originBaseUrl + "/third/classPatrol/getNearlyFiveYearsGrades.htm").success(function (data) {
                if(data.status == 0){
                    $scope.gradeList = data.result;
                    //逆排序
                    $scope.gradeList.sort(function (o1, o2) {
                        return o1.name.localeCompare(o2.name);
                    });
                    $scope.gradeList.unshift({
                        id:"",
                        name:"全部"
                    });
                    $scope.getAllClassList({id:""});
                }else{
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                }
            });
        };
        /**
         * 获取全校的班级信息
         * @param grade
         */
        $scope.getAllClassList = function (grade) {
            angular.forEach($scope.gradeList,function(g){
                g.selected  = g.id == grade.id;
            });
            grade.selected = true;
            $http.get(originBaseUrl + "/third/classPatrol/queryUserSecurityClass.htm?gradeId="+(grade.id?grade.id:"")).success(function (data) {
                if(data.status == 0){
                    $scope.classList = data.result.content;
                    angular.forEach($scope.classList,function (item) {
                        item.selected = $scope.selectedClass.length > 0 ? $scope.selectedClass.some(function (t) {
                            return t.id == item.id;
                        })  : false;
                    });
                }else{
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                }
            });
        };

        /**
         * 选中或者是不选中
         * @param item
         */
        $scope.selectItemClass = function(item){
            item.selected = !item.selected;
            angular.forEach($scope.classList,function(c){
                if(!$scope.selectedClass.some(function (t) {
                    return t.id == c.id;
                    }) &&  c.selected ){
                    $scope.selectedClass.push(c);
                }
            });
        };

        /**
         * 在已选择的名单中删除原来的数据
         */
        $scope.unSelected = function (index) {
            $scope.selectedClass.splice(index,1);
            $scope.classList.forEach(function (clazz) {
                clazz.selected = $scope.selectedClass.some(function (t) {
                    return t.id == clazz.id;
                })
            })
        };
        $scope.getNearlyFiveYearsGrades();
    }])
    //巡查打分
    .controller('classPatrolCtrl', ['classListService','$scope', '$http', '$ionicModal', 'ynuiNotification', '$ionicPopup', '$location', '$filter', '$ionicLoading', '$ionicScrollDelegate',"$cordovaBarcodeScanner", function (classListService,$scope, $http, $ionicModal, ynuiNotification, $ionicPopup, $location, $filter, $ionicLoading, $ionicScrollDelegate,$cordovaBarcodeScanner) {

        $scope.checkSuccess = false;

        $ionicModal.fromTemplateUrl('student.html', {
            scope: $scope,
            animation: 'slide-in-left'
        }).then(function (modal) {
            $scope.modal = modal;
        });
        /**
         * 考核项备注
         */
        var item = $scope.item = JSON.parse($location.search().item);
        var itemId = item.itemId;
        /**
         * 选择的班级的信息
         */
        $scope.selectedClass = classListService.getAllList();

        /**
         * 显示浮动窗口
         */
        $scope.showClassListModal = function () {
            $scope.classListShow = !$scope.classListShow;
        };
        /**
         * 选择学生的列表
         */
        $scope.openName = function () {
            getClassStu();
            $scope.modal.show();
        };
        /**
         * 关闭查询学生接口
         */
        $scope.closeName = function () {
            $scope.modal.hide();
        };
        /**
         * 考核项备注
         * @type {boolean}
         */
        $scope.isOver = false;

        /**
         * 获取当前的时间
         * @returns {string}
         */
        $scope.getCurrentTime = function(){
            var currentTime = new Date();
            var month = (currentTime.getMonth() + 1) > 9 ? (currentTime.getMonth() + 1) : '0' + (currentTime.getMonth() + 1);
            var day = currentTime.getDate() > 9 ? currentTime.getDate() : '0' + currentTime.getDate();
            var hour = currentTime.getHours() > 9 ? currentTime.getHours() : '0' + currentTime.getHours();
            var minute = currentTime.getMinutes() > 9 ? currentTime.getMinutes() : '0' + currentTime.getMinutes();
            var currentTimeStr = currentTime.getFullYear() + "-" + month + "-" + day + " " + hour + ":" +
                minute;
            return currentTimeStr;
        };

        $scope.options = {};
        $scope.isOpen = false;
        $scope.submitObj = {
            moraleduChClassItemId:item.id,
            itemName:item.itemName,
            itemRemark:item.remark,
            remark:"",
            scoreType:0,
            score:0,
            baseScore:item.baseScore,
            delBeginScore:item.delBeginScore,
            delEndScore:item.delEndScore,
            addBeginScore:item.addBeginScore,
            addEndScore:item.addEndScore,
            stuIdList:[],//学生
            attachmentList:[],//文件
            platformAdminClassIds:[],//班级
            stuInfoList:[],//回传的学生的信息
            datetime:$scope.getCurrentTime()
        };
        /**
         * 获取上传的文件的fdfsId
         * @param data
         */
        $scope.getDfsIds = function (data) {
            $scope.submitObj.attachmentList = angular.copy(data.data);
        };


        /**
         * 返回打分页面
         */
        $scope.lastStep = function () {
            window.history.go(-1);
        };
        /**
         * js浮点数计算不准确计算解决
         * @param a
         * @param b
         * @returns {number}
         */
        var mul = function (a, b) {
            var c = 0,
                d = a.toString(),
                e = b.toString();
            try {
                c += d.split(".")[1].length;
            } catch (f) {
            }
            try {
                c += e.split(".")[1].length;
            } catch (f) {
            }
            return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
        };
        /**
         * 加
         * @param a
         * @param b
         */
        var accAdd = function (a, b) {
            var c, d, e;
            try {
                c = a.toString().split(".")[1].length;
            } catch (f) {
                c = 0;
            }
            try {
                d = b.toString().split(".")[1].length;
            } catch (f) {
                d = 0;
            }
            return e = Math.pow(10, Math.max(c, d)), (mul(a, e) + mul(b, e)) / e;
        };
        /**
         *
         * @param a
         * @param b
         */
        var accSub = function (a, b) {
            var c, d, e;
            try {
                c = a.toString().split(".")[1].length;
            } catch (f) {
                c = 0;
            }
            try {
                d = b.toString().split(".")[1].length;
            } catch (f) {
                d = 0;
            }
            return e = Math.pow(10, Math.max(c, d)), (mul(a, e) - mul(b, e)) / e;
        }
        /**
         *
         * @param isAdd
         */
        $scope.isAddScore = function (isAdd) {
            if ($scope.submitObj.score == "") {
                $scope.submitObj.score = 0;
            }
            if (isAdd > 0) {
                $scope.submitObj.score = accAdd($scope.submitObj.score, 0.1);
            } else {
                $scope.submitObj.score = accSub($scope.submitObj.score, 0.1);
            }
            if ($scope.submitObj.scoreType == 0) {
                if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.addBeginScore) {
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                    $scope.submitObj.score = $scope.submitObj.addBeginScore;
                } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.addEndScore) {
                    ynuiNotification.error({msg: "不能大于" + $scope.submitObj.addEndScore});
                    $scope.submitObj.score = $scope.submitObj.addEndScore;
                }
            } else {
                if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.delEndScore) {
                    ynuiNotification.error({msg: "不能大于" + $scope.submitObj.delEndScore});
                    $scope.submitObj.score = $scope.submitObj.delEndScore;
                } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.delBeginScore) {
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.delBeginScore});
                    $scope.submitObj.score = $scope.submitObj.delBeginScore;
                }
            }
            $scope.returnItemScore();
        };
        /**
         * 监控
         */
        $scope.$watch(function () {
            return $scope.submitObj.score;
        }, function (newVal, oldVal) {
            var reg = /^\d+\.?\d{0,2}$/;
            if (newVal && !reg.test(newVal)) {
                $scope.submitObj.score = oldVal;
            }
        });
        /**
         * 监视成绩的改变
         * @returns {boolean}
         */
        $scope.changeScore = function () {
            var canGoOn = false;
            if ($scope.submitObj.scoreType == 0) {
                if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.addBeginScore) {
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.addBeginScore});
                    $scope.submitObj.score = $scope.submitObj.addBeginScore;
                    canGoOn = true;
                } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.addEndScore) {
                    ynuiNotification.error({msg: "不能大于" + $scope.submitObj.addEndScore});
                    $scope.submitObj.score = $scope.submitObj.addEndScore;
                    canGoOn = true;
                }
            } else {
                if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score > $scope.submitObj.delEndScore) {
                    ynuiNotification.error({msg: "不能大于" + $scope.submitObj.delEndScore});
                    $scope.submitObj.score = $scope.submitObj.delEndScore;
                    canGoOn = true;
                } else if (($scope.submitObj.score && $scope.submitObj.score != 0) && $scope.submitObj.score < $scope.submitObj.delBeginScore) {
                    ynuiNotification.error({msg: "不能小于" + $scope.submitObj.delBeginScore});
                    $scope.submitObj.score = $scope.submitObj.delBeginScore;
                    canGoOn = true;
                }
            }
            $scope.returnItemScore();
            if (canGoOn) {
                return false;
            } else {
                return true;
            }
        };
        /**
         * 返回成绩
         */
        $scope.returnItemScore = function () {
            if ($scope.submitObj.scoreType == 0) {
                $scope.submitObj.itemScore = accAdd($scope.submitObj.baseScore, $scope.submitObj.score);
            } else {
                $scope.submitObj.itemScore = accSub($scope.submitObj.baseScore, $scope.submitObj.score);
            }
        };

        /**
         * 提交成绩
         * @returns {boolean}
         */

        $scope.submitScore = function () {
            if($scope.selectedClass.length == 0){
                ynuiNotification.warning({msg: "重新选择班级！"});
                return false;
            }

            if($scope.submitObj.datetime){
                var dateAndTime = $scope.submitObj.datetime.split(" ");
                if(!dateAndTime||dateAndTime.length!=2){
                    return false;
                }
                $scope.submitObj.checkDate=$scope.submitObj.datetime;
            }

            $scope.confirmPopParam= {};
            $scope.confirmPopParam.classNameStr = $scope.selectedClass.map(function (t) { return t.name });
            $scope.confirmPopParam.checkItemName = $scope.submitObj.itemName;
            $scope.confirmPopParam.scoreStr =  ($scope.submitObj.scoreType == '1' ? '扣' : '加') + $scope.submitObj.score + "分";
            var confirmPopup = $ionicPopup.confirm({
                title:"请确认考核信息",
                templateUrl: 'templates/classexamineall/templ/confirmCheckInfoPop.html',
                scope: $scope,
                cancelText:"取消",
                okText:"确认提交"
            });
            confirmPopup.then(function(res){
                if(!res){return false;}
                var obj = angular.copy($scope.submitObj);
                delete  obj.itemRemark;
                obj.stuInfoIdList = obj.stuInfoList.map(function(s){
                    return s.id
                }).join(",");
                delete  obj.stuInfoList;
                obj.platformAdminClassIds = $scope.selectedClass.map(function(clazz){
                    return  clazz.id;
                }).join(",");
                classListService.save([]);
                $http.post(originBaseUrl + '/third/classPatrol/savePatrolDetail.htm?', obj).success(function (data) {
                    if (data.status != 0) {
                        ynuiNotification.error({msg: data.message});
                    } else {
                        $scope.checkSuccess = true;
                    }
                });
            });
        };

        /**
         * 添加学生
         */
        var getClassStu = function (studentPlatformInfo) {
            $scope.classStudent = [];
            $scope.stuCheckedNum = 0;
            var classIds = $scope.selectedClass.map(function(c){return c.id});
            $http.get(originBaseUrl + '/third/classPatrol/queryClassStudentByClassIds.htm?classIds=' + classIds+"&studentPlatformInfo="+ (studentPlatformInfo?studentPlatformInfo:"") + '&time=' + (new Date()).getTime()).success(function (data) {
                if (data.status == 0) {
                    $scope.classStudent = data.result;
                    $scope.classStudentCopy = angular.copy(data.result);
                    angular.forEach($scope.classStudent, function (item, index) {
                        item.checked = false;
                        $scope.classStudentCopy[index].checked = false;
                        angular.forEach($scope.submitObj.stuInfoList, function (ite) {
                            if (item.id == ite.id) {
                                item.checked = true;
                                $scope.classStudentCopy[index].checked = true;
                                $scope.stuCheckedNum += 1;
                            }
                        })
                    });
                }
            });
        };

        /**
         * 扫描二位嘛
         */
        $scope.scanCode = function () {

            $cordovaBarcodeScanner.scan().then(function (barcodeData) {
                $scope.number = barcodeData.text;
                if(!$scope.number){return false;}
                $scope.number =  JSON.parse($scope.number);
                var classIds = $scope.selectedClass.map(function(c){return c.id});
                $http.get(originBaseUrl + '/third/classPatrol/queryClassStudentByClassIds.htm?classIds=' + classIds+"&studentPlatformInfo="+ encodeURIComponent($scope.number.USERID) + '&time=' + (new Date()).getTime()).success(function (data) {
                    $ionicLoading.hide();
                    if (data.status == 0) {
                        var arr  = data.result;
                        if(arr.length > 0){
                            angular.forEach(arr,function (a) {
                                if(!$scope.submitObj.stuInfoList.some(function(s){
                                        return  s.id == a.id;
                                    })){
                                    $scope.submitObj.stuInfoList.push(a);
                                }
                            });
                        }else{
                            ynuiNotification.error({msg: '被考核班级中没有该学生!'});
                        }
                    } else {
                        ynuiNotification.error({msg: '获取学生信息失败！'});
                    }
                }).error(function (data) {
                    ynuiNotification.error({msg: '获取学生信息失败！'});
                });
            }, function (error) {
                ynuiNotification.error({msg: "扫描失败！"});
            });
        };

        /**
         * 刷新学生
         */
        $scope.doRefreshStudent = function () {
            getClassStu("");
        };
        /**
         * 点击或者是取消选中学生
         * @param index
         */
        $scope.stuCheckedNum = 0;
        $scope.changeChecked = function (index) {
            $scope.classStudent[index].checked = !$scope.classStudent[index].checked;
            $scope.stuCheckedNum = $scope.classStudent.filter(function (t) {
                return t.checked;
            }).length;
        };
        /**
         * 完成学生的选择
         */
        $scope.submitClassStu = function () {
            var arr  = $scope.classStudent.filter(function (t) {
                return t.checked;
            });
            $scope.submitObj.stuInfoList = angular.copy(arr);
            $scope.closeName();
        };

        /**
         * 继续其他项目的考核
         */
        $scope.previousStep = function(){
            classListService.save($scope.selectedClass);
            window.history.go(-1);
        };

        /**
         * 其他班级的考核
         */
        $scope.checkOtherClass = function(){
            classListService.save([]);
            $location.path("/class_search");
        };

        /**
         *  删除数据
         * @param index
         */
        $scope.unSelected = function(index){
            $scope.selectedClass.splice(index,1);
        };

        /**
         * 上一部
         */
        $scope.lastStep = function(){
            classListService.save($scope.selectedClass);
            window.history.go(-1);
        }
    }])
    //自定义保存存储服务
    .service("classListService", function () {
        //初始化服务列表
        return {
            getAllList: function () {
                var classList = window.localStorage.getItem("_class_list_storage");
                return !classList? []:JSON.parse(classList);
            },
            save:function (arr) {
                window.localStorage.setItem("_class_list_storage",null == arr?"":JSON.stringify(arr));
            }
        }
    })
    .run(["$rootScope","classListService",function($rootScope,classListService){
        $rootScope.$on("$stateChangeSuccess",function(event,toState,toParams,fromState,fromParams){
            var state = ["class_search","search_classList","selectClass_examine_project","select_class","class_patrol"];
            var toStatuRoute = toState.name,fromStatuRoute = fromState.name;
            if(fromStatuRoute == ""){
                return;
            }
            //如果是从其他地方跳转过来的，就需要删除原来的数据格式
            if(!state.some(function (t) { return t ==  fromStatuRoute})){
                classListService.save(null);
                return ;
            }
            if(!state.some(function (t) { return t ==  toStatuRoute})){
                classListService.save(null);
                return;
            }
        });
}]);
