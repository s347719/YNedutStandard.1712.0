(function () {
    'use strict';

    angular
        .module('myApp')
        .factory('ionicHandler', ionicHandler);

    ionicHandler.$inject = ['$ionicPlatform','$rootScope'];

    function ionicHandler($ionicPlatform,$rootScope){
        return {
            initialize: initialize
        };

        function initialize(){
            $ionicPlatform.ready(function () {
                ionic.Platform.fullScreen(true,true);
                //本应为false，因messenger对windowSoftInputMode有配置，此处特定为true
                ionic.Platform.isFullScreen = true;
                if (window.cordova && window.cordova.plugins && window.cordova.plugins.Keyboard) {
                    cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
                    cordova.plugins.Keyboard.disableScroll(true);
                }
                if (window.StatusBar) {
                    // org.apache.cordova.statusbar required
                    StatusBar.styleLightContent();
                }
            });

            window.addEventListener('native.keyboardshow', keyboardShowHandler);
            window.addEventListener('native.keyboardhide', keyboardHideHandler);
            function keyboardShowHandler(e){
                document.body.classList.add('keyboard-open');
                if(angular.element('.popup-container').length && (angular.element('.popup-container').height() == angular.element(document).height())){
                    var popupContainerHeight = angular.element('.popup-container').outerHeight() - e.keyboardHeight;
                    angular.element('.popup-container').css({height:popupContainerHeight})
                }
            }
            function keyboardHideHandler(){
                document.body.classList.remove('keyboard-open');
                if(angular.element('.popup-container').length){
                    angular.element('.popup-container').css({height:'auto'})
                }
            }

            // Android端 物理返回键 事件
            $ionicPlatform.registerBackButtonAction(backButtonAction, 100, 'views');
            function backButtonAction() {
                if($rootScope.homePage){
                    yn.plugin.yncordova.close();
                }else{
                    window.history.back();
                }
            }
            $rootScope.backButtonActions = {};
            $rootScope.registerBackButtonAction =  function(action, actionId) {
                if(!actionId){
                    console.error('actionId can be required!');
                    return;
                }
                $rootScope.backButtonActions[actionId] = $ionicPlatform.registerBackButtonAction(action, 201, actionId);
            };
            $rootScope.deregisterBackButtonAction =  function(actionId) {
                $rootScope.backButtonActions[actionId]();
            }

        }
    }

})();
