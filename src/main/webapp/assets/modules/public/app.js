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
      .otherwise('/login');

    $locationProvider.html5Mode(false);

    var pref = window.appinfo.basepath + '/assets/modules/public/';  

    $stateProvider
      .state('main', {
        templateUrl: pref + 'main.html',
        controller: 'mainCtrl'
      })
      .state('main.login', {
        url: '/login',
        controller: 'loginCtrl',
        templateUrl: pref + 'login.html'
      })
      .state('main.signup', {
        url: '/signup',
        controller: 'signupCtrl',
        templateUrl: pref + 'signup.html'
      })
      .state('main.newworkspace', {
        url: '/newworkspace/:wstoken',
        controller: 'newworkspaceCtrl',
        templateUrl: pref + 'newworkspace.html'
      })
      .state('main.error', {
        url: '/error/:code',
        controller: 'errorCtrl',
        templateUrl: pref + 'error.html'
      })
      .state('main.forgotpwd', {
        url: '/forgotpwd',
        controller: 'forgotpwdCtrl',
        templateUrl: pref + 'forgotpwd.html'
      });

    $httpProvider.interceptors.push('authInterceptor');

    // decorator to prevent caching the template files
/*
    $provide.decorator('$http', ['$delegate', function($delegate) {
      var get = $delegate.get;
      $delegate.get = function(url, config) {
        if (url.indexOf('.html', url.length-5) !== -1) {
          url = locales.selected.id + '/' + url;
        }
        return get(url, config);
      };
      return $delegate;
    }]);
*/

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
