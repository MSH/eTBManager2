angular.module('docapp', [])
.controller('docController', function($scope, $http) {

        $scope.user = 'Ricardo';

        // initialization
        $http.get('/etbmanager/resources/api/pub/apidoc')
            .success(function (data) {
                $scope.doc = data;
            })


        $scope.groupClick = function (grp) {
            console.log(grp);
            $scope.group = grp;
        }

    });