angular.module('docapp', [])
.controller('docController', function($scope, $http) {

        $scope.user = 'Ricardo';

        // initialization
        $http.get('/etbmanager/resources/api/pub/apidoc/groups')
            .success(function (data) {
                $scope.doc = data;
            })


        $scope.groupClick = function (grp) {
            $http.get('/etbmanager/resources/api/pub/apidoc/routes?grp=' + grp.name)
                .success(function(data) {
                    $scope.group = data;
                });
            $scope.group = grp;
            $scope.routes = undefined;
        }

    });