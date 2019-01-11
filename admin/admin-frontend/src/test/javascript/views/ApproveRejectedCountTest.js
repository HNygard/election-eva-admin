describe('EVA.View.ApproveRejectedCount', function () {

	it('is available', function () {
		expect(EVA.View.ApproveRejectedCount).not.toBe(null);
	});

	it('is setup correctly on contructor', function () {
		setPageHTML();
		
		var view = new EVA.View.ApproveRejectedCount();
		
		expect(view.getBallotApproveRejectedMainTable().length).toBe(1);
		expect(view.getBallotCountTable().length).toBe(1);
	});

	it('sums total row when ballotApproveRejectedMainTable input changes', function () {
		setPageHTML();

		var view = new EVA.View.ApproveRejectedCount();
		var inputs = view.getBallotApproveRejectedMainTable().find('.col-total input');
		enterInputValue(inputs.eq(0), 10);
		enterInputValue(inputs.eq(1), 5);
		enterInputValue(inputs.eq(2), 0);
		enterInputValue(inputs.eq(3), 0);

		expect(view.getBallotApproveRejectedMainTableTotalField().text()).toBe('15');
	});

	it('sums input and total row when ballotCountTable input changes', function () {
		setPageHTML();

		var view = new EVA.View.ApproveRejectedCount();
		var modified = view.getBallotCountTable().find('tbody input.modified');
		var unmodified = view.getBallotCountTable().find('tbody input.unmodified');
		var blank = view.getBallotCountTable().find('tbody input.blank');
		enterInputValue(modified.eq(0), 1);
		enterInputValue(unmodified.eq(0), 1);
		enterInputValue(modified.eq(1), 2);
		enterInputValue(unmodified.eq(1), 2);
		enterInputValue(modified.eq(2), 3);
		enterInputValue(unmodified.eq(2), 3);
		enterInputValue(blank, 3);

		expect(modified.eq(0).closest('tr').find('.col-total span').text()).toBe("2");
		expect(modified.eq(1).closest('tr').find('.col-total span').text()).toBe("4");
		expect(modified.eq(2).closest('tr').find('.col-total span').text()).toBe("6");
		expect(view.getBallotCountTableTotalField().text()).toBe('15');
	});

	it('modified value toggles button', function () {
		setPageHTML();

		var view = new EVA.View.ApproveRejectedCount();
		var modified = view.getBallotCountTable().find('tbody input.modified');
		enterInputValue(modified.eq(0), 1);

		expect(view.getApproveToSettlementButton().is(':hidden')).toBe(true);
		expect(view.getRegisterCorrectionsButton().css("display")).toBe("inline-block");

		enterInputValue(modified.eq(0), 0);

		expect(view.getRegisterCorrectionsButton().is(':hidden')).toBe(true);
		expect(view.getApproveToSettlementButton().css("display")).toBe("inline-block");
	});

	
});

function setPageHTML() {
	EVA.View.ApproveRejectedCount.prototype.getPage = function () {
		return $(PAGE_HTML);
	};
}

function enterInputValue(input, val) {
	input.val(val);
	var e = $.Event("keyup");
	e.which = 13;
	input.trigger(e);
}

var PAGE_HTML = '<div id="page-approve-rejected-count" class="page" data-view="ApproveRejectedCount"> <h1 class="page-title">Manuelt forkastede stemmesedler </h1> <form id="j_idt83:pageTitle" name="j_idt83:pageTitle" method="post" action="/secure/counting/approveManualRejectedCount.xhtml?cid=20" enctype="application/x-www-form-urlencoded"> <input type="hidden" name="j_idt83:pageTitle" value="j_idt83:pageTitle"> <ul class="form-group col-md-12 page-title-meta" style="z-index: 10;"> <li class="meta-group "><span class="bold">Tellekategori:</span><span class="">Sent innkomne/lagt til side</span> </li> </ul><input type="hidden" name="javax.faces.ViewState" id="j_id1:javax.faces.ViewState:5" value="7312740673601579843:2610135384753822628"> </form> <form id="j_idt93:pageTitle" name="j_idt93:pageTitle" method="post" action="/secure/counting/approveManualRejectedCount.xhtml?cid=20" enctype="application/x-www-form-urlencoded"> <input type="hidden" name="j_idt93:pageTitle" value="j_idt93:pageTitle"> <ul class="form-group col-md-12 page-title-meta" style="z-index: 10;"> <li class="meta-group "><span class="bold">Fylkeskommune:</span><span class="">Oppland</span> </li> <li class="meta-group dashed-border-left"><span class="bold">Kommune:</span><span class="">Lunner</span> </li> <li class="meta-group dashed-border-left"><span class="bold">Stemmekrets:</span><span class="">Hele kommunen</span> </li> <li class="meta-group dashed-border-left"><span class="bold">Tellestatus:</span><span class="">Godkjent</span> </li> </ul><input type="hidden" name="javax.faces.ViewState" id="j_id1:javax.faces.ViewState:6" value="7312740673601579843:2610135384753822628"> </form> <div class="row"> <div class="col-md-12"><div id="messageBox" class="ui-messages ui-widget" aria-live="polite"></div> </div> </div> <form id="ballotApproveRejectedForm" name="ballotApproveRejectedForm" method="post" action="/secure/counting/approveManualRejectedCount.xhtml?cid=20" enctype="application/x-www-form-urlencoded"> <input type="hidden" name="ballotApproveRejectedForm" value="ballotApproveRejectedForm"> <div class="row"> <div class="col-md-6"><div id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped ballotApproveRejectedMainTable"><thead id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable_head"><tr role="row"><th id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:j_idt125" class="ui-state-default" role="columnheader"><span class="ui-column-title">Foreslått forkastede</span></th><th id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:j_idt129" class="ui-state-default col-total" role="columnheader"><span class="ui-column-title"></span></th></tr></thead><tfoot id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable_foot"><tr><td class="ui-state-default">Total antall forkastede</td><td class="ui-state-default col-total"><span id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:totalRejectedBallotCount">1</span></td></tr></tfoot><tbody id="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_rejected_FA" role="row"><td role="gridcell">Mangler off. stempel</td><td role="gridcell" class="col-total"><input type="text" name="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:0:j_idt131" autocomplete="off" value="1" class="form-control rejected" size="4"></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_rejected_FB" role="row"><td role="gridcell">Fremgår ikke hvilket valg</td><td role="gridcell" class="col-total"><input type="text" name="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:1:j_idt131" autocomplete="off" value="0" class="form-control rejected" size="4"></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_rejected_FC" role="row"><td role="gridcell">Fremgår ikke hvilken liste</td><td role="gridcell" class="col-total"><input type="text" name="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:2:j_idt131" autocomplete="off" value="0" class="form-control rejected" size="4"></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_rejected_FD" role="row"><td role="gridcell">Parti/gruppe stiller ikke liste</td><td role="gridcell" class="col-total"><input type="text" name="ballotApproveRejectedForm:j_idt124:rejectedBallotCountTable:3:j_idt131" autocomplete="off" value="0" class="form-control rejected" size="4"></td></tr></tbody></table></div></div> </div> <div class="col-md-6"></div> </div> <div class="row"> <div class="col-md-12"><div id="ballotApproveRejectedForm:j_idt133:ballotCountTable" class="ui-datatable ui-widget"><div class="ui-datatable-tablewrapper"><table role="grid" class="table table-striped ballotCountTable"><thead id="ballotApproveRejectedForm:j_idt133:ballotCountTable_head"><tr role="row"><th id="ballotApproveRejectedForm:j_idt133:ballotCountTable:j_idt134" class="ui-state-default" role="columnheader"><span class="ui-column-title">Parti</span></th><th id="ballotApproveRejectedForm:j_idt133:ballotCountTable:j_idt138" class="ui-state-default" role="columnheader"><span class="ui-column-title">Rettet</span></th><th id="ballotApproveRejectedForm:j_idt133:ballotCountTable:j_idt168" class="ui-state-default" role="columnheader"><span class="ui-column-title">Urettet</span></th><th id="ballotApproveRejectedForm:j_idt133:ballotCountTable:j_idt171" class="ui-state-default col-total bold" role="columnheader"><span class="ui-column-title">Total</span></th></tr></thead><tfoot id="ballotApproveRejectedForm:j_idt133:ballotCountTable_foot"><tr><td class="ui-state-default">Totalt antall godkjente stemmesedler</td><td class="ui-state-default"></td><td class="ui-state-default"></td><td class="ui-state-default col-total bold"><span id="ballotApproveRejectedForm:j_idt133:ballotCountTable:totalSum">0</span></td></tr></tfoot><tbody id="ballotApproveRejectedForm:j_idt133:ballotCountTable_data" class="ui-datatable-data ui-widget-content"><tr data-ri="0" class="ui-widget-content ui-datatable-even row_ballot_ELP" role="row"><td role="gridcell">Ellinor-Lina partiet</td><td role="gridcell"><input type="text" name="ballotApproveRejectedForm:j_idt133:ballotCountTable:0:j_idt140" autocomplete="off" value="0" class="form-control modified" size="4"></td><td role="gridcell"><input type="text" name="ballotApproveRejectedForm:j_idt133:ballotCountTable:0:j_idt170" autocomplete="off" value="0" class="form-control unmodified" size="4"></td><td role="gridcell" class="col-total bold"><span class="total">0</span></td></tr><tr data-ri="1" class="ui-widget-content ui-datatable-odd row_ballot_H" role="row"><td role="gridcell">Høyre</td><td role="gridcell"><input type="text" name="ballotApproveRejectedForm:j_idt133:ballotCountTable:1:j_idt140" autocomplete="off" value="0" class="form-control modified" size="4"></td><td role="gridcell"><input type="text" name="ballotApproveRejectedForm:j_idt133:ballotCountTable:1:j_idt170" autocomplete="off" value="0" class="form-control unmodified" size="4"></td><td role="gridcell" class="col-total bold"><span class="total">0</span></td></tr><tr data-ri="2" class="ui-widget-content ui-datatable-even row_ballot_V" role="row"><td role="gridcell">Venstre</td><td role="gridcell"><input type="text" name="ballotApproveRejectedForm:j_idt133:ballotCountTable:2:j_idt140" autocomplete="off" value="0" class="form-control modified" size="4"></td><td role="gridcell"><input type="text" name="ballotApproveRejectedForm:j_idt133:ballotCountTable:2:j_idt170" autocomplete="off" value="0" class="form-control unmodified" size="4"></td><td role="gridcell" class="col-total bold"><span class="total">0</span></td></tr><tr data-ri="3" class="ui-widget-content ui-datatable-odd row_ballot_BLANK" role="row"><td role="gridcell">Blanke stemmer</td><td role="gridcell"></td><td role="gridcell"></td><td role="gridcell" class="col-total bold"><input type="text" name="ballotApproveRejectedForm:j_idt134:ballotCountTable:3:j_idt150" autocomplete="off" value="0" class="form-control blank" size="4"></td></tr></tbody></table></div></div> </div> </div> <div class="row"> <div class="form-actions col-md-12"><div id="ballotApproveRejectedForm:j_idt150:j_idt151" class="ui-panel ui-widget ui-widget-content ui-corner-all form-actions form-group onModifiedBallotCountChange" data-widget="widget_ballotApproveRejectedForm_j_idt150_j_idt151"><div id="ballotApproveRejectedForm:j_idt150:j_idt151_content" class="ui-panel-content ui-widget-content"><button id="ballotApproveRejectedForm:j_idt150:approveToSettlement" name="ballotApproveRejectedForm:j_idt150:approveToSettlement" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary approveToSettlement" style="" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-checkmark"></span><span class="ui-button-text ui-c">Godkjenn og sett til valgoppgjør</span></button><button id="ballotApproveRejectedForm:j_idt150:goToRegisterCorrectedBallotsRejectionMode" name="ballotApproveRejectedForm:j_idt150:goToRegisterCorrectedBallotsRejectionMode" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-left btn btn-primary registerCorrections" onclick="" style="display:none;" type="submit" role="button" aria-disabled="false"><span class="ui-button-icon-left ui-icon ui-c eva-icon-plus"></span><span class="ui-button-text ui-c">Registrer rettede stemmesedler</span></button></div></div> </div> </div><input type="hidden" name="javax.faces.ViewState" id="j_id1:javax.faces.ViewState:16" value="7312740673601579843:2610135384753822628"> </form></div>';
