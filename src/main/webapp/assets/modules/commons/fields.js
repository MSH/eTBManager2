'use strict';

angular.module('app').directive('field', function() {
	return {
		restrict: 'E',
		transclude: true,
		scope: {
			errorMsg: '='
		},
		template: '<div ng-transclude></div>' +
			'<span class="text-danger" ng-show="errorMsg">' +
			'<b>{{errorMsg}}</b>' + 
			'</span>'
	};
});