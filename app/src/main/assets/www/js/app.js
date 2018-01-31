var originBaseUrl = '';
angular.module('starter', ['ionic', 'ngCordova', 'ksSwiper','staticModule'])

    .run(function ($ionicPlatform, $rootScope, $location) {
        $ionicPlatform.ready(function () {
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
                cordova.plugins.Keyboard.disableScroll(true)
            }
            if (window.StatusBar) {
                StatusBar.styleDefault();
            }
            ionic.Platform.fullScreen(true,true);
            //本应为false，因messenger对windowSoftInputMode有配置，此处特定为true
            ionic.Platform.isFullScreen = true;
        });

        if(!$rootScope.originBaseUrl){
            var searchObj = $location.search(),
                access_token = searchObj.access_token,
                userId = searchObj.userId,
                userType= searchObj.userType,
                originUrl = searchObj.originUrl;
            if(originUrl && userId && access_token){
                $rootScope.authorizationStr = {
                    'access_token': access_token,
                    'userId': userId,
                    'userType': userType
                };
                originBaseUrl = $rootScope.originBaseUrl = originUrl;
                if(window&&window.localStorage){
                    window.localStorage.setItem("authorizationStr",JSON.stringify($rootScope.authorizationStr));
                    window.localStorage.setItem("originBaseUrl",JSON.stringify($rootScope.originBaseUrl));
                }
            }else{
                if(window&&window.localStorage){
                    $rootScope.authorizationStr = JSON.parse(window.localStorage.getItem("authorizationStr"));
                    originBaseUrl = $rootScope.originBaseUrl = JSON.parse(window.localStorage.getItem("originBaseUrl"));
                }
            }
        }
    })
    //路由配置
    .config(function ($stateProvider, $ionicConfigProvider, $urlRouterProvider) {
        $ionicConfigProvider.templates.maxPrefetch(0);
        $ionicConfigProvider.views.maxCache(0);
        $stateProvider
            //test
            .state('demo', {
                url: '/demo',
                templateUrl: 'templates/demo/demo.html'
            })
            //一卡通
            .state('onecard', {
                url: '/onecard',
                templateUrl: 'templates/onecard/onecard.html'
            })
            //学生成绩-期中
            .state('acvmt_stu_midterm', {
                url: '/acvmt_stu_midterm',
                templateUrl: 'templates/achievement/acvmt_stu.html'
            })
            //学生成绩-期末
            .state('acvmt_stu_terminal', {
                url: '/acvmt_stu_terminal',
                templateUrl: 'templates/achievement/acvmt_stu.html'
            })
            //班主任成绩-期中
            .state('acvmt_teacher_midterm', {
                url: '/acvmt_teacher_midterm',
                templateUrl: 'templates/achievement/acvmt_teacher.html'
            })
            //班主任成绩-期末
            .state('acvmt_teacher_terminal', {
                url: '/acvmt_teacher_terminal',
                templateUrl: 'templates/achievement/acvmt_teacher.html'
            })
            .state('acvmt_detail', {

                url: '/acvmt_detail',
                templateUrl: 'templates/achievement/acvmt_detail.html'
            })
            //按课表点名
            .state('attendance', {

                url: '/attendance',
                templateUrl: 'templates/attendance/attendance.html'
            })
            //选人点名
            .state('attendance_people', {

                url: '/attendance_people',
                templateUrl: 'templates/attendance/attendance_people.html'
            })
            //课堂考勤（学生访问）
            .state('attendance_student', {

                url: '/attendance_student',
                templateUrl: 'templates/attendance/attendance_student.html'
            })
            //学生访问
            .state('leave_view', {

                url: '/leave_view',
                templateUrl: 'templates/leave/leave_view.html'
            })
            //请假审核
            .state('leave_check', {

                url: '/leave_check',
                templateUrl: 'templates/leave/leave_check.html'
            })
            //学生销假
            .state('leave_delete', {

                url: '/leave_delete',
                templateUrl: 'templates/leave/leave_delete.html'
            })
            //创建或修改请假
            .state('leave_createOrModify', {

                url: '/leave_createOrModify',
                templateUrl: 'templates/leave/leave_createOrModify.html'
            })
            //销假记录
            .state('leave_no', {

                url: '/leave_no',
                templateUrl: 'templates/leave/leave_no.html'
            })
            //通知详情
            .state('inform_detail', {

                url: '/inform_detail',
                templateUrl: 'templates/inform/inform_detail.html'
            })
            //查看通知公告
            .state('appViewNotice',{

                url:'/appViewNotice',
                templateUrl:'templates/message/appViewNotice.html'
            })
            //班级情况统计
            .state('class_statistics',{

                url:'/class_statistics',
                templateUrl:'templates/classSituation/class_statistics.html'
            })
            //班级情况统计 应到学生
            .state('class_ydStudent',{

                url:'/class_ydStudent',
                templateUrl:'templates/classSituation/class_ydStudent.html'
            })
            //班级情况统计 处分学生
            .state('class_punishStudent',{

                url:'/class_punishStudent',
                templateUrl:'templates/classSituation/class_punishStudent.html'
            })
            //班级情况统计 请假学生
            .state('class_leaveStudent',{

                url:'/class_leaveStudent',
                templateUrl:'templates/classSituation/class_leaveStudent.html'
            })
            //课堂考勤统计
            .state('ktkq_atistics',{

                url:'/ktkq_atistics',
                templateUrl:'templates/classSituation/ktkq_tatistics.html'
            })
            //课堂考勤统计 学生详情
            .state('ktkq_student',{
                url:'/ktkq_student',
                templateUrl:'templates/classSituation/ktkq_student.html'
            })
            //添加学生
            .state('add_report_stus',{
                url:'/add_report_stus',
                templateUrl:'templates/report/addReportStu.html'
            })
            //学生报到
            .state('report_detail', {
                url: '/report_detail',
                templateUrl: 'templates/report/report_detail.html'

            })
				/*************************************移动端选课start**********************************************************/
            //=====选课
            .state('choose_lessons', {
                url: '/choose_lessons',
                templateUrl: 'templates/choose_lessons/choose_lessons.html'
            })
            .state('choose_lessons_detail', {
                url: '/choose_lessons_detail',
                templateUrl: 'templates/choose_lessons/choose_lessons_detail.html'
            })
			/*************************************移动端选课end**********************************************************/
            //========宿舍考核
            //任务管理
            .state('dormitory_examine', {
                url: '/dormitory_examine',
                templateUrl: 'templates/dormitory_examine/dormitory_examine.html'
            })
            //添加任务
            .state('add_task_dmt', {
                url: '/add_task_dmt',
                templateUrl: 'templates/dormitory_examine/add_task_dmt.html'
            })
            //打分 宿舍考核
            .state('grade_dormitory', {
                url: '/grade_dormitory',
                templateUrl: 'templates/dormitory_examine/grade_dormitory.html'
            })
            //填写表单
            .state('dormitory_form', {
                url: '/dormitory_form',
                templateUrl: 'templates/dormitory_examine/dormitory_form.html'

            })
            //班级寝室统计
            .state('dormitory_list', {
                url: '/dormitory_list',
                templateUrl: 'templates/class_dormitory_statistics/dormitory_list.html'
            })
            //批量审批 请假申请
            .state('apply_approval', {
                url: '/apply_approval',
                templateUrl: 'templates/apply/apply_approval.html'
            })
        $urlRouterProvider.otherwise('/schedule');
    })
    .config(function ($httpProvider,$provide) {
        $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
        $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=utf-8';
        //全局拦截器
        $httpProvider.interceptors.push('globalHttpInterceptor');

        var param = function (obj) {
            var query = "", name, value, fullSubName, subName, subValue, innerObj, i;
            for (name in obj) {
                value = obj[name];
                if (value instanceof Array) {
                    for (i = 0; i < value.length; ++i) {
                        subValue = value[i];
                        fullSubName = name;
                        innerObj = {};
                        innerObj[fullSubName] = subValue;
                        query += param(innerObj) + '&';
                    }
                } else if (value instanceof Object) {
                    for (subName in value) {
                        subValue = value[subName];
                        fullSubName = name + "[" + subName + "]";
                        innerObj = {};
                        innerObj[fullSubName] = subValue;
                        query += param(innerObj) + "&";
                    }
                } else if (value !== undefined && value !== null) {
                    query += encodeURIComponent(name) + "=" + encodeURIComponent(value) + "&";
                }
            }
            return query.length ? query.substr(0, query.length - 1) : query;
        };
        $httpProvider.defaults.transformRequest = [function (data) {
            return angular.isObject(data) && String(data) !== "[object File]" ? param(data) : data;
        }];
    })
    .factory('globalHttpInterceptor', ['$q','$rootScope','$injector', function($q,$rootScope,$injector) {
        var reqTimes = 0,
            reqTime = 0;
        var $ionicLoading;
        return {
            // optional method
            'request': function(config) {
                $ionicLoading = $injector.get('$ionicLoading');
                if(/\.htm/.test(config.url) && !(/\.html/.test(config.url))){
                    if(!angular.element('loading-container visible').length){
                        $ionicLoading.show({
                            noBackdrop: true,
                            hideOnStateChange: true
                        }).then(function(){
                            reqTime = new Date().getTime()
                        });
                    }
                    if(window&&window.localStorage && JSON.parse(window.localStorage.getItem("authorizationStr"))){
                        $rootScope.authorizationStr = JSON.parse(window.localStorage.getItem("authorizationStr"));
                    }
                    if(config.method == 'GET'){
                        if(/\?/.test(config.url)){
                            config.url += '&access_token=' + $rootScope.authorizationStr.access_token +
                            '&userId=' + $rootScope.authorizationStr.userId +
                            '&userType=' + $rootScope.authorizationStr.userType;
                        }else{
                            config.url += '?access_token=' + $rootScope.authorizationStr.access_token +
                            '&userId=' + $rootScope.authorizationStr.userId +
                            '&userType=' + $rootScope.authorizationStr.userType;
                        }
                    }else{
                        config.data = config.data || {};
                        config.data.access_token = $rootScope.authorizationStr.access_token;
                        config.data.userId = $rootScope.authorizationStr.userId;
                        config.data.userType = $rootScope.authorizationStr.userType;
                        // 过滤utf-16字符
                        var dataString = JSON.stringify(config.data);
                        var newDataString = dataString.replace(/[\uD800-\uDBFF][\uDC00-\uDFFF]/g,'');
                        config.data = JSON.parse(newDataString);
                    }
                }
                return config;
            },
            'requestError': function(rejection) {
                // do something on error
                $ionicLoading.hide();
                return $q.reject(rejection);
            },
            'response': function(response) {
                // do something on success
                reqTimes--;
                if(reqTimes<1){
                    reqTimes = 0;
                    var timeDif = new Date().getTime()-reqTime;
                    if(timeDif < 300){
                        var $timeout = $injector.get('$timeout');
                        $timeout(function(){
                            $ionicLoading.hide();
                        },300-timeDif)
                    }else{
                        $ionicLoading.hide();
                    }
                }
                return response;
            },
            'responseError': function(rejection) {
                // do something on error
                $ionicLoading.hide();
                return $q.reject(rejection);
            }
        };
    }])
    //onFinishRender
    .directive('onFinishRender', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                if (scope.$last === true) {
                    $timeout(function () {
                        scope.$emit('ngRepeatFinished');
                    });
                }
            }
        }
    })
    //ynuiDatetimepicker fanjiaben
    .directive('ynuiDatetimepicker', [function () {
        return {
            restrict: 'A',
            require: '?ngModel',
            scope: {
                range: '@'
            },
            link: function (scope, elem, attr, ctrl) {
                var type = attr.ynuiDatetimepicker;
                var isRange = function () {
                    return scope.range;
                };
                var rangeOpt = {
                    mode: 'scroller',
                    display: 'modal',
                    lang: 'zh',
                    stepMinute: 1,
                    minWidth: 50,
                    maxDate: new Date((new Date()).getFullYear() + 10, 11, 31, 23, 59),
                    dateFormat: 'yyy-mm-dd',
                    timeFormat: 'HH:ii'
                };
                var optDefault = {
                    mode: 'scroller',
                    display: 'modal',
                    lang: 'zh',
                    stepMinute: 1,
                    minWidth: 50,
                    maxDate: new Date((new Date()).getFullYear() + 10, 11, 31, 23, 59),
                    dateFormat: 'yyy-mm-dd',
                    timeFormat: 'HH:ii',
                    onSelect: function (valueText, inst) {
                        if (ctrl) {
                            ctrl.$setViewValue(valueText);
                        }
                        if (isRange()) {
                            var rangeElem = angular.element('#' + scope.range);
                            rangeOpt.minDate = inst.getVal();
                            bindPicker(rangeElem,rangeOpt);
                            if (valueText > rangeElem.val() && rangeElem.val() != '') {
                                rangeElem.val(valueText);
                                if (ctrl) {
                                    ctrl.$setViewValue(valueText);
                                }
                            }
                        }
                    },
                    onShow: function (html, valueText, inst) {

                    },
                    onHide: function (inst) {

                    }
                };
                var bindPicker = function (bindElem,opt) {
                    switch (type) {
                        case 'YMD':
                            bindElem.mobiscroll().date(opt);
                            break;
                        case 'HI':
                            bindElem.mobiscroll().time(opt);
                            break;
                        default :
                            bindElem.mobiscroll().datetime(opt);
                    }
                };
                bindPicker(elem,optDefault);
            }
        }
    }])
    .directive('viewPicture',['$ionicModal', function($ionicModal){
        return {
            restrict: 'A',
            link: function ($scope, elem, attrs) {
                $scope.modelIsRemove = false;
                $scope.picUrl = null;
                $scope.viewHeight = angular.element(window).height()-44;
                elem.css('max-width','100%');
                elem.on('click',function(){
                    $scope.picUrl = attrs.src;
                    $scope.openModal();
                });
                var template = '<ion-modal-view>' +
                    '<ion-header-bar align-title="center">' +
                    '<button class="button button-clear button-primary" ng-click="closeModal()">返回</button>' +
                    '<h1 class="title">预览</h1>' +
                    '</ion-header-bar>' +
                    '<ion-content>' +
                    '<ion-scroll direction="xy" class="text-center" style="height: {{viewHeight}}px">' +
                    '<img ng-src="{{picUrl}}">' +
                    '</ion-scroll>' +
                    '</ion-content>' +
                    '</ion-modal-view>';
                $scope.pictureModel = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });
                $scope.openModal = function() {
                    if($scope.modelIsRemove){
                        $scope.pictureModel = $ionicModal.fromTemplate(template, {
                            scope: $scope,
                            animation: 'slide-in-up'
                        });
                    }
                    $scope.pictureModel.show().then(function(){
                        $scope.modelIsRemove = false;
                    });
                };
                $scope.closeModal = function() {
                    $scope.pictureModel.remove().then(function(){
                        $scope.modelIsRemove = true;
                    });
                };
                // Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.pictureModel.remove();
                });
            }
        }
    }])
	//头像预览
	 .directive('viewHead',['$ionicModal', function($ionicModal){
        return {
            restrict: 'A',
            link: function ($scope, elem, attrs) {
                $scope.modelIsRemove = false;
                $scope.picUrl = null;
                $scope.viewHeight = angular.element(window).height()-44;
                elem.on('click',function($event){
					if(attrs.viewHead){
						$scope.picUrl = attrs.viewHead;
					}else{
						$scope.picUrl = attrs.src;
					}
					if(!$scope.picUrl){
						return;
					}
                    $scope.openModal();
					 if ($event && $event.stopPropagation){
						$event.stopPropagation();
					}
                });
                var template = '<ion-modal-view>' +
                    '<ion-header-bar align-title="center">' +
                    '<button class="button button-clear button-primary" ng-click="closeModal()">返回</button>' +
                    '<h1 class="title">预览</h1>' +
                    '</ion-header-bar>' +
                    '<ion-content>' +
                    '<ion-scroll delegate-handle="viewHeadIonScroll" direction="xy" class="text-center" style="height: {{viewHeight}}px">' +
                    '<img ng-src="{{picUrl}}">' +
                    '</ion-scroll>' +
                    '</ion-content>' +
                    '</ion-modal-view>';
                $scope.pictureModel = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });
                $scope.openModal = function() {
                    if($scope.modelIsRemove){
                        $scope.pictureModel = $ionicModal.fromTemplate(template, {
                            scope: $scope,
                            animation: 'slide-in-up'
                        });
                    }
                    $scope.pictureModel.show().then(function(){
                        $scope.modelIsRemove = false;
                    });
                };
                $scope.closeModal = function() {
                    $scope.pictureModel.remove().then(function(){
                        $scope.modelIsRemove = true;
                    });
                };
                // Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.pictureModel.remove();
                });
            }
        }
    }])
    //ynuiNotification fanjiaben
    .factory('ynuiNotification', [function () {
        var ynuiNotification = {};
        var optDefault = {
            "closeButton": false,
            "debug": false,
            "newestOnTop": false,
            "progressBar": false,
            "positionClass": "toast-top-center",
            "preventDuplicates": false,
            "onclick": null,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "2000",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        };
        toastr.options = optDefault;
        var beforeShow = function (obj) {
            toastr.remove();
            toastr.options = angular.extend({}, optDefault, obj.opts);
        };
        ynuiNotification.warning = function (obj) {
            beforeShow(obj);
            toastr.warning(obj.msg, obj.title);
        };
        ynuiNotification.success = function (obj) {
            beforeShow(obj);
            toastr.success(obj.msg, obj.title);
        };
        ynuiNotification.error = function (obj) {
            beforeShow(obj);
            toastr.error(obj.msg, obj.title);
        };
        ynuiNotification.clear = function () {
            toastr.clear();
        };
        return ynuiNotification;
    }])
    //调试用
    .controller('testCtrl', ['$scope', 'ynuiNotification', function ($scope, ynuiNotification) {
        //提示组件Demo
        $scope.notifyTest1 = function () {
            ynuiNotification.success({msg: '成功提示'});
        };
        $scope.notifyTest2 = function () {
            ynuiNotification.error({msg: '成功提示'});
        };
        $scope.notifyTest3 = function () {
            ynuiNotification.warning({msg: '成功提示'});
        };
        $scope.notifyTest4 = function () {
            ynuiNotification.success({msg: '你已学会提示组件用法', title: '设置标题'});
        };
        $scope.notifyTest5 = function () {
            ynuiNotification.success({msg: '设置延迟500ms', opts: {timeOut: 500}});
        };
        $scope.notifyTest6 = function () {
            ynuiNotification.clear();
        }
    }])
    //一卡通
    .controller('OnecardCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate) {
            $scope.balanceDetailToggle = false;
            $scope.isBack = false;
            $scope.loadECardInfoError = false;
            //查询
            $scope.queryOneCard = function () {
                $http.post(originBaseUrl + '/third/onecard/queryEcardInfo.htm').success(function (data) {
                    if (data.status == 0) {
                        $scope.eCardInfo = data.result;
                        $scope.eCardInfo.totalBalance = formatNumber($scope.eCardInfo.totalBalance, 2, 1);
                        $scope.eCardInfo.accountBalance = formatNumber($scope.eCardInfo.accountBalance, 2, 1);
                        $scope.eCardInfo.allowanceBalance = formatNumber($scope.eCardInfo.allowanceBalance, 2, 1);
                        $scope.eCardInfo.todayTotalMoney = formatNumber($scope.eCardInfo.todayTotalMoney, 2, 1);
                        $scope.eCardInfo.weekTotalMoney = formatNumber($scope.eCardInfo.weekTotalMoney, 2, 1);
                        $scope.eCardInfo.monthTotalMoney = formatNumber($scope.eCardInfo.monthTotalMoney, 2, 1);
                    }
                }).error(function () {
                    $scope.loadECardInfoError = true;
                    $scope.loadErrorMsg = '加载失败，请重试！';
                });
            };
            $scope.queryOneCard();
            //数字处理
            function formatNumber(num, cent, isThousand) {
                if (num != null) {
                    num = num.toString().replace(/\$|\,/g, '');

                    // 检查传入数值为数值类型
                    if (isNaN(num))
                        num = "0";
                    // 获取符号(正/负数)
                    sign = (num == (num = Math.abs(num)));

                    num = Math.floor(num * Math.pow(10, cent) + 0.50000000001);  // 把指定的小数位先转换成整数.多余的小数位四舍五入
                    cents = num % Math.pow(10, cent);              // 求出小数位数值
                    num = Math.floor(num / Math.pow(10, cent)).toString();   // 求出整数位数值
                    cents = cents.toString();               // 把小数位转换成字符串,以便求小数位长度

                    // 补足小数位到指定的位数
                    while (cents.length < cent)
                        cents = "0" + cents;

                    if (isThousand) {
                        // 对整数部分进行千分位格式化.
                        for (var i = 0; i < Math.floor((num.length - (1 + i)) / 3); i++)
                            num = num.substring(0, num.length - (4 * i + 3)) + ',' + num.substring(num.length - (4 * i + 3));
                    }

                    if (cent > 0)
                        return (((sign) ? '' : '-') + num + '.' + cents);
                    else
                        return (((sign) ? '' : '-') + num);
                }
            }

            var pageNumber = 0;
            var pageSize = 40;
            var currentData = new Date();
            //详细的一卡通数据的重新拼装以便遍历
            var list = [];
            $scope.assembleData = function (data) {
                var dataDetail;
                var consumeList = [];    //当天消费记录
                var year = currentData.getFullYear();
                var month = currentData.getMonth() + 1;
                var day = currentData.getDate();
                for (var a = 0; a < data.length; a++) {
                    if (data[a].time.getFullYear() == year) {
                        if (data[a].time.getMonth() == month) {
                            if (data[a].time.getDate() == day) {
                                consumeList.push({address: data[a].address, money: data[a].money});
                            }
                        }
                    }
                    dataDetail = {
                        day: data[a].time.getMonth() + "-" + data[a].time.getDate(),
                        week: week(data[a].time),
                        detail: consumeList
                    };
                }
                currentData = new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
                list.push(dataDetail);
            };
            $scope.getShowData = function (arr) {
                if (arr instanceof Array) {
                    var finalResult = [],
                        dayDatas,//同一天的消费记录
                        year,
                        month,
                        day;
                    for (var i = 0, len = arr.length; i < len; i++) {
                        var itemDate = arr[i].time,
                            _year = itemDate.getFullYear(),
                            _month = itemDate.getMonth(),
                            _day = itemDate.getDate();
                        if (_year == year && _month == month && day == _day) {//同一天
                            dayDatas.arr.push({
                                address: arr[i].address,
                                time: arr[i].time.getHours() + ":" + (arr[i].time.getMinutes() >= 10 ? arr[i].time.getMinutes() : "0" + arr[i].time.getMinutes()),
                                money: arr[i].money > 0 ? "+" + arr[i].money.toFixed(2) : arr[i].money.toFixed(2)
                            });
                        } else {
                            if (i == arr.length - 1) {
                                doPush = true;
                            }
                            if (dayDatas) {
                                finalResult.push(dayDatas);
                            }
                            dayDatas = {
                                name: $scope.isSameDayOrPre(arr[i].time, new Date(), false) ? "今天" : ($scope.isSameDayOrPre(arr[i].time, new Date(), true) ? "昨天" : _month + 1 + "-" + _day),
                                arr: [],
                                weekDay: week(arr[i].time)
                            };
                            dayDatas.arr.push({
                                address: arr[i].address,
                                time: arr[i].time.getHours() + ":" + (arr[i].time.getMinutes() >= 10 ? arr[i].time.getMinutes() : "0" + arr[i].time.getMinutes()),
                                money: arr[i].money > 0 ? "+" + arr[i].money.toFixed(2) : arr[i].money.toFixed(2)
                            });
                        }
                        year = _year;
                        month = _month;
                        day = _day

                    }
                    finalResult.push(dayDatas);
                    return finalResult;
                } else {
                    return [];
                }
            };
            /**
             * 判断当前日期是当天或者前一天
             * @returns {boolean}
             */
            $scope.isSameDayOrPre = function (item, sysDate, isPre) {
                if (isPre) {
                    return !$scope.isSameDayOrPre(item, sysDate, false) && Math.abs(item - sysDate) < 24 * 60 * 60 * 1000;
                } else {//同一天
                    return item.getFullYear() == sysDate.getFullYear() && item.getMonth() == sysDate.getMonth() && item.getDate() == sysDate.getDate();
                }
            };
            $scope.items = [];
            $scope.items_copy = [];
            $scope.eCardInfoDetail = [];
            ($scope.getData = function (func) {
                $http.post(originBaseUrl + '/third/onecard/queryConsumptionScores.htm?pageNumber=' + pageNumber + "&pageSize=" + pageSize).success(function (data) {
                    if (data.status == 0) {
                        $scope.page = data.result;
                        if ($scope.page.content && $scope.page.content.length > 0) {
                            var arr = $scope.items_copy.concat($scope.page.content);
                            $scope.items_copy = arr;
                            $scope.items = arr;
                            $scope.eCardInfoDetail = data.result.content;
                            angular.forEach($scope.eCardInfoDetail, function (item) {
                                var a = item.time.replace(/-/g, "/");
                                item.time = new Date(a);
                            });
                            $scope.items = $scope.getShowData($scope.items);
                        }
                        $scope.moreDataCanBeLoaded = function () {
                            return data.result.content.length;
                        };
                        $scope.dataError = $scope.items.length < 1;
                        if ($scope.loadECardInfoError) {
                            $scope.dataErrorMsg = '加载失败，请重试！';
                        } else {
                            $scope.dataErrorMsg = '没有消费/充值记录！';
                        }
                        if (func) {
                            func();
                        }
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.dataError = true;
                    if ($scope.loadECardInfoError) {
                        $scope.dataErrorMsg = '加载失败，请重试！';
                    } else {
                        $scope.dataErrorMsg = '加载消费/充值记录失败，请重试！';
                    }
                });

            })(0);
            function week(day) {
                var week;
                if (day.getDay() == 0) {
                    week = "星期日";
                }
                if (day.getDay() == 1) {
                    week = "星期一"
                }
                if (day.getDay() == 2) {
                    week = "星期二"
                }
                if (day.getDay() == 3) {
                    week = "星期三"
                }
                if (day.getDay() == 4) {
                    week = "星期四"
                }
                if (day.getDay() == 5) {
                    week = "星期五"
                }
                if (day.getDay() == 6) {
                    week = "星期六"
                }
                return week;
            }

            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.page = null;//重新获取page对象
                $scope.items_copy = [];
                $scope.items = [];
                pageNumber = 0;
                $scope.queryOneCard();
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };

            $scope.loadMore = function () {
                //模拟请求数据
                pageNumber += 1;
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                });
            };

            $scope.$on('stateChangeSuccess', function () {
                $scope.loadMore();
            });
        }])
    //成绩(学生)
    .controller('AchievementCtrl', ['$location', '$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',"$rootScope","$ionicModal",
        function ($location, $scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$rootScope,$ionicModal) {
            if ($location.$$path == "/acvmt_stu_midterm") {//期中
                $scope.scoreProperty = 2;
            } else {//期末
                $scope.scoreProperty = 3;
            }
            $scope.dataLoading = true;
            $scope.isBack = false;
            $scope.items = [];
            //请求成绩数据
            $scope.getData = function (pageNumber, termId, func) {
                $scope.dataLoading = true;
                $http.post(originBaseUrl+"/third/appAcvmt/queryResultByStudent.htm", {
                    termId: termId,
                    pageNumber: pageNumber,
                    scoreProperty: $scope.scoreProperty,
                    userId:$rootScope.authorizationStr.userId
                })
                    .success(function (data) {
                        $scope.loadError = false;
                        $scope.dataLoading = false;
                        if (data.result) {
                            $scope.items = $scope.items.concat(data.result.content);
                            $scope.page = data.result;
                            console.log($scope.page)
                        }
                        if ($scope.items.length < 1) {
                            $scope.loadError = true;
                            $scope.loadErrorMsg = '您在该学期没有课程，无法查看成绩！';
                        }
                        if (func) {
                            func();
                        }
                    }).error(function () {
                        $scope.dataLoading = false;
                        $scope.loadError = true;
                        $scope.loadErrorMsg = '加载失败，请重试！';
                        if (func) {
                            func();
                        }
                    });
            };
            //进重新请求成绩
            $scope.reloadData = function () {
                if (!$scope.termList) {
                    $scope.doRefresh();
                } else {
                    $scope.dataLoading = true;
                    $ionicLoading.show({
                        template: '正在刷新...'
                    });
                    $scope.page = null;//重新获取page对象
                    $scope.items = [];
                    if ($scope.term && $scope.term.id) {
                        $scope.getData(0, $scope.term.id, function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                            $ionicScrollDelegate.scrollTop();
                            $ionicLoading.hide();
                        });
                    } else {
                        $scope.dataLoading = false;
                        $timeout(function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                            $ionicScrollDelegate.scrollTop();
                            $ionicLoading.hide();
                        }, 250);
                    }
                }
            };
            $scope.getLastOrNextTerm = function (termList, termId, type) {
                if (!(termList instanceof Array)) {
                    return false;
                } else {
                    for (var i = 0, len = termList.length; i < len; i++) {
                        if (termList[i].id == termId) {
                            if (type < 0 && i > 0) {//上一个学期
                                if ($scope.curTerm && termList[i - 1].id == $scope.curTerm.id) {
                                    termList[i - 1].isCur = true;
                                }
                                termList[i - 1].isFirst = i == 1;
                                return termList[i - 1];
                            } else if (type > 0 && i < len - 1) {//下一个学期
                                if ($scope.curTerm && termList[i + 1].id == $scope.curTerm.id) {
                                    termList[i + 1].isCur = true;
                                }
                                termList[i + 1].isLast = i == len - 2;
                                return termList[i + 1];
                            } else {
                                return false;
                            }
                        }
                    }
                }
            };
            $scope.toLastOrNextTerm = function (type) {
                if ($scope.curTerm && $scope.termList && $scope.termList.length > 0) {
                    $scope.page = null;//重新获取page对象
                    var curTermId = $scope.term.id;
                    var result = $scope.getLastOrNextTerm($scope.termList, curTermId, type);
                    if (result) {
                        $scope.items = [];
                        $scope.term = result;
                        $scope.termTitle = result.isCur ? result.name + "[本学期]" : result.name;
                        $scope.getData(0, $scope.term.id);
                    } else {
                        $scope.showWarning("没有了！")
                    }
                }
            };
            $scope.verifyTermIsFirstOrLast = function (arr, id) {
                if (!(arr instanceof Array) || arr.length < 2) {
                    return {isFirst: true, isLast: true};
                } else {
                    for (var i = 0, len = arr.length; i < len; i++) {
                        if (arr[i].id == id) {
                            var isFirst, isLast;
                            if (i == 0) {
                                isFirst = true;
                            }
                            if (i == len - 1) {
                                isLast = true;
                            }
                            return {isFirst: isFirst, isLast: isLast};
                        }
                    }
                }
            };
            ($scope.getTerm = function (fun) {
                $scope.hideHead = false;
                $http.post(originBaseUrl+"/third/appAcvmt/getTerm.htm").success(function (data) {
                    $scope.loadError = false;
                    var result = data.result;
                    if (result && result.curTerm) {
                        $scope.curTerm = result.curTerm;
                        $scope.term = result.curTerm;
                        $scope.termTitle = result.curTerm.name + "[本学期]";//2014-2015-2 [本学期]
                        $scope.getData(0, result.curTerm.id, fun);
                    }
                    if (result && result.termList && result.termList.length > 0) {
                        $scope.termList = result.termList;
                        if (!$scope.curTerm) {
                            $scope.term = result.termList[0];
                            $scope.curTerm = $scope.term;
                            $scope.termTitle = $scope.term.name;
                            $scope.getData(0, $scope.term.id, fun);
                        }
                    } else {
                        if (fun) {
                            fun();
                        }
                        $scope.dataLoading = false;
                        $scope.loadError = true;
                        $scope.hideHead = true;
                        $scope.loadErrorMsg = '您没有学期信息，无法查看成绩！';
                    }
                }).error(function () {
                    if (fun) {
                        fun();
                    }
                    $scope.dataLoading = false;
                    $scope.loadError = true;
                    $scope.loadErrorMsg = '加载失败，请重试！';

                });
            })();

            $scope.loadMore = function () {
                $scope.getData($scope.page.number + 1, $scope.term.id, function () {
                    $timeout(function () {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                    }, 500);
                });
            };

            $scope.$on('stateChangeSuccess', function () {
                $scope.loadMore();
            });

            $scope.moreDataCanBeLoaded = function () {
                return $scope.page && $scope.items.length < $scope.page.totalElements;
            };

            $scope.doRefresh = function () {
                $scope.dataLoading = true;
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.page = null;//重新获取page对象
                $scope.items = [];
                $scope.getTerm(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.showWarning = function (msg) {
                $ionicLoading.show({
                    template: msg
                });
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 500);
            };
            /*成绩记录*/
            $ionicModal.fromTemplateUrl('recordList.html',{
                scope:$scope,
                animation:'slide-in-up'
            }).then(function(modal){
                $scope.modal1 = modal;
            });
            $scope.recordListClick = function(item) {
                $http.post(originBaseUrl+"/third/appAcvmt/queryRecordByCourse.htm?",{
                    termId:$scope.term.id,
                    stuId:$rootScope.authorizationStr.userId,
                    kourseId:item.courseId,
                    executeTerm:item.executeTermByte,
                    teachType:item.teachTypeByte,
                    source:$scope.scoreProperty
                }).success(function (data) {
                    $scope.recoredList = data.result;
                })
                $scope.modal1.show();
            };
            $scope.closeModal = function() {
                $scope.modal1.hide();
            };
        }])
    //成绩-班主任
    .controller('AchievementTeacherCtrl', ['$location', '$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',"$rootScope",
        function ($location, $scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$rootScope) {
            $scope.isBack = false;
            $scope.dataLoading = true;
            if ($location.$$path == "/acvmt_teacher_midterm") {//期中
                $scope.scoreProperty = 2;
            } else {//期末
                $scope.scoreProperty = 3;
            }
            $scope.items = [];
            $scope.chooseTerm = function (term, func) {
                $scope.page = null;//重新获取page对象
                if (!term) {
                    term = $scope.curTerm;
                }
                $scope.selectedTerm = term;
                $scope.items = [];
                $scope.getData(0, func);
            };
            $scope.chooseAdminClass = function (item, func) {
                $scope.selectedAdminClass = item;
                $scope.chooseTerm(undefined, func);
            };
            //请求行政班&学期
            ($scope.getAdminClassAndTerm = function (func) {
                $scope.hideHead = false;
                $http.post(originBaseUrl+"/third/appAcvmt/getAdminClassAndTerm.htm?",{userId:$rootScope.authorizationStr.userId}).success(function (data) {
                    $scope.loadError = false;
                    var result = data.result;
                    $scope.curTerm = result.curTerm;
                    $scope.termList = result.termList;
                    $scope.adminClassList = result.adminClassList;
                    if (!angular.isArray($scope.adminClassList) || $scope.adminClassList.length < 1) {
                        if (func) {
                            func();
                        }
                        $scope.curTerm = null;
                        $scope.termList = null;
                        $scope.dataLoading = false;
                        $scope.loadError = true;
                        $scope.hideHead = true;
                        $scope.loadErrorMsg = '没有管理的班级，无法查看成绩！';
                        return false;
                    }
                    if(!$scope.curTerm){
                        if(!angular.isArray($scope.termList)||$scope.termList.length<1){
                            if (func) {
                                func();
                            }
                            $scope.dataLoading = false;
                            $scope.loadError = true;
                            $scope.hideHead = true;
                            $scope.loadErrorMsg = '您没有学期信息，无法查看成绩！';
                            return false;
                        }
                        $scope.curTerm = $scope.termList[0];
                    }
                    $scope.chooseAdminClass($scope.adminClassList[0], func);
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.dataLoading = false;
                    $scope.loadError = true;
                    $scope.hideHead = true;
                    $scope.loadErrorMsg = '加载失败，请重试！';
                });
            })();
            //模拟请求数据
            $scope.getData = function (pageNumber, func) {
                $scope.loadError = false;
                $http.post(originBaseUrl+"/third/appAcvmt/queryResultByTeacher.htm",
                    {
                        classId: $scope.selectedAdminClass.id,
                        termId: $scope.selectedTerm.id,
                        pageNumber: pageNumber,
                        scoreProperty: $scope.scoreProperty,
                        userId:$rootScope.authorizationStr.userId
                    }).success(function (data) {
                        $scope.loadError = false;
                        $scope.dataLoading = false;
                        if (data.result) {
                            $scope.page = data.result;
                            $scope.items = $scope.items.concat($scope.page.content);
                        }
                        if ($scope.items.length < 1) {
                            $scope.loadError = true;
                            $scope.loadErrorMsg = '您没有管理的班级或管理的班级没有课程，无法查看成绩！';
                        }
                        if (func) {
                            func();
                        }
                    }).error(function () {
                        $scope.dataLoading = false;
                        $scope.loadError = true;
                        $scope.loadErrorMsg = '加载失败，请重试！';
                        if (func) {
                            func();
                        }
                    });
            };
            //刷新数据
            $scope.reloadData = function () {
                if (!$scope.termList || !$scope.adminClassList) {
                    $scope.doRefresh();
                } else {
                    $scope.dataLoading = true;
                    $ionicLoading.show({
                        template: '正在刷新...'
                    });
                    if ($scope.selectedAdminClass && $scope.selectedTerm && $scope.scoreProperty) {
                        $scope.getData(0, function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                            $ionicScrollDelegate.scrollTop();
                            $ionicLoading.hide();
                        });
                    } else {
                        $scope.dataLoading = false;
                        $timeout(function () {
                            $scope.$broadcast('scroll.infiniteScrollComplete');
                            $ionicScrollDelegate.scrollTop();
                            $ionicLoading.hide();
                        }, 250);
                    }
                }
            };
            $scope.loadMore = function () {
                //请求数据
                $scope.getData($scope.page.number + 1, function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                });
            };

            $scope.$on('stateChangeSuccess', function () {
                $scope.loadMore();
            });

            $scope.moreDataCanBeLoaded = function () {
                return $scope.page && $scope.items.length < $scope.page.totalElements;
            };

            $scope.doRefresh = function () {
                $scope.dataLoading = true;
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.page = null;//重新获取page对象
                $scope.items = [];
                $scope.getAdminClassAndTerm(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.showWarning = function (msg) {
                $ionicLoading.show({
                    template: msg
                });
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 500);
            };
        }])
    //成绩-明细
    .controller('AchievementDetailCtrl', ['$location', '$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',"$rootScope",
        function ($location, $scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$rootScope) {
            $scope.isBack = true;
            $scope.status = 0;
            $scope.searchParam = $location.search();
            $scope.items = [];
            $scope.page = undefined;
            $scope.dataLoading = true;
            //模拟请求数据
            ($scope.getData = function (pageNumber, func,sort) {
                $scope.loadError = false;
                $http.post(originBaseUrl+"/third/appAcvmt/queryResultByCourse.htm", {
                    classId: $scope.searchParam.classId,
                    termId: $scope.searchParam.termId,
                    courseId: $scope.searchParam.courseId,
                    scoreProperty: $scope.searchParam.state,
                    teachType:$scope.searchParam.teachType,
                    executeTerm: $scope.searchParam.executeTerm,
                    pageNumber: pageNumber,
                    isPass: $scope.status==1?1:$scope.status==2?2:undefined,
                    name: $scope.name,
                    sort:sort,
                    userId:$rootScope.authorizationStr.userId
                }).success(function (data) {
                    $scope.loadError = false;
                    $scope.dataLoading = false;
                    $scope.items = $scope.items.concat(data.result.content);
                    $scope.page = data.result;
                    if($scope.searchBar){//如果处于搜索状态，则过滤数据
                        $scope.search();
                    }
                    if ($scope.items.length < 1) {
                        $scope.loadError = true;
                        $scope.loadErrorMsg = '没有可查看的成绩！';
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    $scope.dataLoading = false;
                    $scope.loadError = true;
                    $scope.loadErrorMsg = '加载失败，请重试！';
                    if (func) {
                        func();
                    }
                });
            })(0);

            $scope.sortData = function(sort){
                $scope.page = undefined;//重新获取page对象
                $scope.items = [];
                $scope.getData(0, null,"asc");
            }

            $scope.doRefresh = function () {
                $scope.dataLoading = true;
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.page = undefined;//重新获取page对象
                $scope.items = [];
                $scope.getData(0, function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.changeStatus = function (status) {
                $scope.items = [];
                $scope.page = undefined;
                $scope.status = status;
                $scope.getData(0, function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.loadMore = function () {
                //请求数据
                $scope.getData($scope.page.number + 1, function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                });
            };

            $scope.$on('stateChangeSuccess', function () {
                $scope.loadMore();
            });

            $scope.moreDataCanBeLoaded = function () {
                return $scope.page && $scope.items.length < $scope.page.totalElements;
            };

            //搜索
            $scope.searchBar = false;
            $scope.waitSearch = false;
            $scope.search = function () {
                if($scope.waitSearch){//已经有定时过滤则return
                    return;
                }else{//改变定时过滤，return以后的过滤
                    $scope.waitSearch = true;
                    $scope.items = [];
                    $scope.page = undefined;
                }
                $timeout(function () {
                    $scope.items = [];
                    $scope.getData(0, function () {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                        $scope.waitSearch = false;
                    });
                }, 500);
            };
            $scope.cancelBack = function () {
                $scope.searchBar = !$scope.searchBar;
                $scope.name = undefined;
                $scope.doRefresh();
            };

            $scope.doBack = function () {
                history.back(-1);
            };
            $scope.showWarning = function (msg) {
                $ionicLoading.show({
                    template: msg
                });
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 500);
            };
        }])
    //课堂考勤（学生访问）
    .controller('attendanceStuCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',"$location","$ionicPopup",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$location,$ionicPopup){
            //获取当前学期
            $http.get(originBaseUrl + "/third/result/getTerm.htm"+ "?_datatime=" + new Date().getTime()).success(function(data){
                if(data.status == 0){
                    if(data.result.curTerm){
                        $scope.isCurTerm = true;//用于后面获取教学周信息的判断
                        $scope.term = data.result.curTerm
                        $scope.termId = $scope.term.id;
                        //当前学期的开始时间和结束时间
                        $scope.beginTime = $scope.term.termBeginDateString;
                        $scope.endTime = $scope.term.termEndDateString;
                        $scope.loadData();
                    }else{//如果当前日期不在学期内，就默认为上学期
                        $scope.isCurTerm = false;
                    }
                }
            })
            // 选中的周数
            $scope.selectedWeek = "";
            /**
             * 查询总周数 以及每周的信息
             */
            /**
             * 查询总周数 以及每周的信息
             */
            var firstDayArr = [],endDayArr=[];
            $scope.loadData = function(){
                $http.post(originBaseUrl + "/third/attendance/queryTimeTableWeekTHead.htm",{termId:$scope.termId,requestSource:2,containChecking:false}).success(function(data){
                    if(data.status == 0){
                        //当前学期周相关信息
                        $scope.oTableWeekTHead = data.result;
                        if ($scope.oTableWeekTHead.length > 0) {
                            //获取教学周第一周和最后一周的起始时间
                            var firstDate = $scope.oTableWeekTHead[0].startEndDate.split("~")[0],
                                firstDateEnd = $scope.oTableWeekTHead[0].startEndDate.split("~")[1],
                                endDate =  $scope.oTableWeekTHead[$scope.oTableWeekTHead.length-1].startEndDate.split("~")[1];
                            firstDayArr = firstDate.split("-"), endDayArr = endDate.split("-");
                            angular.forEach($scope.oTableWeekTHead, function(o,key) {
                                if (o.isCurrentWeek) {
                                    $scope.selectedWeek = o.week;
                                    var tempArr = o.startEndDate.split("~"),
                                    //校历日期当前周的开始和结束时间
                                        startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                        startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                                    //判断校历日期一周有几天
                                    if(startDay>endDay){    //跨月
                                        if(key+1<=$scope.oTableWeekTHead.length){
                                            tempArr = $scope.oTableWeekTHead[key+1].startEndDate.split("~");
                                        }else{
                                            tempArr = $scope.oTableWeekTHead[key-1].startEndDate.split("~");
                                        }
                                        startDate1 =tempArr[0],endDate1  = tempArr[1];
                                        getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1);
                                    }else{
                                        getDateByStartEndDate(startDate,endDate);
                                    }
                                    getAssembleDate(false,false,new Date().getMonth()+1,new Date().getDate(),false,false);
                                }
                            });
                            if(!$scope.selectedWeek){    //当前日期不在教学周内，默认加载教学周第一周的第一天
                                var  startMonth =firstDate.split("-")[0],startDay = firstDate.split("-")[1];
                                getDateByStartEndDate(firstDate,firstDateEnd);
                                getAssembleDate(false,false,startMonth,startDay,false,false);
                                $scope.selectedWeek = 1;
                                $scope.oTableWeekTHead[0].isCurrentWeek = true;
                            }
                        }
                    }
                });
            }
            //查询课表信息
            $scope.fLoadTimeTableData = function() {
                $http.post(originBaseUrl + "/third/attendance/queryRollBackByquery.htm",
                    {termId:$scope.termId,week:$scope.selectedWeek,day:$scope.showDay,requestSource:2}
                ).success(function(data){
                        if (data.status === 0) {
                            $scope.oTimeTableData = data.result;
                        }else{
                            $scope.showWarning("加载失败，请重试！");
                        }
                    }).error(function(){
                        $scope.showWarning("加载失败，请重试！");
                    });
            }

            /**
             * 根据开始时间和结束时间计算每周的日期
             * @param start
             * @param end
             * @returns {Array}
             */

            function getDateByStartEndDate(start,end,isAdd,start1,end1){
                $scope.dateArr = [];
                var startMonthStr = parseInt(start.split("-")[0]),endMonthStr = parseInt(end.split("-")[0]),
                    startDateStr = parseInt(start.split("-")[1]),endDateStr = parseInt(end.split("-")[1]);
                var tempNum = 0;
                if(isAdd){ //跨月
                    $scope.weekNum = Math.abs(parseInt(end1.split("-")[1]-parseInt(start1.split("-")[1])))+1;
                }else{
                    $scope.weekNum = Math.abs(endDateStr-startDateStr)+1;
                }
                if(startMonthStr == endMonthStr){ //同月
                    for(var i =1 ; i<= $scope.weekNum; i++){
                        $scope.dateArr.push({id:i,monthStr:startMonthStr,dateStr:startDateStr+tempNum});
                        tempNum++ ;
                    }
                }else{ //跨月
                    var currentNum = $scope.weekNum - endDateStr ,  //当前月占几天
                        tempNumCur = 0;
                    for(var i =1  ; i<= currentNum; i++){ //当前月日期
                        $scope.dateArr.push({id:i,monthStr:startMonthStr,dateStr:startDateStr+tempNumCur});
                        tempNumCur ++ ;
                    }
                    for(var j= 1 ; j <= endDateStr ; j++){ //跨月占日期
                        $scope.dateArr.push({id:++tempNumCur,monthStr:endMonthStr,dateStr:j});
                    }
                }
                return $scope.dateArr;
            }

            /**
             * 获取显示的日期
             * @param month
             * @param date
             * @param up    上一天
             * @param down  下一天
             * @param first 上一天跨周
             * @param end   下一天跨周
             */
            function getAssembleDate(first,end,month,date,up,down){
                if(first){
                    $scope.judgePar = {month:$scope.dateArr[$scope.dateArr.length-1].monthStr,date:$scope.dateArr[$scope.dateArr.length-1].dateStr};
                    $scope.showDay = $scope.dateArr[$scope.dateArr.length-1].id;
                }else if(end){
                    $scope.judgePar = {month:$scope.dateArr[0].monthStr,date:$scope.dateArr[0].dateStr};
                    $scope.showDay = $scope.dateArr[0].id;
                }else{
                    for(var i = 0; i<$scope.dateArr.length;i++){
                        var item = $scope.dateArr[i];
                        if(item.monthStr ==month && item.dateStr == date ){
                            if(up){
                                $scope.judgePar = {month:$scope.dateArr[i-1].monthStr,date:$scope.dateArr[i-1].dateStr};
                                $scope.showDay = $scope.dateArr[i-1].id;
                            }else if(down){
                                $scope.judgePar = {month:$scope.dateArr[i+1].monthStr,date:$scope.dateArr[i+1].dateStr};
                                $scope.showDay = $scope.dateArr[i+1].id;
                            }else{
                                $scope.judgePar = {month:item.monthStr,date:item.dateStr};
                                $scope.showDay = item.id;
                            }
                        }
                    }
                }
                $scope.fLoadTimeTableData();
            }

            /**
             * 上一天、下一天
             * @param item
             */
            $scope.chooseDay = function(item){
                if(item == 1){
                    if($scope.judgePar.month ==parseInt(endDayArr[0]) && $scope.judgePar.date == parseInt(endDayArr[1])){
                        $scope.showWarning("已经是最后一天！");
                        return ;
                    }
                    //跨周处理
                    if($scope.showDay < $scope.weekNum){
                        getAssembleDate(false,false,$scope.judgePar.month,$scope.judgePar.date,false,true);
                    }else{
                        for(var i = 0 ; i<$scope.oTableWeekTHead.length;i++){
                            var val = $scope.oTableWeekTHead[i];
                            if (val.isCurrentWeek) {
                                val.isCurrentWeek = false;
                                $scope.oTableWeekTHead[i+1].isCurrentWeek = true;
                                $scope.selectedWeek = $scope.oTableWeekTHead[i+1].week;
                                var tempArr = $scope.oTableWeekTHead[i+1].startEndDate.split("~"),
                                    startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                    startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                                if(startDay>endDay){    //跨月
                                    if(i-2<=$scope.oTableWeekTHead.length && i-2>=0 ){
                                        tempArr = $scope.oTableWeekTHead[i].startEndDate.split("~");
                                    }else{
                                        tempArr = $scope.oTableWeekTHead[i-2].startEndDate.split("~");
                                    }
                                    startDate1 =tempArr[0],endDate1  = tempArr[1];
                                    getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1)
                                }else{
                                    getDateByStartEndDate(startDate,endDate);
                                }
                                getAssembleDate(false,true);
                                return ;
                            }
                        }
                    }

                }
                else{
                    if($scope.judgePar.month ==parseInt(firstDayArr[0]) && $scope.judgePar.date == parseInt(firstDayArr[1])){
                        $scope.showWarning("已经是第一天！");
                        return ;
                    }
                    if($scope.showDay > 1){
                        getAssembleDate(false,false,$scope.judgePar.month,$scope.judgePar.date,true,false);
                    }else{
                        for(var i = 0 ; i<$scope.oTableWeekTHead.length;i++){
                            var val = $scope.oTableWeekTHead[i];
                            if (val.isCurrentWeek) {
                                val.isCurrentWeek = false;
                                $scope.oTableWeekTHead[i-1].isCurrentWeek = true;
                                $scope.selectedWeek = $scope.oTableWeekTHead[i-1].week;
                                var tempArr = $scope.oTableWeekTHead[i-1].startEndDate.split("~"),
                                    startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                    startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                                if(startDay>endDay){    //跨月
                                    if(i-2<=$scope.oTableWeekTHead.length && i-2>=0 ){
                                        tempArr = $scope.oTableWeekTHead[i].startEndDate.split("~");
                                    }else{
                                        tempArr = $scope.oTableWeekTHead[i-2].startEndDate.split("~");
                                    }
                                    startDate1 =tempArr[0],endDate1  = tempArr[1];
                                    getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1)
                                }else{
                                    getDateByStartEndDate(startDate,endDate);
                                }
                                getAssembleDate(true,false);
                                return ;
                            }
                        }
                    }

                }
            }
            /**
             * 选择周
             * @param item 选中的周数
             */
            $scope.chooseWeek = function(item){
                var count = true;
                if(item<=$scope.oTableWeekTHead.length && item>0){
                    for(var i = 0 ; i<$scope.oTableWeekTHead.length;i++){
                        if($scope.oTableWeekTHead[i].week == item){
                            var val = $scope.oTableWeekTHead[i];
                            val.isCurrentWeek = true;
                            $scope.selectedWeek = val.week;
                            var tempArr = val.startEndDate.split("~"),
                                startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                            if(startDay>endDay){    //跨月
                                if(i+1<=$scope.oTableWeekTHead.length){
                                    tempArr = $scope.oTableWeekTHead[i+1].startEndDate.split("~");
                                }else{
                                    tempArr = $scope.oTableWeekTHead[i-1].startEndDate.split("~");
                                }
                                startDate1 =tempArr[0],endDate1  = tempArr[1];
                                getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1)
                            }else{
                                getDateByStartEndDate(startDate,endDate);
                            }
                            if($scope.showDay <= $scope.dateArr[$scope.dateArr.length-1].id){ //避免每周的天数不一致
                                angular.forEach($scope.dateArr,function(item){
                                    if(item.id ==$scope.showDay && count){
                                        $scope.judgePar = {month:item.monthStr,date:item.dateStr};
                                        $scope.showDate = item.monthStr+"."+item.dateStr;
                                        $scope.showDay = item.id;
                                        count = false;
                                    }
                                })
                                $scope.fLoadTimeTableData();
                            }else{
                                getAssembleDate(true,false);
                            }
                        }else{
                            $scope.oTableWeekTHead[i].isCurrentWeek = false;
                        }
                    }
                }
            }

            /**
             * 选择大周（大周1相当于星期1）
             */
            $scope.chooseBigWeek = function(obj){
                angular.forEach($scope.dateArr,function(item){
                    if(item.id ==obj){
                        $scope.judgePar = {month:item.monthStr,date:item.dateStr};
                        $scope.showDate = item.monthStr+"."+item.dateStr;
                        $scope.showDay = item.id;
                    }
                });
                angular.element("#headDrop1").removeClass('open');
                $scope.fLoadTimeTableData();
            };

            /**
             * 刷新
             */
            $scope.doRefresh = function () {
                $scope.showWarning("正在加载...");
                $scope.fLoadTimeTableData();
            };
            /**
             * 提示信息
             * @param msg 提示内容
             */
            $scope.showWarning = function (msg) {
                $ionicLoading.show({
                    template: msg
                });
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 500);
            };
        }])
    //课堂考勤（教职工访问）
    .controller('ClassAttendanceCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',"$location","$ionicPopup",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$location,$ionicPopup){
            //获取当前学期
            $http.get(originBaseUrl + "/third/result/getTerm.htm"+ "?_datatime=" + new Date().getTime()).success(function(data){
                if(data.status == 0){
                    if(data.result.curTerm){
                        $scope.isCurTerm = true;//用于后面获取教学周信息的判断
                        $scope.term = data.result.curTerm;
                        $scope.termId = $scope.term.id;
                        //当前学期的开始时间和结束时间
                        $scope.beginTime = $scope.term.termBeginDateString;
                        $scope.endTime = $scope.term.termEndDateString;
                        $scope.loadData();
                    }else{//如果当前日期不在学期内，就默认为上学期
                        $scope.isCurTerm = false;
                    }
                }
            });
            // 选中的周数
            $scope.selectedWeek = "";

            /**
             * 查询总周数 以及每周的信息
             */
            var firstDayArr = [],endDayArr=[];
            $scope.loadData = function(){
                $http.post(originBaseUrl + "/third/attendance/queryTimeTableWeekTHead.htm",{termId:$scope.termId,requestSource:1,containChecking:false}).success(function(data){
                    if(data.status == 0){
                        //当前学期周相关信息
                        $scope.oTableWeekTHead = data.result;
                        if ($scope.oTableWeekTHead.length > 0) {
                            //获取教学周第一周和最后一周的起始时间
                            var firstDate = $scope.oTableWeekTHead[0].startEndDate.split("~")[0],
                                firstDateEnd = $scope.oTableWeekTHead[0].startEndDate.split("~")[1],
                                endDate =  $scope.oTableWeekTHead[$scope.oTableWeekTHead.length-1].startEndDate.split("~")[1];
                                firstDayArr = firstDate.split("-"); endDayArr = endDate.split("-");
                            angular.forEach($scope.oTableWeekTHead, function(o,key) {
                                if (o.isCurrentWeek) {
                                    $scope.selectedWeek = o.week;
                                    var tempArr = o.startEndDate.split("~"),
                                    //校历日期当前周的开始和结束时间
                                        startDate = tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                        startDay = startDate.split("-")[1],endDay  = endDate.split("-")[1];
                                    //判断校历日期一周有几天
                                    if(startDay>endDay){    //跨月
                                        if(key+1<=$scope.oTableWeekTHead.length){
                                            tempArr = $scope.oTableWeekTHead[key+1].startEndDate.split("~");
                                        }else{
                                            tempArr = $scope.oTableWeekTHead[key-1].startEndDate.split("~");
                                        }
                                        startDate1 =tempArr[0];endDate1  = tempArr[1];
                                        getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1);
                                    }else{
                                        getDateByStartEndDate(startDate,endDate);
                                    }
                                    getAssembleDate(false,false,new Date().getMonth()+1,new Date().getDate(),false,false);
                                }
                            });
                            if(!$scope.selectedWeek){    //当前日期不在教学周内，默认加载教学周第一周的第一天
                                var  startMonth =firstDate.split("-")[0],startDay = firstDate.split("-")[1];
                                getDateByStartEndDate(firstDate,firstDateEnd);
                                getAssembleDate(false,false,startMonth,startDay,false,false);
                                $scope.selectedWeek = 1;
                                $scope.oTableWeekTHead[0].isCurrentWeek = true;
                            }
                        }
                    }
                });
            };

            //查询课表信息
            $scope.fLoadTimeTableData = function() {
                $http.post(originBaseUrl + "/third/attendance/queryRollBackByquery.htm",
                    {termId:$scope.termId,week:$scope.selectedWeek,day:$scope.showDay,requestSource:1}
                ).success(function(data){
                        if (data.status === 0) {
                            $scope.oTimeTableData = data.result;
                        }else{
                            $scope.showWarning("加载失败，请重试！");
                        }
                    }).error(function(){
                        $scope.showWarning("加载失败，请重试！");
                    });
            };

            /**
             * 根据开始时间和结束时间计算每周的日期
             * @param start
             * @param end
             * @returns {Array}
             */

            function getDateByStartEndDate(start,end,isAdd,start1,end1){
                $scope.dateArr = [];
                var startMonthStr = parseInt(start.split("-")[0]),endMonthStr = parseInt(end.split("-")[0]),
                    startDateStr = parseInt(start.split("-")[1]),endDateStr = parseInt(end.split("-")[1]);
                var tempNum = 0;
                if(isAdd){ //跨月
                    $scope.weekNum = Math.abs(parseInt(end1.split("-")[1]-parseInt(start1.split("-")[1])))+1;
                }else{
                    $scope.weekNum = Math.abs(endDateStr-startDateStr)+1;
                }
                if(startMonthStr == endMonthStr){ //同月
                    for(var i =1 ; i<= $scope.weekNum; i++){
                        $scope.dateArr.push({id:i,monthStr:startMonthStr,dateStr:startDateStr+tempNum});
                        tempNum++ ;
                    }
                }else{ //跨月
                    var currentNum = $scope.weekNum - endDateStr ,  //当前月占几天
                        tempNumCur = 0;
                    for(var i =1  ; i<= currentNum; i++){ //当前月日期
                        $scope.dateArr.push({id:i,monthStr:startMonthStr,dateStr:startDateStr+tempNumCur});
                        tempNumCur ++ ;
                    }
                    for(var j= 1 ; j <= endDateStr ; j++){ //跨月占日期
                        $scope.dateArr.push({id:++tempNumCur,monthStr:endMonthStr,dateStr:j});
                    }
                }
                return $scope.dateArr;
            }

            /**
             * 获取显示的日期
             * @param month
             * @param date
             * @param up    上一天
             * @param down  下一天
             * @param first 上一天跨周
             * @param end   下一天跨周
             */
            function getAssembleDate(first,end,month,date,up,down){
                if(first){
                    $scope.judgePar = {month:$scope.dateArr[$scope.dateArr.length-1].monthStr,date:$scope.dateArr[$scope.dateArr.length-1].dateStr};
                    $scope.showDay = $scope.dateArr[$scope.dateArr.length-1].id;
                }else if(end){
                    $scope.judgePar = {month:$scope.dateArr[0].monthStr,date:$scope.dateArr[0].dateStr};
                    $scope.showDay = $scope.dateArr[0].id;
                }else{
                    for(var i = 0; i<$scope.dateArr.length;i++){
                        var item = $scope.dateArr[i];
                        if(item.monthStr ==month && item.dateStr == date ){
                            if(up){
                                $scope.judgePar = {month:$scope.dateArr[i-1].monthStr,date:$scope.dateArr[i-1].dateStr};
                                $scope.showDay = $scope.dateArr[i-1].id;
                            }else if(down){
                                $scope.judgePar = {month:$scope.dateArr[i+1].monthStr,date:$scope.dateArr[i+1].dateStr};
                                $scope.showDay = $scope.dateArr[i+1].id;
                            }else{
                                $scope.judgePar = {month:item.monthStr,date:item.dateStr};
                                $scope.showDay = item.id;
                            }
                        }
                    }
                }
                $scope.fLoadTimeTableData();
            }

            /**
             * 上一天、下一天
             * @param item
             */
            $scope.chooseDay = function(item){
                if(item == 1){
                    if($scope.judgePar.month ==parseInt(endDayArr[0]) && $scope.judgePar.date == parseInt(endDayArr[1])){
                        $scope.showWarning("已经是最后一天！");
                        return ;
                    }
                    //跨周处理
                    if($scope.showDay < $scope.weekNum){
                        getAssembleDate(false,false,$scope.judgePar.month,$scope.judgePar.date,false,true);
                    }else{
                        for(var i = 0 ; i<$scope.oTableWeekTHead.length;i++){
                            var val = $scope.oTableWeekTHead[i];
                            if (val.isCurrentWeek) {
                                val.isCurrentWeek = false;
                                $scope.oTableWeekTHead[i+1].isCurrentWeek = true;
                                $scope.selectedWeek = $scope.oTableWeekTHead[i+1].week;
                                var tempArr = $scope.oTableWeekTHead[i+1].startEndDate.split("~"),
                                    startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                    startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                                if(startDay>endDay){    //跨月
                                    if(i-2<=$scope.oTableWeekTHead.length && i-2>=0 ){
                                        tempArr = $scope.oTableWeekTHead[i].startEndDate.split("~");
                                    }else{
                                        tempArr = $scope.oTableWeekTHead[i-2].startEndDate.split("~");
                                    }
                                    startDate1 =tempArr[0],endDate1  = tempArr[1];
                                    getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1)
                                }else{
                                    getDateByStartEndDate(startDate,endDate);
                                }
                                getAssembleDate(false,true);
                                return ;
                            }
                        }
                    }

                }
                else{
                    if($scope.judgePar.month ==parseInt(firstDayArr[0]) && $scope.judgePar.date == parseInt(firstDayArr[1])){
                        $scope.showWarning("已经是第一天！");
                        return ;
                    }
                    if($scope.showDay > 1){
                        getAssembleDate(false,false,$scope.judgePar.month,$scope.judgePar.date,true,false);
                    }else{
                        for(var i = 0 ; i<$scope.oTableWeekTHead.length;i++){
                            var val = $scope.oTableWeekTHead[i];
                            if (val.isCurrentWeek) {
                                val.isCurrentWeek = false;
                                $scope.oTableWeekTHead[i-1].isCurrentWeek = true;
                                $scope.selectedWeek = $scope.oTableWeekTHead[i-1].week;
                                var tempArr = $scope.oTableWeekTHead[i-1].startEndDate.split("~"),
                                    startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                    startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                                if(startDay>endDay){    //跨月
                                    if(i-2<=$scope.oTableWeekTHead.length && i-2>=0 ){
                                        tempArr = $scope.oTableWeekTHead[i].startEndDate.split("~");
                                    }else{
                                        tempArr = $scope.oTableWeekTHead[i-2].startEndDate.split("~");
                                    }
                                    startDate1 =tempArr[0],endDate1  = tempArr[1];
                                    getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1)
                                }else{
                                    getDateByStartEndDate(startDate,endDate);
                                }
                                getAssembleDate(true,false);
                                return ;
                            }
                        }
                    }

                }
            };

            /**
             * 选择周
             * @param item 选中的周数
             */
            $scope.chooseWeek = function(item){
                var count = true;
                if(item<=$scope.oTableWeekTHead.length && item>0){
                    for(var i = 0 ; i<$scope.oTableWeekTHead.length;i++){
                        if($scope.oTableWeekTHead[i].week == item){
                            var val = $scope.oTableWeekTHead[i];
                            val.isCurrentWeek = true;
                            $scope.selectedWeek = val.week;
                            var tempArr = val.startEndDate.split("~"),
                                startDate =tempArr[0],endDate  = tempArr[1],startDate1,endDate1,
                                startDay =startDate.split("-")[1],endDay  = endDate.split("-")[1];
                            if(startDay>endDay){    //跨月
                                if(i+1<=$scope.oTableWeekTHead.length){
                                    tempArr = $scope.oTableWeekTHead[i+1].startEndDate.split("~");
                                }else{
                                    tempArr = $scope.oTableWeekTHead[i-1].startEndDate.split("~");
                                }
                                startDate1 =tempArr[0],endDate1  = tempArr[1];
                                getDateByStartEndDate(startDate,endDate,true,startDate1,endDate1)
                            }else{
                                getDateByStartEndDate(startDate,endDate);
                            }
                            if($scope.showDay <= $scope.dateArr[$scope.dateArr.length-1].id){ //避免每周的天数不一致
                                angular.forEach($scope.dateArr,function(item){
                                    if(item.id ==$scope.showDay && count){
                                        $scope.judgePar = {month:item.monthStr,date:item.dateStr};
                                        $scope.showDate = item.monthStr+"."+item.dateStr;
                                        $scope.showDay = item.id;
                                        count = false;
                                    }
                                });
                                $scope.fLoadTimeTableData();
                            }else{
                                getAssembleDate(true,false);
                            }
                        }else{
                            $scope.oTableWeekTHead[i].isCurrentWeek = false;
                        }
                    }
                }
            };

            /**
             * 选择大周（大周1相当于星期1）
             */
            $scope.chooseBigWeek = function(obj){
                angular.forEach($scope.dateArr,function(item){
                    if(item.id ==obj){
                        $scope.judgePar = {month:item.monthStr,date:item.dateStr};
                        $scope.showDate = item.monthStr+"."+item.dateStr;
                        $scope.showDay = item.id;
                    }
                });
                angular.element("#headDrop1").removeClass('open');
                $scope.fLoadTimeTableData();
            };

            /**
             * 点名
             */
            $scope.callRoll = function(item){
                $http.post(originBaseUrl + "/third/attendance/saveKQKtRollCall.htm",
                    {resultsFormalId:item.kpKBid}).success(function(data){
                        if(data.status == 0){
                            $scope.callBackId = data.result;
                            item.isOk = "1";
                            item.id = $scope.callBackId;
                            $location.path("/attendance_people").search("id",$scope.callBackId);
                        }else{
                            $scope.showWarning("加载失败，请重试！");
                        }
                    }).error(function(){
                        $scope.showWarning("加载失败，请重试！");
                    });
            };

            /**
             * 提醒
             */
            $scope.remind = function(item){
                var opts = {
                    cancelText:"取消",
                    okText:"确定",
                    title:"发送异常信息",
                    template:"确定要将“"+item.courseName+"（"+item.timeStatus+item.sectionName+"）”下，所有考勤异常的学生名单发送给班主任？",
                    buttons: [{
                        text: '取消',
                        type: 'button-default',
                        onTap: function() {
                            return false;
                        }
                    }, {
                        text: '确定',
                        type: 'button-positive',
                        onTap: function() {
                            $http.post(originBaseUrl + "/third/attendance/sendUnusualcMessage.htm",{
                                KQKTRollCallId:item.id
                            }).success(function(data){
                                if(data.status == 0){
                                    $scope.showWarning("发送成功！");
                                }  else{
                                    $scope.showWarning(data.message);
                                }
                            }).error(function(){
                                $scope.showWarning("加载失败，请重试！");
                            });
                            return true;
                        }
                    }]
                };
                $ionicPopup.confirm(opts);
            };
            /**
             * 刷新
             */
            $scope.doRefresh = function () {
                $scope.showWarning("正在加载...")
                $scope.fLoadTimeTableData();
            };
            /**
             * 提示信息
             * @param msg 提示内容
             */
            $scope.showWarning = function (msg) {
                $ionicLoading.show({
                    template: msg
                });
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 1000);
            };
        }])
    //选人点名
    .controller('AttendancePeopleCtrl',['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate',"$ionicPopup","$location",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$ionicPopup,$location){
            $scope.searchBar = false;
            $scope.showStatus = false;
            $scope.isLoad = true;
            $scope.callBackId = $location.search().id;
            $scope.clearName = function(){
                $scope.name = "";
                $scope.queryKQKtUserInfo($scope.callBackId,true);
            };
            $scope.changeShow = function(){
                $scope.searchBar = !$scope.searchBar;
                if(!$scope.searchBar){
                    $scope.name = "";
                    $scope.queryKQKtUserInfo($scope.callBackId,true);
                }
            };


            /**
             * 获取学生信息
             */
            $scope.queryKQKtUserInfo = function(callBackId,isLoad){
                $http.post(originBaseUrl + "/third/attendance/queryKQKtUserInfo.htm",
                    {callBackId:callBackId,queryStr:$scope.searchBar?$scope.name:''}).success(function(data){
                        if(data.status == 0){
                            $scope.isLoad = isLoad;
                            $scope.showHead = data.result;
                            $scope.userInfoData = data.result.list;
                            if($scope.userInfoData.length>0){
                                angular.forEach($scope.userInfoData,function(val){
                                    var aliensNumber;
                                    if(val.aliens && val.number){
                                        aliensNumber="("+val.aliens+","+val.number+")";
                                    }else if(val.aliens && !val.number){
                                        aliensNumber="("+val.aliens+")";
                                    }else if(!val.aliens && val.number){
                                        aliensNumber="("+val.number+")";
                                    }
                                    val.aliensNumbers = aliensNumber;
                                    val.showStatus = false;
                                    //处理头像
                                    if(val.imageId){
                                        var tempImgArr = val.imageId.split(".");
                                        tempImgArr[0]+="_2";
                                        val.imageId=tempImgArr[0]+"."+tempImgArr[1];
                                    }
                                })
                            }
                            //$scope.queryDictionaryByKeyAndStatus();
                        }else{
                            $scope.showWarning("加载失败！"+data.message);
                        }
                    }).error(function(){
                        $scope.showWarning("加载失败，请重试！");
                    });
            };




            $scope.changeShowState = function(item){
                angular.forEach($scope.userInfoData,function(val){
                    val.showStatus = false;
                });
                item.showStatus = true;
            };


            //查询所有考勤状态
            $scope.queryDictionaryByKeyAndStatus = function(){
                $http.post(originBaseUrl + "/third/attendance/queryDictionaryByKeyAndStatus.htm").success(function(data){
                    if(data.status == 0){
                        $scope.ktStatusList = data.result;
                        $scope.queryKQKtUserInfo($scope.callBackId,true);
                    }else{
                        $scope.showWarning("加载失败！"+data.message);
                    }
                }).error(function(){
                    $scope.showWarning("加载失败，请重试！");
                });
            };

            /**
             * 提交考勤状态
             * @param item
             */
            $scope.commitStatus = function(item,dictCode){
                $http.post(originBaseUrl + '/third/attendance/updateKQStatus.htm',{
                    kqKTInfoId:item.id,dictCode:dictCode
                }).success(function (data) {
                    if (data.status == 0) {
                        $scope.queryKQKtUserInfo($scope.callBackId,true);
                    }
                })
            };

            /**
             * 刷新
             */
            $scope.doRefresh = function () {
                $scope.showWarning("正在加载...");
                $scope.searchBar = false;
                $scope.name = "";
                $scope.queryKQKtUserInfo($scope.callBackId,true);
            };
            /**
             * 提示信息
             * @param msg 提示内容
             */
            $scope.showWarning = function (msg) {
                $ionicLoading.show({
                    template: msg
                });
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 500);
            };

            $scope.back = function(){
                history.back();
            };



            $scope.queryDictionaryByKeyAndStatus();
        }])
    .controller('leaveViewCtrl', ['$location', '$scope', '$http','ynuiNotification','$ionicLoading','$ionicScrollDelegate',"$timeout", function ($location, $scope, $http,ynuiNotification,$ionicLoading,$ionicScrollDelegate,$timeout) {
        $scope.loadingError = false;
        $scope.loadingErrorInfo = "";
        //请假信息查询
        $scope.queryLeaveInfo = function () {
            $http.get(originBaseUrl + "/third/leaveAndRelease/queryLeaveInfoOfStu.htm?time=" + (new Date()).getTime()).success(function (data) {
                if (data.status == 0) {
                    $scope.leaveInfo = data.result;
                    $scope.assembleData();
                    $scope.loadingError = false;
                }
            }).error(function () {
                $scope.loadingError = true;
                $scope.loadingErrorInfo = '加载失败，请重试！';
            });
        };
        $scope.queryLeaveInfo();
        $scope.queryLeaveInfoRefresh = function(){
            $scope.queryLeaveInfo();
            $ionicLoading.show({
                template: '正在刷新...'
            });
            $timeout(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            }, 500);
        };
        //数据编辑
        $scope.assembleData = function(){
            $http.get(originBaseUrl + "/third/leaveAndRelease/leaveType.htm?time=" + (new Date()).getTime()).success(function (data) {
                if (data.status == 0) {
                    $scope.leaveTypeList = data.result;
                    angular.forEach($scope.leaveInfo, function (item) {
                        item.showStartTime=item.leaveStartTime.substring(5);
                        item.showEndTime=item.leaveEndTime.substring(5);
                        //返回数据中的dictcode是码值，在这里leavetype是类型名称
                        angular.forEach($scope.leaveTypeList, function (it) {
                            if (it.dictName == item.dictCode) {
                                item.leaveTypeCode = it.dictCode;
                            }
                        });
                        switch(item.status){
                            case 0:
                                item.checkContent = "未提交";
                                break;
                            case 1:
                                item.checkContent = "通过";
                                break;
                            case 2:
                                item.checkContent = "不通过";
                                break;
                            case 3:
                                item.checkContent = "审核中";
                                break;
                            case 4:
                                item.checkContent = "返回修改";
                                break;
                        }
                    })
                }
            });
        };

        //获取请假类型
        $scope.getLeaveType = function (dictCode) {
            $scope.leaveTypeCode="";
            $http.get(originBaseUrl + "/third/leaveAndRelease/leaveType.htm?time=" + (new Date()).getTime()).success(function (data) {
                if (data.status == 0) {
                    $scope.leaveTypeList = data.result;
                    angular.forEach($scope.leaveTypeList, function (item) {
                        if (item.dictName == dictCode) {
                            $scope.leaveTypeCode = item.dictCode;
                        }
                    });
                    return $scope.leaveTypeCode;
                }
            });
        };
        //创建请假条
        $scope.createLeave = function () {
            $location.path("/leave_createOrModify");
        };
        //修改请假条
        $scope.showModifyLeave = function (no) {
            var i=1;
            $scope.leaveInfo[no].moreType=false;
            angular.forEach($scope.leaveTypeList, function (item) {
                item.selected = false;
                if ($scope.leaveInfo[no].leaveTypeCode == item.dictCode) {
                    item.selected = true;
                    if (i > 5) {
                        $scope.leaveInfo[no].moreType = true;
                    }
                }
                i++;
            });
            $scope.leaveInfo[no].leaveTypeList = $scope.leaveTypeList;
            $location.path("/leave_createOrModify").search({"isModify":true,"item":JSON.stringify($scope.leaveInfo[no])});
        };

        //删除请假条
        $scope.del = function (LeaveId) {
            $http.post(originBaseUrl + "/third/leaveAndRelease/deleteLeaveInfoByStu.htm?LeaveId=" + LeaveId).success(function (data) {
                if (data.status == 0) {
                    $scope.queryLeaveInfo();
                    ynuiNotification.success({msg:'删除成功'});
                }
            });
        };

    }])
    //请假条添加或修改
    .controller('leaveModifyCtrl', ['$location', '$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate', 'ynuiNotification','$ionicPopup',
        function ($location, $scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate, ynuiNotification,$ionicPopup) {
            //参数初始化
            $scope.wordNumber = 0;
            var currentTime = new Date();
            var month = (currentTime.getMonth() + 1) > 9 ? (currentTime.getMonth() + 1) : '0' + (currentTime.getMonth() + 1);
            var day = currentTime.getDate() > 9 ? currentTime.getDate() : '0' + currentTime.getDate();
            var hour = currentTime.getHours() > 9 ? currentTime.getHours() : '0' + currentTime.getHours();
            var minute = currentTime.getMinutes() > 9 ? currentTime.getMinutes() : '0' + currentTime.getMinutes();
            $scope.backTime = currentTime.getFullYear() + "-" + month + "-" + day + " " + hour + ":" +
                minute;

            var addOneDay = new Date(currentTime.getTime() + 24 * 60 * 60 * 1000);
            var month1 = (addOneDay.getMonth() + 1) > 9 ? (addOneDay.getMonth() + 1) : '0' + (addOneDay.getMonth() + 1);
            var day1 = addOneDay.getDate() > 9 ? addOneDay.getDate() : '0' + addOneDay.getDate();
            var hour1 = addOneDay.getHours() > 9 ? addOneDay.getHours() : '0' + addOneDay.getHours();
            var minute1 = addOneDay.getMinutes() > 9 ? addOneDay.getMinutes() : '0' + addOneDay.getMinutes();
            $scope.endTime = addOneDay.getFullYear() + "-" + month1 + "-" + day1 + " " + hour1 + ":" +
                minute1;
            $scope.leaveParam = {
                leaveReason: "",
                isOutSchoolFlag: false,
                leaveStartTime: $scope.backTime,
                leaveEndTime: $scope.endTime,
                passStartTime: $scope.backTime,
                passEndTime: $scope.endTime,
                stuPlatformSysUserId: "",
                createPlatformSysUserId: "",
                leaveDays: "1",
                isBackSleep:false
            };
            if($location.search().isModify){//修改时
                $scope.leaveInfoParam = JSON.parse($location.search().item);
            }
            //更多请假类型
            $scope.moreLeaveType = function () {
                $scope.moreType = !$scope.moreType;
            };
            //请假类型赋值
            $scope.selectLeaveType = function (code,no,num) {
                $scope.leaveParam.dictCode = code;
                var index=parseInt(no)+parseInt(num);
                angular.forEach($scope.fourLeaveType,function(item){
                    item.selected=false;
                });
                if(index<5){
                    angular.forEach($scope.lastLeaveType,function(item){
                        item.selected=false;
                    });
                    $scope.fourLeaveType[no].selected=true;
                }else{
                    angular.forEach($scope.lastLeaveType,function(item){
                        item.selected=false;
                    });
                    $scope.lastLeaveType[no].selected=true;
                }
            };
            //请假类型
            $scope.fourLeaveType = [];
            $scope.lastLeaveType = [];
            $scope.$watch(function(){return $scope.fourLeaveType.length+$scope.lastLeaveType.length;},function(){
               if($scope.fourLeaveType.length+$scope.lastLeaveType.length<=5){
                   $scope.moreType = false;
               }else{
                   $scope.moreType = true;
               }
            });
            //请假类型 -----后期还需修改
            var getLeaveType = function(){
                $http.get(originBaseUrl + "/third/leaveAndRelease/leaveType.htm?time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        var typeList = data.result;
                        if($scope.leaveInfoParam){
                            $scope.moreType = $scope.leaveInfoParam.moreType;
                            typeList = $scope.leaveInfoParam.leaveTypeList;
                            $scope.leaveParam.dictCode = $scope.leaveInfoParam.leaveTypeCode;
                        }else{
                            $scope.leaveParam.dictCode = typeList[0].dictCode;
                            typeList[0].selected = true;
                        }
                        if(typeList.length>=5){
                            $scope.moreType = true;
                            $scope.fourLeaveType = typeList.splice(0, 5);
                            $scope.lastLeaveType = typeList;
                        }else{
                            $scope.fourLeaveType = typeList;
                        }
                    }
                });
            };
            getLeaveType();
            //修改的参数赋值
            if ($scope.leaveInfoParam) {
                $scope.wordNumber = $scope.leaveInfoParam.leaveReason.length;
                $scope.leaveParam = {
                    id:$scope.leaveInfoParam.id,
                    leaveReason: $scope.leaveInfoParam.leaveReason,
                    isOutSchoolFlag: !$scope.leaveInfoParam.isOutSchoolFlag,
                    leaveStartTime: $scope.leaveInfoParam.leaveStartTime,
                    leaveEndTime: $scope.leaveInfoParam.leaveEndTime,
                    passStartTime: $scope.leaveInfoParam.passStartTime,
                    passEndTime: $scope.leaveInfoParam.passEndTime,
                    stuPlatformSysUserId: "",
                    createPlatformSysUserId: "",
                    leaveDays: $scope.leaveInfoParam.leaveDays,
                    isBackSleep:$scope.leaveInfoParam.isBackSleep
                };
            }

            //请假原因
            $scope.leaveReasonList = [];
            var getLeaveReason = function(){
                $http.get(originBaseUrl + "/third/leaveAndRelease/leaveReason.htm?time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.leaveReasonList = data.result;
                    }
                });
            };
            getLeaveReason();
            //选择请假模板
            $scope.chooseTem = function (dictName) {
                $scope.leaveParam.leaveReason = dictName;
            };

            //时间字符串转换为日期类型（因为火狐兼容性问题从new date()方法改为这个）
            $scope.getNowFormatDate = function (s) {
                var d = new Date();
                d.setYear(parseInt(s.substring(0, 4), 10));
                d.setMonth(parseInt(s.substring(5, 7) - 1, 10));
                d.setDate(parseInt(s.substring(8, 10), 10));
                d.setHours(parseInt(s.substring(11, 13), 10));
                d.setMinutes(parseInt(s.substring(14, 16), 10));
                return d;
            };

            //出校时间随着请假时间变化
            $scope.changeTimeLeave = function (no) {
                //1.请假开始时间
                if (no == 1) {
                    $scope.leaveParam.passStartTime = $scope.leaveParam.leaveStartTime;
                    var addDay = $scope.getNowFormatDate($scope.leaveParam.passStartTime);
                    var changeTimeType = new Date(addDay.getTime() + 24 * 60 * 60 * 1000);
                    var month2 = (changeTimeType.getMonth() + 1) > 9 ? (changeTimeType.getMonth() + 1) : '0' + (changeTimeType.getMonth() + 1);
                    var day2 = changeTimeType.getDate() > 9 ? changeTimeType.getDate() : '0' + changeTimeType.getDate();
                    var hour2 = changeTimeType.getHours() > 9 ? changeTimeType.getHours() : '0' + changeTimeType.getHours();
                    var minute2 = changeTimeType.getMinutes() > 9 ? changeTimeType.getMinutes() : '0' + changeTimeType.getMinutes();

                    addDay = changeTimeType.getFullYear() + "-" + month2 + "-" + day2 + " " + hour2 + ":" +
                        minute2;
                    $scope.leaveParam.leaveEndTime = addDay;
                    $scope.leaveParam.passEndTime = addDay;
                    if (!$scope.leaveParam.leaveStartTime) {
                        $scope.leaveParam.leaveEndTime = "";
                        $scope.leaveParam.passStartTime = "";
                        $scope.leaveParam.passEndTime = "";
                    }
                }
                //2.请假开始结束
                if (no == 2) {
                    if (!$scope.leaveParam.leaveEndTime) {
                        $scope.leaveParam.passEndTime = "";
                    } else {
                        $scope.leaveParam.passEndTime = $scope.leaveParam.leaveEndTime;
                    }
                }
            };
            $scope.initLeaveday=function(){
                if(!$scope.leaveParam.leaveDays)
                $scope.leaveParam.leaveDays="0";
            };
            //申请出校时间
            $scope.changeTimePass = function () {
                if (!$scope.leaveParam.passEndTime) {
                    $scope.leaveParam.passEndTime = "";
                } else {
                    $scope.leaveParam.passEndTime = $scope.leaveParam.passStartTime;
                }
            };
            //校验天数
            $scope.tureDay = true;
            $scope.verifyLeaveDay=function(){
                //var days = $scope.leaveParam.leaveDays;
                //if (days) {
                //    if((days+"").indexOf(".")>0) {
                //        var point = (days + "").length - (days + "").indexOf(".") - 1;
                //    }
                //    if (point == 0 || point >1 || isNaN(days) || days <= 0) {
                //        $scope.tureDay = false;
                //        $scope.leaveParam.leaveDays = "";
                //        ynuiNotification.warning({msg: '只能输入0-999之间的数值！（含小数）'});
                //    } else {
                //        $scope.tureDay = true;
                //    }
                //} else {
                //    $scope.tureDay = false;
                //}

                var a  = $scope.leaveParam.leaveDays;
                var n = a.indexOf(".")+1;//12.34
                if(((n>0 && a.length-n>=2) || (n==5&& a.length==5) || isNaN(a) || a<0) ||(n==0&&a.length>=4)){
                    $scope.tureDay = false;
                    $scope.leaveParam.leaveDays = "";
                }else if(a == ""){
                    $scope.tureDay = false;
                    $scope.leaveParam.leaveDays = "";
                }else if(parseFloat($scope.leaveParam.leaveDays)>999){
                    $scope.tureDay = false;
                    $scope.leaveParam.leaveDays = "";
                }else{
                    $scope.tureDay = true;
                }
            };
            //天数增减
            $scope.changeDays = function (num) {
                if(num<0&&parseFloat($scope.leaveParam.leaveDays)<=0){
                    return false;
                }
                if (!isNaN($scope.leaveParam.leaveDays)) {
                    $scope.leaveParam.leaveDays = parseFloat($scope.leaveParam.leaveDays) + num;
                }
            };
            //字数
            $scope.changeNumber = function (num) {
                $scope.wordNumber = num.length;
            };
            $scope.$watch(function(){return $scope.leaveParam.leaveReason},function(){
                $scope.wordNumber = $scope.leaveParam.leaveReason.length;
            });
            //改变出校状态值
            $scope.changeIsOutSchool = function () {
                if ($scope.leaveParam.isOutSchoolFlag) {
                    $scope.leaveParam.passStartTime = "";
                    $scope.leaveParam.passEndTime = "";
                } else {
                    $scope.leaveParam.passStartTime = $scope.leaveParam.leaveStartTime;
                    $scope.leaveParam.passEndTime = $scope.leaveParam.leaveEndTime;
                }
            };

            $scope.isCancel = function(){
                if(!$location.search().item){
                    var confirmPopup = $ionicPopup.confirm({
                        title:"提示",
                        template: '是否保存已输入的内容？',
                        cancelText:"取消",
                        okText:"确认"
                    });
                    confirmPopup.then(function(res) {
                        if(res) {
                            $scope.commitLeave(0);
                        }else {
                            history.back(-1);
                        }
                    });
                }else{
                    history.back(-1);
                }
            };

            //请假信息提交
            $scope.commitLeave = function (isDraft) {
                if ($scope.leaveParam.leaveReason == "") {
                    ynuiNotification.warning({msg: '请填写请假原因！'});
                    return;
                }
                if(!$scope.leaveParam.leaveDays||$scope.leaveParam.leaveDays == ""){
                    ynuiNotification.warning({msg: '请填写请假天数！'});
                    return;
                }
                if(!$scope.tureDay){
                    ynuiNotification.warning({msg: '只能输入0-999之间的数值！（含小数）'});
                    return;
                }
                var outStatus = !$scope.leaveParam.isOutSchoolFlag;
                $scope.leaveParam.isDraft = isDraft != 1;
                $scope.leaveParam.isOutSchool = outStatus ? "1" : "2";
                $scope.leaveParam.time = (new Date()).getTime();
                $http.post(originBaseUrl + '/third/leaveAndRelease/applyLeave.htm', $scope.leaveParam).success(function (data) {
                    if (data.status == 0) {
                        if(isDraft != 1) {
                            ynuiNotification.success({msg: '保存草稿成功！'});
                        }else {
                            ynuiNotification.success({msg: '提交成功！'});
                        }
                        $location.path("/leave_view");
                    }else{
                        if(isDraft != 1){
                            ynuiNotification.error({msg: '保存草稿失败！'});
                        }else{
                            ynuiNotification.error({msg: data.message});
                        }
                    }
                })
            };
            $scope.doBack = function () {
                history.back(-1);
            };
            //刷新
            $scope.createLeaveRefresh = function () {
                getLeaveType();
                getLeaveReason();
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.wordNumber = 0;
                $scope.leaveParam = {
                    leaveReason: "",
                    isOutSchoolFlag: false,
                    leaveStartTime: $scope.backTime,
                    leaveEndTime: $scope.endTime,
                    passStartTime: $scope.backTime,
                    passEndTime: $scope.endTime,
                    leaveDays:"1",
                    stuPlatformSysUserId: ""
                };
                //修改的参数赋值
                if ($scope.leaveInfoParam) {
                    $scope.wordNumber = $scope.leaveInfoParam.leaveReason.length;
                    $scope.leaveParam = {
                        id:$scope.leaveInfoParam.id,
                        leaveReason: $scope.leaveInfoParam.leaveReason,
                        isOutSchoolFlag: !$scope.leaveInfoParam.isOutSchoolFlag,
                        leaveStartTime: $scope.leaveInfoParam.leaveStartTime,
                        leaveEndTime: $scope.leaveInfoParam.leaveEndTime,
                        passStartTime: $scope.leaveInfoParam.passStartTime,
                        passEndTime: $scope.leaveInfoParam.passEndTime,
                        stuPlatformSysUserId: "",
                        createPlatformSysUserId: "",
                        leaveDays: $scope.leaveInfoParam.leaveDays,
                        isBackSleep:$scope.leaveInfoParam.isBackSleep
                    };
                }
                $timeout(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                }, 500);
            }
        }])
    //学生销假
    .controller('leaveDeleteCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','$filter',function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,$filter) {
            $scope.dataError = false;//错误信息
            $scope.searchBar = false;  //搜索框
            $scope.emptyInfo = false;  //数据为空
            var pageNumber = 0;
            var pageSize = 20;
            //查询
            $scope.data_copy = [];   //备份数据，用于还原
            $scope.showDates = [];     //展示数据
            $scope.searchlenShow=false; //即时搜索显示
            $scope.searchlength=0;      //即时搜索初始化
            $scope.getData = function (func) {
                $http.get(originBaseUrl + '/third/leaveAndRelease/queryReturnStu.htm?from=app&pageNumber=' + pageNumber + "&pageSize="
                    + pageSize + "&time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.leaveData = data.result.content;
                        angular.forEach($scope.leaveData, function (item) {
                            if(item.headUrl !="" && item.headUrl !=null ){
                                item.headUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + item.headUrl;
                            }
                            item.showDetail = false;//点击展开或收起每条详细信息
                            //item.delStatus = false;//销假状态，点击置灰使用
                            item.searchContent = item.name;
                            if(item.alias){
                                item.searchContent=item.searchContent+item.number+item.alias
                            }
                        });
                        if ($scope.leaveData.length > 0) {
                            $scope.leaveData[0].showDetail = true;//第一条默认展开
                            $scope.emptyInfo = false;
                        } else {
                            if(pageNumber<1){
                                $scope.emptyInfo = true;              //展示空数据提示
                            }
                            $scope.dataErrorMsg = "没有需要销假的记录！";
                        }
                        if ($scope.leaveData.length > 0) {
                            var arr = $scope.data_copy.concat($scope.leaveData);
                            $scope.data_copy = arr;               //下拉叠加数据
                            $scope.showDates = arr;               //展示数据
                        }
                        $scope.moreDataCanBeLoaded = function () {
                            return data.result.content.length;    //当没有数据，不显示加载图标
                        };
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

            //取消搜索
            $scope.cancelBack = function () {
                $scope.searchBar = !$scope.searchBar;
                if($scope.search != undefined && $scope.search.searchContent != undefined){
                    $scope.search.searchContent = "";
                }
                $scope.showDates = $scope.data_copy;
                $scope.searchlenShow=false;
            };

            //清空搜索框
            $scope.cleanSearchContent = function () {
                $scope.search.searchContent = "";
            };

            //光标焦点的定位
            $scope.changeSearchStatus = function () {
                $scope.searchBar = !$scope.searchBar;
                document.getElementById("searchIt").autofocus = true;
            };
            //获取当前时间 YY-MM-dd hh:mm:ss
            var getNowTimeYMDHMS = function(){
                var myDate = new Date();
                var currentYear = myDate.getFullYear();     //获取当前年份
                var currentMonth = myDate.getMonth()+1;     //获取当前月份 (0 ~ 11)
                var currentDate = myDate.getDate();         //获取当前日(1 ~ 31)
                var currentHours = myDate.getHours();       //获取当前小时数(0-23)
                var currentMinutes =  myDate.getMinutes();     //获取当前分钟数(0-59)
                var currentSeconds = myDate.getSeconds();     //获取当前秒数(0-59)
                if(currentMonth<10){
                    currentMonth = "0"+currentMonth;
                }
                if(currentDate<10){
                    currentDate = "0"+currentDate;
                }
                if(currentHours<10){
                    currentHours = "0"+currentHours;
                }
                if(currentMinutes<10){
                    currentMinutes = "0"+currentMinutes;
                }
                if(currentSeconds<10){
                    currentSeconds = "0"+currentSeconds;
                }
                return currentMonth+"-"+currentDate+" "+currentHours+":"+currentMinutes;
            };
            //销假
            $scope.delLeave = function ($event, no) {
                //阻止冒泡事件
                $event.stopPropagation();
                $event.preventDefault();
                var leaveIdArr = [];
                leaveIdArr.push($scope.showDates[no].id);
                if($scope.showDates[no].backSchoolTime==""||$scope.showDates[no].backSchoolTime==null){
                    $scope.showDates[no].backSchoolTime = getNowTimeYMDHMS();
                }
                $scope.delParam = {
                    backTime: $scope.showDates[no].backSchoolTime,
                    ids: leaveIdArr
                };
                $http.get(originBaseUrl + "/third/leaveAndRelease/returnConfirm.htm?backTime=" + $scope.showDates[no].backSchoolTime +
                    "&ids=" + leaveIdArr+ "&_datatime=" + new Date().getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.showDates[no].delStatus = true;
                    }
                })
            };
            //刷新
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                //置空数据，在获取时候叠加数据
                $scope.leaveData = null;
                $scope.data_copy = [];
                $scope.showDates = [];
                $scope.searchContent = "";//搜索内容
                $scope.searchBar = !$scope.searchBar;
 				if($scope.search != undefined && $scope.search.searchContent != undefined){
                    $scope.search.searchContent = "";
                }
                pageNumber = 0;
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };

            //下拉获取更多数据
            $scope.loadMore = function () {
                //请求数据
                pageNumber += 1;
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                });
            };

            //接收event事件
            $scope.$on('stateChangeSuccess', function () {
                $scope.loadMore();
            });
        var filterSameId = function(arr,name){
            var newArr = [];
            var json = {};
            for(var i= 0;i<arr.length;i++){
                if(!json[arr[i][name]]){
                    newArr.push(arr[i]);
                    json[arr[i][name]] = true;
                }
            }
            return newArr;
        };
        $scope.$watch('search.searchContent', function(newVal, oldVal) {
            if (newVal !== oldVal) {
                var nameAttr = $filter('filter')($scope.data_copy,{name:$scope.search.searchContent});
                var aliasAttr = $filter('filter')($scope.data_copy,{alias:$scope.search.searchContent});
                var numberAttr = $filter('filter')($scope.data_copy,{number:$scope.search.searchContent});
                $scope.showDates = filterSameId(nameAttr.concat(aliasAttr,numberAttr),'id');
                $scope.searchlenShow=true;
            }
        }, true);
        }])
    //查看通知公告
    .controller('appViewNoticeController',["$scope","$location","$http","$sce","$window",function($scope,$location,$http,$sce,$window){
        /**
         * 根据URL mapping获取到对应的值
         * @param firstIndex    需要的获取的值得key
         * @param lastIndex
         * @returns {string|*}
         */
        $scope.getUrlParams = function(firstIndex,lastIndex,indexof){
            var fi = firstIndex + '=';
            if(lastIndex){
                //如果不是最后一个参数
                return $location.$$absUrl.substring(($location.$$absUrl.lastIndexOf(fi)+fi.length),($location.$$absUrl.lastIndexOf(lastIndex)))
            }else{
                return $location.$$absUrl.substring($location.$$absUrl.lastIndexOf(fi)+fi.length);
            }
        };
        //sanitize富文本的内容，使其能正常在页面显示
        $scope.initNoticeContent = function(){
            if($scope.notice){
                $scope.notice.noticeContent = $scope.notice.noticeContent ? $sce.trustAsHtml("<div>"+$scope.notice.noticeContent+"</div>") : "";
                if($scope.notice.messageNoticeSignedTemplateVO){
                    $scope.notice.messageNoticeSignedTemplateVO.signedContent = $scope.notice.messageNoticeSignedTemplateVO.signedContent ?  $sce.trustAsHtml("<div>"+$scope.notice.messageNoticeSignedTemplateVO.signedContent+"</div>") : '';
                }
            }
        };
        //判断客户端是手机还是PC
        var IsPC = function(){
            var userAgentInfo = navigator.userAgent;
            var Agents = ["Android", "iPhone",
                "SymbianOS", "Windows Phone",
                "iPad", "iPod"];
            var flag = true;
            $scope.flagShow = true;
            var AgentsLen = Agents.length;
            for (var v = 0; v <AgentsLen ; v++) {
                if (userAgentInfo.indexOf(Agents[v]) > 0) {
                   flag = false;
                    $scope.flagShow = false;
                    break;
                }
            }
            return flag;
        };
        $scope.initData = function(){
            $scope.type = $scope.getUrlParams("type","&");
            $scope.id = $scope.getUrlParams("id");
            $scope.requestSource = IsPC()?0:1;
            //根据传过来的参数判断此页面是 “预览公告，查看公告，查看公告（接收）” 中的哪种
            if($scope.type == 'preview'){
                //$scope.notice = localStorageService.get("previewNoticeObj");
                //查出发布人
                $http.get(originBaseUrl + "/third/notice/queryPublisher.htm"+ "?_datatime=" + new Date().getTime(),{params:{id:$scope.id,isUpdate:false,isReceiver:true,timeStamp:new Date().getTime()}}).success(function(data){
                    if(data.status == 0){
                        $scope.notice.publisherName = data.result.name+"("+data.result.userNumber+")";
                    }
                });
                $scope.initNoticeContent();
            }else{
                //查询当前需要查看的通知
                var isReceiver = $scope.type=='receiver'?true:false;
                $http.get(originBaseUrl + "/third/notice/queryNotice.htm"+ "?_datatime=" + new Date().getTime(),{params:{id:$scope.id,isUpdate:false,requestSource:$scope.requestSource,isReceiver:isReceiver,timeStamp:new Date().getTime()}}).success(function(data){
                    if(data.status == 0){
                        $scope.notice = data.result;
                        $scope.initNoticeContent();
                        //转换byte格式为kb
                        angular.forEach($scope.notice.messageNoticeAttachmentList,function(item){
                            item.size = (parseInt(item.size)/1024).toFixed(2);
                        });
                    }
                });
                if($scope.type == 'publisher'){
                    //当时发布者查看时，还需要把所有的接收者 阅读情况查出来
                    $http.get(originBaseUrl + "/third/notice/queryReceiverInfo.htm"+ "?_datatime=" + new Date().getTime(),{params:{id:$scope.id,timeStamp:new Date().getTime()}}).success(function(data){
                        if(data.status == 0){
                            $scope.receiverInfo = data.result;
                            //默认显示所有读取人的信息
                            $scope.readInfoList = $scope.receiverInfo.readList.concat($scope.receiverInfo.unreadList);
                        }
                    });
                }
            }
        };
        $scope.initData();
        //下载
        $scope.downLoad = function(item){
            $window.open("/ynedut/file/downloadStream.htm?fastDFSId="+item.attachmentId,"_blank");
        };
		if ($scope.flagShow) {
			angular.element('title').text('通知消息 - 智慧校园平台-YNedut V8');
		}
    }])
    //班级情况统计
    .controller('ClassTatisticsController', ['$rootScope', '$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location",
        function ($rootScope, $scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location) {
            $scope.emptyInfo = false;  //数据加载失败
            //获取当前时间
            $scope.getCurrentTime = function(){
                var addDay = new Date();
                var month2 = (addDay.getMonth() + 1) > 9 ? (addDay.getMonth() + 1) : '0' + (addDay.getMonth() + 1);
                var day2 = addDay.getDate() > 9 ? addDay.getDate() : '0' + addDay.getDate();
                addDay = addDay.getFullYear() + "-" + month2 + "-" + day2;
                return addDay;
            }

            $scope.conditions =  {
                queryDate:$scope.getCurrentTime()
            };
            $scope.classInfo = {};
            $scope.classList = [];

            $scope.selectedClass = function(item){
                $scope.classInfo = item;
                $scope.doRefresh();
            }
            $scope.classCountList = [];
            //查询班级情况统计
            $scope.queryClassConditionCountInfo = function(func){
                if(!$scope.classInfo.id){
                    return false;
                }
                $http.get(originBaseUrl + '/third/classStatistics/queryClassConditionCountInfo.htm?&time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.classCountList = data.result;
                        $scope.emptyInfo = false;
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            };
            //查询 （type）1：应到 2：请假 3：处分  详情
            $scope.viewDetails = function(type,kqktId){
                if(type == 1){
                    $location.path("/class_ydStudent").search("item",JSON.stringify({classId:arguments[1]}));
                }else if(type == 2){
                    $location.path("/class_leaveStudent").search("item",JSON.stringify({classId:arguments[1],queryType:arguments[2]}));
                }else if(type == 3){
                    $location.path("/class_punishStudent").search("item",JSON.stringify({classId:arguments[1]}));
                }
            };
            $scope.getData = function(func){
                    $scope.queryClassConditionCountInfo(func);
            };
            //查询班级下拉列表
            $.ajax({
                url : originBaseUrl + '/third/classStatistics/queryPlatformAdminClassNoGraduteByUserId.htm?' +
                'access_token=' + $rootScope.authorizationStr.access_token +
                '&userId=' + $rootScope.authorizationStr.userId +
                '&userType=' + $rootScope.authorizationStr.userType,
                cache : false,
                async : false,
                type : "POST",
                dataType : 'json',
                success : function (data){
                    if (data.status == 0) {
                        $scope.classList = data.result;
                        if($scope.classList&&  $scope.classList.length>0 ){
                            $scope.classInfo = $scope.classList[0];
                            $scope.getData();
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                }
            });
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
        }])
    //班级考情统计 应到学生
    .controller('classYdStudentCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location) {
            $scope.classId = JSON.parse($location.search().item).classId;
            $scope.emptyInfo = false;
            $scope.isExistStu = true;
            $scope.getData = function(func){
                $scope.stuList = [];
                $http.get(originBaseUrl + '/third/classStatistics/queryClassTotalStudentInfo.htm?classId='+$scope.classId+"&time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.stuList = data.result;
                        angular.forEach($scope.stuList,function(item){
                            if(item.headUrl !="" && item.headUrl !=null ){
                                item.headUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + item.headUrl;
                            }
                        });
                        $scope.emptyInfo = false;
                        if($scope.stuList&&$scope.stuList.length>0){
                            $scope.isExistStu = true;
                        }else{
                            $scope.isExistStu = false;
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }

                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });

            };

            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.doBack = function () {
                history.back(-1);
            };
        }])
    //班级考情统计 处分学生
    .controller('classPunishStudentCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location) {
            $scope.classId = JSON.parse($location.search().item).classId;
            $scope.emptyInfo = false;
            $scope.isExistStu = true;
            $scope.getData = function(func){
                $scope.stuList = [];
                $http.get(originBaseUrl + '/third/classStatistics/queryClassStudentPunishInfo.htm?classId='+$scope.classId+"&time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.stuList = data.result;
                        angular.forEach($scope.stuList,function(item){
                            item.isUnfold = false;
                            if(item.headUrl !="" && item.headUrl !=null ){
                                item.headUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + item.headUrl;
                            }
                        });
                        $scope.emptyInfo = false;
                        if($scope.stuList&&$scope.stuList.length>0){
                            $scope.isExistStu = true;
                        }else{
                            $scope.isExistStu = false;
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }

                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });

            };
            $scope.clickUnfold = function(item){
                item.isUnfold = ! item.isUnfold;
            };
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.doBack = function () {
                history.back(-1);
            };
        }])
    //班级考情统计 请假学生
    .controller('classLeaveStudentCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location) {
            $scope.queryType = JSON.parse($location.search().item).queryType;//0:出校学生  1:请假人数 2:逾期未销假人数
            $scope.classId = JSON.parse($location.search().item).classId;
            $scope.emptyInfo = false;
            $scope.isExistStu = true;
            $scope.getData = function(func){
                $scope.stuList = [];
                $http.get(originBaseUrl + '/third/classStatistics/queryClassStudentCountInfo.htm?classId='+$scope.classId+"&queryType="+$scope.queryType+"&time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.stuList = data.result;
                        angular.forEach($scope.stuList,function(item){
                            item.isUnfold = false;
                            if(item.headUrl !="" && item.headUrl !=null ){
                                item.headUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + item.headUrl;
                            }
                        });
                        $scope.emptyInfo = false;
                        if($scope.stuList&&$scope.stuList.length>0){
                            $scope.isExistStu = true;
                        }else{
                            $scope.isExistStu = false;
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });

            };
            $scope.clickUnfold = function(item){
                item.isUnfold = ! item.isUnfold;
            };
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.doBack = function () {
                history.back(-1);
            };
        }])
    //课堂考勤统计
    .controller('ktkqTatisticsController', ['$rootScope', '$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location",
        function ($rootScope, $scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location) {
            $scope.emptyInfo = false;  //数据加载失败
            //获取当前时间
            $scope.getCurrentTime = function(){
                var addDay = new Date();
                var month2 = (addDay.getMonth() + 1) > 9 ? (addDay.getMonth() + 1) : '0' + (addDay.getMonth() + 1);
                var day2 = addDay.getDate() > 9 ? addDay.getDate() : '0' + addDay.getDate();
                addDay = addDay.getFullYear() + "-" + month2 + "-" + day2;
                return addDay;
            };

            $scope.conditions =  {
                queryDate:$scope.getCurrentTime()
            };
            $scope.classInfo = {};
            $scope.classList = [];

            $scope.selectedClass = function(item){
                $scope.classInfo = item;
                $scope.doRefresh();
            };
            $scope.classCountList = [];
            //课堂考勤统计
            $scope.queryKqktClassCountInfo = function(func){
                if(!$scope.classInfo.id||!$scope.conditions.queryDate){
                    return false;
                }
                $http.get(originBaseUrl + '/third/attendance/queryKqktClassCountInfo.htm?classId='+$scope.classInfo.id+"&queryDate="+$scope.conditions.queryDate+"&time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.kqKTCount = data.result;
                        $scope.emptyInfo = false;
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            };
            //查询 （type）1：应到 2：请假 3：处分 4:课堂考勤学生 详情
            $scope.viewDetails = function(kqktId,ktStatus){
                    $location.path("/ktkq_student").search("item",JSON.stringify({classId:$scope.classInfo.id,kqktId:kqktId,ktStatus:ktStatus}));
            };
            $scope.getData = function(func){
                    $scope.queryKqktClassCountInfo(func);
            };
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            //查询班级下拉列表
            $.ajax({
                url : originBaseUrl + '/third/classStatistics/queryPlatformAdminClassNoGraduteByUserId.htm?' +
                'access_token=' + $rootScope.authorizationStr.access_token +
                '&userId=' + $rootScope.authorizationStr.userId +
                '&userType=' + $rootScope.authorizationStr.userType,
                cache : false,
                async : false,
                type : "POST",
                dataType : 'json',
                success : function (data){
                    if (data.status == 0) {
                        $scope.classList = data.result;
                        if($scope.classList&&  $scope.classList.length>0 ){
                            $scope.classInfo = $scope.classList[0];
                            $scope.doRefresh();
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                }
            });
        }])
    //课堂考勤 学生详情
    .controller('ktkqStudentCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location) {
            $scope.ktStatus = JSON.parse($location.search().item).ktStatus;;//考勤状态 all:应到 act:实到 1：请假 2：缺席 3：迟到 4：早退 5：按时到

            $scope.ktStatusList = [
                {ktStatus:"all",ktStatusName:"应到学生"},
                {ktStatus:"act",ktStatusName:"实到学生"},
                {ktStatus:"1",ktStatusName:"请假学生"},
                {ktStatus:"2",ktStatusName:"缺席学生"},
                {ktStatus:"3",ktStatusName:"迟到学生"},
                {ktStatus:"4",ktStatusName:"早退学生"},
                {ktStatus:"5",ktStatusName:"按时到学生"}
            ];
            $scope.getStatusName = function(ktStatus){
                var ktStatusName = "";
                if(ktStatus == "all"){
                    ktStatusName =  '应到学生';
                }
                if(ktStatus == "act"){
                    ktStatusName =  '实到学生';
                }
                if(ktStatus == "1"){
                    ktStatusName =  '请假学生';
                }
                if(ktStatus == "2"){
                    ktStatusName =  '缺席学生';
                }
                if(ktStatus == "3"){
                    ktStatusName =  '迟到学生';
                }
                if(ktStatus == "4"){
                    ktStatusName =  '早退学生';
                }
                if(ktStatus == "5"){
                    ktStatusName =  '按时到学生';
                }
                return ktStatusName;
            };
            $scope.ktStatusName =  $scope.getStatusName($scope.ktStatus);
            $scope.classId = JSON.parse($location.search().item).classId;
            $scope.kqktId = JSON.parse($location.search().item).kqktId;
            $scope.emptyInfo = false;
            $scope.getData = function(func){
                $scope.stuList = [];
                $http.get(originBaseUrl + '/third/attendance/queryKqktClassStudentCountInfo.htm?classId='+$scope.classId+"&kqktId="+$scope.kqktId +"&ktStatus="+$scope.ktStatus+"&time=" + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.stuList = data.result;
                        angular.forEach($scope.stuList,function(item){
                            item.isUnfold = false;
                            if(item.headUrl !="" && item.headUrl !=null ){
                                item.headUrl = originBaseUrl + "/file/downloadStream.htm?fastDFSId=" + item.headUrl;
                            }
                        });
                        $scope.emptyInfo = false;
                        if($scope.stuList&&$scope.stuList.length>0){
                            $scope.isExistStu = true;
                        }else{
                            $scope.isExistStu = false;
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });

            };
            $scope.clickUnfold = function(item){
                if(!item.leave){
                     return false;
                }
                item.isUnfold = ! item.isUnfold;
            }
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.doBack = function () {
                history.back(-1);
            };
        }])
		/*************************************移动端选课start**********************************************************/
	//已选课程
	.controller('chooseLessonsCtrl',['$scope','$http','$ionicModal','$ionicLoading','$ionicScrollDelegate','ynuiNotification','$ionicSlideBoxDelegate','$location',function($scope,$http,$ionicModal,$ionicLoading,$ionicScrollDelegate,ynuiNotification,$ionicSlideBoxDelegate,$location){
	  $scope.selectCourseRequire = "";
	  $scope.currentTermId = "";
	$scope.windowHeight=$(window).height()-211;
	if($location.search().termId){
		    $scope.currentTermId = $location.search().termId;
	  }
	  $scope.clickTaskDate = function(str){
		if(str){
			ynuiNotification.warning({msg:  str});
		}
	  }
	  $scope.isInit = true;
	 //获取已选择的课程
	  $scope.getSelectedCourse = function(termId){
		    $scope.selectCourseRequire = "";
			if(!$scope.isInit){
				$ionicLoading.show({
				template: "正在加载..."
				});
			}

		  	$http.post(originBaseUrl + '/third/xkStuMobile/queryMySelectedCourseByMobile.htm',{termId:termId}).success(function(data){
				$ionicLoading.hide();
				$scope.emptyInfo = false;
				if($scope.isInit){
					$scope.isInit = false;
				}
               if(data.status==0){
					$scope.$broadcast('scroll.infiniteScrollComplete');
					$ionicScrollDelegate.scrollTop();
					$scope.selectedCourseVO = data.result;
					if($scope.selectedCourseVO){
						$scope.currentTermId = $scope.selectedCourseVO.defaultTermId;
					}else{
						return ;
					}

					if($scope.selectedCourseVO.maxCredit!=null&&$scope.selectedCourseVO.maxCredit!=""&&$scope.selectedCourseVO.maxCredit!=undefined){
						 $scope.selectCourseRequire+="选课学分要求不超过"+$scope.selectedCourseVO.maxCredit+"分，";
					}else{
						$scope.selectCourseRequire+="选课学分要求不限制，";
					}
					if($scope.selectedCourseVO.maxCourse!=null&&$scope.selectedCourseVO.maxCourse!=""&&$scope.selectedCourseVO.maxCourse!=undefined){
							 $scope.selectCourseRequire+="选课数量要求不超过"+$scope.selectedCourseVO.maxCourse
					}else{
							$scope.selectCourseRequire+="选课数量要求不限制";
					}
               }else{
				   ynuiNotification.error({msg: data.message});
			   }

            }).error(function () {
				  $ionicLoading.hide();
					if($scope.isInit){
						$scope.isInit = false;
					}
				//  ynuiNotification.error({msg: '加载失败！'});
				$scope.emptyInfo = true;
				$scope.dataErrorMsg = '加载失败，请重试！';
            });
	  }
	  //切换学期
	  $scope.switchoverTerm = function(){
		   $scope.getSelectedCourse($scope.selectedCourseVO.termId);
	  }
	   $scope.getSelectedCourse($scope.currentTermId);
	   //选课要求提示
	   $scope.clickRequire = function(){
		   ynuiNotification.warning({msg:  $scope.selectCourseRequire});
	   }
	   //撤销选课
	   $scope.revocation = function(item){
		   	$ionicLoading.show({
                template: "正在撤销..."
            });
		  	$http.post(originBaseUrl + '/third/xkStuMobile/cancelCourseByMobile.htm',{id:item.id}).success(function(data){
               if(data.status==0){
					ynuiNotification.success({msg:  "撤销成功"});
					$scope.getSelectedCourse($scope.currentTermId);
               }else{
				   ynuiNotification.error({msg: data.message});
			   }
			  $ionicLoading.hide();
            }).error(function () {
				  $ionicLoading.hide();
				  ynuiNotification.error({msg: '提交失败！'});
				//$scope.emptyInfo = true;
				//$scope.dataErrorMsg = '加载失败，请重试！';
            });
	   }
	   //不选课原因提示
	   $scope.notGoReason = function(item){
		   	ynuiNotification.warning({msg:  item.remark});
	   }
	   // 跳转的选课页面
	   $scope.gotoSelectCourse = function(){
		   if(!$scope.currentTermId){
			   ynuiNotification.error({msg: "学期ID不能为空！"});
			   return ;
		   }
		    $location.path("/choose_lessons_detail").search("termId", $scope.currentTermId);
	   }
    }])
	//选择课程
	.controller('chooseLessonsDetailCtrl',['$scope','$http','$ionicModal','$ionicLoading','$ionicScrollDelegate','ynuiNotification','$ionicSlideBoxDelegate','$location',function($scope,$http,$ionicModal,$ionicLoading,$ionicScrollDelegate,ynuiNotification,$ionicSlideBoxDelegate,$location){

		$scope.termId = $location.search().termId;
			$scope.showCourseList = [];
		$scope.dataLoading = true;
		//获取可以选择的课程
		$scope.getSelectingCorseList = function(termId){
			if(!$scope.dataLoading){
				$ionicLoading.show({
					template: "正在加载..."
				});
			}
			$http.post(originBaseUrl + '/third/xkStuMobile/queryStuSelectingCorseByMobile.htm',{termId:termId}).success(function(data){
               		$scope.emptyInfo = false;
				    $scope.dataLoading = false;
			   if(data.status==0){
				$scope.courseList = data.result;
				$scope.showCourseList = $scope.courseList;
				angular.forEach($scope.courseList,function(group){
					angular.forEach(group.xkStuMobileTeachClassDetailVOList,function(item){
						item.isSelect = false;
					})
				});
               }else{
				   ynuiNotification.error({msg: data.message});
			   }
			  $ionicLoading.hide();
            }).error(function () {
				$ionicLoading.hide();
				$scope.dataLoading = false;
				//  ynuiNotification.error({msg: '加载失败！'});
				$scope.emptyInfo = true;
				$scope.dataErrorMsg = '加载失败，请重试！';
            });
		}
		$scope.getSelectingCorseList($scope.termId);
		$scope.tempSelectCorse = null;
		//点击选择课程按钮
		$scope.clickSelectCorse = function(item){
			if(item.isRequired){
				$scope.openModal(item);
			}else{
				$scope.selectCorse(item);
			}
		}
		 $ionicModal.fromTemplateUrl('template.html',{
            scope:$scope,
            animation:'slide-in-up'
        }).then(function(modal){
            $scope.modal = modal;
        });
        $scope.openModal = function(item) {
			$scope.tempSelectCorse = item;
            $scope.modal.show();
        };
        $scope.closeModal = function() {
            $scope.modal.hide();
        };
		//选择课程
        $scope.isFinished = true;
		$scope.selectCorse = function(item){
            $ionicLoading.show({
                template: "正在选课..."
            });
            $scope.closeModal();
            if($scope.isFinished){
                $scope.isFinished = false;
                $http.post(originBaseUrl + '/third/xkStuMobile/selectCourseByMobile.htm',{termId:$scope.termId,teachClassId:item.id}).success(function(data){
                    if(data.status==0){
                        var returnVO = data.result;
                        if(returnVO.successStuCount>0){
                            item.selectedNumber=returnVO.selectedNum;

                            item.isSelect = true;
                            setTimeout(function(){
                                $scope.$apply();
                            });
                            ynuiNotification.success({msg:  "选课成功"});
                        }else{
                            ynuiNotification.error({msg: "选课失败，"+returnVO.errorMsg});
                        }
                    }else{
                        ynuiNotification.error({msg: "选课失败，"+data.message});
                    }
                    $scope.isFinished = true;
                    $ionicLoading.hide();
                }).error(function () {
                    $scope.isFinished = true;
                    $ionicLoading.hide();
                    //  ynuiNotification.error({msg: '加载失败！'});
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            }


		}

		$scope.searchValue = "";
		$scope.clearSearchValue = function(){
			$scope.searchValue = "";
		}
		$scope.$watch("searchValue",function(){
			if($scope.searchValue){
				$scope.searchCorse($scope.searchValue);
			}else{
				$scope.showCourseList = $scope.courseList;
				setTimeout(function(){
					$scope.$apply();
				});
			}
		});
		// 搜索课程
		$scope.searchCorse = function(searchValue){
			var currentCourseList = [];
			angular.forEach($scope.courseList,function(group){
				angular.forEach(group.xkStuMobileTeachClassDetailVOList,function(item){
					if(item.electiveCourseNameStr.indexOf(searchValue) != -1){
						$scope.assemblyShowCourseList(item,group,currentCourseList);
					}
				});
			});
			$scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
			$scope.showCourseList = currentCourseList;
			setTimeout(function(){
				$scope.$apply();
			});
		}
		//将搜索出匹配的结果放入showCourseList
		$scope.assemblyShowCourseList = function(item,group,courseList){
			var isExist = false;
			angular.forEach(courseList,function(currentGroup){
				if(currentGroup.id == group.id){
					currentGroup.xkStuMobileTeachClassDetailVOList.push(item);
					if(currentGroup.xkStuMobileTeachClassDetailVOList.length == group.xkStuMobileTeachClassDetailVOList.length){
						//isShowAll = false 表示不显示“显示此组所有课程”此选项
						currentGroup.isShowAll = false;
					}else{
						currentGroup.isShowAll = true;
					}
					isExist = true;
				}
			});
			if(!isExist){
				var currentGroup = {
					id:group.id,
					groupName:group.groupName,
					isShowAll:group.xkStuMobileTeachClassDetailVOList.length == 1?false:true,
					xkStuMobileTeachClassDetailVOList:[]
				}
				currentGroup.xkStuMobileTeachClassDetailVOList.push(item);
				courseList.push(currentGroup);
			}

		}
		//搜索的时候显示全部课程
		$scope.showAllCourse = function(group){
			var currentGroupAll = null;
			angular.forEach($scope.courseList,function(item){
				if(item.id == group.id){
					currentGroupAll = item;
				}
			});
			angular.forEach(currentGroupAll.xkStuMobileTeachClassDetailVOList,function(item){
				var isExist = false;
				angular.forEach(group.xkStuMobileTeachClassDetailVOList,function(i){
					if(item.id == i.id){
						isExist = true;
					}
				});
				if(!isExist){
					group.xkStuMobileTeachClassDetailVOList.push(item);
				}
			});
			setTimeout(function(){
				$scope.$apply();
			});
			group.isShowAll = false;
		}
		// 返回选课记录页面
	   $scope.getBack = function(){
		    $location.path("/choose_lessons").search("termId", $scope.termId);
	   }

    }])
	/*************************************移动端选课end**********************************************************/
    //班级宿舍统计
    .controller('dormitoryCtrl',['$scope','$http','$ionicModal','$ionicLoading','$ionicScrollDelegate','ynuiNotification','$ionicSlideBoxDelegate',function($scope,$http,$ionicModal,$ionicLoading,$ionicScrollDelegate,ynuiNotification,$ionicSlideBoxDelegate){
        $ionicModal.fromTemplateUrl('dormitory_member.html',{
            scope:$scope,
            animation:'slide-in-up'
        }).then(function(modal){
            $scope.modal = modal;
        });
		$scope.selectDormitory = null;
        $scope.openDormitory = function(item) {
			$scope.selectDormitory = item;
			$scope.getDormitoryDetailInfo(item.dormitoryRoomId,$scope.showClassDormitoryInfo.platformSysAdminClassId);

        };
        $scope.closeDormitory = function() {
            $scope.modal.hide();
        };

        $ionicModal.fromTemplateUrl('dormitory_Administrator.html',{
            scope:$scope,
            animation:'slide-in-up'
        }).then(function(modal1){
            $scope.modal1 = modal1;
        });
        $scope.openAdministrator = function() {
            $scope.modal1.show();
        };
        $scope.closeAdministrator = function() {
            $scope.modal1.hide();
        };
		$scope.showClassDormitoryInfo = null;
		$scope.selectGender = "all";

		$scope.nextSlide = function() {
			$ionicSlideBoxDelegate.next();
		}
		$scope.previousSlide = function() {
			$ionicSlideBoxDelegate.previous();
		}
	
		$scope.selectGenderChange = function(gender){
			$scope.selectGender = gender;
			setTimeout(function(){
				$scope.$apply();
			});
			 $scope.$broadcast('scroll.infiniteScrollComplete');
			$ionicScrollDelegate.scrollTop();
		}
        //滑动框展示的数据集合固定3个
		$scope.slideList=[{},{},{}];
        //滑动框滑动之结束后调用
        $scope.slideChange = function(index){
			var currentIndex = $ionicSlideBoxDelegate.currentIndex();
			if($scope.classDormitoryInfoList&&$scope.classDormitoryInfoList.length>0){
				$scope.showClassDormitoryInfo = $scope.slideList[index];
				$scope.selectGender = "all";
				setTimeout(function(){
					$scope.$apply();
				});
				$ionicScrollDelegate.scrollTop();
				$scope.setNextAndPreviousClassInfo ();
			}
        }
        //设置下一个班级和上一个班级信息
		$scope.setNextAndPreviousClassInfo = function(){
			var currentIndex = $ionicSlideBoxDelegate.currentIndex();
			var currentClassIndex = $scope.slideList[currentIndex].classIndex;
			if(currentClassIndex == $scope.classDormitoryInfoList.length-1){
				$scope.slideList[$scope.getNextAndPreviousSlideIndex("next")] = $scope.classDormitoryInfoList[0]
			}else{
				$scope.slideList[$scope.getNextAndPreviousSlideIndex("next")] = $scope.classDormitoryInfoList[currentClassIndex+1]
			}
			if(currentClassIndex == 0){
				$scope.slideList[$scope.getNextAndPreviousSlideIndex("previous")] = $scope.classDormitoryInfoList[$scope.classDormitoryInfoList.length-1];
			}else{
				$scope.slideList[$scope.getNextAndPreviousSlideIndex("previous")] = $scope.classDormitoryInfoList[currentClassIndex-1];
			}
		}
        //获取下一个或者上一个滑动框的序号
		$scope.getNextAndPreviousSlideIndex = function(type){
			var currentIndex = $ionicSlideBoxDelegate.currentIndex();
			if(type == "next"){
				if(currentIndex == ($scope.slideList.length-1)){
					return 0;
				}else{
					return parseInt(currentIndex)+1;
				}
			}else{
				if(currentIndex == 0){
					return $scope.slideList.length-1;
				}else{
					return parseInt(currentIndex)-1;
				}
			}
		}
		$scope.queryClassDormitoryInfoByTeacher = function(func){
			 $http.get(originBaseUrl + '/third/classDormStatistics/queryClassDormitoryInfoByTeacher.htm?'+ "_datatime=" + new Date().getTime()).success(function(data){
               if(data.status==0){
				   $scope.emptyInfo = false;
				   $scope.classDormitoryInfoList = data.result;
				  angular.forEach($scope.classDormitoryInfoList ,function(item,index){
					   item.classIndex = index;
				   });				
				   if($scope.classDormitoryInfoList&&$scope.classDormitoryInfoList.length>0){
					    var currentIndex = $ionicSlideBoxDelegate.currentIndex();
						$scope.slideList[currentIndex] =  $scope.classDormitoryInfoList[0];
						$scope.setNextAndPreviousClassInfo();
					   	$scope.showClassDormitoryInfo = $scope.classDormitoryInfoList[0];
						if($scope.classDormitoryInfoList.length == 1){
							//使滑动框不能滑动
							$ionicSlideBoxDelegate.enableSlide(false);
						}else{
							$ionicSlideBoxDelegate.enableSlide(true);
						}
				   }else{
						//清空滑动框数据
						$scope.slideList=[{},{},{}];
						$ionicSlideBoxDelegate.enableSlide(false);
                   }
               }else{
				   ynuiNotification.error({msg: data.message});
			   }
				if (func) {
					func();
				}
				$ionicSlideBoxDelegate.update()
            }).error(function () {
                if (func) {
                    func();
                }
                $scope.emptyInfo = true;
                $scope.dataErrorMsg = '加载失败，请重试！';
            });
        }
        //刷新
        $scope.doRefresh = function (temp) {
            var template = '正在刷新...';
            if(temp){
                template = temp;
            }
            $ionicLoading.show({
                template: template
            });
            $scope.classDormitoryInfoList = [];;
            $scope.queryClassDormitoryInfoByTeacher(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };
        $scope.setectBedInfo = null;
        $scope.clickBedInfo = function(item){
            $scope.setectBedInfo = item;
        }
        $scope.doRefresh("正在加载...");
        $scope.getDormitoryDetailInfo = function(dormitoryRoomId,platformSysAdminClassId){
            $ionicLoading.show({
                template: "正在加载..."
            });
            $http.post(originBaseUrl + '/third/classDormStatistics/queryDormitoryDetailInfoById.htm?',{dormitoryRoomId:dormitoryRoomId,platformSysAdminClassId:platformSysAdminClassId}).success(function(data){
                if(data.status==0){
                    $scope.bedDetailInfoList = data.result;
                    if($scope.bedDetailInfoList.ssMobileBedDetailInfoVOList){
                        for(var i=0;i<$scope.bedDetailInfoList.ssMobileBedDetailInfoVOList.length;i++){
                            if(!$scope.bedDetailInfoList.ssMobileBedDetailInfoVOList[i].isFreeBed){
                                $scope.clickBedInfo($scope.bedDetailInfoList.ssMobileBedDetailInfoVOList[i]);
                                break;
                            }
                        }
                    }

                    $scope.modal.show();
                }else{
                    ynuiNotification.error({msg: data.message});
                }
                $ionicLoading.hide();
            }).error(function () {
                $ionicLoading.hide();
                ynuiNotification.error({msg: '加载失败！'});
            });
        }
    }])//宿舍考核  任务列表
    .controller('dormitoryExamineCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location","$rootScope",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location,$rootScope) {
            $scope.getData = function(func){
                $scope.emptyInfo = false;
                $scope.stuList = [];
                $scope.addButtonName = "";
                $scope.dataErrorMsg = '没有可选的考核任务，请先添加！';
                var basePath = originBaseUrl+ "/third/dormitoryExamineTask";
                $http.get(basePath + '/findTaskList.htm?time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.scoreTaskList = data.result;
                        if ($scope.scoreTaskList == null || $scope.scoreTaskList.length == 0){
                            $scope.emptyInfo = true;
                            $scope.addButtonName = "添加";
                        }else {
                            $scope.addButtonName = "新增";
                        }
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.addButtonName = "添加";
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            };

            $scope.addScoreTask = function () {
                $location.path("/add_task_dmt");
            };
            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.doBack = function () {
                history.back(-1);
            };

            //去考核页面
            $scope.toDormitoryForm = function(item){
                $location.path("/grade_dormitory").search("task",angular.toJson(item));
            }
        }])
    //宿舍考核  添加任务
    .controller('addTaskCtrlDorm', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location","$ionicModal","$rootScope",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location,$ionicModal,$rootScope) {
            //添加考核任务
            $scope.taskConditions = {
                scoreTable : "",
                checkRegion : "",
                checkNumber : "",
                term : "",
                checkDate : ""
            };
            //添加考核任务显示名称
            $scope.showTask = {
                scoreTableName : "",
                checkRegionName : "",
                checkNumberName : "",
                termName : ""
            };
            var basePath = originBaseUrl+ "/third/dormitoryExamineTask";
            //区域类型
            $scope.checkType = 0;
            $scope.selectFrequency=[];
            $scope.selectArea = [];
            $scope.showRegionInfo = "请先选择打分表";
            var date = new Date();
            var currentDate = date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
            $scope.taskConditions.checkDate = currentDate;
            $scope.getData = function(func){
                $scope.emptyInfo = false;
                $scope.stuList = [];
                $http.get(basePath + '/findTaskList.htm?time=' + (new Date()).getTime()).success(function (data) {
                    if (data.status == 0) {
                        $scope.scoreTaskList = data.result;
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });

            };

            //打分表浮层
            $ionicModal.fromTemplateUrl('show_score_table.html',{
                scope:$scope,
                animation:'slide-in-up'
            }).then(function(modal){
                $scope.tableList = modal;
            });

            //考核区域浮层
            $ionicModal.fromTemplateUrl('show_check_region.html',{
                scope:$scope,
                animation:'slide-in-up'
            }).then(function(modal){
                $scope.regionList = modal;
            });

            //考核次数浮层
            $ionicModal.fromTemplateUrl('show_check_number.html',{
                scope:$scope,
                animation:'slide-in-up'
            }).then(function(modal){
                $scope.numberList = modal;
            });

            //学期浮层
            $ionicModal.fromTemplateUrl('show_term.html',{
                scope:$scope,
                animation:'slide-in-up'
            }).then(function(modal){
                $scope.termList = modal;
            });

            //选择打分表
            $scope.chooseTable = function () {
                //加载打分表
                $http.post( basePath + "/queryUseScoreTable.htm", {}).success(function (data) {
                    if (data.status == 0) {
                        $scope.selectScoreTable = data.result;
                        $scope.tableList.show();
                    }
                });
            };
            $scope.confirmTable = function (item) {
                $scope.taskConditions.scoreTable = item.id;
                $scope.showTask.scoreTableName = item.name;
                $scope.showRegionInfo = "请选择";
                $scope.getNumber(item.id);
                $scope.hideTable();
                $scope.chooseRegion(item.id);
            };
            //隐藏打分表
            $scope.hideTable = function() {
                $scope.tableList.hide();
            };

            //选择考核区域
            $scope.chooseRegion = function (item) {
                if (item == null || item == ""){
                    ynuiNotification.error({msg: '请先选择打分表'});
                    return;
                }
                $scope.checkType = 0;
                //加载打分区域(管理员的考核区)
                $http.post( basePath+ "/getAreaAndNumber.htm?id="+item+"&checkType="+$scope.checkType, {}).success(function (data) {
                    if (data.status == 0) {
                        $scope.selectAreaAdmin = data.result;
                        // $scope.regionList.show();
                    }
                });
                $scope.checkType = 1;
                //加载打分区域(我的考核区)
                $http.post( basePath+ "/getAreaAndNumber.htm?id="+item+"&checkType="+$scope.checkType, {}).success(function (data) {
                    if (data.status == 0) {
                        $scope.selectAreaMy = data.result;
                        $scope.selectArea = data.result;
                        // $scope.regionList.show();
                    }
                });
            };

            $scope.showRegion = function(){
                if ($scope.taskConditions.scoreTable == null || $scope.taskConditions.scoreTable == ""){
                    ynuiNotification.error({msg: '请先选择打分表'});
                    return;
                }
                $scope.regionList.show();
            };
            $scope.confirmRegion = function (item) {
                $scope.taskConditions.checkRegion = item.id;
                $scope.showTask.checkRegionName = item.name;
                $scope.hideRegion();
            };

            //隐藏考核区域
            $scope.hideRegion = function() {
                $scope.regionList.hide();
            };

            //选择考核次数
            $scope.chooseNember = function (item) {
                if (item == null || item == ""){
                    ynuiNotification.error({msg: '请先选择打分表'});
                    return;
                }
                $scope.numberList.show();
            };

            $scope.getNumber = function (item) {
                //选择考核次数
                $http.post(basePath+"/queryByScoreTableId.htm?id="+item, {}).success(function (data) {
                    if (data.status == 0) {
                        $scope.selectFrequency = data.result;
                        if ($scope.taskConditions.checkNumber == "" && $scope.selectFrequency.length > 0) {
                            $scope.taskConditions.checkNumber = $scope.selectFrequency[0].id;
                            $scope.showTask.checkNumberName = $scope.selectFrequency[0].name;
                        }
                    }
                });
            };

            $scope.confirmFrequency = function (item) {
                $scope.taskConditions.checkNumber = item.id;
                $scope.showTask.checkNumberName = item.name;
                $scope.hideNumber();
            };

            //隐藏考核次数
            $scope.hideNumber = function() {
                $scope.numberList.hide();
            };

            //获取当前学期
            $scope.getCurrentTerm = function () {
                $http.post(basePath+"/getCurrentTerm.htm", {}).success(function (data) {
                    if (data.status == 0) {
                        $scope.currentTerm = data.result;
                        $scope.taskConditions.term = $scope.currentTerm.id;
                        $scope.showTask.termName = $scope.currentTerm.name;
                    }
                });
            };
            $scope.getCurrentTerm();

            $scope.chooseTerm = function () {
                //选择考核学期
                $scope.termList.show();
            };

            $scope.getTermList = function () {
                //选择考核学期
                $http.post(basePath + "/getSelectPlatformSysTerm.htm", {}).success(function (data) {
                    if (data.status == 0 && data.result.length > 0) {
                        $scope.selectPlatformSysTerm = data.result;
                        if ($scope.taskConditions.term == "" && $scope.selectPlatformSysTerm.length > 0){
                            $scope.taskConditions.term = $scope.selectPlatformSysTerm[0].id;
                            $scope.showTask.termName = $scope.selectPlatformSysTerm[0].name;
                            $scope.taskConditions.checkDate = $scope.selectPlatformSysTerm[0].termEndDateString;
                        }
                    }
                });
            };
            $scope.getTermList();
            $scope.getTaskUrl = function () {
                var url = basePath + "/saveAndCreateMoraleduDormitoryCheckTask.htm?";
                if($scope.taskConditions.term != "" && $scope.taskConditions.term.length > 0){
                    url += "&termId="+$scope.taskConditions.term;
                }
                if($scope.taskConditions.scoreTable != "" && $scope.taskConditions.scoreTable.length > 0){
                    url += "&scoreTableId="+$scope.taskConditions.scoreTable;
                    url += "&scoreTableName="+$scope.showTask.scoreTableName;
                }
                if($scope.taskConditions.checkRegion != "" && $scope.taskConditions.checkRegion.length > 0){
                    url += "&DormitoryRegionId="+$scope.taskConditions.checkRegion;
                }
                if($scope.taskConditions.checkDate != "" && $scope.taskConditions.checkDate.length > 0){
                    url += "&checkDate="+$scope.taskConditions.checkDate;
                }
                if($scope.taskConditions.checkNumber != ""){
                    url += "&checkMember="+$scope.taskConditions.checkNumber;
                }
                return url;
            };

            $scope.confirmTerm = function (item) {
                $scope.taskConditions.term = item.id;
                $scope.showTask.termName = item.name;
                $scope.hideTerm();
            };

            //隐藏考核次数
            $scope.hideTerm = function() {
                $scope.termList.hide();
            };

            //切换考核区域
            $scope.chengeArea = function (item) {
                $scope.checkType = item;
                if (item == 1){
                    $scope.selectArea = $scope.selectAreaMy;
                }else if(item == 0){
                    $scope.selectArea = $scope.selectAreaAdmin;
                }
                // $scope.chooseRegion($scope.taskConditions.scoreTable);
            };

            //取消添加任务
            $scope.closeAddTask = function () {
                $location.path("/dormitory_examine");
            };

            //生成考核任务
            $scope.generateTask = function () {
                if($scope.taskConditions.scoreTable ==null || $scope.taskConditions.scoreTable ==""){
                    ynuiNotification.error({msg: '请选择打分表！'});
                    return;
                }
                if($scope.taskConditions.checkRegion == null || $scope.taskConditions.checkRegion == ""){
                    ynuiNotification.error({msg: '请选择考核区域！'});
                    return;
                }
                if($scope.taskConditions.checkNumber ==null || $scope.taskConditions.checkNumber == ""){
                    ynuiNotification.error({msg: '请选择考核次数！'});
                    return;
                }
                if($scope.taskConditions.term ==null || $scope.taskConditions.term == ""){
                    ynuiNotification.error({msg: '请选择学期！'});
                    return;
                }
                if($scope.taskConditions.checkDate ==null || $scope.taskConditions.checkDate == ""){
                    ynuiNotification.error({msg: '请选择考核日期！'});
                    return;
                }
                var url = $scope.getTaskUrl();
                $http.post(url, {}).success(function (data) {
                    if (data.status == 0 && data.result == null) {
                        //任务进度
                        ynuiNotification.success({msg: '添加成功！'});
                        $location.path("/dormitory_examine");
                    }else{
                        ynuiNotification.error({msg: '添加失败！'+data.result.failedReason});
                        $scope.taskConditions.checkDate = currentDate;
                    }
                });
            };

            $scope.cleanData = function () {
                //添加考核任务
                $scope.taskConditions.scoreTable="";
                $scope.taskConditions.checkRegion="";
                $scope.taskConditions.checkNumber="";
                $scope.taskConditions.checkDate = date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
                $scope.getCurrentTerm();
                //添加考核任务显示名称
                $scope.showTask.scoreTableName="";
                $scope.showTask.checkRegionName="";
                $scope.showTask.checkNumberName="";
                $scope.getCurrentTerm();
            };

            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.cleanData();
                $scope.getData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.doBack = function () {
                history.back(-1);
            };
        }])
    //宿舍考核填写表单
    .controller('fromCtrl', ['$scope','$rootScope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location","$ionicModal","$ionicPopup","$cordovaBarcodeScanner",
        function ($scope,$rootScope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location,$ionicModal,$ionicPopup,$cordovaBarcodeScanner) {
            /**
             * 考核项目
             */
            var item = angular.fromJson($location.search().checkItem);

            $scope.showInfo = "扣分";
            /**
             * 一级路径配置
             * @type {string}
             */
            var basePath = originBaseUrl+ "/third/dormitoryExamine";
            /**
             * 默认不上传
             * @type {boolean}
             */
            $scope.showUploader = false;

            /**
             *
             * @type {Array}
             */
            $scope.selectStudent = [];
            /**
             * 悬浮侧
             */
            $ionicModal.fromTemplateUrl('student.html', {
                scope: $scope,
                animation: 'slide-in-left'
            }).then(function (modal) {
                $scope.stuList = modal;
            });
            /**
             * 是否在页面上做出编辑
             * @type {boolean}
             */
            $scope.isHaveChange = false;
            /**
             * 已经选择了的学生信息
             * @type {Array}
             */
            $scope.selectStudent = [];

            /**
             * 文件上传选项
             * @type {{}}
             */
            $scope.options = {

            };
            /**
             * B0029103
             * 全局验证规则
             * @type {RegExp}
             */
            var reg = /^([1-9]\d{0,6}|0)(.[0-9]{1,2})?$/;
            /**
             * 是否可以保存
             * 手机端验证慢的问题
             * @type {boolean}
             */
            $scope.canSave = true;
            /**
             * 文件信息
             * @type {Array}
             */
            $scope.fileIds = [];
            /**
             * 获取详情
             * @param taskId
             */
            $scope.getDormitoryScoreDetail = function (func) {
                $ionicLoading.show({
                    template: '正在加载...'
                });
                $http.get(basePath + "/queryDormitoryChDetailItem.htm?taskId=" + item.taskId +
                "&dormitoryId=" + item.dormitory + "&typeId=" + item.typeId
                +"&ItemId=" + item.ItemId + "&checkNumber=" + item.checkMember + "&_datatime=" + new Date().getTime()).success(function (data) {
                    if (func) {
                        func();
                    }
                    $scope.showUploader = true;
                    if (data.status == 0) {
                        $scope.MoraleduDormitoryCheckDetailVO = data.result;
                        if(!$scope.MoraleduDormitoryCheckDetailVO.score){
                            $scope.MoraleduDormitoryCheckDetailVO.score = 0;
                        }
                        $scope.scoreTypeName = $scope.MoraleduDormitoryCheckDetailVO.scoreType == 0?"总扣分":"总加分";
                        if($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs == null){
                            $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs = [];
                        }
                        if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 0){
                            $scope.MoraleduDormitoryCheckDetailVO.lastScore =sub(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore) , parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                        }else{
                            $scope.MoraleduDormitoryCheckDetailVO.lastScore = add(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore), parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                        }

                        /* 班级信息*/
                        if(null == $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs){
                            $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs = [];
                        }
                        /*班级的分数*/
                        var avg = div($scope.MoraleduDormitoryCheckDetailVO.score,$scope.MoraleduDormitoryCheckDetailVO.platformSysAdminClassVOs.length );
                        angular.forEach($scope.MoraleduDormitoryCheckDetailVO.platformSysAdminClassVOs,function(clazz){
                            var has = false;
                            angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs,function(ckeckedClass){
                                if(ckeckedClass.platformSysAdminClassId == clazz.id){
                                    has = true;
                                }
                            });
                            if(!has){
                                $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs.push({
                                    platformSysAdminClassId:clazz.id,
                                    platformSysAdminClassName:clazz.name,
                                    scoreType:$scope.MoraleduDormitoryCheckDetailVO.scoreType,
                                    score:avg.toFixed(2)
                                })
                            }
                        });
                        /*附件信息*/
                        $scope.fileIds  = [];
                        angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailFileVOs,function(fileInfo){
                            $scope.fileIds.push(fileInfo.fastDfSId);
                        });
                        $scope.options.initBackFile($scope.fileIds);
                        $scope.$watch(function(){
                            return $scope.MoraleduDormitoryCheckDetailVO.remark;
                        },function(newValue,oldValue){
                            if(newValue != oldValue){
                                $scope.isHaveChange = true;
                            }
                        });
                    } else {
                        ynuiNotification.error({msg: '获取宿舍信息失败！'});
                    }
                }).error(function (data) {
                    if (func) {
                        func();
                    }
                    ynuiNotification.error({msg: '获取宿舍信息失败！'});
                });
            };

            /**
             * 改变分数类型
             * @param index 1 加分 0 减分
             */
            $scope.changeScoreType = function(index){
                $scope.isHaveChange = true;
                $scope.MoraleduDormitoryCheckDetailVO.scoreType = index;
                $scope.MoraleduDormitoryCheckDetailVO.score = 0;
                $scope.totalScoreDeductDisable = false;
                $scope.totalScoreAddDisable = false;
                $scope.scoreTypeName = $scope.MoraleduDormitoryCheckDetailVO.scoreType == 0?"总扣分":"总加分";
                angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs,function(clazz){
                    clazz.scoreType =  $scope.MoraleduDormitoryCheckDetailVO.scoreType;
                    clazz.score = 0;
                    clazz.totalScoreDeductDisable = false;
                    clazz.totalScoreAddDisable = false;
                });
                /*计算显示的总分*/
                if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 0){
                    $scope.showInfo = "扣分";
                    $scope.MoraleduDormitoryCheckDetailVO.lastScore =sub(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore) , parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                }else{
                    $scope.showInfo = "加分";
                    $scope.MoraleduDormitoryCheckDetailVO.lastScore = add(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore), parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                }
            };

            /**
             * 扫描二维码 js二维码扫描乱码，但是今后还是会使用，所以不要删除了
             */
            // $scope.scanCode = function () {
            //     $cordovaBarcodeScanner.scan().then(function (barcodeData) {
            //         /*
            //          楼栋：ch女
            //          寝室：ch女101
            //          床位：1
            //          */
            //         $scope.number = barcodeData.text;
            //         var str = $scope.number.split(/\r?\n/); //楼栋  寝室 床位
            //         if($scope.MoraleduDormitoryCheckDetailVO.buildCode == str[0]){
            //             if($scope.MoraleduDormitoryCheckDetailVO.roomCode == str[1]){
            //                 var hasCodeStudent = false;
            //                 angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryStuInfoVOsmoraleduDormitoryStuInfoVOs,function(stu){
            //                     if(stu.bedCode == str[2]){
            //                         hasCodeStudent = true;
            //                         $scope.isHaveChange = true;
            //                         if($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs == null ){
            //                             $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs = [];
            //                         }
            //                         var save = true;
            //                         angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs,function(saveStuden){
            //                             if(saveStuden.id == stu.id){
            //                                 save = false;
            //                             }
            //                         });
            //                         if(save){
            //                             $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs.push(stu);
            //                         }
            //                     }
            //                 });
            //                 if(!hasCodeStudent){
            //                     ynuiNotification.error({msg: "扫描"+ str[2] +"未入住学生！!"});
            //                 }
            //             }else{
            //                 ynuiNotification.error({msg: "扫描"+ str[1] +"不是考核寝室!"});
            //             }
            //         }else{
            //             ynuiNotification.error({msg: " 扫描寝室"+ str[0] + "不是考核寝室楼栋!"});
            //         }
            //
            //     }, function (error) {
            //         ynuiNotification.error({msg: "扫描失败！"});
            //     });
            // };

            $scope.scanCode = function () {
                $cordovaBarcodeScanner.scan().then(function (barcodeData) {
                    $scope.number = barcodeData.text;
                    if(!$scope.number){return false;}
                    $http.get(basePath + "/quertCodeDormitoryById.htm?dormitoryBedId=" + $scope.number + "&_datatime=" + new Date().getTime()).success(function (data) {
                        $ionicLoading.hide();
                        if (data.status == 0) {
                            $scope.moraleduDormitoryBedCodeInfoVO = data.result;
                            if($scope.MoraleduDormitoryCheckDetailVO.buildCode == $scope.moraleduDormitoryBedCodeInfoVO.buildCode){
                                if($scope.MoraleduDormitoryCheckDetailVO.roomCode == $scope.moraleduDormitoryBedCodeInfoVO.roomCode){
                                    var hasCodeStudent = false;
                                    angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryStuInfoVOs,function(stu){
                                        if(stu.bedCode == $scope.moraleduDormitoryBedCodeInfoVO.bedCode){
                                            hasCodeStudent = true;
                                            $scope.isHaveChange = true;
                                            if($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs == null ){
                                                $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs = [];
                                            }
                                            var save = true;
                                            angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs,function(saveStuden){
                                                if(saveStuden.id == stu.id){
                                                    save = false;
                                                }
                                            });
                                            if(save){
                                                $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs.push(stu);
                                            }
                                        }
                                    });
                                    if(!hasCodeStudent){
                                        ynuiNotification.error({msg: "扫描"+ $scope.moraleduDormitoryBedCodeInfoVO.bedCode +"未入住学生！!"});
                                    }
                                }else{
                                    ynuiNotification.error({msg: "扫描"+ $scope.moraleduDormitoryBedCodeInfoVO.roomCode +"不是考核寝室!"});
                                }
                            }else{
                                ynuiNotification.error({msg: " 扫描寝室"+ $scope.moraleduDormitoryBedCodeInfoVO.buildCode + "不是考核寝室楼栋!"});
                            }
                        } else {
                            ynuiNotification.error({msg: '获取学生床位信息失败！'});
                        }
                    }).error(function (data) {
                        ynuiNotification.error({msg: '获取学生床位信息失败！'});
                    });
                }, function (error) {
                    ynuiNotification.error({msg: "扫描失败！"});
                });
            };
            /**
             * 总分数
             * 加分或者减分
             * @param index 1 加分 -1 减分
             */
            $scope.addScore = function(index){
                $scope.isHaveChange = true;
                var score = $scope.MoraleduDormitoryCheckDetailVO.score;
                if(index == 1){
                    score = add($scope.MoraleduDormitoryCheckDetailVO.score,0.1);
                }else{
                    score = sub($scope.MoraleduDormitoryCheckDetailVO.score,0.1);
                }
                var disableButton = function(index){
                    if(index == 1){
                        $scope.totalScoreAddDisable = true;
                    }else{
                        $scope.totalScoreDeductDisable = true;
                    }
                };
                var notification = function(message){
                    if(index == 1){
                        if(!$scope.totalScoreAddDisable){
                            ynuiNotification.error({msg:message});
                        }
                    }
                    if(index == 0){
                        if(!$scope.totalScoreDeductDisable){
                            ynuiNotification.error({msg:message});
                        }
                    }
                };
                /*验证*/
                if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 1){
                    if(score < $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin){
                        notification('不能小于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin +'分');
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin;
                        disableButton(index);
                        return false;
                    } else if(score > $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd){
                        notification('不能大于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd +'分');
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd;
                        disableButton(index);
                        return false;
                    }
                }else {
                    if (score < $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin) {
                        notification('不能小于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin + '分');
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin;
                        disableButton(index);
                        return false;
                    }else if (score > $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd) {
                        notification('不能大于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd + '分');
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd;
                        disableButton(index);
                        return false;
                    }
                }
                $scope.totalScoreDeductDisable = false;
                $scope.totalScoreAddDisable = false;
                $scope.MoraleduDormitoryCheckDetailVO.score = score;
                if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 0){
                    $scope.MoraleduDormitoryCheckDetailVO.lastScore = sub(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore) , parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                }else{
                    $scope.MoraleduDormitoryCheckDetailVO.lastScore = add(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore) , parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                }
                $scope.getClassScore();

            };

            /**
             *  获取每一个班级的数据
             */
            $scope.getClassScore = function(){
                var avg = div($scope.MoraleduDormitoryCheckDetailVO.score,$scope.MoraleduDormitoryCheckDetailVO.platformSysAdminClassVOs.length );
                angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs,function(clazz){
                    clazz.score =  avg.toFixed(2);
                    clazz.totalScoreDeductDisable = false;
                    clazz.totalScoreAddDisable = false;
                });
                $scope.totalScoreDeductDisable = false;
                $scope.totalScoreAddDisable = false;
            };

            /**
             * 手动输入的时候
             */
            $scope.changeScore = function(index){
                $scope.isHaveChange = true;

                if(!isNotBlank($scope.MoraleduDormitoryCheckDetailVO.score)){
                    $scope.MoraleduDormitoryCheckDetailVO.score = 0;
                    angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs,function(clazz){
                        clazz.score =  0;
                    });
                    return false;
                }
                if(isNotBlank($scope.MoraleduDormitoryCheckDetailVO.score) && isNaN(parseFloat($scope.MoraleduDormitoryCheckDetailVO.score)) || !reg.test($scope.MoraleduDormitoryCheckDetailVO.score)){
                    $scope.MoraleduDormitoryCheckDetailVO.score = 0;
                    angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs,function(clazz){
                        clazz.score =  0;
                    });
                    if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 1){
                        ynuiNotification.error({msg: '只能输入'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin +'至' + $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd +'的分数'});
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin;
                    }else {
                        ynuiNotification.error({msg: '只能输入' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin +'至' +  $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd+'的分数'});
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin;
                    }
                    return false;
                }
                if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 1){
                    if($scope.MoraleduDormitoryCheckDetailVO.score < $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin){
                        ynuiNotification.error({msg: '宿舍分数不能小于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin +'分'});
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin;
                    } else if($scope.MoraleduDormitoryCheckDetailVO.score > $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd){
                        ynuiNotification.error({msg: '宿舍分数不能大于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd +'分'});
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd;
                    }
                }else {
                    if ($scope.MoraleduDormitoryCheckDetailVO.score < $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin) {
                        ynuiNotification.error({msg: '宿舍分数不能小于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin + '分'});
                        $scope.MoraleduDormitoryCheckDetailVO.score = $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin;
                    }else if ($scope.MoraleduDormitoryCheckDetailVO.score > $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd) {
                        ynuiNotification.error({msg: '宿舍分数不能大于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd + '分'});
                        $scope.MoraleduDormitoryCheckDetailVO.score =   $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd;
                    }
                }
                if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 0){
                    $scope.MoraleduDormitoryCheckDetailVO.lastScore = sub(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore) , parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                }else{
                    $scope.MoraleduDormitoryCheckDetailVO.lastScore = add(parseFloat($scope.MoraleduDormitoryCheckDetailVO.baseScore) , parseFloat($scope.MoraleduDormitoryCheckDetailVO.score));
                }
                if(index == undefined){
                    $scope.getClassScore();
                }
            };

            /**
             * 监控显示
             */
            $scope.changeClazzScoreChange = function(){
                $scope.isHaveChange = true;
            };
            /**
             * 手动输入班级的分数验证规则
             * @param clazz
             */
            $scope.changeClazzScore = function(clazz,index){
                var avg = div($scope.MoraleduDormitoryCheckDetailVO.score,$scope.MoraleduDormitoryCheckDetailVO.platformSysAdminClassVOs.length );
                $scope.isHaveChange = true;
                var isError = false;
                if(isNotBlank(clazz.score) && (isNaN(parseFloat(clazz.score)) ||!reg.test(clazz.score))){
                    if(clazz.scoreType == 1){
                        ynuiNotification.error({msg: '只能输入'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin + "至" + $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd + '的分数'});
                    }else {
                        ynuiNotification.error({msg: '只能输入'+ $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin + "至" + $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd + '的分数'});
                    }
                    $scope.canSave = false;
                    isError = true;
                    if(index == undefined){
                        clazz.score = avg;
                    }
                }
                if(isNotBlank(clazz.score) && !isNaN(parseFloat(clazz.score))){
                    if(clazz.scoreType == 1){
                        if(clazz.score < $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin){
                            ynuiNotification.error({msg: '班级分数不能小于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin +'分'});
                            if(index == undefined){
                                clazz.score = avg;
                            }
                            $scope.canSave = false;
                            isError = true;
                        } else if(clazz.score > $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd){
                            ynuiNotification.error({msg: '班级分数不能大于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd +'分'});
                            isError = true;
                            $scope.canSave = false;
                            if(index == undefined){
                                clazz.score = avg;
                            }
                        }
                    }else {
                        if (clazz.score < $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin) {
                            ynuiNotification.error({msg: '班级分数不能小于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin + '分'});
                            if(index == undefined){
                                clazz.score = avg;
                            }
                            isError = true;
                            $scope.canSave = false;
                        }else if (clazz.score > $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd) {
                            ynuiNotification.error({msg: '班级分数不能大于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd + '分'});
                            if(index == undefined){
                                clazz.score = avg;
                            }
                            isError = true;
                            $scope.canSave = false;
                        }
                    }
                }
                if(isError){
                    window.setTimeout(function(){
                        $scope.canSave = true;
                    },400);
                }
                return isError;
            };

            /**
             * 班级分数
             * @param index 0 减分 1 加分
             */
            $scope.addClassScore = function(index,checkItem){
                $scope.isHaveChange = true;
                var score = 0;
                if(index == 1){
                    score = add(checkItem.score,0.1);
                }else{
                    score = sub(checkItem.score,0.1);
                }
                /*禁止使用按钮*/
                var disableButton = function(index){
                    if(index == 1){
                        checkItem.totalScoreAddDisable = true;
                    }else{
                        checkItem.totalScoreDeductDisable = true;
                    }
                };
                /*提示信息管理*/
                var notification = function(message){
                    if(index == 1){
                        if(!checkItem.totalScoreAddDisable){
                            ynuiNotification.error({msg:message});
                        }
                    }
                    if(index == 0){
                        if(!checkItem.totalScoreDeductDisable){
                            ynuiNotification.error({msg:message});
                        }
                    }
                };
                if($scope.MoraleduDormitoryCheckDetailVO.scoreType == 1){
                    if(score < $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin){
                        notification('不能小于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreBegin +'分');
                        disableButton(index);
                        return false;
                    }else  if(score > $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd){
                        notification('不能大于'+ $scope.MoraleduDormitoryCheckDetailVO.addScoreEnd +'分');
                        disableButton(index);
                        return false;
                    }
                }else {
                    if (score < $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin) {
                        notification('不能小于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreBegin + '分');
                        disableButton(index);
                        return false;
                    }
                    if (score > $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd) {
                        notification('不能大于' + $scope.MoraleduDormitoryCheckDetailVO.deductScoreEnd + '分');
                        disableButton(index);
                        return false;

                    }
                }
                checkItem.score = score;
                checkItem.totalScoreDeductDisable = false;
                checkItem.totalScoreAddDisable = false;
            };

            /**
             * 选择学生
             * @param stu
             */
            $scope.selectStu = function(stu){
                stu.select = !stu.select;
                var index = -1;
                if(stu.select){
                    $scope.selectStudent.push(stu);
                }else{
                    angular.forEach($scope.selectStudent,function(student,ind){
                        if(stu.id == student.id){
                            index = ind;
                        }
                    })
                }
                if(index != -1){
                    $scope.selectStudent.splice(index,1);
                }
            };

            /**
             * 选择学生调用
             */
            $scope.selectStudentList = function(){
                $scope.selectStudent = [];
                angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryStuInfoVOs,function(stu){
                    stu.select = false;
                });
                angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs,function(student){
                    angular.forEach($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryStuInfoVOs,function(stu){
                        if(student.id == stu.id){
                            stu.select = true;
                            $scope.selectStudent.push(stu);
                        }
                    })
                });
                $scope.stuList.show();
            };

            /**
             * 完成学生的选择
             */
            $scope.finishSelectSu = function(){
                $scope.isHaveChange = true;
                if($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs == null ){
                    $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs = [];
                }
                $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs = [];
                angular.forEach($scope.selectStudent,function(student,ind){
                    $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailStulItemVOs.push(student);
                });
                $scope.closeName();
            };

            /**
             * 关闭选择学生的悬浮侧
             */
            $scope.closeName = function(){
                $scope.stuList.hide();
            };

            /**
             * 返回列表
             */
            $scope.backItemLists = function(){
                if($scope.isHaveChange){
                    var confirmPopup = $ionicPopup.confirm({
                        title:"提示",
                        template: '已编辑考核内容确定要放弃吗？',
                        cancelText:"取消",
                        okText:"确认"
                    });
                    confirmPopup.then(function(res) {
                        if(res) {
                            $rootScope.dormitoryPage = {
                                returnPage:1
                            };
                            history.back(-1);
                        }
                    });
                }else{
                    $rootScope.dormitoryPage = {
                        returnPage:1
                    };
                    history.back(-1);
                }

            };

            /**
             * 文件上传
             * @param data
             */
            $scope.getDfsIds = function (data) {
                $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailFileVOs = [];
                if($scope.fileIds == null || $scope.fileIds == undefined){
                    $scope.fileIds = [];
                }
                angular.forEach(data.data,function(fileId){
                    $scope.fileIds.push(fileId);
                    $scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailFileVOs.push({
                        fastDfSId:fileId,
                        fileName:""
                    });
                });
            };


            /**
             * 保存
             */
            $scope.saveScoreDetail = function () {
                var obj = {};
                if($scope.changeScore(1)){
                    return false;
                }
                var classIsErr = false;
                for(var i in $scope.moraleduDormitoryCheckDetailClassItemVOs){
                    var clazz = $scope.moraleduDormitoryCheckDetailClassItemVOs[i];
                    if($scope.changeClazzScore(clazz,1)){
                        classIsErr = true;
                    }
                }
                if(classIsErr){
                    return false;
                }
                if(!$scope.canSave){
                    return false;
                }
                obj = angular.copy($scope.MoraleduDormitoryCheckDetailVO);
                /*删除不需要的数据*/
                delete obj.platformSysAdminClassVOs;
                delete obj.moraleduDormitoryStuInfoVOs;
                delete obj.remarks;
                var emptyClassIndex = [];

                angular.forEach(obj.moraleduDormitoryCheckDetailClassItemVOs, function (clazz,index) {
                    if (!isNotBlank(clazz.score)) {
                        emptyClassIndex.push(index);
                    }
                });
                angular.forEach(emptyClassIndex, function (index) {
                    obj.moraleduDormitoryCheckDetailClassItemVOs.splice(index,1);
                });
                if($scope.MoraleduDormitoryCheckDetailVO.moraleduDormitoryCheckDetailClassItemVOs.length > 0 &&  obj.moraleduDormitoryCheckDetailClassItemVOs.length == 0){
                    ynuiNotification.error({msg: '请输入班级的得分！'});
                    return false;
                }
                obj.scoreUserId = $rootScope.authorizationStr.userId;
                $http.post(basePath + "/saveMoraleduDormitoryCheckDetailByMobile.htm", parseParamForSpringMVC(obj)).success(function (data) {
                    if (data.status == 0) {
                        //任务进度
                        ynuiNotification.success({msg: '添加成功！'});
                        //$location.path("/grade_dormitory");
                        $rootScope.dormitoryPage = {
                            returnPage:1
                        };
                        history.back(-1);
                    } else {
                        ynuiNotification.error({msg: '添加失败！'});
                    }
                })
            };

            /**
             * js浮点数计算不准确计算解决
             * @param a
             * @param b
             * @returns {number}
             */
            var mul=function(a, b) {
                var c = 0,
                    d = a.toString(),
                    e = b.toString();
                try {
                    c += d.split(".")[1].length;
                } catch (f) {}
                try {
                    c += e.split(".")[1].length;
                } catch (f) {}
                return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
            };

            /**
             * 加法
             * @param a
             * @param b
             */
            function add(a, b) {
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

            /**
             * 剪发
             * @param a
             * @param b
             */
            function sub(a, b) {
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
             * 乘法
             * @param a
             * @param b
             * @returns {number}
             */
            function mul(a, b) {
                var c = 0,
                    d = a.toString(),
                    e = b.toString();
                try {
                    c += d.split(".")[1].length;
                } catch (f) {}
                try {
                    c += e.split(".")[1].length;
                } catch (f) {}
                return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
            }

            /**
             * 触发
             * @param a
             * @param b
             */
            function div(a, b) {
                var c, d, e = 0,
                    f = 0;
                try {
                    e = a.toString().split(".")[1].length;
                } catch (g) {}
                try {
                    f = b.toString().split(".")[1].length;
                } catch (g) {}
                return c = Number(a.toString().replace(".", "")), d = Number(b.toString().replace(".", "")), mul(c / d, Math.pow(10, f - e));
            }

            /**
             *  字符
             * @param str
             * @returns {boolean}
             */
            var isNotBlank = function (str){
                if(null == str){
                    return false;
                }
                if(/\S+/.test(str)){
                    return true;
                }
                return false;
            };

            /**
             * 数据结构改变
             * @param obj
             * @returns {{}}
             */
            var parseParamForSpringMVC = function (obj) {
                //最终组合成功的对象
                var resultObj = {};
                //递归方法，用于深度转换每一个参数
                //key 对象时，是属性名
                //value 属性值
                //prefix（关键），和key进行组合相组合成想要的格式类型如name.age（对象）或name[0].age（数组）
                var deepParseParams = function (key, value, prefix) {
                    //先判断是否是数组
                    if (value instanceof Array) {
                        for (var i in value) {
                            deepParseParams("", value[i], prefix + key + "[" + i + "]");
                        }
                    }
                    //再判断是否是对象
                    else if (value instanceof Object) {
                        for (var i in value) {
                            deepParseParams("." + i, value[i], prefix + key);
                        }
                    }
                    //如果不是数组或对象，到了此次递归的最后一次，将完成组合的这条最终数据放在最终组合对象中
                    else {
                        resultObj[prefix + key] = value;
                    }

                };
                //因为传入的转换参数必须是对象,而且第一次传入和第二次开始组合“.”号是很特殊的地方，所有
                //第一次单独循环
                for (var i in obj) {
                    deepParseParams("", obj[i], i);
                }

                //返回转换成功的对象集合
                return resultObj;
            };
            /**
             * 开始
             */
            $scope.getDormitoryScoreDetail(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });

        }])
    //宿舍考核填写表单
    .controller('gradeCtrl', ['$scope','$rootScope' ,'$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location","$ionicModal","$filter",
        function ($scope, $rootScope,$http, $timeout, $ionicLoading, $ionicScrollDelegate, ynuiNotification, $location, $ionicModal,$filter) {

            /**
             * 当前考核的任务
             */
            var task = angular.fromJson($location.search().task);

            if(task.notOverClassNumber == 0){
                if($rootScope.dormitoryPage && $rootScope.dormitoryPage.returnPage == 1){
                    task.notOverClassNumber = 1;
                    $rootScope.dormitoryPage.returnPage = null;
                }
            }
            /**
             * 任务ID
             */
            var taskId = task.id;

            /**
             * 一级路径配置
             * @type {string}
             */
            var basePath = originBaseUrl+ "/third/dormitoryExamine";

            /**
             * 查询考核项目的选择框
             * @type {string}
             */
            $scope.MoraleduDormitoryChInfoVO = null;

            /**
             *  查询条件 考核项目
             * @type {string}
             */
            $scope.ItemName = "";
            /**
             * 上一条和下一条数据信息
             * @type {{region: string, dormitory: string}}
             */
            $scope.condition = {
                checkMember:task.checkMember,//考核次数
                checkDate:task.checkDate, //考核日期
                region:task.dormitoryRegionId,//区域信息
                regionName:"",
                dormitory: "",//考核寝室
                lastRegion: "",
                nextRegion: "",
                lastDormitory: "",
                nextDormitory: "",
                dormitoryName:""
            };

            /**
             * 清除条件
             */
            $scope.clearAllDormitoryInfo = function () {
                $scope.condition.nextDormitory = null;
                $scope.condition.nextDormitoryName = null;
                $scope.condition.lastDormitory = null;
            };

            /**
             * 悬浮侧
             */
            $ionicModal.fromTemplateUrl('grouping.html', {
                scope: $scope,
                animation: 'slide-in-up'
            }).then(function (modal) {
                $scope.classList = modal;
            });
            /**
             * 打开选择班级的悬浮
             */
            $scope.openDormiGrouping = function(){
                $scope.doRefreshClass(1);
                $ionicLoading.hide();
                $scope.classList.show();
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
            };
            /**
             * 获取寝室
             * @param type 0  上一个 1 下一个
             * @param index undefined 为正确跳转  ！undefined为非正确跳转 没有了需要显示选择班级的悬浮侧
             */
            $scope.getDormitory = function (type,index) {
                $scope.clearAllDormitoryInfo();
                var currentIndex = 0, lastIndex = 0, nextIndex = 0;
                if ($scope.dormitoryList.length > 0) {
                    for (var i = 0; i < $scope.dormitoryList.length; i++) {
                        var item = $scope.dormitoryList[i];
                        if (item.id == $scope.condition.dormitory) {
                            if (type == 0) {
                                lastIndex = i - 2;
                                currentIndex = i - 1;
                                nextIndex = i;
                            } else if (type == 1) {
                                lastIndex = i;
                                currentIndex = i + 1;
                                nextIndex = i + 2;
                            }
                        }
                    }
                    /*当前为最后一个*/
                    if (nextIndex > $scope.dormitoryList.length) {
                        if(angular.isUndefined(index)){
                            $scope.condition.nextDormitory = null;
                            $scope.condition.nextDormitoryName = null;
                            ynuiNotification.error({msg: '没有了'});
                            return false;
                        }else{
                            $scope.openDormiGrouping();
                            return false;
                        }
                    }
                    /*当前为第一个*/
                    if (lastIndex == -2) {
                        $scope.condition.lastDormitory = null;
                        ynuiNotification.error({msg: '没有了'});
                        return false;
                    }
                    /*在中间的时候*/
                    if (lastIndex >=0) {
                        $scope.condition.lastDormitory = $scope.dormitoryList[lastIndex].id;
                    }
                    if (nextIndex < $scope.dormitoryList.length) {
                        $scope.condition.nextDormitory = $scope.dormitoryList[nextIndex].id;
                        $scope.condition.nextDormitoryName = $scope.dormitoryList[nextIndex].name;
                    }
                    $scope.condition.dormitory = $scope.dormitoryList[currentIndex].id;
                    $scope.getDormitoryScoreDetail(taskId);
                }
            };

            /**
             * 获取下一个区域
             * @param type
             */
            $scope.toLastOrNextRegion = function(type){
                $scope.clearAllDormitoryInfo();
                var currentIndex = 0, lastIndex = 0, nextIndex = 0;
                if ($scope.regionList.length > 0) {
                    for (var i = 0; i < $scope.regionList.length; i++) {
                        var item = $scope.regionList[i];
                        if (item.id == $scope.condition.region) {
                            if (type == 0) {
                                lastIndex = i - 2;
                                currentIndex = i - 1;
                                nextIndex = i;
                            } else if (type == 1) {
                                lastIndex = i;
                                currentIndex = i + 1;
                                nextIndex = i + 2;
                            }
                        }
                    }
                    /*当前为最后一个*/
                    if (nextIndex > $scope.regionList.length) {
                        $scope.condition.nextRegion = null;
                        ynuiNotification.error({msg: '已经是最后1个区域或分组！'});
                        return false;
                    }
                    /*当前为第一个*/
                    if (lastIndex ==-2) {
                        $scope.condition.lastRegion = null;
                        ynuiNotification.error({msg: '已经是第1个区域或分组！'});
                        return false;
                    }
                    /*在中间的时候*/
                    if (lastIndex >=0) {
                        $scope.condition.lastRegion = $scope.regionList[lastIndex].id;
                    }
                    if (nextIndex < $scope.regionList.length) {
                        $scope.condition.nextRegion = $scope.regionList[nextIndex].id;
                    }
                    $scope.condition.region = $scope.regionList[currentIndex].id;
                    $scope.condition.regionName = $scope.regionList[currentIndex].name;
                    $scope.getDormitoryList(function () {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                    },1);
                }
            };

            /**
             * 手机端 查询考核下的考核项目
             * @param taskId
             * @param dormitoryId
             */
            $scope.closeDataClick = function(item){
               if(item == $scope.condition.dormitoryName){
                   $scope.condition.dormitoryName="";
               }else if(item == $scope.ItemName){
                   $scope.ItemName="";
               }
            };
            $scope.getDormitoryScoreDetail = function (taskId) {
                $scope.MoraleduDormitoryChInfoVO = [];
                $ionicLoading.show({
                    template: '正在加载...'
                });
                $http.get(basePath + "/querDormitoryCheckItemByTaskAndDormId.htm?taskId=" + taskId +
                "&dormitoryId=" + $scope.condition.dormitory + "&ItemName=" + $scope.ItemName
                +"&checkMember=" + $scope.condition.checkMember + "&checkDate=" + $scope.condition.checkDate + "&_datatime=" + new Date().getTime()).success(function (data) {
                    $ionicLoading.hide();
                    if (data.status == 0) {
                        $scope.MoraleduDormitoryChInfoVO = data.result;
                        $scope.moraleduDormitoryChItemInfoVOs = angular.copy($scope.MoraleduDormitoryChInfoVO.moraleduDormitoryChItemInfoVOs);
                    } else {
                        ynuiNotification.error({msg: '获取宿舍信息失败！'});
                    }
                }).error(function (data) {
                    ynuiNotification.error({msg: '获取宿舍信息失败！'});
                });
            };

            /**
             * 获取宿舍
             * @param func 回调函数
             * @param index 1 不查询宿舍下的考核的信息  undefined 查询
             */
            $scope.getDormitoryList = function (func,index) {
                $scope.dormitoryList = [];
                $http.get(basePath + "/queryDormitoryByScoreTask.htm?taskId=" + taskId +
                "&dormitoryName=" + (isNotBlank($scope.condition.dormitoryName)?$scope.condition.dormitoryName:"")  +"&regionId="+ $scope.condition.region
                + "&checkNumber=" + $scope.condition.checkMember + "&checkData=" + $scope.condition.checkDate + "&checkUserId=" + $rootScope.authorizationStr.userId + "&usser=" + new Date().getTime()).success(function (data) {
                    if(func){
                        func();
                    }
                    if (data.status == 0) {
                        $scope.dormitoryList = data.result;
                        $scope.dormitoryListCopy = angular.copy($scope.dormitoryList);
                        $scope.condition.lastDormitory = null;
                        if ($scope.dormitoryList.length > 0) {
                            $scope.emptyDormitory = false;              //展示空数据提示
                            $scope.dataErrorMsgDormitory = "";

                            if($rootScope.gradeCtrl && $rootScope.gradeCtrl.condition){
                                $scope.condition = $rootScope.gradeCtrl.condition;
                            }else{
                                for(var i=0;i<$scope.dormitoryList.length;i++){
                                    var item = $scope.dormitoryList[i];
                                    if(item.isCheck == '0'){
                                        $scope.condition.dormitory = item.id;
                                        if($scope.dormitoryList.length > i+1){
                                            $scope.condition.lastDormitory = $scope.dormitoryList[i+1].id;
                                        }
                                        break;
                                    }
                                }
                                if(!$scope.condition.dormitory && $scope.dormitoryList.length > 0){
                                    $scope.condition.dormitory = $scope.dormitoryList[0].id;
                                    if($scope.dormitoryList.length > 1){
                                        $scope.condition.lastDormitory = $scope.dormitoryList[1].id;
                                    }
                                }
                            }
                            if(index == undefined){
                                $scope.getDormitoryScoreDetail(taskId);
                            }
                        }else{
                            $scope.emptyDormitory = true;              //展示空数据提示
                            $scope.dataErrorMsgDormitory = "没有可选择的宿舍！";
                        }
                    } else {
                        ynuiNotification.error({msg: data.message});
                    }
                }).error(function (data) {
                    if(func){
                        func();
                    }
                    ynuiNotification.error({msg: '获取考核区域失败！'});
                });
            };

            /**
             * 获取考核的区域信息
             */
            $scope.getRegionByScoreTaskId = function (func) {
                if (taskId) {
                    $ionicLoading.show({
                        template: '正在加载...'
                    });
                    $scope.regionList = [];
                    $http.get(basePath + "/queryRegionByScoreTaskId.htm?taskId=" + taskId + "&dormitoryUserType=2"+ "&_datatime=" + new Date().getTime()).success(function (data) {
                       if(func){
                           func();
                       }
                        if (data.status == 0) {
                            $scope.regionList = data.result;
                            var hasGroup = false;
                            if($rootScope.gradeCtrl && $rootScope.gradeCtrl.condition){
                                $scope.condition = $rootScope.gradeCtrl.condition;
                            }else {
                                for(var i=0;i<$scope.regionList.length ; i ++){
                                    var region = $scope.regionList[i];
                                    if("1" == region.checkType){
                                        hasGroup = true;
                                        $scope.condition.region = region.id;
                                        $scope.condition.regionName = region.name;
                                        $scope.getDormitoryList(function () {
                                            $scope.$broadcast('scroll.infiniteScrollComplete');
                                            $ionicScrollDelegate.scrollTop();
                                            $ionicLoading.hide();
                                        });
                                        break;
                                    }
                                }
                                if(! hasGroup){
                                    $scope.condition.region = $scope.regionList[0].id;
                                    $scope.condition.regionName = $scope.regionList[0].name;
                                }
                            }
                            $scope.getDormitoryList(function () {
                                $scope.$broadcast('scroll.infiniteScrollComplete');
                                $ionicScrollDelegate.scrollTop();
                                $ionicLoading.hide();
                            });
                        } else {
                            ynuiNotification.error({"msg":"获取区域信息失败！"});
                        }
                    }).error(function (data) {
                        if (func) {
                            func();
                        }
                        ynuiNotification.error({"msg":"获取区域信息失败！"});
                    });
                } else {
                    if (func) {
                        func();
                    }
                }
            };

            /**
             * 点击宿舍信息
             */
            $scope.searchCheckItem = function(dormitory){
                $scope.goBackToItem();
                $scope.condition.dormitory = dormitory.id;
                $scope.doRefreshItems();

            };
            /**
             * 刷新宿舍考核项目列表
             */
            $scope.doRefreshItems = function(){
                $scope.getDormitoryScoreDetail(taskId);
            };

            /**
             * 返回项目信息
             */
            $scope.goBackToItem = function(){
                $scope.classList.hide();
            };

            /**
             * 刷新宿舍考核宿舍信息
             */
            $scope.doRefreshClass = function(index){
                $scope.condition.dormitoryName = null;
                $scope.getDormitoryList(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                },index);
            };

            /**
             * 回任务列表
             */
            $scope.goTaskLists = function(){
                history.back(-1);
            };

            /**
             * 收缩
             */
            $scope.$watch("ItemName",function(newVal, oldVal){
                if($scope.ItemName == ""){
                    $scope.isItemName = false
                }else{
                    $scope.isItemName = true;
                }
                if (isNotBlank(newVal)) {
                    $scope.moraleduDormitoryChItemInfoVOs = $filter('filter')($scope.MoraleduDormitoryChInfoVO.moraleduDormitoryChItemInfoVOs,{checkItemName:$scope.ItemName});
                    if(!$scope.moraleduDormitoryChItemInfoVOs||$scope.moraleduDormitoryChItemInfoVOs.length<1){
                        $scope.emptyChItemIn = true; //展示空数据提示
                        $scope.dataErrorMsgChItemIn = "没有可选择的宿舍！";
                    }else{
                        $scope.emptyChItemIn = false;
                    }
                }else{
                    if($scope.MoraleduDormitoryChInfoVO != null ){
                        $scope.moraleduDormitoryChItemInfoVOs = angular.copy($scope.MoraleduDormitoryChInfoVO.moraleduDormitoryChItemInfoVOs);
                    }
                }
            },true);

            /**
             * 搜索
             */
            $scope.$watch("condition.dormitoryName",function(newVal, oldVal){
                if($scope.condition.dormitoryName == ""){
                    $scope.isDormitoryName = false
                }else{
                    $scope.isDormitoryName = true;
                }
                if (isNotBlank(newVal)) {
                    $scope.dormitoryListCopy = $filter('filter')($scope.dormitoryList,{dormitoryName:$scope.condition.dormitoryName});
                    if(!$scope.dormitoryListCopy||$scope.dormitoryListCopy.length<1){
                        $scope.emptyDormitory = true;              //展示空数据提示
                        $scope.dataErrorMsgDormitory = "没有可选择的宿舍！";
                    }else{
                        $scope.emptyDormitory = false;
                    }
                }else{
                    $scope.dormitoryListCopy = angular.copy($scope.dormitoryList);
                }
            },true);

            /**
             * 去打分
             */
            $scope.checkDormitory = function(checkItem){
                var obj = {
                    taskId:taskId,
                    checkMember:$scope.condition.checkMember,//考核次数
                    checkDate:$scope.condition.checkDate, //考核日期
                    region:$scope.condition.region,//区域信息
                    dormitory: $scope.condition.dormitory,//考核寝室
                    ItemId:checkItem.checkItemId,
                    typeId:checkItem.checkTypeId
                };
                $rootScope.gradeCtrl = {
                    condition:$scope.condition
                };
                $location.path("/dormitory_form").search("checkItem",angular.toJson(obj));
            };
            /**
             * 字符串处理
             * @param str
             * @returns {boolean}
             */
            var isNotBlank = function (str){
                if(null == str){
                    return false;
                }
                if(/\S+/.test(str)){
                    return true;
                }
                return false;
            };

            /**
             * 最后的提交
             */
            $scope.sumbitDormitoryScore = function(){
                $http.post(basePath + "/saveMorleduDormitoryCheckTypeByMobile.htm?taskId=" + taskId + "&dormitoryId=" + $scope.condition.dormitory  + "&checkMember=" + $scope.condition.checkMember + "&checkUserId=" + $rootScope.authorizationStr.userId).success(function (data) {
                    if (data.status == 0) {
                        //任务进度
                        ynuiNotification.success({msg: $scope.MoraleduDormitoryChInfoVO.dormitoryName + '考核完成！'});
                        $scope.getDormitory(1,1);
                    } else {
                        ynuiNotification.error({msg: '添加失败！'});
                    }
                })
            };

            /**
             * 如果未打分的数量为0 则显示悬浮层
             */
            if(task.notOverClassNumber == 0){
                window.setTimeout(function(){
                    $scope.openDormiGrouping();
                },300);
            }

            $scope.getRegionByScoreTaskId(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        }]);+function ($) {
    'use strict';

    // DROPDOWN CLASS DEFINITION
    // =========================

    var backdrop = '.dropdown-backdrop'
    var toggle = '[data-toggle="dropdown"]'
    var Dropdown = function (element) {
        $(element).on('click.bs.dropdown', this.toggle)
    };

    Dropdown.VERSION = '3.3.5'

    function getParent($this) {
        var selector = $this.attr('data-target')

        if (!selector) {
            selector = $this.attr('href')
            selector = selector && /#[A-Za-z]/.test(selector) && selector.replace(/.*(?=#[^\s]*$)/, '') // strip for ie7
        }

        var $parent = selector && $(selector)

        return $parent && $parent.length ? $parent : $this.parent()
    }

    function clearMenus(e) {
        if (e && e.which === 3) return
        $(backdrop).remove()
        $(toggle).each(function () {
            var $this = $(this)
            var $parent = getParent($this)
            var relatedTarget = {relatedTarget: this}
            if ( e && ( !$parent.hasClass('open') || $(e.target).parents('.holdon').length)) return;

            if (e && e.type == 'click' && /input|textarea/i.test(e.target.tagName) && $.contains($parent[0], e.target)) return;

            $parent.trigger(e = $.Event('hide.bs.dropdown', relatedTarget));

            if (e.isDefaultPrevented()) return;

            $this.attr('aria-expanded', 'false');
            $parent.removeClass('open').trigger('hidden.bs.dropdown', relatedTarget)
        })
    }

    Dropdown.prototype.toggle = function (e) {
        var $this = $(this)

        if ($this.is('.disabled, :disabled')) return

        var $parent = getParent($this)
        var isActive = $parent.hasClass('open')

        clearMenus();

        if (!isActive) {
            if ('ontouchstart' in document.documentElement && !$parent.closest('.navbar-nav').length) {
                // if mobile we use a backdrop because click events don't delegate
                $(document.createElement('div'))
                    .addClass('dropdown-backdrop')
                    .insertAfter($(this))
                    .on('click', clearMenus)
            }

            var relatedTarget = {relatedTarget: this}
            $parent.trigger(e = $.Event('show.bs.dropdown', relatedTarget))

            if (e.isDefaultPrevented()) return

            $this
                .trigger('focus')
                .attr('aria-expanded', 'true')

            $parent
                .toggleClass('open')
                .trigger('shown.bs.dropdown', relatedTarget)
        }

        return false
    };

    Dropdown.prototype.keydown = function (e) {
        if (!/(38|40|27|32)/.test(e.which) || /input|textarea/i.test(e.target.tagName)) return

        var $this = $(this);

        e.preventDefault();
        e.stopPropagation();

        if ($this.is('.disabled, :disabled')) return;

        var $parent = getParent($this);
        var isActive = $parent.hasClass('open');

        if (!isActive && e.which != 27 || isActive && e.which == 27) {
            if (e.which == 27) $parent.find(toggle).trigger('focus');
            return $this.trigger('click')
        }

        var desc = ' li:not(.disabled):visible a';
        var $items = $parent.find('.dropdown-menu' + desc);

        if (!$items.length) return;

        var index = $items.index(e.target)

        if (e.which == 38 && index > 0)                 index--;        // up
        if (e.which == 40 && index < $items.length - 1) index++;       // down
        if (!~index)                                    index = 0;

        $items.eq(index).trigger('focus')
    };


    // DROPDOWN PLUGIN DEFINITION
    // ==========================

    function Plugin(option) {
        return this.each(function () {
            var $this = $(this);
            var data = $this.data('bs.dropdown');

            if (!data) $this.data('bs.dropdown', (data = new Dropdown(this)))
            if (typeof option == 'string') data[option].call($this)
        })
    }

    var old = $.fn.dropdown;

    $.fn.dropdown = Plugin;
    $.fn.dropdown.Constructor = Dropdown;


    // DROPDOWN NO CONFLICT
    // ====================

    $.fn.dropdown.noConflict = function () {
        $.fn.dropdown = old;
        return this
    };


    // APPLY TO STANDARD DROPDOWN ELEMENTS
    // ===================================

    $(document)
        .on('click.bs.dropdown.data-api', clearMenus)
        .on('click.bs.dropdown.data-api', '.dropdown form', function (e) {
            e.stopPropagation()
        })
        .on('click.bs.dropdown.data-api', toggle, Dropdown.prototype.toggle)
        .on('keydown.bs.dropdown.data-api', toggle, Dropdown.prototype.keydown)
        .on('keydown.bs.dropdown.data-api', '.dropdown-menu', Dropdown.prototype.keydown)

}(jQuery);