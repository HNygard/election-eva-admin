package no.valg.eva.admin.frontend.counting.view.ballotcount;

import no.valg.eva.admin.frontend.counting.view.ModelRow;

public interface BallotCountsModelRow extends ModelRow {

	String getId();

	Integer getProtocolCount();

	Integer getDiff();

	String getStyleClass();

}
