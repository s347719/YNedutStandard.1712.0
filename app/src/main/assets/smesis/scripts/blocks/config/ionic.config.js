(function () {
    'use strict';

    angular
        .module('myApp')
        .config(ionicConfig);

    ionicConfig.$inject = ['$ionicConfigProvider'];

    function ionicConfig($ionicConfigProvider){
        $ionicConfigProvider.platform.android.tabs.position('bottom');
        $ionicConfigProvider.tabs.style('standard');
        $ionicConfigProvider.navBar.alignTitle('center');
        $ionicConfigProvider.backButton.icon('ion-chevron-left');
        $ionicConfigProvider.backButton.text('');
        $ionicConfigProvider.templates.maxPrefetch(0);
        $ionicConfigProvider.views.maxCache(0);
    }
})();
