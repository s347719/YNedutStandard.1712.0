/**
 * Created by xionghongsong on 2017/1/5.
 * 选择授课计划
 */

angular.module('starter').controller("lessonsPlanCtrl", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification","tkStorageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification,tkStorageService) {
    $scope.tkRegisterData = tkStorageService.getItemNoDel("tkRegisterData");
    $scope.saveTKResultRegisterVO =  $scope.tkRegisterData.saveTKResultRegisterVO;
    $scope.isOnly = false;
    //判断听课信息中有教学班则获取这个这个教学班级的授课计划（不可选择）    如果不存在教学班则获取授课教师对应课程的教学班（下拉选择）
    if($scope.saveTKResultRegisterVO.pkTeachClassId){
        $scope.selectTeachClassId = $scope.saveTKResultRegisterVO.pkTeachClassId;
        $scope.selectTeachClassName = $scope.saveTKResultRegisterVO.pkTeachClassName;
        $scope.isOnly = true;
    }else{
        $scope.teachClassList = $scope.tkRegisterData.teachClassList;
        $scope.selectTeachClassId = $scope.teachClassList[0].id;
        $scope.selectTeachClassName = $scope.teachClassList[0].name;
    }
    $scope.saveTKResultRegisterVO =  $scope.tkRegisterData.saveTKResultRegisterVO;
    //选择的周次 默认为听课信息的周次
    $scope.selectWeek = $scope.saveTKResultRegisterVO.week;
    //选择教学班级
    $scope.clickTeachClass = function(item){
        if( $scope.selectTeachClassId  == item.id){
            return ;
        }
        $scope.selectTeachClassId = item.id;
        $scope.selectTeachClassName = item.name;
        $scope.getWeekInfo(function(){
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    }

    //获取教学班下面做过授课计划的周次
    $scope.getWeekInfo = function(func) {
        $scope.selectWeek = $scope.saveTKResultRegisterVO.week;
        $scope.pageInfo = {
            totalPage:0,
            pageSize:20,
            pageNumber:0,
            itemSize:0
        };
        //清空教学计划数据 以及周次数据
        $scope.weekList = [];
        $scope.studyPlanList = [];
        $http.post(originBaseUrl + "/third/tkTaskRegisterApp/getClassTimeWeekList.htm?teachClassId=" + $scope.selectTeachClassId).success(function (data) {
            if (func) {
                func();
            }
            $scope.emptyInfo = false;
            if (data.status == 0) {
                $scope.weekList = data.result;
                if (!$scope.weekList || $scope.weekList.length == 0) {
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '没有授课计划';
                }else{
                    var isExist = false;
                    angular.forEach($scope.weekList,function(week){
                        if(week == $scope.selectWeek){
                            isExist = true;
                        }
                    });
                    if(!isExist){
                        $scope.selectWeek = $scope.weekList[0];
                    }

                    $ionicLoading.show({
                        template: '正在加载...'
                    });
                    $scope.getStudyChapterInfo(function () {
                        $ionicScrollDelegate.scrollTop();
                        $ionicLoading.hide();
                    });
                }
            }
            else {
                ynuiNotification.error({msg: data.message});
            }
        }).error(function () {
            if (func) {
                func();
            }
            $scope.emptyInfo = true;
            $scope.dataErrorMsg = '加载失败';
        });
    }
    $ionicLoading.show({
        template: '正在加载...'
    });
    //首次获取当前选中教学班下有授课计划周次
    $scope.getWeekInfo(function(){
        $ionicScrollDelegate.scrollTop();
        $ionicLoading.hide();
    });
    $scope.studyPlanList = [];
    //获取当前选中教学班下对应周次的授课计划
    $scope.getStudyChapterInfo = function(func){
        $http.post(originBaseUrl + '/third/tkTaskRegisterApp/getStudyPlanClassTime.htm?teachClassId='+$scope.selectTeachClassId+"&weeks="+$scope.selectWeek+"&pageNumber=" + $scope.pageInfo.pageNumber+ "&pageSize=20").success(function (data) {
            if (func) {
                func();
            }
            if (data.status == 0) {
                $scope.studyPlanList.push.apply($scope.studyPlanList,data.result.content);
                $scope.pageInfo = {
                    totalPage:data.result.totalPages,
                    pageSize:data.result.size,
                    pageNumber:data.result.number,
                    itemSize:data.result.totalElements
                };
                if(data.result.totalPages>0){
                    $scope.pageInfo.pageNumber = data.result.number + 1;
                }
            }else{
                ynuiNotification.error({msg: data.message});
            }
        }).error(function () {
            if (func) {
                func();
            }
            $scope.emptyInfo = true;
            $scope.dataErrorMsg = '加载失败';
        });
    }
    /**
     * 加载更多
     */
    $scope.loadMore = function () {
        if ($scope.pageInfo.pageNumber >= $scope.pageInfo.totalPage) {
            $scope.$broadcast('scroll.infiniteScrollComplete');
            return false;
        }
        $ionicLoading.show({
            template: '正在加载...'
        });
        $scope.getStudyChapterInfo(function () {
            $ionicLoading.hide();
            $scope.$broadcast('scroll.infiniteScrollComplete');
        });
    };
    $scope.moreDataCanBeLoaded = function () {
        return $scope.pageInfo && $scope.studyPlanList.length < $scope.pageInfo.itemSize;
    };
    $scope.clickWeek = function(week){
        if(week == $scope.selectWeek){
            return ;
        }
        $scope.selectWeek = week;
        $scope.pageInfo = {
            totalPage:0,
            pageSize:20,
            pageNumber:0,
            itemSize:0
        };
        $ionicLoading.show({
            template: '正在加载...'
        });
        $scope.studyPlanList = [];
        $scope.getStudyChapterInfo(function () {
            $ionicScrollDelegate.scrollTop();
            $ionicLoading.hide();
        });
    }
    //刷新
    $scope.doRefresh = function () {

        if(!$scope.weekList||$scope.weekList.length ==0){
            $ionicLoading.show({
                template: '正在刷新...'
            });
            $scope.getWeekInfo(function(){
                $ionicScrollDelegate.scrollTop();
                $ionicLoading.hide();
            });

        }else{
            $scope.loadMore();
        }

    };
    $scope.showContent = "";
    //授课计划内容显示
    $scope.clickContent = function(item){
        $scope.showContent = item.teachContent;
        $scope.modal.show();
    }
    $scope.hideModal = function(){
        $scope.modal.hide();
    }
    //点击选择按钮
    $scope.clickSelectBut = function(item){
        $scope.saveTKResultRegisterVO.lmsStudyChapterInfoId = item.id;
        var contentMsg = "第" + item.week + "周第" + item.classTime + "次课 " + item.teachContent;
        $scope.saveTKResultRegisterVO.lmsStudyChapterInfoShowValue = contentMsg;
        $scope.tkRegisterData.saveTKResultRegisterVO = $scope.saveTKResultRegisterVO;
        tkStorageService.setItem("tkRegisterData",$scope.tkRegisterData);
        $location.path("/register");

    }
    //取消
    $scope.cancel = function(){
        $location.path("/register");
    }
    $ionicModal.fromTemplateUrl('fullScreen.html', {
        scope: $scope,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.modal = modal;
    });
}]);