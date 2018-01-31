angular.module('myApp')
    .controller('demoCtrl', ['$scope', '$ionicModal', '$http', function ($scope, $ionicModal, $http) {
        /**
         * 酒店选择组件示例
         */
        $scope.areaObj = {provinceId:510000,cityId:510100,areaId:510124};//四川省成都市郫縣
        $scope.hotelSelect = function (item) {
            //取得所选对象
            console.log($scope.date);
            console.log(item);
        };

        /**
         * 抽屉式单选(以交通工具为例)
         */
        //初始化选项
        $http.get( basePath+'/platformcommondictionaryresource/findVehicleCodeTable').then(function(res){
            $scope.initData = res.data;
        },function(res){
            $scope.initData = [];
            console.log('抽屉式单选 数据获取失败')
        });
        $scope.diySelect = function (item) {
            //取得所选对象
            console.log(item);
        };

        /**
         * 地区选择组件示例
         */
        //初始已选省市区
        $scope.area ={provinceId : 130000,cityId : 130400,areaId : 130404};
        $scope.areaSelect = function (item) {
            //取得所选对象
            console.log(item);
        };

        /**
         * 日期日间选择组件
         */
        $scope.ymdhiSelect = function(item){
            //取得所选对象
            console.log(item);
        };
        $scope.ymdSelect = function(item){
            //取得所选对象
            console.log(item);
        };
        $scope.hiSelect = function(item){
            //取得所选对象
            console.log(item);
        };
        $scope.callBackDate = '';
        $scope.callYmdSelect = function(item){
            //取得所选对象
            console.log(item);
            $scope.callBackDate = item;
        };
        $scope.selectDate = function(){
            angular.element('#date').click();
        };

    }]);
