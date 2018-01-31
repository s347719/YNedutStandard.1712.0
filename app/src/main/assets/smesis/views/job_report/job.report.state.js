(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*工作报告主界面*/
            .state('job_report',{
                homePage: true,
                url: '/job_report',
                templateUrl: 'views/job_report/job_report.html',
                controller: 'jobReportController'
            })
            /*添加或修改工作报告*/
            .state('add_report',{
                url: '/add_report',
                templateUrl: 'views/job_report/add_report.html',
                controller: 'jobReportAddController'
            })
            /*查看报告/报告详情*/
            .state('report_detail',{
                url: '/report_detail',
                templateUrl: 'views/job_report/report_detail.html',
                controller: 'jobReportDetailController'
            })
    }
})();
