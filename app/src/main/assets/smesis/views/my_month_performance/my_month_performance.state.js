(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider

        //绩效主页面
            .state('my_month_performance', {
                homePage: true,
                url: '/my_month_performance',
                templateUrl: 'views/my_month_performance/my_month_performance.html',
                controller:'myMonthPerformanceCtrl'
            });
    }
})();
