(function () {
    'use strict';

    angular
        .module('myApp')
        .config(httpConfig);

    httpConfig.$inject = ['$httpProvider'];

    function httpConfig($httpProvider){

        $httpProvider.interceptors.push('authInterceptor');
    }
})();
