angular.module('myApp')
    .directive('hotelAdd', ['$http', function ($http) {
        var template = '<ion-modal-view class="right half min-modal-height">' +
            '<ion-header-bar class="item-inset bar-stable">' +
            '<h1 class="title">添加酒店</h1>' +
            '<button class="button button-clear button-custom" ng-click="closeModal()">取消</button>' +
            '</ion-header-bar>' +
            '<ion-content>' +
            '<form name="hotelForm">' +
            '<div class="list">' +
            '<label class="item item-input">' +
            '<span class="input-label">酒店名称</span>' +
            '<input type="text" placeholder="请输入" ng-model="hotel.name">' +
            '</label>' +
            '<label class="item item-input">' +
            '<span class="input-label">酒店类型</span>' +
            '<input slide-select init-data="initData" name-field="dictName" ng-model="hotel.hotelTypeName" ng-value="hotelTypeCode.dictCode" selected-obj="hotelTypeCode" on-select="hotelTypeCodeSelect(item)" readonly="readonly" type="text" placeholder="请选择">' +
            '</label>' +
            '<label class="item item-input">' +
            '<span class="input-label">酒店地址</span>' +
            '<input area-selector  province-id="areaObj.provinceId" city-id="areaObj.cityId" area-id="areaObj.areaId" ng-model="area.name" on-select="areaSelect(item)" type="text" placeholder="请选择所在地区" readonly="readonly">'+
            '</label>' +
            '<label class="item item-input">' +
            '<span class="input-label">&nbsp;</span>' +
            '<input type="text" placeholder="请输入详细地址" ng-model="hotel.address">' +
            '</label>' +
            '</div>' +
            '<div class="padding">' +
            '<input type="submit" class="button button-block button-theme button-local" ng-click="submit()" value="保存">' +
            '</div>' +
            '</form>' +
            '</ion-content>' +
            '</ion-modal-view>';
        return {
            scope: {
                modal: '=',
                success : '&onSuccessed'
            },
            controller: ['$scope', '$ionicModal','$ionicLoading','$ionicPopup', function($scope, $ionicModal,$ionicLoading,$ionicPopup){
                $scope.addHotelModal = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-right'
                });
                $scope.openModal = function() {
                    $scope.initData =[];
                    //初始化地址选择
                    $scope.areaObj ={provinceId:"",cityId:"",areaId:""};
                    $scope.area ={name:""};
                    //初始化酒店对象
                    $scope.hotel = {name:"", hotelTypeCode:"",hotelTypeName:"",provinceId:"", cityId:"", areaId:"", address:"", isPhone:"1",isAgreementStr:"0"};
                    $scope.initHotelData();
                    $scope.addHotelModal.show();
                };
                $scope.closeModal = function() {
                    $scope.addHotelModal.hide();
                };
                //Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.addHotelModal.remove();
                });
                // Execute action on hide modal
                $scope.$on('modal.hidden', function() {
                    // Execute action
                });
                // Execute action on remove modal
                $scope.$on('modal.removed', function() {
                    // Execute action
                });
                //初始化地址选择
                $scope.areaObj ={provinceId:"",cityId:"",areaId:""};
                $scope.area ={name:""};
                //初始化酒店对象
                $scope.hotel = {name:"", hotelTypeCode:"", provinceId:"", cityId:"", areaId:"", address:"", isPhone:"1",isAgreementStr:"0"};
                //初始化酒店类型选项
                $scope.initHotelData = function () {
                    $http.get(basePath + "/third/businessjournal/findTravelHotelTypeCodeTable").then(function(res){
                        $scope.initData = res.data;
                    });
                }
                $scope.initHotelData();

                //省市县
                $scope.areaSelect = function (item) {
                    //取得所选对象
                    $scope.hotel.provinceId = item[0].id;//省
                    $scope.areaObj.provinceId =item[0].id;//省
                   var name = item[0].name+","
                    $scope.hotel.cityId = item[1].id;//市
                    $scope.areaObj.cityId = item[1].id;//市
                    name +=item[1].name;
                    if (item.length > 2) {
                        $scope.hotel.areaId = item[2].id;//县
                        $scope.areaObj.areaId = item[2].id;//县
                        name +=","+item[2].name;
                    }else{
                        $scope.hotel.areaId = "";
                    }
                    $scope.area.name =name;
                };
                //酒店类型
                $scope.hotelTypeCodeSelect = function (item) {
                    //取得所选对象
                    if(item){
                        $scope.hotel.hotelTypeName = item.dictName;
                        $scope.hotel.hotelTypeCode = item.dictCode;
                    }

                };

                //非空验证
                $scope.dataBusin = function(data){
                    var alertPopup = $ionicPopup.alert({
                        title : '提示',
                        template : '<h5 class="margin-bottom-0">'+data+'不能为空!</h5>',
                        buttons : [
                            {
                                text : '确定',
                                type : ' button-theme button-local'


                            }
                        ]

                    });
                }
                //保存酒店
                $scope.submit = function(){
                    if(!$scope.hotel.name){
                        $scope.dataBusin("酒店名称");
                        return;
                    }
                    if(!$scope.hotel.hotelTypeCode){
                        $scope.dataBusin("酒店类型");
                        return;
                    }
                    if(!($scope.hotel.provinceId &&$scope.hotel.cityId)){
                        $scope.dataBusin("酒店所在地");
                        return;
                    }
                    if(!$scope.hotel.address){
                        $scope.dataBusin("酒店地址");
                        return;
                    }
                    $http.post(basePath + "/third/businessjournal/saveOrUpdateEntity",$scope.hotel).success(function (data) {
                        if (data.status == "0") {
                            $scope.initHotelData();
                            $scope.hotelTypeCode ={dictName:""};
                            $ionicLoading.show({
                                template: '保存成功'
                            });
                            setTimeout(function () {
                                $ionicLoading.hide();
                            }, 500);
                            $scope.closeModal();
                            $scope.success();
                        } else {
                            var alertPopup = $ionicPopup.alert({
                                title : '提交失败',
                                template : '<h5 class="margin-bottom-0">'+data.message+'</h5>',
                                buttons : [
                                    {
                                        text : '确定',
                                        type : ' button-theme button-local'


                                    }
                                ]
                            });
                        }
                    });

                };
            }],
            link: function (scope, element, attrs) {
                element.on('click',function(){
                    scope.openModal();
                })
            }
        }
    }]);
