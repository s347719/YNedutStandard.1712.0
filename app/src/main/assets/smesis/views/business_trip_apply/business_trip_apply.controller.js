(function () {
    'use strict';

    angular
        .module('myApp')
        .controller('businessTripApplyCtrl', businessTripApplyCtrl);
    businessTripApplyCtrl.$inject = ['$scope', '$ionicModal', '$http', '$ionicPopup', '$rootScope', '$ionicLoading', '$filter', '$timeout'];
    function businessTripApplyCtrl($scope, $ionicModal, $http, $ionicPopup, $rootScope, $ionicLoading, $filter, $timeout) {
        //出差申请
        $ionicModal.fromTemplateUrl('business-trip-content.html', {
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function (modal) {
            $scope.modalOne = modal;
        });
        $scope.openModalOne = function () {
            $scope.modalOne.show();
        };
        $scope.closeModalOne = function () {
            $scope.modalOne.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function () {
            $scope.modalOne.remove();
        });

        //出差申请添加&修改
        $ionicModal.fromTemplateUrl('business-trip-add.html', {
            scope: $scope,
            animation: 'slide-in-right'
        }).then(function (modal) {
            $scope.modalTwo = modal;
        });
        $scope.openModalTwo = function (e) {
            $scope.modalTwo.show();
            e.stopPropagation();
        };
        $scope.closeModalTwo = function () {
            $scope.modalTwo.hide();
        };
        // Cleanup the modal when we're done with it!
        $scope.$on('$destroy', function () {
            $scope.modalTwo.remove();
        });
        /**
         * 自定义时间样式
         * @param dataList
         */
        var entityToShowDataFun = function (dataList) {

            angular.forEach(dataList, function (data) {
                /**
                 * 重写申请时间样式
                 */
                if (data.beginTime.indexOf(" ") != -1) {

                    var applyTimeArr = data.beginTime.split(" "), applyDate = [];
                    if (applyTimeArr[0] && applyTimeArr[0].indexOf("-" != -1)) {
                        applyDate = applyTimeArr[0].split("-");
                        if (applyDate.length == 3) {

                            data.beginShowTime = applyDate[1] + "月" + applyDate[2] + "日";
                        }
                    }
                }
                /**
                 * 重写出差时间
                 */
                if (data.beginTime.indexOf("-") != -1) {
                    data.beginTimeStr = data.beginTime.replace(/-/g, "/");
                }
                if (data.endTime.indexOf("-") != -1) {
                    data.endTimeStr = data.endTime.replace(/-/g, "/");
                }
            });
        }
        /**
         * 初始化方法
         */
            //审批状态码表
        $scope.processScheduleList = ["保存草稿", "审批通过", "返回修改", "审批中"];
        $scope.addSecurityFlag = false;//出差申请流程 权限

        var initFun = function () {
            //分页信息
            $scope.pageNumber = 0;
            $scope.showInfo = false;
            $scope.dataList = [];//数据源
            $scope.editBusiness = {};//初始化编辑参数
        };

        initFun();
        // 判断是否有出差申请流程 权限
        $http.post(basePath + "/third/leaveapply/checkAddSecurity?name=出差申请").success(function (data) {
            $scope.addSecurityFlag = data;
        });
        /**
         * 获取申请数据记录
         */
        $scope.loadData = function () {

            $http.post(basePath + "/third/businessjournal/findXTBGMissionManageByJSTXConditions?pageSize=10&pageNumber=" + $scope.pageNumber).success(function (data) {
                if (data.status == "0") {

                    entityToShowDataFun(data.result);
                    //获取时间
                    $scope.dataList = $scope.dataList.concat(data.result);
                    if (data.result.length == 0) {
                        $scope.showInfo = true;
                        return false;
                    }
                    //下一页
                    $scope.pageNumber += 1;
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                }
            });
        };
        /**
         * 流程相关初始化
         * index 0 添加 1 编辑
         */
        $scope.initActive = function (index) {

            if (index == 0) {
                //根据流程Key获取流程id
                $scope.flowKey = {actKey: "YNBusinessTravel", Id: ""};
                $http.post(basePath + "/third/leaveapply/getXTBGActivityByActKeyForProcessDefinition?actKey=" + $scope.flowKey.actKey).success(function (data) {
                    if (data.status == 0) {
                        // 流程ID
                        $scope.editBusiness.processDefinitionId = data.result.id;

                    }
                });
                //获取申请单号
                $http.post(basePath + "/third/businessjournal/getXTBGMissionManageApplyNumber").success(function (data) {
                    $scope.editBusiness.applyNumber = data.result;
                });
            }

            if (index == 1) {
                if ($scope.editBusiness.actExecutionProcinstId != null) {
                    //根据实例id获取当前节点Id
                    $http.post(basePath + "/third/businessjournal/getXTBGActivityByProInstdForCurTask?processInstanceId=" + $scope.editBusiness.actExecutionProcinstId).success(function (data) {
                        if (data.status == 0) {
                            //当前节点ID
                            $scope.editBusiness.currentTaskId = data.result;
                        }
                    });
                }
            }

        }

        /**
         * 查看
         * @param item
         */
        $scope.clientShowInstructFun = function (item) {

            $scope.showInstruction = item.instruction;
        }
        /**
         * 添加
         */
        $scope.addBusinessTripFun = function () {
            $scope.editBusiness = {
                isAdvanceCost: false,
                beginTime: $filter('date')(new Date, "yyyy-MM-dd HH:mm")
            };
            /**
             * 获取流程定义
             */
            $scope.initActive(0);
        }
        /**
         * 编辑
         */
        $scope.editBusinessTripFun = function (item) {
            $scope.editBusiness = item;
            /**
             * 获取流程定义
             */
            $scope.initActive(1);
        }

        /**
         * 金额格式验证
         * @param cost
         * @param editBusiness
         */
        $scope.checkNumber = function (cost, editBusiness) {

            if (!/^\d*\.?\d{0,2}$/.test(cost) && cost) {
                editBusiness.cost = "";
                return $scope.notifyMsg("金额格式不正确！");
            }
        }
        /**
         * 日期操作(指定日期加上天数)
         * @param date 旧的日期
         * @param days 需要加上的天数
         * @returns {string} 新的日期
         * @constructor
         */
        var AddDays = function (date, days) {
            var nd = new Date(date);
            nd = nd.valueOf();
            nd = nd + days * 24 * 60 * 60 * 1000;
            nd = new Date(nd);
            var y = nd.getFullYear();
            var m = nd.getMonth() + 1;
            var d = nd.getDate();
            if (m <= 9) m = "0" + m;
            if (d <= 9) d = "0" + d;
            var cdate = y + "-" + m + "-" + d;
            return cdate;
        }
        /**
         * 回填日期方法
         * @param time 时间
         * @param status 1开始 2结束
         */
        $scope.timeSelect = function (time,status) {
            $timeout(function () {
                if(status){
                    $scope.beginTime = {
                        minDate: new Date(time)
                    }
                }else{
                    $scope.endTime = {
                        minDate: new Date(time)
                    }
                }

            })
        };
        /**
         * 日期天数加1
         */
        $scope.addOneDay = function () {
            var startDate = $scope.editBusiness.beginTime;
            var str = AddDays(startDate.replace("-", "/").replace("-", "/"), 1);
            str = str + " " + $scope.editBusiness.beginTime.split(" ")[1];
            $scope.editBusiness.endTime = str;
        }
        /**
         * 人员选择器
         */
        $scope.selectUser = function (item) {
            // 下一节点人ID
            $scope.editBusiness.nextTaskUserId = item.id;
            // 下一节点人的名字
            $scope.editBusiness.approvalName = item.name;
        }
        /*取消提示弹框*/
        $scope.cancelTipsFun = function () {
            $ionicPopup.show({
                title: '取消提示',
                template: '<h5> 返回后，当前编辑内容将丢失，确定要返回吗？</h5>',
                buttons: [
                    {
                        text: '继续编辑',
                        type: 'button-outline button-positive button-local'
                    },
                    {
                        text: '确定返回',
                        type: 'button-positive button-local',
                        onTap: function () {
                            $scope.editBusiness = {};//清空数据
                            $scope.closeModalTwo();//关闭模态框
                        }
                    }
                ]
            });
        };
        /**
         * 信息提示框
         * @param msg 需要显示的信息
         */
        $scope.notifyMsg = function (msg) {
            $ionicLoading.show({
                template: msg
            });
            setTimeout(function () {
                $ionicLoading.hide();
            }, 800);
        }
        /**
         * 保存状态
         */
        var num = 0;//默认第一次添加
        $scope.submit = function () {
            /*********表单验证Sta*/
            // 请假类型
            if (!$scope.editBusiness.instruction) {
                $scope.notifyMsg("出差事由不能为空！");
                return;
            }
            // 请假天数
            if (!$scope.editBusiness.beginTime) {
                $scope.notifyMsg("出差时间(开始)不能为空！");
                return;
            }
            // 开始时间
            if (!$scope.editBusiness.endTime) {
                $scope.notifyMsg("出差时间(结束)不能为空！");
                return;
            }

            if ($scope.editBusiness.beginTime > $scope.editBusiness.endTime) {
                $scope.notifyMsg("请假开始时间不能大于结束时间！");
                return;
            }
            // 预支金额为真则 预支，否则不预支
            if ($scope.editBusiness.cost) {
                $scope.editBusiness.isAdvanceCost = true;
            }
            // 下一节点人
            if (!$scope.editBusiness.nextTaskUserId) {
                $scope.notifyMsg("审批人不能为空！");
                return;
            }
            /*********表单验证End*/

            // 发起流程    -------------------------------------  // 返回修改后同意
            var url = "updateXTBGMissionManageBySubmitProcess";
            //提交审批
            if ($scope.editBusiness.processScheduleStates != 2) {
                url = "saveOrUpdateXTBGMissionManageByDraftboxTurnProcess";
            }

            num++;

            if (num <= 1) {
                $http.post(basePath + "/third/businessjournal/" + url, $scope.editBusiness).success(function (data) {
                    if (data.status == 0) {

                        $scope.notifyMsg("提交成功");
                        initFun();//初始化
                        setTimeout(function () {
                            $scope.closeModalTwo();//关闭模态框
                            $scope.loadData();//刷新数据
                            num = 0;
                        }, 500);
                    } else {
                        $scope.notifyMsg(data.message);
                    }
                })
            }

        }
    }
})();
