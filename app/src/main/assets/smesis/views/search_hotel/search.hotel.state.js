(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider

            //出差酒店查询主页面
            .state('search_hotel_main', {
                homePage: true,
                url: '/search_hotel_main',
                templateUrl: 'views/search_hotel/search_hotel_main.html',
                controller: 'searchHotelMainController'
            })
            //查询酒店
            .state('search_hotel', {
                url: '/search_hotel',
                templateUrl: 'views/search_hotel/search_hotel.html',
                controller: 'searchHotelController'
            });


    }
})();
