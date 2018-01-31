(function(){
    'use strict';

    angular
        .module('myApp')
        .controller('outworkApplyController', outworkApplyController);
    outworkApplyController.$inject = ['$scope', '$http',"$location","$ionicModal"];
    function outworkApplyController($scope, $http,$location,$ionicModal){
        // 获得用户id
        $scope.platformSysUserId = $location.search().platformSysUserId;
        // token获取
        $scope.access_token =  $location.search().access_token;
        $scope.list = [];
        $scope.errorFlag = false;

        $scope.addSecurityFlag = false;

        $scope.moreData = true;

        $scope.pageNumber = 0;

        $http.post(
            basePath + "/third/jstxOutSideManageResource/getXTBGOutSideManageListById?pageNumber="+$scope.pageNumber+"&pageSize=10")
            .success(function(data){
                if(data.status == 0){
                    if(null != data.result){
                        $scope.list = data.result.content;
                    }else{
                        $scope.errorFlag = true;
                    }
                }
            })
        /**
         * 编辑页面
         */
        $scope.edit = function(item){
            $location.path("/outwork_apply_add").search({id:item.id})
        }

        /**
         * 添加
         */
        $scope.add = function(){
            $location.path("/outwork_apply_add").search();
        }

        /**
         * 查看全部内容
         */
        var viewTemplate = '<ion-modal-view class="modal-opacity" ng-click="closeViewModal()"> ' +
            '<div class="modal-screen-full">'+
            '<div class="screen-center padding">{{instruction}}</div>'+
            '</div>'+
            '</ion-modal-view>';
        $scope.modal = $ionicModal.fromTemplate(viewTemplate, {
            scope: $scope,
            animation: 'slide-in-up'
        });
        $scope.viewModal = function (data) {
            $scope.instruction = data;
            $scope.modal.show();
        };

        $scope.goTo = {
            isGoTo: true,
            gotoFun: function(data,type){
                if(this.isGoTo){
                    if(type == 1){
                        this.isGoTo = !this.isGoTo;
                        $scope.viewModal(data)
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

        // 判断是否有申请 权限
        $http.get(
                basePath + "/third/jstxOutSideManageResource/checkAddSecurity?name=外勤申请" ).success(function(data){
                $scope.addSecurityFlag = data;
            }).error(function(){
            $scope.addSecurityFlag = false;
        })
        //上拉加载更多

        $scope.loadData = function(){
            $scope.pageNumber++;
            $http.post(
                    basePath + "/third/jstxOutSideManageResource/getXTBGOutSideManageListById?pageNumber="+$scope.pageNumber+"&pageSize=10")
                .success(function (data) {

                    if (null != data.result) {

                        $scope.list = $scope.list.concat(data.result.content);
                        if(data.result.content.length != 0){

                            $scope.$broadcast('scroll.infiniteScrollComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                        }else{
                            $scope.moreData = false;
                        }
                    }
                })
        }
            //下拉刷新页面
        $scope.doRefresh = function(){
            $http.post(
                    basePath + "/third/jstxOutSideManageResource/getXTBGOutSideManageListById?pageNumber=0&&pageSize="+($scope.pageNumber+1)*10)
                .success(function (data) {
                    if (data.result != null) {
                        $scope.list = data.result.content;
                        $scope.$broadcast('scroll.refreshComplete');//这里是告诉ionic更新数据完成，可以再次触发更新事件
                    }
                })
        }

        $scope.closeViewModal = function(){
            $scope.modal.hide();
        }
    }
})();
