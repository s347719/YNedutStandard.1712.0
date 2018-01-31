angular.module('myApp')
    .directive('areaSelector', ['$http', '$ionicModal', '$q', function ($http, $ionicModal, $q) {
        var template = '<ion-modal-view class="bottom-half area-selector min-modal-height">' +
            '<ion-header-bar class="bar-stable">' +
            //'<button class="button button-clear button-custom" ng-click=""><i class="icon ion-search placeholder-icon"></i>搜索</button>' +
            '<h1 class="title">选择地区</h1>' +
            '<button class="button button-clear button-custom" ng-click="modal.hide()">取消</button>' +
            '</ion-header-bar>' +
            '<div class="area-breadcrumb">' +
            '<i class="ion-location"></i>' +
            '<a ng-show="showProvince" ng-click="view(0)" ng-class="{active:showProvinceList}">{{selectedObj[0].name || "请选择省"}} <i class="arrow">></i></a>' +
            '<a ng-show="showCity" ng-click="view(1)" ng-class="{active:showCityList}">{{selectedObj[1].name || "请选择市"}} <i class="arrow">></i></a>' +
            '<a ng-show="showArea" ng-click="view(2)" ng-class="{active:showAreaList}">{{selectedObj[2].name || "请选择区"}}</a>' +
            '</div>' +
            '<ion-content delegate-handle="mainScroll">' +
            '<div class="select-history" ng-if="history.length">' +
            '<button class="button button-theme button-local button-outline" ng-repeat="item in history" ng-click="historyChoose(item.platformCommonAreaDTOList)">{{item.platCommonAreaName?item.platCommonAreaName:item.platCommonCityName}}</button>' +
            '</div>' +
            '<div class="text-center" ng-if="loading">' +
            '<ion-spinner icon="ios-small"></ion-spinner>' +
            '</div>' +
            '<div class="list area-list" ng-show="showProvinceList">' +
            '<div class="item" ng-repeat="item in viewObj[0] track by $index" ng-click="choose(0, item)" ng-class="{active:selectedObj[0].name==item.name}">' +
            '<i class="ion-checkmark-round"></i>{{item.name}}' +
            '</div>' +
            '</div>' +
            '<div class="list area-list" ng-show="showCityList">' +
            '<div class="item" ng-repeat="item in viewObj[1] track by $index" ng-click="choose(1, item)" ng-class="{active:selectedObj[1].name==item.name}">' +
            '<i class="ion-checkmark-round"></i>{{item.name}}' +
            '</div>' +
            '</div>' +
            '<div class="list area-list" ng-show="showAreaList">' +
            '<div class="item" ng-repeat="item in viewObj[2] track by $index" ng-click="choose(2, item)" ng-class="{active:selectedObj[2].name==item.name}">' +
            '<i class="ion-checkmark-round"></i>{{item.name}}' +
            '</div>' +
            '</div>' +
            '</ion-content>' +
            '</ion-modal-view>';
        return {
            scope: {
                ngModel: '=',
                diyNgModel : '=',
                provinceId: '=',
                cityId: '=',
                areaId: '=',
                select: '&onSelect'
            },
            template: template,
            controller: ['$scope', '$ionicScrollDelegate',"$ionicLoading", function ($scope, $ionicScrollDelegate,$ionicLoading) {
                $scope.viewObj = [];
                $scope.selectedObj = [];
                $scope.history = [];
                $scope.isEnd = false;
                $scope.loading = true;

                $scope.clearData = function(){
                    $scope.ngModel = '';
                    $scope.selectedObj = [];
                    $scope.provinceId = null;
                    $scope.cityId = null;
                    $scope.areaId = null;
                };
                $scope.getDataById = function(id){
                    var defer = $q.defer();
                    if(!id){
                        $http.get(basePath + "/third/commresource/findAreaMessage").then(function(res){
                            defer.resolve(res);
                        },function(res){
                            defer.reject(res);
                        });
                    }else{
                        $http.get(basePath + "/third/commresource/findProvince?id=" + id).then(function(res){
                            defer.resolve(res);
                        },function(res){
                            defer.reject(res);
                        });
                    }
                    return defer.promise;
                };
                $scope.getData = function(type, id){
                    var defer = $q.defer();
                    $scope.isEnd = false;
                    $scope.loading = true;
                    $scope.getDataById(id).then(function(res){
                        if(res.data.result.length){
                            $scope.viewObj[type] = res.data.result;
                            $scope.view(type);
                        }else{
                            $scope.isEnd = true;
                        }
                        defer.resolve(res);
                        $scope.loading = false;
                    });
                    return defer.promise;
                };
                $scope.choose = function(type, area){
                    if(type == 0){
                        $scope.clearData();
                    }
                    $scope.selectedObj[type] = area;
                    $scope.getData(type+1, area.id).then(function(){
                        if($scope.isEnd){
                            $scope.ngModel = '';
                            if($scope.selectedObj != null &&$scope.selectedObj.length ==1){
                                $scope.notifyMsg("该省下面没有任何区县,请重新选择!")
                                return false;
                            }else{
                                var split = function(k){
                                    return $scope.selectedObj.length-1 == k ? '' : ',';
                                };
                                angular.forEach($scope.selectedObj,function(v, k){
                                    if(!$scope.diyNgModel) {
                                        $scope.ngModel += v.name + split(k);
                                    }
                                });
                                $scope.provinceId = $scope.selectedObj[0].id;
                                $scope.cityId = $scope.selectedObj[1].id;
                                $scope.areaId = $scope.selectedObj[2] ? $scope.selectedObj[2].id : null;
                                $scope.select({item:$scope.selectedObj});
                                $scope.closeModal();
                            }


                        }
                    });
                    $ionicScrollDelegate.scrollTop(true);
                };
                $scope.historyChoose = function(area){
                    $scope.clearData();
                    $scope.selectedObj = area;
                    $scope.provinceId = $scope.selectedObj[0].id;
                    $scope.cityId = $scope.selectedObj[1].id;
                    $scope.areaId = $scope.selectedObj[2] ? $scope.selectedObj[2].id : null;
                    $scope.init();
                    $scope.select({item:$scope.selectedObj});
                    $scope.closeModal();

                };
                $scope.view = function(type){
                    $scope.showProvince = false;
                    $scope.showCity = false;
                    $scope.showArea = false;
                    $scope.showProvinceList = false;
                    $scope.showCityList = false;
                    $scope.showAreaList = false;
                    switch (type){
                        case 0:
                            $scope.showProvince = true;
                            $scope.showProvinceList = true;
                            break;
                        case 1:
                            $scope.showProvince = true;
                            $scope.showCity = true;
                            $scope.showCityList = true;
                            break;
                        case 2:
                            $scope.showProvince = true;
                            $scope.showCity = true;
                            $scope.showArea = true;
                            $scope.showAreaList = true;
                            break;
                    }
                };
                $scope.init = function(){
                    $scope.getData(0, null).then(function(){
                        $scope.ngModel = '';
                        angular.forEach($scope.viewObj[0],function(v){
                            if(v.id == $scope.provinceId){
                                if(!$scope.diyNgModel) {
                                    $scope.ngModel += v.name;
                                }
                                $scope.selectedObj[0] = v;
                            }
                        });
                        if($scope.cityId){
                            $scope.getData(1, $scope.provinceId).then(function(){
                                angular.forEach($scope.viewObj[1],function(v){
                                    if(v.id == $scope.cityId){
                                        if(!$scope.diyNgModel) {
                                            $scope.ngModel += ',' + v.name;
                                        }
                                        $scope.selectedObj[1] = v;
                                    }
                                });
                                if($scope.areaId){
                                    $scope.getData(2, $scope.cityId).then(function(){
                                        angular.forEach($scope.viewObj[2],function(v){
                                            if(v.id == $scope.areaId){
                                                if(!$scope.diyNgModel){
                                                    $scope.ngModel += ',' + v.name;
                                                }
                                                $scope.selectedObj[2] = v;
                                            }
                                        });
                                    });
                                }
                            });
                        }
                    });

                    $http.get(basePath + "/third/commresource/findAddrSelHistoryByMaxTimeInIndex?index=4&ownedBusiness=mobile").then(function(res){
                        $scope.history = res.data.result;
                    },function(res){
                        $scope.history = [];
                    });
                };
                //冒泡提示
                $scope.notifyMsg = function (msg) {
                    $ionicLoading.show({
                        template: msg
                    });
                    setTimeout(function () {
                        $ionicLoading.hide();
                    }, 800);
                }

                $scope.modal = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });
                $scope.openModal = function() {
                    $scope.modal.show();
                };
                $scope.closeModal = function() {
                    $scope.modal.hide();
                };
                //Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.modal.remove();
                });
                // Execute action on hide modal
                $scope.$on('modal.hidden', function() {
                    // Execute action
                });
                // Execute action on remove modal
                $scope.$on('modal.removed', function() {
                    // Execute action
                });
            }],
            link: function (scope, elem, attrs) {
                scope.init();
                elem.on('click',function(){
                    scope.openModal();
                });
            }
        }
    }]);
