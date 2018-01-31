/**
 * Created by yineng on 2016/12/30.
 */
angular.module('starter')
    .config(function ($stateProvider, $urlRouterProvider) {
        $stateProvider
            /*登记任务-列表*/
            .state('register_task', {
                url: '/register_task',
                templateUrl: 'templates/register/register_task.html',
                controller: 'registerTaskController',
                cache: false
            })
            /*登记*/
            .state('register', {
                url: '/register',
                templateUrl: 'templates/register/register.html',
                controller: 'registerCtrl',
                cache: false
            })
            /*项目打分*/
            .state('project_grade', {
                url: '/project_grade',
                templateUrl: 'templates/register/project_grade.html',
                controller: 'projectGradeCtrl',
                cache: false
            })
            /*选择教师*/
            .state('chooseTeacher', {
                url: '/chooseTeacher',
                templateUrl: 'templates/register/chooseTeacher.html',
                controller: 'chooseTeacherController',
                cache: false
            })
            /*授课计划*/
            .state('lessons_plan', {
                url: '/lessons_plan',
                templateUrl: 'templates/register/lessons_plan.html',
                controller: 'lessonsPlanCtrl',
                cache: false
            })
            /*填写备注*/
            .state('fill_remark', {
                url: '/fill_remark',
                templateUrl: 'templates/register/fill_remark.html',
                controller: 'fillRemarkCtrl',
                cache: false
            })
            /*登记额外任务-基本信息*/
            .state('basic_msg', {
                url: '/basic_msg',
                templateUrl: 'templates/register/basic_msg.html',
                controller: 'basicMsgController',
                cache: false
            })
            /*教室分类*/
            .state('classroomClassify', {
                url: '/classroomClassify',
                templateUrl: 'templates/register/classroomClassify.html',
                controller: 'classroomClassifyController',
                cache: false
            })
            /*搜索教室*/
            .state('classroomSearch', {
                url: '/classroomSearch',
                templateUrl: 'templates/register/classroomSearch.html',
                controller: 'classroomSearchController',
                cache: false
            })
            /*按类型查看教室*/
            .state('classroomCheck', {
                url: '/classroomCheck',
                templateUrl: 'templates/register/classroomCheck.html',
                controller: 'classroomCheckController',
                cache: false
            })
        $urlRouterProvider.otherwise('/register_task');
    }).factory("tkStorageService", function () {
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
    })
    .controller('registerTaskController',['$scope','$http','$location','$ionicPopup','$ionicModal','$ionicLoading',"tkStorageService","$ionicScrollDelegate","$rootScope",function($scope,$http,$location,$ionicPopup,$ionicModal,$ionicLoading,tkStorageService,$ionicScrollDelegate,$rootScope){
        $scope.topTermId = tkStorageService.getItem("topTermId");
        var getTerm = function(func){
            $scope.termList = [];
            $scope.termId = "";
            $scope.termName = "";
            $scope.taskRegisterVOs = [];
            $http.post(originBaseUrl+'/third/tkOutsideTaskRegisterApp/getPreviousAndCurrentTermList.htm')
                .success(function (data){
                    if(data.status==0){
                        if(data.result&&data.result.length>0){
                            $scope.termList = data.result;
                            if($scope.topTermId){
                                angular.forEach($scope.termList,function(obj){
                                    if(obj.termId == $scope.topTermId){
                                        $scope.termId = obj.termId;
                                        $scope.termName = obj.termName;
                                    }
                                });
                            }else{
                                $scope.termId = $scope.termList[0].termId;
                                $scope.termName = $scope.termList[0].termName;
                            }
                            if($scope.termId){
                                $scope.getData();
                            }
                            $scope.emptyInfoTerm = false;
                        }else{
                            $scope.emptyInfoTerm = true;
                            $scope.dataErrorMsgTerm ='当前时间不在当前学期和上学期内，无法进行听课登记';
                        }
                        if (func) {
                            func();
                        }
                    }else{
                        if (func) {
                            func();
                        }
                        $scope.emptyInfoTerm = true;
                        $scope.dataErrorMsgTerm ='加载学期失败';
                    }
                })
                .error(function(){
                    if (func) {
                        func();
                    }
                    $scope.emptyInfoTerm = true;
                    $scope.dataErrorMsgTerm ='加载学期失败';
                });
        };
        getTerm();
        $scope.pageInfo = {
            totalPage:0,
            pageSize:20,
            pageNumber:0,
            itemSize:0
        };
        $scope.clickTerm = function(item){
            $scope.termId = item.termId;
            $scope.termName = item.termName;
            $scope.pageInfo = {
                totalPage:0,
                pageSize:20,
                pageNumber:0,
                itemSize:0
            };
            $scope.taskRegisterVOs = [];
            $scope.getData(function () {
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };
        $scope.getData = function (func) {
            $http.post(originBaseUrl + '/third/tkTaskRegisterApp/queryTaskRegisterAppPage.htm?termId='+$scope.termId+"&pageNumber=" + $scope.pageInfo.pageNumber + "&pageSize="
                + $scope.pageInfo.pageSize + "&time=" + (new Date()).getTime()).success(function (data) {
                if (data.status == 0) {
                    $scope.taskRegisterVOs.push.apply($scope.taskRegisterVOs,data.result.content);
                    $scope.pageInfo = {
                        totalPage:data.result.totalPages,
                        pageSize:data.result.size,
                        pageNumber:data.result.number,
                        itemSize:data.result.totalElements
                    };
                    if(data.result.totalPages>0){
                        $scope.pageInfo.pageNumber = data.result.number + 1;
                    }
                    if(! $scope.taskRegisterVOs|| $scope.taskRegisterVOs.length<1){
                        $scope.emptyInfo = true;
                        $scope.dataErrorMsg ='没有听课任务';
                    }else{
                        $scope.emptyInfo = false;
                    }
                    if (func) {
                        func();
                    }
                }else{
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg ='加载失败';
                }
            }).error(function () {
                if (func) {
                    func();
                }
                $scope.emptyInfo = true;
                $scope.dataErrorMsg ='加载失败';
            });
        };

        if($scope.termId){
            $scope.getData();
        }
        //刷新
        $scope.doRefresh = function () {
            $ionicLoading.show({
                template: '正在刷新...'
            });
            //置空数据，在获取时候叠加数据
            $scope.taskRegisterVOs = [];
            $scope.pageInfo = {
                totalPage:0,
                pageSize:20,
                pageNumber:0,
                itemSize:0
            };
            $scope.getData(function () {
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
        };

        $scope.$on('stateChangeSuccess', function () {
            $scope.loadMore();
        });

        /**
         * 加载更多
         */
        $scope.loadMore = function () {
            if(!$scope.termId){
                return false;
            }
            if ($scope.pageInfo.pageNumber >= $scope.pageInfo.totalPage) {
                return false;
            }
            $scope.getData(function () {
                $scope.$broadcast('scroll.infiniteScrollComplete');
                $ionicLoading.hide();
            });
        };
        //学期异常 刷新
        $scope.reloadData = function(){
            $ionicLoading.show({
                template: '正在刷新...'
            });
            getTerm(function(){
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });
            $scope.pageInfo = {
                totalPage:0,
                pageSize:20,
                pageNumber:0,
                itemSize:0
            };
        };

        //登记任务外听课
        $scope.toBasicMsg = function(){
            tkStorageService.clearKey("tkRegisterData");
            tkStorageService.clearKey("tkBasicInfo");
            tkStorageService.setItem("queryType",2);
            tkStorageService.clearKey("queryId");
            tkStorageService.setItem("topTermId",$scope.termId);
            $location.search().isChooseTeacher = null;
            $location.search().isChangeTeacher = null;
            $location.search().isFromChooseClassRoom = null;
            $location.path("/basic_msg");
        };
        //登记或修改
        $scope.toRegister = function(queryId,queryType){

            tkStorageService.clearKey("tkRegisterData");
            tkStorageService.clearKey("tkBasicInfo");
            //    * 1：听课任务ID（通过任务，登记听课结果）
            //    * 3：听课登记结果ID（查询登记结果修改）
            tkStorageService.setItem("queryType",queryType);
            tkStorageService.setItem("queryId",queryId);
            tkStorageService.setItem("topTermId",$scope.termId);
            $location.path("/register");
        };
        //删除还没通过的听课记录
        $scope.deleteItem = function(index,item){
            var confirmPopup = $ionicPopup.confirm({
                title:"提示",
                template: '确定要删除吗？',
                cancelText:"取消",
                okText:"确认"
            });
            confirmPopup.then(function(res) {
                if(res) {
                  $http.post(originBaseUrl+"/third/tkTaskRegisterApp/deleteTask.htm?",{id:item.id,taskId:item.taskId}).success(function(data){
                      if(data.status==0){
                          if(item.hasTask){
                              item.id = item.taskId;
                              item.submit = false;
                          }else{
                              $scope.taskRegisterVOs.splice(index,1);
                              $scope.pageInfo.itemSize-=1;
                          }
                      }
                  });
                }
            });
        };
    }]);