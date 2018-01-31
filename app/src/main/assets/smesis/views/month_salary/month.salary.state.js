(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*月薪酬查看*/
            .state('view_month_salary',{
                homePage: true,
                url: '/view_month_salary',
                templateUrl: 'views/month_salary/view_month_salary.html',
                controller: 'monthSalaryController'
            })
    }
})();
