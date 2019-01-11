describe('EVA.View.MarkOffVoter', function () {


	it('is available', function () {
		expect(EVA.View.MarkOffVoter).not.toBe(null);
	});

	it('calls initialize upon instantiation', function () {
		var initializeSpy = spyOn(EVA.View.MarkOffVoter.prototype, 'initialize');
		var view = new EVA.View.MarkOffVoter();

		expect(initializeSpy).toHaveBeenCalled();
	});
	
	it('calls onDocumentReady on initialize',function(){

		var onDocumentReadySpy = spyOn(EVA.View.MarkOffVoter.prototype, 'onDocumentReady');
		var view = new EVA.View.MarkOffVoter();

		expect(onDocumentReadySpy).toHaveBeenCalled();
	});
	
	it('calls attachOnComplete on initialize',function(){

		var attachOnCompleteSpy = spyOn(EVA.View.MarkOffVoter.prototype, 'attachOnComplete');
		var view = new EVA.View.MarkOffVoter();

		expect(attachOnCompleteSpy).toHaveBeenCalled();
	});
	 
});
