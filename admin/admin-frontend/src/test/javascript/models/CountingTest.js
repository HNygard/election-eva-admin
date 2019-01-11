describe('EVA.Model.Counting', function () {

	it('BALLOT_COUNTS_WITHOUT_PROTOCOL_COUNTS_HTML created correctly', function () {
		var model = new EVA.Model.BallotCountsModel();

		addAllRows(model, BALLOT_COUNTS_WITHOUT_PROTOCOL_COUNTS_HTML);
		
		expect(model.getBallotRows().length).toBe(3);
		expect(model.getBallotRows()[0].count.getValue()).toBe(1);
		expect(model.getBallotRows()[1].count.getValue()).toBe(3);
		expect(model.getBallotRows()[2].count.getValue()).toBe(2);
		expect(model.getBallotRowsTotal().count.getValue()).toBe(6);
		expect(model.getBlankRow().count.getValue()).toBe(2);
		expect(model.getQuestionableRow().count.getValue()).toBe(3);
		expect(model.getRejectedRows().length).toBe(0);
		expect(model.getRejectedRowsTotal().count.exists()).toBe(false);
		expect(model.getTotalRow().count.getValue()).toBe(11);
	});

	it('BALLOT_COUNTS_WITHOUT_PROTOCOL_COUNTS_HTML calculated correctly', function () {
		var model = new EVA.Model.BallotCountsModel();
		addAllRows(model, BALLOT_COUNTS_WITHOUT_PROTOCOL_COUNTS_HTML);
		model.getBallotRows()[0].count.jq.val(3);
		model.getBallotRows()[1].count.jq.val(5);
		model.getBallotRows()[2].count.jq.val(4);
		model.getBlankRow().count.jq.val(10);
		model.getQuestionableRow().count.jq.val(5);
		
		model.calculate();

		expect(model.getBallotRowsTotal().count.getValue()).toBe(12);
		expect(model.getTotalRow().count.getValue()).toBe(27);
		
	});

	it('BALLOT_COUNTS_WITH_PROTOCOL created correctly', function () {
		var model = new EVA.Model.BallotCountsModel();

		addAllRows(model, BALLOT_COUNTS_WITH_PROTOCOL);

		expect(model.getBallotRows().length).toBe(3);
		expect(model.getBallotRows()[0].count.getValue()).toBe(2);
		expect(model.getBallotRows()[1].count.getValue()).toBe(2);
		expect(model.getBallotRows()[2].count.getValue()).toBe(2);
		assertNumbers(model.getBallotRowsTotal(), 6, NaN, NaN, 6, 0);
		assertNumbers(model.getBlankRow(), 0, NaN, NaN, 2, 2);
		assertNumbers(model.getQuestionableRow(), 4, NaN, NaN, 2, -2);
		expect(model.getRejectedRows().length).toBe(0);
		expect(model.getRejectedRowsTotal().count.exists()).toBe(false);
		assertNumbers(model.getTotalRow(), 10, NaN, NaN, 10, 0);
	});

	it('BALLOT_COUNTS_WITH_PROTOCOL calculated correctly', function () {
		var model = new EVA.Model.BallotCountsModel();
		addAllRows(model, BALLOT_COUNTS_WITH_PROTOCOL);
		model.getBallotRows()[0].count.jq.val(3);
		model.getBallotRows()[1].count.jq.val(5);
		model.getBallotRows()[2].count.jq.val(4);
		model.getBlankRow().count.jq.val(8);
		model.getQuestionableRow().count.jq.val(7);
		
		model.calculate();

		assertNumbers(model.getBallotRowsTotal(), 6, NaN, NaN, 12, 6);
		assertNumbers(model.getTotalRow(), 10, NaN, NaN, 27, 17);
	});

	it('BALLOT_COUNTS_WITH_SPLIT created correctly', function () {
		var model = new EVA.Model.BallotCountsModel();

		addAllRows(model, BALLOT_COUNTS_WITH_SPLIT);

		expect(model.getBallotRows().length).toBe(3);
		assertNumbers(model.getBallotRows()[0], NaN, 1, 3, 4, NaN);
		assertNumbers(model.getBallotRows()[1], NaN, 3, 1, 4, NaN);
		assertNumbers(model.getBallotRows()[2], NaN, 2, 2, 4, NaN);
		assertNumbers(model.getBallotRowsTotal(), NaN, 6, 6, 12, NaN);
		assertNumbers(model.getBlankRow(), NaN, NaN, NaN, 4, NaN);
		expect(model.getQuestionableRow().count.exists()).toBe(false);
		expect(model.getRejectedRows().length).toBe(4);
		assertNumbers(model.getRejectedRows()[0], NaN, NaN, NaN, 3, NaN);
		assertNumbers(model.getRejectedRows()[1], NaN, NaN, NaN, 2, NaN);
		assertNumbers(model.getRejectedRows()[2], NaN, NaN, NaN, 3, NaN);
		assertNumbers(model.getRejectedRows()[3], NaN, NaN, NaN, 4, NaN);
		assertNumbers(model.getRejectedRowsTotal(), NaN, NaN, NaN, 12, NaN);
		assertNumbers(model.getTotalRow(), NaN, NaN, NaN, 28, NaN);
	});

	it('BALLOT_COUNTS_WITH_SPLIT calculated correctly', function () {
		var model = new EVA.Model.BallotCountsModel();
		addAllRows(model, BALLOT_COUNTS_WITH_SPLIT);
		model.getBallotRows()[0].modified.jq.val(9);
		model.getBallotRows()[0].unmodified.jq.val(7);
		model.getBallotRows()[1].modified.jq.val(8);
		model.getBallotRows()[1].unmodified.jq.val(6);
		model.getBallotRows()[2].modified.jq.val(7);
		model.getBallotRows()[2].unmodified.jq.val(5);
		model.getBlankRow().count.jq.val(8);
		model.getRejectedRows()[0].count.jq.val(4);
		model.getRejectedRows()[1].count.jq.val(2);
		model.getRejectedRows()[2].count.jq.val(3);
		model.getRejectedRows()[3].count.jq.val(1);
		
		model.calculate();
		
		assertNumbers(model.getBallotRows()[0], NaN, 9, 7, 16, NaN);
		assertNumbers(model.getBallotRows()[1], NaN, 8, 6, 14, NaN);
		assertNumbers(model.getBallotRows()[2], NaN, 7, 5, 12, NaN);
		assertNumbers(model.getBallotRowsTotal(), NaN, 24, 18, 42, NaN);
		assertNumbers(model.getRejectedRowsTotal(), NaN, NaN, NaN, 10, NaN);
		assertNumbers(model.getTotalRow(), NaN, NaN, NaN, 60, NaN);
	});
	
});

function assertNumbers(row, protocol, modified, unmodified, count, protocolDiff) {
	if (!isNaN(protocol)) {
		expect(row.protocol.getValue()).toBe(protocol);
	}
	if (!isNaN(modified)) {
		expect(row.modified.getValue()).toBe(modified);
	}
	if (!isNaN(unmodified)) {
		expect(row.unmodified.getValue()).toBe(unmodified);
	}
	if (!isNaN(count)) {
		expect(row.count.getValue()).toBe(count);
	}
	if (!isNaN(protocolDiff)) {
		expect(row.protocolDiff.getValue()).toBe(protocolDiff);
	}
}

function addAllRows(model, html) {
	getBodyRows(html).each(function(i, elem) {
		model.addBodyRow($(elem));
	});
	model.setFootRow(getFootRows(html));
}
function getBodyRows(html) {
	return $(html).find('tbody tr');
}

function getFootRows(html) {
	return $(html).find('tfoot tr');
}

// HTML under hentet fra EVA Admin webapp. Skulle markup endre seg og testene feiler, er det bare å kopiere source kode og lime inn her.
var BALLOT_COUNTS_WITHOUT_PROTOCOL_COUNTS_HTML = '<table role="grid" class="table table-striped"><thead id="countingForm:tabView:ballotCounts:ballotCountsTable_head"><tr role="row"><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt98" class="ui-state-default" role="columnheader"><span class="ui-column-title">Parti</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt101" class="ui-state-default" role="columnheader"><span class="ui-column-title">ID</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt106" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Stemmer</span></th></tr></thead><tfoot id="countingForm:tabView:ballotCounts:ballotCountsTable_foot"><tr><td class="ui-state-default">Totalt antall stemmesedler</td><td class="ui-state-default"></td><td class="ui-state-default col-votes"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:totalBallotCount">11</span></td></tr></tfoot><tbody id="countingForm:tabView:ballotCounts:ballotCountsTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ballot row_ballot_ELP" role="row"><td role="gridcell"><span class="">Ellinor-Lina partiet</span></td><td role="gridcell"><span class="">ELP</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:0:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:0:unmodifiedBallotCount" type="text" value="1" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_ballot row_ballot_H" role="row"><td role="gridcell"><span class="">Høyre</span></td><td role="gridcell"><span class="">H</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:1:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:1:unmodifiedBallotCount" type="text" value="3" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_ballot row_ballot_V" role="row"><td role="gridcell"><span class="">Venstre</span></td><td role="gridcell"><span class="">V</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:2:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:2:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_total_ballot_count" role="row"><td role="gridcell"><span class="bold">Stemmesedler til fordeling totalt</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:3:unmodifiedBallotCountText" class="bold">6</span></td></tr><tr data-ri="4" class="ui-widget-content ui-datatable-even row_blank" role="row"><td role="gridcell"><span class="">Blanke stemmer</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:4:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:4:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="5" class="ui-widget-content ui-datatable-odd row_questionable" role="row"><td role="gridcell"><span class="">Tvilsomme sedler</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:5:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:5:unmodifiedBallotCount" type="text" value="3" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr></tbody></table>';
var BALLOT_COUNTS_WITH_PROTOCOL = '<table role="grid" class="table table-striped"><thead id="countingForm:tabView:ballotCounts:ballotCountsTable_head"><tr role="row"><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt98" class="ui-state-default" role="columnheader"><span class="ui-column-title">Parti</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt101" class="ui-state-default" role="columnheader"><span class="ui-column-title">ID</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt104" class="ui-state-default col-protocol" role="columnheader"><span class="ui-column-title">Fra urne</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt106" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Stemmer</span></th><th id="countingForm:tabView:ballotCounts:ballotCountsTable:j_idt107" class="ui-state-default col-protocol-sum" role="columnheader"><span class="ui-column-title">Endring</span></th></tr></thead><tfoot id="countingForm:tabView:ballotCounts:ballotCountsTable_foot"><tr><td class="ui-state-default">Totalt antall stemmesedler</td><td class="ui-state-default"></td><td class="ui-state-default col-protocol"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:totalProtocolCounts">10</span></td><td class="ui-state-default col-votes"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:totalBallotCount">10</span></td><td class="ui-state-default col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:totalProtocolCountDiff">0</span></td></tr></tfoot><tbody id="countingForm:tabView:ballotCounts:ballotCountsTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ballot row_ballot_ELP" role="row"><td role="gridcell"><span class="">Ellinor-Lina partiet</span></td><td role="gridcell"><span class="">ELP</span></td><td role="gridcell" class="col-protocol"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:0:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:0:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:0:ballotCountSum" class=""></span></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_ballot row_ballot_H" role="row"><td role="gridcell"><span class="">Høyre</span></td><td role="gridcell"><span class="">H</span></td><td role="gridcell" class="col-protocol"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:1:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:1:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:1:ballotCountSum" class=""></span></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_ballot row_ballot_V" role="row"><td role="gridcell"><span class="">Venstre</span></td><td role="gridcell"><span class="">V</span></td><td role="gridcell" class="col-protocol"><span class=""></span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:2:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:2:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:2:ballotCountSum" class=""></span></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_total_ballot_count" role="row"><td role="gridcell"><span class="bold">Stemmesedler til fordeling totalt</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-protocol"><span class="bold">6</span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:3:unmodifiedBallotCountText" class="bold">6</span></td><td role="gridcell" class="col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:3:ballotCountSum" class="bold">0</span></td></tr><tr data-ri="4" class="ui-widget-content ui-datatable-even row_blank" role="row"><td role="gridcell"><span class="">Blanke stemmer</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-protocol"><span class="">0</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:4:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:4:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:4:ballotCountSum" class="">2</span></td></tr><tr data-ri="5" class="ui-widget-content ui-datatable-odd row_questionable" role="row"><td role="gridcell"><span class="">Tvilsomme sedler</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-protocol"><span class="">4</span></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:ballotCounts:ballotCountsTable:5:unmodifiedBallotCount" name="countingForm:tabView:ballotCounts:ballotCountsTable:5:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-protocol-sum"><span id="countingForm:tabView:ballotCounts:ballotCountsTable:5:ballotCountSum" class="">-2</span></td></tr></tbody></table>';
var BALLOT_COUNTS_WITH_SPLIT = '<table role="grid" class="table table-striped"><thead id="countingForm:tabView:j_idt155:ballotCountsTable_head"><tr role="row"><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt157" class="ui-state-default" role="columnheader"><span class="ui-column-title">Parti</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt160" class="ui-state-default" role="columnheader"><span class="ui-column-title">ID</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt165" class="ui-state-default col-modified" role="columnheader"><span class="ui-column-title">Rettet</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt167" class="ui-state-default col-unmodified" role="columnheader"><span class="ui-column-title">Urettet</span></th><th id="countingForm:tabView:j_idt155:ballotCountsTable:j_idt168" class="ui-state-default col-votes" role="columnheader"><span class="ui-column-title">Total</span></th></tr></thead><tfoot id="countingForm:tabView:j_idt155:ballotCountsTable_foot"><tr><td class="ui-state-default">Totalt antall stemmesedler</td><td class="ui-state-default"></td><td class="ui-state-default col-modified"></td><td class="ui-state-default col-unmodified"></td><td class="ui-state-default col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:totalBallotCount">28</span></td></tr></tfoot><tbody id="countingForm:tabView:j_idt155:ballotCountsTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ballot row_ballot_ELP" role="row"><td role="gridcell"><span class="">Ellinor-Lina partiet</span></td><td role="gridcell"><span class="">ELP</span></td><td role="gridcell" class="col-modified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:0:modifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:0:modifiedBallotCount" type="text" value="1" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control modified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-unmodified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:0:unmodifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:0:unmodifiedBallotCount" type="text" value="3" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control unmodified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:0:ballotCountText" class="">4</span></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_ballot row_ballot_H" role="row"><td role="gridcell"><span class="">Høyre</span></td><td role="gridcell"><span class="">H</span></td><td role="gridcell" class="col-modified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:1:modifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:1:modifiedBallotCount" type="text" value="3" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control modified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-unmodified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:1:unmodifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:1:unmodifiedBallotCount" type="text" value="1" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control unmodified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:1:ballotCountText" class="">4</span></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_ballot row_ballot_V" role="row"><td role="gridcell"><span class="">Venstre</span></td><td role="gridcell"><span class="">V</span></td><td role="gridcell" class="col-modified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:2:modifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:2:modifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control modified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-unmodified"><input id="countingForm:tabView:j_idt155:ballotCountsTable:2:unmodifiedBallotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:2:unmodifiedBallotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control unmodified " role="textbox" aria-disabled="false" aria-readonly="false"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:2:ballotCountText" class="">4</span></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_total_ballot_count" role="row"><td role="gridcell"><span class="bold">Stemmesedler til fordeling totalt</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-modified"><span id="countingForm:tabView:j_idt155:ballotCountsTable:3:modifiedBallotCountText" class="bold">6</span></td><td role="gridcell" class="col-unmodified"><span id="countingForm:tabView:j_idt155:ballotCountsTable:3:unmodifiedBallotCountText" class="bold">6</span></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:3:ballotCountText" class="bold">12</span></td></tr><tr data-ri="4" class="ui-widget-content ui-datatable-even row_blank" role="row"><td role="gridcell"><span class="">Blanke stemmer</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:4:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:4:ballotCount" type="text" value="4" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="5" class="ui-widget-content ui-datatable-odd row_header" role="row"><td role="gridcell"><span class="bold">Foreslått forkastede</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"></td></tr><tr data-ri="6" class="ui-widget-content ui-datatable-even row_rejected row_rejected_VA" role="row"><td role="gridcell"><span class="">Mangler  off. stempel (VA)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:6:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:6:ballotCount" type="text" value="3" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="7" class="ui-widget-content ui-datatable-odd row_rejected row_rejected_VB" role="row"><td role="gridcell"><span class="">Fremgår ikke hvilket valg (VB)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:7:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:7:ballotCount" type="text" value="2" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="8" class="ui-widget-content ui-datatable-even row_rejected row_rejected_VC" role="row"><td role="gridcell"><span class="">Fremgår ikke hvilken liste (VC)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:8:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:8:ballotCount" type="text" value="3" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="9" class="ui-widget-content ui-datatable-odd row_rejected row_rejected_VD" role="row"><td role="gridcell"><span class="">Parti/gruppe stiller ikke liste (VD)</span></td><td role="gridcell"><span class=""></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><input id="countingForm:tabView:j_idt155:ballotCountsTable:9:ballotCount" name="countingForm:tabView:j_idt155:ballotCountsTable:9:ballotCount" type="text" value="4" size="4" class="ui-inputfield ui-inputtext ui-widget ui-state-default ui-corner-all form-control count " role="textbox" aria-disabled="false" aria-readonly="false"></td></tr><tr data-ri="10" class="ui-widget-content ui-datatable-even row_total_rejected_ballot_count" role="row"><td role="gridcell"><span class="bold">Totalt foreslått forkastede</span></td><td role="gridcell"><span class="bold"></span></td><td role="gridcell" class="col-modified"></td><td role="gridcell" class="col-unmodified"></td><td role="gridcell" class="col-votes"><span id="countingForm:tabView:j_idt155:ballotCountsTable:10:ballotCountText" class="bold">12</span></td></tr></tbody></table>';
