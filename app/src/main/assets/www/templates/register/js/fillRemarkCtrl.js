/**
 * Created by YN on 2017/1/10.
 */
/**
 * Created by xionghongsong on 2017/1/9.
 * 打分项目
 */
angular.module('starter').controller("fillRemarkCtrl", ['$scope', '$location',"tkStorageService", function ($scope, $location,tkStorageService) {
    $scope.tkRegisterData = tkStorageService.getItemNoDel("tkRegisterData");
    $scope.tkRegisterContentType = tkStorageService.getItemNoDel("tkRegisterContentType");
    $scope.placeholder = "";
    $scope.item = {};
    if($scope.tkRegisterContentType == "suggestion"){
        $scope.placeholder = " 请填写意见或建议（非必填）";
        $scope.item = $scope.tkRegisterData.suggestionItem;
    }else if($scope.tkRegisterContentType == "actContent"){
        $scope.placeholder = " 请填写实际讲解内容";
        $scope.item = $scope.tkRegisterData.actContentItem;
    }
    $scope.saveTKResultRegisterVO =  $scope.tkRegisterData.saveTKResultRegisterVO;
    $scope.clickSub = function(){
        $scope.tkRegisterData.saveTKResultRegisterVO = $scope.saveTKResultRegisterVO;
        tkStorageService.setItem("tkRegisterData",$scope.tkRegisterData);
        $location.path("/register");

    }
    if($scope.saveTKResultRegisterVO[$scope.item.code]){
        $scope.inputWordLenth =$scope.saveTKResultRegisterVO[$scope.item.code].length;
    }else{
        $scope.inputWordLenth = 0;
    }
    $scope.showWordLenth = function(word){
        if(word){
            $scope.inputWordLenth = word.length;
        }else{
            $scope.inputWordLenth = 0;
        }
    }
    $scope.cancel = function(){
        $location.path("/register");
    }
}]);