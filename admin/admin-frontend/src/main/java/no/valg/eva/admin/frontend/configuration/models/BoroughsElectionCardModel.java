package no.valg.eva.admin.frontend.configuration.models;

import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;

public class BoroughsElectionCardModel extends ElectionCardModel {

	private Borough borough;

	public BoroughsElectionCardModel(ElectionCardConfig electionCard, Borough borough) {
		super(electionCard);
		this.borough = borough;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public String getLabel() {
		return borough.getName();
	}
}
