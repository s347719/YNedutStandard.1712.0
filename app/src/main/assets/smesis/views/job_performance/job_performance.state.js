(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider

            //工作绩效主页面
            .state('job_performance', {
                homePage: true,
                url: '/job_performance',
                templateUrl: 'views/job_performance/job_performance.html',
                controller:'jobPerformanceCtrl'
            });
    }
})();
