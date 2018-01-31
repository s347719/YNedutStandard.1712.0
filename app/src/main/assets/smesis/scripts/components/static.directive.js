angular.module('staticModule',[]);

angular
    .module('staticModule')
    .directive('alertTop',[function () {
        return {
            restrict: 'C',
            link: function (scope,elem,attrs) {
                var $close = elem.find('.ion-close-round');
                $close.on('click',function () {
                    elem.remove();
                })
            }
        }
    }]);
