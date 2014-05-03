'use strict';

angular.module('publicApp').
    factory('roverSocket', ['$q', '$rootScope', function ($q, $rootScope) {
        // We return this object to anything injecting our service
        var Service = {};
        // Keep all pending requests here until they get responses
        var callbacks = {};
        // Create a unique callback ID to map requests to responses
        var currentCallbackId = 0;
        // Create our websocket object with the address to the websocket
        var url = "ws://" + location.host + "/roverSocket"
        console.log(url)
        var ws = new WebSocket(url);
//        var ws = new WebSocket("ws://localhost:8888/roverSocket");

        ws.onopen = function () {
            console.log("Socket has been opened!");
        };

        ws.onmessage = function (message) {
//            listener(message.data);
            console.log(message)
            console.log(JSON.parse(message.data))
            listener(JSON.parse(message.data));
        };

        function sendRequest(request) {
            var defer = $q.defer();
            var callbackId = getCallbackId();
            callbacks[callbackId] = {
                time: new Date(),
                cb: defer
            };
            request.callback_id = callbackId;
            console.log('Sending request', request);
            ws.send(JSON.stringify(request));
            return defer.promise;
        }

        function listener(data) {
            var messageObj = data;
            console.log("Received data from websocket: ", messageObj);
            // If an object exists with callback_id in our callbacks object, resolve it

            if (messageObj.callback_id === undefined) {
                $rootScope.$broadcast("ws", messageObj)
            } else if (callbacks.hasOwnProperty(messageObj.callback_id)) {
                console.log(callbacks[messageObj.callback_id]);
                $rootScope.$apply(callbacks[messageObj.callback_id].cb.resolve(messageObj.data));
                delete callbacks[messageObj.callbackID];
            }
        }

        // This creates a new callback ID for a request
        function getCallbackId() {
            currentCallbackId += 1;
            if (currentCallbackId > 10000) {
                currentCallbackId = 0;
            }
            return currentCallbackId;
        }

        // Define a "getter" for getting customer data
        Service.comando = function (request) {
            // Storing in a variable for clarity on what sendRequest returns
            console.log(request, request.toString(), JSON.stringify(request))
            return sendRequest(request);
        }

        return Service;
    }]);
// AngularJS will instantiate a singleton by calling "new" on this function

