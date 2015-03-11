'use strict';

angular.module('app', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ui.router',
  'ngBusy'
])
  .config(function ($stateProvider, $urlRouterProvider, $provide, $httpProvider, $locationProvider) {
    $urlRouterProvider
      .otherwise('/home');

    $locationProvider.html5Mode(false);

    var pref = '/modules/app/';

    $stateProvider
      .state('main', {
        templateUrl: pref + 'main.html',
        controller: 'mainCtrl'
      })
      .state('main.home', {
        templateUrl: pref + 'home.html',
        url: '/home',
        controller: 'homeCtrl'
      });

    $httpProvider.interceptors.push('authInterceptor');

    // decorator to append the language in the template
    $provide.decorator('$http', ['$delegate','locales', function($delegate, locales) {
      var get = $delegate.get;
      $delegate.get = function(url, config) {
        if (url.indexOf('.html', url.length-5) !== -1) {
          url = locales.selected.id + '/' + url;
        }
        return get(url, config);
      };
      return $delegate;
    }]);
  })
  .factory('authInterceptor', function ($rootScope, $q, $cookieStore, $location) {
    return {
      // Add authorization token to headers
      request: function (config) {
        config.headers = config.headers || {};
        if ($cookieStore.get('token')) {
          config.headers.Authorization = 'Bearer ' + $cookieStore.get('token');
        }
        return config;
      },

      // Intercept 401s and redirect you to login
      responseError: function(response) {
        if(response.status === 401) {
          $location.path('/login');
          // remove any stale tokens
          $cookieStore.remove('token');
          return $q.reject(response);
        }
        else {
          return $q.reject(response);
        }
      }
    };
  });
