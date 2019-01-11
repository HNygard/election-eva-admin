package no.valg.eva.admin.frontend.configuration.models;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ReportingUnit;

import org.apache.commons.lang3.StringUtils;

public class ElectionCardModel {
	private final ElectionCardConfig electionCard;
	private final ElectionDayPollingPlace pollingPlace;
	private static final int ELECTION_CARD_INFO_TEXT_MAX_LENGTH = 150;
	private boolean infoTextChanged;
	private boolean addressChanged;
	private boolean customInfoText;

	public ElectionCardModel(ElectionCardConfig electionCard) {
		this(electionCard, null);
	}

	public ElectionCardModel(ElectionCardConfig electionCard, ElectionDayPollingPlace pollingPlace) {
		this.electionCard = electionCard;
		this.pollingPlace = pollingPlace;
		this.customInfoText = !isEmpty(getElectionCardInfoText()) && !StringUtils.equals(electionCard.getInfoText(), getElectionCardInfoText());
	}

	public ElectionCardConfig getElectionCard() {
		return electionCard;
	}

	public boolean isRoot() {
		return pollingPlace == null;
	}

	public ElectionDayPollingPlace getPollingPlace() {
		return pollingPlace;
	}

	public ReportingUnit getReportingUnit() {
		return electionCard.getReportingUnit();
	}

	public int getElectionCardInfoTextMaxLength() {
		return ELECTION_CARD_INFO_TEXT_MAX_LENGTH;
	}

	public String getMunicipalityId() {
		return getReportingUnit().getAreaPath().getMunicipalityId();
	}

	public String getPollingDistrictId() {
		return pollingPlace == null ? "-" : pollingPlace.getPath().getPollingDistrictId();
	}

	public void setElectionCardInfoText(String electionCardInfoText) {
		if (pollingPlace == null) {
			electionCard.setInfoText(electionCardInfoText);
		} else {
			pollingPlace.setInfoText(electionCardInfoText);
		}
		infoTextChanged = true;
	}

	public String getElectionCardInfoText() {
		if (pollingPlace == null) {
			return electionCard.getInfoText();
		} else {
			return pollingPlace.getInfoText();
		}
	}

	public boolean isCustomInfoText() {
		return customInfoText;
	}

	public String getLabel() {
		if (pollingPlace == null) {
			return electionCard.getReportingUnit().getAreaPath().getLeafId() + "-" + electionCard.getReportingUnit().getAreaName();
		} else {
			return getPollingDistrictId() + "-" + pollingPlace.getParentName();
		}
	}

	public boolean isInfoTextChanged() {
		return infoTextChanged;
	}

	public boolean isAddressChanged() {
		return addressChanged;
	}

	public String getAddress() {
		return isRoot() ? getReportingUnit().getAddress() : null;
	}

	public void setAddress(String address) {
		if (isRoot() && !StringUtils.equals(address, getReportingUnit().getAddress())) {
			getReportingUnit().setAddress(address);
			addressChanged = true;
		}
	}

	public String getPostalCode() {
		return isRoot() ? getReportingUnit().getPostalCode() : null;
	}

	public void setPostalCode(String postalCode) {
		if (isRoot() && !StringUtils.equals(postalCode, getReportingUnit().getPostalCode())) {
			getReportingUnit().setPostalCode(postalCode);
			addressChanged = true;
		}
	}

	public String getPostTown() {
		return isRoot() ? getReportingUnit().getPostTown() : null;
	}

	public void setPostTown(String postTown) {
		if (isRoot() && !StringUtils.equals(postTown, getReportingUnit().getPostTown())) {
			getReportingUnit().setPostTown(postTown);
			addressChanged = true;
		}
	}

	public boolean isValid() {
		return !isRoot() || getReportingUnit().hasAddressFields();
	}
}
