/**
 * Created by xionghongsong on 2017/1/9.
 * 打分项目
 */
angular.module('starter').controller("projectGradeCtrl", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification","tkStorageService", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification,tkStorageService) {
    $scope.tkRegisterData = tkStorageService.getItemNoDel("tkRegisterData");
    $scope.saveTKResultRegisterVO =  $scope.tkRegisterData.saveTKResultRegisterVO;
    $scope.scoreItemList = $scope.tkRegisterData.scoreItemList;

    $scope.verifyScore = function(item){
        var maxScore = parseFloat(item.score);
        var currentScore = item.value;
        if(!currentScore){
            return ;
        }
        currentScore = parseFloat(currentScore);
        if(currentScore<0){
            currentScore = 0;
        }else if(currentScore>maxScore){
            currentScore = maxScore;
        }
        //验证两位小数
        if(!(/^\d*(\.\d{1,2})?$/.test(currentScore))){
            var scoreStr = currentScore+"";
            var strs = scoreStr.split(".");
            if(strs.length == 1){
                currentScore=parseFloat(strs[0]);
            }else if(strs.length == 2){
                if(strs[1].length>2){
                    strs[1] =  strs[1].substring(0,2);
                }
                currentScore = parseFloat(strs[0]+"."+strs[1]);
            }
        }
        item.value = parseFloat(currentScore);

    }
    $scope.clickSub = function(){
        var itemTotalScore = 0;
        angular.forEach($scope.scoreItemList,function(item){
            $scope.verifyScore(item);
            if(item.value){
                itemTotalScore=accAdd(itemTotalScore,parseFloat(item.value));
            }
            if (item.code) {
                $scope.saveTKResultRegisterVO[item.code] = item.value;
            } else {
                $scope.saveTKResultRegisterVO.customItemMap[item.id] =  item.value;
            }
        });
        $scope.saveTKResultRegisterVO.itemTotalScore = itemTotalScore;
        $scope.tkRegisterData.saveTKResultRegisterVO = $scope.saveTKResultRegisterVO;
        $scope.tkRegisterData.scoreItemList = $scope.scoreItemList;
        $scope.saveTKResultRegisterVO.totalScore = itemTotalScore;
        tkStorageService.setItem("tkRegisterData",$scope.tkRegisterData);
        $location.path("/register");

    }
    $scope.cancel = function(){
        $location.path("/register");
    }
    $scope.updateScore = function(item,type){
        if(!parseFloat(item.value)){
            item.value = 0;
        }
        if(type == 0){
            item.value = accAdd(parseFloat(item.value),0.5);
        }else{
            item.value = accSub(parseFloat(item.value),0.5);
        }
        $scope.verifyScore(item);
    }
    //js浮点数计算不准确计算解决
    function mul(a, b) {
        var c = 0,
            d = a.toString(),
            e = b.toString();
        try {
            c += d.split(".")[1].length;
        } catch (f) {}
        try {
            c += e.split(".")[1].length;
        } catch (f) {}
        return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
    }
    function div(a, b) {
        var c, d, e = 0,
            f = 0;
        try {
            e = a.toString().split(".")[1].length;
        } catch (g) {}
        try {
            f = b.toString().split(".")[1].length;
        } catch (g) {}
        return c = Number(a.toString().replace(".", "")), d = Number(b.toString().replace(".", "")), mul(c / d, Math.pow(10, f - e));
    }
    function accAdd(a,b){
        var c, d, e;
        try {
            c = a.toString().split(".")[1].length;
        } catch (f) {
            c = 0;
        }
        try {
            d = b.toString().split(".")[1].length;
        } catch (f) {
            d = 0;
        }
        return e = Math.pow(10, Math.max(c, d)), (mul(a, e) + mul(b, e)) / e;
    }
    function accSub(a,b){
        var c, d, e;
        try {
            c = a.toString().split(".")[1].length;
        } catch (f) {
            c = 0;
        }
        try {
            d = b.toString().split(".")[1].length;
        } catch (f) {
            d = 0;
        }
        return e = Math.pow(10, Math.max(c, d)), (mul(a, e) - mul(b, e)) / e;
    }
}]);