(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('leaveApplyController', leaveApplyController);

    leaveApplyController.$inject = ['$scope', '$http',"$location","$ionicModal"];
    function leaveApplyController($scope, $http,$location,$ionicModal){
        // 获得用户id
        $scope.platformSysUserId = $location.search().platformSysUserId;
        // token获取
        $scope.access_token =  $location.search().access_token;

        $scope.errorFlag = false;

        $scope.addSecurityFlag = false;

        // 默认展示添加按钮页面
        $scope.showFlag = false;
        // 获取当前用户的请假信息
        $http.post(
            basePath + "/third/leaveapply/findXTBGMyLeaveManageByUserId" )
            .success(function(data){
                if(data.status == 0){
                    if(null != data.result){
                        $scope.showFlag = true;
                        $scope.list = data.result;
                        angular.forEach(  $scope.list,function(data){
                            data.flag = false;
                        })
                    }
                }else{
                    $scope.errorFlag = true;
                }
            }).error(function(){
                $scope.errorFlag = true;
            })

            // 判断是否有请假流程 权限
        $http.get(
            basePath + "/third/leaveapply/checkAddSecurity?name=请假申请" )
            .success(function(data){
                $scope.addSecurityFlag = data;
            }).error(function(){
                $scope.addSecurityFlag = false;
            })

        /**
         * 切换备注和内容
         * @param flag
         */
        $scope.changeFlag = function(item){
            item.flag = !item.flag;
        }

        /**
         * 编辑页面
         */
        $scope.edit = function(item){
            $location.path("/add_apply").search(item)
        }

        /**
         * 添加
         */
        $scope.add = function(item){
            $location.path("/add_apply");
        }

        /**
         * 查看全部请假原因
         */
        /*查看全部请假原因弹框*/
        var viewReasonTemplate = '<ion-modal-view class="modal-opacity" ng-click="closeViewReasonModal()"> ' +
            '<div class="modal-screen-full">'+
            '<div class="screen-center padding">{{instruction}}</div>'+
            '</div>'+
            '</ion-modal-view>';
        $scope.modal = $ionicModal.fromTemplate(viewReasonTemplate, {
            scope: $scope,
            animation: 'slide-in-up'
        });
        $scope.viewReasonModal = function (data) {
            $scope.instruction = data;
            $scope.modal.show();

        };

        $scope.goTo = {
            isGoTo: true,
            gotoFun: function(data,type){
                if(this.isGoTo){
                    if(type == 1){
                        this.isGoTo = !this.isGoTo;
                        $scope.viewReasonModal(data)
                    }else if(type == 2){
                        this.isGoTo = !this.isGoTo;
                        $scope.edit(data);

                    }
                    setTimeout(function(){
                        $scope.goTo.isGoTo = true;
                    },100);

                }
            }
        };

        $scope.refreshModal = function (data) {
            // 获取当前用户的请假信息
            $http.post(
                basePath + "/third/leaveapply/findXTBGMyLeaveManageByUserId" )
                .success(function(data){
                    if(data.status == 0){
                        if(null != data.result){
                            $scope.showFlag = true;
                            $scope.list = data.result;
                            angular.forEach(  $scope.list,function(data){
                                data.flag = false;
                            })
                        }
                    }else{
                        $scope.errorFlag = true;
                    }
                }).error(function(){
                    $scope.errorFlag = true;
                })

        };
        $scope.closeViewReasonModal = function () {
            $scope.modal.hide();
        };
        //Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function () {
            $scope.modal.remove();
        });
    }
})();
