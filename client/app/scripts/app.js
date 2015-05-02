'use strict';

/**
 * @ngdoc overview
 * @name ralpthbotApp
 * @description
 * # ralpthbotApp
 *
 * Main module of the application.
 */
angular
  .module('ralpthbotApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch'
  ])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  });
