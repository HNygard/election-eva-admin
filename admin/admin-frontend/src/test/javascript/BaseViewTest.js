describe('EVA.Components.BaseView', function () {

	it('is available', function () {
		expect(EVA.Components.BaseView).not.toBe(null);
	});

	it('has necessary base methods in prototype', function () {

		var baseMethods = [
			'widget',
			'resolveWidget',
			'onDocumentReady',
			'attachOnComplete',
			'attachOnStart',
			'attachOnError',
			'escapeClientId',
			'getRequestBroker'
		];
		var isMissingMethod = false;

		_.each(baseMethods, function (methodName) {

			if (!(methodName in EVA.Components.BaseView.prototype)) {
				isMissingMethod = true;
			}
		});

		expect(isMissingMethod).toBeFalse();
	});
	
	
	it('triggers document ready with readystate complete', function(){

		var method = {stub: function () {}};
		var methodSpy = spyOn(method, 'stub');
		
		var view = new EVA.Components.BaseView();
		
		view.onDocumentReady(function(){
			method.stub();
		});
		
		expect(methodSpy).toHaveBeenCalled();
	});

	it('retrieves a primefaces widget with widgetVar', function () {
		var view = new EVA.Components.BaseView();
		var widgetVar = "fixtureDialog";
		PrimeFaces.cw("Dialog", widgetVar, {
			id: "test",
			widgetVar: widgetVar,
			draggable: false,
			resizable: false,
			modal: true
		});

		var dialogExpectation = expect(PrimeFaces.widgets[widgetVar]);

		dialogExpectation.not.toBeUndefined();
		dialogExpectation.not.toBeNull();

		var resolvedWidget = view.widget(widgetVar);

		expect(PrimeFaces.widgets[widgetVar]).toEqual(resolvedWidget);
		expect(PrimeFaces.widgets[widgetVar]).toEqual(PF(widgetVar));
	});

	it('retrieves a primefaces widget with a element scoped into a widget that has a data-widgetVar attribute', function () {
		var view = new EVA.Components.BaseView();
		var widgetVar = "fixtureDialog";
		var widgetId = "test:" + new Date().getTime();
		PrimeFaces.cw("Dialog", widgetVar, {
			id: "test",
			widgetVar: widgetVar,
			draggable: false,
			resizable: false,
			modal: true
		});

		var dialogMarkupShim = $('<div id="' + widgetId + '" data-widgetVar="' + widgetVar + '">' +
			'<a class="test-link"> im a test link</a>' +
			'</div>');

		var dialogExpectation = expect(PrimeFaces.widgets[widgetVar]);

		dialogExpectation.not.toBeUndefined();
		dialogExpectation.not.toBeNull();

		var resolvedWidget = view.resolveWidget(dialogMarkupShim.find('.test-link').get(0));

		expect(PrimeFaces.widgets[widgetVar]).toEqual(resolvedWidget);
		expect(PrimeFaces.widgets[widgetVar]).toEqual(PF(widgetVar));

	});

	it('retrieves a primefaces widget with a event object that has a source attribute', function () {
		var view = new EVA.Components.BaseView();
		var widgetVar = "fixtureDialog";
		var widgetId = "test:" + new Date().getTime();
		PrimeFaces.cw("Dialog", widgetVar, {
			id: widgetId,
			widgetVar: widgetVar,
			draggable: false,
			resizable: false,
			modal: true
		});

		var dialogMarkupShim = $('<div id="' + widgetId + '" data-widgetVar="' + widgetVar + '">' +
			'<a class="test-link"> im a test link</a>' +
			'</div>');

		dialogMarkupShim.appendTo('body');

		var dialogExpectation = expect(PrimeFaces.widgets[widgetVar]);

		dialogExpectation.not.toBeUndefined();
		dialogExpectation.not.toBeNull();

		var eventObjectShim = {
			source: widgetId
		};

		var resolvedWidget = view.resolveWidget(eventObjectShim);

		expect(PrimeFaces.widgets[widgetVar]).toEqual(resolvedWidget);
		expect(PrimeFaces.widgets[widgetVar]).toEqual(PF(widgetVar));

	});

	it('escapes clientIds correctly', function () {

		var view = new EVA.Components.BaseView();
		expect(view.escapeClientId('foo:bar')).toEqual('#foo\\:bar');
	});

	it('can attach on error events for ajax components', function () {

		var onErrorSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnError');
		var view = new EVA.Components.BaseView();
		var noOp = function(){};
		var componentId = 'foo:bar';
		view.attachOnError(componentId, noOp);
		
		expect(onErrorSpy).toHaveBeenCalledWith(componentId, noOp, false);
	});
	
	it('can attach on success events for ajax components', function () {

		var onErrorSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnSuccess');
		var view = new EVA.Components.BaseView();
		var noOp = function(){};
		var componentId = 'foo:bar';
		view.attachOnSuccess(componentId, noOp);
		
		expect(onErrorSpy).toHaveBeenCalledWith(componentId, noOp, false);
	});
	
	
	it('can attach on start events for ajax components', function () {

		var onStartSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnStart');
		var view = new EVA.Components.BaseView();
		var noOp = function(){};
		var componentId = 'foo:bar';
		view.attachOnStart(componentId, noOp);
		
		expect(onStartSpy).toHaveBeenCalledWith(componentId, noOp, false);
	});
	
	it('can attach on complete events for ajax components', function () {

		var onCompleteSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnComplete');
		var view = new EVA.Components.BaseView();
		var noOp = function(){};
		var componentId = 'foo:bar';
		view.attachOnComplete(componentId, noOp);
		
		expect(onCompleteSpy).toHaveBeenCalledWith(componentId, noOp, false);
	});
	
	it('creates a waitButtonManager on initialize', function () {
		var waitButtonInstanceSpy = spyOn(EVA.Components.WaitButtonManager, 'createInstance').and.callThrough();;
		var view = new EVA.Components.BaseView();
		expect(waitButtonInstanceSpy).toHaveBeenCalled();
		expect(view.waitButtonManager instanceof EVA.Components.WaitButtonManager).toBeTrue();
	});
});
