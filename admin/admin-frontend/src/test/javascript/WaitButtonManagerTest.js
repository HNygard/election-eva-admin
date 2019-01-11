describe('EVA.Components.WaitButtonManager', function () {

	function createButtonFixture(buttonId) {
		return $(document.createElement('button'))
			.attr('id', buttonId)
			.append('<span class="ui-icon"></span>');
	}

	function deleteButtonFixture(buttonId) {
		$('#' + buttonId).remove();
	}

	it('is available', function () {
		expect(EVA.Components.WaitButtonManager).not.toBe(null);
	});

	it('has necessary base methods in prototype', function () {

		var baseMethods = [
			'attachButton',
			'enableButton',
			'disableButton'
		];
		var isMissingMethod = false;

		_.each(baseMethods, function (methodName) {

			if (!(methodName in EVA.Components.WaitButtonManager.prototype)) {
				isMissingMethod = true;
			}
		});

		expect(isMissingMethod).toBeFalse();
	});

	it('disables a button', function () {

		var buttonId = 'myButton';
		var button = createButtonFixture(buttonId);
		var onStartSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnStart');
		var onCompleteSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnComplete');

		$('body').append(button);

		var waitButtonManager = new EVA.Components.WaitButtonManager();

		waitButtonManager.attachButton(buttonId);

		expect(waitButtonManager.handledButtons[buttonId]).not.toBeUndefined();
		expect(onStartSpy).toHaveBeenCalled();
		expect(onCompleteSpy).toHaveBeenCalled();

		waitButtonManager.disableButton(buttonId);

		expect(button.is(':disabled')).toBeTrue();
		expect(button.find('.ui-icon').hasClass(EVA.Components.WaitButtonManager.PLEASE_WAIT_CSS_CLASS)).toBeTrue();

		deleteButtonFixture(buttonId);
	});

	it('enables a button', function () {

		var buttonId = 'myButton';
		var button = createButtonFixture(buttonId);
		var onStartSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnStart');
		var onCompleteSpy = spyOn(EVA.Components.RequestBroker.prototype, 'attachOnComplete');

		$('body').append(button);

		var waitButtonManager = new EVA.Components.WaitButtonManager();

		waitButtonManager.attachButton(buttonId);

		expect(waitButtonManager.handledButtons[buttonId]).not.toBeUndefined();
		expect(onStartSpy).toHaveBeenCalled();
		expect(onCompleteSpy).toHaveBeenCalled();

		waitButtonManager.disableButton(buttonId);
		waitButtonManager.enableButton(buttonId);

		expect(button.is(':disabled')).toBeFalse();
		expect(button.find('.ui-icon').hasClass(EVA.Components.WaitButtonManager.PLEASE_WAIT_CSS_CLASS)).toBeFalse();

		deleteButtonFixture(buttonId);
	});

});
