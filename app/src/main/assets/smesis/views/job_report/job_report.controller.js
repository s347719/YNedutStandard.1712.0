(function () {
    'use strict';
    angular.module('myApp').controller('jobReportController', jobReportController);
    jobReportController.$inject = ['$scope', '$ionicPopup', '$ionicModal', '$location', '$http', '$ionicLoading', '$timeout'];
    function jobReportController($scope, $ionicPopup, $ionicModal, $location, $http, $ionicLoading, $timeout) {
        $scope.reportList = [];  //页面展示数据
        $scope.isTab = $location.search().isTab || 2;
        //设置默认 分页对象 初始 要重置就调用此方法
        var getPageDataFun = function(){
            return {
                number: 0,
                size: 20,
                totalPages: 100    // --2017-5-22 11:38:39 chenlaichun 要处理 数据为0 的时候进不了没有数据的判断 默认总数为100 查询一次返回数据就修改为数据库的总数据
            };
        }
        $scope.pageData = getPageDataFun();
        //点击 顶部tab 获取数据方法
        var maxLen = 0;
        $scope.setIsTabFun = function (item) {
            if(item){
                if($scope.isTab == item) return false;
                $scope.isTab = item;    //查询类型
                $scope.reportList = [];   //重置集合
                $scope.pageData = getPageDataFun();
                $scope.ionicLoadingShow();
            }
            if(!$scope.pageData.totalPages || $scope.pageData.totalPages <= maxLen){
                $scope.isMoreData = true;
                $scope.ionicLoadingHide();
                $scope.$broadcast('scroll.infiniteScrollComplete');
            }else{
                var suFun = function (data) {
                    if(item) $scope.reportList = [];   //重置集合
                    $scope.reportList = $scope.reportList.concat(data.content);
                    maxLen = $scope.reportList.length;
                    $scope.pageData.number = data.number + 1;
                    $scope.pageData.size = data.size;
                    $scope.pageData.totalPages = data.totalPages;
                    $scope.ionicLoadingHide();
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                };
                httpVisit("findJSTXWorkReportDTOByPage", {type: $scope.isTab, pageNumber: $scope.pageData.number, pageSize: $scope.pageData.size}, {}, suFun);
            }
        };

        //瀑布流加载
        $scope.loadMoreData = function(){
            $scope.setIsTabFun();
        };
        //加载中 提示信息
        $scope.ionicLoadingShow = function () {
            $ionicLoading.show({
                template: '<ion-spinner class="spinner-theme"></ion-spinner><h4 class="text-theme">加载中</h4>'
            });
        };
        // 关闭正在加载提示信息
        $scope.ionicLoadingHide = function () {
            $ionicLoading.hide();
        };
        /**
         *  跳转 方法
         */
        $scope.jumpFun = {
            add: function () {
                $location.path('/add_report').search({});
            },
            view: function (info) {
                $location.path('/report_detail').search({id: info.id, isTab: $scope.isTab});
            }
        };

        /**
         *  获取 数据方法
         * @param url  路径
         * @param params get数据
         * @param data post 数据
         * @param fun 访问成功 方法
         * @param errFun 访问失败方法
         */
        var httpVisit = function (url, params, data, fun, errFun) {
            //$scope.ionicLoadingShow();
            params = params || angular.noop;
            data = data || angular.noop;
            fun = fun || angular.noop;
            errFun = errFun || angular.noop;
            var config = {
                url: basePath + "/third/workreport/" + url,
                method: "post",
                params: params, //后台用 get接收
                data: data //后台用@RequestBody 接收
            };
            $http(config).success(function (resultData) {
                $scope.isError = false;
                //$scope.ionicLoadingHide();
                fun && fun(resultData);
            }).error(function (resultData) {
                $scope.isError = true;
                //$scope.ionicLoadingHide();
                errFun && errFun(resultData);
            });
        };

        //时间 格式改变
        $scope.getReportTime = function (date) {
            if(date){
                var dates = date.split("-");
               return dates[1] + "/" + dates[2];
            }
        };
    }
})();
