'use strict';

/* globals angular */
angular.module('app')
.controller('signupCtrl', function($scope, $http) {

	$scope.signin = function() {
		$scope.messageSent = false;

		$http.get('/sapi/pub/signup', {params: {email: $scope.email}})
		.success(function(data) {
			console.log(data);
			$scope.messageSent = true;
		});
	};
});