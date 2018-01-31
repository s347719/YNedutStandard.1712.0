(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*月薪酬查看*/
            .state('salary',{
                homePage: true,
                url: '/salary',
                templateUrl: 'views/salary/salary.html'
            })
    }
})();
