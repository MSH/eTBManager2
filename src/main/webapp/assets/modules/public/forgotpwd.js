'use strict';

/* globals angular */
angular.module('app')
.controller('forgotpwdCtrl', function($scope, $cookies, $http) {
	$scope.email = $cookies.email;

	/**
	 * Send e-mail to start the process of changing the user password
	 * @return {[type]} [description]
	 */
	$scope.send = function() {
		$http.get(window.appinfo.basepath + '/sapi/pub/forgotpwd', {params: {email:$scope.email}})
		.success(function(data) {
			window.alert(data);
		});
	};
});