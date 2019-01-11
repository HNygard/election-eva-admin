package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.dto.ApproveVotingStatisticsDto;
import no.evote.dto.PickListItem;
import no.evote.model.views.PollingPlaceVoting;
import no.evote.service.configuration.LegacyPollingPlaceService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletRedirectInfo;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.VotingNumberInterval;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.primefaces.event.SelectEvent;

/**
 * Controller for prøving av forhåndstemmer samlet.
 */
@Named
@ViewScoped
public class ForhandProvingSamletController extends ProvingSamletController {

	@Inject
	private LegacyPollingPlaceService pollingPlaceService;

	@Override
	public void initialized(Kontekst kontekst) {
		super.initialized(kontekst);
		setAdvancedPollingPlaceVotingList(pollingPlaceService.findAdvancedPollingPlaceByMunicipality(
				getUserData(), getUserDataController().getElectionEvent().getPk(), getKommune().getMunicipalityId()));
	}

	@Override
	public boolean isValgting() {
		return false;
	}

	@Override
	public void findVotings() {
		if (!getForm().isRegisteredToday() && (getForm().getStartDate() == null || getForm().getEndDate() == null)) {
			showMessage(getMessageProvider().get("@voting.approveVoting.searchApproveNegativeVoting.errorMissingDate"), FacesMessage.SEVERITY_ERROR);
			return;
		}
		if (!getForm().isRegisteredToday() && (getForm().getEndDate().isBefore(getForm().getStartDate()))) {
			showMessage(getMessageProvider().get("@common.message.evote_application_exception.END_DATE_BEFORE_START_DATE"), FacesMessage.SEVERITY_ERROR);
			return;
		}
		if (getForm().isRegisteredToday()) {
			getForm().setStartDate(LocalDate.now());
			getForm().setEndDate(LocalDate.now());
		}

		VotingNumberInterval votingNumberInterval = new VotingNumberInterval(getForm().getStartVotingNumber(), getForm().getEndVotingNumber());
		if (votingNumberInterval.hasError()) {
			showMessage(getMessageProvider().get("@voting.approveVoting.searchApproveNegativeVoting.errorMissingVotingNumber"), FacesMessage.SEVERITY_ERROR);
			return;
		}

		setVotingStatistics(getVotingService().findVotingStatistics(
				getUserData(), getActualSelectedPollingPlacePk(), getKommune().getMunicipality().getPk(),
				getValgGruppe().getPk(), getForm().getStartDate(), getForm().getEndDate(), votingNumberInterval.start(), votingNumberInterval.end(),
				false, new String[] { FI.getId(), FU.getId(), FB.getId(), FE.getId() }, false));

		if (getVotingStatistics().isEmpty()) {
			showMessage(getMessageProvider().get("@voting.approveVoting.searchApproveNegativeVoting.noAdvanceVotings"), FacesMessage.SEVERITY_INFO);
			return;
		}

		setPickListItems(getVotingService().findAdvanceVotingPickList(
				getUserData(), getActualSelectedPollingPlacePk(), getKommune().getMunicipality().getPk(),
				getValgGruppe().getPk(), getForm().getStartDate(), getForm().getEndDate(),
				votingNumberInterval.start(), votingNumberInterval.end()));

		setApproveVotingStatistics(new ApproveVotingStatisticsDto(getVotingStatistics(), getPickListItems()));
		setVisResultat(true);
	}

	@Override
	public void updateVotingsApproved() {
		if (getForm().isRegisteredToday()) {
			getForm().setStartDate(LocalDate.now());
			getForm().setEndDate(LocalDate.now());
		}
		execute(() -> {
			int votingNumberStart = 0;
			int votingNumberEnd = 0;
			if (!StringUtils.isEmpty(getForm().getStartVotingNumber()) && Integer.parseInt(getForm().getStartVotingNumber()) > 0) {
				votingNumberStart = Integer.parseInt(getForm().getStartVotingNumber());
			}
			if (!StringUtils.isEmpty(getForm().getEndVotingNumber()) && Integer.parseInt(getForm().getEndVotingNumber()) > 0) {
				votingNumberEnd = Integer.parseInt(getForm().getEndVotingNumber());
			}
			int numberOfUpdates = getVotingService().updateAdvanceVotingsApproved(
					getUserData(), getActualSelectedPollingPlacePk(), getKommune().getMunicipality().getPk(),
					getValgGruppe().getPk(), getForm().getStartDate(), getForm().getEndDate(), votingNumberStart, votingNumberEnd);
			showMessage(getMessageProvider().get("@voting.approveVoting.noVotingsApproved", numberOfUpdates), FacesMessage.SEVERITY_INFO);
			setVotingStatistics(getVotingService().findVotingStatistics(
					getUserData(), getActualSelectedPollingPlacePk(), getKommune().getMunicipality().getPk(),
					getValgGruppe().getPk(), getForm().getStartDate(), getForm().getEndDate(),
					votingNumberStart, votingNumberEnd, false, new String[] { FI.getId(), FU.getId(), FB.getId(), FE.getId() }, false));

			setApproveVotingStatistics(new ApproveVotingStatisticsDto(getVotingStatistics(), getPickListItems()));
		});
		setVisResultat(false);
	}

	public void selectVoterInNegativeVotingList(SelectEvent event) {
		PickListItem dto = PickListItem.class.cast(event.getObject());
		try {
			leggRedirectInfoPaSession(
					new ProvingSamletRedirectInfo(dto.getVoterId(), getDenneSidenURL(), "@menu.approveVoting.approveVotingNegative", getForm()));
			String redirectTil = getDenneSidenURL().replace("forhandProvingSamlet.xhtml", "forhandProvingVelger.xhtml");
			getFacesContext().getExternalContext().redirect(redirectTil);
		} catch (IOException e) {
			throw new RuntimeException("Failed to redirect " + e, e);
		}
	}

	private long getActualSelectedPollingPlacePk() {
		if (getValgGruppe() == null || getValgGruppe().isAdvanceVoteInBallotBox() || getForm().getSelectedPollingPlacePk() == null) {
			return 0;
		}
		return getForm().getSelectedPollingPlacePk();
	}

	@Override
	public String getSelectedPollingPlaceName() {
		for (PollingPlaceVoting sted : getAdvancedPollingPlaceVotingList()) {
			if (sted.getId().getPollingPlacePk().equals(getForm().getSelectedPollingPlacePk())) {
				return sted.getPollingPlaceId() + " " + sted.getPollingPlaceName();
			}
		}
		return getMessageProvider().get("@voting.approveVoting.allPollingPlaces");
	}

}
