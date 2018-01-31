(function () {
    'use strict';

    angular
        .module('myApp')
        .factory('authInterceptor', authInterceptor);

    authInterceptor.$inject = ['$injector', '$q'];

    function authInterceptor($injector, $q){
        var service = {
            request: request,
            responseError: responseError
        };
        return service;

        function request(config) {
            if(window.localStorage.getItem('authentication') && config.url.indexOf('.html') == -1){
                var authentication = JSON.parse(window.localStorage.getItem('authentication'));
                var separate = /\?/.test(config.url) ? '&' : '?';
                config.headers.authorization = authentication.access_token;
                config.url += (separate + 'platformSysUserId=' + authentication.platformSysUserId + '&access_token=' + authentication.access_token);
            }
            return config;
        }

        function responseError(response){
            if(response.status == 401){
                $injector.get('$state').go('401');
                return $q.reject(response);
            }else{
                return $q.reject(response);
            }
        }
    }
})();
