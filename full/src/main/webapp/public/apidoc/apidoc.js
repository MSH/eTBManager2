angular.module('docapp', ['ngSanitize'])
.controller('docController', function($scope, $http) {

        $scope.user = 'Ricardo';

        // initialization
        $http.get('/etbmanager/resources/api/pub/apidoc/groups')
            .success(function (data) {
                $scope.doc = data;
            });

        $scope.groupClick = function (grp) {
            $http.get('/etbmanager/resources/api/pub/apidoc/routes?grp=' + grp.name)
                .success(function(data) {
                    $scope.loadingRoutes = false;
                    $scope.group = data;
                    var routes = $scope.group.routes;
                    if (routes) {
                        routes.forEach(function(route) {
                            $scope.updateRouteInfo(route);
                        });
                    }
                });
            $scope.loadingRoutes = true;
            $scope.group = grp;
        }


        $scope.updateRouteInfo = function(route) {
            if (route.inputObject) {
                route.inputObjectHTML = genSchemaExample(route.inputObject);
            }
            if (route.returnObject) {
                route.returnObjectHTML = genSchemaExample(route.returnObject);
            }
        }

        $scope.showInputObj = function(it) {
            it.inputmd = false;
        }

        $scope.showInputmd = function(it) {
            it.inputmd = true;
        }
        $scope.showRetObj = function(it) {
            it.retmd = false;
        }

        $scope.showRetmd = function(it) {
            it.retmd = true;
        }

        $scope.toggleDetail = function(it) {
            it.showDetail = !it.showDetail;
        }
    });

function genSchemaExample(schema) {
    var s = '<div class="md-item">';
    if ((schema.properties && !schema.type) || (schema.type === 'Object')) {
        s += genObjectSchema(schema, true);
    }
    else {
        if (schema.type === 'Array') {
            s += genSchemaArray(schema);
        }
        else {
            s += genSchemaType(schema);
        }
    }


    return s + ',</div>';
}

function genObjectSchema(schema, showname) {
    var s = schema.name && showname? schema.name + ': {': '{';
    if (schema.properties) {
        schema.properties.forEach(function (it) {
            s += genSchemaExample(it);
        });
    }
    s += '}'
    return s;
}

function genSchemaArray(sc) {
    var s = addComment(sc);
    s += sc.name + ': [';
    if (sc.properties) {
        s += genObjectSchema(sc, false);
    }
    s += ']';
    return s;
}

function genSchemaType(sc) {
    var s = addComment(sc);
    s += sc.name + ': <span class="badge">' + sc.type + '</span>';
    return s;
}

function addComment(sc) {
    var s = sc.description;
    if (!s) {
        return '';
    }
    var i = 70;
    while (i < s.length) {
        var end = s.indexOf(' ', i);
        s = s.substring(0, end) + '<br>' + s.substring(end, s.length);
        i = end + 70;
    }
    return '<div class="md-rem">/* ' + s + ' */</div>';
}