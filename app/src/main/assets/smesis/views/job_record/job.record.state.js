(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*工作日志主界面*/
            .state('job_record',{
                homePage: true,
                url: '/job_record',
                templateUrl: 'views/job_record/job_record.html',
                controller: 'jobRecordController'
            })
    }
})();
