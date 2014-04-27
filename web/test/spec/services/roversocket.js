'use strict';

describe('Service: Roversocket', function () {

  // load the service's module
  beforeEach(module('publicApp'));

  // instantiate service
  var Roversocket;
  beforeEach(inject(function (_Roversocket_) {
    Roversocket = _Roversocket_;
  }));

  it('should do something', function () {
    expect(!!Roversocket).toBe(true);
  });

});
