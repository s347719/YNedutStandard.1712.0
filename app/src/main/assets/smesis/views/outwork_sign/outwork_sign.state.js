(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider

            //外勤签到主页面
            .state('outwork_sign', {
                homePage: true,
                url: '/outwork_sign',
                templateUrl: 'views/outwork_sign/outwork_sign.html',
                controller:'outworkSignCtrl'
            });
    }
})();
