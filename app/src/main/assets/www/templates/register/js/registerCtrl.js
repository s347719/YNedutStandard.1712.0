/**
 * Created by xionghongsong on 2017/1/5.
 * 登记信息页面
 */

angular.module('starter').controller("registerCtrl", ['$scope', '$http', '$ionicModal', '$ionicLoading', '$ionicScrollDelegate', '$location', "ynuiNotification","tkStorageService","$ionicPopup", function ($scope, $http, $ionicModal, $ionicLoading, $ionicScrollDelegate, $location, ynuiNotification,tkStorageService,$ionicPopup) {
    $scope.tkRegisterData = tkStorageService.getItem("tkRegisterData");
    $scope.tkBasicInfo = tkStorageService.getItemNoDel("tkBasicInfo");
    $scope.queryType = tkStorageService.getItemNoDel("queryType");
    //$scope.queryType = 3;
    $scope.queryId = tkStorageService.getItemNoDel("queryId");
    /**
     * 获取记录项目
     * 1：听课任务ID（通过任务，登记听课结果）
     * 2：听课表ID（登记任务外的听课结果）
     * 3：听课登记结果ID（查询登记结果修改）
     * @param queryId
     * @param queryType
     */
    $scope.getTableItem = function (queryId, queryType) {
        $ionicLoading.show({
            template: '正在加载...'
        });
        $scope.queryItemCondition = {
            queryIdType: queryType,
            queryId: queryId,
            isView: false,
            isNullTable: false
        }
        $http.post(originBaseUrl + "/third/tkTaskRegisterApp/queryResultRegisterTable.htm?", $scope.queryItemCondition).success(function (data) {
            $ionicLoading.hide();
            if (data.status == 0) {
                $scope.emptyInfo = false;
                $scope.recordTableVO = data.result;
                $scope.initSaveRegisterVO($scope.recordTableVO);
                $scope.oldShouldStuNum = $scope.saveTKResultRegisterVO.shouldStuNum;
            } else {
                ynuiNotification.error({msg: data.message});
                $scope.emptyInfo = true;
                $scope.dataErrorMsg = '加载失败';
            }
        }).error(function () {
            $ionicLoading.hide();
            $scope.emptyInfo = true;
            $scope.dataErrorMsg = '加载失败';
        });
    }
    $scope.oldShouldStuNum = 0;
    //获取应到学生
    $scope.getShouldStu = function(){
        $ionicLoading.show({
            template: '正在加载...'
        });
        //获取应到学生
        var teachingAdminClassIds = [];
        if($scope.saveTKResultRegisterVO.teachingAdminClassIdList){
            teachingAdminClassIds = $scope.saveTKResultRegisterVO.teachingAdminClassIdList.split(",");
        }
        $http.post(originBaseUrl+"/third/tkTaskRegisterApp/getShouldStuNum.htm?",{teachClassId:$scope.saveTKResultRegisterVO.pkTeachClassId,teachingAdminClassIds:teachingAdminClassIds}).success(function(data){
            $ionicLoading.hide();
            if(data.status == 0){
                $scope.saveTKResultRegisterVO.shouldStuNum = data.result;
                $scope.oldShouldStuNum = data.result;
            }else {
                ynuiNotification.error({msg: data.message});
            }
        }).error(function () {
            $ionicLoading.hide();
        });
    }
    $scope.emptyInfo = false;
    //获取听课节次显示字符
    var getShowKnob = function(knobType,knob){
        var  showKnob = "";
        if(knobType ==1){
            showKnob = "上午";
        }
        if(knobType == 2){
            showKnob = "下午";
        }
        if(knobType == 3){
            showKnob = "晚上";
        }
        showKnob += "第"+knob+"节";
        return showKnob;
    }
    //将选择的基本信息封装到saveTKResultRegisterVO
    $scope.setTKBasicInfo = function(){
        if(!$scope.tkBasicInfo ){
            return ;
        }
        $scope.saveTKResultRegisterVO.platformSysTermId = $scope.tkBasicInfo.termId;//学期
        $scope.saveTKResultRegisterVO.skSysUserId = $scope.tkBasicInfo.teacherId;//授课教师ID
        $scope.saveTKResultRegisterVO.pkScheduleResultsFormalId = $scope.tkBasicInfo.scheduleResultsId;//正式课表ID
        $scope.saveTKResultRegisterVO.dateStr = $scope.tkBasicInfo.selectedTKDate;//听课日期
        $scope.saveTKResultRegisterVO.week = $scope.tkBasicInfo.week;//听课周
        $scope.saveTKResultRegisterVO.day = $scope.tkBasicInfo.day;//听课星期
        $scope.saveTKResultRegisterVO.weekDayName = $scope.tkBasicInfo.weekAndDayDesc;//显示星期名称
        $scope.saveTKResultRegisterVO.timeType = $scope.tkBasicInfo.timeType;// 1上午、2下午、3晚上
        $scope.saveTKResultRegisterVO.knob = $scope.tkBasicInfo.knob;//节次
        $scope.saveTKResultRegisterVO.kcCourseId = $scope.tkBasicInfo.courseId;//课程ID
        $scope.saveTKResultRegisterVO.platformSysClassRoomId = $scope.tkBasicInfo.selectedClassRoomId;//授课地点ID
        $scope.saveTKResultRegisterVO.teachingAdminClassIdList = $scope.tkBasicInfo.teachingAdminClassIdStr;//'教学行政班ID组成的字'
        $scope.saveTKResultRegisterVO.pkTeachClassId = $scope.tkBasicInfo.teachClassId;//教学班id
        $scope.saveTKResultRegisterVO.pkTeachClassName = $scope.tkBasicInfo.teacherName;//教学班id
        //听课记录表
        $scope.saveTKResultRegisterVO.tkglTableId = $scope.tkBasicInfo.selectedTableId;
        //任务名称
        $scope.saveTKResultRegisterVO.showValueName.showKnob= getShowKnob($scope.saveTKResultRegisterVO.timeType,$scope.saveTKResultRegisterVO.knob);
        $scope.saveTKResultRegisterVO.showValueName.lectureTeacherName= $scope.tkBasicInfo.teacherName;
        $scope.saveTKResultRegisterVO.showValueName.courseName= $scope.tkBasicInfo.courseName;
        $scope.saveTKResultRegisterVO.showValueName.classRoomName= $scope.tkBasicInfo.selectedClassRoomName;
        $scope.saveTKResultRegisterVO.showValueName.teachingAdminClassName= $scope.tkBasicInfo.teachingAdminClassNameStr;
    }
    $scope.getTeachClassList = function(){
        $scope.isSelectStudyPlan = true;
        $scope.teachClassList = [];
        //当有教学班级的时候，授课计划只能是这个教学班级的，所以不用查询课程下的教学班级
        if($scope.saveTKResultRegisterVO.pkTeachClassId){
            return ;
        }
        $ionicLoading.show({
            template: '正在加载...'
        });
        $http.post(originBaseUrl + "/third/tkTaskRegisterApp/queryTeacherTeachClass.htm", {
            teacherId: $scope.saveTKResultRegisterVO.skSysUserId,
            courseId: $scope.saveTKResultRegisterVO.kcCourseId,
            termId:  $scope.saveTKResultRegisterVO.platformSysTermId
        }).success(function (data) {
            $ionicLoading.hide();
            if (data.status == 0) {
                $scope.teachClassList = data.result;
                if( !$scope.teachClassList||$scope.teachClassList.length==0){
                    $scope.isSelectStudyPlan = false;
                }
            }else{
                $ionicLoading.hide();
            }
        }).error(function () {
            $ionicLoading.hide();
        });;
    }
    //判断是否存在tkRegisterData 如果存在则是通过其他选择页面返回， 如果为空这是通过列表页面进入
    if($scope.tkRegisterData){
        $scope.isSelectStudyPlan=$scope.tkRegisterData.isSelectStudyPlan;
        $scope.teachClassList=$scope.tkRegisterData.teachClassList;
        $scope.queryType=$scope.tkRegisterData.queryType;
        $scope.tkTypeCodeItem=$scope.tkRegisterData.tkTypeCodeItem;
        $scope.registerItemList=$scope.tkRegisterData.registerItemList;
        $scope.saveTKResultRegisterVO=$scope.tkRegisterData.saveTKResultRegisterVO;
        $scope.showItem=$scope.tkRegisterData.showItem;
        $scope.scoreItemList=$scope.tkRegisterData.scoreItemList;
        $scope.suggestionItem = $scope.tkRegisterData.suggestionItem;
        $scope.actContentItem = $scope.tkRegisterData.actContentItem;
        //判断是否有修改听课记录表，如果修改则重新获取听课记录表的项目
        if($scope.tkBasicInfo&&$scope.saveTKResultRegisterVO.tkglTableId != $scope.tkBasicInfo.selectedTableId){
            if($scope.queryType ==2){
                $scope.queryId = $scope.tkBasicInfo.selectedTableId
            }
            $scope.getTableItem($scope.queryId ,$scope.queryType );
        }else if($scope.tkBasicInfo){

            var isGetTeachClassList = false;
            var isGetShouldStu = false;
            //授课老师以及授课课程以及学期改变 重新获取相应的教学班级（选择授课计划）
            if($scope.saveTKResultRegisterVO.skSysUserId!=$scope.tkBasicInfo.teacherId||
                $scope.saveTKResultRegisterVO.kcCourseId!=$scope.tkBasicInfo.courseId||
                $scope.saveTKResultRegisterVO.platformSysTermId!=$scope.tkBasicInfo.termId){
                $scope.saveTKResultRegisterVO.lmsStudyChapterInfoId = "";
                $scope.saveTKResultRegisterVO.lmsStudyChapterInfoShowValue = "";
                //$scope.getTeachClassList();
                isGetTeachClassList = true;
            }
            //判断是否重新获取应到学生
            if($scope.saveTKResultRegisterVO.teachingAdminClassIdList!=$scope.tkBasicInfo.teachingAdminClassIdStr||
                $scope.saveTKResultRegisterVO.pkTeachClassId!=$scope.tkBasicInfo.teachClassId
            ){
                isGetShouldStu = true;
            }
            $scope.setTKBasicInfo();
            if(isGetTeachClassList){
                $scope.getTeachClassList();
            }
            if(isGetShouldStu){
                $scope.getShouldStu();
            }
        }

    }else{

        if($scope.queryType ==2){
            $scope.queryId = $scope.tkBasicInfo.selectedTableId
        }
        $scope.getTableItem($scope.queryId ,$scope.queryType );
    }
    //获取自定义字段的输入长度
    $scope.getMaxLength = function(item){
        //整数和小数
        if(item.itemType == 2&&(item.inputLimit == 2||item.inputLimit == 1)){
            var typeRange = {};
            if(item.digitalRange){
                angular.forEach(item.digitalRange.split(","),function(i){
                    typeRange[i] = i;
                });
            }
            var maxLength = item.maxLength;
            if(item.inputLimit == 2){
                maxLength += item.decimalMaxLength+1;
            }
            if(typeRange[2]){
                maxLength += 1;
            }
            item.limitMaxLength = maxLength;

        }else{
            item.limitMaxLength = item.maxLength;
        }
    }
    //验证自定义无文本输入数字的范围
    $scope.verifyNumber = function(item){
        //整数和小数
        if(item.itemType == 2&&(item.inputLimit == 2||item.inputLimit == 1)) {
            var value = "";
            var checkRule = "";
            if (item.code) {
                value = $scope.saveTKResultRegisterVO[item.code];
            } else {
                value = $scope.saveTKResultRegisterVO.customItemMap[item.id];
            }
            if(!value){
                return true;
            }
            var errorMsg = item.name;
            if(1 == item.inputLimit){
                eval("checkRule=\/^-?\\d{1,"+item.maxLength+"}$\/");
                errorMsg += "请输入不大于"+item.maxLength+"位的整数.";
            }else if(2 == item.inputLimit){
                eval("checkRule = \/^-?\\d{1," + item.maxLength + "}(\\.\\d{1," + item.decimalMaxLength + "})?$\/");
                errorMsg += "请输入整数位不大于"+item.maxLength+"位小数位不大于"+item.decimalMaxLength+"位的数字.";
            }
            if(value &&!checkRule.test(value) ){
                if (item.code) {
                    $scope.saveTKResultRegisterVO[item.code] = "";
                } else {
                    $scope.saveTKResultRegisterVO.customItemMap[item.id] = "";
                }
                ynuiNotification.error({msg: errorMsg,opts:{timeOut:3000}});
                return false;
            }
            var typeRange = {};
            if(item.digitalRange){
                angular.forEach(item.digitalRange.split(","),function(i){
                    typeRange[i] = i;
                });
            }
            errorMsg = item.name;
            //只等于0
            if(typeRange[1]&&!typeRange[0]&&!typeRange[2]) {
                errorMsg += "只能等于0!";
            }else if(!typeRange[1]&&typeRange[0]&&!typeRange[2]) {
                //只大于0
                errorMsg += "请输入大于0的数！";
            }else if(!typeRange[1]&&!typeRange[0]&&typeRange[2]) {
                //只小于0
                errorMsg += "请输入小于0的数！";
            }else if(typeRange[1]&&typeRange[0]&&typeRange[2]) {
                //大于小于等于0
            }else if(typeRange[1]&&!typeRange[0]&&typeRange[2]) {
                //小于等于0
                errorMsg += "请输入小于等于0的数！";
            }else if(typeRange[1]&&typeRange[0]&&!typeRange[2]) {
                //大于等于0
                errorMsg += "请输入大于等于0的数！";
            }else if(!typeRange[1]&&typeRange[0]&&typeRange[2]) {
                //不等于0
                errorMsg +="请输入不等于0的数！";
            }
            //是否存在范围类
            var isRang = false;
            if(typeRange[0]&&value>0){
                isRang = true;
            }
            if(typeRange[1]&&value==0){
                isRang = true;
            }
            if(typeRange[2]&&value<0){
                isRang = true;
            }
            if(!isRang){
                if (item.code) {
                    $scope.saveTKResultRegisterVO[item.code] = "";
                } else {
                    $scope.saveTKResultRegisterVO.customItemMap[item.id] = "";
                }
                ynuiNotification.error({msg: errorMsg});
                return false;
            }
        }
        return true;
    }



    //封装需要提交的参数
    $scope.initSaveRegisterVO = function (recordTableVO) {
        if (!recordTableVO) {
            recordTableVO = {};
        }
        //固定项目是否显示
        $scope.showItem = {
            //应到学生，实到学生 是否按计划上课一共显示几个
            number:0,
            //打分项目
            scoreItem:false
        }

        $scope.tkTypeCodeItem = null;
        $scope.registerItemList = [];
        //打分项目
        $scope.scoreItemList = [];

        $scope.saveTKResultRegisterVO = {
            id: recordTableVO.id,
            /**
             * 听课任务ID
             */
            taskId: recordTableVO.tkTaskId,
            /**
             * 听课教师ID
             */
            tkSysUserId: "",
            /**
             * 授课教师ID
             */
            skSysUserId: recordTableVO.lectureTeacherId,
            /**
             * 听课表ID
             */
            tkglTableId: recordTableVO.tkTableId,
            /**
             * 学期
             */
            platformSysTermId: recordTableVO.platformSysTermId,
            /**
             * 听课类型
             */
            tkTypeCode: recordTableVO.tkType,
            /**
             * 选择方式 0:依据课表 1:手动指定'
             */
            chooseWay: 1,
            /**
             * 正式课表ID
             */
            pkScheduleResultsFormalId: recordTableVO.scheduleId,

            /**
             * 听课日期
             */
            dateStr: recordTableVO.tkDate,
            /**
             *听课周
             */
            week: recordTableVO.week,
            /**
             *听课星期
             */
            day: recordTableVO.weekDay,
            /**
             * 显示星期名称
             */
            weekDayName: recordTableVO.weekDayName,
            /**
             * 1上午、2下午、3晚上
             */
            timeType: recordTableVO.knobType,
            /**
             * 节次
             */
            knob: recordTableVO.knob,
            /**
             * 课程ID
             */
            kcCourseId: recordTableVO.courseId,
            /**
             * 授课地点ID
             */
            platformSysClassRoomId: recordTableVO.classRoomId,
            /**
             * '教学行政班ID组成的字'
             */
            teachingAdminClassIdList: recordTableVO.teachingAdminClassIdList,
            /**
             * 教学班id
             */
            pkTeachClassId: recordTableVO.teachClassId,
            pkTeachClassName: recordTableVO.teachClassName,
            /**
             * 应到学生
             */
            shouldStuNum: "",
            /**
             * 实到学生
             */
            actStuNum: "",
            /**
             * 教学计划课次ID
             */
            lmsStudyChapterInfoId: "",
            lmsStudyChapterInfoShowValue: "",
            /**
             * 实际讲解内容
             */
            actContent: "",
            /**
             * 打分项总得分
             */
            totalScore: recordTableVO.score?recordTableVO.score:0,
            /**
             * 建议和意见
             */
            suggestion: "",
            /**
             * 是否按计划上课 true:是(默认) false:否
             */
            isPlanStudy: false,
            /**
             * 登记终端 0:PC(默认) 1:APP
             */
            terminal: 1,
            /**
             * 登记来源 0:管理员 1:听课人(默认)
             */
            source: 1,

            /**
             * 自定义项目的值
             * Map<itemId,输入的值>
             */
            customItemMap: {},
            //打分项目总分
            itemTotalScore:0,
            showValueName: {
                //任务名称
                tkTaskName: recordTableVO.tkTaskName,
                showDate: recordTableVO.showDate,
                showKnob: recordTableVO.showKnob,
                lectureTeacherName: recordTableVO.lectureTeacherDisplayName,
                courseName: recordTableVO.courseName,
                classRoomName: recordTableVO.classRoomName,
                teachingAdminClassName: recordTableVO.teachingAdminClassName
            }

        }
        if (recordTableVO.rowVOList && recordTableVO.rowVOList.length > 0) {
            angular.forEach(recordTableVO.rowVOList, function (row) {
                if (row.type == 1) {
                    angular.forEach(row.itemVOList, function (item) {
                        //$scope.getHint(item);
                        //$scope.getCheckRule(item);
                        //$scope.registerItemList.push(item);
                        $scope.getMaxLength(item);
                        if (item.code) {
                            //是否按计划上课
                            if ('isPlanStudy' == item.code) {
                                $scope.saveTKResultRegisterVO[item.code] = item.value == 'true' ? true : false;
                            } else {
                                $scope.saveTKResultRegisterVO[item.code] = item.value;
                            }
                            //获取那些固定项目需要显示
                            $scope.showItem[item.code] = true;
                            if('shouldStuNum' == item.code||'actStuNum' == item.code||'isPlanStudy' == item.code){
                                $scope.showItem.number+=1;
                            }
                            //授课计划讲解内容
                            if ('lmsStudyChapterInfoId' == item.code) {
                                $scope.saveTKResultRegisterVO.lmsStudyChapterInfoShowValue = item.showValue;
                            }
                            else if ('tkTypeCode' == item.code) {
                                //听课类型   保存下拉选项（跳转页面后再返回本页则不需要再获取）
                                $scope.tkTypeCodeItem = item;
                                for (var i in item.optionList) {
                                    if ($scope.saveTKResultRegisterVO.tkTypeCode == item.optionList[i].id) {
                                        $scope.saveTKResultRegisterVO.showValueName['tkTypeCode'] = item.optionList[i].name;
                                        break;
                                    }
                                }
                            }
                            else if ('tkSysUserId' == item.code) {
                                for (var i in item.optionList) {
                                    if ($scope.saveTKResultRegisterVO.tkSysUserId == item.optionList[i].id) {
                                        $scope.saveTKResultRegisterVO.showValueName['tkSysUserId'] = item.optionList[i].name;
                                        break;
                                    }
                                }
                            }else if('suggestion' == item.code){
                                $scope.suggestionItem = item;
                            }else if('actContent' == item.code){
                                $scope.actContentItem = item;
                            }
                        } else {
                            if(item.itemType==3&&(item.inputLimit==1||item.inputLimit==2)){
                                //移动端日期组件不能选择秒 所以特殊处理
                                if(item.value&&item.value.split(":").length==3){
                                    var dateStr = item.value.split(":");
                                    $scope.saveTKResultRegisterVO.customItemMap[item.id] = dateStr[0]+":"+dateStr[1];
                                }else{
                                    $scope.saveTKResultRegisterVO.customItemMap[item.id] = item.value;
                                }
                            }else{
                                $scope.saveTKResultRegisterVO.customItemMap[item.id] = item.value;
                            }


                            //单选下拉框
                            if (item.itemType == 4) {
                                for (var i in item.optionList) {
                                    if (item.value == item.optionList[i].id) {
                                        $scope.saveTKResultRegisterVO.showValueName[item.id] = item.optionList[i].name;
                                        break;
                                    }
                                }
                            }else if(item.itemType == 3){
                                item.dateVO = null;
                            }
                            $scope.registerItemList.push(item);
                        }

                    });
                } else {
                    $scope.showItem.scoreItem = true;
                    //不是修改听课结果的时候打分项目默认满分

                    if($scope.queryType!=3){
                        angular.forEach(row.itemVOList,function(item){
                            item.value =parseFloat(item.score);
                        });
                    }else{
                        //打分项目每项打分值的总和
                        var itemTotalScore = 0;
                        angular.forEach(row.itemVOList,function(item){
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
                    }

                    $scope.scoreItemList = row.itemVOList;
                }
            });
        }

        if($scope.queryType ==2){
            $scope.setTKBasicInfo();
            $scope.getShouldStu();
        }
        //查询老师在课程下的教学班级，用于查询授课计划
        $scope.getTeachClassList();
    }


    //单选操作
    $ionicModal.fromTemplateUrl('template.html', {
        scope: $scope,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.modal = modal;
    });
    $scope.choiceInfo = {};
    //点击单选项目
    $scope.showChoiceModal = function (value, item) {
        if(item.itemType!=4){
            $scope.showDate(item);
            return;
        }
        if(item.code == "tkTypeCode"&&$scope.queryType==1){
            return ;
        }
        $scope.choiceInfo = {
            value: value,
            item: item,
        };
        $scope.modal.show();
    };
    $scope.clickChoice = function (option) {
        $scope.choiceInfo.value = option.id;
        if ($scope.choiceInfo.item.code) {
            $scope.saveTKResultRegisterVO[$scope.choiceInfo.item.code] = option.id;
            $scope.saveTKResultRegisterVO.showValueName[$scope.choiceInfo.item.code] = option.name;
        } else {
            $scope.saveTKResultRegisterVO.customItemMap[$scope.choiceInfo.item.id] = option.id;
            $scope.saveTKResultRegisterVO.showValueName[$scope.choiceInfo.item.id] = option.name;
        }
        $scope.closeModal();
    }
    $scope.closeModal = function() {
        $scope.modal.hide();
    };
    //保存操作的数据导本地
    $scope.saveDate = function(){
        var tkRegisterData = {
            //是否可选择授课计划
            isSelectStudyPlan:$scope.isSelectStudyPlan,
            //授课老师以及课程对应的教学班
            teachClassList:$scope.teachClassList,
            queryType:$scope.queryType,
            //听课类型夏卡
            tkTypeCodeItem:$scope.tkTypeCodeItem,
            //打分表项目
            registerItemList:$scope.registerItemList,
            //保存的数据
            saveTKResultRegisterVO:$scope.saveTKResultRegisterVO,
            //显示的项目
            showItem:$scope.showItem,
            //打分项目集合
            scoreItemList:$scope.scoreItemList,
            //实际讲解内容
            suggestionItem:$scope.suggestionItem,
            //意见建议
            actContentItem:$scope.actContentItem
        }
        tkStorageService.setItem("tkRegisterData",tkRegisterData);

    }
    //跳转到授课计划选择页面
    $scope.gotoLessonsPlan = function(){
        if(!$scope.isSelectStudyPlan){
            return ;
        }
        $scope.saveDate();
        $location.path("/lessons_plan");
    }
    //跳转到打分项目页面
    $scope.gotoProjectGrade = function(){
        $scope.saveDate();
        $location.path("/project_grade");
    }
    //跳转到文本填写页面
    $scope.gotoContent = function(tkRegisterContentType){
        $scope.saveDate();
        tkStorageService.setItem("tkRegisterContentType",tkRegisterContentType);
        $location.path("/fill_remark");
    }
    $scope.back = function(){
        if($scope.queryType == 2){
            $scope.saveDate();
            $location.path("/basic_msg").search({source:"register"});
        }else{
            var confirmPopup = $ionicPopup.confirm({
                title:"提示",
                template: '确定要放弃当前编辑内容吗？',
                cancelText:"继续编辑",
                okText:"放弃"
            });
            confirmPopup.then(function(res) {
                if(res) {
                    tkStorageService.clearKey("tkRegisterData");
                    tkStorageService.clearKey("tkBasicInfo");
                    tkStorageService.clearKey("queryType");
                    tkStorageService.clearKey("queryId");
                    $location.path("/register_task");
                }
            });
        }
    }

    $scope.validStuNum = function(type){
        if(type ==1){
            if($scope.saveTKResultRegisterVO.shouldStuNum){
                if(!(/^\d{1,4}$/.test($scope.saveTKResultRegisterVO.shouldStuNum))){
                    $scope.saveTKResultRegisterVO.shouldStuNum = $scope.oldShouldStuNum;
                    ynuiNotification.error({msg: "应到学生只能在0~9999以内的整数!"});
                }
            }
        }else{
            if($scope.saveTKResultRegisterVO.actStuNum){
                if(!(/^\d{1,4}$/.test($scope.saveTKResultRegisterVO.actStuNum))){
                    $scope.saveTKResultRegisterVO.actStuNum = null;
                    ynuiNotification.error({msg: "实到学生只能在0~9999以内的整数!"});
                }
            }
        }
    }
    $scope.verifyScore = function(){
        var maxScore = 100;
        var currentScore = $scope.saveTKResultRegisterVO.totalScore;
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
        $scope.saveTKResultRegisterVO.totalScore = parseFloat(currentScore);

    }
    $scope.sub = function(){
        if(!$scope.saveTKResultRegisterVO.tkTypeCode){
            ynuiNotification.error({msg: "听课类型不能为空！"});
            return ;
        }
        for(var i in $scope.registerItemList){
            if(!$scope.verifyNumber($scope.registerItemList[i])){
                return;
            }
        }
        var subVO = angular.copy($scope.saveTKResultRegisterVO);
        for(var i in $scope.registerItemList){
            if($scope.registerItemList[i].itemType==3&&($scope.registerItemList[i].inputLimit==1||$scope.registerItemList[i].inputLimit==2)){
                if(subVO.customItemMap[$scope.registerItemList[i].id]){
                    if(subVO.customItemMap[$scope.registerItemList[i].id].split(":").length==2){
                        //移动端日期组件不能选择秒 所以特殊处理
                        subVO.customItemMap[$scope.registerItemList[i].id]+=":00";
                    }

                }
            }
        }
        subVO.isAdmin = false;
        delete  subVO.showValueName;
        $ionicLoading.show({
            template: '正在提交...'
        });
        $http.post(originBaseUrl+"/third/tkTaskRegisterApp/saveResultRegister.htm?",subVO).success(function(data){
            $ionicLoading.hide();
            if(data.status == 0){
                ynuiNotification.success({msg: "提交成功！"});
                tkStorageService.clearKey("tkRegisterData");
                tkStorageService.clearKey("tkBasicInfo");
                tkStorageService.clearKey("queryType");
                tkStorageService.clearKey("queryId");
                $location.path("/register_task");
            }else{
                ynuiNotification.error({msg: data.message});
            }
        }).error(function () {
            $ionicLoading.hide();
            ynuiNotification.error({msg: "提交失败！"});
        });
    }
    $scope.showDate = function(item){
        if(item.itemType != 3){
            return ;
        }
        item.dateVO.show();
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