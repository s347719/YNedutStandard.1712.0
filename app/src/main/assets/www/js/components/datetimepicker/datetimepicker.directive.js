angular.module('staticModule')
    .directive('datetimepicker', [function () {
        return {
            scope: {
                datetimeSettings: '=',
                onSelect: '&onSelect',
                datetimeInstance: '=',
                datetimeType: '@'
            },
            link: function (scope, element, attrs) {
                if(scope.ngModel){
                    scope.date = new Date(scope.ngModel);
                    console.log(scope.date)
                }else{
                    scope.date = null
                }
                scope.defaultOpts = {
                    lang: 'zh',
                    stepMinute: 1,
                    minWidth: 50,
                    showLabel: true,
                    minDate: new Date((new Date()).getFullYear() - 50, 00, 01, 00, 00),
                    maxDate: new Date((new Date()).getFullYear() + 10, 11, 31, 23, 59),
                    dateOrder: 'yymmdd',
                    dateFormat: 'yy-mm-dd',
                    timeFormat: 'HH:ii',
                    buttons: [ 'set', 'cancel', 'clear']
                };
                scope.defaultOpts.onSelect = function (valueText, inst) {
                    scope.onSelect({item: valueText});
                };
                function getSettings() {
                    return scope.settings = angular.extend({}, scope.defaultOpts, scope.datetimeSettings);
                }

                function init() {
                    switch (scope.datetimeType) {
                        case  'datetime' :
                            element.mobiscroll().datetime(getSettings());
                            break;
                        case  'time' :
                            element.mobiscroll().time(getSettings());
                            break;
                        default :
                            element.mobiscroll().date(getSettings());
                            break;
                    }
                    if(attrs.datetimeInstance){
                        scope.datetimeInstance = {
                            show: function () {
                                element.mobiscroll('show');
                            },
                            close: function(){
                                element.mobiscroll('close');
                            }
                        };
                    }
                }

                scope.$parent.$watch(attrs.datetimeSettings, function () {
                    init();
                });
            }
        }
    }]);
