/**
 * Main controller of the public pages. Responsible for display and change the available languages
 *
 * Ricardo Memoria
 * Oct 2014
 * 
 */
'use strict';

/* globals angular */
angular.module('app')
.controller('mainCtrl', ['$scope', function($scope) {

	$scope.locales = [];
//	$scope.selLocale = locales.selected;

	$scope.localeChanged = function() {
		locales.selectLocale($scope.selLocale);
	};
}]);