describe('EVA.View.ListElections', function () {

	it('is available', function () {
		expect(EVA.View.ListElections).not.toBe(null);
	});

	it('calls initialize upon instantiation', function () {
		var initializeSpy = spyOn(EVA.View.ListElections.prototype, 'initialize');
		var view = new EVA.View.ListElections();

		expect(initializeSpy).toHaveBeenCalled();
	});


});
