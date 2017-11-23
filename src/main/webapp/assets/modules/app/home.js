/**
 * Controller for the home page
 *
 * Ricardo Memoria
 * Nov 2014
 * 
 */
'use strict';

/* globals angular */
angular.module('app')
.controller('homeCtrl', ['$scope', '$cookies', function($scope, $cookies) {

	$scope.token = $cookies.authToken;

	console.log($scope.token);

}]);