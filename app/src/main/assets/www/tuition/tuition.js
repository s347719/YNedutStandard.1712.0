/**
 * Created by hejiao on 2017/7/12.
 */
angular
    .module('tuitionApp')
    .factory('ynuiNotification', [function () {
        var ynuiNotification = {};
        var optDefault = {
            "closeButton": false,
            "debug": false,
            "newestOnTop": false,
            "progressBar": false,
            "positionClass": "toast-top-center",
            "preventDuplicates": false,
            "onclick": null,
            "showDuration": "300",
            "hideDuration": "1000",
            "timeOut": "2000",
            "extendedTimeOut": "1000",
            "showEasing": "swing",
            "hideEasing": "linear",
            "showMethod": "fadeIn",
            "hideMethod": "fadeOut"
        };
        toastr.options = optDefault;
        var beforeShow = function (obj) {
            toastr.remove();
            toastr.options = angular.extend({}, optDefault, obj.opts);
        };
        ynuiNotification.warning = function (obj) {
            beforeShow(obj);
            toastr.warning(obj.msg, obj.title);
        };
        ynuiNotification.success = function (obj) {
            beforeShow(obj);
            toastr.success(obj.msg, obj.title);
        };
        ynuiNotification.error = function (obj) {
            beforeShow(obj);
            toastr.error(obj.msg, obj.title);
        };
        ynuiNotification.clear = function () {
            toastr.clear();
        };
        return ynuiNotification;
    }])
    .controller('tuitionPayCtrl', ['$scope', '$http', '$location', '$state', '$ionicModal', '$ionicPopup', "$rootScope", "$window","$ionicLoading","ynuiNotification", function ($scope, $http, $location, $state, $ionicModal, $ionicPopup, $rootScope, $window, $ionicLoading,ynuiNotification) {
        function getCookie(cname) {
            var name = cname + "=";
            var ca = document.cookie.split('; ');
            for(var i=0; i<ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1);
                if (c.indexOf(name) != -1) return decodeURI(c.substring(name.length, c.length));
            }
            return "";
        }
        function setCookie(cname, cvalue, exdays) {
            var d = new Date();
            d.setTime(d.getTime() + (exdays*24*60*60*1000));
            var expires = "expires="+d.toUTCString();
            document.cookie = cname + "=" + encodeURI(cvalue) + "; " + expires + ";path=/";
        }
        var originBaseUrl = '/ynedut';
			if(!!navigator.userAgent.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/)){
                
            }else{
                window.native.disableBackButton();
            }
        /*模态框*/
        /*选择缴费项目*/
        $ionicModal.fromTemplateUrl('pay-project.html', {
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function (modal) {
            $scope.modal1 = modal;
        });
        $scope.currentIndex = -1;
        $scope.projectPayClick = function (order, index) {
            $scope.orderItemList = order.feeInfoList;
            $scope.currentIndex = index;
            $scope.modal1.show();
        };
        //1  我要交费  2 缴费订单
        $scope.tab = getCookie('currentTab')==""?1:getCookie('currentTab');
        //合计缴费
        $scope.tootalPay = 0;
        //合计欠费
        $scope.totaloweAmt = "0.00";
        //网上缴费设置  1 可以缴每学期的欠费   2 必须先缴清历史学期的欠费   3 先缴活动收费学期的费用
        $scope.paymentSetting = 1;
        //错误信息
        $scope.errInfo = "";
        /*选择支付方式ʽ*/
        $ionicModal.fromTemplateUrl('pay-way.html', {
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function (modal) {
            $scope.modal2 = modal;
        });

        $scope.payWayClick = function () {
            if ($scope.tootalPay == 0) {
                ynuiNotification.error({msg: "至少要选择一个本次收费金额大于0的项目！"});
                return;
            }
            $scope.cancelOrder();//取消原来的订单再创建新订单
            //查询学生行政班信息
            $http.get(originBaseUrl + '/onLineOrderController/getStuInfo.htm').success(function (data) {
                if (data.status == 0) {
                    $scope.stuInfo = data.result[0];
                    $scope.stuBaseInfo = data.result[1];
                    $scope.sum = 0;
                    angular.forEach($scope.orderList, function (order) {
                        if (order.isSelected) {
                            $scope.sum += parseFloat(order.currentPay);
                        }
                    });
                    //组装保存数据
                    var confirmOrderInfo = {
                        isOnline: 1,//网上缴费
                        source:2,//手机端
                        time: new Date(),
                        payUserNumber: $scope.stuInfo.userNumber,
                        payPlatformSysUserId: $scope.stuBaseInfo.id,
                        cardType: $scope.stuInfo.credentialType,
                        cardNumber: $scope.stuInfo.credentialNumber,
                        remark: "",
                        payAmt: $scope.sum,
                        orderDetailInfoVOList: []
                    };
                    angular.forEach($scope.orderList, function (value) {
                        if (value.isSelected) {
                            angular.forEach(value.feeInfoList, function (item) {
                                if (item.isSelected) {
                                    var orderDetailInfoVOList = {
                                        xfFeeSpecialtyStandId: item.xfFeeSpecialtyStandardId,
                                        termId: value.termId,
                                        xfFeeSpecialtyItemId: item.xfFeeSpecialtyItemId,
                                        bedRoomType: item.room == null ? "" : item.room.id,//寢室类型
                                        shouldAmt: item.feeAmt,
                                        payAmt: item.oweAmt,
                                        bedRoomStandard: item.room == null ? "" : item.shouldAmt//寝室收费标准
                                    };
                                    confirmOrderInfo.orderDetailInfoVOList.push(orderDetailInfoVOList);
                                }
                            });
                        }
                    });
                    //生成订单
                    $http.post(originBaseUrl + '/onLineOrderController/createPayOrder.htm?', {orderInfoVO: angular.toJson(confirmOrderInfo)}).success(function (data) {
                        if (data.status == 0) {
                            $scope.detailOrder = data.result;
                            $scope.orderId = $scope.detailOrder.id;
                            $scope.modal2.show();
                        }
                    });
                } else {
                    ynuiNotification.error({msg: data.message});
                }
            });
            $scope.modal2.show();
        };

        /**
         * 网上缴费设置
         */
        $scope.getPaymentSetting = function () {
            $http.post(originBaseUrl + '/onLineOrderController/getPaymentSetting.htm').success(function (data) {
                if (data.status == 0) {
                    $scope.paymentSetting = data.result;
                }
            });
        };

        $scope.getPaymentSetting();

        $scope.cancelOrder = function () {
            $http.post(originBaseUrl + '/onLineOrderController/cancelOrders.htm');
        };

        $scope.changeTab = function (tab, isRefresh) {
            $scope.tab = tab;
            if (tab == 1 && isRefresh) {
                $scope.getOrderList();
            }
            if (tab == 2 && isRefresh) {
                $scope.checkOrders();
            }
        };

        /**
         * 查询未交费的订单
         */
        $scope.getOrderList = function () {
            //查询未完成订单
            //查询缴费完成和缴费中的订单
            $http.post(originBaseUrl + '/onLineOrderController/getMyOrderList.htm?isNotPay=true').success(function (data) {
                if (data.status == 0) {
                    $scope.notPayOrder = data.result;
                    $http.post(originBaseUrl + '/onLineOrderController/queryAllXFCollectByStuId.htm').success(function (data) {
                        if (data.status == 0) {
                            $scope.orderList = data.result;
                            if($scope.orderList && $scope.orderList.length>0 && $scope.notPayOrder.length == 0){
                                $scope.errInfo = "";
                                $scope.makeupOrder();
                            }else if($scope.orderList && $scope.orderList.length>0 && $scope.notPayOrder.length > 0){
                                $scope.errInfo = "";
                                $scope.reMakeupOrder();
                            }else{
                                $scope.errInfo = "没有可选学期";
                            }
                        }else{
                            $scope.orderList = [];
                            $scope.errInfo = "加载学期失败，请重试";
                        }
                    });
                }
            });
        };
        $scope.getOrderList();

        /**
         *  提取 查询学生的宿舍的信息
         * @param value
         * @param id
         */
        $scope.queryStandard = function (value, id) {
            /* 如果学生的宿舍信息为空，则使用学生的入住意向的宿舍信息*/
            if(value.peferenceId == null){
                value.peferenceId = $scope.peferenceId;
            }
            angular.forEach(value.dormitoryVOList, function (item) {
                if (item.id == value.peferenceId) {
                    value.room = item;
                    value.roomId=item.id;
                }
            });


            //数据回填 生成随机数的URL，防止相同的请求被拦截
            var url = originBaseUrl + "/onLineOrderController/queryStandardByDormitoryTypeId.htm?dormitoryTypeId=" + id + "&type" + Math.round(Math.random() * 1000) + "=" + new Date().getTime();
            $http.get(url).success(function (data2) {
                if (data2.status == 0) {
                    value.feeInfo = data2.result;
                    angular.forEach(value.feeInfo, function (item) {
                        var typeName = item.feeType == 3 ? "年" : item.feeType == 2 ? "学期" : "月";
                        item.show = (item.feeAmt / 100) + "/" + typeName;
                    });
                    //匹配收费标准
                    var isFind = false;
                    var isHaveYear = false;
                    var isHaveTerm = false;
                    var isHaveMonth = false;
                    var isCanBackOfMonth = false;
                    var isCanBackOfTerm = false;
                    var isCanBackOfYear = false;
                    //当收费区间大于一学期   优先选年    等于一学期   优先选学期   都没有  选月
                    angular.forEach(value.feeInfo, function (item) {
                        if (item.feeType == 1) {
                            //看看这个标准能不能满足上一次的收费标准
                            if(parseFloat(value.feeAmt) * 100 == parseFloat(item.feeAmt) * value.termInfoVO.totalMonths){
                                isCanBackOfMonth = true;
                            }
                            isHaveMonth = true;
                        }
                        if (item.feeType == 2) {
                            if(parseFloat(value.feeAmt)  * 100 == parseFloat(item.feeAmt) * value.termInfoVO.itemsOfTerm){
                                isCanBackOfTerm = true;
                            }
                            isHaveTerm = true;
                        }
                        if (item.feeType == 3) {
                            if(parseFloat(value.feeAmt)  * 100 == parseFloat(item.feeAmt) * value.termInfoVO.years){
                                isCanBackOfYear = true;
                            }
                            isHaveYear = true;
                        }
                    });
                    angular.forEach(value.feeInfo, function (item) {
                        if (!isFind) {
                            //如果这里面有可以回填上上一次交费数据的选项  则按照规则选取
                            if (isCanBackOfMonth || isCanBackOfTerm || isCanBackOfYear) {
                                if (isCanBackOfYear) {
                                    if (item.feeType == 3) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    }
                                }else if(isCanBackOfTerm){
                                    if (item.feeType == 2) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    }
                                }else{
                                    if (item.feeType == 1) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    }
                                }
                            } else {
                                if (value.termInfoVO.itemsOfTerm > 1) {
                                    if (item.feeType == 3 && isHaveYear) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    } else if (item.feeType == 2 && !isHaveYear && isHaveTerm) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    } else if (item.feeType == 1 && !isHaveTerm && !isHaveYear) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    }
                                } else {
                                    if (item.feeType == 2 && isHaveTerm) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    } else if (item.feeType == 3 && !isHaveTerm && isHaveYear) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    } else if (item.feeType == 1 && !isHaveTerm && !isHaveYear) {
                                        value.chooseFeeTypeId = item.id;
                                        isFind = true;
                                    }
                                }
                            }
                        }
                    });
                    if(value.feeInfo == null || value.feeInfo.length == 0){
                        value.feeInfo = [];
                        value.feeInfo.push({show: "0.00", id: "-1"});
                        value.chooseFeeTypeId = "-1";
                    }
                    $scope.changeRoomFeeType(value);
                }
            });
        };

        /**
         * 查询学生入住寝室或入住意向
         */
        $scope.findStudentDormitory = function (value,termId) {
            $http.get(originBaseUrl + "/onLineOrderController/queryStudentDormitory.htm?termId=" + termId + "&str=" + new Date().getTime()).success(function (data) {
                if (data.status == 0) {
                    $scope.dormitoryTypeId = data.result;
                    $scope.peferenceId = data.result;
                    if($scope.dormitoryTypeId){
                        $scope.queryStandard(value, $scope.peferenceId);
                    }
                } else {
                    ynNotification.notify("error", data.message);
                }
            });
        };

        /**
         * 修改收费单位
         * @param specialtyItem
         */
        $scope.changeRoomFeeType = function (specialtyItem) {
            //按照收费标准和收费执行学期进行相应的运算
            specialtyItem.roomFeeAmt = "0.00";
            angular.forEach(specialtyItem.feeInfo, function (item) {
                if (item.id == specialtyItem.chooseFeeTypeId) {
                    //年
                    if (item.feeType == 3)
                        specialtyItem.roomFeeAmt = specialtyItem.termInfoVO.years * item.feeAmt / 100;
                    //学期
                    if (item.feeType == 2)
                        specialtyItem.roomFeeAmt = specialtyItem.termInfoVO.itemsOfTerm * item.feeAmt / 100;
                    //月
                    if (item.feeType == 1)
                        specialtyItem.roomFeeAmt = specialtyItem.termInfoVO.totalMonths * item.feeAmt / 100;
                }
            });
            specialtyItem.roomFeeAmt = parseFloat(specialtyItem.roomFeeAmt).toFixed(2);
            //如果为全免
            if (specialtyItem.exemptType == 2) {
                specialtyItem.exemptAmt = specialtyItem.roomFeeAmt;
            }
            specialtyItem.feeAmt = isNaN(parseFloat(specialtyItem.roomFeeAmt))?"0.00":parseFloat(specialtyItem.roomFeeAmt).toFixed(2);
            specialtyItem.shouldAmt = specialtyItem.dormitoryIsConfirm?specialtyItem.shouldAmt:specialtyItem.roomFeeAmt - specialtyItem.exemptAmt > 0 ? (specialtyItem.roomFeeAmt - specialtyItem.exemptAmt).toFixed(2) : "0.00";
            specialtyItem.oweAmt = specialtyItem.shouldAmt - specialtyItem.payAmt > 0 ? (specialtyItem.shouldAmt - specialtyItem.payAmt).toFixed(2) : "0.00";
            if (specialtyItem.shouldAmt > 0) {
                specialtyItem.isSelected = true;//默认选中所有缴费项目
                specialtyItem.couldChoose = true;
            }
            $scope.calcPay();
        };

        /**
         * 当没有订单的时候，直接勾选所有的欠费项
         * 已缴清的勾选还有点问题
         */
        $scope.makeupOrder = function () {
            var errIndex = -1;
            $scope.tootalPay = 0;
            angular.forEach($scope.orderList, function (item, index) {
                if (item.errorInfo == null || item.errorInfo == "") {
                    item.shouldAmt = 0;//应缴
                    item.currentPay = 0;//本次缴费
                    item.ownAmt = 0;//欠费
                    item.totalItem = 0;
                    item.index = index;//学期顺序，用于判断都选
                    item.isSelected = true;//默认选中所有缴费学期
                    item.couldChoose = true;//默认可以勾选
                    angular.forEach(item.feeInfoList, function (value) {
                        item.shouldAmt += parseFloat(value.shouldAmt);//应交的学费
                        if (value.itemType == "2" && value.dormitoryVOList != null && value.dormitoryVOList.length > 0) {
                            if (value.peferenceId) {//如果以前收过住宿费，说明住过宿
                                $scope.queryStandard(value, value.peferenceId);
                            }else{//以前没有住宿，查询住宿意向
                                value.feeInfo = [];
                                $scope.findStudentDormitory(value,item.termId);
                                value.feeInfo.push({show: "0.00", id: "-1"});
                                angular.forEach(item.feeInfoList, function (item1) {
                                    if (item1.itemType == 2) {
                                        item1.chooseFeeTypeId = "-1";
                                    }
                                });
                                $scope.changeRoomFeeType(value);
                                $scope.queryStandard(value, "-1");
                            }
                        }
                        value.shouldAmt = parseFloat(value.oweAmt);
                        item.currentPay += parseFloat(value.shouldAmt);
                        $scope.tootalPay += parseFloat(value.shouldAmt);
                        if (!item.couldChoose || value.shouldAmt == 0) {
                            //当学期不允许勾选或者已结清费用的时候，收费项目也不允许勾选
                            value.isSelected = false;
                            value.couldChoose = false;
                        } else {
                            value.isSelected = true;//默认选中所有缴费项目
                            value.couldChoose = true;
                            item.totalItem += 1;
                        }
                    });
                    if (item.currentPay == 0 && item.ownAmt == 0) {
                        item.isSelected = false;//没有缴费也没有欠费，就不能勾选
                        item.couldChoose = false;
                    }
                    item.currentPay = parseFloat(item.currentPay).toFixed(2);
                    item.ownAmt = parseFloat(item.ownAmt).toFixed(2);
                    item.shouldAmt = parseFloat(item.shouldAmt).toFixed(2);
                }else{
                    $scope.orderExceptInfo = item.errorInfo;
                    errIndex = index;
                }
            });
            if (errIndex != -1){
                $scope.orderList.splice(errIndex,1);
                if ($scope.orderList.length==0){
                    $scope.errInfo = "没有可选学期";
                }
            }
            $scope.tootalPay = parseFloat($scope.tootalPay).toFixed(2);
        };

        /**
         * 计算本次缴费金额
         */
        $scope.calcPay = function () {
            //合计缴费
            $scope.tootalPay = 0;
            //合计欠费
            $scope.totaloweAmt = 0;
            angular.forEach($scope.orderList, function (item) {
                item.currentPay = 0;//本次缴费
                item.ownAmt = 0;//欠费
                item.totalItem = 0;
                item.shouldAmt = 0;//应交的学费
                angular.forEach(item.feeInfoList, function (value) {
                    item.shouldAmt += parseFloat(value.shouldAmt);//应交的学费
                    if (value.isSelected) {
                        item.totalItem += 1;//勾选项目个数+1
                        item.currentPay += parseFloat(value.shouldAmt);//勾选了的应收金额
                    } else {
                        item.ownAmt += parseFloat(value.shouldAmt);//没有勾选的欠费金额
                    }
                });
                //判断学期是否勾选
                if (item.isSelected) {
                    $scope.tootalPay += item.currentPay;
                    $scope.totaloweAmt += item.ownAmt;
                } else {
                    //没有勾选学期的计算出没有勾选的欠费
                    $scope.totaloweAmt += (item.currentPay + item.ownAmt);
                }
                item.currentPay = parseFloat(item.currentPay).toFixed(2);
                item.ownAmt = parseFloat(item.ownAmt).toFixed(2);
                item.shouldAmt = parseFloat(item.shouldAmt).toFixed(2);
            });
            $scope.tootalPay = parseFloat($scope.tootalPay).toFixed(2);
            $scope.totaloweAmt = parseFloat($scope.totaloweAmt).toFixed(2);
        };

        /**
         * 当有订单的时候，勾选上个订单的缴费项
         */
        $scope.reMakeupOrder = function () {
            $scope.tootalPay = 0;
            angular.forEach($scope.orderList, function (item, index) {
                item.isSelected = false;//默认选中所有缴费学期
                item.currentPay = 0;//本次缴费
                item.ownAmt = 0;//欠费
                item.totalItem = 0;//缴费项目数
                item.index = index;//学期顺序，用于判断都选
                item.shouldAmt = 0;//应缴
                item.couldChoose = true;//默认可以勾选
                if (item.errorInfo){
                    $scope.errorInfo.push(item.errorInfo);
                    item.couldChoose =false;
                }
                angular.forEach(item.feeInfoList, function (value) {
                    item.shouldAmt += parseFloat(value.shouldAmt);//应交的学费
                    value.isSelected = false;
                    value.shouldAmt = parseFloat(value.oweAmt);
                    $scope.tootalPay += parseFloat(value.shouldAmt);
                    value.couldChoose = true;
                    if(value.shouldAmt<=0){
                        value.couldChoose = false;
                    }
                });
                angular.forEach($scope.notPayOrder[0].payOrderVOList,function (notPay, notIndex) {
                    if (item.termId == notPay.termId){
                        item.isSelected = true;//默认选中所有缴费学期
                        angular.forEach(item.feeInfoList, function (value) {
                            angular.forEach(notPay.feeInfoList,function (notValue) {
                                if (value.itemCode == notValue.itemCode){
                                    item.currentPay += parseFloat(value.shouldAmt);
                                    if (!item.couldChoose || value.shouldAmt == 0) {
                                        //当学期不允许勾选或者已结清费用的时候，收费项目也不允许勾选
                                        value.isSelected = false;
                                        value.couldChoose = false;
                                    } else {
                                        value.isSelected = true;//选中所有缴费项目
                                        item.totalItem += 1;
                                    }
                                }
                            });
                        });
                    }
                });
                if (item.currentPay == 0 && item.ownAmt == 0) {
                    item.isSelected = false;//没有缴费也没有欠费，就不能勾选
                    item.couldChoose = false;
                }
                item.ownAmt = parseFloat(item.ownAmt).toFixed(2);
                item.shouldAmt = parseFloat(item.shouldAmt).toFixed(2);
            });
            $scope.tootalPay = parseFloat($scope.tootalPay).toFixed(2);
            $scope.calcPay();
        };

        /**
         * 查询缴费完成和缴费中的订单
         * 查看订单列表
         */
        $scope.checkOrders = function () {
            //查询缴费完成和缴费中的订单
            $http.post(originBaseUrl + '/onLineOrderController/getMyOrderList.htm?isNotPay=false').success(function (data) {
                if (data.status == 0) {
                    $scope.confirmOrder = data.result;
                    if($scope.confirmOrder.length>0  && $scope.orderList.length>0){
                        $scope.errInfo = "";
                        angular.forEach($scope.confirmOrder, function (termInfo) {
                            termInfo.detail = false;
                            angular.forEach(termInfo.payOrderVOList, function (order) {
                                order.currentPayAmt = 0;
                                angular.forEach(order.feeInfoList, function (item) {
                                    order.currentPayAmt += parseFloat(item.currentPayAmt);
                                });
                                order.currentPayAmt = parseFloat(order.currentPayAmt).toFixed(2);
                            });
                        });
                    }else{
                        $scope.confirmOrder = [];
                        $scope.errInfo = "没有缴费订单";
                    }
                }else{
                    $scope.errInfo = "加载订单失败，请重试";
                }
            });
        };

        /*弹出框*/
        $scope.showConfirm = function () {
            $ionicPopup.confirm({
                template: '<h4 class="text-center">提示</h4>'+
                '<h4 class="margin-top-10">请确认是否已完成付款</h4>',
                buttons: [
                    {
                        text: '付款失败',
                        type: 'button-default',
                        onTap: function () {
                            // $scope.changeTab(1, false);
                            $scope.modal2.hide();
                            ynuiNotification.error({msg: "付款失败，请重新进行缴费"});
                        }
                    },
                    {
                        text: '已完成付款',
                        type: 'button-positive button-more-space',
                        onTap: function () {
                            $scope.modal2.hide();
                            $ionicLoading.show();
                            $scope.success();
                            // $scope.changeTab(2, true);
                        }
                    }
                ]
            });
            if ($scope.orderStatus == 7) {
                $scope.check();
            }
        };


        $scope.reload = function (index) {
            if (index == 1) {
                setCookie('currentTab',index);
                $scope.getOrderList();
            }else{
                setCookie('currentTab',index);
                window.reload();
            }
        };

        // 判定是否是支付回调
        var localUrl = decodeURI($window.location.href);
        $scope.params = localUrl.split(/[?&]/);

        $scope.numberToThird = $scope.params[1];
        if($scope.numberToThird){
            $scope.numberToThird = $scope.numberToThird.replace("numberToThird=", "");
        }
        if($scope.numberToThird){
            $scope.reload();
            $scope.showConfirm();
            if(navigator.userAgent.indexOf("iPhone") > -1 && navigator.userAgent.indexOf("Safari") > -1){
                window.location.href = "MessengerForYNedut://1";
            }
        }

        /**
         *  1 可以缴每学期的欠费   2 必须先缴清历史学期的欠费   3 先缴活动收费学期的费用
         *  选中学期
         *  $scope.paymentSetting
         */
        $scope.chooseTerm = function (index) {
            $scope.currentOrder = $scope.orderList[index];
            if (!$scope.currentOrder.couldChoose) {
                return;
            }
            if ($scope.paymentSetting == 1) {//可以缴每学期的欠费
                $scope.currentOrder.isSelected = !$scope.currentOrder.isSelected;
            }
            if ($scope.paymentSetting == 2) {//必须先缴清历史学期的欠费
                $scope.currentOrder.isSelected = !$scope.currentOrder.isSelected;
                angular.forEach($scope.orderList, function (order, ind) {
                    if (ind > index && order.couldChoose && order.isSelected) {//如果是取消勾选学期，也取消后面的学期
                        order.isSelected = !order.isSelected;
                    }
                    if (ind < index) {//如果是勾选学期，那么将历史学期所有收费项勾选上
                        if (order.couldChoose) {
                            if (!order.isSelected) {
                                order.isSelected = !order.isSelected;
                            }
                            angular.forEach(order.feeInfoList, function (item) {
                                $scope.choosePayItem(item, 2);
                            });
                        }
                    }
                });
            }
            if ($scope.paymentSetting == 3) {//先缴活动收费学期的费用
                if (($scope.currentOrder.isLifeTerm && !$scope.currentOrder.isSelected) || (!$scope.currentOrder.isLifeTerm && $scope.currentOrder.isSelected)) {
                    $scope.currentOrder.isSelected = !$scope.currentOrder.isSelected;//勾选活动学期或者取消勾选非活动学期
                }
                else if ($scope.currentOrder.isLifeTerm && $scope.currentOrder.isSelected) {//取消勾选活动学期
                    angular.forEach($scope.orderList, function (order, ind) {
                        if (order.couldChoose && order.isSelected) {
                            order.isSelected = !order.isSelected;
                        }
                    });
                }
                else if (!$scope.currentOrder.isLifeTerm && !$scope.currentOrder.isSelected) {//勾选非活动学期
                    angular.forEach($scope.orderList, function (order, ind) {
                        if (order.isLifeTerm && order.couldChoose && ind != index) {
                            order.isSelected = true;
                            angular.forEach(order.feeInfoList, function (item) {
                                $scope.choosePayItem(item, 2);
                            });
                        }
                    });
                    $scope.currentOrder.isSelected = !$scope.currentOrder.isSelected;
                }
            }
            $scope.calcPay();
        };

        /**
         * 选中收费项目
         * @param item 项目
         * @param isChecked  1，正常情况勾选，2，只能选中 3，只能取消
         */
        $scope.choosePayItem = function (item, isChecked) {
            if (!item.couldChoose) {
                return;
            }
            if (isChecked == 1) {
                item.isSelected = !item.isSelected;
            } else if (isChecked == 2 && !item.isSelected) {
                item.isSelected = !item.isSelected;
            } else if (isChecked == 3 && item.isSelected) {
                item.isSelected = !item.isSelected;
            }
            $scope.calcPay();
        };

        /**
         * 选中收费项目
         * @param item 项目
         * @param isChecked  1，正常情况勾选，2，只能选中 3，只能取消
         */
        $scope.clickPayItem = function (item) {
            if (item.oweAmt ==0 ){
                ynuiNotification.success({msg:"该项费用已结清"});
                return
            }
            if ($scope.paymentSetting == 1) {//可以缴每学期的欠费
                $scope.choosePayItem(item, 1);
            }
            if ($scope.paymentSetting == 2) {//必须先缴清历史学期的欠费
                $scope.currentTerm = $scope.orderList[$scope.currentIndex];//判断当前学期勾选没有，如果没有勾选，那么收费项的改变不影响其他的
                if ($scope.currentTerm.isSelected) {
                    angular.forEach($scope.orderList, function (order, ind) {//当前学期之后的学期
                        if (ind < $scope.currentIndex && order.couldChoose && order.isSelected) {//如果是取消勾选，那么后面的学期也取消勾选
                            order.isSelected = !order.isSelected;
                        }
                        if (ind > $scope.currentIndex && order.couldChoose && !order.isSelected) {//如果是勾选，历史学期也勾选上
                            order.isSelected = true;//当前学期之前的学期
                            angular.forEach(order.feeInfoList, function (item) {
                                $scope.choosePayItem(item, 2);
                            });
                        }
                    });
                }
                $scope.choosePayItem(item, 1);
            }
            if ($scope.paymentSetting == 3) {//先缴活动收费学期的费用
                $scope.currentTerm = $scope.orderList[$scope.currentIndex];//判断当前学期勾选没有，如果没有勾选，那么收费项的改变不影响其他的
                if (($scope.currentTerm.isLifeTerm && !item.isSelected) || (!$scope.currentTerm.isLifeTerm && item.isSelected) || !$scope.currentTerm.isSelected) {//勾选活动学期，取消非活动学期，对其他收费项不影响
                    $scope.choosePayItem(item, 1);
                } else if ($scope.currentTerm.isLifeTerm && item.isSelected && $scope.currentTerm.isSelected) {//取消勾选活动学期的收费项，将非活动学期的勾选取消
                    $scope.choosePayItem(item, 1);
                    angular.forEach($scope.orderList, function (order) {
                        if (!order.isLifeTerm && order.isSelected && order.couldChoose) {
                            order.isSelected = !order.isSelected;
                        }
                    });
                } else if (!$scope.currentTerm.isLifeTerm && !item.isSelected && $scope.currentTerm.isSelected) {//勾选非活动学期，非活动学期勾选一项，活动学期及其所有的收费项都勾选上
                    $scope.choosePayItem(item, 1);
                    angular.forEach($scope.orderList, function (order) {
                        if (order.isLifeTerm && order.couldChoose) {
                            order.isSelected = true;
                            angular.forEach(order.feeInfoList, function (item) {
                                $scope.choosePayItem(item, 2);
                            });
                        }
                    });
                }
            }
            $scope.calcPay();
        };

        $scope.showDetail = function (order) {
            order.detail = !order.detail;
        };


        $scope.hideItem = function () {
            $scope.modal1.hide();
        };

        /*弹出框*/
        $scope.showPopup = function () {
            $ionicPopup.confirm({
                template: '<div class="text-center"><img src="../img/error.png" alt=""></div><h4 class="text-center">缴费失败</h4>'+
                '<span class="help-block text-left">1. 请在支付宝或微信APP中查看帐单状态是否为支付成功。</span>' +
                '<span class="help-block text-left">2. 如果确实已经付款，那可能由于网络原因，无法及时获取付款信息，请尝试刷新看看。</span>' +
                '<span class="help-block text-left">3. 还可以咨询班主任核实付款情况。</span>',
                buttons: [
                    {
                        text: '重新付款',
                        type: 'button-default',
                        onTap: function () {
                            $scope.modal2.hide();
                            $scope.changeTab(1, false);
                        }
                    },
                    {
                        text: '刷新',
                        type: 'button-positive',
                        onTap: function () {
                            $scope.modal2.hide();
                            $ionicLoading.show();
                            $scope.success();
                        }
                    }
                ]
            });
        };


        /**
         * 支付方式配置
         */
        $scope.getThirdChargeConfig = function () {
            $http.get(originBaseUrl + '/onLineOrderController/queryAllConfig.htm').success(function (data) {
                if (data.status == 0) {
                    $scope.xfThirdChargeRespVO = data.result.xfThirdChargeConfigVOList;
                    angular.forEach($scope.xfThirdChargeRespVO,function (item) {
                        if(item.id ==2){
                            //支付宝支付是否开启
                            $scope.zhifubao = item.status;
                            $scope.payWay = 1;
                        }if(item.id ==6){
                            //微信H5支付是否开启
                            $scope.weixin = item.status;
                            if ($scope.zhifubao !=1){
                                $scope.payWay = 2;
                            }
                        }
                    });
                    if($scope.zhifubao!=1 && $scope.weixin!=1){
                        $scope.isConfirm = true;
                    }
                } else {
                    ynuiNotification.error({msg: data.message});
                }
            }).error(function (data) {
                ynuiNotification.error({msg: data.message});
            })
        };
        $scope.getThirdChargeConfig();

        /**
         * 取消缴费项目
         */
        $scope.cancleChoose = function () {
            $scope.modal1.hide();
        };

        /**
         * 取消支付
         */
        $scope.canclePay = function () {
            $scope.modal2.hide();
        };

        $scope.weixinPay = function () {
            if(!!navigator.userAgent.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/)){
                window.webkit.messageHandlers.getMyIp.postMessage(null);
            }else{
                window.native.getMyIp();
            }
        };

        window.payByWx = function(ip){
            if(ip){
                $http.get(originBaseUrl + '/onLineOrderController/payOnlineByWx.htm?orderId=' + $scope.orderId + "&ip=" + ip).success(function (data) {
                    if (data.status == 0) {
                        window.location.href=data.result;
                    }else{
                        ynuiNotification.error({msg: data.message});
                    }
                });
            }else{
                var confirmPopup = $ionicPopup.confirm({
                    title:"提示",
                    template: '请检查网络是否存在异常',
                    scope: $scope,
                    okText:"确定"
                });
            }
        };

        //支付成功回调方法  等app组件支付成功时调用  轮询获取异步请求真实付款状态
        $scope.success = function(){
            $scope.isContinue = true;
            //轮询方法
            var loop = function(){
                if($scope.isContinue){
                    $.ajax({
                        url:originBaseUrl + "/onLineOrderController/getOrderIsPaySuccess.htm?currentTime="+new Date().getTime(),
                        type:"get",
                        async:false,
                        dataType:"json",
                        data: {thirdInfoId : $scope.numberToThird},
                        success:function(data){
                            if(data.status == 0 && data.result){
                                //在得到正常的返回结果后  代表支付成功   跳转到支付成功页面
                                $scope.isContinue = false;
                                $scope.changeTab(2,true);
                            }
                        }
                    });
                }else{
                    return false;
                }
                $scope.$apply();
            };
            var clearLoop = function () {
                window.clearInterval(interval);
                if($scope.isContinue){
                    $scope.showPopup();
                }
                $ionicLoading.hide();
            };
            var interval = window.setInterval(loop, 500);
            window.setTimeout(function(){
                clearLoop();
            }, 3000);
        }
        ;
        /**
         * 支付宝支付
         */
        $scope.aliPay = function () {
            $http.get(originBaseUrl + '/onLineOrderController/payOnlineByAli.htm?orderId='+$scope.orderId).success(function(data){
                if(data.status == 0){
                    angular.element(data.result).appendTo(angular.element('body'));
                    //var option = {
                    //    orderString : data.result[0]
                    //};
                    //var fail = function(){
                    //    //暂时没有失败的交互
                    //};
                    //yn.plugins.pay.alipay(option, $scope.success, fail);
                }else{
                    ynuiNotification.error({msg: data.message});
                }
            });
            // if("成功"){
            //    $scope.changeTab(2,true);
            // }
        };

        $scope.show = function() {
            $ionicLoading.show({
                template: '<ion-spinner></ion-spinner>'
            });
        };
        $scope.hide = function(){
            $ionicLoading.hide();
        };

        $scope.reloadOrder = function () {
            $scope.checkOrders();
        };

    }]);
