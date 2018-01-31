/**
 * 操行分考核-填写考核信息-控制器
 * Created by wangl on 2017/8/10 13:46
 *
 */
angular.module('starter').controller('fillInCheckInfoCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location","creditStorageService","$ionicPopup","$ionicSlideBoxDelegate","$ionicModal",
    function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location,creditStorageService,$ionicPopup,$ionicSlideBoxDelegate,$ionicModal) {
        // 已选中的学生
        $scope.selectedStuList = creditStorageService.getItemNoDel("credit_selectedStuList");
        // 已选中的考核项目
        $scope.ckeckItem = creditStorageService.getItemNoDel("credit_checkItem");
        // 已缓存的已填写的考核信息
        var checkInfoCache = creditStorageService.getItemNoDel("credit_checkInfo");

        $scope.locationStr = "center";
        // $scope.ckeckItem.isAllowAdjust = false;
        // 是否展开显示考核项目描述详情
        $scope.showAllRemark = false;
        // 考核是否成功标志
        $scope.checkSuccess = false;
        $scope.getCurrentTime = function(){
            var currentTime = new Date();
            var month = (currentTime.getMonth() + 1) > 9 ? (currentTime.getMonth() + 1) : '0' + (currentTime.getMonth() + 1);
            var day = currentTime.getDate() > 9 ? currentTime.getDate() : '0' + currentTime.getDate();
            var hour = currentTime.getHours() > 9 ? currentTime.getHours() : '0' + currentTime.getHours();
            var minute = currentTime.getMinutes() > 9 ? currentTime.getMinutes() : '0' + currentTime.getMinutes();
            var currentTimeStr = currentTime.getFullYear() + "-" + month + "-" + day + " " + hour + ":" +
                minute;
            return currentTimeStr;
        };
        $scope.initFiles = [];
        $scope.getDfsIds = function(data){
            $scope.checkInfo.fastDfsIds = data.data;
        };

        // 考核信息
        $scope.checkInfo = null;

        // 如果有缓存就从缓存中读取
        if (checkInfoCache) {
            $scope.checkInfo = checkInfoCache;
            // 如果考核项目已改变
            if ($scope.checkInfo.examineItemId != $scope.ckeckItem.id) {
                $scope.checkInfo.examineItemId = $scope.ckeckItem.id;
                $scope.checkInfo.score = $scope.ckeckItem.creditScore;
                $scope.checkInfo.scoreNumber = 1;
                $scope.checkInfo.creditType = $scope.ckeckItem.creditType;
                $scope.checkInfo.isEffectAdminClassExamine = $scope.ckeckItem.isAffectClassCheck;
                $scope.checkInfo.convertClassCheckWay = $scope.ckeckItem.convertClassCheckWay;
                $scope.checkInfo.datetime = $scope.getCurrentTime();
            }
            $scope.initFiles = angular.copy($scope.checkInfo.fastDfsIds);
        } else {
            $scope.checkInfo = {
                examineItemId: $scope.ckeckItem.id,
                score: $scope.ckeckItem.creditScore,
                scoreNumber: 1,
                //0 减分 1 加分
                creditType: $scope.ckeckItem.creditType,
                totalSocre: "",
                isEffectAdminClassExamine: $scope.ckeckItem.isAffectClassCheck,
                convertClassCheckWay: $scope.ckeckItem.convertClassCheckWay,
                stuIdList: [],
                fastDfsIds: [],
                recordSource: 1,
                examineDate: "",
                examineTime: "",
                remark: "",
                examineAddress: "",
                datetime: $scope.getCurrentTime()
            };
        }


        var reg = /^\d{1,4}(\.\d{1,1})?$/;

        $scope.validateScore = function (score) {
            if (!score) {
                ynuiNotification.warning({msg: '请填写' + ($scope.ckeckItem.creditType == 0 ? '扣分' : '加分') + "分值"});
                return false;
            }
            if (!reg.test(score)) {
                ynuiNotification.warning({msg: "单次分值只能是" + $scope.ckeckItem.creditBeginScore + "~" + $scope.ckeckItem.creditEndScore + "以内的数字，且只能保留一位小数！"});
                return false;
            }
            if (parseFloat($scope.checkInfo.score) > parseFloat($scope.ckeckItem.creditEndScore)) {
                ynuiNotification.warning({msg: "不能大于" + $scope.ckeckItem.creditEndScore + "分"});
                $timeout(function () {
                    $scope.checkInfo.score = $scope.ckeckItem.creditEndScore;
                }, 500);
                return false;
            }
            if (parseFloat($scope.checkInfo.score) < parseFloat($scope.ckeckItem.creditBeginScore)) {
                ynuiNotification.warning({msg: "不能小于" + $scope.ckeckItem.creditBeginScore + "分"});
                $timeout(function () {
                    $scope.checkInfo.score = $scope.ckeckItem.creditBeginScore;
                }, 500);
                return false;
            }
            return true;
        };

        /**
         * 监听项目打分值变化
         */
        $scope.$watch("checkInfo.score", function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.validateScore(newVal);
            }
        });


        //获取常用考核备注
        $scope.getRemarkList = function (item,isShowModal) {
            $scope.remarkList = [];
            $ionicLoading.show({
                template: "正在加载..."
            });
            $http.post(originBaseUrl + "/third/behavioRassess/queryCommonRemarkList.htm?itemId="+item.id).success(function (data) {
                $ionicLoading.hide();
                if (data.status == 0) {
                    $scope.remarkList = data.result;
                    if(isShowModal){
                        $scope.remarkModal.show();
                    }

                }
            }).error(function () {
                $ionicLoading.hide();
            });

        };
        $scope.selectedRemark = null;
        $scope.closeModal = function () {
            $scope.remarkModal.hide();
        };
        //选择常用备注
        $scope.selectRemarkFun = function(item){
            $scope.checkInfo.remark = item.name;
            $scope.selectedRemark = item;
            $scope.closeModal();
        };

        $scope.addAndSubNumber = function(isAdd){

            if(isAdd){
                if(parseFloat($scope.checkInfo.scoreNumber)>=999){
                    return false;
                }
                $scope.checkInfo.scoreNumber = parseFloat($scope.checkInfo.scoreNumber) +1;
            }else{
                if(parseFloat($scope.checkInfo.scoreNumber)<=1){
                    return false;
                }
                $scope.checkInfo.scoreNumber = parseFloat($scope.checkInfo.scoreNumber) -1;
            }
        }
        $scope.$watch("checkInfo.scoreNumber",function(){
            if(parseFloat($scope.checkInfo.scoreNumber)<=0){
                $scope.checkInfo.scoreNumber = 1;
            }
            $scope.verifyNumber ();
        });
        $scope.verifyNumber = function(){
            var reg = /^\+?[1-9][0-9]{0,5}$/;
            if (reg.test($scope.checkInfo.scoreNumber) ) {
                if(parseFloat($scope.checkInfo.scoreNumber)<=0){
                    return false;
                }
                return true;
            }else{
                ynuiNotification.warning({msg: "次数只能是正整数!"});
                return false;
            }

        }

        $scope.addAndSubScore = function(isAdd){

            if(isAdd){
                if(parseFloat($scope.checkInfo.score)<parseFloat($scope.ckeckItem.creditEndScore)){
                    $scope.checkInfo.score = accAdd(parseFloat($scope.checkInfo.score),parseFloat(0.1));
                    if(parseFloat($scope.checkInfo.score)>=parseFloat($scope.ckeckItem.creditEndScore)){
                        ynuiNotification.warning({msg: "不能大于"+$scope.ckeckItem.creditEndScore+"分"});
                        return;
                    }
                    if(parseFloat($scope.checkInfo.score)<=parseFloat($scope.ckeckItem.creditBeginScore)){
                        ynuiNotification.warning({msg: "不能小于"+$scope.ckeckItem.creditBeginScore+"分"});
                        return;
                    }
                }

            }else{
                if(parseFloat($scope.checkInfo.score)>parseFloat($scope.ckeckItem.creditBeginScore)){
                    $scope.checkInfo.score = accSub(parseFloat($scope.checkInfo.score),parseFloat(0.1));
                    if(parseFloat($scope.checkInfo.score)>=parseFloat($scope.ckeckItem.creditEndScore)){
                        ynuiNotification.warning({msg: "不能大于"+$scope.ckeckItem.creditEndScore+"分"});
                        return false;
                    }
                    if(parseFloat($scope.checkInfo.score)<=parseFloat($scope.ckeckItem.creditBeginScore)){
                        ynuiNotification.warning({msg: "不能小于"+$scope.ckeckItem.creditBeginScore+"分"});
                        return false;
                    }
                }
            }


        };

        /**
         * 确认考核信息对话框参数信息
         *
         */
        $scope.confirmPopParam = {
            stuNameStr: null,
            checkItemName: null,
            scoreStr: null
        };

        var openConfirmPopParam = function () {
            var stuNameStr = "";
            angular.forEach($scope.selectedStuList, function (stu) {
                if (stuNameStr.length > 0) {
                    stuNameStr += "、";
                }
                stuNameStr += stu.stuName;
            });
            $scope.confirmPopParam.stuNameStr = stuNameStr;
            $scope.confirmPopParam.checkItemName = $scope.ckeckItem.name;
            $scope.confirmPopParam.scoreStr =  ($scope.ckeckItem.creditType == '0' ? '扣' : '加') + mul($scope.checkInfo.score,$scope.checkInfo.scoreNumber)  + "分";
            var confirmPopup = $ionicPopup.confirm({
                title:"请确认考核信息",
                templateUrl: 'templates/moralityMeasurement/templates/confirmCheckInfoPop.html',
                scope: $scope,
                cancelText:"取消",
                okText:"确认提交"
            });
            confirmPopup.then(function(res) {
                $ionicLoading.hide();
                if(res) {
                    submitCheckInfo();
                }
            });
        };

        $scope.submit = function(){
            if(!$scope.validateScore($scope.checkInfo.score)){
                $ionicScrollDelegate.scrollTop();
                return;
            }
            if(!$scope.verifyNumber()){
                $ionicScrollDelegate.scrollTop();
                return;
            }
            // 验证是否选了学生
            if ($scope.selectedStuList.length == 0) {
                ynuiNotification.warning({msg: "请添加学生！"});
                return;
            }
            if($scope.checkInfo.datetime){
                var dateAndTime = $scope.checkInfo.datetime.split(" ");
                if(!dateAndTime||dateAndTime.length!=2){
                    return false;
                }
                $scope.checkInfo.examineDate=dateAndTime[0];
                $scope.checkInfo.examineTime=dateAndTime[1];
            }
            // 验证考核日期是否被锁定
            $http.post(originBaseUrl + "/third/behavioRassess/isLockDate.htm", {date: $scope.checkInfo.examineDate}).success(function (data) {
                if (data.status == 0) {
                    if (data.result) {
                        ynuiNotification.warning({msg: "考核日期" + $scope.checkInfo.examineDate + "已被锁定，请重新选择！"});
                        return;
                    } else {
                        $scope.checkInfo.stuIdList = [];
                        angular.forEach($scope.selectedStuList,function(item){
                            $scope.checkInfo.stuIdList.push(item.platformSysUserId);
                        });
                        $scope.checkInfo.totalSocre = mul(parseFloat($scope.checkInfo.score),parseFloat($scope.checkInfo.scoreNumber));
                        openConfirmPopParam()
                    }
                }
            });
        };

        /**
         * 向后台提交数据
         */
        var submitCheckInfo = function () {
            $ionicLoading.show({
                template: '正在提交...'
            });
            $http.post(originBaseUrl + '/third/behavioRassess/batchSaveStuCredit.htm?', $scope.checkInfo).success(function (data) {
                $ionicLoading.hide();
                $ionicScrollDelegate.scrollTop();
                if (data.status == 0) {
                    $scope.checkSuccess = true;
                } else {
                    ynuiNotification.error({msg: data.message});
                }
            }).error(function () {
                $ionicLoading.hide();
                $ionicScrollDelegate.scrollTop();
                ynuiNotification.error({msg: "提交失败！"});
            });
        };

        //输入原因浮层
        $ionicModal.fromTemplateUrl('show_cause.html',{
            scope:$scope,
            animation:'slide-in-up'
        }).then(function(modal){
            $scope.remarkModal = modal;
        });

        /**
         * 上一步
         */
        $scope.previousStep = function () {
            // 将填写的考核信息放入缓存
            creditStorageService.setItem("credit_checkInfo", $scope.checkInfo);
            $location.path("/selectCheckItem");
        };

        /**
         * 考核其他学生
         */
        $scope.checkOtherStu = function () {
            $scope.selectedStuList = [];
            creditStorageService.setItem("credit_selectedStuList", $scope.selectedStuList);
            $location.path("/search_student").search("fromInternal", true);
        };

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
    }])