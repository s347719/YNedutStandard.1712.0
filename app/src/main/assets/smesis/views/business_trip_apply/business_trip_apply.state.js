(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider

            //出差申请主页面
            .state('business_trip_apply', {
                homePage: true,
                url: '/business_trip_apply',
                templateUrl: 'views/business_trip_apply/business_trip_apply.html',
                controller: 'businessTripApplyCtrl'
            })
            //出差申请发起申请页面
            .state('business_trip_add', {
                firstPage: true,
                url: '/business_trip_add',
                templateUrl: 'views/business_trip_apply/business_trip_Add.html',
                controller: 'businessTripAddController'
            })
            //出差申请查看页面
            .state('business_trip_view', {
                firstPage: true,
                url: '/business_trip_view',
                templateUrl: 'views/business_trip_apply/business_trip_view.html',
                controller: 'businessTripViewController'
            });
    }
})();
