describe('EVA.View.Counting', function () {

	it('is available', function () {
		expect(EVA.View.Counting).not.toBe(null);
	});

	it('widgets are created at setup', function () {
		var stopSpy = spyOn(EVA.View.Counting.prototype, 'stop').and.callThrough();
		var view = new EVA.View.Counting();
		
		view.setup();
		
		expect(stopSpy.calls.count()).toBe(9);
		expect(view.lateValidationCoversWidget).not.toBe(null);
		expect(view.ballotCountsWidget).not.toBe(null);
		expect(view.ballotCountsWithSplitWidget).not.toBe(null);
		expect(view.markOffCountsWidget).not.toBe(null);
		expect(view.markOffCountsForAllPollingDistrictsWidget).not.toBe(null);
		expect(view.dailyMarkOffCountsWidget).not.toBe(null);
		expect(view.dailyMarkOffsWidget).not.toBe(null);
		expect(view.protocolBallotCountsWidget).not.toBe(null);
		expect(view.bottomButtons).not.toBe(null);
	});

	it('confirm with required comment and initial comment does not disable button', function () {
		dialogFormHTML(CONFIRM_APPROVE_DIALOG_WITH_COMMENT_REQUIRED_AND_INITIAL_COMMENT_HTML);
		var view = new EVA.View.Counting();
		
		view.onConfirmApproveDialogShow();
		var button = view.getYesButton();
		
		expect(view.isCommentEmpty()).toBe(false);
		expect(button.attr('disabled')).toBe(undefined);
		expect(button.attr('aria-disabled')).toBe("false");
		expect(button.hasClass('ui-state-disabled')).toBe(false);
	});

	it('confirm with required comment and no initial comment disables button', function () {
		dialogFormHTML(CONFIRM_APPROVE_DIALOG_WITH_COMMENT_REQUIRED_AND_NO_INITIAL_COMMENT_HTML);
		var view = new EVA.View.Counting();

		view.onConfirmApproveDialogShow();
		var button = view.getYesButton();

		expect(view.isCommentEmpty()).toBe(true);
		expect(button.attr('disabled')).toBe("disabled");
		expect(button.attr('aria-disabled')).toBe("true");
		expect(button.hasClass('ui-state-disabled')).toBe(true);
	});

	it('confirm with required comment and initial comment and removes comment, disables button', function () {
		dialogFormHTML(CONFIRM_APPROVE_DIALOG_WITH_COMMENT_REQUIRED_AND_INITIAL_COMMENT_HTML);
		var view = new EVA.View.Counting();

		view.onConfirmApproveDialogShow();
		enterInputValue(view.getCommentField(), '');
		var button = view.getYesButton();

		expect(view.isCommentEmpty()).toBe(true);
		expect(button.attr('disabled')).toBe("disabled");
		expect(button.attr('aria-disabled')).toBe("true");
		expect(button.hasClass('ui-state-disabled')).toBe(true);
	});

	it('confirm with required comment and no initial comment and adds comment, enables button', function () {
		dialogFormHTML(CONFIRM_APPROVE_DIALOG_WITH_COMMENT_REQUIRED_AND_NO_INITIAL_COMMENT_HTML);
		var view = new EVA.View.Counting();

		view.onConfirmApproveDialogShow();
		enterInputValue(view.getCommentField(), 'Some comment');
		var button = view.getYesButton();

		expect(view.isCommentEmpty()).toBe(false);
		expect(button.attr('disabled')).toBe(undefined);
		expect(button.attr('aria-disabled')).toBe("false");
		expect(button.hasClass('ui-state-disabled')).toBe(false);
	});
});

function dialogFormHTML(html) {
	EVA.View.Counting.prototype.loadDialogForm = function () {
		return $(html);
	};
}

function enterInputValue(input, val) {
	input.val(val);
	var e = $.Event("keyup");
	e.which = 13;
	input.trigger(e);
}

// HTML under hentet fra EVA Admin webapp. Skulle markup endre seg og testene feiler, er det bare å kopiere source kode og lime inn her.
var CONFIRM_APPROVE_DIALOG_WITH_COMMENT_REQUIRED_AND_INITIAL_COMMENT_HTML = '<form id="approveDialog:dialogForm" name="approveDialog:dialogForm" method="post" action="/secure/counting/counting.xhtml?cid=13" class="dialogForm" enctype="application/x-www-form-urlencoded"><input type="hidden" name="approveDialog:dialogForm" value="approveDialog:dialogForm"><div id="approveDialog:dialogForm:msgDialog" class="ui-messages ui-widget msgDialog" aria-live="polite"></div><span class="isCommentRequired" style="display: none">true</span><div class="ui-grid bold"><div class="ui-grid-row"><div class="ui-grid-col-3">Totalt krysset i manntall</div><div class="ui-grid-col-1">44</div></div><div class="ui-grid-row"><div class="ui-grid-col-3">Totalt antall stemmesedler</div><div class="ui-grid-col-1">0</div></div><div class="ui-grid-row"><div class="ui-grid-col-2">Kommentar</div><div class="ui-grid-col-10"><span style="color: red">Kommentar er påkrevd dersom antallet manntallskryss ikke stemmer overens med antallet stemmer</span></div></div><div class="ui-grid-row"><div class="ui-grid-col-12"><textarea id="approveDialog:dialogForm:comment" name="approveDialog:dialogForm:comment" cols="20" rows="3" style="width:100%" class="ui-inputfield ui-inputtextarea ui-widget ui-state-default ui-corner-all ui-inputtextarea-resizable" role="textbox" aria-disabled="false" aria-readonly="false" aria-multiline="true">My comment</textarea></div></div></div><button id="approveDialog:dialogForm:confirmButton" name="approveDialog:dialogForm:confirmButton" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-checkmark"></span><span class="ui-button-text ui-c">Ja</span></button><a id="approveDialog:dialogForm:j_idt580:approveDialogClose" href="#" class="ui-commandlink ui-widget btn btn-link" >Avbryt</a> <input type="hidden" name="javax.faces.ViewState" value="-7845974625095179253:3652193232787600451" autocomplete="off"></form>';
var CONFIRM_APPROVE_DIALOG_WITH_COMMENT_REQUIRED_AND_NO_INITIAL_COMMENT_HTML = '<form id="approveDialog:dialogForm" name="approveDialog:dialogForm" method="post" action="/secure/counting/counting.xhtml?cid=13" class="dialogForm" enctype="application/x-www-form-urlencoded"><input type="hidden" name="approveDialog:dialogForm" value="approveDialog:dialogForm"><div id="approveDialog:dialogForm:msgDialog" class="ui-messages ui-widget msgDialog" aria-live="polite"></div><span class="isCommentRequired" style="display: none">true</span><div class="ui-grid bold"><div class="ui-grid-row"><div class="ui-grid-col-3">Totalt krysset i manntall</div><div class="ui-grid-col-1">44</div></div><div class="ui-grid-row"><div class="ui-grid-col-3">Totalt antall stemmesedler</div><div class="ui-grid-col-1">0</div></div><div class="ui-grid-row"><div class="ui-grid-col-2">Kommentar</div><div class="ui-grid-col-10"><span style="color: red">Kommentar er påkrevd dersom antallet manntallskryss ikke stemmer overens med antallet stemmer</span></div></div><div class="ui-grid-row"><div class="ui-grid-col-12"><textarea id="approveDialog:dialogForm:comment" name="approveDialog:dialogForm:comment" cols="20" rows="3" style="width:100%" class="ui-inputfield ui-inputtextarea ui-widget ui-state-default ui-corner-all ui-inputtextarea-resizable" role="textbox" aria-disabled="false" aria-readonly="false" aria-multiline="true"></textarea></div></div></div><button id="approveDialog:dialogForm:confirmButton" name="approveDialog:dialogForm:confirmButton" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-checkmark"></span><span class="ui-button-text ui-c">Ja</span></button><a id="approveDialog:dialogForm:j_idt580:approveDialogClose" href="#" class="ui-commandlink ui-widget btn btn-link" >Avbryt</a> <input type="hidden" name="javax.faces.ViewState" value="-7845974625095179253:3652193232787600451" autocomplete="off"></form>';
