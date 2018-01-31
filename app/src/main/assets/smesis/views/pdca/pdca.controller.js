(function () {
    'use strict';
    angular.module('myApp').controller('pdcaController', pdcaController);
    pdcaController.$inject = ['$scope','$ionicModal','$ionicPopup','$location','$http','$timeout'];
    function pdcaController($scope,$ionicModal,$ionicPopup,$location,$http,$timeout) {
        //添加任务
        $ionicModal.fromTemplateUrl('add-task.html',{
            scope: $scope,
            animation:'slide-in-right'
        }).then(function(modal){
            $scope.modalOne = modal;
        });
        $scope.openModalOne = function(){
            $scope.modalOne.show();
        };
        $scope.closeModalOne = function(){
            $scope.modalOne.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalOne.remove();
        });

        //添加任务模态框
        $ionicModal.fromTemplateUrl('add-task-modal.html',{
            scope: $scope,
            animation:'slide-in-right'
        }).then(function(modal){
            $scope.modalTwo = modal;
        });
        $scope.openModalTwo = function(){
            $scope.modalTwo.show();
        };
        $scope.closeModalTwo = function(){
            $scope.modalTwo.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalTwo.remove();
        });

        //任务总结
        $ionicModal.fromTemplateUrl('task-conclusion.html',{
            scope: $scope,
            animation:'slide-in-right'
        }).then(function(modal){
            $scope.modalThree = modal;
        });
        $scope.openModalThree = function(){
            $scope.modalThree.show();
        };
        $scope.closeModalThree = function(){
            $scope.modalThree.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalThree.remove();
        });

    }
})();
