describe('EVA.View.OperatorAdmin', function () {


	it('is available', function () {
		expect(EVA.View.OperatorAdmin).not.toBe(null);
	});

	it('calls initialize upon instantiation', function () {
		var initializeSpy = spyOn(EVA.View.OperatorAdmin.prototype, 'initialize');
		var view = new EVA.View.OperatorAdmin();

		expect(initializeSpy).toHaveBeenCalled();
	});
	
	it('calls onDocumentReady on initialize',function(){

		var onDocumentReadySpy = spyOn(EVA.View.OperatorAdmin.prototype, 'onDocumentReady');
		var view = new EVA.View.OperatorAdmin();

		expect(onDocumentReadySpy).toHaveBeenCalled();
	});
	
	it('sets correct edit mode',function(){

		var initializeModeSpy = spyOn(EVA.View.OperatorAdmin.prototype, 'initializeMode').and.callThrough();
		var handleEditAndCreateModeSpy = spyOn(EVA.View.OperatorAdmin.prototype, 'handleEditAndCreateMode');
		var view = new EVA.View.OperatorAdmin();

		view.setMode(EVA.View.OperatorAdmin.EDIT);
		
		expect(view.mode).toEqual(EVA.View.OperatorAdmin.EDIT);
		expect(initializeModeSpy).toHaveBeenCalled();
		expect(handleEditAndCreateModeSpy).toHaveBeenCalled();
	});
	
	it('sets correct search mode',function(){

		var initializeModeSpy = spyOn(EVA.View.OperatorAdmin.prototype, 'initializeMode').and.callThrough();
		var handleSearchModeSpy = spyOn(EVA.View.OperatorAdmin.prototype, 'handleSearchMode');
		var view = new EVA.View.OperatorAdmin();

		view.setMode(EVA.View.OperatorAdmin.SEARCH);
		
		expect(view.mode).toEqual(EVA.View.OperatorAdmin.SEARCH);
		expect(initializeModeSpy).toHaveBeenCalled();
		expect(handleSearchModeSpy).toHaveBeenCalled();
	});
	
	
});
