(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*申请主界面*/
            .state('flow_apply',{
	    		homePage: true,
                url: '/flow_apply',
                templateUrl: 'views/apply/apply.html',
                controller: 'applyController'
            })
    }
})();
