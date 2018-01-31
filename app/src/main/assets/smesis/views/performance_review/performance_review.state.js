(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider

            //绩效主页面
            .state('performance_review', {
                homePage: true,
                url: '/performance_review',
                templateUrl: 'views/performance_review/performance-review.html',
                controller:'performanceReviewCtrl'
            });
    }
})();
