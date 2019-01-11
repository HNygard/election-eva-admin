package no.valg.eva.admin.frontend.listproposal.ctrls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ErrorCode;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.CandidateService;
import no.valg.eva.admin.util.CSVUtil;
import no.valg.eva.admin.util.ExcelUtil;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;

import org.apache.log4j.Logger;
import org.primefaces.event.ReorderEvent;

@Named
@ViewScoped
public class VelgRedigerListeforslagController extends ListeforslagBaseController implements ErrorCodeHandler {

	private static final Logger LOGGER = Logger.getLogger(VelgRedigerListeforslagController.class);

	@Inject
	private CandidateService candidateService;

	private List<Affiliation> affiliationList;
	private Kontekst kontekst;

	@Override
	public void initialized(Kontekst kontekst) {
		super.initialized(kontekst);
		this.kontekst = kontekst;
		affiliationList = new ArrayList<>();
		updateAffiliations();
	}

	public void candidateMergeFile(String type) {
		List<Candidate> candidates = new ArrayList<>();

		List<Affiliation> affiliations = getAffiliationService().findByContest(getUserData(), getContest().getPk());
		for (Affiliation affiliation : affiliations) {
			String partyName = getMessageProvider().get(affiliation.getParty().getName());
			for (Candidate candidate : candidateService.findByAffiliation(getUserData(), affiliation.getPk())) {
				candidate.setPartyName(partyName);
				candidates.add(candidate);
			}
		}

		List<List<String>> candidatesData = generateMergeFileData(candidates);
		createMergeFile(type, candidatesData);
	}

	public void onRowReorder(ReorderEvent event) {
		// If reorder among non-approved, rollback
		Affiliation source = affiliationList.get(event.getToIndex());
		if (!source.isApproved()
				|| (event.getFromIndex() < event.getToIndex() && event.getToIndex() > 0 && !affiliationList.get(event.getToIndex() - 1).isApproved())) {
			// Invalid reorder, rollback!
			affiliationList.add(event.getFromIndex(), affiliationList.remove(event.getToIndex()));
			return;
		}

		execute(() -> {
			List<Affiliation> list = getAffiliationService().changeDisplayOrder(getUserData(), source, event.getFromIndex() + 1, event.getToIndex() + 1);
			// Swap in sublist
			if (list.size() == affiliationList.size()) {
				affiliationList = list;
			} else {
				int start = event.getFromIndex() < event.getToIndex() ? event.getFromIndex() : event.getToIndex();
				int counter = 0;
				for (int i = start; i < start + list.size(); i++) {
					affiliationList.remove(i);
					affiliationList.add(i, list.get(counter++));
				}
			}
		}, this);
	}

	public void editListProposal(Affiliation affiliation) {
		if (affiliation.getBallot().getId().equals(EvoteConstants.BALLOT_BLANK)) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.canNotEditBlankBallot"), FacesMessage.SEVERITY_INFO);
			return;
		}

		redirectTilRediger(affiliation, "chooseEditListProposal.xhtml");
	}

	public boolean isContestCountBegun() {
		return false;
	}

	@Override
	public String onError(ErrorCode errorCode, String... params) {
		if (isOptimisticLockingException(errorCode)) {
			initialized(kontekst);
			return getMessageProvider().get("@listProposal.save.optimisticLockingException");
		}
		return null;
	}

	private void updateAffiliations() {
		affiliationList.clear();
		affiliationList.addAll(getAffiliationService().findByContest(getUserData(), getContest().getPk()));
	}

	private List<List<String>> generateMergeFileData(final List<Candidate> candidates) {
		List<List<String>> candidatesData = new ArrayList<>();
		for (Candidate candidate : candidates) {
			List<String> candidateRow = new ArrayList<>();
			candidateRow.add(Integer.toString(candidate.getDisplayOrder()));
			candidateRow.add(candidate.getPartyName());
			candidateRow.add(candidate.toString());
			candidateRow.add(candidate.getAddressLine1());
			candidateRow.add(candidate.getPostalCode());
			candidateRow.add(candidate.getPostTown());
			candidateRow.add(candidate.getEmail());
			candidatesData.add(candidateRow);
		}
		return candidatesData;
	}

	private void createMergeFile(final String type, final List<List<String>> candidatesData) {
		byte[] bytes;
		Contest contest = getContest();
		String contestName = contest.getName().toLowerCase().replaceAll("\\.\\s+", "-");
		String electionName = contest.getElection().getName().toLowerCase().replaceAll("\\.\\s+", "-");
		String filename = "flettefil-" + electionName + "-" + contestName;
		String fileExtension;
		String contentType;

		switch (type) {
		case "CSV":
			bytes = CSVUtil.createCsvFromRowData(candidatesData);
			contentType = "text/csv";
			fileExtension = ".csv";
			break;
		case "XLS":
			bytes = ExcelUtil.createXlsFromRowData(candidatesData);
			contentType = "application/ms-excel";
			fileExtension = ".xls";
			break;
		case "XLSX":
			bytes = ExcelUtil.createXlsxFromRowData(candidatesData);
			contentType = "application/ms-excel";
			fileExtension = ".xlsx";
			break;
		default:
			throw new IllegalArgumentException("Only CSV, XLS and XLSX is supported!");
		}

		try {
			FacesUtil.sendFile(filename + fileExtension, bytes, contentType);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public List<Affiliation> getAffiliationList() {
		return affiliationList;
	}
}
