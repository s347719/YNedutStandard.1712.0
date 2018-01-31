/**
 * Created by liulijia on 2017/7/13.
 */
angular.module('starter')
    .config(function ($stateProvider, $urlRouterProvider) {
        $stateProvider
            /*登记任务-列表*/
            .state('report_login_view', {
                url: '/report_login_view',
                templateUrl: 'templates/report_login_view/reportLoginView.html',
                controller: 'reportLoginViewCtrl',
                cache: false
            });
        $urlRouterProvider.otherwise('/report_login_view');
    }).controller('reportLoginViewCtrl', ['$scope', '$sce', '$timeout', function ($scope, $sce, $timeout) {
        // 加载过渡
        var stop, loadingText = '', $loadingEl = $('.report-loading');
        var begainTimestamp = new Date().getTime();
        $loadingEl.show();
        loadingText = '载入中';
        stop = setInterval(function () {
            loadingText += '.';
            if (loadingText.length > 6) {
                loadingText = '载入中';
            }
            $loadingEl.text(loadingText);

            if (new Date().getTime() - begainTimestamp > 15000) {
                $loadingEl.text('载入超时');
                clearInterval(stop);
            }
        }, 500);
        function hideLoading() {
            $timeout(function () {
                $loadingEl.hide().text('');
                clearInterval(stop);
            }, 500)
        }

        function iframeload() {
            if (!iframe.readyState || iframe.reayState == "complete") {
                hideLoading();
            }
        }

        // 加载过滤 结束


        var GetQueryString = function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.hash.substr(window.location.hash.indexOf("?") + 1).match(reg);
            if (r != null)return r[2];
            return null;
        };
        var reportNumber = GetQueryString("reportNumber");
        var access_token = GetQueryString("access_token");
        var userId = GetQueryString("userId");
        var loginName = "";
        var passWord = "";
        var reportService = "";
        var resultUrl = "";
        var headUrl = originBaseUrl.replace("/ynedut", "");
        var iframe;
        $.ajax({
            url: originBaseUrl + '/third/reportView/viewLoginReportByNumber.htm?',
            data: {reportNumber: reportNumber, access_token: access_token, userId: userId},
            cache: false,
            async: false,
            type: "POST",
            dataType: 'json',
            success: function (data) {
                if (data.status == 0) {
                    console.log(data.result)
                    angular.forEach(data.result, function (value, key) {
                        if ("loginName" == key) {
                            loginName = value;
                        } else if ("passWord" == key) {
                            passWord = value;
                        } else if ("reportService" == key) {
                            reportService = headUrl + value;
                        } else {
                            resultUrl = headUrl + value;
                        }
                    });
                }
            }
        });

        //登陆成功后才开始初始化化报表页面
        var initReportPage = function () {
//            $scope.trueReportUrl = $sce.trustAsResourceUrl(resultUrl);
//            $scope.$apply();
            iframe = document.getElementById('reportFrame');
            iframe.onload = iframe.onreadystatechange = iframeload;
            iframe.src = resultUrl;
        };
        var reportState = {success: false};//登录是否成功状态
        var i = 0;
        loginReport(loginName, passWord, reportService, reportState);
        setTimeout(function () {
            if (!reportState.success) {
                var loginInterval = window.setInterval(function () {
                    loginReport(loginName, passWord, reportService, reportState);
                    if (reportState.success || i > 3) {
                        window.clearInterval(loginInterval);
                        if (reportState.success) {
                            initReportPage();
                        }
                    } else {
                        i++;
                    }
                }, 1000)
            } else {
                initReportPage();
            }
        }, 1000);

    }]);