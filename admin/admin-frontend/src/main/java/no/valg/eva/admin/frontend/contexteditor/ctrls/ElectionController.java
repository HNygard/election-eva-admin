package no.valg.eva.admin.frontend.contexteditor.ctrls;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;

@Named
@ConversationScoped
public class ElectionController extends BaseElectionController {
	static final BigDecimal DEFAULT_SETTLEMENT_FIRST_DIVISOR = BigDecimal.valueOf(1.4);
	static final int DEFAULT_NAME_LENGTH = 25;
	static final int DEFAULT_RESIDENCE_LENGTH = 20;

	// Injected
	private ElectionService electionService;
	private MvElectionService mvElectionService;
	private MvAreaService mvAreaService;

	private String activeIndex;
	private Election currentElection;
	private String selectedAreaLevel;
	private boolean brukStemmetillegg;
	private MinMaxCandidateType minCandidatesType;
	private MinMaxCandidateType maxCandidatesType;
	private List<AreaLevel> areaLevelList;

	public ElectionController() {
		// For CDI
	}

	@Inject
	public ElectionController(ElectionService electionService, MvElectionService mvElectionService, MvAreaService mvAreaService) {
		this.electionService = electionService;
		this.mvElectionService = mvElectionService;
		this.mvAreaService = mvAreaService;
	}

	@PostConstruct
	public void init() {
		areaLevelList = mvAreaService.findAllAreaLevels(getUserData());
	}

	/**
	 * @see MvElectionPickerController#injectParentMvElection(int)
	 */
	public void prepareForCreate(ElectionPath parentElectionPath, String electionGroupName) {
		resetNewElection(parentElectionPath, electionGroupName);
		currentElection.setAutoGenerateContests(false);
		setSelectedAreaLevel(null);
		setActiveIndex("0");
		setBrukStemmetillegg(false);
		setMaxCandidatesType(null);
		setMinCandidatesType(null);
	}

	public void doCreateElection(Election election) {
		if (checkRenumberLogic(election)) {
			execute(() -> {
				election.setAreaLevel(Integer.parseInt(getSelectedAreaLevel()));
				correctMaxCandidates(election);
				correctMinCandidates(election);
				if (checkSaveElectionResponse(electionService.save(getUserData(), election), election)) {
					getMvElectionPickerController().update(ElectionLevelEnum.ELECTION.getLevel(), election.getElectionPath().path());
					MessageUtil.buildDetailMessage(MessageUtil.CREATE_SUBLEVEL_SUCCESSFUL_KEY, getSummaryParams(election, false), FacesMessage.SEVERITY_INFO);
					resetNewElection(election.getParentElectionPath(), election.getElectionGroupName());
					closeDialogAndUpdateHierarchyEditor("createElectionLevel2Widget");
					return;
				}
			});
		}
	}

	/**
	 * @see MvElectionPickerController#editMvElection(MvElectionMinimal)
	 */
	public void prepareForUpdate(MvElectionMinimal mvElectionMini) {
		MvElection mvElection = mvElectionService.findByPk(mvElectionMini.getPk());
		currentElection = electionService.get(getUserData(), mvElection.electionPath());
		selectedAreaLevel = String.valueOf(currentElection.getAreaLevel());
		setReadOnly(false);
		setActiveIndex("0");
		setBrukStemmetillegg(currentElection.getBaselineVoteFactor() != null);
		setMinCandidatesType(resolveMinMaxCandidateType(currentElection.getMinCandidatesAddition(), currentElection.getMinCandidates()));
		setMaxCandidatesType(resolveMinMaxCandidateType(currentElection.getMaxCandidatesAddition(), currentElection.getMaxCandidates()));
	}

	public void doUpdateElection(Election election) {
		if (checkRenumberLogic(election)) {
			correctMaxCandidates(election);
			correctMinCandidates(election);
			execute(() -> {
				if (checkSaveElectionResponse(electionService.save(getUserData(), getCurrentElection()), election)) {
					getMvElectionPickerController().update(ElectionLevelEnum.ELECTION.getLevel(), null);
					MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_INFO, MessageUtil.UPDATE_SUCCESSFUL_KEY);
					closeDialogAndUpdateHierarchyEditor("editElectionLevel2Widget");
					return;
				}
			});
		}
	}

	public void doDeleteElection(Election election) {
		if (!isCurrentRemovable()) {
			return;
		}
		if (execute(() -> {
			electionService.delete(getUserData(), election.getElectionPath());
			getMvElectionPickerController().update(ElectionLevelEnum.ELECTION.getLevel(), null);
		})) {
			MessageUtil.buildDetailMessage(MessageUtil.DELETE_FROM_LEVEL_SUCCESSFUL_KEY, getSummaryParams(election, false), FacesMessage.SEVERITY_INFO);
		}
	}

	public List<SelectItem> getMinMaxCandidateTypes() {
		return Arrays.asList(
				selectItem(MinMaxCandidateType.MIN_MAX_ADDITION),
				selectItem(MinMaxCandidateType.MIN_MAX),
				selectItem(MinMaxCandidateType.OVERRIDE));
	}

	void correctMaxCandidates(Election election) {
		if (getMaxCandidatesType() == MinMaxCandidateType.MIN_MAX_ADDITION) {
			election.setMaxCandidates(null);
		} else if (getMaxCandidatesType() == MinMaxCandidateType.MIN_MAX) {
			election.setMaxCandidatesAddition(null);
		} else {
			election.setMaxCandidatesAddition(null);
			election.setMaxCandidates(null);
		}
	}

	void correctMinCandidates(Election election) {
		if (getMinCandidatesType() == MinMaxCandidateType.MIN_MAX_ADDITION) {
			election.setMinCandidates(null);
		} else if (getMinCandidatesType() == MinMaxCandidateType.MIN_MAX) {
			election.setMinCandidatesAddition(null);
		} else {
			election.setMinCandidatesAddition(null);
			election.setMinCandidates(null);
		}
	}

	private SelectItem selectItem(MinMaxCandidateType type) {
		return new SelectItem(type, getMessageProvider().get(type.getLabel()));
	}

	private void resetNewElection(ElectionPath parentElectionPath, String electionGroupName) {
		currentElection = new Election(parentElectionPath);
		currentElection.setGenericElectionType(GenericElectionType.F);
		currentElection.setElectionGroupName(electionGroupName);
		currentElection.setId("");
		currentElection.setName("");
		currentElection.setPenultimateRecount(true);
		currentElection.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		currentElection.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);
		currentElection.setSettlementFirstDivisor(DEFAULT_SETTLEMENT_FIRST_DIVISOR);
		currentElection.setCandidatesInContestArea(true);
		currentElection.setSingleArea(true);
		currentElection.setMaxCandidateNameLength(DEFAULT_NAME_LENGTH);
		currentElection.setMaxCandidateResidenceProfessionLength(DEFAULT_RESIDENCE_LENGTH);
	}

	public Election getCurrentElection() {
		return currentElection;
	}

	public String getSelectedAreaLevel() {
		return selectedAreaLevel;
	}

	public void setSelectedAreaLevel(final String selectedAreaLevel) {
		this.selectedAreaLevel = selectedAreaLevel;
	}

	public String getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(String activeIndex) {
		this.activeIndex = activeIndex;
	}

	public boolean isBrukStemmetillegg() {
		return brukStemmetillegg;
	}

	public void setBrukStemmetillegg(boolean brukStemmetillegg) {
		this.brukStemmetillegg = brukStemmetillegg;
	}

	public MinMaxCandidateType getMinCandidatesType() {
		return minCandidatesType;
	}

	public void setMinCandidatesType(MinMaxCandidateType minCandidatesType) {
		this.minCandidatesType = minCandidatesType;
	}

	public MinMaxCandidateType getMaxCandidatesType() {
		return maxCandidatesType;
	}

	public void setMaxCandidatesType(MinMaxCandidateType maxCandidatesType) {
		this.maxCandidatesType = maxCandidatesType;
	}

	public boolean isEdit() {
		return currentElection != null && currentElection.getElectionRef() != null;
	}

	public void valueChangeSingleArea(final ValueChangeEvent evt) {
		if ((Boolean) evt.getNewValue()) {
			currentElection.setSingleArea(true);
		} else {
			currentElection.setSingleArea(false);
		}
	}

	public Valgtype[] getValgtyper() {
		return Valgtype.values();
	}

	private String[] getSummaryParams(Election election, boolean levelInfo) {
		if (levelInfo) {
			return new String[] { "@election_level[2].name", election.getId(), election.getElectionGroupName() };
		}
		return new String[] { election.getName(), election.getElectionGroupName() };
	}

	private boolean checkRenumberLogic(Election election) {
		if (election.isRenumberLogicAllowed()) {
			return true;
		}
		MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, "@election.election.renumberConstraint");
		return false;
	}

	private boolean checkSaveElectionResponse(SaveElectionResponse saveElectionResponse, Election election) {
		if (saveElectionResponse.idNotUniqueError()) {
			MessageUtil.buildDetailMessage(MessageUtil.CHOOSE_UNIQUE_ID, getSummaryParams(election, true), FacesMessage.SEVERITY_ERROR);
			return false;
		}
		return true;
	}

	MinMaxCandidateType resolveMinMaxCandidateType(Integer minMaxCandidateAddition, Integer minMaxCandidate) {
		if (minMaxCandidateAddition != null) {
			return MinMaxCandidateType.MIN_MAX_ADDITION;
		} else if (minMaxCandidate != null) {
			return MinMaxCandidateType.MIN_MAX;
		}
		return MinMaxCandidateType.OVERRIDE;
	}

	public enum MinMaxCandidateType {
		MIN_MAX_ADDITION, MIN_MAX, OVERRIDE;

		public String getLabel() {
			return "@election.election.minMaxCandidateType[" + this.name() + "].name";
		}
	}

	public List<AreaLevel> getAreaLevelList() {
		return areaLevelList;
	}
}
