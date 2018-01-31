angular
    .module('loginApp', ['ionic'])
    .config(function ($httpProvider) {
        $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded';
        $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
        var param = function (obj) {
            var query = "", name, value, fullSubName, subName, subValue, innerObj, i;
            for (name in obj) {
                value = obj[name];
                if (value instanceof Array) {
                    for (i = 0; i < value.length; ++i) {
                        subValue = value[i];
                        fullSubName = name;
                        innerObj = {};
                        innerObj[fullSubName] = subValue;
                        query += param(innerObj) + '&';
                    }
                } else if (value instanceof Object) {
                    for (subName in value) {
                        subValue = value[subName];
                        fullSubName = name + "[" + subName + "]";
                        innerObj = {};
                        innerObj[fullSubName] = subValue;
                        query += param(innerObj) + "&";
                    }
                } else if (value !== undefined && value !== null) {
                    query += encodeURIComponent(name) + "=" + encodeURIComponent(value) + "&";
                }
            }
            return query.length ? query.substr(0, query.length - 1) : query;
        };
        $httpProvider.defaults.transformRequest = [function (data) {
            return angular.isObject(data) && String(data) !== "[object File]" ? param(data) : data;
        }];
    })
    .controller('loginCtrl', ['$rootScope', '$scope', '$http', '$window',  function ($rootScope, $scope, $http, $window) {
        $scope.loginInfo = {
            username: null,
            password: null,
            rememberMe: false,
            isRemember: null,
            randCode: null,
            isAutoLogin: true // 设置为true是为了跳过验证码验证
        };
        $scope.loginErr = null;
        $scope.loginButtonText = "登录";
        var location = (window.location + '').split('/');
        var basePath = location[0] + '//' + location[2] + '/' + location[3];
        var isLoginSuccess = true;
        $scope.login = function () {
            if (!isLoginSuccess) {
                return;
            }
            isLoginSuccess = false;
            $scope.loginErr = null;
            if (!$scope.loginInfo.username && !$scope.loginInfo.password) {
                $scope.loginErr = "请输入登录账号和密码！";
                isLoginSuccess = true;
                return;
            }
            if (!$scope.loginInfo.username) {
                $scope.loginErr = "请输入登录账号！";
                isLoginSuccess = true;
                return;
            }
            if (!$scope.loginInfo.password) {
                $scope.loginErr = "请输入密码！";
                isLoginSuccess = true;
                return;
            }
            $scope.loginButtonText = "登录...";
            $scope.loginInfo.password = base64encode($scope.loginInfo.password);
            $http.post(basePath + "/loginForm.htm", $scope.loginInfo)
                .success(function (data, status, headers, config) {
                    isLoginSuccess = true;
                    $scope.loginButtonText = "登录";
                    if (data.status == 0 || data.status == 1003) {
                        $window.location.href = basePath
                            + "/mobile/tuition/index.html";
                    } else {
                        $scope.loginErr = data.message;
                    }
                });
        };


    }]);