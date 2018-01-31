
(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*费用申请*/
            .state('cost_apply',{
                homePage: true,
                url: '/cost_apply',
                templateUrl: 'views/cost_apply/cost_apply.html',
                controller: 'costApplyCtrl'
            })
            /*详情查看*/
            .state('cost_apply_view',{
                url: '/cost_apply_view',
                templateUrl: 'views/cost_apply/cost_apply_view.html',
                controller: 'costApplyViewController'
            })
    }
})();
