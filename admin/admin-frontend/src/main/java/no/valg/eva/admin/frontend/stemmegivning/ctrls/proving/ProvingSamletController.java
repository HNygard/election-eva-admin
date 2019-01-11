package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import no.evote.dto.ApproveVotingStatisticsDto;
import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.model.views.PollingPlaceVoting;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletForm;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.proving.models.ProvingSamletRedirectInfo;
import org.primefaces.event.SelectEvent;

/**
 * Controller for prøving av samlet.
 */
public abstract class ProvingSamletController extends KontekstAvhengigController {

	@Inject
	@EjbProxy
	private VotingService votingService;

	// Init fields
	private String denneSidenURL;
	private ElectionGroup valgGruppe;
	private MvArea kommune;
	private List<PollingPlaceVoting> advancedPollingPlaceVotingList = new ArrayList<>();

	// Form fields
	private ProvingSamletForm form;

	// Result fields
	private boolean visResultat;
	private List<VotingDto> votingStatistics;
	private List<PickListItem> pickListItems;
	private ApproveVotingStatisticsDto approveVotingStatistics;

	public abstract boolean isValgting();

	public abstract void findVotings();

	public abstract void selectVoterInNegativeVotingList(SelectEvent event);

	public abstract void updateVotingsApproved();

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		setup.leggTil(geografi(KOMMUNE));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
        denneSidenURL = getPageURL();
		valgGruppe = getMvElectionService().findSingleByPath(kontekst.valggruppeSti()).getElectionGroup();
		kommune = getMvAreaService().findSingleByPath(kontekst.kommuneSti());
		ProvingSamletRedirectInfo redirectInfo = (ProvingSamletRedirectInfo) getAndRemoveRedirectInfo();
		if (redirectInfo == null) {
			form = new ProvingSamletForm();
		} else {
			form = redirectInfo.getForm();
			findVotings();
		}
	}

	void showMessage(String message, FacesMessage.Severity severityInfo) {
		buildDetailMessage(message, severityInfo);
	}

	public List<SelectItem> votingCategoryChoiceList() {
		return new ArrayList<>();
	}

	public int getTotalMultipleVotes() {
		return getApproveVotingStatistics().getTotalMultipleVotes();
	}

	public ElectionGroup getValgGruppe() {
		return valgGruppe;
	}

	public MvArea getKommune() {
		return kommune;
	}

	public boolean isVisStemmestedListe() {
		return !isValgting() && valgGruppe != null && !valgGruppe.isAdvanceVoteInBallotBox();
	}

	public List<PollingPlaceVoting> getAdvancedPollingPlaceVotingList() {
		return advancedPollingPlaceVotingList;
	}

    void setAdvancedPollingPlaceVotingList(List<PollingPlaceVoting> advancedPollingPlaceVotingList) {
		this.advancedPollingPlaceVotingList = advancedPollingPlaceVotingList;
	}

	public List<VotingDto> getVotingStatistics() {
		return votingStatistics;
	}

	public void setVotingStatistics(List<VotingDto> votingStatistics) {
		this.votingStatistics = votingStatistics;
	}

	public List<PickListItem> getPickListItems() {
		return pickListItems;
	}

	public void setPickListItems(List<PickListItem> pickListItems) {
		this.pickListItems = pickListItems;
	}

	public ApproveVotingStatisticsDto getApproveVotingStatistics() {
		return approveVotingStatistics;
	}

	public void setApproveVotingStatistics(ApproveVotingStatisticsDto approveVotingStatistics) {
		this.approveVotingStatistics = approveVotingStatistics;
	}

	public VotingService getVotingService() {
		return votingService;
	}

	public boolean isVisResultat() {
		return visResultat;
	}

	public void setVisResultat(boolean visResultat) {
		this.visResultat = visResultat;
	}

	public String getSelectedPollingPlaceName() {
		return "";
	}

	public String getDenneSidenURL() {
		return denneSidenURL;
	}

	public ProvingSamletForm getForm() {
		return form;
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return getPageTitleMetaBuilder().area(getKommune());
	}
}
