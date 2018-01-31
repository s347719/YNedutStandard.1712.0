(function () {
    'use strict';

    angular.module('myApp').controller('monthSalaryController', monthSalaryController);

    monthSalaryController.$inject = ['$scope', '$http', '$ionicLoading'];

    function monthSalaryController(
        $scope, $http, $ionicLoading) {

        $scope.pageData = {
            tabNumber: 1,
            onlyImport: false,
            noData: false,
            monthList: [],
            salary: {}
        };

        $scope.getMonthList = getMonthList;
        $scope.formatShowDate = formatShowDate;
        $scope.findSalaryByMonth = findSalaryByMonth;
        $scope.changeMonth = changeMonth;
        $scope.changeTab = changeTab;

        // 获取数据
        $scope.getMonthList().then(function () {
            $scope.findSalaryByMonth($scope.pageData.monthList[1]);
        });

        /**
         * 获取薪酬年月，不传参数查询的最新的薪酬年月
         * @param month 薪酬日期 yyyy-MM 格式
         */
        function getMonthList(month) {
            $ionicLoading.show({
                template: '<h4>正在获取...</h4>',
                scope: $scope
            });
            return $http.get(basePath + '/third/monthsalary/getMonthList', {
                params: {
                    showDate: month
                }
            }).then(function (response) {
                $ionicLoading.hide();
                $scope.pageData.monthList = response.data;
                return response;
            })
        }

        /**
         * 把 yyyy-MM 格式的日期转换成 yyyy年MM月
         * @param date 要转化的日期
         * @returns {String}
         */
        function formatShowDate(date) {
            if (date) {
                var chars = date.split("-");
                return chars[0] + '年' + chars[1] + '月';
            }
            return '';
        }

        // 展示公积金或者社保
        $scope.getSocialSecurityShow = function (o1, o2) {
              var str = "";
              if(o1){
                  str += "公积金";
              }
              if (o1 && o2) {
                  str += "和社保";
              } else if (o2) {
                  str += "社保";
              }
              return str;
        };
        // 展示请假和旷工
        $scope.getLeavelAbsenteeismShow = function (o1, o2) {
              var str = "";
              if(o1){
                  str += "请假";
              }
              if (o1 && o2) {
                  str += "和旷工";
              } else if (o2) {
                  str += "旷工";
              }
              return str;
        };
        /**
         * 根据年月查询工资
         * @param month 格式 yyyy-MM 的字符串
         */
        function findSalaryByMonth(month) {
            return $http.get(basePath + '/third/monthsalary/findJSTXMonthSalaryDTOByMonth', {
                params: {
                    month: month
                }
            }).then(function (response) {
                $scope.pageData.salary = response.data;
                // 判断是否只有一个导入的工资条
                if (null !== $scope.pageData.salary.incomeItemNames && $scope.pageData.salary.incomeItemNames.length === 0
                    && null !== $scope.pageData.salary.deductItemNames && $scope.pageData.salary.deductItemNames.length === 0
                    && null !== $scope.pageData.salary.salaryTableNames && $scope.pageData.salary.salaryTableNames.length > 0
                    && null !== $scope.pageData.salary.gzglBonusPaywayDTOS && $scope.pageData.salary.gzglBonusPaywayDTOS.length === 0) {
                    // 显示单独的模块
                    $scope.pageData.onlyImport = true;
                    // 显示 tab 3 的数据
                    $scope.changeTab(3);
                } else {
                    // 显示 tab 3 的数据
                    $scope.changeTab(1);
                }
                return response;
            })
        }

        /**
         * 改变月份，重新获取数据
         * @param month 格式 yyyy-MM 月份，为 null 直接返回
         * @returns {boolean}
         */
        function changeMonth(month) {
            if (null === month) {
                return false;
            }
            // 获取数据
            $scope.getMonthList(month).then(function () {
                $scope.findSalaryByMonth($scope.pageData.monthList[1]);
            });
        }

        /**
         * 改变tab页时，处理没有数据时，显示无明细
         * @param number tab index
         */
        function changeTab(number) {
            $scope.pageData.tabNumber = number;
            switch ($scope.pageData.tabNumber) {
                case 1:
                    $scope.pageData.noData = null === $scope.pageData.salary.incomeItemNames ||
                        $scope.pageData.salary.incomeItemNames.length === 0;
                    break;
                case 2:
                    $scope.pageData.noData = null === $scope.pageData.salary.deductItemNames ||
                        $scope.pageData.salary.deductItemNames.length === 0;
                    break;
                case 3:
                    $scope.pageData.noData = null === $scope.pageData.salary.salaryTableNames ||
                        $scope.pageData.salary.salaryTableNames.length === 0;
                    break;
                case 4:
                    $scope.pageData.noData = null === $scope.pageData.salary.gzglBonusPaywayDTOS ||
                        $scope.pageData.salary.gzglBonusPaywayDTOS.length === 0;
                    break;
                default:
                    $scope.pageData.noData = false;
            }
        }
    }
})();
