describe('EVA.Components.Response', function () {

    it('is available', function () {
        expect(EVA.Components.Response).not.toBe(null);
    });

	it('has necessary base methods in prototype', function () {

		var baseMethods = [
			'setPrimeFacesArguments',
			'isValidationFailed',
			'setStatus',
			'getStatus',
			'setComponentId',
			'getComponentId'
		];
		var isMissingMethod = false;

		_.each(baseMethods, function (methodName) {

			if (!(methodName in EVA.Components.Response.prototype)) {
				isMissingMethod = true;
			}
		});

		expect(isMissingMethod).toBeFalse();
	});
	
	it('sets validationFailed flag to false upon construction', function () {
		var response = new EVA.Components.Response();
		expect(response.isValidationFailed()).toBeFalse();
	});
	
	it('sets status correctly', function () {
		var response = new EVA.Components.Response();
		response.setStatus(200);
		expect(response.getStatus()).toEqual(200);
	});
	
	it('sets primefaces response arguments correctly', function () {
		var response = new EVA.Components.Response();
		
		var pfResponseArgFixture = {
			foo:'bar',
			validationFailed:true
		};
		
		response.setPrimeFacesArguments(pfResponseArgFixture);
		expect(response.isValidationFailed()).toBeTrue();
		expect(response.response).toEqual(pfResponseArgFixture);
	});
});
