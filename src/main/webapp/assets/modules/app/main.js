/**
 * Main controller of the application pages. Responsible for display and change the available languages
 *
 * Ricardo Memoria
 * Oct 2014
 * 
 */
'use strict';

/* globals angular */
angular.module('app')
.controller('mainCtrl', ['$scope', 'locales', function($scope, locales) {

	$scope.locales = locales.list;
	$scope.selLocale = locales.selected;

	$scope.localeChanged = function() {
		locales.selectLocale($scope.selLocale);
	};
}]);