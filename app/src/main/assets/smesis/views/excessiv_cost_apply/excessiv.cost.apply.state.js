(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*移动端添加超标费用申请*/
            .state('excessiv_cost_apply',{
                firstPage: true,
                url: '/excessiv_cost_apply',
                templateUrl: 'views/excessiv_cost_apply/excessiv_cost_apply.html',
                controller: 'excessivCostApplyCtrl'
            })

    }
})();
