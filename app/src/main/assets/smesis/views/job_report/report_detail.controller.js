(function () {
    'use strict';
    angular.module('myApp').controller('jobReportDetailController', jobReportDetailController);
    jobReportDetailController.$inject = ['$scope','$ionicPopup','$ionicModal','$location','$http','$ionicLoading','Attachment','$timeout','$ionicScrollDelegate'];
    function jobReportDetailController($scope,$ionicPopup,$ionicModal,$location,$http,$ionicLoading,Attachment,$timeout,$ionicScrollDelegate) {
        $scope.id = $location.search().id;
        $scope.type = $location.search().isTab;
        $scope.reportData = {};  //页面展示数据
        //点击 顶部tab
        $scope.getDataFun = function (id) {
            //重新计算页面大小
            $ionicScrollDelegate.resize();
            $scope.isTab =1;
            var suFun = function (data) {
                $scope.reportData = angular.copy(data);
                if($scope.reportData && $scope.reportData.reportContent){
                    $scope.reportData.instructionShow = $scope.getTitle($scope.reportData.reportContent);
                }
                if($scope.reportData.fsAnnexIDs){
                    $scope.reportData.uploadVoList = $scope.reportData.fsAnnexIDs.split(",");
                }
                //如果有反馈批示默认选中反馈批示否则选中基本信息

            }
            httpVisit("findJSTXWorkReportDTOById", {reportId: $scope.id}, {}, suFun);
        };
        /**
         *  获取 抄送人
         */
        $scope.getUserName = function (list) {
            var names = [];
            if(list && list.length==0) return  "无";
            angular.forEach(list, function (val) {
                names.push(val.userName);
            });
            return names.toString();
        }
        /**
         * 删除
         */
        $scope.del = function ( id) {
            var fun = function () {
                var suFun = function (data) {
                    $scope.jumpFun.back();
                }
                httpVisit("delJSTXWorkReport", {reportId: id}, {}, suFun);
            }
            getMessageFun("删除","确定要删除此工作报告吗？",fun, true);
        };

        /**
         *  查看附件
         */
        $scope.viewFileFun = function (fsIds) {
             if(fsIds && fsIds.length > 0){
                Attachment.show(fsIds.toString());
            }
        };

        /**
         *  跳转 方法
         */
        $scope.jumpFun = {
            back: function () {
                $location.path('/job_report').search({isTab: $scope.type});
            },
            update: function () {
                $location.path('/add_report').search({id: $scope.id});
            }
        };

        //反馈和批示
        $scope.feedBack = function(item){
            $scope.report ={};
            $scope.show ={};
            $scope.feedType ="";
            if(item != null && item=="0"){
                $scope.feedType =0;
                $scope.show.showText ="反馈";
                $scope.show.message ="反馈成功!"
            }else{
                $scope.feedType =1;
                $scope.show.showText ="批示";
                $scope.show.message ="批示成功!"
            }
            //item 0 反馈 1 批示
            var confirmPopup = $ionicPopup.show({
                title : ''+$scope.show.showText+'',
                template : '<textarea type="input" rows="5" class="form-control"  placeholder="输入'+$scope.show.showText+'内容" maxlength="1000" ng-model="report.content"></textarea>',
                scope: $scope,
                buttons : [
                    {
                        text : '取消',
                        type : 'button-theme button-local button-outline'
                    },
                    {
                        text : '确认',
                        type : 'button-theme button-local',
                        onTap: function (e) {
                            if (!$scope.report.content) {
                                $scope.showFirm (""+$scope.show.showText+"内容不能为空!");
                                e.preventDefault();
                            }else{
                                var dataInfo = {
                                    type : $scope.feedType,
                                    workReportId : $scope.id,
                                    content : $scope.report.content
                                }
                                $http.post(basePath + "/third/workreport/saveFeedBackMessage?", dataInfo).success(function (data) {
                                    if (data.status == "0") {
                                        $scope.getAppoval();
                                    } else {
                                        if(item && item==0){
                                            $scope.show.message ="反馈失败!"
                                        }else{
                                            $scope.show.message ="批示失败!"
                                        }
                                        $scope.showFirm($scope.show.message)
                                    }


                                })
                            }

                        }
                    }
                ]
            });


        }

        //提示信息
        $scope.showFirm = function(msg){
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        };

        //获取反馈和批示记录
        $scope.getAppoval = function(type){
            //重新计算页面大小
            $ionicScrollDelegate.resize();
            if(!type){
                $scope.isTab =2;
            }
            $http.post(basePath + "/third/workreport/findAppovalDataById?reportId=" + $scope.id).success(function (data) {
                if (data.status =='0') {
                    $scope.appovalData = data.result;
                    if($scope.appovalData.length >0){
                        $scope.isTab =2;
                        angular.forEach($scope.appovalData,function(item){
                            item.instructionShow =$scope.getTitle(item.content);
                        });
                    }else{
                        if(type){
                            $scope.isTab =1;
                        }

                    }
                }
            });
        }
        $scope.getAppoval(1);

        //按时间倒序排列
        $scope.timedesc = true;
        $scope.timeDesc = function(){
            $scope.timedesc = !$scope.timedesc;

        }

        //对字符串进行截取
        $scope.getTitle = function(data){
            var title ="";
            if(data != null && data.length >50){
                $scope.showTitle =true;
                title = data.substring(0,50)+"...";
            }else{
                title = data;
            }
            return  title;

        }

        //查看更多
        $scope.showMore = function(item,type){
            if(item.instructionShow.length > 53){
                if(type==2){
                    item.instructionShow = $scope.getTitle(item.content);
                }else{
                    item.instructionShow = $scope.getTitle(item.content);
                }

            }else{
                if(type==2){
                    item.instructionShow = item.content;
                }else{
                    item.instructionShow ="";
                    item.instructionShow = item.reportContent;
                }

            }


        }



        /**
         *  获取 数据方法
         * @param url  路径
         * @param params get数据
         * @param data post 数据
         * @param fun 访问成功 方法
         * @param errFun 访问失败方法
         */
        var httpVisit = function (url,params,data,fun,errFun){
            params = params || angular.noop;
            data = data || angular.noop;
            fun = fun || angular.noop;
            errFun = errFun || angular.noop;
            var config= {
                url: basePath + "/third/workreport/" + url,
                method:"post",
                params:params, //后台用 get接收
                data: data, //后台用@RequestBody 接收
            };
            $http(config).success(function(resultData){
                fun && fun(resultData);
            }).error(function (resultData) {
                $scope.isError = true;
                errFun && errFun(resultData);
            });
        };
        // 提示 信息
        var getMessageFun = function ( title, message, fun, isCancel) {
            var config = {
                title: title,
                template: '<h5> ' + message + '</h5>',
                buttons : [
                    {text : '确认', type : ' button-theme button-local',
                        onTap: function () {
                           fun && fun();
                        }
                    }
                ]
            };
            if(isCancel){
                config.buttons.push({text : '取消', type : ' button-theme button-local'});
            }
            $ionicPopup.show(config);
        }

        $scope.getDataFun($scope.id);
    }
})();
