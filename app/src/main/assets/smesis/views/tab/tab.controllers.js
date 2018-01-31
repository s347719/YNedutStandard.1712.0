angular.module('myApp')

.controller('tabDemoCtrl',['$scope','$ionicLoading',function($scope, $ionicLoading) {
        $scope.doRefresh = function () {
            $ionicLoading.show({
                template: '加载中...'
            });
        };
        $scope.doBack = function () {
            history.back(-1);
        };
    }]);
