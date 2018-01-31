(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*通知公告*/
            .state('inform_index',{
                firstPage: true,
                url: '/inform_index',
                templateUrl: 'views/inform_index/inform_index.html',
                controller: 'inform_indexController'
            })
    }
})();
