/**
 * Created by wuhaiying on 2017/11/10.
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('performanceReviewCtrl', performanceReviewCtrl);

    performanceReviewCtrl.$inject = ['$scope','$ionicPopup', '$http', '$timeout', '$ionicLoading', '$ionicModal', '$filter'];
    function performanceReviewCtrl($scope, $ionicPopup,$http, $timeout, $ionicLoading, $ionicModal, $filter){
        /***********************************************xiaobowen 主查询页面开始 ******************************/
        //查询条件
        $scope.conditions = {interviewMonth:"",name:""};
        //
        //处理月份
        var dealMonth = function (month,type) {
            if(type == 1){
                $scope.conditions.interviewMonth = $scope.mainSearchYear + "-";
                $scope.mainPageYear = angular.copy($scope.mainSearchYear);
                $scope.mainSearchMonth = angular.copy(month);
                if(month.toString().length == 1){
                    //1-9月 拼接0
                    $scope.conditions.interviewMonth += "0"+month;
                }else{
                    //10-12月
                    $scope.conditions.interviewMonth += month;
                }
                $scope.notInterviewSearchFun();
                $scope.interviewSearchFun();
            }else if(type == 2){
                if(month == 1){
                }
                $scope.childConditions.interviewMonth = $scope.childSearchYear + "-";
                $scope.childPageYear = angular.copy($scope.childSearchYear);
                $scope.childSearchMonth = angular.copy(month);
                if(month.toString().length == 1){
                    //1-9月 拼接0
                    $scope.childConditions.interviewMonth += "0"+month;
                }else{
                    //10-12月
                    $scope.childConditions.interviewMonth += month;
                }
                if($scope.childConditions.name != ""){
                    //此时可以开始查询
                    $scope.searchChildPageFun();
                }
            }
        };
        //获取当前日期
        var nowDate = $filter("date")(new Date(), "yyyy-MM");
        //本年
        $scope.nowYear = angular.copy(nowDate.toString().split('-')[0]);
        //本月
        $scope.nowMonth = angular.copy(nowDate.toString().split('-')[1]);
        //初始化显示月份
        $scope.mainPageMonth = angular.copy(nowDate.toString().split('-')[1]);
        //初始化所有年月查询组件date
        var initYearMonth = function (type){
            if(type == 1){
                //主页面搜索年份初始化
                $scope.mainSearchYear = angular.copy(nowDate.toString().split('-')[0]);
                //主页面搜索月份选中效果初始化
                $scope.mainSearchMonth = angular.copy(nowDate.toString().split('-')[1]);
            }else if(type == 2){
                //子页面搜索搜索年份初始化
                $scope.childSearchYear = angular.copy(nowDate.toString().split('-')[0]);
                //子页面搜索搜索月份选中效果初始化
                $scope.childSearchMonth = angular.copy(nowDate.toString().split('-')[1]);
            }
        };
        //获取派发最近日期
        $http.post(basePath + "/third/jxglPerformInterview/currentJXGLPlanByStatus").success(function (data) {
            if(data.status =="0"){
                var sendDate = data.result;
                //此处初始化年月查询条件
                $scope.mainSearchYear = angular.copy(sendDate.toString().split('-')[0]);
                $scope.mainSearchMonth = angular.copy(sendDate.toString().split('-')[1]);
                $scope.mainPageYear = angular.copy(sendDate.toString().split('-')[0]);
                var initMonth = angular.copy(sendDate.toString().split('-')[1]);
                if(initMonth.toString().substring(0,1) == "0"){
                    $scope.mainPageMonth = initMonth.toString().substring(1,2);
                }else{
                    $scope.mainPageMonth = initMonth.toString();
                }
            }
            dealMonth($scope.mainPageMonth,1);
        });
        //初始化加载未面谈数据
        $scope.nowPage = 1;
        //错误提示
        $scope.oneTypeErrorFlag = false;
        $scope.oneTypeNoResultFlag = false;
        $scope.twoTypeErrorFlag = false;
        $scope.twoTypeNoResultFlag = false;
        $scope.threeTypeErrorFlag = false;
        $scope.threeTypeNoResultFlag = false;
        $scope.onePageNumber = 0;
        $scope.twoPageNumber = 0;
        $scope.threePageNumber = 0;
        $scope.onePageMoreData = false;
        $scope.twoPageMoreData = false;
        $scope.threePageSize = 0;
        var refreshFlag = true;
        //初始化年月下拉的年份
        $scope.initSearchYear = function () {
            $scope.mainSearchYear = angular.copy($scope.mainPageYear);
        };
        //切换今年
        $scope.currentYear = function (type) {
            if(type == 1){
                initYearMonth(1);
                $scope.mainPageMonth = $scope.nowMonth;
                dealMonth(nowDate.toString().split('-')[1],type);
            }else if(type == 2){
                initYearMonth(2);
                $scope.childPageMonth = $scope.nowMonth;
                dealMonth(nowDate.toString().split('-')[1],type);
            }
        };
        //切换上一年
        $scope.lastYear = function (type) {
            if(type == 1){
                $scope.mainSearchYear = $scope.mainSearchYear * 1 - 1 ;
                $scope.mainSearchMonth = 0;
            }else if(type == 2){
                $scope.childSearchYear = $scope.childSearchYear * 1 - 1;
                $scope.childSearchMonth = 0;
            }
        };

        //切换下一年
        $scope.nextYear = function (type) {
            if(type == 1){
                $scope.mainSearchYear = $scope.mainSearchYear * 1 + 1 ;
                $scope.mainSearchMonth = 0;
            }else if(type == 2){
                $scope.childSearchYear = $scope.childSearchYear * 1 + 1;
                $scope.childSearchMonth = 0;
            }
        };
        //拼接年月
        $scope.chooseMonth = function (month,type) {
            refreshFlag = true;
            if(type == 1){
                $scope.mainPageMonth = month;
                $scope.mainSearchMonth = month;
                dealMonth($scope.mainPageMonth,type);
            }else if(type == 2){
                $scope.childPageMonth = month;
                $scope.childSearchMonth = month;
                dealMonth($scope.childPageMonth,type);
            }
        };
        //未面谈页面查询
        $scope.notInterviewSearchFun = function () {
            var url = "";
            if(refreshFlag){
                //清空
                $scope.notInterviewItems = [];
                //刷新
                url = basePath + "/third/jxglPerformInterview/findPerformanInterviewByStatusEqOne?pageNumber=0&pageSize="+($scope.onePageNumber+1) *50;
            }else{
                //加载
                url = basePath + "/third/jxglPerformInterview/findPerformanInterviewByStatusEqOne?pageNumber="+$scope.onePageNumber+"&pageSize=50";
            }
            $scope.oneTypeNoResultFlag = false;
            $http.post( url,$scope.conditions).success(function(data){
                if(data.status == 0){
                    if(null != data.result && data.result.content.length>0){
                        if(refreshFlag){
                            //初始化加载或刷新操作
                            $scope.notInterviewItems = data.result;
                            $scope.onePageSize = data.result.totalElements;
                            $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                        }else{
                            //加载更多
                            $scope.notInterviewItems.content = $scope.notInterviewItems.content.concat(data.result.content);
                        }
                        //此处验证当前页面是否已经是最后一页
                        if(data.result.content.length >= data.result.size){
                            $scope.onePageMoreData = true;
                            $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                        }else{
                            $scope.onePageMoreData = false;
                        }
                    }else{
                        //如果始终没有值
                        if(!$scope.notInterviewItems || !$scope.notInterviewItems.content || !$scope.notInterviewItems.content.length == 0 ){
                            $scope.oneTypeNoResultFlag = true;
                            $scope.onePageSize = 0;
                        }
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                    }
                }else{
                    $scope.onePageSize = 0;
                    $scope.oneTypeErrorFlag = true;
                }
            })
        };
        //已面谈页面查询
        $scope.interviewSearchFun = function () {
            var url = "";
            if(refreshFlag){
                //清空
                $scope.interviewItems = [];
                //刷新
                url = basePath + "/third/jxglPerformInterview/findPerformanInterviewByStatusNotEqOne?pageNumber=0&pageSize="+($scope.twoPageNumber+1) *50;
            }else{
                //加载
                url = basePath + "/third/jxglPerformInterview/findPerformanInterviewByStatusNotEqOne?pageNumber="+$scope.twoPageNumber+"&pageSize=50";
            }
            $scope.twoTypeNoResultFlag = false;
            $http.post(url,$scope.conditions).success(function(data){
                if(data.status == 0){
                    if(null != data.result && data.result.content.length>0){
                        if(refreshFlag){
                            //初始化加载或刷新操作
                            $scope.interviewItems = data.result;
                            $scope.twoPageSize = data.result.totalElements;

                            $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                        }else{
                            //加载更多
                            $scope.interviewItems.content = $scope.interviewItems.content.concat(data.result.content);
                        }
                        //验证最后一页
                        if(data.result.content.length >= data.result.size){
                            $scope.twoPageMoreData = true;
                            $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                        }else{
                            $scope.twoPageMoreData = false;
                        }
                    }else{
                        if(!$scope.interviewItems || !$scope.interviewItems.content || $scope.interviewItems.content.length == 0){
                            $scope.twoTypeNoResultFlag = true;
                            $scope.twoPageSize = 0;
                        }
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                    }
                }else{
                    $scope.twoPageSize = 0;
                    $scope.twoTypeErrorFlag = true;
                }
            })
        };

        //子页面查询
        $scope.childConditions = {interviewMonth:$scope.conditions.interviewMonth,name:""};
        //查询开始
        $scope.searchChildPageFun = function () {
            //加载数据
            $scope.loadFlag = true;
            //延时加载，防止自动加载数据递归
            $scope.threePageMoreData = false;
            $scope.threeTypeErrorFlag = false;
            $scope.threeTypeNoResultFlag = false;
            if(!$scope.childConditions.name){
                $scope.modalItems = null;
                $scope.threePageSize = 0;
                return;
            }
            $http.post(basePath + "/third/jxglPerformInterview/findPerformanInterviewByName?pageNumber="+ $scope.threePageNumber+"&pageSize=9999",$scope.childConditions).success(function(data){
                if(data.status == 0){
                    $scope.loadFlag = false;
                    if(null != data.result && data.result.content.length>0){
                        if(refreshFlag){
                            //初始化加载或刷新操作
                            $scope.modalItems = data.result;
                            //初始化页面数量
                            $scope.threePageSize = data.result.content.length;
                            $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                        }else{
                            //加载更多
                            $scope.modalItems.content = $scope.modalItems.content.concat(data.result.content);
                            $scope.threePageSize += data.result.totalElements;
                        }
                        //验证最后一页
                        if(data.result.content.length >= data.result.size){
                            $scope.threePageMoreData = true;
                            $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                        }
                    }else{
                        if(data.result && data.result.content && data.result.content.length == 0){
                            $scope.modalItems = null;
                            $scope.threePageSize = 0;
                            $scope.threeTypeNoResultFlag = true;
                        }
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic刷新数据完成，可以再次触发更新事件
                    }
                }else{
                    $scope.threePageSize = 0;
                    $scope.threeTypeErrorFlag = true;
                }
            })

        };
        //切换页面
        $scope.changePageType = function (type) {
            $scope.nowPage = type;
            $scope.conditions.interviewType = type;
        };
        //刷新操作
        $scope.doRefresh = function (type) {
            refreshFlag = true;
            if(type == 1){
                $scope.notInterviewSearchFun();
            }else if(type == 2){
                $scope.interviewSearchFun();
            }else if(type == 3){
                $scope.searchChildPageFun();
            }
        };
        //加载更多
        $scope.loadData = function(type){
            refreshFlag = false;
            if(type == 1){
                $scope.onePageNumber++;
                $scope.notInterviewSearchFun();
            }else if(type == 2){
                $scope.twoPageNumber++;
                $scope.interviewSearchFun();
            }else if(type == 3){
                $scope.searchChildPageFun();
            }
        };
        /***********************************************xiaobowen 主查询页面结束 ******************************/

        $scope.openModal = function() {
            //搜索页面
            $ionicModal.fromTemplateUrl('search.html',{
                scope: $scope,
                animation: 'slide-in-right'
            }).then(function(modal){
                $scope.modal = modal;
                $scope.modalItems = null;
                $scope.modalFlag = true;
                $scope.threePageSize = 0;
                //同步显示年月
                $scope.childPageYear = $scope.mainPageYear;
                $scope.childPageMonth = $scope.mainPageMonth;
                $scope.childSearchYear = $scope.mainPageYear;
                $scope.childSearchMonth = $scope.mainPageMonth;
                //初始化年月
                $scope.childConditions.interviewMonth = $scope.conditions.interviewMonth;
                $scope.childConditions.name = "";
                $scope.modal.show();
            });
        };
        $scope.closeModal = function() {
            $scope.modalItems = null;
            $scope.threePageSize = 0;
            $scope.threeTypeNoResultFlag = false;
            $scope.threeTypeErrorFlag = false;
            $scope.modal.remove();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modal.remove();
        });

        $scope.openModalOne = function(item) {
            getInterview(item);
        };
        //清空搜索栏
        $scope.clearSearchInput = function () {
            $scope.childConditions.name = "";
        };
        $scope.closeModalOne = function() {
            $scope.modalOne.remove();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalOne.remove();
        });

        $scope.showMiddleModal = function(data,type){
            //中间弹窗
            $ionicModal.fromTemplateUrl('middle-modal.html',{
                scope: $scope
            }).then(function(modal){
                $scope.modalTwo2 = modal;
                $scope.modalTwo2.show();
                $scope.dialogData = data;
                $scope.dialogData.type = type;
            });
        };
        $scope.closeMiddleModal = function(){
            $scope.modalTwo2.remove();
            checkData();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalTwo2.remove();
        });

        $scope.showClassDialog = function(){
            //依能课堂弹窗 xiaobowen 2017年11月22日15:50:22
            $ionicModal.fromTemplateUrl('showClassDialog.html',{
                scope: $scope
            }).then(function(modal){
                $scope.classDialog = modal;
                $scope.classDialog.show();
            });
        };
        $scope.closeClassDialog = function(){
            $scope.classDialog.remove();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.classDialog.remove();
        });
        $scope.openModalTwo = function() {
            //查看页面
            $ionicModal.fromTemplateUrl('check.html',{
                scope: $scope,
                animation: 'slide-in-right'
            }).then(function(modal){
                $scope.modalTwo = modal;
                $scope.modalTwo.show();
            });
        };
        $scope.closeModalTwo = function() {
            $scope.modalTwo.remove();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalTwo.remove();
        });

        //切换tab
        $scope.changePage = function (type) {
            $scope.showStep = type;
        };
        //展开收缩flag
        $scope.performFlag = true;
        $scope.workFlag = true;
        //展开收缩
        $scope.checkFlagFun = function (type) {
            if(type == 1){
                $scope.performFlag = !$scope.performFlag;
            }else if (type == 2){
                $scope.workFlag = !$scope.workFlag;
            }
        };
        $scope.closeViewModal = function() {
            $scope.viewModal.remove();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.viewModal.remove();
        });

        //提示信息 添加自定义 按钮和按钮方法
        var getMessage_new_fun = function ( title, mess_1, mess_2, buttons) {
            $ionicPopup.show({
                title :  title,
                template : '<h5> ' + mess_1 + '</h5>'+
                '<p class="text-gray">' + mess_2+ '</p>',
                buttons : buttons || []
            });
        };
        //提示信息
        var info = function () {
            var buttons = [
                {text : '我要填', type: ' button-outline button-theme button-local', onTap: function(){
                    $scope.showStep = 2;
                }},
                {text : '继续提交', type: ' button-theme button-local', onTap: function(){
                    $scope.save();
                }}
            ];
            getMessage_new_fun("提示","还没有填写面谈记录", "确定不需要填写吗？", buttons);
        };

        //返回提示
        $scope.backInfo = function(){
            var buttons = [
                {text : '继续面谈', type: ' button-outline button-theme button-local'},
                {text : '确定返回', type: ' button-theme button-local', onTap: function(){
                    $scope.closeModalOne();
                }}
            ];
            getMessage_new_fun("提示","返回后，当前面谈内容将丢失", "确定要返回吗？", buttons);
        };

        //面谈
        var getInterview = function(item){
            $http.post(basePath+"/third/jxglPerformInterview/findPostEvaluateSet",item).success(function(data){
                $scope.item = data.result;
                $scope.item.planId = item.planId;
                $scope.item.userId = item.userId;
                checkData();
                if(data.result.interviewStatus != 3){
                    //面谈页面
                    $ionicModal.fromTemplateUrl('interview.html',{
                        scope: $scope,
                        animation: 'slide-in-right'
                    }).then(function(modal){
                        $scope.modalOne = modal;
                        $scope.modalOne.show();
                    });
                }else{
                    //查看页面xiaobowen
                    $ionicModal.fromTemplateUrl('view.html',{
                        scope: $scope,
                        animation: 'slide-in-right'
                    }).then(function(modal){
                        $scope.viewModal = modal;
                        //此处特殊处理，如果是已确认，跳转到另外一个查看模态框
                        // xiaobowen 2017年11月20日13:55:41
                        $scope.viewModal.show();
                    });
                }
                $scope.showStep = 1;
            })
        };
        //修改显示状态
        $scope.changeType = function(type){
            $scope.showStep = type;
        };

        //面谈数据处理
        var checkData = function(){
            $scope.allDetailNum = 0;//已处理数据
            $scope.checkStatus = false; //是否填完状态

            //履职
            if($scope.item.jxglPerformInterviewDTOList){
                if($scope.item.jxglPerformInterviewDTOList.length>0){
                    var performNums = $scope.item.jxglPerformInterviewDTOList.filter(function(data){
                        return data.indexPoints||data.indexPoints==0;
                    });
                    $scope.allDetailNum += performNums.length;
                    if(performNums.length!=$scope.item.jxglPerformInterviewDTOList.length)$scope.checkStatus = true;
                    if(performNums.length>0){
                        $scope.item.preformPoints = 0;
                        angular.forEach(performNums,function(value){
                            $scope.item.preformPoints += parseFloat(value.indexPoints)*value.weight/100;
                        });
                        $scope.item.preformPoints =  $scope.item.preformPoints.toFixed(2)
                    }
                }else{
                    $scope.item.preformPoints = "未评"; //履职系数
                }
            }

            //工作
            if($scope.item.jxglWorkInterviewDTOList){
                if($scope.item.jxglWorkInterviewDTOList.length>0){
                    var workNums = $scope.item.jxglWorkInterviewDTOList.filter(function(data){
                        return data.indexConefficient||data.indexConefficient==0;
                    });
                    $scope.allDetailNum += workNums.length;
                    if(workNums.length!=$scope.item.jxglWorkInterviewDTOList.length)$scope.checkStatus = true;
                    if(workNums.length>0){
                        $scope.item.workPoints = 1;
                        angular.forEach(workNums,function(value,index){
                            if(index==$scope.item.jxglWorkInterviewDTOList.length-1){//最后一个指标
                                $scope.item.workPoints += parseFloat(value.indexConefficient);
                            }else{
                                $scope.item.workPoints =  $scope.item.workPoints*parseFloat(value.indexConefficient);
                            }
                        });
                        $scope.item.workPoints =  $scope.item.workPoints.toFixed(2)
                    }
                }else{
                    $scope.item.workPoints = "未评";    //工作系数
                }
            }
        };

        $scope.$on('$destroy', function() {
            $scope.notifyModel.remove();
        });
        
        //提交
        $scope.submit = function(){
            checkData();
            if($scope.checkStatus){ //  冒泡提示
                $scope.showErorr = true;
                //初始化
                $ionicModal.fromTemplateUrl('notify-info.html',{
                    scope: $scope
                }).then(function(modal){
                    $scope.notifyModel = modal;
                    $scope.notifyModel.show();
                });
                $timeout(function() {
                    $scope.notifyModel.remove(); //hide the modal after 3 seconds for some reason
                }, 3000);
                return;
            }
            if(!$scope.item.currentMonthWorkingPoint && !$scope.item.currentMonthWorkingQuestion && !$scope.item.nextMonthPoint){
                info();
                return;
            }
            $scope.save();
        };

        $scope.save = function(){
            $http.post(basePath+"/third/jxglPerformInterview/savePerformanInterview",$scope.item).success(function(data){
                // 冒泡提示
                $scope.showErorr = false;
                //初始化
                $ionicModal.fromTemplateUrl('notify-info.html',{
                    scope: $scope
                }).then(function(modal){
                    $scope.notifyModel = modal;
                    $scope.showErorr = false;
                    $scope.notifyModel.show();
                    $scope.notInterviewSearchFun();
                    $scope.interviewSearchFun();
                    $scope.searchChildPageFun();
                });
                $timeout(function() {
                    $scope.notifyModel.remove(); //hide the modal after 3 seconds for some reason
                    $scope.closeModalOne();
                }, 3000);
            })
        };
    }
})();
