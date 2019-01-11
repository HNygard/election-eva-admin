package no.valg.eva.admin.frontend.electoralroll.ctrls;

import no.evote.exception.EvoteException;
import no.evote.model.views.VoterAudit;
import no.evote.service.SpecialPurposeReportService;
import no.valg.eva.admin.common.voter.service.VoterAuditService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

@Named
@ViewScoped
public class VoterAuditController extends KontekstAvhengigController {
	private static final Logger LOGGER = Logger.getLogger(VoterAuditController.class);

	private VoterAuditService voterAuditService;
	private SpecialPurposeReportService specialPurposeReportService;

	private ElectionEvent electionEvent;
	private List<VoterAudit> voterAuditList;
	private MvArea kommune;
	private String selectedEndringsType;
	private String selectedSearchMode;
	private boolean searchOnlyApproved;
	private LocalDate startDate;
	private LocalDate endDate;

	@SuppressWarnings("unused")
	public VoterAuditController() {
		// CDI
	}

	@Inject
	public VoterAuditController(VoterAuditService voterAuditService, SpecialPurposeReportService specialPurposeReportService) {
		this.voterAuditService = voterAuditService;
		this.specialPurposeReportService = specialPurposeReportService;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		if (getUserData().isElectionEventAdminUser()) {
			setup.leggTil(geografi(KOMMUNE));
		}
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		kommune = getMvAreaService().findSingleByPath(kontekst.kommuneSti());
		populateUserData();
		resetSearchFields();

	}

	public void searchVoterAudit() {
		if (endDate.isBefore(startDate)) {
			showMessage(getMessageProvider().get("@common.date.invalidRange"), FacesMessage.SEVERITY_ERROR);
			return;
		}
		voterAuditList = voterAuditService.getHistoryForMunicipality(getUserData(), kommune.getMunicipalityId(), selectedEndringsType.charAt(0), startDate,
				endDate, electionEvent.getPk(), selectedSearchMode, searchOnlyApproved);
		if (voterAuditList.isEmpty()) {
			showMessage(getMessageProvider().get("@voterAudit.noChanges"), FacesMessage.SEVERITY_WARN);
		}
	}

	public void makeReport() {
		try {
			byte[] bytes = specialPurposeReportService.generateElectoralRollHistoryForMunicipality(getUserData(), kommune.getMunicipality(),
					selectedEndringsType.charAt(0),
					startDate, endDate, electionEvent.getPk(), selectedSearchMode, searchOnlyApproved);
			FacesUtil.sendFile("voterAudit.pdf", bytes);
		} catch (IOException | EvoteException e) {
			LOGGER.error(e.getMessage(), e);
			MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}

	private void resetSearchFields() {
		selectedEndringsType = " ";
		selectedSearchMode = " ";
		searchOnlyApproved = true;
		startDate = null;
		endDate = null;
	}

	private void populateUserData() {
		electionEvent = getUserDataController().getElectionEvent();
	}

	private void showMessage(final String message, final Severity severityInfo) {
		getFacesContext().addMessage("", new FacesMessage(severityInfo, message, message));
	}

	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public List<VoterAudit> getVoterAuditList() {
		return voterAuditList;
	}

	public void setVoterAuditList(final List<VoterAudit> voterAuditList) {
		this.voterAuditList = voterAuditList;
	}

	public String getSelectedEndringsType() {
		return selectedEndringsType;
	}

	public void setSelectedEndringsType(final String selectedEndringsType) {
		this.selectedEndringsType = selectedEndringsType;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(final LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(final LocalDate endDate) {
		this.endDate = endDate;
	}

	public String getSelectedSearchMode() {
		return selectedSearchMode;
	}

	public void setSelectedSearchMode(final String selectedSearchMode) {
		this.selectedSearchMode = selectedSearchMode;
	}

	public boolean isSearchOnlyApproved() {
		return searchOnlyApproved;
	}

	public void setSearchOnlyApproved(final boolean searchOnlyApproved) {
		this.searchOnlyApproved = searchOnlyApproved;
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return getPageTitleMetaBuilder().area(kommune);
	}
}
