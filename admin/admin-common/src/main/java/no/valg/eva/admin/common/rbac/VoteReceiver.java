package no.valg.eva.admin.common.rbac;


/**
 * Dto for import av nye brukere med rollen "stemmemottaker VALGTING".
 */
public class VoteReceiver extends ImportOperatorRoleInfo {

    /**
     * Kretsnummer/polling_district_id.
     */
    private String votingDistrict;

    public VoteReceiver(String operatorId, String firstName, String lastName, String email, String telephoneNumber, String votingDistrict) {
		super(operatorId, firstName, lastName, email, telephoneNumber);
		this.votingDistrict = votingDistrict;
    }

    public String getVotingDistrict() {
        return votingDistrict;
    }

	@Override
	public String getAreaId() {
		return votingDistrict;
	}

	@Override
	public boolean areaIsPollingDistrict() {
		return true;
	}
}
