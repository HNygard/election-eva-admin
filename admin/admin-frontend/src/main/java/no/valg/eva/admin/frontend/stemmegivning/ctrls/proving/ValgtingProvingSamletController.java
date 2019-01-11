package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import no.evote.dto.ApproveVotingStatisticsDto;
import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletRedirectInfo;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.VotingNumberInterval;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.VotingStatisticCategories;
import org.primefaces.event.SelectEvent;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;

/**
 * Controller for prøving av valgtingstemmer samlet.
 */
@Named
@ViewScoped
public class ValgtingProvingSamletController extends ProvingSamletController {

	private static final String ALL = "ALL";
	private static final String LATE_ADVANCE_VOTES = "LATE_ADVANCE_VOTES";

	@Override
	public boolean isValgting() {
		return true;
	}

	@Override
	public void findVotings() {
		VotingNumberInterval votingNumberInterval = new VotingNumberInterval(getForm().getStartVotingNumber(), getForm().getEndVotingNumber());
		if (votingNumberInterval.hasError()) {
			showMessage(getMessageProvider().get("@voting.approveVoting.searchApproveNegativeVoting.errorMissingVotingNumber"), FacesMessage.SEVERITY_ERROR);
			return;
		}

		setVotingStatistics(findElectionDayVotingStatistics(votingNumberInterval));

		Long municipalityPk = getKommune().getMunicipality().getPk();
		Long electionGroupPk = getValgGruppe().getPk();
		int votingNumberStart = votingNumberInterval.start();
		int votingNumberEnd = votingNumberInterval.end();
		String[] votingCategories = electionDayPickListVotingCats();
		setPickListItems(getVotingService().findElectionDayVotingPickList( 
				getUserData(), municipalityPk, electionGroupPk, votingNumberStart, votingNumberEnd, votingCategories));

		if (getVotingStatistics().isEmpty() && getPickListItems().isEmpty()) {
			showMessage(getMessageProvider().get("@voting.approveVoting.searchApproveElectionDayNegativeVoting.noVotings"), FacesMessage.SEVERITY_INFO);
			return;
		}

		setApproveVotingStatistics(new ApproveVotingStatisticsDto(getVotingStatistics(), getPickListItems()));

		setVisResultat(true);
	}

	@Override
	public void updateVotingsApproved() {

		VotingNumberInterval votingNumberInterval = new VotingNumberInterval(getForm().getStartVotingNumber(), getForm().getEndVotingNumber());

		execute(() -> {
			int numberOfUpdates = getVotingService().updateElectionDayVotingsApproved(
					getUserData(), getKommune().getMunicipality().getPk(),
					getValgGruppe().getPk(), votingNumberInterval.start(), votingNumberInterval.end(),
					electionDayPickListVotingCats());
			showMessage(getMessageProvider().get("@voting.approveVoting.noVotingsApproved", numberOfUpdates), FacesMessage.SEVERITY_INFO);

			setVotingStatistics(findElectionDayVotingStatistics(votingNumberInterval));
			setApproveVotingStatistics(new ApproveVotingStatisticsDto(getVotingStatistics(), getPickListItems()));
		});
		setVisResultat(false);
	}

	public void selectVoterInNegativeVotingList(SelectEvent event) {
		PickListItem dto = PickListItem.class.cast(event.getObject());
		try {
			leggRedirectInfoPaSession(
					new ProvingSamletRedirectInfo(dto.getVoterId(), getDenneSidenURL(), "@menu.approveVoting.approveVotingNegativeElectionDay", getForm()));
			String redirectTil = getDenneSidenURL().replace("valgtingProvingSamlet.xhtml", "valgtingProvingVelger.xhtml");
			getFacesContext().getExternalContext().redirect(redirectTil);
		} catch (IOException e) {
			throw new RuntimeException("Failed to redirect " + e, e);
		}
	}

	@Override
	public List<SelectItem> votingCategoryChoiceList() {
		List<SelectItem> choiceList = new ArrayList<>();
		choiceList.add(new SelectItem(ALL, this.getVotingCategoryChoiceLabel(ALL)));
		choiceList.add(new SelectItem(VS.getId(), this.getVotingCategoryChoiceLabel(VS.getId())));
		choiceList.add(new SelectItem(VB.getId(), this.getVotingCategoryChoiceLabel(VB.getId())));
		choiceList.add(new SelectItem(LATE_ADVANCE_VOTES, this.getVotingCategoryChoiceLabel(LATE_ADVANCE_VOTES)));
		return choiceList;
	}

	public String getVotingCategoryChoiceLabel(String key) {
		if (ALL.equals(key)) {
			return getMessageProvider().get("@voting.approveVoting.allCategories");
		} else if (VS.getId().equals(key)) {
			return getMessageProvider().get("@voting_category[VS].name");
		} else if (VB.getId().equals(key)) {
			return getMessageProvider().get("@voting_category[VB].name");
		} else if (LATE_ADVANCE_VOTES.equals(key)) {
			return getMessageProvider().get("@voting.approveVoting.lateAdvanceVotes");
		} else {
			return "NA";
		}
	}

	private List<VotingDto> findElectionDayVotingStatistics(VotingNumberInterval votingNumberInterval) {
		VotingStatisticCategories vsc = new VotingStatisticCategories(getForm().getSelectedVotingCategoryId());
		return getVotingService().findVotingStatistics(
				getUserData(), 0L, getKommune().getMunicipality().getPk(), getValgGruppe().getPk(), null, null, votingNumberInterval.start(),
				votingNumberInterval.end(), false,
				vsc.electionDayVotingCategoriesForStatistics(), vsc.includeLateAdvanceVotes());
	}

	private String[] electionDayPickListVotingCats() {
		return getForm().getSelectedVotingCategoryId().equals(ALL) ? new String[] { VS.getId(), VB.getId(), FB.getId(),
				FU.getId(), FI.getId(), FE.getId() }
				: getForm().getSelectedVotingCategoryId().equals(LATE_ADVANCE_VOTES) ? new String[] { FB.getId(), FU.getId(),
						FI.getId(), FE.getId() }
						: new String[] { getForm().getSelectedVotingCategoryId() };
	}

}
