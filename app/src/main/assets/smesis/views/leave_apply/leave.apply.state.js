(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*移动端添加请假申请*/
            .state('leave_apply',{
                homePage: true,
                url: '/leave_apply',
                templateUrl: 'views/leave_apply/leave_apply.html',
                controller: 'leaveApplyController'
            })
            /*移动端添加请假申请*/
            .state('add_apply',{
                url: '/add_apply',
                templateUrl: 'views/leave_apply/add_leave.html',
                controller: 'addApplyController'
            })
            //请假申请查看
            .state('leave_apply_view',{
                url: '/leave_apply_view',
                templateUrl: 'views/leave_apply/leave_apply_view.html',
                controller: 'leaveApplyViewController'
            })
    }
})();
