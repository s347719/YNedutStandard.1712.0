(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            //外勤申请主页面
            .state('outwork_apply', {
                homePage: true,
                url: '/outwork_apply',
                templateUrl: 'views/outwork_apply/outwork_apply.html',
                controller: 'outworkApplyController'
            })
            //外勤添加申请
            .state('outwork_apply_add', {
                url: '/outwork_apply_add',
                templateUrl: 'views/outwork_apply/outwork_apply_add.html',
                controller: 'outworkApplyAddController'
            })
            //外勤申请查看
            .state('outwork_apply_view', {
                url: '/outwork_apply_view',
                templateUrl: 'views/outwork_apply/outwork_apply_view.html',
                controller: 'outworkApplyViewController'
            })
    }
})();
