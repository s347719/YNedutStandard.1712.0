angular.module('myApp')
    .directive('slideSelect', ['$http', function ($http) {
        var template = '<ion-modal-view class="auto min-modal-height">' +
            '<ion-header-bar class="bar-stable">' +
            '<h1 class="title">{{slideTitle}}</h1>' +
            '<button class="button button-clear button-custom" ng-click="closeModal()">取消</button>' +
            '</ion-header-bar>' +
            '<div class="padding has-header button-list">' +
            '<div class="slide-select-buttons">' +
            '<div class="slide-select-button" ng-repeat="item in initData">' +
            '<button class="button button-local button-block button-theme button-outline" ng-class="{active:selectedObj==item}" ng-click="onSelect(item)">{{item[nameField]}}</button>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</ion-modal-view>';
        return {
            scope: {
                modal: '=',
                initData : '=',
                nameField : '@',
                slideTitle : '@',
                selectedObj: '=',
                select : '&onSelect'
            },
            template: template,
            controller: ['$scope', '$ionicModal', function($scope,$ionicModal){

                $scope.slideSelect = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });
                $scope.openModal = function() {
                    $scope.slideSelect.show();
                };
                $scope.closeModal = function() {
                    $scope.slideSelect.hide();
                };
                //Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.slideSelect.remove();
                });
                // Execute action on hide modal
                $scope.$on('modal.hidden', function() {
                    // Execute action
                });
                // Execute action on remove modal
                $scope.$on('modal.removed', function() {
                    // Execute action
                });

                $scope.onSelect = function(item){
                    $scope.selectedObj = item;
                    $scope.select({item:item});
                    $scope.closeModal();
                }
            }],
            link: function (scope, element, attrs) {
                element.on('click',function(){
                    scope.slideTitle = scope.slideTitle || '请选择';
                    scope.openModal();
                });
            }
        }
    }]);
