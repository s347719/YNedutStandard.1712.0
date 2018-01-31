(function () {
    'use strict';
    angular.module('myApp').controller('jobReportAddController', jobReportAddController);
    jobReportAddController.$inject = ['$scope','$ionicPopup','$http','$filter','$ionicLoading','$ionicScrollDelegate','$location','$timeout'];
    function jobReportAddController($scope,$ionicPopup,$http,$filter,$ionicLoading,$ionicScrollDelegate,$location,$timeout) {
         $scope.id = $location.search().id;
        $scope.type = $location.search().type;
        $scope.reportData = {};  //页面展示数据
        $scope.importantList = [{dictName: "低", dictCode: "1"},{dictName: "中", dictCode: "2"},{dictName: "高", dictCode: "3"}];
        //点击 顶部tab
        $scope.getDataFun = function ( id) {
            var suFun = function (data) {
                $scope.reportData = angular.copy(data);
                $scope.userNames = $scope.getUserName($scope.reportData.userDTOList);
                // 通知
                if($scope.reportData && $scope.reportData.fsAnnexIDs){
                    $scope.$broadcast('filesDone');
                }
            };
            httpVisit("findJSTXWorkReportDTOById", {reportId: id}, {}, suFun);
        };
        /**
         *  获取文字的 长度
         */
        $scope.getContentLength = function (info) {
            if(info){
                return info.length;
            }else{
                return 0;
            }
        };

        // 获取 展示的人员名称
        $scope.getUserName = function (list) {
            var names = [];
            if(list && list.length==0) return  "无";
            angular.forEach(list, function (val) {
                names.push(val.userName);
            });
            return names.toString();
        }
        /**
         *  保存数据方法
         */
        //控制操作按钮
        $scope.opreation ={canSubmit:false};
        $scope.saveData = function () {
            var checkFun = function (item) {
                //验证 标题
                if(!item.title){
                    getMessageFun("提示信息", "请填写标题！");
                    return false;
                }
                //验证 内容
                if(!item.reportContent){
                    getMessageFun("提示信息", "请填写内容！");
                    return false;
                }
                //验证 报告类型
                if(!item.reportTypeName){
                    getMessageFun("提示信息", "请选择报告类型！");
                    return false;
                }
                //验证 重要级别
                if(!item.importantName){
                    getMessageFun("提示信息", "请选择重要级别！");
                    return false;
                }
                //验证 主送人
                if(!item.mainUserName){
                    getMessageFun("提示信息", "请选择主送人！");
                    return false;
                }
                //验证主送人和抄送人 不能一致
                if(item.userDTOList && item.userDTOList.length > 0){
                    var flag = false;
                    angular.forEach(item.userDTOList, function (val) {
                        if(val.id == item.mainUserId){
                            flag = true;
                        }
                    });
                    if(flag){
                        getMessageFun("提示信息", "主送人和抄送人重复，请重新选择！");
                        return false;
                    }
                }
                return true;
            };
            var suFun =function (data) {
                $scope.opreation.canSubmit = false;
                if(data.status == '0'){
                    $scope.jumpFun.back();
                }else{
                    getMessageFun("操作失败", data.message);
                }
            }
            //转换 附件信息
            if(checkFun($scope.reportData)){
                $scope.opreation.canSubmit = true;
                httpVisit("saveOrUpdateJSTXWorkReport",null,$scope.reportData,suFun);
            }
        };

        // 人员选择器
        $scope.selectMainUser = function (item) {
            $scope.reportData.mainUserName = item.name;
            $scope.reportData.mainUserId = item.id;
        };
        $scope.selectUserList = function ( userList ) {
            var names = [],userDTOList = [];
            angular.forEach(userList,function (val) {
                names.push(val.name);
                userDTOList.push(val);
            });
            $scope.userNames = angular.copy(names);
            $scope.reportData.userDTOList = angular.copy(userDTOList);
        };
        var getMessageFun = function ( title, message) {
            $ionicPopup.show({
                title: title,
                template: '<h5> ' + message + '</h5>',
                buttons : [
                    {text : '确认', type : ' button-theme button-local'}
                ]
            });
        };
        //提示信息 添加自定义 按钮和按钮方法
        var getMessage_new_fun = function ( title, mess_1, mess_2, buttons) {
            $ionicPopup.show({
                title :  title,
                template : '<h5> ' + mess_1 + '</h5>'+
                '<p class="text-gray">' + mess_2+ '</p>',
                buttons : buttons || []
            });
        };
        //返回 页面方法
        $scope.backMethod = function () {
            var buttons = [
                {text : '继续编辑', type: ' button-outline button-theme button-local'},
                {text : '确认返回', type: ' button-theme button-local', onTap: function(){
                    $scope.jumpFun.back();
                }}
            ];
            getMessage_new_fun("返回提示","返回后，当前编辑内容将丢失", "确定要返回吗？", buttons);
        };
        /**
         *  跳转 方法
         */
        $scope.jumpFun = {
            back: function () {
                if($scope.id){
                    $location.path('/report_detail').search({id: $scope.id, isTab: 1});
                }else{
                    $location.path('/job_report').search({isTab: 1});
                }
            },
            update: function () {
                $location.path('/add_report').search({id: $scope.id});
            }
        };
        $scope.reportTypeSelectData = {};
        $scope.importantSelectData = {};
        // 选择 报告类型
        $scope.reportTypeSelect = function (item) {
            $scope.reportData.reportTypeName = item.dictName;
            $scope.reportData.reportTypeCode = item.dictCode;
        }
        $scope.importantSelect = function (item) {
            $scope.reportData.importantName = item.dictName;
            $scope.reportData.importantStatus = item.dictCode;
        };

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
        //获得报告类型码表值
        $scope.getReportTypeList = function () {
            var suFun = function (data) {
                $scope.reportTypeList = data;
            };
            httpVisit("findReportType",null,null,suFun);
        };
        $scope.getReportTypeList();
        //判断是编辑还是 新添
        if($scope.id){
            $scope.getDataFun($scope.id);
        }
    }
})();
