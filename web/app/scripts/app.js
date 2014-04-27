'use strict';

angular
    .module('publicApp', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
        'ui.slider'
    ])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: '../views/main.html',
                controller: 'MainCtrl'
            })
            .when('/bot', {
                templateUrl: '../views/bot.html',
                controller: 'MainCtrl'
            })
            .otherwise({
                redirectTo: '/bot'
            });
    });
