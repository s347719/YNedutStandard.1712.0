(function () {
    'use strict';

    angular
        .module('myApp')
        .factory('stateHandler', stateHandler);

    stateHandler.$inject = ['$rootScope'];

    function stateHandler($rootScope){
        return {
            initialize: initialize
        };

        function initialize(){
            $rootScope.$on('$stateChangeSuccess',  function(event, toState, toParams, fromState, fromParams) {
                $rootScope.homePage = false;
                if(toState.homePage){
                    $rootScope.homePage = true;
                }
                // 移除所有物理返回键监听事件
                if($rootScope.backButtonActions && $rootScope.backButtonActions.length){
                    angular.forEach($rootScope.backButtonActions, function(v,k){
                        $rootScope.backButtonActions[k]();
                    })
                }
            });
        }
    }

})();
