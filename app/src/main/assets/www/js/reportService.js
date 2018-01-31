/**
 * Created by YN on 2016/4/26.
 */
angular.module('starter')
    .factory('reportUtilService', ['ionicPopupService', '$timeout', '$ionicLoading', '$ionicScrollDelegate','dataService',
        function (ionicPopupService, $timeout, $ionicLoading, $ionicScrollDelegate,dataService) {
            var isArray = function (arr) {
                if (Object.prototype.toString.apply(arr) !== "[object Array]") {
                    return false;
                }
                return true;
            }

            return {
                init: function (arr) {
                    if (isArray(arr) && arr.length > 0) {
                        var currentTask = dataService.getcurrentData('currentTask');
                        var currentClass = dataService.getcurrentData('currentClass');
                        for (var i = 0, nLen = arr.length; i < nLen; i++) {
                            if (!isArray(arr[i].adminClassList)) {
                                continue;
                            }
                            if (arr[i].adminClassList.length === 0) {
                                continue;
                            }

                            for (var j = 0, _nLen = arr[i].adminClassList.length; j < _nLen; j++) {
                                arr[i].adminClassList[j].isSelect = false;
                                if (currentTask && arr[i].id === currentTask.id &&
                                    currentClass && arr[i].adminClassList[j].id === currentClass.id) {
                                    arr[i].adminClassList[j].isSelect = true;
                                }
                            }
                        }
                    }
                },
                initAttr: function (arr, attr, b) {
                    if (isArray(arr) && arr.length > 0) {
                        for (var i = 0, nLen = arr.length; i < nLen; i++) {
                            arr[i][attr] = b;
                        }
                    }
                },
                filterArrByAttr: function (arr, attr, b) {
                    var _arr = [];
                    if (isArray(arr) && arr.length > 0) {
                        for (var i = 0, nLen = arr.length; i < nLen; i++) {
                            if (arr[i][attr] === b) {
                                _arr.push(arr[i]);
                            }
                        }
                    }
                    return _arr;
                },
                getValuesByAttr: function (arr, attr) {
                    var _arr = [];
                    if (isArray(arr) && arr.length > 0) {
                        for (var i = 0, nLen = arr.length; i < nLen; i++) {
                            _arr.push(arr[i][attr]);
                        }
                    }
                    return _arr;
                },
                rotateEnrollView: function (arr) {
                    if (Object.prototype.toString.call(arr) == '[object Array]' && arr.length > 0) {
                        var a = arr.shift();
                        arr = arr.push(a);
                    }
                },
                showMsg: function (msg) {
                    $ionicLoading.show({
                        template: msg
                    });
                    $timeout(function () {
                        //$scope.$broadcast('scroll.infiniteScrollComplete');
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                    }, 500);
                },
                showPopup: function ($scope, callBack) {
                    var config = {
                        template: '<textarea ng-model="report.remark" rows="3" placeholder="输入备注内容（选填）"></textarea>',
                        title: '未报到（备注）',
                        scope: $scope,
                        buttons: [{
                            text: '取消'
                        }, {
                            text: '<b>确定</b>',
                            type: 'button-positive',
                            onTap: function (e) {
                                callBack($scope.report.remark);
                            }
                        }, ]
                    };
                    ionicPopupService.showPopup(config);
                }
            }
        }
    ])
    .factory('dataService', ['$http',
        function ($http) {
            var currentData = {};
            return {
                setcurrentData: function (name, data) {
                    currentData[name] = data;
                },
                getcurrentData: function (name) {
                    return currentData[name];
                },
                getEnrolls: function (obj) {
                    obj.enrolls = [{
                        id: '',
                        name: "春/秋季招生"
                    }, {
                        id: '01',
                        name: "春季招生"
                    }, {
                        id: '02',
                        name: "秋季招生"
                    }, ];
                    return obj.enrolls;
                }
            }
        }
    ])
    .factory('ionicPopupService', ['$ionicPopup', '$timeout',
        function ($ionicPopup, $timeout) {
            return {
                showPopup: function (config) {
                    var myPopup = $ionicPopup.show(config);
                    myPopup.then(function (res) {
                        console.log('Rapp', res);
                    });
                },
                confirmPopup: function (config) {
                    var confirmPopup = $ionicPopup.confirm({
                        title: config.title,
                        template: config.template
                    });
                    confirmPopup.then(function (res) {

                    })
                },
                showAlert: function (config) {
                    var alertPopup = $ionicPopup.alert({
                        title: config.title,
                        template: config.template
                    });
                    alertPopup.then(function (res) {

                    });
                }
            }
        }
    ])