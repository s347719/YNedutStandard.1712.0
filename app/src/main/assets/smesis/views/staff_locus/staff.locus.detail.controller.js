(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('staffLocusDetailController', staffLocusDetailController);

    staffLocusDetailController.$inject = ['$scope', '$http',"$location"];
    function staffLocusDetailController($scope, $http,$location){

        // 页面用户名字
        $scope.userName = $location.search().name;
        $scope.orgName = $location.search().orgName;

        $scope.showList = null;
        // 获取对应的轨迹信息
        $http.post(basePath +"/third/stafflocus/getLocationDetailByuserId?id="+$location.search().id).success(function(data){
            var total = [];
            $scope.messageList = {name:"",messlist:[]};
            if(data.result){
                var toDay = [],zuotian = [],qiantian=[],ntian=[];
                angular.forEach(data.result,function(val){
                    if(val.howLongAgo.indexOf("天前")==-1){
                        toDay.push(val);
                    }else{
                        var num = Number(val.howLongAgo.replace("天前",""));
                        if(num==1){//昨天
                            zuotian.push(val);
                        }else if(num==2){//前天
                            qiantian.push(val);
                        }else{//多少天前
                            ntian.push(val);
                        }
                    }
                });
                if(toDay.length>0){
                    $scope.messageList = {};
                    $scope.messageList.name = "今天";
                    $scope.messageList.dateTime = toDay[0].positionTime.substring(0,10);
                    $scope.messageList.messlist = toDay;
                    total.push($scope.messageList);
                }
                if(zuotian.length>0){
                    $scope.messageList = {};
                    $scope.messageList.name = "昨天";
                    $scope.messageList.dateTime = zuotian[0].positionTime.substring(0,10);
                    $scope.messageList.messlist = zuotian;
                    total.push($scope.messageList);
                }
                if(qiantian.length>0){
                    $scope.messageList = {};
                    $scope.messageList.name = "前天";
                    $scope.messageList.dateTime = qiantian[0].positionTime.substring(0,10);
                    $scope.messageList.messlist = qiantian;
                    total.push($scope.messageList);
                }
                if(ntian.length>0){
                    var one=0,two=0,three= 0,onelist=[],twolist=[],threelist=[];
                    angular.forEach(ntian,function(va){
                        var num = Number(va.howLongAgo.replace("天前",""));
                        if(num>2){
                            if(one==0 || one==num){
                                one = num;
                                onelist.push(va);
                            }else if(two==0 || two==num){
                                two = num;
                                twolist.push(va);
                            }else if(three==0 || three==num){
                                three = num;
                                threelist.push(va);
                            }
                        }
                    });
                    if(one!=0){
                        $scope.messageList = {};
                        $scope.messageList.name = one+"天前";
                        $scope.messageList.dateTime = onelist[0].positionTime.substring(0,10);
                        $scope.messageList.messlist = onelist;
                        total.push($scope.messageList);
                    }
                    if(two!=0){
                        $scope.messageList = {};
                        $scope.messageList.name = two+"天前";
                        $scope.messageList.dateTime = twolist[0].positionTime.substring(0,10);
                        $scope.messageList.messlist = twolist;
                        total.push($scope.messageList);
                    }
                    if(three!=0){
                        $scope.messageList = {};
                        $scope.messageList.name = three+"天前";
                        $scope.messageList.dateTime = threelist[0].positionTime.substring(0,10);
                        $scope.messageList.messlist = threelist;
                        total.push($scope.messageList);
                    }
                }
                $scope.showList = total;
                angular.forEach($scope.showList,function(info){
                    if(info.messlist.length>0){
                        var detailList = [],showTime="",showFlag=false;

                        //时间倒序
                        info.messlist.sort(function(a,b){
                            var dataA = new Date(a.positionTime);
                            var dataB = new Date(b.positionTime);
                            return dataB-dataA});

                        //处理提留时间
                        angular.forEach(info.messlist,function(item){
                            item.showFlag = false;
                            if(item.standingTime>0){
                                if(Math.floor(item.standingTime/60)){
                                    showTime =  Math.floor(item.standingTime/60) + "小时" + Math.floor((item.standingTime%60)) + "分钟";
                                }else{
                                    showTime =  Math.floor((item.standingTime%60)) + "分钟";
                                }
                                showFlag=true;
                                detailList.push({showFlag:showFlag,showTime:showTime});
                            }
                            detailList.push(item);
                        });
                        info.messlist = detailList;
                    }
                });

            }
        })


    }
})();
