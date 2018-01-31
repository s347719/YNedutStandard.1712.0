/**
 * Created by YN on 2016/4/26.
 */
angular.module('starter')
    .factory('sendPromiseService', ['$q', '$http',
        function ($q, $http) {
            var sendPromise = function (config) {
                var deffered = $q.defer(),
                    promise;
                var bIsString = Object.prototype.toString.apply(config.type) === "[object String]";
                if (bIsString && config.type === 'GET') {
                    promise = $http.get(config.url);
                }
                if (bIsString && config.type === 'POST') {
                    promise = $http.post(config.url, config.postParams);
                }
                promise.then(
                    function (response) {
                        deffered.resolve(response.data);
                    },
                    function (response) {
                        deffered.reject(response.data);
                    }
                );
                return deffered.promise;
            };

            var oUrl = {
                queryTasks: originBaseUrl + "/third/studentRegistration/queryTasksForApp.htm",
                queryStudents: originBaseUrl + '/third/studentRegistration/queryMoraleduReportStudentByConditionForApp.htm',
                updateStatus: originBaseUrl + '/third/studentRegistration/updateStudentReportStatus.htm',
                statisticReport: originBaseUrl + "/third/studentRegistration/statisticReport.htm",
                getGrade: originBaseUrl + "/third/studentRegistration/getGradeDesc.htm",
                queryNoClassStu: originBaseUrl + "/third/studentRegistration/queryNoClassStudent.htm",
                addStudent: originBaseUrl + '/third/studentRegistration/saveMoraleduReportStudent.htm'
            };

            return {
                send: function (attr, type, params) {
                    if (Object.prototype.toString.apply(params) !== "[object Object]") {
                        params = {};
                    }
                    var config;
                    if (type === 'POST') {
                        config = {
                            type: type,
                            url: oUrl[attr],
                            postParams: params
                        }
                    }
                    if (type === 'GET') {
                        config = {
                            type: type,
                            url: oUrl[attr]
                        }
                    }
                    return sendPromise(config);
                }
            }
        }
    ])
    .factory('receivePromiseService', ['sendPromiseService',
        function (sendPromiseService) {
            var receivePromise = function (promise, obj, attr, fn) {
                promise.then(
                    function (data) {
                        if (Object.prototype.toString.apply(obj) === "[object Object]" && Object.prototype.toString.apply(attr) === "[object String]") {
                            if (Object.prototype.toString.apply(data.result) === "[object Object]") {
                                obj[attr] = data.result;
                            }
                            if (Object.prototype.toString.apply(data.result.content) === "[object Array]") {
                                obj[attr] = data.result.content;
                            }
                            if (Object.prototype.toString.apply(data.result) === "[object Array]") {
                                obj[attr] = data.result;
                            }
                            if (Object.prototype.toString.apply(fn) === "[object Function]") {
                                fn(obj[attr]);
                            }
                        }
                    }
                );

                return promise;
            };

            return {
                queryTasks: function (obj, attr, fn) {
                    var promise = sendPromiseService.send('queryTasks', 'GET');
                    return receivePromise(promise, obj, attr, fn);
                },
                queryStudents: function (postParmas, obj, attr) {
                    var promise = sendPromiseService.send('queryStudents', 'POST', postParmas);
                    return receivePromise(promise, obj, attr);
                },
                updateStatus: function (postParmas) {
                    var promise = sendPromiseService.send('updateStatus', 'POST', postParmas);
                    return receivePromise(promise);
                },
                statisticReport: function (postParmas, obj, attr) {
                    var promise = sendPromiseService.send('statisticReport', 'POST', postParmas);
                    return receivePromise(promise, obj, attr);
                },
                getGrade: function (obj, attr) {
                    var promise = sendPromiseService.send('getGrade', 'GET');
                    return receivePromise(promise, obj, attr);
                },
                queryNoClassStu: function (postParmas, obj, attr) {
                    var promise = sendPromiseService.send('queryNoClassStu', 'POST', postParmas);
                    return receivePromise(promise, obj, attr);
                },
                addStudent: function (postParmas) {
                    var promise = sendPromiseService.send('addStudent', 'POST', postParmas);
                    return receivePromise(promise);
                }
            }
        }
    ]);