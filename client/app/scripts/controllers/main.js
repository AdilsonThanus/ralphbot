'use strict';

angular.module('ralpthbotApp')
    .controller('MainCtrl', function ($scope, roverSocket) {
        $scope.rover = function (command) {
            console.log(roverSocket);
            var request = {
                command: command
            };
            roverSocket.comando(request).then(function (res) {
                $scope.status = res;
            });
        };
        $scope.$on('ws', function (_, msg) {
            console.log('ws ', msg);
            $scope.status = msg;
            //$scope.roverModel.pan = status.pan
            //$scope.roverModel.tilt = status.tilt
            //$scope.roverModel.arm = status.arm
        });

        $scope.roverModel = {
            //pan: 90,
            //tilt: 90,
            arm: 90
        };
        //  $scope.$watch('roverModel.pan', function(pan) {
        //     roverSocket.comando('')
        //  }
        $scope.sliderChange = function (slider) {
            var request = {
                command: slider + 'Move',
                params: [
                    JSON.stringify($scope.roverModel[slider])
                ]
            };
            roverSocket.comando(request);
        };
    });
