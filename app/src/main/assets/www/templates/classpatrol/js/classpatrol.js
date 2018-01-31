/**
 * Created by wuhaiying on 2016/11/16.
 */
angular.module('starter').config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
    //巡课登记-打开应用时显示效果
        .state('patrol_register', {
            url: '/patrol_register',
            templateUrl: 'templates/classpatrol/patrol_register.html',
            cache: false
        })
        //巡课登记-选择教室后显示效果
        .state('patrol_register_more', {
            url: '/patrol_register_more',
            templateUrl: 'templates/classpatrol/patrol_register_more.html',
            cache: false
        })
        //选择教室
        .state('choose_classroom', {
            url: '/choose_classroom',
            templateUrl: 'templates/classpatrol/choose_classroom.html',
            cache: false
        })
        //选择课程
        .state('choose_courses', {
            url: '/choose_courses',
            templateUrl: 'templates/classpatrol/choose_courses.html',
            cache: false
        })
        //选择教师
        .state('choose_teacher', {
            url: '/choose_teacher',
            templateUrl: 'templates/classpatrol/choose_teacher.html',
            cache: false
        })
        //选择班级
        .state('choose_grade', {
            url: '/choose_grade',
            templateUrl: 'templates/classpatrol/choose_grade.html',
            cache: false
        })
        //选择巡课人
        .state('choose_patroller', {
            url: '/choose_patroller',
            templateUrl: 'templates/classpatrol/choose_patroller.html',
            cache: false
        }).//备注
    state('patrol_tips', {
        url: '/patrol_tips',
        templateUrl: 'templates/classpatrol/patrol_tips.html',
        cache: false
    });
});
angular.module("starter").factory('viewsParameterService', function () {
    // 不同视图之间传递对象
    var jsonObj = {};
    return {
        clearParameter:function(){

            if(window&&window.localStorage){
                window.localStorage.removeItem("selectSutTemp");
            }
        },
        getParameter : function(){
            var back = jsonObj;
            if(window&&window.localStorage){
                var temp = window.localStorage.getItem("selectSutTemp");
                if(temp){
                    back = JSON.parse(temp);
                }
            }
            return back;
        },
        setParameter:function(obj){
            if(window&&window.localStorage){
                window.localStorage.setItem("selectSutTemp",JSON.stringify(obj));
            }
            jsonObj = obj;
        },
        //判断是否是刷新，jsonObj = {} 就是刚进入页面或者刷新了
        getTempParameter : function(){
            var jStr=JSON.stringify(jsonObj);
            if(jStr == "{}"||!jStr){
                return null;
            }else{
                return jsonObj;
            }
        }
    }

});
/***
 * 本地数据缓存
 */
angular.module('starter').factory("storageService", function () {
    /**
     * 可以插入undefined  null 或者是对象
     * 对key值的保护，只返回string
     * @param key
     */
    var translateKeyToStr = function (key) {
        if (angular.isUndefined(key)) {
            return $rootScope.DEFAULT_KEY;
        } else if (angular.isObject(key) || angular.isArray(key) || angular.isNumber(+key || key)) {
            return angular.toJson(key) + $rootScope.path;
        }
        /*增加对空字符串的支持*/
        else if (angular.isString(key)) {
            if (key.length > 0) {
                return key;
            } else {
                return $rootScope.DEFAULT_KEY;
            }
        }
        /*增加对布尔值的支持*/
        else if (angular.isBoolean(key)) {
            return key + "";
        }
        return $rootScope.DEFAULT_KEY;
    };
    /**
     * 根据 key删除缓存的数据
     * @param key
     */
    var removeValue = function (key) {
        if (window && window.localStorage) {
            window.localStorage.removeItem(translateKeyToStr(key));
        }
    };
    /***
     * 保存数据和删除数据
     */
    return {
        getItem: function (key) {
            if (window && window.localStorage) {
                var _$ = window.localStorage.getItem(translateKeyToStr(key));
                removeValue(translateKeyToStr(key));
                if (_$) {
                    return angular.copy(JSON.parse(_$));
                }
                return null;
            }
        },
        getItemNoDel: function (key) {
            if (window && window.localStorage) {
                var _$ = window.localStorage.getItem(translateKeyToStr(key));
                if (_$) {
                    return angular.copy(JSON.parse(_$));
                }
                return null;
            }
        },
        clearKey: function (key) {
            removeValue(translateKeyToStr(key));
        },
        setItem: function (key, value) {
            if (window && window.localStorage) {
                if (value) {
                    window.localStorage.setItem(translateKeyToStr(key), JSON.stringify(value));
                }
            }
        }
    }
});
/**
 *  巡课登记
 */
angular.module('starter').controller("patrolRegisterController", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService", "$rootScope", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService, $rootScope) {

    /**
     * 路由信息
     * @type {string}
     */
    var basePath = originBaseUrl + "/third/xcMobileRecord";

    /**
     * 获取初始值
     * @type {{}}
     */
    $scope.xcMobileStartVO = {};
    var temp = storageService.getItem("xcMobileStartVO");
    if (temp) {
        $scope.xcMobileStartVO = angular.copy(temp);
    }
    $scope.timeOver = true;
    /**
     * 字符串处理
     * @param str
     * @returns {boolean}
     */
    var isNotBlank = function (str) {
        if (null == str) {
            return false;
        }
        if (/\S+/.test(str)) {
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
        var resultObj = {};
        var deepParseParams = function (key, value, prefix) {
            if (value instanceof Array) {
                for (var i in value) {
                    deepParseParams("", value[i], prefix + key + "[" + i + "]");
                }
            }
            else if (value instanceof Object) {
                for (var i in value) {
                    deepParseParams("." + i, value[i], prefix + key);
                }
            }
            else {
                resultObj[prefix + key] = value;
            }
        };
        for (var i in obj) {
            deepParseParams("", obj[i], i);
        }
        return resultObj;
    };

    /**
     *  获取第一步的基础数据
     */
    $scope.getFirstBaseInfo = function(){
        if (!isNotBlank($scope.xcMobileStartVO.classRoomId)) {
            return false;
        }
        $scope.xcMobileStartVO.currentUser = $rootScope.authorizationStr.userId;
        $http.post(basePath + "/saveBaseInfo.htm", parseParamForSpringMVC($scope.xcMobileStartVO)).success(function (data) {
            if (data.status == 0) {
                $scope.xcMobileDetailInfoVO_key = data.result;
            }
        }).error(function (data) {
            notifyError("query info error,please check.");
        });
    };
    /**
     * 查询条件
     * @type {any}
     */
    $scope.classRoom = storageService.getItem("classRoom");
    if (!$scope.classRoom) {
        $scope.classRoom = {
            id:"",
            name: "请选择教室"
        };
    } else {
        var change = false;
        if($scope.xcMobileStartVO.classRoomId != $scope.classRoom.id) {
            change = true;
        }
        $scope.xcMobileStartVO.classRoomId = $scope.classRoom.id;
        if(change){
            $scope.getFirstBaseInfo();
        }
    }

    /**
     * 报错工具
     * @param message
     */
    var notifyError = function (message) {
        if (message == null || message == "") {
            message = "获取数据失败";
        }
        ynuiNotification.error({msg: message});
    };

    /**
     * 清楚所有的存储
     */
    $scope.clearAllStorage = function (fun) {
        storageService.clearKey("xcMobileDetailInfoVO_key");
        storageService.clearKey("teachClass");
        storageService.clearKey("classRoom");
        storageService.clearKey("classRoomMore");
        storageService.clearKey("teachers");
        storageService.clearKey("teacher");
        storageService.clearKey("remark");
        storageService.clearKey("select-course");
        $scope.classRoom = null;
        if (fun) {
            fun();
        }
    };

    /**
     * 根据选择的日期查询出巡课的时间 课程节次
     */
    $scope.getRegister = function (date, fun) {
        $http.get(basePath + "/queryCheckInfoByDate.htm?date=" + date +"&_timestamp=" + new Date().getTime()).success(function (data) {
            $scope.timeOver = true;
            if (data.status == 0) {
                $scope.xcMobileStartVO = data.result;
                if ($scope.xcMobileStartVO) {
                    var hasDefault = false;
                    angular.forEach($scope.xcMobileStartVO.xcMobileKnobVOs, function (item) {
                        item.select = false;
                        if (item.knob == $scope.xcMobileStartVO.knob && item.timeType == $scope.xcMobileStartVO.timeType) {
                            item.select = true;
                            hasDefault = true;
                        }
                    });
                    if (!hasDefault) {
                        var item = $scope.xcMobileStartVO.xcMobileKnobVOs[0];
                        if (item) {
                            item.select = true;
                            $scope.xcMobileStartVO.knob = item.knob;
                            $scope.xcMobileStartVO.timeType = item.timeType;
                        }
                    }
                }
            } else {
                notifyError(data.message);
                // $scope.getRegister("",function () {
                //     $scope.$broadcast('scroll.infiniteScrollComplete');
                //     $ionicScrollDelegate.scrollTop();
                //     $ionicLoading.hide();
                // })
            }
            if (fun) {
                fun();
            }
        }).error(function (data) {
            notifyError("获取信息失败");
            if (fun) {
                fun();
            }
        });
    };



    /**
     * 选中节次
     * @param item
     */
    $scope.selectKnob = function (item) {
        angular.forEach($scope.xcMobileStartVO.xcMobileKnobVOs, function (knob) {
            knob.select = false;
        });
        item.select = true;
        $scope.xcMobileStartVO.knob = item.knob;
        $scope.xcMobileStartVO.timeType = item.timeType;
        $scope.getFirstBaseInfo()
    };
    /**
     * 选择教师层
     */
    $scope.selectClassRoom = function () {
        storageService.setItem("xcMobileStartVO", $scope.xcMobileStartVO);
        $location.path("/choose_classroom").search("type", 1);
    };
    /**
     * 获取数据
     */
    if (!temp) {
        $scope.getRegister("", function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    }

    $scope.selectDateTime = function (data) {
        $scope.timeOver = false;
        $scope.getRegister(data, function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        })
    };
    /**
     * 下一步
     */
    $scope.nextStep = function () {
        if (!isNotBlank($scope.xcMobileStartVO.classRoomId)) {
            notifyError("请选择巡查的教室！");
            return false;
        }
        $scope.clearAllStorage(function () {
            storageService.setItem("xcMobileDetailInfoVO_key",$scope.xcMobileDetailInfoVO_key);
            $location.path("/patrol_register_more");
        });
    };
}]);
/**
 *  巡查的教室
 */
angular.module('starter').controller("chooseClassRoomCont", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService) {

    /**
     * 1 从第一部而来  2 从第二步而来
     */
    $scope.type = $location.search().type;
    /**
     * 区域的列表
     * @type {Array}
     */
    $scope.areaList = [];

    /**
     *  班级的列表
     * @type {Array}
     */
    $scope.classRoomList = [];

    /**
     * 查询
     * @type {{name: string}}
     */
    $scope.condition = {
        name: "",
        id: "",
        isQuery: false
    };

    /**
     * 路由信息
     * @type {string}
     */
    var basePath = originBaseUrl + "/third/xcMobileRecord";

    /**
     * 报错工具
     * @param message
     */
    var notifyError = function (message) {
        if (message == null || message == "") {
            message = "获取数据失败";
        }
        ynuiNotification.error({msg: message});
    };

    /**
     * 获取全部的区域
     */
    $scope.getXCArea = function (fun) {
        $scope.areaList = [];
        $http.get(basePath + "/queryAllCheckGroup.htm?" +"_timestamp=" + new Date().getTime()).success(function (data) {
            if (data.status == 0) {
                $scope.areaList = data.result;
                if ($scope.areaList.length > 0) {
                    $scope.condition.id = $scope.areaList[0].id;
                    $scope.condition.groupName =  $scope.areaList[0].name;
                    $scope.getXCAreaClassRoom($scope.condition, "",fun);
                }else{
                    $scope.condition.groupName = "(没有巡查区域)";
                }
            } else {
                notifyError("获取巡查区域失败！");
            }
            if (fun) {
                fun();
            }
        }).error(function (data) {
            notifyError("获取巡查区域失败！");
            if (fun) {
                fun();
            }
        })
    };

    /**
     * 查询记录
     * @param area
     * @param classRoomName
     */
    $scope.getXCAreaRoom = function(area,classRoomName){
        $scope.classRoomList = [];
        $scope.condition.isQuery = true;
        $scope.condition.groupName = area.name;
        $scope.condition.id = area.id;
        $scope.getXCAreaClassRoom($scope.condition, $scope.condition.name,function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };
    /**
     * 查询巡查区域下的班级信息
     * @param groupId
     * @param classRoomName
     */
    $scope.getXCAreaClassRoom = function (_group, classRoomName,fun) {
        $scope.classRoomList = [];
        $scope.condition.isQuery = true;
        $scope.condition.groupName = _group.groupName;
        $scope.condition.id = _group.id;
        $http.get(basePath + "/quertAllClassRoomByGroup.htm?groupId=" +  $scope.condition.id + "&name=" + classRoomName +"&_timestamp=" + new Date().getTime()).success(function (data) {
            $scope.condition.isQuery = false;
            $scope.classRoomList = data.result;
            beginWatch();
            if ($scope.classRoomList && $scope.classRoomList.length > 0){
                $scope.condition.title = "";
                $scope.condition.error = false;
            }else{
                $scope.condition.title = "没有可选教室";
                $scope.condition.error = false;
            }
            if (fun) {
                fun();
            }
        }).error(function (data) {
            $scope.condition.isQuery = false;
            $scope.condition.title = "获取教室失败";
            $scope.condition.error = true;
            if (fun) {
                fun();
            }
        });
    };

    /**
     * 查询
     */
    function beginWatch() {
        $scope.$watch("condition.name", function (newValue, oldValue) {
            if (newValue != oldValue) {
                var setTimeOut = window.setTimeout(function () {
                    $scope.getXCAreaClassRoom($scope.condition, $scope.condition.name,function () {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                    });
                    window.clearTimeout(setTimeOut);
                }, 200);
            }
        });
    }

    /**
     * 清楚查询条件
     */
    $scope.clearConditon = function () {
        $scope.condition.name = "";
    };
    /**
     * 刷新数据
     */
    $scope.doRefresh = function () {
        $scope.condition.name = "";
        $scope.getXCAreaClassRoom($scope.condition, $scope.condition.name,function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };

    /**
     * 选中班级
     */
    $scope.selectClassRoom = function (item) {
        if ($scope.type == 1) {
            storageService.setItem("classRoom", item);
            $location.path("/patrol_register");
        } else {
            storageService.setItem("classRoomMore", item);
            $location.path("/patrol_register_more");
        }
    };
    /**
     * 获取数据
     */
    $scope.getXCArea();

    /**
     * 取消选择班级
     */
    $scope.cancelSelectClass = function () {
        if ($scope.type == 1) {
            $location.path("/patrol_register");
        } else {
            $location.path("/patrol_register_more");
        }
    }
}]);
/**
 * 详情
 */
angular.module('starter').controller("patrolRegisterMoreController", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService","$rootScope", "$ionicPopup",function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService,$rootScope,$ionicPopup) {
    /**
     * 路由信息
     * @type {string}
     */
    var basePath = originBaseUrl + "/third/xcMobileRecord";
    /**
     * 报错工具
     * @param message
     */
    var notifyError = function (message) {
        if (message == null || message == "") {
            message = "获取数据失败";
        }
        ynuiNotification.error({msg: message});
    };
    /**
     * 字符串处理
     * @param str
     * @returns {boolean}
     */
    var isNotBlank = function (str) {
        if (null == str) {
            return false;
        }
        if (/\S+/.test(str)) {
            return true;
        }
        return false;
    };
    /**
     * 成功的消息提示
     * @param message
     */
    var notifySuccess = function (message) {
        if (!isNotBlank(message)) {
            message = "操作成功！";
        }
        ynuiNotification.success({msg: message});
    };
    /**
     *  巡查详情
     * @type {{}}
     */
    $scope.xcMobileDetailInfoVO = {};
    /**
     *  单选
     */
    $ionicModal.fromTemplateUrl('single-select.html', {
        scope: $scope,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.singSelect = modal;
    });
    /**
     * 单选  隐藏
     */
    $scope.hideTableSingle = function () {
        $scope.singSelect.hide();
    };
    /**
     * 单选  显示
     */
    $scope.selectCustom = function (custom) {
        /**如果是下拉单选*/
        if (custom.itemType == 4) {
            $scope.optionList = custom.xcMobileCustFieldValueVOs;
            $scope.singSelect.show();
        }
        $scope.custom = custom;
    };
    /**
     * 当选选项
     */
    $scope.selectOption = function (custom) {
        $scope.custom.value = custom.name;
        $scope.hideTableSingle();
    };
    /**
     * 多选
     */
    $ionicModal.fromTemplateUrl('more-select.html', {
        scope: $scope,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.moreSelect = modal;
    });
    /**
     * 多选  隐藏
     */
    $scope.hideTableMore = function () {
        $scope.moreSelect.hide();
    };
    /**
     * 多选 显示
     */
    $scope.showTableMore = function () {
        $scope.moreSelect.show();
    };
    /**
     * 显示节次
     * @type {boolean}
     */
    $scope.moreKnob = false;
    /**
     * 把obj转化为Spring可接受状态
     * @param obj
     * @returns {{}}
     */
    var parseParamForSpringMVC = function (obj) {
        var resultObj = {};
        var deepParseParams = function (key, value, prefix) {
            if (value instanceof Array) {
                for (var i in value) {
                    deepParseParams("", value[i], prefix + key + "[" + i + "]");
                }
            }
            else if (value instanceof Object) {
                for (var i in value) {
                    deepParseParams("." + i, value[i], prefix + key);
                }
            }
            else {
                resultObj[prefix + key] = value;
            }
        };
        for (var i in obj) {
            deepParseParams("", obj[i], i);
        }
        return resultObj;
    };
    /**
     * 初始化选中的值
     * @param data
     */
    var initBaseInfo = function(data){
        $scope.xcMobileDetailInfoVO = angular.copy(data);
        var hasDefault = false,selectIndex = 1;
        angular.forEach($scope.xcMobileDetailInfoVO.xcMobileStartVO.xcMobileKnobVOs, function (item,index) {
            item.select = false;
            if (!hasDefault && item.knob == $scope.xcMobileDetailInfoVO.xcMobileStartVO.knob && item.timeType == $scope.xcMobileDetailInfoVO.xcMobileStartVO.timeType) {
                item.select = true;
                hasDefault = true;
                selectIndex = index+1;
            }
        });
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.index = selectIndex;
        if (!hasDefault) {
            var item = $scope.xcMobileStartVO.xcMobileKnobVOs[0];
            item.select = true;
            $scope.xcMobileStartVO.knob = item.knob;
            $scope.xcMobileStartVO.timeType = item.timeType;
            $scope.xcMobileDetailInfoVO.xcMobileStartVO.index = 1;
        }
    };
    /**
     * 下一步
     */
    $scope.saveBaseInfo = function () {
        if (!isNotBlank($scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId)) {
            return false;
        }
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.currentUser = $rootScope.authorizationStr.userId;
        $http.post(basePath + "/saveBaseInfo.htm", parseParamForSpringMVC($scope.xcMobileDetailInfoVO.xcMobileStartVO)).success(function (data) {
            if (data.status == 0) {
                $scope.clearAllStorage();
                teachClass = null;
                $scope.classRoom = null;
                teachers = null;
                remark = null;
                course = null;
                storageService.setItem("xcMobileDetailInfoVO_key", data.result);
                initBaseInfo(data.result);
            } else {
                notifyError("保存失败！");
            }
        }).error(function (data) {
            notifyError("保存失败！");
        });
    };
    /**
     * 默认值
     */
    var xcMobileDetailInfoVO = storageService.getItemNoDel("xcMobileDetailInfoVO_key");
    if (xcMobileDetailInfoVO) {
        $scope.xcMobileDetailInfoVO = angular.copy(xcMobileDetailInfoVO);
    }
    if (!$scope.xcMobileDetailInfoVO) {
        notifyError("数据查询失败！");
        console.error("数据获取失败！");
    } else {
        if ($scope.xcMobileDetailInfoVO.xcMobileStartVO) {
            initBaseInfo($scope.xcMobileDetailInfoVO)
        }
    }
    /**
     * 查询条件 选择教室
     * @type {any}
     */
    $scope.classRoom = storageService.getItemNoDel("classRoomMore");
    if ($scope.classRoom) {
        var change = false;
        if($scope.classRoom.id != $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId){
            change = true;
        }
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId = $scope.classRoom.id;
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomName = $scope.classRoom.name;
        if(change){
            $scope.saveBaseInfo();
        }
    } else {
        $scope.classRoom = {
            id: $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId,
            name: $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomName
        }
    }

    /**
     * 班级信息 teachClass
     */
    var teachClass = storageService.getItemNoDel("teachClass");
    if (teachClass) {
        var xcMobileDetailClassVOs = [],clazzId = [];
        angular.forEach(teachClass, function (item) {
            xcMobileDetailClassVOs.push({
                platformSysClassId: item.id,
                platformSysClassName: item.name
            });
            clazzId.push(item.id);
        });
        if (null != $scope.xcMobileDetailInfoVO) {
            $scope.xcMobileDetailInfoVO.xcMobileDetailClassVOs = angular.copy(xcMobileDetailClassVOs);
        }
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.teaAdminClassIdList = clazzId;
    } else {
        teachClass = [];clazzId = [];
        angular.forEach($scope.xcMobileDetailInfoVO.xcMobileDetailClassVOs, function (detailClass) {
            teachClass.push({
                id: detailClass.platformSysClassId,
                name: detailClass.platformSysClassName
            });
            clazzId.push(detailClass.platformSysClassId);
        });
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.teaAdminClassIdList = clazzId;
    }
    /**
     * 班级名称
     */
    if ($scope.xcMobileDetailInfoVO.xcMobileDetailClassVOs) {
        var className = [];
        angular.forEach($scope.xcMobileDetailInfoVO.xcMobileDetailClassVOs, function (clazz) {
            className.push(clazz.platformSysClassName);
        });
        $scope.className = className.join(",");
    }

    /**
     * 重新选择了巡课日期
     */
    $scope.selectDateTimeMore = function(data){
        var change = false;
        if(data != $scope.xcMobileDetailInfoVO.xcMobileStartVO.checkDate){
            change = true;
        }
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.checkDate = data;
        if(change){
            $scope.saveBaseInfo();
        }
    };
    /**
     * 选择巡查的教室
     */
    $scope.selectClassRoom = function () {
        $scope.saveStorage();
        $location.path("/choose_classroom").search("type", 2);
    };
    /***
     * 选择的课程信息
     */
    var course = storageService.getItemNoDel("select-course");
    if (course) {
        $scope.xcMobileDetailInfoVO.xcMobileDetailCourseVO = {
            courseId: course.courseId,
            courseName: course.courseName,
            courseNo: course.courseNo
        }
    } else {
        if (xcMobileDetailInfoVO.xcMobileDetailCourseVO) {
            course = {
                courseId: $scope.xcMobileDetailInfoVO.xcMobileDetailCourseVO.courseId,
                courseName: $scope.xcMobileDetailInfoVO.xcMobileDetailCourseVO.courseName,
                courseNo: $scope.xcMobileDetailInfoVO.xcMobileDetailCourseVO.courseNo
            }
        }
    }
    /***
     * 代课教师教师
     */
    var teachers = storageService.getItemNoDel("teachers");
    if (teachers && angular.isArray(teachers)) {
        angular.forEach(teachers, function (tea) {
            tea.attendanceInfo = 1;//教师出勤情况（1.按时到 2.迟到  3未到）
            if($scope.xcMobileDetailInfoVO.classTeacherList.length >0){
                angular.forEach($scope.xcMobileDetailInfoVO.classTeacherList,function(dataTeach){
                    if(tea.userId == dataTeach.userId){
                        tea.attendanceInfo = dataTeach.attendanceInfo;
                    }
                });
            }
        });
        $scope.xcMobileDetailInfoVO.classTeacherList = angular.copy(teachers);
        angular.forEach($scope.xcMobileDetailInfoVO.classTeacherList,function (_teac) {
            _teac.name = _teac.name + (_teac.alien ? "("+_teac.alien+")" : "")
        })
    } else {
        teachers = [];
        angular.forEach($scope.xcMobileDetailInfoVO.classTeacherList, function (tea) {
            teachers.push(tea);
        });
    }
    /**
     * 选择的巡查教师
     */
    var teacher = storageService.getItemNoDel("teacher");
    if (teacher) {
        $scope.xcMobileDetailInfoVO.checkTeacher = teacher;
    } else {
        teacher = $scope.xcMobileDetailInfoVO.checkTeacher;
    }
    var remark = storageService.getItemNoDel("remark");
    if (isNotBlank(remark)) {
        $scope.xcMobileDetailInfoVO.remark = angular.copy(remark);
    } else {
        remark = $scope.xcMobileDetailInfoVO.remark;
    }
    /**
     * 选择班级
     */
    $scope.selectTeachClass = function () {
        $scope.saveStorage();
        $location.path("/choose_grade");
    };
    /**
     * 选中节次
     * @param item
     */
    $scope.selectKnobMore = function (item,index) {
        var change = false;
        angular.forEach($scope.xcMobileDetailInfoVO.xcMobileStartVO.xcMobileKnobVOs, function (knob) {
            knob.select = false;
        });
        item.select = true;
        if($scope.xcMobileDetailInfoVO.xcMobileStartVO.knob != item.knob || item.timeType != $scope.xcMobileDetailInfoVO.xcMobileStartVO.timeType){
             change = true;
        }
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.knob = item.knob;
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.index = index;
        $scope.xcMobileDetailInfoVO.xcMobileStartVO.timeType = item.timeType;
        if(change){
            $scope.saveBaseInfo();
        }
    };
    /**
     * 显示更多
     */
    $scope.showMore = function () {
        $scope.moreKnob = !$scope.moreKnob;
    };
    /**
     *选择课程
     */
    $scope.selectCourse = function () {
        if (!isNotBlank($scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId) || -1 == $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId) {
            notifyError("请选择巡查的教室！");
            return false;
        }
        if (!isNotBlank($scope.xcMobileDetailInfoVO.xcMobileStartVO.knob)) {
            notifyError("请选择巡查的课程！");
            return false;
        }
        if (!isNotBlank($scope.xcMobileDetailInfoVO.xcMobileStartVO.checkDate)) {
            notifyError("请选择巡查的日期！");
            return false;
        }
        $scope.saveStorage();
        var condition = {
            classDate: $scope.xcMobileDetailInfoVO.xcMobileStartVO.checkDate,
            classRoomId: $scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId,
            knob: $scope.xcMobileDetailInfoVO.xcMobileStartVO.knob,
            termId: $scope.xcMobileDetailInfoVO.xcMobileStartVO.checkPlatformSysTermId
        };
        storageService.setItem("courseCondition", condition);
        $location.path("/choose_courses");
    };

    var eventAction = function(event){
        return !angular.isUndefined(event) && (event.target.nodeName == "DIV"
            || event.target.nodeName == "INPUT" || event.target.nodeName == "SPAN");
    };
    /**
     * 修改应到人数
     */
    $scope.clickShouldNumberEdit = function (event) {
        if(eventAction(event)){
            $scope.xcMobileDetailInfoVO.shouldNumberEdit = !$scope.xcMobileDetailInfoVO.shouldNumberEdit;
            if ($scope.xcMobileDetailInfoVO.shouldNumberEdit) {
                $("#shouldNumberEdit").focus();
            } else if (isNaN(parseInt($scope.xcMobileDetailInfoVO.shouldNumber))) {
                $scope.xcMobileDetailInfoVO.shouldNumber = 0;
            }
        }
    };
    /**
     * 学生请假人数
     */
    $scope.clickleaveNumberEdit = function (event) {
        if(eventAction(event)){
            $scope.xcMobileDetailInfoVO.leaveNumberEdit = !$scope.xcMobileDetailInfoVO.leaveNumberEdit;
            if ($scope.xcMobileDetailInfoVO.leaveNumberEdit) {
                $("#leaveNumberEdit").focus();
            } else if (isNaN(parseInt($scope.xcMobileDetailInfoVO.leaveNumber))) {
                $scope.xcMobileDetailInfoVO.leaveNumber = 0;
            }
        }
    };
    /**
     * 未到人数
     */
    $scope.clickAbsenteeismNumberEdit = function (event) {
        if(eventAction(event)){
            $scope.xcMobileDetailInfoVO.absenteeismNumberEdit = !$scope.xcMobileDetailInfoVO.absenteeismNumberEdit;
            if ($scope.xcMobileDetailInfoVO.absenteeismNumberEdit) {
                $("#absenteeismNumberEdit").focus();
            } else if (isNaN(parseInt($scope.xcMobileDetailInfoVO.absenteeismNumber))) {
                $scope.xcMobileDetailInfoVO.absenteeismNumber = 0;
            }
        }
    };
    /**
     * 实到人数
     */
    $scope.clickToNumberEdit = function (event) {
        if(eventAction(event)){
            $scope.xcMobileDetailInfoVO.toNumberEdit = !$scope.xcMobileDetailInfoVO.toNumberEdit;
            if ($scope.xcMobileDetailInfoVO.toNumberEdit) {
                $("#toNumberEdit").focus();
            } else if (isNaN(parseFloat($scope.xcMobileDetailInfoVO.toNumber))) {
                $scope.xcMobileDetailInfoVO.toNumber = 0;
            }
        }
    };
    /**
     * 计算
     */
    $scope.getStudentNumber = function () {
        if (!isNaN(parseInt($scope.xcMobileDetailInfoVO.shouldNumber))) {
            var total = 0;
            if (!isNaN(parseInt($scope.xcMobileDetailInfoVO.leaveNumber))) {
                total = parseInt(total) + parseInt($scope.xcMobileDetailInfoVO.leaveNumber);
            }
            if (!isNaN(parseInt($scope.xcMobileDetailInfoVO.absenteeismNumber))) {
                total = parseInt(total) + parseInt($scope.xcMobileDetailInfoVO.absenteeismNumber);
            }
            $scope.xcMobileDetailInfoVO.toNumber = parseInt($scope.xcMobileDetailInfoVO.shouldNumber) - parseInt(total);
        }
    };
    /**
     * 保存数据到本地
     */
    $scope.saveStorage = function () {
        storageService.setItem("xcMobileDetailInfoVO_key", $scope.xcMobileDetailInfoVO);
        storageService.setItem("teachClass", teachClass);
        storageService.setItem("classRoomMore", $scope.classRoom);
        storageService.setItem("teachers", teachers);
        storageService.setItem("teacher", teacher);
        storageService.setItem("remark", remark);
        storageService.setItem("select-course", course);
    };
    /**
     * 清楚所有的存储
     */
    $scope.clearAllStorage = function () {
        storageService.clearKey("xcMobileDetailInfoVO_key");
        storageService.clearKey("teachClass");
        storageService.clearKey("classRoom");
        storageService.clearKey("classRoomMore");
        storageService.clearKey("teachers");
        storageService.clearKey("teacher");
        storageService.clearKey("remark");
        storageService.clearKey("select-course");
    };
    /**
     * 选择巡查的教师
     */
    $scope.selectPatrollTeacher = function (type) {
        $scope.saveStorage();
        $location.path("/choose_patroller").search("type", type)
    };
    /**
     * 修改上课教师的状态
     * @param item
     * @param type
     */
    $scope.selectType = function (item, type) {
        item.attendanceInfo = type;
    };
    /**
     * 开启备注弹框~
     */
    $scope.openDiageRemark = function () {
        $scope.remarks = angular.copy($scope.xcMobileDetailInfoVO.remark);
        $scope.saveStorage();
        storageService.setItem("remark", $scope.remarks);
        $location.path("/patrol_tips")
    };
    /**
     * 自定义字段的验证
     * （1.文本+无限制  2.文本+整数  3.文本+整数或小数  4.日期  5.时间  6.时间+日期  7.单选下拉框）
     * @param custom
     */
    $scope.changeInputItem = function (custom) {
        var error = false;
        if(2 != custom.inputRuleType && 3 != custom.inputRuleType){
            return error;
        }
        /*文本+整数   xcRange（1.大于0  2.等于0  3.小于0）*/
        var checkRule,maxLength =custom.maxLength,decimalLength = custom.maxLengthFloat,range = custom.xcRange,errorMsg = "";
        if(2 == custom.inputRuleType){
            eval("checkRule=\/^-?\\d{0,"+maxLength+"}$\/");
            errorMsg = "请输入"+maxLength+"位整数.";
        }else if(3 == custom.inputRuleType){
            eval("cusCheckRule = \/^-?\\d{0," + maxLength + "}(\\.\\d{1," + decimalLength + "})?$\/");
            errorMsg = "请输入"+maxLength+"位小数位为"+decimalLength+"的数字.";
        }
        if(isNotBlank(custom.value) &&!checkRule.test(custom.value) ){
            notifyError(errorMsg);
            custom.value = null;
            error = true;
        }
        if(isNotBlank(custom.value) && isNotBlank(range)){
            var simple = $scope.getCalutSimple(range);
            if(isNotBlank(simple)){
                if(!eval(custom.value+simple +"0")){
                    notifyError($scope.getCalutErrorInfo(range));
                    custom.value = null;
                    error = true;
                }
            }
        }
        return error;
    };
    /**
     * 返回计算符号
     * @param range
     */
    $scope.getCalutSimple = function(range) {
        var simple = "",arr = range.split(","),sum = -1 ;
        /*3 做特殊处理*/
        if(arr.length == 2 && $scope.sumRange(arr) == 3){
            simple = ">=";
        }else{
            sum = $scope.sumRange(arr);
            switch(sum){
                case 1: simple = ">"; break;
                case 2: simple = "=="; break;
                case 3: simple = "<";break;
                case 4: simple = "!=";break;
                case 5: simple = "<=";break;
                case 6: simple = null; break;
                case -1: simple = null; break;
                default : console.error(range + " is not number!");
            }
        }
        return simple;
    };
    /**
     * 返回计算符号
     * @param range
     */
    $scope.getCalutErrorInfo = function(range) {
        var simple = "",arr = range.split(","),sum = -1 ;
        /*3 做特殊处理*/
        if(arr.length == 2 && $scope.sumRange(arr) == 3){
            simple = "请输入大于等于0的数";
        }else{
            sum = $scope.sumRange(arr);
            switch(sum){
                case 1: simple = "请输入大于0的数"; break;
                case 2: simple = "请输入等于0的数"; break;
                case 3: simple = "请输入小于0的数";break;
                case 4: simple = "请输入不等于0的数";break;
                case 5: simple = "请输入小于等于0的数";break;
                case 6: simple = null; break;
                default : console.error(range + " is not number!");
            }
        }
        return simple;
    };
    /**
     * 获取值
     * @param arr
     */
    $scope.sumRange = function(arr){
        var sum = -1;
        if(angular.isArray(arr)){
            angular.forEach(arr,function (item) {
                sum += parseInt(item);
            })
        }else{
            sum = parseInt(arr) -1 ;
        }
        return sum == -1 ? -1  : parseInt(sum)+1;
    };
    /**
     * 提交
     */
    $scope.saveDetail = function () {
        /**复制一份数据*/
        var obj = angular.copy($scope.xcMobileDetailInfoVO);
        if(null == obj.classTeacherList || obj.classTeacherList.length == 0){
            notifyError("请选择上课教师.");
            return false;
        }
        if(null == obj.checkTeacher){
            notifyError("巡课教师不能为空.");
            return false;
        }
        if(obj.xcMobileCustomFieldVOs && obj.xcMobileCustomFieldVOs.length  > 0){
            var error = false;
            angular.forEach(obj.xcMobileCustomFieldVOs,function(custom){
                if(!error){
                    error = $scope.changeInputItem(custom);
                }
            });
            if(error){
                return false;
            }
        }
        /**check the data*/
        $http.get(basePath + "/checkXCDetail.htm?checkDate="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.checkDate+
                "&week="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.week+
                "&timeType="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.timeType+
                "&day="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.day+
                "&knob="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.knob+
                "&classRoomId="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.classRoomId+
                "&xcTeacherId="+$scope.xcMobileDetailInfoVO.xcMobileStartVO.currentUser+
                "&currentUserId="+$rootScope.authorizationStr.userId
            +"&_timestamp="+new Date().getTime()).success(function (data) {
            if (data.status == 0) {
                var item = data.result;
                if(isNotBlank(item.failedReason)){
                    item.failedReason = item.failedReason.replace("&%$",$scope.xcMobileDetailInfoVO.xcMobileStartVO.index);
                    var confirmPopup = item.userName == "-1" ? $ionicPopup.alert({
                        title: "提示",
                        template: item.failedReason,
                        okText: "确定"
                    }) : $ionicPopup.confirm({
                        title: "提示",
                        template: item.failedReason,
                        cancelText: "取消",
                        okText: "确定"
                    });
                    confirmPopup.then(function(result){
                        if(result){
                            /**自己以前的记录，可以修改 点击确定有保存数据*/
                            if("1" == item.userName){
                                $scope.saveDetailInfo();
                            }else{
                                /**别人已经巡查，不可更改 点击确认后不做任何处理*/
                            }
                        }else{
                           /**点击取消不做任何处理*/
                        }
                    })
                }else{
                    /**没有已经保存的数据 则直接保存*/
                    $scope.saveDetailInfo();
                }
            }
        }).error(function (data) {

        })
    };

    /**
     * 保存
     * @returns {boolean}
     */
    $scope.saveDetailInfo = function(){
        /**复制一份数据*/
        var obj = angular.copy($scope.xcMobileDetailInfoVO);
        if(null == obj.classTeacherList || obj.classTeacherList.length == 0){
            notifyError("请选择上课教师.");
            return false;
        }
        if(null == obj.checkTeacher){
            notifyError("巡课教师不能为空.");
            return false;
        }
        if(obj.xcMobileCustomFieldVOs && obj.xcMobileCustomFieldVOs.length  > 0){
            var error = false;
            angular.forEach(obj.xcMobileCustomFieldVOs,function(custom){
                if(!error){
                    error = $scope.changeInputItem(custom);
                }
            });
            if(error){
                return false;
            }
        }
        delete  obj.xcMobileStartVO.xcMobileKnobVOs;
        $http.post(basePath + "/saveXCMobileRecordDetail.htm", parseParamForSpringMVC(obj)).success(function (data) {
            if (data.status == 0) {
                /**在本地保存一份数据*/
                $scope.clearAllStorage();
                notifySuccess("保存成功！");
                $location.path("/patrol_register");
            }
        }).error(function (data) {

        })
    };
    console.log($scope.xcMobileDetailInfoVO);
}]);
/**
 * 选择班级信息
 */
angular.module('starter').controller("classController", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService) {

    /**
     * 路由信息
     * @type {string}
     */
    var basePath = originBaseUrl + "/third/xcMobileRecord";
    /**
     * 查询条件
     * @type {{gradeId: string, name: string}}
     */
    $scope.condition = {
        isQuery: false,
        pageNumber: 0,
        pageSize: 20,
        gradeId: "",
        name: "",
        selectedLength: 0,
        title: ""
    };

    $scope.gradeList = [];
    $scope.classList = [];
    $scope.selectClass = [];
    $scope.length = 0;
    $scope.seleectArr = [];

    /**
     * 查询年级
     */
    $scope.queryThreeGrade = function () {
        $scope.condition.isQuery = true;
        $scope.gradeList = [{
            id: "", name: "不限"
        }];
        $http.get(basePath + "/queryAllGrade.htm?" +"_timestamp=" + new Date().getTime()).success(function (data) {
            if (data.status == 0) {
                $scope.condition.error = false;
                var arr = data.result;
                if (arr.length > 0) {
                    angular.forEach(arr, function (grade) {
                        $scope.gradeList.push(grade);
                    });
                    $scope.condition.gradeId = $scope.gradeList[0].id;
                    $scope.queryTeachClass(function () {
                        $scope.$broadcast('scroll.infiniteScrollComplete');
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                    });
                }
            } else {
                $scope.condition.title = "获取班级失败";
                $scope.condition.error = true;
                $scope.condition.isQuery = false;
            }
        }).error(function (data) {
            $scope.condition.title = "获取班级失败";
            $scope.condition.error = true;
            $scope.condition.isQuery = false;
        })
    };

    /**
     * 查询班级
     */
    var _items = storageService.getItemNoDel("teachClass");
    $scope.queryTeachClass = function (fun) {
        $scope.condition.isQuery = true;
        $http.get(basePath + "/queryPKClassByPage.htm?gradeId=" + $scope.condition.gradeId + "&name=" + $scope.condition.name + "&pageSize=" + $scope.condition.pageSize + "&pageNumber=" + $scope.condition.pageNumber+"&_timestamp=" + new Date().getTime()).success(function (data) {
            if (data.status == 0) {
                var arr = data.result.content;
                if (arr.length == 0) {
                    $scope.condition.title = "没有可选班级";
                    $scope.condition.error = false;
                } else {
                    $scope.condition.error = false;
                    if (_items && _items.length > 0) {
                        angular.forEach(_items,function(clazz){
                            clazz.selected = true;
                        });
                        $scope.seleectArr = _items;
                        $scope.length = $scope.seleectArr.length ;
                        angular.forEach(arr, function (clazz) {
                            angular.forEach(_items, function (c) {
                                if (c.id == clazz.id) {
                                    clazz.selected = true;
                                }
                            });
                            $scope.classList.push(clazz);
                        });
                    }else{
                        angular.forEach(arr,function(cla){
                            angular.forEach($scope.seleectArr, function (c) {
                                if (c.id == cla.id) {
                                    cla.selected = true;
                                }
                            });
                            $scope.classList.push(cla);
                        });
                    }
                    $scope.condition.pageSize = data.result.size;
                    $scope.condition.pageNumber = data.result.number;
                    $scope.condition.itemSize = data.result.totalElements;
                    $scope.condition.totalPage = data.result.totalPages;
                    $scope.classMore = $scope.condition && $scope.classList.length < $scope.condition.itemSize;
                }
            } else {
                $scope.condition.title = "获取班级失败";
                $scope.condition.error = true;
            }
            $scope.condition.isQuery = false;
            if(fun){
                fun();
            }
        }).error(function () {
            $scope.condition.title = "获取班级失败";
            $scope.condition.error = true;
            $scope.condition.isQuery = false;
            if(fun){
                fun();
            }
        })
    };

    /**监控
     *
     */
    $scope.$on('stateChangeSuccess', function () {
        $scope.loadMore();
    });

    /**
     * 加载更多
     */
    $scope.loadMore = function () {
        if ($scope.condition.pageNumber >= $scope.condition.totalPage) {
            return false;
        }
        $scope.condition.pageNumber = $scope.condition.pageNumber + 1;
        $scope.queryTeachClass(function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            // $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };

    /**
     * 监控
     */
    $scope.initSelectClass = function(){
        $scope.classList = [];
        $scope.condition.pageNumber = 0;
        $scope.condition.pageSize = 20;
        $scope.queryTeachClass(function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };

    /**
     * 选中年级
     */
    $scope.getSelectGrade = function (item) {
        $scope.condition.gradeId = item.id;
        $scope.condition.pageNumber = 0;
        $scope.condition.pageSize = 20;
        $scope.classList = [];
        $scope.queryTeachClass(function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };
    /**
     * 选择被选中的班级
     * @param item
     */
    $scope.getSelectClass = function (item) {
        item.selected = !item.selected;
        var count = 0;
        if(item.selected){
            angular.forEach($scope.classList, function (_item) {
                if (_item.selected && !$scope.seleectArr.some(function(obj){
                        return obj.id == _item.id
                    })) {
                    $scope.seleectArr.push(item);
                    count++;
                }
            });
        }else{
            $scope.deleteSelectClass(item);
        }
        $scope.length = $scope.seleectArr.length;
        $scope.condition.selectedLength = count;

    };


    /**
     *   删除选择的数据
     * @param index
     */
    $scope.deleteSelectClass = function(item){
        item.selected = false;var index = -1;
        angular.forEach($scope.seleectArr,function(_item,_index){
            if(item.id == _item.id){
                index = _index;
            }
        });
        if(index != -1){
            var _item = $scope.seleectArr[index];
            angular.forEach($scope.classList,function(clazz){
                if(clazz.id  == _item.id){
                    clazz.selected = false;
                }
            });
            $scope.seleectArr.splice(index,1);
            $scope.length = $scope.seleectArr.length;
        }else{
            console.error("angular.forEach get the index error ,please check!")
        }
    };
    /**
     * 取消
     */
    $scope.cancel = function () {
        $location.path("/patrol_register_more");
    };

    /**
     * 完成选择
     */
    $scope.overSelectClass = function () {
        storageService.setItem("teachClass", $scope.seleectArr);
        $location.path("/patrol_register_more");
    };
    /**
     * 清除输入的班级的名称
     */
    $scope.clearCondition = function () {
        $scope.condition.name = "";
    };


    $scope.queryThreeGrade(function () {
        $scope.$broadcast('scroll.infiniteScrollComplete');
        $ionicScrollDelegate.scrollTop();
        $ionicLoading.hide();
    });

    /*查看已选班级浮动框*/
    $scope.isShowMoreCourse = false;
    $scope.selectClass_view = function(){
        $scope.isShowMoreCourse = !$scope.isShowMoreCourse;
    };
}]);
/**
 * 选择课程
 */
angular.module('starter').controller("courseController", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService) {

    /**
     * 路由信息
     * @type {string}
     */
    var basePath = originBaseUrl + "/third/xcMobileRecord";
    /**
     * 查询条件
     * @type {{gradeId: string, name: string}}
     */
    $scope.condition = {
        isQuery: false,
        pageNumber: 0,
        pageSize: 20,
        name: "",
        title: ""
    };
    $scope.courseList = [{
        "courseId":"-1",
        "courseName":"自习课",
        "courseNo":null,
        "courseTeachType":"如果没有找到相应课程，可以选择此项"
    }];
    var condition = storageService.getItemNoDel("courseCondition");
    /**
     * 查询班级
     */
    $scope.queryTeachCourse = function (fun) {
        $scope.condition.isQuery = true;
        $http.get(basePath + "/queryMobileCourseVO.htm?name=" + $scope.condition.name +
            "&pageSize=" + $scope.condition.pageSize +
            "&pageNumber=" + $scope.condition.pageNumber +
            "&classDate=" + condition.classDate +
            "&classRoomId=" + condition.classRoomId +
            "&knob=" + condition.knob +
            "&termId=" + condition.termId+
            "&_timestamp=" + new Date().getTime()
        ).success(function (data) {
            if (data.status == 0) {
                var arrs = data.result;
                if (!arrs || !arrs.content || arrs.content.length == 0) {
                    $scope.condition.title = "没有可选课程";
                    $scope.condition.error = false;
                }else{
                    angular.forEach(arrs.content,function(item){
                        $scope.courseList.push(item);
                    });
                    $scope.condition.pageSize = data.result.size;
                    $scope.condition.pageNumber = data.result.number;
                    $scope.condition.itemSize = data.result.totalElements;
                    $scope.condition.totalPage = data.result.totalPages;
                    initMore();
                }
            } else {
                $scope.condition.title = "获取课程失败";
                $scope.condition.error = true;
            }
            if (arrs) {
                $scope.courseList = arrs.content;
                $scope.condition.isQuery = false;
            }
        }).error(function () {
            $scope.condition.title = "获取课程失败";
            $scope.condition.error = true;
            $scope.condition.isQuery = false;
            if (fun) {
                fun();
            }
        })
    };
    /**
     * 清楚查询条件
     */
    $scope.clearCondition = function () {
        $scope.condition.pageNumber= 0;
        $scope.condition.pageSize=20;
        $scope.condition.name = "";
    };
    /**
     * 刷新数据
     */
    $scope.doRefresh = function () {
        $scope.condition.pageNumber= 0;
        $scope.condition.pageSize=20;
        $scope.courseList = [];
        $scope.queryTeachCourse(function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };

    /**
     * 加载更多
     */
    function initMore() {
        if(!$scope.loadMore){
            $scope.loadMore = function () {
                if ($scope.condition.pageNumber >= $scope.condition.totalPage) {
                    return false;
                }
                $scope.condition.pageNumber = $scope.condition.pageNumber + 1;
                $scope.queryTeachCourse(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    // $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
        }
    }


    /**
     * 消息填写
     */
    $scope.intCourseWatch = function(){
        $scope.condition.pageNumber = 0;
        $scope.condition.pageSize = 20;
        $scope.teacherList = [];
        $scope.queryTeachCourse(function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };
    /**
     * 选中
     * @param item
     */
    $scope.selectCourse = function (item) {
        storageService.setItem("select-course", item);
        $location.path("/patrol_register_more");
    };
    /**
     * 取消
     */
    $scope.cancelCourse = function () {
        $location.path("/patrol_register_more");
    };
}]);
/***
 * 查询选课教师 和多选
 */
angular.module('starter').controller("TeacherController", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService) {
    /**
     *   查询的内容
     *   1 查询上课的教师的信息   2 查询巡查教师
     */
    $scope.type = $location.search().type;
    /**
     * 路由信息
     * @type {string}
     */
    var basePath = originBaseUrl + "/third/xcMobileRecord";
    /**
     * 查询条件
     * @type {{gradeId: string, name: string}}
     */
    $scope.condition = {
        isQuery: false,
        length: 0,
        pageNumber: 0,
        pageSize: 20,
        name: "", //查询条件
        title: "", //显示信息
        character: ""// 字母
    };

    $scope.teacherList = [];
    $scope.seleectTeacher = [];
    $scope.length = 0;
    /**
     * 查询班级
     */
    var _items = storageService.getItemNoDel("teachers");
    $scope.queryClassTeacher = function (fun) {
        $scope.condition.isQuery = true;
        $http.get(basePath + "/queryClassTeacher.htm?value=" + ($scope.condition.name ? $scope.condition.name : "") +
            "&pageSize=" + $scope.condition.pageSize +
            "&pageNumber=" + $scope.condition.pageNumber +
            "&character=" + ($scope.condition.character ? $scope.condition.character : "") +
            "&type=" + $scope.type +
            "&_timestamp=" + new Date().getTime()
        ).success(function (data) {
            if (data.status == 0) {
                var arr = data.result ? data.result.content : [];
                if (!data.result || arr.length == 0) {
                    $scope.condition.title = "没有可选教师";
                    $scope.condition.error = false;
                    $scope.condition.more = false;
                }else{
                    var count = 0;
                    if(_items && _items.length > 0){
                        angular.forEach(_items,function(tea){
                            tea.selected = true;
                        });
                        $scope.seleectTeacher =_items;
                    }
                    $scope.length = $scope.seleectTeacher.length;
                    angular.forEach(arr, function (item) {
                        item.selected = false;
                        if ($scope.type == 1 && $scope.seleectTeacher.some(function(key){return key.userId == item.userId})) {
                            item.selected = true;
                            count++;
                        }
                        $scope.teacherList.push(item);
                    });
                    $scope.condition.length = count;

                    $scope.condition.pageSize = data.result.size;
                    $scope.condition.pageNumber = data.result.number;
                    $scope.condition.itemSize = data.result.totalElements;
                    $scope.condition.totalPage = data.result.totalPages;

                    $scope.more = $scope.condition && $scope.teacherList.length < $scope.condition.itemSize;
                }
            } else {
                $scope.condition.title = "获取教师失败";
                $scope.condition.error = true;
            }
            $scope.condition.isQuery = false;
            if (fun) {
                fun();
            }
        }).error(function () {
            $scope.condition.title = "获取教师失败";
            $scope.condition.error = true;
            $scope.condition.isQuery = false;
            if (fun) {
                fun();
            }
        })
    };

    /**
     * 清楚查询条件
     */
    $scope.clearCondition = function () {
        $scope.condition.pageNumber = 0;
        $scope.condition.pageSize = 20;
        $scope.condition.name = "";
        $scope.teacherList = [];
    };
    /**
     * 刷新数据
     */
    $scope.doRefresh = function () {
        $scope.condition.pageNumber = 0;
        $scope.condition.pageSize = 20;
        $scope.condition.name = "";
        $scope.teacherList = [];
    };

    $scope.$on('stateChangeSuccess', function () {
        $scope.loadMore();
    });

    /**
     * 加载更多
     */
    $scope.loadMore = function () {
        if ($scope.condition.pageNumber >= $scope.condition.totalPage) {
            return false;
        }
        $scope.condition.pageNumber = $scope.condition.pageNumber + 1;
        $scope.queryClassTeacher(function () {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            // $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    };

    /**
     * 选择字母
     */
    $scope.selectCharacter = function (char) {
        $scope.condition.character = char;
        $scope.teacherList = [];
        $scope.condition.pageNumber = 0;
        $scope.condition.pageSize = 20;
        $scope.queryClassTeacher();
    };

    /**
     * 选择教师
     * @param teacher
     */
    $scope.overSelect = function (teacher) {
        storageService.clearKey("teacher");
        storageService.setItem("teacher", teacher);
        $scope.cancelTeacher();
    };

    /**
     * 多选
     * @param tea
     */
    $scope.selectTeacher = function (tea) {
        tea.selected = !tea.selected;
        var count = 0;
        if(tea.selected){
            angular.forEach($scope.teacherList, function (teacher) {
                if (teacher.selected == true && !$scope.seleectTeacher.some(function(key){return key.userId == teacher.userId})) {
                    $scope.seleectTeacher.push(teacher);
                }
            });
        }else{
            var index = -1;
            angular.forEach($scope.seleectTeacher,function(_tea,_index){
                if(_tea.userId == tea.userId){
                    index = _index;
                }
            });
            if(index != -1){
                $scope.seleectTeacher.splice(index,1);
            }
        }
        angular.forEach($scope.teacherList,function(item){
            if(item.selected){
                count ++;
            }
        });
        $scope.length  = $scope.seleectTeacher.length;
        $scope.condition.length = count;
    };

    /**
     *  删除选择的对象
     * @param index
     */
    $scope.deleteSelectTeacher = function(index){
        var item = $scope.seleectTeacher[index],count = 0;
        $scope.seleectTeacher.splice(index,1);
        angular.forEach($scope.teacherList, function (teacher) {
            if(teacher.userId == item.userId){
                teacher.selected = false;
            }
        });
        angular.forEach($scope.teacherList, function (teacher) {
            if(teacher.selected){
                count++;
            }
        });
        $scope.length  = $scope.seleectTeacher.length;
        $scope.condition.length = count;
    };

    /**
     * 开启监控
     */
    $scope.$watch("condition.name",function (newValue,oldValue) {
        var setTime  = window.setTimeout(function(){
            window.clearTimeout(setTime);
            $scope.condition.pageNumber = 0;
            $scope.condition.pageSize = 20;
            $scope.teacherList = [];
            $scope.queryClassTeacher(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        },200)
    });
    /**
     * 返回的信息
     */
    $scope.overSelectTeachers = function () {
        if ($scope.type == 2) {
            return false;
        }
        if($scope.seleectTeacher.length == 0){
            ynuiNotification.error({msg: "请选择教师！"});
            return false;
        }
        storageService.setItem("teachers", $scope.seleectTeacher);
        $location.path("/patrol_register_more");
    };
    /**
     * 取消
     */
    $scope.cancelTeacher = function () {
        $location.path("/patrol_register_more");
    };

    /*查看已选班级浮动框*/
    $scope.selectClass_view = function(){
        $scope.isShowMoreCourse = !$scope.isShowMoreCourse;
    };

}]);
/**
 * 备注
 */
angular.module('starter').controller("remarkController", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification", "storageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification, storageService) {

    /**
     * 报错工具
     * @param message
     */
    var notifyError = function (message) {
        if (message == null || message == "") {
            message = "获取数据失败";
        }
        ynuiNotification.error({msg: message});
    };

    /**
     * 字符串处理
     * @param str
     * @returns {boolean}
     */
    var isNotBlank = function (str) {
        if (null == str) {
            return false;
        }
        if (/\S+/.test(str)) {
            return true;
        }
        return false;
    };

    /**
     * 备注
     */
    $scope.remark = storageService.getItemNoDel("remark");
    /***
     * 取消备注
     */
    $scope.cancelPopRemark = function () {
        $location.path("/patrol_register_more");
    };

    /**
     * 确定
     */
    $scope.okPopRemark = function () {
        storageService.setItem("remark", $scope.remark);
        $scope.cancelPopRemark();
    };
}]);