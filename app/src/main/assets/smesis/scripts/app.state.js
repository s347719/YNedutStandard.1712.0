(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider', '$urlRouterProvider'];

    function stateConfig($stateProvider, $urlRouterProvider){
        $urlRouterProvider.otherwise("/business_journal");

        $stateProvider
            .state('401', {
                url: '/401',
                template: '<ion-view>' +
                    //'<ion-header-bar align-title="center" class="bar-corpsys"><h1 class="title">身份认证失败</h1></ion-header-bar>' +
                '<ion-content class="padding" scroll="false">' +
                '<div class="card">' +
                '<div class="item item-divider">身份认证失败</div>' +
                '<div class="item item-text-wrap">请尝试重新登录后再试</div>' +
                '</div>' +
                '</ion-content>' +
                '</ion-view>'
            });

        $stateProvider
            .state('404', {
                url: '/404',
                template: '<ion-view>' +
                    //'<ion-header-bar align-title="center" class="bar-corpsys"><h1 class="title">身份认证失败</h1></ion-header-bar>' +
                '<ion-content class="padding" scroll="false">' +
                '<div class="card">' +
                '<div class="item item-divider">应用未找到</div>' +
                '<div class="item item-text-wrap">请尝升级客户端后再试</div>' +
                '</div>' +
                '</ion-content>' +
                '</ion-view>'
            });
    }
})();
