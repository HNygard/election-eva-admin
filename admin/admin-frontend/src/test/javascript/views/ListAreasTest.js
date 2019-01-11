describe('EVA.View.ListAreas', function () {

	function mockDialogWidget() {
		return PrimeFaces.cw('Dialog', 'mockWidgetVar_' + new Date().getTime(), {
			id: 'mockedDialog'
		});
	}

	it('is available', function () {
		expect(EVA.View.ListAreas).not.toBe(null);
	});

	it('queues onDocumentReady upon instantiation', function () {
		var onDocumentReadySpy = spyOn(EVA.View.ListAreas.prototype, 'onDocumentReady').and.callThrough();
		var attachEventHandlersSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventHandlers').and.callThrough();
		var handleUIDataTableIEFixSpy = spyOn(EVA.View.ListAreas.prototype, 'handleUIDataTableIEFix');
		var attachEventsForContextLevelSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventsForContextLevel');

		var view = new EVA.View.ListAreas();

		expect(onDocumentReadySpy).toHaveBeenCalled();
		expect(attachEventHandlersSpy).toHaveBeenCalled();
		expect(handleUIDataTableIEFixSpy).toHaveBeenCalled();
		expect(attachEventsForContextLevelSpy).toHaveBeenCalled();
	});

	it('attaches correct event elements for context level', function () {

		var dialogMock = mockDialogWidget();
		
		var attachEventsForContextLevelSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventsForContextLevel').and.callThrough();
		var attachEvensForEditWidgetSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEvensForEditWidget');
		var attachEvensForCreateWidgetSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEvensForCreateWidget');
		var widgetSpy = spyOn(EVA.View.ListAreas.prototype, 'widget').and.returnValue(dialogMock);

		var view = new EVA.View.ListAreas();
		
		
		expect(attachEventsForContextLevelSpy).toHaveBeenCalled();
		expect(attachEvensForEditWidgetSpy).toHaveBeenCalledWith(dialogMock);
		expect(attachEvensForCreateWidgetSpy).toHaveBeenCalledWith(dialogMock);
	});
	
	it('resolves correct context-level', function(){
		
		var attachEventsForContextLevelSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventsForContextLevel');
		var attachEventHandlersSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventHandlers').and.callThrough();
		var resolveLevelForElementSpy = spyOn(EVA.View.ListAreas.prototype, 'resolveLevelForElement').and.callThrough();
		var getContextEditorLevelSpy = spyOn(EVA.View.ListAreas.prototype, 'getContextEditorLevel').and.callThrough();
		
		var testElement = $(
				'<div class="context-level" id="list-area-mock-1">' +
					'<div class="mocked-target-element"></div>' +
			'</div>'
		);
		
		testElement.length = 1;


		var view = new EVA.View.ListAreas();
		
		var contextLevel = view.resolveLevelForElement(testElement.find('.mocked-target-element'));
		
//		expect(attachEventHandlersSpy).toHaveBeenCalled();
		expect(attachEventsForContextLevelSpy).toHaveBeenCalled();
		expect(resolveLevelForElementSpy).toHaveBeenCalled();
		expect(getContextEditorLevelSpy).toHaveBeenCalledWith('1');
		expect(contextLevel).toEqual(EVA.View.ListAreas.LEVEL1);
	});
	
	it('attaches events for edit widget', function(){

		var attachEventsForContextLevelSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventsForContextLevel');
		var attachEventHandlersSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventHandlers').and.callThrough();
		var attachEvensForEditWidgetSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEvensForEditWidget').and.callThrough();
		var getFormActions = spyOn(EVA.View.ListAreas.prototype, 'getFormActions').and.returnValue({
			save: $('<button id="fooo:bar1" class="btn-primary"></button>'),
			cancel: $('<button id="fooo:bar2" class="btn-link"></button>'),
			'delete': $('<button id="fooo:bar3" class="btn-danger"></button>')
		});
		
		var view = new EVA.View.ListAreas();
		
		view.attachEvensForEditWidget(mockDialogWidget());

		expect(getFormActions).toHaveBeenCalled();
		expect(attachEventHandlersSpy).toHaveBeenCalled();
		expect(attachEventsForContextLevelSpy).toHaveBeenCalled();
		expect(attachEvensForEditWidgetSpy).toHaveBeenCalled();
	});
	
	
	it('attaches events for create widget', function(){

		var attachEventsForContextLevelSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventsForContextLevel');
		var attachEventHandlersSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEventHandlers').and.callThrough();
		var attachEvensForCreateWidgetSpy = spyOn(EVA.View.ListAreas.prototype, 'attachEvensForCreateWidget').and.callThrough();
		var getFormActions = spyOn(EVA.View.ListAreas.prototype, 'getFormActions').and.returnValue({
			save: $('<button id="fooo:bar1" class="btn-primary"></button>'),
			cancel: $('<button id="fooo:bar2" class="btn-link"></button>'),
			'delete': $('<button id="fooo:bar3" class="btn-danger"></button>')
		});
		

		var view = new EVA.View.ListAreas();
		
		view.attachEvensForCreateWidget(mockDialogWidget());


		expect(getFormActions).toHaveBeenCalled();
		expect(attachEventHandlersSpy).toHaveBeenCalled();
		expect(attachEventsForContextLevelSpy).toHaveBeenCalled();
		expect(attachEvensForCreateWidgetSpy).toHaveBeenCalled();
	});
});
