'use strict';

angular.module('app')
.controller('loginCtrl', function($scope, $http, $cookies) {
	$scope.user = {};
	$scope.initFocus = true;
    $scope.entering = false;
    $scope.user.login = $cookies.login;

	$scope.login = function() {
        delete $scope.loginfailed;

        var params = 'user=' + $scope.user.login + '&pwd=' + $scope.user.password;
        $http.get(window.appinfo.basepath + '/api/login.seam?' + params)
            .success(function(res) {
                if (res.status === 'ok') {
                    $cookies.authToken = res.tk;
                    $cookies.login = $scope.user.login;
                    location.href = window.appinfo.basepath + '/home.seam';
                    $scope.entering = true;
                }
                else {
                    $scope.loginfailed = true;
                }
            });
	};
});