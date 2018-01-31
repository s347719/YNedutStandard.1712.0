(function(){
    'use strict';

    angular
        .module('myApp')
        .controller('jobPerformanceCtrl', jobPerformanceCtrl);
    jobPerformanceCtrl.$inject = ['$scope','$ionicModal','$http','$timeout'];
    function jobPerformanceCtrl($scope,$ionicModal,$http,$timeout){
        $ionicModal.fromTemplateUrl('check-detail.html',{
            scope: $scope,
            animation:'slide-in-right'
        }).then(function(modal){
            $scope.modalOne = modal;
        });
        $ionicModal.fromTemplateUrl('save-success.html',{
            scope: $scope
        }).then(function(modal){
            $scope.modalTwo = modal;
        });

        $scope.$on('performanceGroup',function(){
            var performanceList = angular.element('.performance-list');/*获取元素*/
            var perforListHeightArray = [];/*获取元素高度的集合*/
            for(var i = 0;i < performanceList.length;i++){
                perforListHeightArray.push(performanceList[i].clientHeight);
            }
            var max = perforListHeightArray[0];/*假设数组第一个数字是最大值*/
            var maxHeight = function(){ /*获取最大高度*/
                for(var j = 0; j < perforListHeightArray.length;j++){
                    if(perforListHeightArray[j] > max){
                        max = perforListHeightArray[j];
                    }
                }
                return max;
            };
            performanceList.css({height:maxHeight() + 'px'})
        });

        var url = basePath + "/third/jobPerformance";

        $scope.search=function(){
            $scope.showError = false;   //显示异常信息

            $http.get(url+"/getJSTXJobPerformanceByConditions").success(function(data){
                $scope.conditions = data.result;

                if(data.status=="0"){
                    /**
                     * assessmentStatus 比较状态 0 当月未考核数据或不比较数据  1 增长 2 下降 3 持平
                     * value 与上月比较相差值
                     */
                    for(var i=0;i<$scope.conditions.length;i++ ){
                        if(!$scope.conditions[i].workCofficient){ //工作
                            $scope.conditions[i].workStatus = 0
                        }else{
                            if(i==($scope.conditions.length-1)){
                                $scope.conditions[i].workStatus = 0
                            }else{
                                $scope.conditions[i].workValue= ($scope.conditions[i].workCofficient-$scope.conditions[i+1].workCofficient).toFixed(2);
                                if($scope.conditions[i].workValue>0)  $scope.conditions[i].workStatus = 1;
                                if($scope.conditions[i].workValue<0)  $scope.conditions[i].workStatus = 2;
                                if($scope.conditions[i].workValue==0)  $scope.conditions[i].workStatus = 3;
                            }
                        }

                        if(!$scope.conditions[i].assessmentCofficient){ //履职
                            $scope.conditions[i].assessmentStatus = 0
                        }else{
                            if(i==($scope.conditions.length-1)){
                                $scope.conditions[i].assessmentStatus = 0
                            }else{
                                $scope.conditions[i].assessmentValue= ($scope.conditions[i].assessmentCofficient-$scope.conditions[i+1].assessmentCofficient).toFixed(2);
                                if($scope.conditions[i].assessmentValue>0)  $scope.conditions[i].assessmentStatus = 1;
                                if($scope.conditions[i].assessmentValue<0)  $scope.conditions[i].assessmentStatus = 2;
                                if($scope.conditions[i].assessmentValue==0)  $scope.conditions[i].assessmentStatus = 3;
                            }
                        }

                    }
                }else{
                    $scope.showError = true;
                }


            });
        };

        $scope.search();

        /**
         * 根据码表userDictCode获取dictName
         * @param item 码表code
         * @returns {string}  码表dictName
         */
        $scope.getCodeName = function(item){
            var resData="";
            item && angular.forEach($scope.postPerformanceIndexList,function(val){
                if(item == val.userDictCode){
                    resData = val.dictName;
                }
            });
            return resData;
        };

        /*查找岗位履职指标考核方式码表信息*/
        $http.get(url + "/findPostPerformanceIndexCodeTable").success(function(data){
            $scope.postPerformanceIndexList = data;
        });
        /**
         * 面谈结果查询
         * @param data 数据对象
         * @param status 数据来源 1 工作面谈； null 履职面谈
         */
        $scope.openModalOne = function(item,status){
            var id = status?item.workId:item.assessmentId;
            $http.get(url + "/getJXGLPerformanInterviewById?id="+id).success(function(data){
                $scope.item = item;
                $scope.searchCondition = data.result;
                //获取对应码表值
                if(status){
                  angular.forEach($scope.searchCondition.jxglWorkAchievementsDTOList,function(value){
                      if(value.jxglLevelContentDTO.leveldescription.indexOf("：")!=-1)value.jxglLevelContentDTO.leveldescription=value.jxglLevelContentDTO.postPerformIndexCode
                  })
                }
            });
            $scope.modalOne.show();
        };

        /**
         * 保存确认
         */
        $scope.save = function(){
            var newData = {
                id:$scope.searchCondition.id,   //确认面谈id
                interviewStatus:3   //确认状态
            };

            $http.post(url + "/updateJXGLPerformanInterviewByState",newData).success(function(data){
                if(data.status == "0"){
                    $scope.modalTwo.show();
                    $timeout(function() {
                        $scope.modalTwo.hide(); //hide the modal after 3 seconds for some reason
                        $scope.modalOne.hide(); //hide the modal after 3 seconds for some reason
                        $scope.search();
                    }, 3000);
                }
            })
        };

        $scope.closeModalOne = function(){
            $scope.modalOne.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function() {
            $scope.modalOne.remove();
        });
    }
})();
