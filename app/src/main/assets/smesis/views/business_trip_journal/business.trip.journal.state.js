(function () {
    'use strict';

    angular
        .module('myApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            /*出差日记账主页面*/
            .state('business_journal',{
                homePage: true,
                url: '/business_journal',
                templateUrl: 'views/business_trip_journal/business_journal.html',
                controller: 'businessJournalController'
            })
            /*出差日记账查看费用记录*/
            .state('view_fee',{
                url: '/view_fee',
                templateUrl: 'views/business_trip_journal/view_fee.html',
                controller: 'viewFeeController'
            })

            /*添加交通费用*/
            .state('add_trans',{
                url: '/add_trans',
                templateUrl: 'views/business_trip_journal/add_trans_fee.html',
                controller: 'addTransController'
            })
            /*查看交通费用*/
            .state('view_trans',{
                url: '/view_trans',
                templateUrl: 'views/business_trip_journal/view_trans.html',
                controller: 'viewTransController'
            })

            /*添加住宿费用*/
            .state('add_hotel',{
                url: '/add_hotel',
                templateUrl: 'views/business_trip_journal/add_hotel_fee.html',
                controller: 'addHotelController'
            })
            /*查看住宿费用*/
            .state('view_hotel',{
                url: '/view_hotel',
                templateUrl: 'views/business_trip_journal/view_hotel.html',
                controller: 'viewHotelController'
            })

            /*添加其他费*/
            .state('add_other',{
                url: '/add_other',
                templateUrl: 'views/business_trip_journal/add_other_fee.html',
                controller: 'addOtherController'
            })
            /*添加其他费*/
            .state('view_other',{
                url: '/view_other',
                templateUrl: 'views/business_trip_journal/view_other.html',
                controller: 'viewOtherController'
            })
    }
})();
