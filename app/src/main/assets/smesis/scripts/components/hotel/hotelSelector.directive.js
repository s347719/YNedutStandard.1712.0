angular.module('myApp')
    .directive('hotelSelector', ['$http', function ($http) {
        var template = '<ion-modal-view class="right half min-modal-height">' +
            '<ion-header-bar class="item-inset bar-stable">' +
            '<h1 class="title">选择酒店</h1>' +
            '<button hotel-add on-successed="addSuccessed()" area-obj="areaObj" class="button button-hollow button-item-local"><i class="ion-plus"></i> 添加酒店</button>' +
            '<button class="button button-clear button-custom" ng-click="closeModal()">取消</button>' +
            '</ion-header-bar>' +
            '<ion-content>' +
            '<div class="item item-input-inset">' +
            '<label class="item-input-wrapper">' +
            '<i class="icon ion-search placeholder-icon"></i>' +
            '<input ng-model="query" type="text" placeholder="搜索酒店">' +
            '</label>' +
            '</div>' +
            '<div class="list">' +
            '<ion-radio class="item-radio radio-left" ng-class="{active:selectedObj==hotel}" ng-click="selectHotel(hotel)" ng-repeat="hotel in hotelData | filter:{name:query}">' +
            '<i class="radio-icon disable-pointer-events icon ion-checkmark"></i>' +
            '<h2>{{hotel.name}}</h2>' +
            '<p>{{hotel.address}}</p>' +
            '</ion-radio>' +
            '</div>' +
            '</ion-content>' +
            '</ion-modal-view>';
        return {
            scope: {
                ngModel: '=',
                selectedObj: '=',
                areaObj: '=',
                select : '&onSelect'
            },
            template: template,
            controller: ['$scope', '$ionicModal', '$ionicLoading', function($scope, $ionicModal, $ionicLoading){
                $scope.hotelModal = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-right'
                });
                $scope.openModal = function(index) {
                    if(index == true){
                        $ionicLoading.show({template: '请先选择酒店所在地区!'});
                        setTimeout(function () {
                            $ionicLoading.hide();
                        }, 800);
                        return false;
                    }
                    $scope.hotelModal.show();
                };
                $scope.closeModal = function() {
                    $scope.hotelModal.hide();
                };
                //Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.hotelModal.remove();
                });
                // Execute action on hide modal
                $scope.$on('modal.hidden', function() {
                    // Execute action
                });
                // Execute action on remove modal
                $scope.$on('modal.removed', function() {
                    // Execute action
                });

                $scope.selectHotel = function(hotel){
                    $scope.selectedObj = hotel;
                    $scope.select({item:hotel});
                    $scope.closeModal();
                }
            }],
            link: function (scope, element,attrs) {
                scope.hotelData = [];
                element.on('click',function(){
                    if(!(scope.areaObj.provinceId && scope.areaObj.cityId)){//强制级联 若省市县为空，不允许加载酒店
                        scope.openModal(true);
                        return;
                    }else{
                        scope.openModal();
                    }
                    scope.getHotelData();//点击时加载数据！ Qiucheng Lu add
                });
                scope.getHotelData = function(){
                    if(scope.data){
                        scope.hotelData = scope.data;
                    }else{
                        //scope.areaObj.name = "";//将显示名称清空（不能作为过滤条件）
                        scope.searchObj ={};
                        scope.searchObj.provinceId = scope.areaObj.provinceId
                        scope.searchObj.cityId =scope.areaObj.cityId;
                        scope.searchObj.areaId =scope.areaObj.areaId

                        $http.post(basePath + "/third/businessjournal/findXZGLHotelTraByConditions?pageNumber=0&pageSize=1000",scope.searchObj).success(function (data) {
                           if(data.result){
                               scope.hotelData = data.result.content;
                           }
                        });

                    }
                };

                scope.addSuccessed = function(){
                    scope.getHotelData();
                };
                //scope.getHotelData();
            }
        }
    }]);
