/**
 * Created by YN on 2016/5/11.
 */
angular.module('enrollByOnline', ['ionic', 'ksSwiper','staticModule'])

    .run(function ($ionicPlatform) {
        $ionicPlatform.ready(function () {
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
            }
            if (window.StatusBar) {
                StatusBar.styleDefault();
            }
        })
    })
    .config(function ($httpProvider) {
        $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded';
        $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
        var param = function (obj) {
            var query = "", name, value, fullSubName, subName, subValue, innerObj, i;
            for (name in obj) {
                value = obj[name];
                if (value instanceof Array) {
                    for (i = 0; i < value.length; ++i) {
                        subValue = value[i];
                        fullSubName = name;
                        innerObj = {};
                        innerObj[fullSubName] = subValue;
                        query += param(innerObj) + '&';
                    }
                } else if (value instanceof Object) {
                    for (subName in value) {
                        subValue = value[subName];
                        fullSubName = name + "[" + subName + "]";
                        innerObj = {};
                        innerObj[fullSubName] = subValue;
                        query += param(innerObj) + "&";
                    }
                } else if (value !== undefined && value !== null) {
                    query += encodeURIComponent(name) + "=" + encodeURIComponent(value) + "&";
                }
            }
            return query.length ? query.substr(0, query.length - 1) : query;
        };
        $httpProvider.defaults.transformRequest = [function (data) {
            return angular.isObject(data) && String(data) !== "[object File]" ? param(data) : data;
        }];
    })//onFinishRender
    .directive('onFinishRender', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                if (scope.$last === true) {
                    $timeout(function () {
                        scope.$emit('ngRepeatFinished');
                    });
                }
            }
        }
    })
    .directive('bindFile', [function () {
        return {
            require: "ngModel",
            restrict: 'A',
            link: function ($scope, el, attrs, ngModel) {
                el.unbind("change");
                el.bind('change', function (event) {
                    ngModel.$setViewValue(event.target.files[0]);
                    $scope.$apply();
                });
                $scope.$watch(function () {
                    return ngModel.$viewValue;
                }, function (value) {
                    if (!value) {
                        el.val("");
                    }
                });
            }
        };
    }])
    //ynuiNotification fanjiaben
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
    }]).controller('enrollByOnlineCtrl', ['$scope', '$http', '$timeout', '$ionicLoading', '$ionicScrollDelegate','ynuiNotification',"$location","$timeout","$ionicModal",
        function ($scope, $http, $timeout, $ionicLoading, $ionicScrollDelegate,ynuiNotification,$location,$timeout,$ionicModal) {
            /*模态*/
            $ionicModal.fromTemplateUrl('template.html',{
                scope:$scope,
                animation:'slide-in-up'
            }).then(function(modal){
                $scope.modal1 = modal;
            });
            $scope.bottomModalClick = function() {
                $scope.modal1.show();
            };
            $scope.closeModal = function() {
                $scope.modal1.hide();
            };

            $scope.isShow = false;
            $scope._isShow = false;
            $scope.toggle = function () {
                $scope.isShow = !$scope.isShow;
                if(!$scope.isShow){
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                }
            }
            $scope.userType ="1";
            $scope.search = {userType:"1",searchText:""};
            $scope.queryUserInfo =  function(){
                if($scope.search.searchText){
                    $http.post(basePath + "/enrollByOnline/queryInnerPresenterPageByType.htm",{type:$scope.search.userType,name:$scope.search.searchText}).success(function(data){
                        if (data.status == 0) {
                            $scope.userItems = data.result.content;
                        }
                    });
                }
            }
            $scope.$watch("search.userType",function(){
                $scope.userItems = [];
                $scope.search.searchText = null;
            });
            $scope.resetInnerPresenter = function(event){
                $scope.innerPresenterName = null;
                $scope.studentInfo.zsAdmissionInfoVO.innerPresenter = null;
            }
            $scope.chooseInnerPresenter = function(userItem){
                $scope.innerPresenterName = userItem.name;
                $scope.studentInfo.zsAdmissionInfoVO.innerPresenter = userItem.id;
                $scope.closeModal();
            }

            /**
             * 查询“入学须知”详情
             */
            $http.post("/ynedut/admitResultQuery/queryAdmissionNotice.htm").success(function(data){
                if(data.status == 0){
                    $scope.noticeObj = data.result;
                }
            });
            $scope.toggleGroup = function (item) {
                item._isShow = !item._isShow;
            }
            $scope.initData = function(){
                $scope.resetInnerPresenter();
                $scope.initStuInfo();
                $scope.initProObj();
                $scope.initFileInfo();
                $scope.doRefresh();
            };
            $scope.initStuInfo = function(){
                $scope.studentInfo = {
                    stuBaseInfoVO:{
                        birthType:"0",
                        nation:"01",
                        homeTownProvinceVO:{}
                    },
                    stuArchivesInfoVO:{
                        recruitType : "0"
                    },
                    stuSubsidizeInfoVO:{},
                    stuContactWayInfoVO:{},
                    stuAttachmentInfoVOList:[],
                    stuFamilyInfoVO:{},
                    zsAdmissionInfoVO:{},
                    zsUserInfoVO:{credentialType:"01"},
                    stuAttmentVO:{},
                    stuFamilyMemberVOList:[],
                    learnExperienceVOList:[],
                    workExperienceVOList:[]
                };

            }
            $scope.beginTimeSelect = function(index){
                $timeout(function(){
                    $scope.timeSettingsList[index].endTimeSettings = {
                        dateOrder: 'yyyymm',
                        dateFormat: 'yyyy-mm',
                        minDate: new Date($scope.studentInfo.learnExperienceVOList[index].beginTimeString)
                    }
                })
            };
            $scope.endTimeSelect = function(index){
                $timeout(function(){
                    $scope.timeSettingsList[index].beginTimeSettings ={
                        dateOrder: 'yyyymm',
                        dateFormat: 'yyyy-mm',
                        maxDate: new Date($scope.studentInfo.learnExperienceVOList[index].endTimeString)
                    }
                })
            };
            //下拉表中字典对象
            $scope.dictInfo = {};
            //层次
            $scope.levelInfo = {};
            //获取生源地
            $scope.sourcePlace = {};
            $scope.initStuInfo();
            $scope.timeSettingsList = [];
            //清除出生日期
            $scope.clearDateOfBirth = function(){
                $scope.studentInfo.stuBaseInfoVO.dateOfBirth = "";
            }
            //添加学习经历
            $scope.addLearnExperience = function(){
                $scope.timeSettingsList.push({
                    beginTimeSettings:{
                        dateOrder: 'yyyymm',
                        dateFormat: 'yyyy-mm'
                    },
                    endTimeSettings : {
                        dateOrder: 'yyyymm',
                        dateFormat: 'yyyy-mm'
                    }
                });
                $scope.studentInfo.learnExperienceVOList.push({beginTimeString:'',endTimeString:'',attendSchool:'',reterence:'',_isShow:true});
            };
            //删除学习经历
            $scope.delLearnExperience = function(index){
                $scope.timeSettingsList.splice(index,1);;
                $scope.studentInfo.learnExperienceVOList.splice(index,1);;
            };
            $scope.isSetData = false;
            //籍贯下拉级联对象
            //省，市，县
            $scope.proListObj = {
                proList:[],
                cityList:[],
                townList:[]
            };

            //户口所在地下拉级联对象
            //省，市，县
            $scope.homeProListObj = {
                proList:[],
                cityList:[],
                townList:[]
            };

            $scope.initProObj = function(){
                $scope.proObj = {
                    pro:{id:'',name:''},
                    city:{id:'',name:''},
                    town:{id:'',name:''}
                };
                $scope.homeProObj = {
                    pro:{id:'',name:''},
                    city:{id:'',name:''},
                    town:{id:'',name:''}
                };
            }
            $scope.initProObj();

            //身份证号验证
            var vcity={ 11:"北京",12:"天津",13:"河北",14:"山西",15:"内蒙古",
                21:"辽宁",22:"吉林",23:"黑龙江",31:"上海",32:"江苏",
                33:"浙江",34:"安徽",35:"福建",36:"江西",37:"山东",41:"河南",
                42:"湖北",43:"湖南",44:"广东",45:"广西",46:"海南",50:"重庆",
                51:"四川",52:"贵州",53:"云南",54:"西藏",61:"陕西",62:"甘肃",
                63:"青海",64:"宁夏",65:"新疆",71:"台湾",81:"香港",82:"澳门",91:"国外"
            };

            //检查号码是否符合规范，包括长度，类型
            isCardNo = function(obj)
            {
                //身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X
                var reg = /(^\d{15}$)|(^\d{17}(\d|X)$)/;
                if(reg.test(obj) === false)
                {
                    return false;
                }

                return true;
            };

            //取身份证前两位,校验省份
            checkProvince = function(obj)
            {
                var province = obj.substr(0,2);
                if(vcity[province] == undefined)
                {
                    return false;
                }
                return true;
            };
            //校验日期
            verifyBirthday = function(year,month,day,birthday)
            {
                var now = new Date();
                var now_year = now.getFullYear();
                //年月日是否合理
                if(birthday.getFullYear() == year && (birthday.getMonth() + 1) == month && birthday.getDate() == day)
                {
                    //判断年份的范围（3岁到100岁之间)
                    var time = now_year - year;
                    if(time >= 0 && time <= 130)
                    {
                        return true;
                    }
                    return false;
                }
                return false;
            };
            //检查生日是否正确
            checkBirthday = function(obj)
            {
                var len = obj.length;
                //身份证15位时，次序为省（3位）市（3位）年（2位）月（2位）日（2位）校验位（3位），皆为数字
                if(len == '15')
                {
                    var re_fifteen = /^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/;
                    var arr_data = obj.match(re_fifteen);
                    var year = arr_data[2];
                    var month = arr_data[3];
                    var day = arr_data[4];
                    var birthday = new Date('19'+year+'/'+month+'/'+day);
                    $scope.birthday = year+'-'+month+'-'+day;
                    return verifyBirthday('19'+year,month,day,birthday);
                }


                //身份证18位时，次序为省（3位）市（3位）年（4位）月（2位）日（2位）校验位（4位），校验位末尾可能为X
                if(len == '18')
                {
                    var re_eighteen = /^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/;
                    var arr_data = obj.match(re_eighteen);
                    var year = arr_data[2];
                    var month = arr_data[3];
                    var day = arr_data[4];
                    var birthday = new Date(year+'/'+month+'/'+day);
                    $scope.birthday = year+'-'+month+'-'+day;
                    return verifyBirthday(year,month,day,birthday);
                }
                return false;
            };

            changeFivteenToEighteen = function(obj)
            {
                if(obj.length == '15')
                {
                    var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
                    var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
                    var cardTemp = 0, i;
                    obj = obj.substr(0, 6) + '19' + obj.substr(6, obj.length - 6);
                    for(i = 0; i < 17; i ++)
                    {
                        cardTemp += obj.substr(i, 1) * arrInt[i];
                    }
                    obj += arrCh[cardTemp % 11];
                    return obj;
                }
                return obj;
            };

            //校验位的检测
            checkParity = function(obj)
            {
                //15位转18位
                obj = changeFivteenToEighteen(obj);
                var len = obj.length;
                if(len == '18')
                {
                    var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
                    var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
                    var cardTemp = 0, i, valnum;
                    for(i = 0; i < 17; i ++)
                    {
                        cardTemp += obj.substr(i, 1) * arrInt[i];
                    }
                    valnum = arrCh[cardTemp % 11];
                    if (valnum == obj.substr(17, 1))
                    {
                        return true;
                    }
                    return false;
                }
                return false;
            };
            $scope.checkCard = function(obj)
            {
                //校验长度，类型
                if(isCardNo(obj) === false)
                {
                    return false;
                }
                //检查省份
                if(checkProvince(obj) === false)
                {
                    return false;
                }
                //校验生日
                if(checkBirthday(obj) === false)
                {

                    return false;
                }
                return true;
            };
            $scope.scCard = function() {
                $scope.credentialNumber = $scope.studentInfo.zsUserInfoVO.credentialNumber;
                if ( $scope.credentialNumber != 0) {
                    if ($scope.checkCard( $scope.credentialNumber)) {
                        $scope.studentInfo.stuBaseInfoVO.dateOfBirth = $scope.birthday;
                        var len = $scope.studentInfo.zsUserInfoVO.credentialNumber.length;
                        if(len == 18){
                            var sexStatus =   $scope.studentInfo.zsUserInfoVO.credentialNumber.substring(16,17);
                            if(sexStatus%2 == 1){
                                $scope.studentInfo.zsUserInfoVO.gender = "1";

                            }else{
                                $scope.studentInfo.zsUserInfoVO.gender = "2";
                            }
                        }
                        else if(len == 15){
                            var sexStatus =   $scope.studentInfo.zsUserInfoVO.credentialNumber.substring(14,15);
                            if(sexStatus%2 == 1){
                                $scope.studentInfo.zsUserInfoVO.gender = "1";

                            }else{
                                $scope.studentInfo.zsUserInfoVO.gender = "2";
                            }
                        }
                    }
                }
            };

            $scope.$watch( 'studentInfo.zsUserInfoVO.credentialNumber',  $scope.scCard );

            /**
             * 获取网上报名设置数据（取年级和招生类型）
             */
            $scope.getOnlineSetData = function(func){
                //获取网上报名设置数据（取年级和招生类型）
                $http.get("/ynedut/enrollByOnline/queryValidRegSetting.htm").success(function(data){
                    if(data.status == 0){
                        $scope.onlineSetData = data.result;
                        if($scope.onlineSetData != null){
                            //报名设置有效年级
                            $scope.studentInfo.stuArchivesInfoVO.platformSysGradeId = $scope.onlineSetData.platformSysGradeId;
                            //报名设置有效招生类型
                            $scope.studentInfo.stuArchivesInfoVO.enrollType = $scope.onlineSetData.enrollType;
                            $scope.isSetData = true;
                            $scope.getSelectData();
                        }else{
                            $scope.isSetData = false;
                        }
                    }
                    $scope.emptyInfo = false;
                    $scope.dataErrorMsg = "";
                    if (func) {
                        func();
                    }
                }).error(function () {
                    if (func) {
                        func();
                    }
                    $scope.emptyInfo = true;
                    $scope.dataErrorMsg = '加载失败，请重试！';
                });
            }

            $scope.doRefresh = function () {
                $ionicLoading.show({
                    template: '正在刷新...'
                });
                $scope.getOnlineSetData(function () {
                    $scope.$broadcast('scroll.infiniteScrollComplete');
                    $ionicScrollDelegate.scrollTop();
                    $ionicLoading.hide();
                });
            };
            $scope.doRefresh();
            $scope.getSelectData = function(){
                /**
                 * 获取报读层次下拉选项列表
                 */
                $http.get("/ynedut/majorGradation/searchMajorGradation.htm?pageNumber=0&pageSize=9999").success(function(data){
                    if(data.status == 0){
                        $scope.levelInfo = data.result.content;
                        //默认选中第一个层次
                        $scope.studentInfo.zsAdmissionInfoVO.regSchoolLevelId = $scope.levelInfo[0].id;
                    }
                });
                /**
                 * 获取生源地
                 */
                $http.get("/ynedut/originMaintain/searchOrigin.htm?pageNumber=0&pageSize=9999").success(function (data) {
                    if(data.status == 0){
                        $scope.sourcePlace = data.result.content;
                    }
                });

                /**
                 * 获取下拉列表
                 */
                $http.get("/ynedut/enrollByOnline/getPhonesFullDictInfo.htm").success(function(data){
                    if(data.status == 0){
                        $scope.dictInfo = data.result;
                        //报读层次
                        $scope.dictInfo.level = $scope.levelInfo;
                        //生源地
                        $scope.dictInfo.sourcePlace = $scope.sourcePlace;
                        //默认选中第一个报读校区
                        $scope.studentInfo.zsAdmissionInfoVO.regOrgNo = $scope.dictInfo.school[0].orgNo;
                    }
                });

                $http.get("/ynedut/basicInfomationManage/queryHouseHoldAddress.htm",{params:{id:null}}).success(function(data){
                    if(data.status == 0){
                        $scope.proListObj.proList = data.result[0];
                        $scope.homeProListObj.proList = data.result[0];
                    }
                });
            };



            $scope.$watch(function(){return $scope.proObj.pro.id;},function(nv){
                //级联省，肯定要清空级联的市
                $scope.proListObj.townList = [];
                $scope.proObj.city.id = '';
                $scope.proObj.town.id = '';
                if(nv){
                    $http.get("/ynedut/basicInfomationManage/queryHouseHoldAddress.htm",{params:{id:nv}}).success(function(data){
                        if(data.status == 0){
                            $scope.proListObj.cityList = data.result[0];
                        }
                    });
                }else{
                    //如果选择空，则清除下面级联的
                    $scope.proListObj.cityList = [];
                }
            });

            /**
             * 观察市，级联县
             */
            $scope.$watch(function(){return $scope.proObj.city.id;},function(nv){
                //级联市，肯定要清空级联的县
                $scope.proObj.town.id = '';
                if(nv){
                    $http.get("/ynedut/basicInfomationManage/queryHouseHoldAddress.htm",{params:{id:nv}}).success(function(data){
                        if(data.status == 0){
                            $scope.proListObj.townList = data.result[0];
                        }
                    });
                }else{
                    $scope.proListObj.townList = [];
                }
            });

            //户口所在地级联
            $scope.$watch(function(){return $scope.homeProObj.pro.id;},function(nv){
                //级联省，肯定要清空级联的市
                $scope.homeProListObj.townList = [];
                $scope.homeProObj.city.id = '';
                $scope.homeProObj.town.id = '';
                if(nv){
                    $http.get("/ynedut/basicInfomationManage/queryHouseHoldAddress.htm",{params:{id:nv}}).success(function(data){
                        if(data.status == 0){
                            $scope.homeProListObj.cityList = data.result[0];
                        }
                    });
                }else{
                    //如果选择空，则清除下面级联的
                    $scope.homeProListObj.cityList = [];
                }
            });

            /**
             * 观察市，级联县
             */
            $scope.$watch(function(){return $scope.homeProObj.city.id;},function(nv){
                //级联市，肯定要清空级联的县
                $scope.homeProObj.town.id = '';
                if(nv){
                    $http.get("/ynedut/basicInfomationManage/queryHouseHoldAddress.htm",{params:{id:nv}}).success(function(data){
                        if(data.status == 0){
                            $scope.homeProListObj.townList = data.result[0];
                        }
                    });
                }else{
                    $scope.homeProListObj.townList = [];
                }
            });
            /**
             * 根据报读层次和报读校区级联出专业
             * @param levelId 层次id
             * @param schoolId  校区id
             */
            $scope.cascadeSpecialty = function(levelId,schoolId){
                $http.get("/ynedut/enrollByOnline/cascadeSpecialty.htm",{params:{levelId:levelId,schoolId:schoolId}}).success(function(data){
                    if(data.status == 0){
                        $scope.specialty = data.result;
                    }
                });
            };
            /**
             * 观察报读层次和校区
             */
            $scope.$watch(function(){
                return $scope.studentInfo.zsAdmissionInfoVO.regSchoolLevelId +
                    $scope.studentInfo.zsAdmissionInfoVO.regOrgNo +
                    $scope.studentInfo.stuArchivesInfoVO.platformSysGradeId +
                    $scope.studentInfo.stuArchivesInfoVO.enrollType;
            },function(){
                //如果年级，招生类型，报读层次，报读校区都有值时才关联出专业，
                if($scope.studentInfo.zsAdmissionInfoVO.regSchoolLevelId && $scope.studentInfo.zsAdmissionInfoVO.regOrgNo){
                    $scope.cascadeSpecialty($scope.studentInfo.zsAdmissionInfoVO.regSchoolLevelId,$scope.studentInfo.zsAdmissionInfoVO.regOrgNo);
                }
            });
            /**
             * 初始化企业
             * @type {{firEnter: {}, secEnter: {}, thiEnter: {}}}
             */
            $scope.enterObj = {
                firEnter:{},
                secEnter:{},
                thiEnter:{}
            };

            /**
             *  级联第一个合作企业
             * @param speId
             * @param enter 要赋值得企业数组
             */
            $scope.cascadeFirEnter = function(speId,enter,seletEnter){
                $scope.studentInfo.zsAdmissionInfoVO[seletEnter] = '';
                //当专业存在时
                if(speId){
                    $http.get("/ynedut/newRegInfoMaintain/cascadeEnterprise.htm",{params:{enrollType:$scope.studentInfo.stuArchivesInfoVO.enrollType,gradeId:$scope.studentInfo.stuArchivesInfoVO.platformSysGradeId,speId:speId,schoolAreaId:$scope.studentInfo.zsAdmissionInfoVO.regOrgNo}}).success(function(data){
                        if(data.status == 0){
                            enter.data = data.result;
                        }
                    });
                }else{
                    //如果点击的请选择，则清空对应的企业
                    enter.data = [];
                }
            };
            //清除学习经历日期
            $scope.clearDateExp = function(item,isBegin){
                if(isBegin){
                    item.beginTimeString = "";
                }else{
                    item.endTimeString = "";
                }
            }
            /**
             * 初始化企业
             * @type {{firEnter: {}, secEnter: {}, thiEnter: {}}}
             */
            $scope.enterObj = {
                firEnter:{},
                secEnter:{},
                thiEnter:{}
            };

            /**
             *  级联第一个合作企业
             * @param speId
             * @param enter 要赋值得企业数组
             */
            $scope.cascadeFirEnter = function(speId,enter,seletEnter){
                $scope.studentInfo.zsAdmissionInfoVO[seletEnter] = '';
                //当专业存在时
                if(speId){
                    $http.get("/ynedut/newRegInfoMaintain/cascadeEnterprise.htm",{params:{enrollType:$scope.studentInfo.stuArchivesInfoVO.enrollType,gradeId:$scope.studentInfo.stuArchivesInfoVO.platformSysGradeId,speId:speId,schoolAreaId:$scope.studentInfo.zsAdmissionInfoVO.regOrgNo}}).success(function(data){
                        if(data.status == 0){
                            enter.data = data.result;
                        }
                    });
                }else{
                    //如果点击的请选择，则清空对应的企业
                    enter.data = [];
                }
            };
            //先将这个转换的工具方法放在添加行政班js中，待以后和上级沟通后是否放在util.js 工具js中
//用于将对象转换为springMVC可识别的对象形式
            var parseParamForSpringMVC = function(obj){
                //最终组合成功的对象
                var resultObj = {};
                //递归方法，用于深度转换每一个参数
                //key 对象时，是属性名
                //value 属性值
                //prefix（关键），和key进行组合相组合成想要的格式类型如name.age（对象）或name[0].age（数组）
                var deepParseParams = function(key,value,prefix){
                    //先判断是否是数组
                    if(value instanceof Array){
                        for(var i in value){
                            deepParseParams("",value[i],prefix+key+"["+i+"]");
                        }
                    }
                    //再判断是否是对象
                    else if(value instanceof Object){
                        for(var i in value){
                            deepParseParams("."+i,value[i],prefix+key);
                        }
                    }
                    //如果不是数组或对象，到了此次递归的最后一次，将完成组合的这条最终数据放在最终组合对象中
                    else{
                        resultObj[prefix+key] = value;
                    }

                };
                //因为传入的转换参数必须是对象,而且第一次传入和第二次开始组合“.”号是很特殊的地方，所有
                //第一次单独循环
                for(var i in obj){
                    deepParseParams("",obj[i],i);
                }

                //返回转换成功的对象集合
                return resultObj;
            };
            var reg = new RegExp(/^\w+@\w+([.]{1}\w+)+$/);
            $scope.submit = function(){

                if(!$scope.studentInfo.zsUserInfoVO.name){
                    ynuiNotification.warning({msg: '请填写姓名！'});
                    return;
                }
                if(!$scope.studentInfo.zsUserInfoVO.gender){
                    ynuiNotification.warning({msg: '请选择性别！'});
                    return;
                }
                if(!$scope.studentInfo.zsUserInfoVO.credentialType){
                    ynuiNotification.warning({msg: '请选择证件类型！'});
                    return;
                }
                if(!$scope.studentInfo.zsUserInfoVO.credentialNumber){
                    ynuiNotification.warning({msg: '请填写证件号码！'});
                    return;
                }
                if(!$scope.studentInfo.stuBaseInfoVO.nation){
                    ynuiNotification.warning({msg: '请选择民族！'});
                    return;
                }
                if($scope.proListObj.townList&&$scope.proListObj.townList.length>0){
                    if(!$scope.proObj.town.id){
                        ynuiNotification.warning({msg: '请选择籍贯(县)！'});
                        return;
                    }
                }
                if($scope.proListObj.cityList&&$scope.proListObj.cityList.length>0){
                    if(!$scope.proObj.city.id){
                        ynuiNotification.warning({msg: '请选择籍贯(市)！'});
                        return;
                    }
                }
                if($scope.proListObj.proList&&$scope.proListObj.proList.length>0){
                    if(!$scope.proObj.pro.id){
                        ynuiNotification.warning({msg: '请选择籍贯(省)！'});
                        return;
                    }
                }
                if(!$scope.studentInfo.stuBaseInfoVO.zsStudentSourceId){
                    ynuiNotification.warning({msg: '请选择生源地！'});
                    return;
                }
                if(!$scope.studentInfo.stuArchivesInfoVO.studentSource){
                    ynuiNotification.warning({msg: '请选择学生来源！'});
                    return;
                }
                if(!$scope.studentInfo.stuArchivesInfoVO.highestEducation){
                    ynuiNotification.warning({msg: '请选择入学前最高学历！'});
                    return;
                }
                if(!$scope.studentInfo.zsAdmissionInfoVO.stayStatus){
                    ynuiNotification.warning({msg: '请选择是否住宿！'});
                    return;
                }
                if(!$scope.studentInfo.stuFamilyInfoVO.familyPhone){
                    ynuiNotification.warning({msg: '请填写家庭电话！'});
                    return;
                }
                if(!$scope.studentInfo.stuFamilyInfoVO.familyAddress){
                    ynuiNotification.warning({msg: '请填写家庭住址！'});
                    return;
                }
                if(!$scope.studentInfo.zsAdmissionInfoVO.regSchoolLevelId){
                    ynuiNotification.warning({msg: '请选择报读层次！'});
                    return;
                }
                if(!$scope.studentInfo.zsAdmissionInfoVO.regOrgNo){
                    ynuiNotification.warning({msg: '请选择报读校区！'});
                    return;
                }
                if(!$scope.studentInfo.zsAdmissionInfoVO.firstSpecialty){
                    ynuiNotification.warning({msg: '请选择第一志愿！'});
                    return;
                }
                if($scope.studentInfo.zsAdmissionInfoVO.outerPresenter && $scope.studentInfo.zsAdmissionInfoVO.innerPresenter){
                    ynuiNotification.warning({msg: '推荐人（校内）、推荐人（校外）只能填写一个！'});
                    return;
                };
                if($scope.studentInfo.zsUserInfoVO.email&&!reg.test($scope.studentInfo.zsUserInfoVO.email)){
                    ynuiNotification.warning({msg: '邮箱格式不正确！'});
                    return;
                };
                var sign = false;
                angular.forEach($scope.studentInfo.learnExperienceVOList,function(item){
                    if(sign){
                        return ;
                    }
                    if(!item.beginTimeString){
                        ynuiNotification.warning({msg: '请填写学习经历起始时间！'});
                        sign = true;
                        return;
                    }
                    if(!item.endTimeString){
                        ynuiNotification.warning({msg: '请填写学习经历结束时间！'});
                        sign = true;
                        return;
                    }
                    if(!item.attendSchool){
                        ynuiNotification.warning({msg: '请填写学习经历就读学校！'});
                        sign = true;
                        return;
                    }
                    if(!item.reterence){
                        ynuiNotification.warning({msg: '请填写学习经历证明人！'});
                        sign = true;
                        return;
                    }
                });

                if(sign){
                    return ;
                }

                //处理查出的籍贯,因为有些籍贯只有到市，没有县
                $scope.studentInfo.stuBaseInfoVO.homeTownProvince = $scope.proObj.town.id ? $scope.proObj.town.id : $scope.proObj.city.id;
                //处理查出的户口所在地,因为有些户口所在地只有到市，没有县
                $scope.studentInfo.stuBaseInfoVO.houseHoldProvince = $scope.homeProObj.town.id ? $scope.homeProObj.town.id : $scope.homeProObj.city.id;
                if($scope.fileInfo.anmeldenFile&&$scope.fileInfo.anmeldenFile.id){
                    $scope.studentInfo.stuAttachmentInfoVOList.push({attachmentType:2,attachmentTypeValue:$scope.fileInfo.anmeldenFile.id});
                }
                if($scope.fileInfo.diplomaFile&&$scope.fileInfo.diplomaFile.id){
                    $scope.studentInfo.stuAttachmentInfoVOList.push({attachmentType:3,attachmentTypeValue:$scope.fileInfo.diplomaFile.id});
                }
                if($scope.fileInfo.idcardFile&&$scope.fileInfo.idcardFile.id){
                    $scope.studentInfo.stuAttachmentInfoVOList.push({attachmentType:1,attachmentTypeValue:$scope.fileInfo.idcardFile.id});
                }
                $scope.stuRegAdmissionVO = parseParamForSpringMVC($scope.studentInfo);
                $http.post("/ynedut/enrollByOnline/saveRegInfo.htm",$scope.stuRegAdmissionVO).success(function(data){
                    if (data.status == 0 && (data.result && data.result.length >0)) {
                        var s = "网上报名信息提交失败！<br/>";
                        angular.forEach(data.result,function(value,key){
                            s += '<li>' + (key+1) + '、' + value.failedReason + '</li>';
                        });
                        var message = '<ul class="list-unstyled">' + s + '</ul>';
                        ynuiNotification.error({msg:message});
                    } else if (data.status == 0){
                        ynuiNotification.success({msg:"网上报名信息提交成功！"});
                        $scope.initData();
                    }else {
                        ynuiNotification.error({msg:data.message});
                    }
                });
            };
            $scope.initFileInfo = function(){
                $scope.fileInfo = {
                    anmeldenFile:null,
                    diplomaFile:null,
                    idcardFile:null
                }
            };
            $scope.initFileInfo();
            $scope.fileChange = function(fileElementId){
                if($scope.fileInfo[fileElementId]&&$scope.fileInfo[fileElementId].id){
                    $http.get(basePath + "/file/delete.htm", {
                        params: {
                            fastDFSId: $scope.fileInfo[fileElementId].id
                        }
                    }).success(function (data) {
                        if (data == "success") {
                        }else{
                        }
                    })
                }

                var file = document.getElementById(fileElementId).files;
                if(file&&file.length != 0){
                    $scope.fileInfo[fileElementId] = {
                        id:"",
                        fileName:file[0].name
                    }
                }else{
                    $scope.fileInfo[fileElementId] = {
                        id:"",
                        fileName:""
                    }
                }

                setTimeout(function(){
                 $scope.$apply();
                });
            };
            $scope.delFile = function(item,fileElementId){
                document.getElementById(fileElementId).value="";
                //删除选择的文件，当文件已经上传需要删除
                if(item.id){
                    $ionicLoading.show({
                        template: '正在删除文件...'
                    });
                    $http.get(basePath + "/file/delete.htm", {
                        params: {
                            fastDFSId: item.id
                        }
                    }).success(function (data) {
                        $ionicLoading.hide();
                        if (data == "success") {
                            ynuiNotification.success({msg:"删除成功！"});
                            $scope.fileInfo[fileElementId] = {
                                id:"",
                                fileName:""
                            };
                            setTimeout(function(){
                                $scope.$apply();
                            });
                        }else{
                            ynuiNotification.error({msg:"删除失败！"});
                        }
                    }).error(function () {
                        $ionicLoading.hide();
                        ynuiNotification.error({msg:"删除失败！"});
                    });
                }else{
                    $scope.fileInfo[fileElementId] = {
                        id:"",
                        fileName:""
                    };
                    setTimeout(function(){
                        $scope.$apply();
                    });
                }

            }
            //用于上传文件后重新编译html，防止上传后不能监听选择文件的改变
            $scope.compileFileHtml = {
                anmeldenFile:true,
                diplomaFile:true,
                idcardFile:true
            };
            //解决上传后不能监听文件改变的问题
            $scope.compileHtmlFun = function(fileElementId){
                //用ng-if的方式重新编译html
                $scope.compileFileHtml[fileElementId]= false;
                setTimeout(function(){
                    $scope.$apply();
                    $scope.compileFileHtml[fileElementId]= true;
                    setTimeout(function(){
                        $scope.$apply();
                    });
                })
            };
            $scope.ajaxFileUpload = function(fileElementId){
                var file = document.getElementById(fileElementId).files;
                if(!file||file.length == 0){
                    ynuiNotification.warning({msg: '请选择文件再上传！'});
                    return false;
                }
                var fileName = file[0].name;
                var ldot = fileName.lastIndexOf(".");
                var type = fileName.substring(ldot + 1).toUpperCase( );
                if(type!="GIF"&&type!="JPG"&&type!="JPEG"&&type!="BMP"&&type!="PNG"){
                    ynuiNotification.warning({msg: '请选择图片文件上传！'});
                    return false;
                }
                $ionicLoading.show({
                    template: '正在上传...'
                });
                $.ajaxFileUpload
                (
                    {
                        url: '/ynedut/enrollByOnline/saveRegInfoAtt.htm', //用于文件上传的服务器端请求地址
                        type: 'post',
                        secureuri: false, //一般设置为false
                        fileElementId:fileElementId, //文件上传空间的id属性  <input type="file" id="file" name="file" />
                        dataType: 'text', //返回值类型 一般设置为json
                        success: function (data, status)  //服务器成功响应处理函数
                        {
                            $ionicLoading.hide();
                            $scope.fileInfo[fileElementId] = {};
                            if(data){
                                $scope.fileInfo[fileElementId] = {
                                    id:data,
                                    fileName:fileName
                                };
                                ynuiNotification.success({msg:"上传成功！"});
                                $scope.compileHtmlFun(fileElementId);

                            }else{
                                ynuiNotification.error({msg:"上传失败！"});
                            }
                        },
                        error: function (data, status, e)//服务器响应失败处理函数
                        {
                            $scope.fileInfo[fileElementId] = {};
                            $ionicLoading.hide();
                            ynuiNotification.error({msg:"上传失败！"});
                        }
                    }
                )
                return false;
            }
        }]);
+function ($) {
    'use strict';

    // DROPDOWN CLASS DEFINITION
    // =========================

    var backdrop = '.dropdown-backdrop'
    var toggle = '[data-toggle="dropdown"]'
    var Dropdown = function (element) {
        $(element).on('click.bs.dropdown', this.toggle)
    }

    Dropdown.VERSION = '3.3.5'

    function getParent($this) {
        var selector = $this.attr('data-target')

        if (!selector) {
            selector = $this.attr('href')
            selector = selector && /#[A-Za-z]/.test(selector) && selector.replace(/.*(?=#[^\s]*$)/, '') // strip for ie7
        }

        var $parent = selector && $(selector)

        return $parent && $parent.length ? $parent : $this.parent()
    }

    function clearMenus(e) {
        if (e && e.which === 3) return
        $(backdrop).remove()
        $(toggle).each(function () {
            var $this = $(this)
            var $parent = getParent($this)
            var relatedTarget = {relatedTarget: this}
            if ( e && ( !$parent.hasClass('open') || $(e.target).parents('.holdon').length)) return;

            if (e && e.type == 'click' && /input|textarea/i.test(e.target.tagName) && $.contains($parent[0], e.target)) return;

            $parent.trigger(e = $.Event('hide.bs.dropdown', relatedTarget));

            if (e.isDefaultPrevented()) return;

            $this.attr('aria-expanded', 'false');
            $parent.removeClass('open').trigger('hidden.bs.dropdown', relatedTarget)
        })
    }

    Dropdown.prototype.toggle = function (e) {
        var $this = $(this)

        if ($this.is('.disabled, :disabled')) return

        var $parent = getParent($this)
        var isActive = $parent.hasClass('open')

        clearMenus()

        if (!isActive) {
            if ('ontouchstart' in document.documentElement && !$parent.closest('.navbar-nav').length) {
                // if mobile we use a backdrop because click events don't delegate
                $(document.createElement('div'))
                    .addClass('dropdown-backdrop')
                    .insertAfter($(this))
                    .on('click', clearMenus)
            }

            var relatedTarget = {relatedTarget: this}
            $parent.trigger(e = $.Event('show.bs.dropdown', relatedTarget))

            if (e.isDefaultPrevented()) return

            $this
                .trigger('focus')
                .attr('aria-expanded', 'true')

            $parent
                .toggleClass('open')
                .trigger('shown.bs.dropdown', relatedTarget)
        }

        return false
    }

    Dropdown.prototype.keydown = function (e) {
        if (!/(38|40|27|32)/.test(e.which) || /input|textarea/i.test(e.target.tagName)) return

        var $this = $(this)

        e.preventDefault()
        e.stopPropagation()

        if ($this.is('.disabled, :disabled')) return

        var $parent = getParent($this)
        var isActive = $parent.hasClass('open')

        if (!isActive && e.which != 27 || isActive && e.which == 27) {
            if (e.which == 27) $parent.find(toggle).trigger('focus')
            return $this.trigger('click')
        }

        var desc = ' li:not(.disabled):visible a'
        var $items = $parent.find('.dropdown-menu' + desc)

        if (!$items.length) return

        var index = $items.index(e.target)

        if (e.which == 38 && index > 0)                 index--         // up
        if (e.which == 40 && index < $items.length - 1) index++         // down
        if (!~index)                                    index = 0

        $items.eq(index).trigger('focus')
    }


    // DROPDOWN PLUGIN DEFINITION
    // ==========================

    function Plugin(option) {
        return this.each(function () {
            var $this = $(this)
            var data = $this.data('bs.dropdown')

            if (!data) $this.data('bs.dropdown', (data = new Dropdown(this)))
            if (typeof option == 'string') data[option].call($this)
        })
    }

    var old = $.fn.dropdown

    $.fn.dropdown = Plugin
    $.fn.dropdown.Constructor = Dropdown


    // DROPDOWN NO CONFLICT
    // ====================

    $.fn.dropdown.noConflict = function () {
        $.fn.dropdown = old
        return this
    }


    // APPLY TO STANDARD DROPDOWN ELEMENTS
    // ===================================

    $(document)
        .on('click.bs.dropdown.data-api', clearMenus)
        .on('click.bs.dropdown.data-api', '.dropdown form', function (e) {
            e.stopPropagation()
        })
        .on('click.bs.dropdown.data-api', toggle, Dropdown.prototype.toggle)
        .on('keydown.bs.dropdown.data-api', toggle, Dropdown.prototype.keydown)
        .on('keydown.bs.dropdown.data-api', '.dropdown-menu', Dropdown.prototype.keydown)

};