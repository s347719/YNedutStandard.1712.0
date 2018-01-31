(function () {
    'use strict';

    angular
        .module('myApp')
        .factory('authHandler', authHandler);

    authHandler.$inject = ['$rootScope', '$location', '$state'];

    function authHandler($rootScope, $location, $state){
        return {
            initialize: initialize
        };

        function initialize(){
            var searchParams = $location.search();
            if(searchParams.originUrl && searchParams.platformSysUserId && searchParams.access_token){
                basePath = searchParams.originUrl;
                var authentication = {
                    originUrl: searchParams.originUrl,
                    platformSysUserId: searchParams.platformSysUserId,
                    access_token: searchParams.access_token
                };
                $rootScope.authentication = authentication;
                window.localStorage.setItem('authentication',JSON.stringify(authentication));
            }else{
                var authentication = JSON.parse(window.localStorage.getItem('authentication')) || '';
                if(authentication.originUrl && authentication.platformSysUserId && authentication.access_token){
                    basePath = authentication.originUrl;
                }else{
                    $state.go('401')
                }
            }
        }
    }

})();
