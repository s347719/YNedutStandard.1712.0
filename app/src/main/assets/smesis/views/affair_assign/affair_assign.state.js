(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*事务交办主界面*/
            .state('affair_assign',{
                homePage: true,
                url: '/affair_assign',
                templateUrl: 'views/affair_assign/affair_assign.html',
                controller: 'affairAssignlController'
            })
            /*添加或修改事务交办*/
            .state('add_assign',{
                url: '/add_assign',
                templateUrl: 'views/affair_assign/add_assign.html',
                controller: 'addAssignlController'
            })
            /*事务反馈*/
            .state('affair_retroaction',{
                url: '/affair_retroaction',
                templateUrl: 'views/affair_assign/affair_retroaction.html',
                controller: 'affairRetroactionController'
            })
            /*查看事务*/
            .state('view_affair',{
                url: '/view_affair',
                templateUrl: 'views/affair_assign/view_affair.html',
                controller: 'viewAffairController'
            })
    }
})();
