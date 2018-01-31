/**
 * Project: yineng-corpSysLand
 * Title: fileName
 * author xiechangwei
 * date 2017/5/8 11:13
 * Copyright: 2017 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
angular.module('myApp')
    .directive('activityView', ['$http', function ($http) {
        var template = '<div class="approving-title signal-wrap">' +
            '<h5>{{approveData.activityTitle}}</h5>' +
            '<p class="text-gray font-12">当前处理人&nbsp;:&nbsp;{{approveData.nowApproveName ?approveData.nowApproveName:"无"}}</p>' +
            '<div class="signal approving" ng-show="options.processScheduleStates ==3"></div>' +
            '<div class="signal back-edit" ng-show="options.processScheduleStates ==2"></div>' +
            '<div class="signal approved" ng-show="options.processScheduleStates ==1"></div>' +
            '</div>' +
            '<div class="approving-flow font-12 padding" ng-if="approveData.lastApproveName">' +
            '<p class="text-gray no-margin">上一审批人&nbsp;:&nbsp;<span>{{approveData.lastApproveName?approveData.lastApproveName:"无"}}</span></p>' +
            '<p class="no-margin"><span class="text-danger">审批意见&nbsp;:&nbsp;</span><span>{{approveData.message?approveData.message:"无"}}</span></p>' +
            '</div>';
        return {
            scope: {
                options: '='
            },
            template: template,
            controller: ['$scope', '$ionicModal', '$ionicLoading', '$ionicPopup',function ($scope, $ionicModal, $ionicLoading, $ionicPopup) {

                $scope.optionsData = angular.copy($scope.options);
                //根据流程实例id获取审批历史数据
                $http.post(basePath + "/third/xtbgactivity/findJSTXActHisTask?processInstanceId=" + $scope.options.actExecutionProcinstId).success(function (data) {
                    if (data.status == 0) {
                        //当前任务节点ID
                        $scope.approveData = data.result;
                    }
                });

            }]

        }
    }]);
