(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*出差人员轨迹正常*/
            .state('staff_locus',{
                homePage: true,
                url: '/staff_locus',
                templateUrl: 'views/staff_locus/staff_locus.html',
                controller: 'staffLocusController'
            })
            /*出差人员轨迹详情*/
            .state('staff_locus_detail',{
                url: '/staff_locus_detail',
                templateUrl: 'views/staff_locus/staff_locus_detail.html',
                controller: 'staffLocusDetailController'
            })
    }
})();
