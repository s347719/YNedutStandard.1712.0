(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*PDCA主界面*/
            .state('pdca',{
                homePage: true,
                url: '/pdca',
                templateUrl: 'views/pdca/pdca.html',
                controller: 'pdcaController'
            })

    }
})();
