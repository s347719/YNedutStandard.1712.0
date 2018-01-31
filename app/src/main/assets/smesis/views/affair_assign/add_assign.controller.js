/**
 * Project: yineng-corpSysLand
 * Package
 * Title: 添加修改事务交办页面控制器
 * author xiechangwei
 * date 2016/12/30 11:03
 * Copyright: 2016 www.yineng.com.cn Inc. All rights reserved.
 * version V1.0
 */
(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('addAssignlController', addAssignlController);

    addAssignlController.$inject = ['$scope', '$ionicPopup', '$http', '$filter', '$ionicLoading', '$ionicScrollDelegate', '$location', '$timeout'];
    function addAssignlController($scope, $ionicPopup, $http, $filter, $ionicLoading, $ionicScrollDelegate, $location, $timeout) {


        $scope.dataId = $location.search().id;
        $scope.showIsTab = $location.search().showIsTab;
        $scope.flagObj ={};
        //声明保存的对象
        $scope.assign = {watchUserSize:0,helpUserSize:0,teamUserIds:[],attentionUserIds:[],helpUserName:"",watchUserName:""};
        $http.post(basePath + "/third/businessjournal/findNowDate").success(function (data) {
            if(data.status == "0"){
                $scope.assign.startDate = data.result;
            }
        });
        //编辑获取数据
        if($scope.dataId){
            $http.post(basePath + "/third/affairsassigned/findJSTXAssignedById?assignedId="+$scope.dataId).success(function (data) {
                if (data.status == "0") {
                    $scope.assign = data.result;
                    if($scope.assign.priorityStatus &&$scope.assign.priorityStatus==1){
                        $scope.flagObj.important =true;
                    }else{
                        $scope.flagObj.important =false;
                    }
                    if($scope.assign.urgentStatus &&$scope.assign.urgentStatus==1){
                        $scope.flagObj.urgent = true;
                    }else{
                        $scope.flagObj.urgent = false;
                    }
                    //设置附件信息
                    if($scope.assign){
                       setFsAnnexIDsFun($scope.assign, $scope.assign.uploadVoList);
                    }
                    $scope.checkText(data.result.instruction)
                }
            })
        }
        //附件处理方法
        var setFsAnnexIDsFun = function ( info , uploadVoList) {
            //附件处理
            if(uploadVoList && uploadVoList.length){
                var fsAnnexIDs = uploadVoList.toString();
                info.fsAnnexIDs = fsAnnexIDs;
                $scope.$broadcast('filesDone');
            }
        };
        //返回到主页
        $scope.backToMain = function () {
            if($scope.dataId){
                var confirmPopup = $ionicPopup.confirm({
                    title : '返回提示',
                    template : '<h5 class="margin-bottom-0">返回后当前编辑内容将丢失,确定要返回吗？</h5>',
                    buttons : [
                        {
                            text : '继续编辑',
                            type : 'button-outline button-theme button-local'
                        },
                        {
                            text : '确定返回',
                            type : ' button-theme button-local',
                            onTap: function (e) {
                                $location.path("/view_affair").search({id:$scope.dataId,showIsTab:$scope.showIsTab});
                            }
                        }
                    ]

                });

            }else{
                $location.path('/affair_assign');
            }

        }
        //主办人
        $scope.selectUser = function (item) {
            $scope.assign.mainUserId = item.id;
            $scope.assign.mainUserName = item.name;
        }

        //事务类型
        $http.get(basePath + "/third/affairsassigned/findQJGLTaskAssignedAll").success(function (data) {
            $scope.transactionType = data;
        });
        //事务来源
        $http.get(basePath + "/third/affairsassigned/findTransactionSourcesCode").success(function (data) {
            $scope.transactionSource = data;
        });

        //选择事务类型
        $scope.typeSelect = function (item) {
            $scope.assign.transactionTypeCode = item.id;
            $scope.assign.qjglTaskAssignedDTO = item;
            $scope.assign.typeName = item.name;
        }

        //选择事务来源
        $scope.sourceSelect = function(item){
            $scope.assign.transactionSourceCode = item.dictCode;
            $scope.assign.fromName = item.dictName;

        }
        //控制操作按钮
        $scope.opreation ={canSubmit:false};

        //保存数据
        $scope.save = function(){
            $scope.checkData();

        }

        //验证必填项
        $scope.error = function(data){
            $ionicLoading.show({
                template: data,
                cssClass: 'text-color:red' // String, 附加的CSS样式类
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 2000);
        };

        $scope.checkData = function(){
            //验证必填效果
            if (!$scope.assign.name) {
                $scope.error ("事务名称不能为空!");
                return false;
            }
            if (!$scope.assign.instruction) {
                $scope.error("描述不能为空!");
                return false;
            }
            if (!($scope.assign.startDate)) {
                $scope.error("开始时间不能为空!");
                return false;
            }
            if($scope.assign.startDate != null && $scope.assign.endDate != null){
                if($scope.assign.startDate > $scope.assign.endDate){
                    $scope.error("开始日期不能大于结束日期!");
                    return false;
                }
            }

            if (!$scope.assign.mainUserName) {
                $scope.error("主办人不能为空!");
                return false;
            }
            if (!$scope.assign.typeName) {
                $scope.error("事务类型不能为空!");
                return false;
            }
            //主办人 关注人 协办人都不能相同
            var userFlag = true;
            if($scope.assign.attentionUserIds != null && $scope.assign.attentionUserIds.length >0){
                angular.forEach($scope.assign.attentionUserIds,function(item){
                    if(item ==$scope.assign.mainUserId){
                        userFlag = false;
                        return;
                    }

                })
            }
            if(!userFlag){
                $scope.error("关注人和主办人不能相同!");
                return false;
            }
            var teamFlag = true;
            if($scope.assign.teamUserIds != null && $scope.assign.teamUserIds.length >0){
                angular.forEach($scope.assign.teamUserIds,function(item){
                    if(item ==$scope.assign.mainUserId){
                        teamFlag = false;
                        return;
                    }

                })
            }
            if(!teamFlag){
                $scope.error("协办人和主办人不能相同!");
                return false;
            }
            var choiseFlag = true;
            if($scope.assign.teamUserIds.length >0 && $scope.assign.attentionUserIds.length >0){
                angular.forEach($scope.assign.attentionUserIds,function(item){
                    angular.forEach($scope.assign.teamUserIds,function(data){
                        if(item ==data){
                            choiseFlag = false;
                            return;
                        }

                    })

                })
            }
            if(!choiseFlag){
                $scope.error("关注人和协办人中不能有相同的人!");
                return false;
            }

            if($scope.flagObj.important){
                $scope.assign.priorityStatus =1;
            }else{
                $scope.assign.priorityStatus =2;
            }
            if($scope.flagObj.urgent){
                $scope.assign.urgentStatus =1;
            }else{
                $scope.assign.urgentStatus =2;
            }
            // 处理附件问题
            if( $scope.assign &&  $scope.assign.fsAnnexIDs){
                $scope.assign.uploadVoList = [];
                var upList = $scope.assign.fsAnnexIDs.split(",");
                angular.forEach(upList, function (u) {
                    if(u){
                        $scope.assign.uploadVoList.push(u);
                    }
                });
            }
            $scope.opreation.canSubmit = true;
            $http.post(basePath + "/third/affairsassigned/saveOrUpdateJSTXAffairsAssigned?", $scope.assign).success(function (data) {
                if (data.status == "0") {
                    if($scope.dataId){
                        $location.path("/view_affair").search({id:$scope.dataId});
                        $scope.opreation.canSubmit = false;
                    }else{
                        $scope.backToMain();
                        $scope.opreation.canSubmit = false;
                    }

                } else {
                    var alertPopup = $ionicPopup.alert({
                        title : '提交失败',
                        template : '<h5 class="margin-bottom-0">'+data.message+'</h5>',
                        buttons : [
                            {
                                text : '确认',
                                type : ' button-theme button-local'

                            }
                        ]
                    });
                }


            })

        }

        //验证字数
        $scope.min =0;
        $scope.checkText = function(text){
            $scope.min = text.length;

        }

        //选择关注人
        $scope.selectUserWatch = function(item){
            $scope.assign.attentionUserIds =[];
            $scope.assign.watchUserName ="";
            var userList =[];
            angular.forEach(item,function(data){
                $scope.assign.attentionUserIds.push(data.id)
                $scope.assign.watchUserName +=data.name +" ";
                userList.push(data)
                $scope.assign.watchUserList= angular.copy(userList);
            })
            $scope.assign.watchUserSize = $scope.assign.attentionUserIds.length;

        }

        //选择协助人
        $scope.selectUserHelp = function(item){
            $scope.assign.teamUserIds =[];
            $scope.assign.helpUserName ="";
            var userDataList =[];
            angular.forEach(item,function(data){
                $scope.assign.teamUserIds.push(data.id)
                $scope.assign.helpUserName +=data.name +" ";
                userDataList.push(data);

                $scope.assign.helpUserList = angular.copy(userDataList);
            })
            $scope.assign.helpUserSize = $scope.assign.teamUserIds.length;

        }

        //点击展开更多
        $scope.showMore = false;
        $scope.clickMore = function(){
            $ionicScrollDelegate.resize();
            $scope.showMore = !$scope.showMore;
        }



    }
})();
