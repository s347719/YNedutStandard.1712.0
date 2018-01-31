(function(){
    'use strict';

    angular
        .module('myApp', [
            'ionic',
            'staticModule',
            'ngCordova'
        ])
        .run(run)
        .directive('onFinishRender',onFinishRender);

    run.$inject = ['stateHandler', 'ionicHandler', 'authHandler'];
    onFinishRender.$inject = ['$timeout'];

    function run(stateHandler, ionicHandler, authHandler){
        stateHandler.initialize();
        ionicHandler.initialize();
        authHandler.initialize();
    }
    function onFinishRender($timeout){
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                if (scope.$last === true) {
                    $timeout(function () {
                        if (!attr.onFinishRender) {
                            scope.$emit('ngRepeatFinished');
                        } else {
                            scope.$emit(attr.onFinishRender);
                        }
                    });
                }
            }
        }
    }
})();
$(document).ready(function(){
    $('body').on('click', '.dropdown-menu .hold-on-click', function (e) {
        e.stopPropagation();
    });
});
