'use strict';

angular.module('publicApp')
    .controller('MainCtrl', function ($scope, roverSocket) {
        $scope.rover = function (command) {
            console.log(roverSocket)
            var request = {
                command: command
            }
            roverSocket.comando(request).then(function (res) {
                console.log(res)
                $scope.status = res;
            });
        };
        $scope.$on('ws', function (_, data) {
            console.log('ws ', data)
            $scope.status = data;
        })
        $scope.roverModel = {
            pan: 90,
            tilt: 90,
            arm: 90
        }
        //  $scope.$watch('roverModel.pan', function(pan) {
        //     roverSocket.comando('')
        //  }
        $scope.sliderChange = function (slider) {
            var request = {
                command: slider + 'Move',
                params: [
                    JSON.stringify($scope.roverModel[slider])
                ]
            }
            roverSocket.comando(request)
        }

    });
