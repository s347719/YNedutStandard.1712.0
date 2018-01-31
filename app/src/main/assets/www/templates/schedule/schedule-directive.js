angular.module('starter')

    .directive('scheduleDirective',['$http', function ($http){
        return {
            restrict: 'AE',
            replace: true,
            templateUrl: 'templates/schedule/schedule-directive.html',
            scope:{
                options:"="
            },
            link:scheduleLink
        };
        function scheduleLink(scope){
           /*开放接口*/
            scope.options.queryTimeTableByWeek = function(weekNum,userId,userType){
                if(weekNum){
                    scope.query(weekNum,userId,userType);
                }else{
                    console.error("weekNum is null !");
                }
            };

           /*查询数据*/
            scope.tableByWeekList = [];
            scope.scheduleList = [];
            scope.query = function(weekNum,userId,userType){
                $http.get(originBaseUrl + '/third/schedules/queryTimeTableByWeek.htm?' +
                    'week=' + weekNum +
                    '&queryObjectId=' + userId +
                    '&requestSource=' + userType
                ).success(function (res) {
                        if(res.status == 0){
                            if (res.result.schedule) {
                                if(scope.tableByWeekList == null){
                                    scope.loadErrorMsg = "课表加载失败！";
                                }
                                elme = angular.element;
                                var scheduleList = res.result.schedule;
                                scope.tableByWeekList = scheduleList.scheduleDayVOList;
                                scope.scheduleList = scheduleList.scheduleWeekCourseVO;
                                scope.dataContent = true;
                                /*设置一天课表，节次高度*/
                                var amKnob = scope.tableByWeekList[0].amKnobList.length,
                                    pmKnob = scope.tableByWeekList[0].pmKnobList.length,
                                    ngKnob = scope.tableByWeekList[0].ngKnobList.length,
                                    wrapHeight = elme(".schedule-times-right").height(),
                                    itemNumber = amKnob + pmKnob + ngKnob,
                                    numberHeight = wrapHeight / itemNumber,
                                    weekWidth = 68 * scope.tableByWeekList.length;
                                scope.itemHeight = {
                                    "height":numberHeight+'px',
                                    "line-height":numberHeight+'px'
                                };
                                scope.amKnobHeight =  {
                                    "height":amKnob*(numberHeight)+'px',
                                    "width": weekWidth + 'px'
                                };
                                scope.pmKnobHeight =  {
                                    "height":pmKnob*(numberHeight)+'px',
                                    "width": weekWidth + 'px'
                                };
                                scope.ngKnobHeight =  {
                                    "height":ngKnob*(numberHeight)+'px',
                                    "width": weekWidth + 'px'
                                };
                                /*判断晚上是否没有节次*/
                                if (ngKnob > 0) {
                                    scope.isNoNgKnob = true;
                                }
                                /*判断是否有周课*/
                                if (scope.scheduleList == null) {
                                    scope.weekCourse = false;
                                    scope.weekActivity = false;
                                    scope.nocourse = false;
                                } else {
                                    scope.weekCourseName = scope.scheduleList.name;
                                    scope.nocourse = true;
                                    if (scope.scheduleList.type == 1) {
                                        scope.weekCourse = true;
                                    } else {
                                        scope.weekActivity = true;
                                    }
                                }
                                scope.loadErrorMsg ='';
                            } else {
                                scope.loadErrorMsg = "课表加载失败！";
                                scope.dataContent = false;
                                if(scope.options.errorCallAction){
                                    scope.options.errorCallAction();
                                }
                            }
                        }else{
                            scope.loadErrorMsg = res.message;
                        }
                    }).error(function () {
                        if(scope.options.errorCallAction){
                            scope.options.errorCallAction();
                        }
                        scope.loadErrorMsg = '加载失败，请重试！';
                    });
            };

            /*课程详情弹框显示*/
            scope.showMoreCourse = function (courseList, daily) {
                if (courseList.courseList == null) {

                } else {
                    scope.isShowMoreCourse = !scope.isShowMoreCourse;
                    scope.courseMsgList = courseList.courseList;
                    scope.knob = courseList.knob;
                    scope.timeTypeValue = courseList.timeTypeValue;
                    scope.dailys = daily;
                }
            };
            scope.hideMoreCourse = function () {
                scope.isShowMoreCourse = !scope.isShowMoreCourse;
            };

            (function(){
                scope.query(scope.options.weekNum,scope.options.userId,scope.options.userType);
            })()
        }
    }]);


