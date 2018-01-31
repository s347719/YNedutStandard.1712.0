angular.module('myApp')
    .directive('userSelector', ['$http', '$ionicModal', '$q', '$filter', '$ionicPopup', '$timeout', function ($http, $ionicModal, $q, $filter, $ionicPopup, $timeout) {
        var template = '<ion-modal-view class="user-selector fixed-header has-input-inset min-modal-height " ng-class="{\'search-only\': searchIsShow}" ' +
            '<div class="fixed-header-wrapper">' +
            '<div class="item-input-inset">' +
            '<div class="margin-right-10" ng-if="title">选择{{title}}</div>'+
            '<div class="item-input-wrapper">' +
            '<i ng-class="{\' ion-ios-loop-strong\': isSearchIsShow, \'ion-ios-search-strong\': !isSearchIsShow}" ></i>' +
            '<input type="text" ng-model="query.queryText" ng-change="setSearchShowFun(query.queryText,inservicestates)" placeholder="搜索">' +  //锁定焦点用属性set-focus=""即可
            '<i class="ion-close-round" ng-click="delSearchText(query.queryText)"></i>'+
            '</div>' +
            // '<button ng-show="searchIsShow" class="button button-clear" ng-click="delSearchText(query.queryText)">关闭</button>'+
            '</div>' +
            '<div class="selector-name-list" ng-if="recentlyUserList">' +
            '<span>最近：</span>' +
            '<button class="button button-theme button-outline" ng-click="selectItem(info.userDTO)" ng-repeat=" info in recentlyUserList">{{info.userDTO.name}}</button>' +
            // '<button class="button button-theme button-outline">张三</button>' +
            // '<button class="button button-theme button-outline">张三</button>' +
            '</div>'+
            '<div ng-show="searchIsShow" class="tips clearfix"><!--正在搜索...-->共找到{{queryListData.length || 0}}个结果</div>' +
            '</div>' +
            '<ion-content ng-class="{\'top-90\':recentlyUserList}" ng-show="!searchIsShow ">' +
            '<ion-list class="list">' +
            '<div ng-repeat="item in listItem track by $index"> ' +
            '<div class="item item-title-divider clearfix" ng-click="orgIsShowUserList(item)"> ' +
                '<span class="pull-left">{{item.name }}</span> ' +
                '<span class="text-gray pull-left margin-left-5" >{{item.number}}人</span><span class="pull-right text-gray">' +
                '   <i ng-class="{\'ion-arrow-down-b\': item.isShowUser,\'ion-arrow-up-b\': !item.isShowUser}"></i>' +
                '</span>' +
            '</div>' +
            '<div class="item-seven-space" ng-show="item.number===0 || !item.isShowUser"></div>'+
            //单选
            '<ion-radio  ng-if="!isMore && item.isShowUser" ng-repeat="info in item.userList | filter:filterFun track by $index" ng-disabled="getDisabled(info)"  class="item-avatar avatar-default radio-left" ng-click="selectItem(info)" ng-model="info.selected">' +
            '<img  src="assets/images/userface_0.png">' +
            '<h2>{{info.name }}</h2>' +
            '<p><span>{{info.jobNumber}}</span><span class="margin-left-5">{{info.postName}}</span></p>' +
            '</ion-radio>' +
            //多选
            '<ion-checkbox ng-if="isMore && item.isShowUser" ng-repeat="info in item.userList | filter:filterFun track by $index" ng-disabled="getDisabled(info)" class="item-avatar avatar-default" ng-change="selectItem(info)" ng-model="info.selected">' +
            '<img src="assets/images/userface_0.png">' +
            '<h2>{{info.name }}</h2>' +
            '<p><span>{{info.jobNumber}}</span><span>{{info.postName}}</span></p>' +
            '</ion-checkbox>' +
            '</div>' +
            '</ion-list>' +
            '</ion-content>' +
             '<ion-content ng-show="searchIsShow">'+
                '<ion-checkbox ng-show="isMore" ng-repeat="info in queryListData | filter:filterFun track by $index" ng-change="selectItem(info)" ng-model="info.selected">' +
                '<h2>{{info.name +\'(\' + info.jobNumber + \')\' }} <span class="margin-left-5 text-gray">{{info.postName}}</span></h2> ' +
                '</ion-checkbox>' +
                '<ion-radio ng-show="!isMore" ng-repeat="info in queryListData | filter:filterFun track by $index" class="radio-left" ng-click="selectItem(info)" ng-model="info.selected">' +
                '<h2>{{info.name +\'(\' + info.jobNumber + \')\' }} <span class="margin-left-5 text-gray">{{info.postName}}</span></h2> ' +
                '</ion-radio>' +
             '</ion-content>'+
            '<ion-footer-bar class="bar-up-shadow preview-pos" >' +
            '<div class="preview-wrap arrow">'+
            '<ul class="preview-list">'+
            '<li class="preview-item" ng-if="userList.length > 0" ng-repeat="info in userList"><span class="text-box">{{info.name + \'(\' + info.jobNumber + \')\'}}</span> <i class="ion-style ion-close" ng-click="selectItem(info, true)"></i></li>'+
            '<li class="no-personnel" ng-if="userList.length == 0">没有选择人员</li>'+
            '</ul>'+
            '</div>'+
            '<div class="row" ng-if="!isMore">'+
            '<div class="col text-center btn-group-wrap" >'+
            '<button class="button button-theme button-local button-outline" ng-click="closeModal()">返回</button>' +
            '</div>'+
            '</div>'+
            '<div class="row" ng-if="isMore">'+
            '<div class="col text-center btn-group-wrap">'+
            '<button class="button button-theme button-local button-outline pull-left" ng-click="closeModal()">返回</button>' +
            '<button ng-show="isMore" class="button button-theme button-local" ng-click="closeModal(true)"> 确定 ({{userList.length}})</button>' +
            '<button class="button button-theme button-local button-outline pull-right" ng-click="previewListClick(isPreview)"> ' +
            '<span ng-show="!isPreview">预览</span><i  ng-show="isPreview" class="ion-style ion-close"></i>' +
            '</button>' +
            '</div>'+
            '</div>'+
            '</ion-footer-bar>' +
            '<div class="fullscreen-box" ng-show="isError || isSelectError">'+
            '<div class="screen-center">'+
            '<h4 ng-show="isError">加载失败</h4>'+
            '<h4 ng-show="isSelectError">没有可选人员</h4>'+
            '<p  ng-show="isError">刷新试试</p>'+
            '</div>'+
            '</div>'+
            '</ion-modal-view>';
        return {
            scope: {
                ngModel: '=',
                select: '&onSelect',
                selectList: '=',
                notShowUser: '=',
            },
            template: template,
            controller: ['$scope', '$ionicScrollDelegate', '$timeout', function ($scope, $ionicScrollDelegate, $timeout) {
                var filter = $filter('filter');
                $scope.isMore = false;  //是否多选 默认为false
                $scope.queryData = {};  //查询对象
                $scope.userList = [];  //多选 选中list
                var selectIds = {};   //多选选中的id
                var notShowIds = {};
                var cookieName = "userListByOrgId_";  //设置获取cookie 的名称 后面跟id
                // 搜索条件
                $scope.query = {
                    queryText: ""
                };
                // 选择方法 如果单选 就直接返回 数据 如果多选 就标记选中
                $scope.selectItem = function(item, isDel){
                    if($scope.isMore){
                        if(item.selected && !isDel){
                            $scope.userList.push(item);
                            selectIds[item.id] = true;
                        }else{
                            selectIds[item.id] = false;
                            $scope.userList = filter($scope.userList, function (val) {
                               return val.id != item.id;
                            });
                        }
                    }else{
                        //单选时保存 所选人员记录
                        $scope.recentlySelected(item);
                        $scope.select({item:item});
                        $scope.closeModal();
                    }
                };
                // 查询样式 修改 和 查询
                $scope.setSearchShowFun = function (item,inservicestates) {
                    $scope.searchIsShow = item && (item.length > 0) ;
                    $scope.isSearchIsShow = $scope.searchIsShow;
                    query.queryFun(item,inservicestates);
                };
                var query = {
                    timeOut: {},
                    queryFun: function (item,inservicestates) {
                        // 如果 在1s内触发 就 清理上次 的 不触发
                        if(this.timeOut){
                            $timeout.cancel(this.timeOut);
                        };
                        if(item){
                            this.timeOut = $timeout(function () {
                                $http.post(basePath + "/third/userselector/findUserDTOByName?name="+item+"&inservicestates="+inservicestates).success(function(data){
                                    $scope.queryListData = data;
                                    $scope.isSearchIsShow = false;
                                });
                            },1000);
                        }
                    }
                };

                $scope.getDisabled = function (val) {
                    return notShowIds[val.id];
                }
                //展示 关闭 组织机构人员
                $scope.orgIsShowUserList = function (item) {
                    var flag = angular.copy(item.isShowUser);
                    if(!flag) {
                        if(item.userList && item.userList.length > 0){
                        	$timeout(function() {
                            	item.isShowUser = true;
                        	});
                        } else {
                            var userList = getCookie(cookieName + item.id);
                            if(userList){
                                item.userList = JSON.parse(userList);
                                setCookie(cookieName+ item.id, userList);
                                $timeout(function() {
	                            	item.isShowUser = true;
	                        	});
                            }else{
                                $http.post(basePath + "/third/userselector/getUserDTOByOrgId?orgId=" + item.id+"&inservicestates="+$scope.inservicestates).success(function(data){
                                    item.userList = data;
                                    setCookie(cookieName+ item.id,  JSON.stringify(data));
                                    $timeout(function() {
		                            	item.isShowUser = true;
		                        	});
                                });
                            }
                        }
                    }else{
                        item.isShowUser = false;
                    }
                };
                // 过滤查询
                $scope.filterFun = function (item) {
                   item.selected = selectIds[item.id];
                   return true;
                };
                $scope.modal = $ionicModal.fromTemplate(template, {
                    scope: $scope,
                    animation: 'slide-in-up'
                });

                $scope.singleUserSelectData = {};
                $scope.openModal = function( ismore,inservicestates,title,routingStatusType,userTypeApp) {
                    $scope.singleUserSelectData = {};//清空
                    $scope.recentlyUserList = null;//清空
                    if(title){
                        $scope.title = title;
                    }
                    //单选的时候显示最近
                    if(ismore==false){
                       //获取最近选择的三个人
                        $scope.singleUserSelectData = {
                            routingStatusType:routingStatusType,//路由状态类型 流程为：流程key 非流程为：路由状态
                            userTypeApp:userTypeApp//人员类型
                        };
                        $http.post(basePath + "/third/leaveapply/findJSTXRecentlyUser",$scope.singleUserSelectData).success(function(data){
                           $scope.recentlyUserList = data.result;
                        }).error(function () {
                            $scope.isError = true;
                        });
                    }
                    $scope.inservicestates = inservicestates?inservicestates:"1,2,3";//在职状态
                    $scope.delSearchText(inservicestates);
                    // 每次打开判断 是否查询数据
                    if($scope.listItem && $scope.listItem.length > 0){
                        setUserList();
                    }else{
                        getUserAllList($scope.inservicestates,setUserList);
                    }
                    $scope.isMore = ismore;  //设置是否多选
                    $scope.modal.show();
                };
                // 数据处理 方法
                var setUserList = function () {
                    $scope.previewListClick(true);
                    if($scope.selectList) {
                        angular.forEach($scope.selectList, function (val) {selectIds[val.id] = true;});
                        $scope.userList = angular.copy($scope.selectList);
                    }else {
                        selectIds = {};
                        $scope.userList = [];
                    }
                    if($scope.notShowUser){
                        angular.forEach($scope.notShowUser, function (val) {notShowIds[val.id] = true;});
                    }
                };
                $scope.closeModal = function( item ) {
                    if($scope.isMore && item){
                        $scope.select( {item: $scope.userList} );
                    }
                    $scope.modal.hide();
                };
                //请空查询条件
                $scope.delSearchText = function (inservicestates) {
                    $scope.query.queryText = "";
                    $scope.setSearchShowFun($scope.query.queryText,inservicestates);
                };
                //Cleanup the modal when we're done with it!
                $scope.$on('$destroy', function() {
                    $scope.modal.remove();
                });
                // Execute action on hide modal
                $scope.$on('modal.hidden', function() {
                    // Execute action
                });
                // Execute action on remove modal
                $scope.$on('modal.removed', function() {
                    // Execute action
                });
                /*preview*/
                $scope.personnel = true;
                $scope.previewListClick = function( isPreview){
                    $scope.isPreview = !isPreview;
                    var elm = angular.element;
                    elm('.preview-wrap').hasClass('animate')==true ?elm('.preview-wrap').removeClass('animate') : elm('.preview-wrap').addClass("animate");
                };
                var getUserAllList = function (inservicestates, fun) {
                    $http.post(basePath + "/third/userselector/getAlluser?inservicestates="+inservicestates ).success(function(data){
                        $scope.isloading = true;
                        $scope.isError = false;
                        $scope.isSelectError = false;
                        $scope.listItem = angular.copy(data);
                        fun && fun(data);
                    }).error(function () {
                        $scope.isError = true;
                    });
                }
                var getMessageFun = function ( title, message) {
                    $ionicPopup.show({
                        title: title,
                        template: '<h5> ' + message + '</h5>',
                        buttons : [
                            {text : '确认', type : ' button-theme button-local'}
                        ]
                    });
                }
                function getCookie(name) {
                    // var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
                    // if(arr=document.cookie.match(reg))
                    //     return unescape(arr[2]);
                    // else
                    //     return null;
                    var time = window.localStorage.getItem(name + "_time");
                    if(time){
                        if(Date.parse(new Date()) <= time ){
                            return window.localStorage.getItem(name);
                        }
                    }else{
                        return null;
                    }
                }
                function setCookie(name,value) {
                    // var time = 10 * 60 * 1000;
                    // var exp = new Date();
                    // exp.setTime(exp.getTime() + time);
                    // document.cookie = name + "="+ escape (value) + "; path: /;expires=" + exp.toGMTString();
                    window.localStorage.setItem(name, value);
                    window.localStorage.setItem(name+"_time",Date.parse(new Date()) + (10 * 60 * 1000));
                }
                //选择最近 保存
                $scope.recentlySelected = function (item) {
                    $scope.singleUserSelectData.selectUserId = item.id;
                    $http.post(basePath + "/third/leaveapply/saveJSTXRecentlyUser",$scope.singleUserSelectData).success(function(data){
                       // console.log(data)
                    }).error(function () {
                        $scope.isError = true;
                    });
                }
            }],
            link: function (scope, elem, attrs) {
                elem.on('click',function(){
                    scope.openModal(attrs.ismore=="true",attrs.inservicestates,attrs.title,attrs.routingstatustype,attrs.usertypeapp);
                });
                if(attrs["onclick"]){
                    scope.$on(attrs["onclick"], function (event, val) {
                        scope.openModal(attrs.ismore=="true",attrs.inservicestates,attrs.title,attrs.routingstatustype,attrs.usertypeapp);
                    });
                }

            }
        }
    }]).directive('setFocus', function () {
            return function(scope, element){
                element[0].focus();
                scope.$on("setFocus", function ( e, data) {
                    element[0].focus();
                });
            };
});
