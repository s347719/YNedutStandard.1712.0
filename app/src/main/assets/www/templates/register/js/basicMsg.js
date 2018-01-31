/**
 * 登记任务外的听课结果的基本信息
 * Created by yineng on 2017/1/5 10:33
 *
 */
angular.module('starter').controller('basicMsgController', ['$scope', '$http', '$location', '$state', '$ionicModal', "$filter", "ynuiNotification", "tkStorageService", "$q", "$ionicPopup",
    function ($scope, $http, $location, $state, $ionicModal, $filter, ynuiNotification, tkStorageService, $q, $ionicPopup) {
        /**
         * 路由信息
         * @type {string}
         */
        var basePath = originBaseUrl;

        var isFromChooseTeacher = $location.search().isChooseTeacher;
        var isChangeTeacher = $location.search().isChangeTeacher;
        var isFromChooseClassRoom = $location.search().isFromChooseClassRoom;
        $ionicModal.fromTemplateUrl('template.html', {
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function (modal) {
            $scope.modal = modal;
        });
        $scope.bottomModalClick = function () {
            $scope.modal.show();
        };
        $scope.closeModal = function () {
            $scope.modal.hide();
        };

        $scope.tkBasicInfo = {
            termId: null, // 学期
            selectedTKDate: null, //听课日期
            week: null, //第几周
            day: null, //星期几,
            timeType: null, //时段类型,
            knob: null, //节次,
            selectedKnobIndex: null,
            classRoomList: [],
            teacherId: null, // 授课教师ID
            selectedClassRoomName: null, //
            selectedClassRoomId: null, //
            selectedTableId: null, // 听课记录表ID
            selectedTableName: null,  // 听课记录表ID
            teachClassId: null,
            scheduleResultsId: null,
            courseName: null,
            courseId: null,
            tkDateDesc: null,
            teachingAdminClassNameStr: null,
            teachingAdminClassIdStr: null,
            teacherName: null,
            termIndex: null,
            weekAndDayDesc: null
        };

        var isHaveCache = false;
        var oldConditon = tkStorageService.getItem("tkBasicInfo");
        if (oldConditon) {
            isHaveCache = true;
            $scope.tkBasicInfo = oldConditon;
        }

        // 当前日期
        $scope.currentDate = new Date();
        var currentDateTime = $filter('date')($scope.currentDate, 'yyyy-MM-dd HH:mm:ss'); // 日期时间
        $scope.currentDate = $filter('date')($scope.currentDate, 'yyyy-MM-dd'); // 格式化

        /**根据日期获取周次*/
        var getWeekAndDay = function (termId, date) {
            $http.post(basePath + "/third/tkTaskRegisterApp/queryWeekAddDate.htm?termId=" + termId + "&queryDate=" + date).success(function (data) {
                if (data.status == 0) {
                    $scope.tkBasicInfo.tkDateDesc = "";
                    if (data.result) {
                        $scope.tkBasicInfo.weekAndDayDesc = data.result.weekDateStr;
                        $scope.tkBasicInfo.week = data.result.week;
                        $scope.tkBasicInfo.day = data.result.date;
                        var weekDateStr = data.result.weekDateStr;
                        var splitIndex = weekDateStr.indexOf("周");
                        var weekStr = weekDateStr.substring(0, splitIndex + 1);
                        var dayStr = weekDateStr.substring(splitIndex + 1);
                        if ($scope.currentDate == $scope.date.tempDate) {
                            $scope.tkBasicInfo.tkDateDesc += "[今天]";
                        }
                        $scope.tkBasicInfo.tkDateDesc += $scope.date.tempDate;
                        $scope.tkBasicInfo.tkDateDesc += " " + weekStr;
                        $scope.tkBasicInfo.tkDateDesc += " " + dayStr;
                    }
                }
            })
        };


        $scope.knobList = [];
        /**获取教学周历下的节次*/
        var getTKKnobList = function (termId) {
            var deferred = $q.defer();
            var promise = deferred.promise;
            $http.post(basePath + "/third/tkTaskRegisterApp/getTKKnobList.htm", {
                termId: termId
            }).success(function (data) {
                $scope.knobList = [];
                if (data.status == 0) {
                    if (data.result) {
                        $scope.knobList = data.result;
                        var i = 1;
                        angular.forEach($scope.knobList, function (item) {
                            item.sequence = i;
                            i++;
                        });
                        // 如果没有缓存，则查询当前时间节点的节次
                        if (!isHaveCache) {
                            getCurrentTimeKnob(termId);
                        }
                    }
                }
                deferred.resolve($scope.knobList);
            });
            return promise;
        };

        /**获取当前时间节点的节次*/
        var getCurrentTimeKnob = function (termId) {
            $scope.tkBasicInfo.timeType = null;
            $scope.tkBasicInfo.knob = null;
            $http.post(basePath + "/third/tkOutsideTaskRegisterApp/getCurrentTimeKnob.htm", {
                termId: termId
            }).success(function (data) {
                if (data.status == 0) {
                    if (data.result) {
                        var index = 0;
                        angular.forEach($scope.knobList, function (item) {
                            if (item.timeType == data.result.timeType && item.knob == data.result.knob) {
                                $scope.tkBasicInfo.timeType = item.timeType;
                                $scope.tkBasicInfo.knob = item.knob;
                                $scope.tkBasicInfo.selectedKnobIndex = index;
                            }
                            index++;
                        });
                    } else {
                        $scope.tkBasicInfo.selectedKnobIndex = 0;
                        $scope.tkBasicInfo.timeType = $scope.knobList[0].timeType;
                        $scope.tkBasicInfo.knob = $scope.knobList[0].knob;
                    }
                }
            });
        };


        $scope.isExist = null; // 是否存在教学周历
        // 学期
        $scope.term = null;
        $scope.termList = [];
        $http.post(basePath + "/third/tkOutsideTaskRegisterApp/getPreviousAndCurrentTermList.htm").success(function (data) {
            $scope.termList = [];
            $scope.term = null;
            if (data.status == 0) {
                $scope.termList = data.result;
                if ($scope.termList.length > 0) {
                    if (!isHaveCache) {
                        if ($scope.termList[0].currenTerm) {
                            $scope.term = $scope.termList[0];
                            $scope.tkBasicInfo.termIndex = 0;
                        }
                    } else {
                        angular.forEach($scope.termList, function (item) {
                            if (item.termId == $scope.tkBasicInfo.termId) {
                                $scope.term = item;

                            }
                        });
                    }
                    $scope.isExist = $scope.term.existWeekCalendar;
                }
            }
        });

        /**切换学期*/
        $scope.changeTerm = function () {
            if ($scope.tkBasicInfo.termIndex < $scope.termList.length - 1) {
                $scope.tkBasicInfo.termIndex++;
                $scope.term = $scope.termList[$scope.tkBasicInfo.termIndex];
            } else {
                $scope.tkBasicInfo.termIndex = 0;
                $scope.term = $scope.termList[$scope.tkBasicInfo.termIndex];
            }
            $scope.isExist = $scope.term.existWeekCalendar;
            isHaveCache = false;
            cleanData();
        };

        $scope.date = {tempDate:""};
        /**监听学期改变，初始化听课日期*/
        $scope.$watch("term", function (newTerm, oldTerm) {
            if (newTerm !== oldTerm) {
                if (newTerm) {
                    $scope.tkBasicInfo.termId = newTerm.termId;
                    getTKKnobList(newTerm.termId).then(function () {
                        if (!isHaveCache) {
                            // 如果当前日期在学期范围内则显示当前日期
                            if (newTerm.startDate <= $scope.currentDate && $scope.currentDate <= newTerm.endDate) {
                                $scope.date.tempDate = $scope.currentDate;
                            } else {
                                // 如果当前日期没在学期日期范围内，则默认显示请选择
                                $scope.tkBasicInfo.tkDateDesc = "";
                                $scope.date.tempDate = null;
                            }
                        } else {
                            $scope.date.tempDate = $scope.tkBasicInfo.selectedTKDate;
                        }
                    });
                }
            }
        }, true);

        /**验证所选日期是否在所选学期日期范围内*/
        var validateDateScope = function (selectedDate) {
            if (!($scope.term.startDate <= selectedDate && selectedDate <= $scope.term.endDate)) {
                $scope.tkBasicInfo.tkDateDesc = null;
                $scope.date.tempDate = null;
                ynuiNotification.error({msg: "只能选择在" + $scope.term.startDate + "~" + $scope.term.endDate + "之间的日期!"});
                return false;
            }
            return true;
        };

        /**监听听课日期，查询教学周历信息*/
        $scope.$watch("date.tempDate", function (newDate, oldDate) {
            if (newDate !== oldDate) {
                if (newDate) {
                    if (validateDateScope(newDate)) {
                        $scope.tkBasicInfo.selectedTKDate = newDate;
                        getWeekAndDay($scope.term.termId, $scope.tkBasicInfo.selectedTKDate);
                    }
                } else {
                    $scope.tkBasicInfo.selectedTKDate = null;
                }
            }
        });

        /**改变日期*/
        $scope.selectedDate = function (data) {
            if (validateDateScope(data)) {
                $scope.date.tempDate = data;
                //$scope.tkBasicInfo.selectedTKDate = data;
                getWeekAndDay($scope.term.termId, $scope.date.tempDate);
            } else {
                $scope.date.tempDate = "";
            }
            cleanData();
            $scope.$apply();
        };

        var cleanData = function () {
            $scope.tkBasicInfo.courseName = null;
            $scope.tkBasicInfo.courseId = null;
            $scope.tkBasicInfo.teachClassId = null;
            $scope.tkBasicInfo.scheduleResultsId = null;
            $scope.tkBasicInfo.teachingAdminClassNameStr = null;
            $scope.tkBasicInfo.teachingAdminClassIdStr = null;
            $scope.tkBasicInfo.buildAndClassRoomNameStr = null;
            $scope.tkBasicInfo.classRoomList = [];
            $scope.tkBasicInfo.selectedClassRoomName = null;
            $scope.tkBasicInfo.selectedClassRoomId = null;
            $scope.tkBasicInfo.teacherId = null;
            $scope.tkBasicInfo.teacherName = null;
        };
        $scope.selectKnob = function (index) {
            $scope.tkBasicInfo.selectedKnobIndex = index;
            $scope.tkBasicInfo.timeType = $scope.knobList[index].timeType;
            $scope.tkBasicInfo.knob = $scope.knobList[index].knob;
            cleanData();
        };


        /**选择教师*/
        $scope.chooseTeacher = function () {
            tkStorageService.setItem("tkBasicInfo", $scope.tkBasicInfo);
            $location.path("/chooseTeacher").search({"type": 1});
        };


        /**获取教师课表中的课程信息*/
        var getScheduleDetailByTeacher = function () {
            $http.post(basePath + "/third/tkOutsideTaskRegisterApp/getScheduleDetailByTeacher.htm", $scope.tkBasicInfo
            ).success(function (data) {
                    if (data.status == 0) {
                        var result = data.result;
                        $scope.tkBasicInfo.courseName = result.courseName;
                        $scope.tkBasicInfo.courseId = result.courseId;
                        $scope.tkBasicInfo.teachClassId = result.teachClassId;
                        $scope.tkBasicInfo.scheduleResultsId = result.scheduleResultsId;
                        $scope.tkBasicInfo.teachingAdminClassNameStr = result.teachingAdminClassNameStr;
                        $scope.tkBasicInfo.teachingAdminClassIdStr = result.teachingAdminClassIdStr;
                        $scope.tkBasicInfo.buildAndClassRoomNameStr = result.buildAndClassRoomNameStr;
                        $scope.tkBasicInfo.classRoomList = result.classRoomList;
                        // 如果有教室
                        if ($scope.tkBasicInfo.classRoomList.length > 0) {
                            $scope.tkBasicInfo.selectedClassRoomName = $scope.tkBasicInfo.classRoomList[0].name;
                            $scope.tkBasicInfo.selectedClassRoomId = $scope.tkBasicInfo.classRoomList[0].id;
                            $scope.tkBasicInfo.selectedClassRoomIndex = 0;
                        } else {
                            $scope.tkBasicInfo.selectedClassRoomName = null;
                            $scope.tkBasicInfo.selectedClassRoomId = null;
                        }
                    }
                });
        };

        // 如果从选择授课教师页面跳转回来，则按授课教师查询课表信息
        if (isFromChooseTeacher) {
            if (isChangeTeacher) {
                getScheduleDetailByTeacher();
            }
        }


        /**切换教室*/
        $scope.changeClassRoom = function () {

            var listLength = $scope.tkBasicInfo.classRoomList.length;
            var index = $scope.tkBasicInfo.selectedClassRoomIndex;
            // 如果不是最后一个，则选中下一个
            if (index != listLength - 1) {
                index += 1;
                $scope.tkBasicInfo.selectedClassRoomName = $scope.tkBasicInfo.classRoomList[index].name;
                $scope.tkBasicInfo.selectedClassRoomId = $scope.tkBasicInfo.classRoomList[index].id;
                $scope.tkBasicInfo.selectedClassRoomIndex = index;
            } else {
                // 如果是最后一个，则选中第一个
                $scope.tkBasicInfo.selectedClassRoomName = $scope.tkBasicInfo.classRoomList[0].name;
                $scope.tkBasicInfo.selectedClassRoomId = $scope.tkBasicInfo.classRoomList[0].id;
                $scope.tkBasicInfo.selectedClassRoomIndex = 0;
            }
        };
        $scope.canChooseRoom = false;
        if (isFromChooseClassRoom) {
            $scope.canChooseRoom = true;
        }

        /**选择教室*/
        $scope.chooseClassRoom = function () {
            // 如果未先选择授课教师，则提示
            if (!$scope.tkBasicInfo.teacherId) {
                ynuiNotification.error({msg: "请先选择授课教师！"});
                return;
            }
            tkStorageService.setItem("tkBasicInfo", $scope.tkBasicInfo);
            $location.path("/classroomClassify");
        };

        var initTips = function () {
            return {
                isError: false,
                isNoData: false
            };
        };
        $scope.tips = initTips();

        $scope.tableList = [];
        var getTable = function () {
            $http.post(basePath + "/third/tkTaskRegisterApp/getTKTableDown.htm")
                .success(function (data) {
                    if (data.status == 0) {
                        $scope.tableList = data.result;
                        if ($scope.tableList.length > 0) {
                            if (!isHaveCache) {
                                $scope.tkBasicInfo.selectedTableId = $scope.tableList[0].id;
                                $scope.tkBasicInfo.selectedTableName = $scope.tableList[0].name;
                            }
                        } else {
                            $scope.tips.isNoData = true;
                        }
                    } else {
                        $scope.tips.isError = true;
                    }
                })
                .error(function () {
                    $scope.tips.isError = true;
                });
        };
        getTable();


        /**选中记录表*/
        $scope.selectTable = function (item) {
            $scope.tkBasicInfo.selectedTableId = item.id;
            $scope.tkBasicInfo.selectedTableName = item.name;
            $scope.closeModal();
        };

        /**取消*/
        $scope.cancle = function () {
            if (!$scope.isExist) {
                $location.path("/register_task");
            } else {
                var confirmPopup = $ionicPopup.confirm({
                    title: "提示",
                    template: '确定要放弃当前编辑内容吗？',
                    cancelText: "继续编辑",
                    okText: "放弃"
                });
                confirmPopup.then(function (res) {
                    if (res) {
                        tkStorageService.clearKey("tkBasicInfo");
                        $location.path("/register_task");
                    }
                });
            }

        };

        /**下一步*/
        $scope.nextStep = function () {
            //1.必传参数验证
            if (!$scope.tkBasicInfo.termId) {
                ynuiNotification.error({msg: "请选择学期！"});
                return;
            }
            if (!$scope.tkBasicInfo.selectedTKDate) {
                ynuiNotification.error({msg: "请选择听课日期！"});
                return;
            }
            if (!$scope.tkBasicInfo.knob) {
                ynuiNotification.error({msg: "请选择听课节次！"});
                return;
            }
            if (!$scope.tkBasicInfo.teacherId) {
                ynuiNotification.error({msg: "请选择授课教师！"});
                return;
            }
            if (!$scope.tkBasicInfo.selectedClassRoomId) {
                ynuiNotification.error({msg: "请选择教室！"});
                return;
            }
            if (!$scope.tkBasicInfo.selectedTableId) {
                ynuiNotification.error({msg: "请选择听课记录表！"});
                return;
            }
            tkStorageService.setItem("tkBasicInfo", $scope.tkBasicInfo);
            var tkRegisterData = tkStorageService.getItemNoDel("tkRegisterData");
            if(tkRegisterData && (tkRegisterData.saveTKResultRegisterVO.tkglTableId != $scope.tkBasicInfo.selectedTableId)){
                var confirmPopup = $ionicPopup.confirm({
                    title:"提示",
                    template: '切换听课打分表，会清空个别打分项信息，确定要切换吗？',
                    cancelText:"取消",
                    okText:"确定"
                });
                confirmPopup.then(function(res) {
                    if(res) {
                        $location.path("/register");
                    }
                });
            }else{
                $location.path("/register");
            }
        };


    }]);