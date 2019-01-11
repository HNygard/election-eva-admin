package no.valg.eva.admin.frontend.listproposal.ctrls;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import no.evote.dto.ListProposalValidationData;
import no.evote.security.UserData;
import no.evote.service.configuration.AffiliationService;
import no.evote.service.configuration.CandidateService;
import no.evote.service.configuration.ContestAreaService;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.util.ExcelUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections.CollectionUtils.subtract;
import static org.apache.commons.lang3.StringUtils.join;

@Named
@ViewScoped
public class CandidateUploadController extends BaseController {
	public static final String TEMPLATE_NO_BM = "/resources/files/ListeforslagMal.xlsx";
	public static final String TEMPLATE_NO_NN = "/resources/files/ListeframleggMal.xlsx";
	private static final ImmutableSet<String> MANDATORY_COLUMNS = of("B", "D", "E");
	private static final int FIRST_ROW_STARTS_AT = 2;

	private static final Predicate<Pair<String, String>> ONLY_NON_NULL = new Predicate<Pair<String, String>>() {
		@Override
		public boolean apply(Pair<String, String> nullableCell) {
			return nullableCell != null && nullableCell.getKey() != null && nullableCell.getValue() != null;
		}
	};

	private static final Function<Pair<String, String>, String> EXTRACT_COLUMN_LETTER = new Function<Pair<String, String>, String>() {
		@Override
		public String apply(Pair<String, String> cell) {
			return cell.getKey().substring(0, 1);
		}
	};

	@Inject
	private UserData userData;
	@Inject
	private MessageProvider mms;
	@Inject
	private CandidateService candidateService;
	@Inject
	private AffiliationService affiliationService;
	@Inject
	private ContestAreaService contestAreaService;
	@Inject
	private RedigerListeforslagController redigerListeforslagController;
	@Inject
	private CandidateController candidateController;

	private List<Candidate> candidateList;
	private int maximumBaselineVotes;
	private Affiliation currentAffiliation;
	private boolean writeOverCandidates;

	private String cid;

	public void setAffiliationPk(final Long affiliationPk) {
		currentAffiliation = affiliationService.findByPk(userData, affiliationPk);
		candidateList = candidateService.findByAffiliation(userData, affiliationPk);
		maximumBaselineVotes = getMaximumBaselineVotes(currentAffiliation.getBallot().getContest().getNumberOfPositions());
		writeOverCandidates = false;
	}

	/**
	 * Imports file. Deletes all existing candidates if writeOverCandidates flag is set.
	 */
	public void fileImport(FileUploadEvent fileUploadEvent) {
		if (writeOverCandidates) {
			candidateService.deleteAll(userData, candidateList);
		}
		Contest contest = currentAffiliation.getBallot().getContest();
		MvArea mvArea = getMvAreaFromContest(contest);

		List<Candidate> importedCandidates = getCandidatesFromFile(fileUploadEvent.getFile(), currentAffiliation, maximumBaselineVotes, mvArea);
		importCandidates(importedCandidates);
	}

	private List<Candidate> getCandidatesFromFile(final UploadedFile file, final Affiliation affiliation, final int maximumBaselineVotes, final MvArea mvArea) {
		if (file != null) {
			try {
				InputStream inStream = file.getInputstream();
				List<List<Pair<String, String>>> candidateRows;
				try {
					if (file.getFileName().endsWith(".xlsx") || file.getFileName().endsWith(".xls")) {
						candidateRows = ExcelUtil.getRowDataFromExcelFile(inStream).getRows();
					} else {
						throw new RuntimeException("Invalid file type uploaded");
					}

					final int[] rowNumber = { 0 };
					boolean foundErrors = false;
					for (List<Pair<String, String>> row : candidateRows) {
                        if (row.isEmpty()) {
                            continue;
                        }
						Set<String> cellReferenceColumnParts = cellReferenceColumnPartsOf(row);
						if (columnsAreMissingFrom(cellReferenceColumnParts)) {
							buildInvalidFormatCellsMessage(commaSeparatedCellReferences(rowNumber[0], cellReferenceColumnParts));
							foundErrors = true;
						}
						rowNumber[0]++;
					}
					if (!foundErrors) {
						return getCandidates(affiliation, maximumBaselineVotes, mvArea, candidateRows);
					} else {
						getRequestContext().execute("PF('candidateAjaxUploadWidget').uploadedFileCount = 0;");
					}
				} catch (InvalidFormatException | OfficeXmlFileException e) {
					buildInvalidFormatMessage();
				}
				return null;
			} catch (IOException e) {
				buildIOErrorMessage();
				return null;
			}
		} else {
			return null;
		}
	}

	private String commaSeparatedCellReferences(final int i, Set<String> cellReferenceColumnParts) {
		return join(new TreeSet<>(transform(new ArrayList<String>(subtract(MANDATORY_COLUMNS, cellReferenceColumnParts)),
				new Function<String, String>() {
					@Override
					public String apply(String cellColumnLetters) {
						return cellColumnLetters + cellRowNumber(i);
					}
				})), ", ");
	}

	private int cellRowNumber(int i) {
		return (FIRST_ROW_STARTS_AT + i);
	}

	private Set<String> cellReferenceColumnPartsOf(List<Pair<String, String>> row) {
		return new HashSet<>(transform(newArrayList(filter(row, ONLY_NON_NULL)), EXTRACT_COLUMN_LETTER));
	}

	private boolean columnsAreMissingFrom(Set<String> cellReferenceColumnParts) {
		return !cellReferenceColumnParts.containsAll(MANDATORY_COLUMNS);
	}

	private void buildIOErrorMessage() {
		MessageUtil.buildDetailMessage(mms.get("@listProposal.candidateList.ioerror"), FacesMessage.SEVERITY_ERROR);
	}

	private void buildInvalidFormatMessage() {
		MessageUtil.buildDetailMessage(mms.get("@listProposal.candidateList.invalidFormat"), FacesMessage.SEVERITY_ERROR);
	}

	private void buildInvalidFormatCellsMessage(String cellReference) {
		MessageUtil.buildDetailMessage(mms.get("@listProposal.candidateList.invalidFormat.in.cell", cellReference), FacesMessage.SEVERITY_ERROR);
	}

	private List<Candidate> getCandidates(Affiliation affiliation, int maximumBaselineVotes, MvArea mvArea, List<List<Pair<String, String>>> rowCandidates) {
		return candidateService.convertRowsToCandidateList(userData, newArrayList(transform(rowCandidates, ExcelUtil.VALUES_FROM_PAIRS_F::apply)),
				affiliation, maximumBaselineVotes, mvArea);
	}

	private void importCandidates(final List<Candidate> importedCandidates) {
		if (importedCandidates != null) {

			List<Candidate> newCandidateList = new ArrayList<>();
			newCandidateList.addAll(candidateList);
			newCandidateList.addAll(importedCandidates);

			ListProposalValidationData validationData = candidateService.isCandidatesValid(userData, newCandidateList, currentAffiliation.getBallot().getPk(),
					maximumBaselineVotes);

			if (validationData.isApproved()) {
				int candidatesImported = importedCandidates.size();
				candidateService
						.createAllBelow(userData, validationData.getCandidateList(), currentAffiliation.getPk(), currentAffiliation.getBallot().getPk());
				if (candidatesImported == 1) {
					MessageUtil.buildDetailMessage(candidatesImported + " " + mms.get("@listProposal.candidateListImported.singular"),
							FacesMessage.SEVERITY_INFO);
				} else {
					MessageUtil.buildDetailMessage(candidatesImported + " " + mms.get("@listProposal.candidateListImported.plural"),
							FacesMessage.SEVERITY_INFO);
				}
				redigerListeforslagController.updateProposalPersons();
				candidateController.getUploadCandidatesDialog().closeAndUpdate("editListProposalForm:msg", "editListProposalForm:tabs:candidatesDataTable");
			} else {
				showValidationMessage(validationData.getCandidateList());
			}
		} else {
			MessageUtil.buildDetailMessage(mms.get("@listProposal.candidateList.couldNotRead"), FacesMessage.SEVERITY_ERROR);
		}
	}

	private MvArea getMvAreaFromContest(final Contest contest) {
		if (contest.getElection().isSingleArea()) {
			List<ContestArea> contestAreaList = contestAreaService.findContestAreasForContest(contest.getPk());
			if (contestAreaList.size() == 1) {
				return contestAreaList.get(0).getMvArea();
			}
		}
		return null;
	}

	private int getMaximumBaselineVotes(final int members) {
		
		if (members >= 11 && members <= 23) {
			return 4;
		}
		if (members >= 25 && members <= 53) {
			return 6;
		}
		if (members >= 55) {
			return 10;
		}
		return 0;
		
	}

	private void showValidationMessage(final List<Candidate> candidateList) {
		int i = 1;
		String format = "%s %d# %s";

		for (Candidate candidate : candidateList) {
			if (candidate.getPk() == null) {
				i++;
				if (candidate.isInvalid()) {
					MessageUtil.buildDetailMessage(String.format(format, mms.get("@listProposal.candidateList.excelRow"), i,
							translate(candidate.getValidationMessageList())),
							FacesMessage.SEVERITY_ERROR);
				}
			}
		}
	}

	private String translate(List<UserMessage> messages) {
		return messages.stream().map(message -> mms.get(message.getMessage(), message.getArgs())).collect(joining(", "));
	}

	public String downloadCandidateFileTemplate() {
		switch (userData.getLocale().getId()) {
		case "nn-NO":
			return TEMPLATE_NO_NN;
		case "nb-NO":
		default:
			return TEMPLATE_NO_BM;
		}
	}

	/**
	 * For keeping the conversation alive
	 */
	public String getCid() {
		return cid;
	}

	public void setCid(final String cid) {
		this.cid = cid;
	}

	public boolean isWriteOverCandidates() {
		return writeOverCandidates;
	}

	public void setWriteOverCandidates(boolean writeOverCandidates) {
		this.writeOverCandidates = writeOverCandidates;
	}
}
