describe('EVA.View.SearchElectoralRoll', function () {


	it('is available', function () {
		expect(EVA.View.SearchElectoralRoll).not.toBe(null);
	});

	it('calls initialize upon instantiation', function () {
		var initializeSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'initialize');
		var view = new EVA.View.SearchElectoralRoll();

		expect(initializeSpy).toHaveBeenCalled();
	});
	
	it('calls onDocumentReady on initialize',function(){

		var onDocumentReadySpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'onDocumentReady').and.callThrough();
		var setupTabsSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'setupTabs');
		var toggleEnabledSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'toggleEnabled');
		var setupSubmitOnEnterSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'setupSubmitOnEnter');
		
		var view = new EVA.View.SearchElectoralRoll();

		expect(onDocumentReadySpy).toHaveBeenCalled();
		expect(setupTabsSpy).toHaveBeenCalled();
		expect(setupSubmitOnEnterSpy).toHaveBeenCalled();
		expect(toggleEnabledSpy).toHaveBeenCalled();
	});
	
	it('sets up toggleEnabled on tabShow', function(){

		
		PrimeFaces.cw('TabView','tabViewWidget',{
			id:'tabViewWidget'
		});
		
		
		
		var getTabViewWidgetSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'getTabViewWidget').and.callThrough();
		var toggleEnabledSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'toggleEnabled');
		var setupTabsSpy = spyOn(EVA.View.SearchElectoralRoll.prototype, 'setupTabs').and.callThrough();

		var view = new EVA.View.SearchElectoralRoll();
		var tabsWidget = view.widget('tabViewWidget');

		tabsWidget.cfg.onTabShow(0);
		
		expect(setupTabsSpy).toHaveBeenCalled();
		expect(getTabViewWidgetSpy).toHaveBeenCalled();
		expect(toggleEnabledSpy).toHaveBeenCalled();
	});
	
	
	
	
});
