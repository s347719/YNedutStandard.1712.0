<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en" class="no-js">
<%@include file="../../../common/taglibs.jsp" %>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width">
    <title>网上报名-YNedut V8</title>
    <script>
        var basePath = "${basePath}";

    </script>
    <link href="${basePath}/mobile/lib/ionic/css/ionic.css" rel="stylesheet">
    <link href="${basePath}/mobile/lib/toastr/toastr.min.css" rel="stylesheet">
    <link href="${basePath}/mobile/lib/swiper/css/swiper.min.css" rel="stylesheet">
    <link href="${basePath}/mobile/lib/mscroll/css/mobiscroll.animation.css" rel="stylesheet" type="text/css"/>
    <link href="${basePath}/mobile/lib/mscroll/css/mobiscroll.frame.css" rel="stylesheet" type="text/css"/>
    <link href="${basePath}/mobile/lib/mscroll/css/mobiscroll.scroller.css" rel="stylesheet" type="text/css"/>
    <link href="${basePath}/mobile/css/style.css" rel="stylesheet">
    <link href="${basePath}/mobile/css/ynedut_main.css" rel="stylesheet">

    <script src="${basePath}/mobile/lib/jquery/dist/jquery.min.js"></script>
    <script src="${basePath}/mobile/js/moment.min.js"></script>
    <script src="${basePath}/mobile/lib/ionic/js/ionic.bundle.min.js"></script>
    <script src="${basePath}/mobile/lib/underScore/underscore-min.js"></script>
    <script src="${basePath}/mobile/lib/toastr/toastr.min.js"></script>
    <script src="${basePath}/mobile/lib/swiper/js/swiper.min.js"></script>
    <script src="${basePath}/mobile/lib/swiper/js/angular-swiper.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/mobiscroll.core.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/mobiscroll.frame.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/mobiscroll.scroller.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/mobiscroll.util.datetime.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/mobiscroll.datetimebase.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/mobiscroll.datetime.js"></script>
    <script src="${basePath}/mobile/lib/mscroll/js/i18n/mobiscroll.i18n.zh.js"></script>

    <script src="${basePath}/mobile/js/components/static.directive.js"></script>
    <script src="${basePath}/mobile/js/components/datetimepicker/datetimepicker.directive.js"></script>
    <script src="${basePath}/mobile/js/enroll_stu.js"></script>
    <script src="${basePath}/mobile/js/ajaxfileupload.js"></script>
</head>
<body ng-app="enrollByOnline"  ng-controller="enrollByOnlineCtrl">

<ion-header-bar class="bar-calm" align-title="center">
    <h1 class="title">网上报名</h1><a class="pos-a-rxxz" ng-show="noticeObj && noticeObj.studentCanView" href="/ynedut/pages/enroll/stuEnroll/viewNotice.html" target="_blank">入学须知</a>
</ion-header-bar>
<ion-content class="has-header">
    <div ng-show="!emptyInfo&&isSetData">
        <div class="list">
            <!--基本信息-->
            <div>
                <div class="item item-button-right">
                    <strong>基本信息</strong>
                </div>
                <label class="item item-input">
                    <span class="input-label text-right">姓名<span class="required"> * </span></span>
                    <input type="text" ng-model="studentInfo.zsUserInfoVO.name" maxlength="30">
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        性别
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.zsUserInfoVO.gender" ng-options="i.dictCode as i.dictName for i in dictInfo.gender">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        证件类型
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.zsUserInfoVO.credentialType" ng-options="i.dictCode as i.dictName for i in dictInfo.credentialType">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input">
                    <span class="input-label text-right">证件号码<span class="required"> * </span></span>
                    <input type="text" ng-model="studentInfo.zsUserInfoVO.credentialNumber"  maxLength="30">
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        民族
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.stuBaseInfoVO.nation" ng-options="i.dictCode as i.dictName for i in dictInfo.nation">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        籍贯(省)
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="proObj.pro.id"  ng-options="i.id as i.name for i in proListObj.proList">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select" ng-show="proListObj.cityList&&proListObj.cityList.length>0">
                    <div class="input-label text-right">
                        籍贯(市)
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="proObj.city.id" ng-options="i.id as i.name for i in proListObj.cityList">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select" ng-show="proListObj.townList&&proListObj.townList.length>0">
                    <div class="input-label text-right">
                        籍贯(县)
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="proObj.town.id" ng-options="i.id as i.name for i in proListObj.townList">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        生源地
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.stuBaseInfoVO.zsStudentSourceId" ng-options="i.id as i.sourceName for i in dictInfo.sourcePlace">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        学生来源
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.stuArchivesInfoVO.studentSource" ng-options="i.dictCode as i.dictName for i in dictInfo.studentSource">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        入学前最高学历<span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.stuArchivesInfoVO.highestEducation" ng-options="i.dictCode as i.dictName for i in dictInfo.highestEducation">
                        <option value="">请选择</option>
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        是否住宿<span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.zsAdmissionInfoVO.stayStatus">
                        <option value="">请选择</option>
                        <option value="0">否</option>
                        <option value="1">是</option>
                    </select>
                </label>
                <label class="item item-input">
                    <div class="input-label text-right">
                        家庭电话<span class="required"> * </span>
                    </div>
                    <input type="number" ng-model="studentInfo.stuFamilyInfoVO.familyPhone"  maxlength="30">
                </label>
                <label class="item item-input">
                    <div class="input-label text-right">
                        家庭地址<span class="required"> * </span>
                    </div>
                    <input type="text" ng-model="studentInfo.stuFamilyInfoVO.familyAddress" maxlength="180">
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        报读层次
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.zsAdmissionInfoVO.regSchoolLevelId" ng-options="i.id as i.name for i in dictInfo.level">
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        报读校区
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.zsAdmissionInfoVO.regOrgNo" ng-options="i.orgNo as i.name for i in dictInfo.school">
                    </select>
                </label>
                <label class="item item-input item-select">
                    <div class="input-label text-right">
                        第一志愿
                        <span class="required"> * </span>
                    </div>
                    <select ng-model="studentInfo.zsAdmissionInfoVO.firstSpecialty" ng-options="i.id as i.name for i in specialty" ng-change="cascadeFirEnter(studentInfo.zsAdmissionInfoVO.firstSpecialty,enterObj.firEnter,'firstCompany')">
                        <option value="">请选择</option>
                    </select>
                </label>
                <div class="icon text-center padding-top padding-bottom" ng-class="!isShow ? 'ion-chevron-down' : 'ion-chevron-up'" ng-click="toggle()" ng-show="!isShow">
                    更多
                </div>
                <div ng-show="isShow">
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            订单班合作企业
                        </div>
                        <select ng-model="studentInfo.zsAdmissionInfoVO.firstCompany" ng-options="i.id as i.companyName for i in enterObj.firEnter.data">
                            <option value="">无</option>
                        </select>
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            第二志愿
                        </div>
                        <select ng-model="studentInfo.zsAdmissionInfoVO.secondSpecialty" ng-options="i.id as i.name for i in specialty" ng-change="cascadeFirEnter(studentInfo.zsAdmissionInfoVO.secondSpecialty,enterObj.secEnter,'secondCompany')">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            订单班合作企业
                        </div>
                        <select ng-model="studentInfo.zsAdmissionInfoVO.secondCompany" ng-options="i.id as i.companyName for i in enterObj.secEnter.data">
                            <option value="">无</option>
                        </select>
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            第三志愿
                        </div>
                        <select ng-model="studentInfo.zsAdmissionInfoVO.thirdSpecialty" ng-options="i.id as i.name for i in specialty" ng-change="cascadeFirEnter(studentInfo.zsAdmissionInfoVO.thirdSpecialty,enterObj.thiEnter,'thirdCompany')">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            订单班合作企业
                        </div>
                        <select ng-model="studentInfo.zsAdmissionInfoVO.thirdCompany" ng-options="i.id as i.companyName for i in enterObj.thiEnter.data">
                            <option value="">无</option>
                        </select>
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">毕业学校</span>
                        <input type="text" ng-model="studentInfo.stuArchivesInfoVO.graduatedUniversity" maxlength="100">
                    </label>
                    <div class="item item-input calender-icon touch-area">
                        <span class="input-label text-right">出生日期</span>
                        <%--<input ng-model="studentInfo.stuBaseInfoVO.dateOfBirth" type="text"  ynui-datetimepicker="YMD" range="enddateone" readonly>--%>
                        <input type="text" placeholder="日期" ng-model="studentInfo.stuBaseInfoVO.dateOfBirth" datetimepicker>
                        <i class="icon ion-close-round placeholder-icon" ng-click="clearDateOfBirth()"></i>
                    </div>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            生日类型
                        </div>
                        <select ng-model="studentInfo.stuBaseInfoVO.birthType">
                            <option value="0">公历</option>
                            <option value="1">农历</option>
                        </select>
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">毕业成绩</span>
                        <input type="text" ng-model="studentInfo.stuArchivesInfoVO.graduatedScore">
                    </label>

                    <label class="item item-input item-select">
                        <div class="input-label text-right" >
                            招生对象
                        </div>
                        <select ng-model="studentInfo.stuArchivesInfoVO.recruitTarget" ng-options="i.dictCode as i.dictName for i in dictInfo.admissionsObject">
                            <option value="">请选择</option>
                        </select>
                    </label>

                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            入住意向
                        </div>
                        <select ng-model="studentInfo.zsAdmissionInfoVO.platformSysDormitoryTypeId" ng-options="i.id as i.typeName for i in dictInfo.dormitoryRoomType.content">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            招生方式
                        </div>
                        <select ng-model="studentInfo.stuArchivesInfoVO.recruitType" ng-options="i.dictCode as i.dictName for i in dictInfo.admissionsWay">
                        </select>
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            报名方式
                        </div>
                        <select ng-model="studentInfo.ZSAdmissionInfoVO.regWay" ng-options="i.dictCode as i.dictName for i in  dictInfo.registrationWay">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">推荐人(校外)</span>
                        <input type="text" ng-model="studentInfo.zsAdmissionInfoVO.outerPresenter" maxlength="20">
                    </label>
                    <div class="item item-input calender-icon touch-area">
                        <span class="input-label text-right">推荐人(校内)</span>
                        <input type="text"  ng-model="innerPresenterName" readonly ng-click="bottomModalClick();">
                        <input type="hidden" ng-model="studentInfo.zsAdmissionInfoVO.innerPresenter">
                        <i class="icon ion-close-round placeholder-icon" ng-click="resetInnerPresenter();" ng-show="studentInfo.zsAdmissionInfoVO.innerPresenter"></i>
                    </div>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            入学方式
                        </div>
                        <select ng-model="studentInfo.stuArchivesInfoVO.enrollmentType" ng-options="i.dictCode as i.dictName for i in  dictInfo.enterStudy">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">手机号</span>
                        <input type="number" ng-model="studentInfo.zsUserInfoVO.mobile" maxlength="30">
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">邮箱</span>
                        <input type="text" ng-model="studentInfo.zsUserInfoVO.email" maxlength="20">
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right" >QQ&MSN</span>
                        <input type="text" ng-model="studentInfo.stuContactWayInfoVO.qqMsn" maxlength="50">
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right" >微信号</span>
                        <input type="text" ng-model="studentInfo.zsUserInfoVO.wechat" maxlength="50">
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">家庭联系人</span>
                        <input type="text" ng-model="studentInfo.stuFamilyInfoVO.familyContacts" maxlength="30">
                    </label>

                    <label class="item item-input">
                        <span class="input-label text-right">家庭邮政编码</span>
                        <input type="number" ng-model="studentInfo.stuFamilyInfoVO.familyZipCode" maxlength="10">
                    </label>

                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            户口性质
                        </div>
                        <select ng-model="studentInfo.stuBaseInfoVO.houseHoldType" ng-options="i.dictCode as i.dictName for i in  dictInfo.accountProperties">
                            <option value="">请选择</option>
                        </select>

                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            户口所在地(省)

                        </div>
                        <select ng-model="homeProObj.pro.id"  ng-options="i.id as i.name for i in homeProListObj.proList">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input item-select" ng-show="homeProListObj.cityList&&homeProListObj.cityList.length>0">
                        <div class="input-label text-right">
                            户口所在地(市)
                        </div>
                        <select ng-model="homeProObj.city.id" ng-options="i.id as i.name for i in homeProListObj.cityList">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input item-select" ng-show="homeProListObj.townList&&homeProListObj.townList.length>0">
                        <div class="input-label text-right">
                            户口所在地(县)
                        </div>
                        <select ng-model="homeProObj.town.id" ng-options="i.id as i.name for i in homeProListObj.townList">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">所属派出所</span>
                        <input type="text" ng-model="studentInfo.stuBaseInfoVO.belongsPolice" maxlength="100">
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">户口所在详细地址</span>
                        <input type="text" ng-model="studentInfo.stuBaseInfoVO.houseHoldAddress" maxlength="100">
                    </label>
                    <label class="item item-input item-select">
                        <div class="input-label text-right">
                            学生居住地类型
                        </div>
                        <select ng-model="studentInfo.stuBaseInfoVO.residenceType" ng-options="i.dictCode as i.dictName for i in  dictInfo.residenceType">
                            <option value="">请选择</option>
                        </select>
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">准考证号</span>
                        <input type="text" ng-model="studentInfo.stuArchivesInfoVO.ticketNumber" maxlength="30">
                    </label>
                    <label class="item item-input">
                        <span class="input-label text-right">备注</span>
                        <input type="text" ng-model="studentInfo.stuArchivesInfoVO.remark" maxlength="500">
                    </label>
                </div>
            </div>
            <!--学习经历-->
            <div  ng-show="isShow">
                <div class="item item-button-right">
                    <strong>学习经历</strong>
                    <button class="button button-small button-balanced"  ng-click="addLearnExperience()">
                        <i class="icon ion-plus-round"></i>
                    </button>
                </div>
                <ion-list>
                    <div ng-repeat="item in studentInfo.learnExperienceVOList">
                        <ion-item class="item-stable clearfix touch-area" ng-class="{active: item._isShow}">
                            <i class="icon"  ng-click="toggleGroup(item)" ng-class="item._isShow ? 'ion-minus' : 'ion-plus'"></i>
                            &nbsp;
                            <span ng-bind="'学习经历'+($index+1)"></span>
                            <a class="pull-right margin-top-10" ng-click="delLearnExperience($index)">删除</a>
                        </ion-item>
                        <div class="list" ng-show="item._isShow">
                            <div class="item item-input calender-icon touch-area">
                                <div class="input-label text-right">
                                    起始时间
                                    <span class="required"> * </span>
                                </div>
                                <input type="text" placeholder="年月" ng-model="item.beginTimeString" datetimepicker datetime-settings="timeSettingsList[$index].beginTimeSettings" ng-change="beginTimeSelect($index)">
                                <%--<input ng-model="item.beginTimeString" type="text"  ynui-datetimepicker="" range="{{'learnExperience'+$index}}" readonly>--%>
                                <i class="icon ion-close-round placeholder-icon" ng-click="clearDateExp(item,true)"></i>
                            </div>
                            <div class="item item-input calender-icon touch-area">
                                <div class="input-label text-right">
                                    结束时间
                                    <span class="required"> * </span>
                                </div>
                                <input type="text" placeholder="年月" ng-model="item.endTimeString" datetimepicker datetime-settings="timeSettingsList[$index].endTimeSettings" ng-change="endTimeSelect($index)">
                                <%--<input ng-model="item.endTimeString" type="text"  ynui-datetimepicker="" id="{{'learnExperience'+$index}}" readonly>--%>
                                <i class="icon ion-close-round placeholder-icon" ng-click="clearDateExp(item,false)"></i>
                            </div>
                            <label class="item item-input">
                                <div class="input-label text-right">
                                    就读学校
                                    <span class="required"> * </span>
                                </div>
                                <input type="text" ng-model="item.attendSchool" maxlength="30">
                            </label>
                            <label class="item item-input">
                                <div class="input-label text-right">
                                    证明人
                                    <span class="required"> * </span>
                                </div>
                                <input type="text" ng-model="item.reterence" maxlength="20">
                            </label>
                        </div>
                    </div>
                </ion-list>
            </div>
            <!--附件-->
            <!--
            <div ng-show="isShow">
                <input type="hidden" ng-model="userId">
                <div class="item item-button-right">
                    <strong>附件信息</strong>
                </div>
                <div>
                    <div class="list file-uploader-wrap">
                        <div class="item">
                            <span class="name">户口本附件</span>
                            <div class="select-file" >
                                <input type="file"  ng-if="compileFileHtml.anmeldenFile" bind-file ng-model="anmeldenFileData" name="anmeldenFile" id="anmeldenFile" accept="image/*" ng-change="fileChange('anmeldenFile')">
                                <button class="button button-small button-balanced">
                                    选择文件
                                </button>
                            </div>
                            <button id="1" class="button button-small button-balanced" ng-click="ajaxFileUpload('anmeldenFile')">
                                上传文件
                            </button>
                            <ul class="file-list" ng-show="fileInfo.anmeldenFile.fileName">
                                <li>{{fileInfo.anmeldenFile.fileName}}
                                    (<span style='color:red'ng-show="!fileInfo.anmeldenFile.id">未上传</span>
                                    <span style='color: blue'ng-show="fileInfo.anmeldenFile.id">上传成功</span>)
                                    <a href="javascript:;" ng-click="delFile(fileInfo.anmeldenFile,'anmeldenFile')">&nbsp;&nbsp;删除</a></li>
                            </ul>
                        </div>
                        <div class="item file-uploader-wrap">
                            <span class="name">毕业证附件</span>
                            <div class="select-file">
                                <input type="file"   ng-if="compileFileHtml.diplomaFile" bind-file ng-model="diplomaFileData" name="diplomaFile" id="diplomaFile" accept="image/*"  ng-change="fileChange('diplomaFile')">
                                <button class="button button-small button-balanced">
                                    选择文件
                                </button>
                            </div>
                            <button id="2" class="button button-small button-balanced" ng-click="ajaxFileUpload('diplomaFile')">
                                上传文件
                            </button>
                            <ul class="file-list"  ng-show="fileInfo.diplomaFile.fileName">
                                <li>{{fileInfo.diplomaFile.fileName}}
                                    (<span style='color:red'ng-show="!fileInfo.diplomaFile.id">未上传</span>
                                    <span style='color: blue'ng-show="fileInfo.diplomaFile.id">上传成功</span>)
                                    <a href="javascript:;" ng-click="delFile(fileInfo.diplomaFile,'diplomaFile')">&nbsp;&nbsp;删除</a></li>
                            </ul>
                        </div>
                        <div class="item file-uploader-wrap">
                            <span class="name">身份证附件</span>
                            <div class="select-file" >
                                <input type="file"  ng-if="compileFileHtml.idcardFile" bind-file ng-model="idcardFileData" name="idcardFile" id="idcardFile" accept="image/*"  ng-change="fileChange('idcardFile')">
                                <button class="button button-small button-balanced">
                                    选择文件
                                </button>
                            </div>
                            <button id="3" class="button button-small button-balanced" ng-click="ajaxFileUpload('idcardFile')">
                                上传文件
                            </button>
                            <ul class="file-list" ng-show="fileInfo.idcardFile.fileName">
                                <li>{{fileInfo.idcardFile.fileName}}
                                    (<span style='color:red'ng-show="!fileInfo.idcardFile.id">未上传</span>
                                    <span style='color: blue'ng-show="fileInfo.idcardFile.id">上传成功</span>)
                                    <a href="javascript:;" ng-click="delFile(fileInfo.idcardFile,'idcardFile')">&nbsp;&nbsp;删除</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
            -->
            <div class="icon text-center padding-top padding-bottom" ng-class="isShow ? 'ion-chevron-up' : 'ion-chevron-down'" ng-click="toggle()" ng-show="isShow">
                收起
            </div>
        </div>
        <button class="button button-full button-positive" ng-click="submit()">
            提交
        </button>
    </div>
    <div class="creat-wrap" ng-show="!emptyInfo&&!isSetData">
        <div class="content-body-text text-center">
            <h4 class="">温馨提示：当前时间不在报名时间范围内，不能进行网上报名！</h4>
        </div>
    </div>
    <div class="creat-wrap" ng-show="emptyInfo">
        <div class="content-body-text text-center">
            <h4 class="" ng-bind="dataErrorMsg"></h4>
            <div class=" text-muted" ng-click="doRefresh()">单击重试</div>
        </div>
    </div>
</ion-content>
<ion-footer-bar align-title="left">
</ion-footer-bar>
<!--推荐人弹出模态框-->
<script id="template.html" type="text/ng-template">
    <div class="modal modal-btm modal-height">
        <div class="modal-list-wrap">
            <div class="list-heading clearfix">
                选择校内推荐人
                <span class="calm" ng-click="closeModal()">取消</span>
            </div>
            <div class="padding-10 border-bottom">
                <select name="userType" id="userType" class="input-inline input-border form-control" ng-model="search.userType">
                    <option value="1">老师</option>
                    <option value="2">学生</option>
                </select>
                <input type="text" placeholder="搜索" class="form-control input-border input-inline padding-left-10" ng-model="search.searchText"/>
                <button class="button button-calm input-inline" type="button" ng-click="queryUserInfo();">查询</button>
            </div>
            <ul class="list-wrap text-center" style="max-height: 350px;overflow-y: auto;">
                <li class="list-item" ng-if="!userItems || userItems.length<=0"> 没有相关数据！</li>
                <li ng-if="userItems && userItems.length>0" class="list-item" ng-class="{'active':(studentInfo.zsAdmissionInfoVO.innerPresenter && item.id == studentInfo.zsAdmissionInfoVO.innerPresenter)}" ng-repeat="item in userItems" ng-click="chooseInnerPresenter(item)">{{item.name}}</li>
            </ul>
        </div>
    </div>
</script>
<!--推荐人弹出模态框-->
</body>

</html>
