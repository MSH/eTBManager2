'use strict';


angular.module('app')
.controller('errorCtrl', function($scope, $http, $stateParams) {

	// get error message from server
	$scope.code = $stateParams.code;

	$http.get('/sapi/pub/error/' + $scope.code)
	.success(function(data) {
		$scope.message = data;
		$scope.processing = false;
	});
});