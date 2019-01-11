package no.valg.eva.admin.common.configuration.model.local;

import no.valg.eva.admin.common.AreaPath;

public class AdvancePollingPlace extends PollingPlace {

	private boolean publicPlace;
	private boolean advanceVoteInBallotBox;

	public AdvancePollingPlace(AreaPath path) {
		this(path, 0);
	}

	public AdvancePollingPlace(AreaPath path, int version) {
		super(path, version);
	}

	public boolean isPublicPlace() {
		return publicPlace;
	}

	public void setPublicPlace(boolean publicPlace) {
		this.publicPlace = publicPlace;
	}

	public boolean isAdvanceVoteInBallotBox() {
		return advanceVoteInBallotBox;
	}

	public void setAdvanceVoteInBallotBox(boolean advanceVoteInBallotBox) {
		this.advanceVoteInBallotBox = advanceVoteInBallotBox;
	}

	@Override
	boolean isAddressFieldsRequired() {
		return publicPlace && !AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID.equals(getId());
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
