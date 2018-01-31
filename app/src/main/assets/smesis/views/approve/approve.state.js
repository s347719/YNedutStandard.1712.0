(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*审批主界面*/
            .state('flow_approve',{
	    		homePage: true,
                url: '/flow_approve',
                templateUrl: 'views/approve/approve.html',
                controller: 'approveController'
            })
    }
})();
