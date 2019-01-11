describe('EVA.Widget.Counting', function () {
	
	it('LateValidationCoversWidget created correctly', function () {
		widgetHTML(LATE_VALIDATION_COVERS_HTML);
		
		var widget = new EVA.Widget.LateValidationCoversWidget();
		
		expect(widget.getWidgetName()).toBe("lateValidationCovers");
		expect(widget.getInputs().length).toBe(1);
	});

	it('LateValidationCoversWidget with string input adds error class', function () {
		widgetHTML(LATE_VALIDATION_COVERS_HTML);
		var widget = new EVA.Widget.LateValidationCoversWidget();
		var input = $(widget.getInputs()[0]);
		
		enterInputValue(input, "string");
		
		expect(input.hasClass('ui-state-error')).toBe(true);
	});

	it('LateValidationCoversWidget with negative int adds error class', function () {
		widgetHTML(LATE_VALIDATION_COVERS_HTML);
		var widget = new EVA.Widget.LateValidationCoversWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "-1");

		expect(input.hasClass('ui-state-error')).toBe(true);
	});

	it('LateValidationCoversWidget with valid int calculates new values and fires MARKOFF_TOTAL event', function () {
		widgetHTML(LATE_VALIDATION_COVERS_HTML);
		var notifySpy = spyOn(EVA.Widget.BaseCountingWidget.prototype, "notifyListeners").and.callThrough();
		var widget = new EVA.Widget.LateValidationCoversWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "10");

		expect(input.hasClass('ui-state-error')).toBe(false);
		expect(widget.getTotalField().text()).toBe("-9");
		expect(notifySpy).toHaveBeenCalledWith(EVA.Widget.CountingFieldUpdated.MARKOFF_TOTAL, -9);
	});

	it('BallotCountWidget new value fires BALLOTS_TOTAL event', function () {
		widgetHTML(BALLOT_COUNTS_HTML);
		var notifySpy = spyOn(EVA.Widget.BaseCountingWidget.prototype, "notifyListeners").and.callThrough();
		var widget = new EVA.Widget.BallotCountsWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "10");

		expect(widget.getWidgetName()).toBe("ballotCounts");
		expect(notifySpy).toHaveBeenCalledWith(EVA.Widget.CountingFieldUpdated.BALLOTS_TOTAL, 10);
	});

	it('BallotCountsWithSplitWidget new modified value fires BALLOTS_TOTAL and CORRECTIONS events', function () {
		widgetHTML(BALLOT_COUNTS_WITH_SPLIT_HTML);
		var notifySpy = spyOn(EVA.Widget.BaseCountingWidget.prototype, "notifyListeners").and.callThrough();
		var widget = new EVA.Widget.BallotCountsWithSplitWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "20");

		expect(widget.getWidgetName()).toBe("ballotCountsWithSplit");
		expect(notifySpy).toHaveBeenCalledWith(EVA.Widget.CountingFieldUpdated.BALLOTS_TOTAL, 20);
		expect(notifySpy).toHaveBeenCalledWith(EVA.Widget.CountingFieldUpdated.CORRECTIONS, 20);
	});

	it('MarkOffCountsWidget with BALLOTS_TOTAL field update updates ballot and diff field', function () {
		widgetHTML(MARKOFF_COUNTS_HTML);
		var widget = new EVA.Widget.MarkOffCountsWidget();

		widget.onFieldUpdate(EVA.Widget.CountingFieldUpdated.BALLOTS_TOTAL, 10);

		expect(widget.getWidgetName()).toBe("markOffCounts");
		expect(widget.getBallotField().text()).toBe("10");
		expect(widget.getDiffField().text()).toBe("9");
		expect(widget.getDiffField().hasClass("diff-pos")).toBe(true);
	});

	it('MarkOffCountsWidget with MARKOFF_TOTAL field update updates markoff and diff field', function () {
		widgetHTML(MARKOFF_COUNTS_HTML);
		var widget = new EVA.Widget.MarkOffCountsWidget();

		widget.onFieldUpdate(EVA.Widget.CountingFieldUpdated.MARKOFF_TOTAL, 5);

		expect(widget.getMarkOffsField().text()).toBe("5");
		expect(widget.getDiffField().text()).toBe("-5");
		expect(widget.getDiffField().hasClass("diff-neg")).toBe(true);
	});

	it('MarkOffCountsWidget with expected input updates diff', function () {
		widgetHTML(MARKOFF_COUNTS_WITH_EXPECTED_HTML);
		var widget = new EVA.Widget.MarkOffCountsWidget();
		var input = $(widget.getInputs()[0]);
		
		enterInputValue(input, "5");

		expect(widget.getDiffField().text()).toBe("-5");
		expect(widget.getDiffField().hasClass("diff-neg")).toBe(true);
	});

	it('MarkOffCountsForAllPollingDistrictsWidget with BALLOTS_TOTAL field update updates votes field and calculates diff', function () {
		widgetHTML(MARKOFF_COUNTS_FOR_ALL_POLLING_DISTRICTS_HTML);
		var widget = new EVA.Widget.MarkOffCountsForAllPollingDistrictsWidget();

		widget.onFieldUpdate(EVA.Widget.CountingFieldUpdated.BALLOTS_TOTAL, 10);

		expect(widget.getWidgetName()).toBe("markOffCountsForAllPollingDistricts");
		expect(widget.getVotesField().text()).toBe("10");
		expect(widget.getDiffField().text()).toBe("-7960");
		expect(widget.getDiffField().hasClass("diff-neg")).toBe(true);
	});

	it('MarkOffCountsForAllPollingDistrictsWidget with MARKOFF_TOTAL field update updates markoff field and calculates diff', function () {
		widgetHTML(MARKOFF_COUNTS_FOR_ALL_POLLING_DISTRICTS_HTML);
		var widget = new EVA.Widget.MarkOffCountsForAllPollingDistrictsWidget();

		widget.onFieldUpdate(EVA.Widget.CountingFieldUpdated.MARKOFF_TOTAL, 10);

		expect(widget.getMarkOffField().text()).toBe("10");
		expect(widget.getDiffField().text()).toBe("-10");
		expect(widget.getDiffField().hasClass("diff-neg")).toBe(true);
	});

	it('DailyMarkOffCountsWidget input change updates sum and fires MARKOFF_TOTAL event', function () {
		widgetHTML(DAILY_MARKOFF_COUNTS_HTML);
		var notifySpy = spyOn(EVA.Widget.BaseCountingWidget.prototype, "notifyListeners").and.callThrough();
		var widget = new EVA.Widget.DailyMarkOffCountsWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "20");

		expect(widget.getWidgetName()).toBe("dailyMarkOffCounts");
		expect(widget.getTotalField().text()).toBe("20");
		expect(notifySpy).toHaveBeenCalledWith(EVA.Widget.CountingFieldUpdated.MARKOFF_TOTAL, 20);
	});

	it('DailyMarkOffsWidget input change updates sum', function () {
		widgetHTML(DAILY_MARKOFFS_HTML);
		var widget = new EVA.Widget.DailyMarkOffsWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "9");

		expect(widget.getWidgetName()).toBe("dailyMarkOffs");
		expect($(widget.getContent()).find('.ui-datatable table tfoot tr td span').eq(0).text()).toBe("9");
	});

	it('ProtocolBallotCountsWidget input change updates sum', function () {
		widgetHTML(PROTOCOL_BALLOT_COUNTS_HTML);
		var widget = new EVA.Widget.ProtocolBallotCountsWidget();
		var input = $(widget.getInputs()[0]);

		enterInputValue(input, "4");

		expect(widget.getWidgetName()).toBe("protocolBallotCounts");
		expect(widget.getTotalField().text()).toBe("4");
	});

	it('BottomButtonsWidget with zero CORRECTIONS field update toggles button correctly', function () {
		widgetHTML(BOTTOM_BUTTONS_HTML);
		var widget = new EVA.Widget.BottomButtonsWidget();

		widget.onFieldUpdate(EVA.Widget.CountingFieldUpdated.CORRECTIONS, 0);

		expect(widget.getWidgetName()).toBe("bottomButtons");
		expect(widget.getCorrectionsButton().is(':hidden')).toBe(true);
		expect(widget.getDoneButton().css("display")).toBe("inline-block");
	});

	it('BottomButtonsWidget with positive CORRECTIONS field update toggles button correctly', function () {
		widgetHTML(BOTTOM_BUTTONS_HTML);
		var widget = new EVA.Widget.BottomButtonsWidget();

		widget.onFieldUpdate(EVA.Widget.CountingFieldUpdated.CORRECTIONS, 10);

		expect(widget.getDoneButton().is(':hidden')).toBe(true);
		expect(widget.getCorrectionsButton().css("display")).toBe("inline-block");
	});
});

function enterInputValue(input, val) {
	input.val(val);
	var e = $.Event("keyup");
	e.which = 13;
	input.trigger(e);
}

function widgetHTML(html) {
	EVA.Widget.BaseCountingWidget.prototype.getActivePanel = function () {
		return $(wrapWidget(html));
	};
}

function wrapWidget(html) {
	return '<form id="countingForm"><div class="ui-tabs-panel" aria-hidden="false">' + html + '</div></form>';
}

// HTML under hentet fra EVA Admin webapp. Skulle markup endre seg og testene feiler, er det bare å kopiere source kode og lime inn her.
var LATE_VALIDATION_COVERS_HTML = '<div class="row" data-counting-widget="lateValidationCovers"><div class="col-md-12"><h4>Lagt til side</h4><span class="countCategory" style="display:none;">FO</span><div id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel" class="ui-datatable ui-widget" style="width: 50%;"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel_head"><tr role="row"><th id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel:j_idt91" class="ui-state-default" role="columnheader"><span class="ui-column-title"></span></th><th id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel:j_idt94" class="ui-state-default" role="columnheader"><span class="ui-column-title">Antall</span></th></tr></thead><tfoot id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel_foot"><tr><td class="ui-state-default"><span class="bold">Total</span></td><td class="ui-state-default"><span id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel:lateValidationCoversTotal" class="bold lateValidationCoversTotal">1</span></td></tr></tfoot><tbody id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_markoff_count" role="row"><td role="gridcell">Manntallsavkrysninger</td><td role="gridcell"><span data-aft="markoff_count">1</span></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_late_validation_covers" role="row"><td role="gridcell">Antall lagt til side for telling sammen med sent innkomne</td><td role="gridcell"><input id="countingForm:tabView:lateValidationCovers:lateValidationCoversModel:1:lateValidationCoversModelCount" type="text" name="countingForm:tabView:lateValidationCovers:lateValidationCoversModel:1:lateValidationCoversModelCount" value="0" class="form-control" size="4"></td></tr></tbody></table></div></div></div> </div>';
var BALLOT_COUNTS_HTML = '<div class="row" data-counting-widget="ballotCounts"><div class="col-md-12"><h4>Registrere foreløpig telling</h4><div id="countingForm:tabView:ballotCounts:ballotCountsTable" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:ballotCounts:ballotCountsTable_head"><tr role="row"><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt98" class="ui-state-default" role="columnheader"><span class="ui-column-title">Parti</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt101" class="ui-state-default" role="columnheader"><span class="ui-column-title">ID</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt106" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Stemmer</span></th></tr></thead><tfoot id="countingForm:tabView:ballotCounts:ballotCountsTable_foot"><tr><td class="ui-state-default">Totalt antall stemmesedler</td><td class="ui-state-default"></td><td class="ui-state-default col-votes"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:totalBallotCount">0</span></td></tr></tfoot><tbody id="countingForm:tabView:ballotCounts:ballotCountsTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ballot row_ballot_ELP" role="row"><td role="gridcell"><span class="">Ellinor-Lina partiet</span></td><td role="gridcell"><span class="">ELP</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:0:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:0:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_ballot row_ballot_H" role="row"><td role="gridcell"><span class="">Høyre</span></td><td role="gridcell"><span class="">H</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:1:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:1:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_ballot row_ballot_V" role="row"><td role="gridcell"><span class="">Venstre</span></td><td role="gridcell"><span class="">V</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:2:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:2:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_total_ballot_count" role="row"><td role="gridcell"><span class="bold">Stemmesedler til fordeling totalt</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:3:unmodifiedBallotCountText" class="bold">0</span></td></tr><tr data-ri="4" class="ui-widget-content ui-datatable-even row_blank" role="row"><td role="gridcell"><span class="">Blanke stemmer</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:4:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:4:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="5" class="ui-widget-content ui-datatable-odd row_questionable" role="row"><td role="gridcell"><span class="">Tvilsomme sedler</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:5:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:5:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr></tbody></table></div></div></div></div>';
var BALLOT_COUNTS_WITH_SPLIT_HTML = '<div class="row" data-counting-widget="ballotCountsWithSplit"><div class="col-md-12"><h4>Registrere endelig telling #1</h4><div id="countingForm:tabView:j_idt155:ballotCountsTable" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:j_idt155:ballotCountsTable_head"><tr role="row"><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt157" class="ui-state-default" role="columnheader"><span class="ui-column-title">Parti</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt160" class="ui-state-default" role="columnheader"><span class="ui-column-title">ID</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt165" class="ui-state-default col-modified" role="columnheader"><span class="ui-column-title">Rettet</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt167" class="ui-state-default col-unmodified" role="columnheader"><span class="ui-column-title">Urettet</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt168" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Total</span></th></tr></thead><tfoot id="countingForm:tabView:j_idt155:ballotCountsTable_foot"><tr><td class="ui-state-default">Totalt antall stemmesedler</td><td class="ui-state-default"></td><td class="ui-state-default col-modified"></td><td class="ui-state-default col-unmodified"></td><td class="ui-state-default col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:totalBallotCount">0</span></td></tr></tfoot><tbody id="countingForm:tabView:j_idt155:ballotCountsTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ballot row_ballot_ELP" role="row"><td role="gridcell"><span class="">Ellinor-Lina partiet</span></td><td role="gridcell"><span class="">ELP</span></td><td role="gridcell" class="col-modified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:0:modifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:0:modifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control modified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-unmodified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:0:unmodifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:0:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control unmodified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:0:ballotCountText" class="">0</span></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_ballot row_ballot_H" role="row"><td role="gridcell"><span class="">Høyre</span></td><td role="gridcell"><span class="">H</span></td><td role="gridcell" class="col-modified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:1:modifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:1:modifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control modified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-unmodified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:1:unmodifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:1:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control unmodified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:1:ballotCountText" class="">0</span></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_ballot row_ballot_V" role="row"><td role="gridcell"><span class="">Venstre</span></td><td role="gridcell"><span class="">V</span></td><td role="gridcell" class="col-modified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:2:modifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:2:modifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control modified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-unmodified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:2:unmodifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:2:unmodifiedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control unmodified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:2:ballotCountText" class="">0</span></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_total_ballot_count" role="row"><td role="gridcell"><span class="bold">Stemmesedler til fordeling totalt</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-modified"><span id="countingForm:tabView:j_idt155:ballotCountsTable:3:modifiedBallotCountText" class="bold">0</span></td><td role="gridcell" class="col-unmodified"><span id="countingForm:tabView:j_idt155:ballotCountsTable:3:unmodifiedBallotCountText" class="bold">0</span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:3:ballotCountText" class="bold">0</span></td></tr><tr data-ri="4" class="ui-widget-content ui-datatable-even row_blank" role="row"><td role="gridcell"><span class="">Blanke stemmer</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:4:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:4:ballotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="5" class="ui-widget-content ui-datatable-odd row_header" role="row"><td role="gridcell"><span class="bold">Foreslått forkastede</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"></td></tr><tr data-ri="6" class="ui-widget-content ui-datatable-even row_rejected row_rejected_FA" role="row"><td role="gridcell"><span class="">Mangler off. stempel (FA)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:6:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:6:ballotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="7" class="ui-widget-content ui-datatable-odd row_rejected row_rejected_FB" role="row"><td role="gridcell"><span class="">Fremgår ikke hvilket valg (FB)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:7:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:7:ballotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="8" class="ui-widget-content ui-datatable-even row_rejected row_rejected_FC" role="row"><td role="gridcell"><span class="">Fremgår ikke hvilken liste (FC)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:8:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:8:ballotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="9" class="ui-widget-content ui-datatable-odd row_rejected row_rejected_FD" role="row"><td role="gridcell"><span class="">Parti/gruppe stiller ikke liste (FD)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:9:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:9:ballotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="10" class="ui-widget-content ui-datatable-even row_total_rejected_ballot_count" role="row"><td role="gridcell"><span class="bold">Totalt foreslått forkastede</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:10:ballotCountText" class="bold">0</span></td></tr></tbody></table></div></div></div></div>';
var MARKOFF_COUNTS_HTML = '<div class="row" data-counting-widget="markOffCounts"><div class="col-md-12"><div id="countingForm:tabView:markOffCounts:markOffCountsModel" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:markOffCounts:markOffCountsModel_head"><tr role="row"><th id="countingForm:tabView:markOffCounts:markOffCountsModel:j_idt126" class="ui-state-default col-markOffs" role="columnheader"><span class="ui-column-title">Kryss i manntall</span></th><th id="countingForm:tabView:markOffCounts:markOffCountsModel:j_idt129" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Sedler</span></th><th id="countingForm:tabView:markOffCounts:markOffCountsModel:j_idt130" class="ui-state-default col-diff" role="columnheader"><span class="ui-column-title">Avvik</span></th></tr></thead><tfoot id="countingForm:tabView:markOffCounts:markOffCountsModel_foot"></tfoot><tbody id="countingForm:tabView:markOffCounts:markOffCountsModel_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even" role="row"><td role="gridcell" class="col-markOffs"><span id="countingForm:tabView:markOffCounts:markOffCountsModel:0:totalMarkOffCount" class="bold">1</span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:markOffCounts:markOffCountsModel:0:totalBallotCount" class="bold">0</span></td><td role="gridcell" class="col-diff"><span id="countingForm:tabView:markOffCounts:markOffCountsModel:0:totalBallotCountDifferenceFromPreviousCount" class="bold diff-neg">-1</span></td></tr></tbody></table></div></div></div></div>';
var MARKOFF_COUNTS_WITH_EXPECTED_HTML = '<div class="row" data-counting-widget="markOffCounts"><div class="col-md-12"><div id="countingForm:tabView:markOffCounts:markOffCountsModel" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:markOffCounts:markOffCountsModel_head"><tr role="row"><th id="countingForm:tabView:markOffCounts:markOffCountsModel:j_idt127" class="ui-state-default col-expected" role="columnheader"><span class="ui-column-title">Antall stemmesedler i denne kretsen</span></th><th id="countingForm:tabView:markOffCounts:markOffCountsModel:j_idt129" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Sedler</span></th><th id="countingForm:tabView:markOffCounts:markOffCountsModel:j_idt131" class="ui-state-default col-diff-expected" role="columnheader"><span class="ui-column-title">Avvik</span></th></tr></thead><tfoot id="countingForm:tabView:markOffCounts:markOffCountsModel_foot"></tfoot><tbody id="countingForm:tabView:markOffCounts:markOffCountsModel_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even" role="row"><td role="gridcell" class="col-expected"><input id="countingForm:tabView:markOffCounts:markOffCountsModel:0:expectedBallotCount" name="countingForm:tabView:markOffCounts:markOffCountsModel:0:expectedBallotCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control" role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:markOffCounts:markOffCountsModel:0:totalBallotCount" class="bold">0</span></td><td role="gridcell" class="col-diff-expected"><span id="countingForm:tabView:markOffCounts:markOffCountsModel:0:expectedBallotCountDifference" class="bold diff-zero">0</span></td></tr></tbody></table></div></div></div></div>';
var MARKOFF_COUNTS_FOR_ALL_POLLING_DISTRICTS_HTML = '<div class="row" data-counting-widget="markOffCountsForAllPollingDistricts"><span class="totalBallotCountForOtherPollingDistricts" style="display:none;">0</span><div class="col-md-12"><div id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts_head"><tr role="row"><th id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts:j_idt135" class="ui-state-default col-markOffs" role="columnheader"><span class="ui-column-title">Kryss i manntall</span></th><th id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts:j_idt136" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Antall stemmesedler registrert for alle kretser</span></th><th id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts:j_idt137" class="ui-state-default col-diff" role="columnheader"><span class="ui-column-title">Avvik</span></th></tr></thead><tfoot id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts_foot"></tfoot><tbody id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even" role="row"><td role="gridcell" class="col-markOffs"><span id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts:0:totalMarkOffCount" class="bold">7970</span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts:0:totalBallotCountForAllPollingDistricts" class="bold">0</span></td><td role="gridcell" class="col-diff"><span id="countingForm:tabView:markOffCountsForAllPollingDistricts:markOffCountsModelForAllPollingDistricts:0:totalBallotCountDifferenceForAllPollingDistricts" class="bold diff-neg">-7970</span></td></tr></tbody></table></div></div></div></div>';
var DAILY_MARKOFF_COUNTS_HTML = '<div class="row" data-counting-widget="dailyMarkOffCounts"><div class="col-md-12"><h4>Urnetelling</h4><div id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts" class="ui-datatable ui-widget" style="width: 50%;"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts_head"><tr role="row"><th id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:j_idt550" class="ui-state-default" role="columnheader"><span class="ui-column-title">Avkrysninger i manntall</span></th><th id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:j_idt553" class="ui-state-default col-markoff" role="columnheader"><span class="ui-column-title">Antall</span></th></tr></thead><tfoot id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts_foot"><tr><td class="ui-state-default"><span class="bold">Total</span></td><td class="ui-state-default col-markoff"><span id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:dailyMarkOffTotal" class="bold">0</span></td></tr></tfoot><tbody id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_daily_markoff_count" role="row"><td role="gridcell">søndag 7 sep. 2014</td><td role="gridcell" class="col-markoff"><input id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:0:dailyMarkOffCountsCount" name="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:0:dailyMarkOffCountsCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control" role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_daily_markoff_count" role="row"><td role="gridcell">mandag 8 sep. 2014</td><td role="gridcell" class="col-markoff"><input id="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:1:dailyMarkOffCountsCount" name="countingForm:tabView:dailyMarkOffCounts:dailyMarkOffCounts:1:dailyMarkOffCountsCount" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control" role="textbox" aria-disabled="false" aria-readonly="false"></td></tr></tbody></table></div></div></div></div>';
var DAILY_MARKOFFS_HTML = '<div data-counting-widget="dailyMarkOffs"><div id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts_head"><tr role="row"><th id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:j_idt378" class="ui-state-default" role="columnheader"><span class="ui-column-title">Ordinære krysset i manntall</span></th><th id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:j_idt381" class="ui-state-default col-markoff" role="columnheader"><span class="ui-column-title">Antall</span></th></tr></thead><tfoot id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts_foot"><tr><td class="ui-state-default"><label class="bold">Totalt krysset i manntall</label></td><td class="ui-state-default col-markoff"><span id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:totalMarkOffCount" class="bold">0</span></td></tr></tfoot><tbody id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even" role="row"><td role="gridcell">07.09.2014</td><td role="gridcell" class="col-markoff"><input id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:0:j_idt382" name="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:0:j_idt382" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all" role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd" role="row"><td role="gridcell">08.09.2014</td><td role="gridcell" class="col-markoff"><input id="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:1:j_idt382" name="countingForm:tabView:dailyMarkOffsView:dailyMarkOffsTable:dailyMarkOffCounts:1:j_idt382" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all" role="textbox" aria-disabled="false" aria-readonly="false"></td></tr></tbody></table></div></div></div>';
var PROTOCOL_BALLOT_COUNTS_HTML = '<div data-counting-widget="protocolBallotCounts"><div id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped"><thead id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable_head"><tr role="row"><th id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:j_idt366" class="ui-state-default" role="columnheader"><span class="ui-column-title">Stemmesedler i urne</span></th><th id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:j_idt369" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Antall</span></th></tr></thead><tfoot id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable_foot"><tr><td class="ui-state-default"><label class="bold">Totalt i urnene</label></td><td class="ui-state-default col-votes"><span id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:totalBallotCount" class="bold">0</span></td></tr></tfoot><tbody id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ordinary" role="row"><td role="gridcell"><label class="bold">Ordinære</label></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:0:j_idt370" name="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:0:j_idt370" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all count" role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_questionable" role="row"><td role="gridcell"><label class="bold">Tvilsomme</label></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:1:j_idt370" name="countingForm:tabView:protocolBallotCounts:protocolBallotCountsTable:1:j_idt370" type="text" value="0" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all count" role="textbox" aria-disabled="false" aria-readonly="false"></td></tr></tbody></table></div></div></div>';
var BOTTOM_BUTTONS_HTML = '<div data-counting-widget="bottomButtons"><div id="countingForm:tabView:j_idt172:j_idt174" class="ui-panel ui-widget ui-widget-content ui-corner-all form-actions form-group" data-widget="widget_countingForm_tabView_j_idt172_j_idt174"><div id="countingForm:tabView:j_idt172:j_idt174_content" class="ui-panel-content ui-widget-content"><button id="countingForm:tabView:j_idt172:j_idt175" name="countingForm:tabView:j_idt172:j_idt175" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary saveCountAndRegisterCountCorrections" style="display:none;" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-caret"></span><span class="ui-button-text ui-c">Registrer rettede stemmesedler</span></button><button id="countingForm:tabView:j_idt172:j_idt179" name="countingForm:tabView:j_idt172:j_idt179" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary saveCount" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-download"></span><span class="ui-button-text ui-c">Lagre</span></button><button id="countingForm:tabView:j_idt172:done" name="countingForm:tabView:j_idt172:done" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary modifiedBallotProcessed" style="" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-caret"></span><span class="ui-button-text ui-c">Ferdig</span></button></div></div></div>';
