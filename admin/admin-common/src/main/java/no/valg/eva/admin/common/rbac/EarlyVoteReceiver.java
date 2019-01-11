package no.valg.eva.admin.common.rbac;

/**
 * Dto for import av nye brukere med rollen "forhåndsstemmemottaker".
 */
public class EarlyVoteReceiver extends ImportOperatorRoleInfo {

    /**
     * Forhåndsstemmestedets polling_place_id.
     */
    private String advancedPollingPlaceId;

    public EarlyVoteReceiver(String operatorId, String firstName, String lastName, String email, String telephoneNumber, String advancedPollingPlaceId) {
		super(operatorId, firstName, lastName, email, telephoneNumber);
	    this.advancedPollingPlaceId = advancedPollingPlaceId;
    }

    public String getAdvancedPollingPlaceId() {
        return advancedPollingPlaceId;
    }

	@Override
	public String getAreaId() {
		return getAdvancedPollingPlaceId();
	}

	@Override
	public boolean areaIsPollingDistrict() {
		return false;
	}
}
