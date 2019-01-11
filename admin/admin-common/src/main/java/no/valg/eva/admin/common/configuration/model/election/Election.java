package no.valg.eva.admin.common.configuration.model.election;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.counting.model.configuration.ElectionRef;
import no.valg.eva.admin.configuration.domain.model.Valgtype;

import org.joda.time.LocalDate;

/**
 * Common Election object in a context crossing the front end and back end components
 */
public class Election extends VersionedObject {

	private String id;
	private String name;
	private String electionGroupName;
	private ElectionPath electionPath;
	private ElectionPath parentElectionPath;
	private GenericElectionType genericElectionType;
	private Valgtype valgtype;
	private int areaLevel;
	private boolean singleArea;
	private boolean renumber;
	private boolean renumberLimit;
	private boolean writein;
	private boolean writeinLocalOverride;
	private boolean strikeout;
	private boolean personal;
	private boolean penultimateRecount;
	private boolean candidatesInContestArea;
	private LocalDate endDateOfBirth;
	private BigDecimal baselineVoteFactor;
	private BigDecimal candidateRankVoteShareThreshold;
	private BigDecimal settlementFirstDivisor;
	private BigDecimal levelingSeatsVoteShareThreshold;
	private int levelingSeats;
	private ElectionRef electionRef;
	private int maxCandidateNameLength;
	private int maxCandidateResidenceProfessionLength;
	private Integer minCandidates;
	private Integer minCandidatesAddition;
	private Integer maxCandidates;
	private Integer maxCandidatesAddition;
	private boolean isAutoGenerateContests;

	public Election(ElectionPath parentElectionPath) {
		this(parentElectionPath, 0);
	}

	public Election(ElectionPath parentElectionPath, int version) {
		super(version);
		this.parentElectionPath = requireNonNull(parentElectionPath);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		if (id != null && !id.trim().isEmpty()) { // JSF guard
			electionPath = parentElectionPath.add(id);
		}
	}

	public boolean isRenumberLogicAllowed() {
		boolean isRenumberAndPersonalOrWriteinElection = this.isRenumber() && (this.isPersonal() || this.isWritein());
		boolean isRenumberOrNotRenumberLimit = this.isRenumber() || !this.isRenumberLimit();
		return !isRenumberAndPersonalOrWriteinElection && isRenumberOrNotRenumberLimit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getElectionGroupName() {
		return electionGroupName;
	}

	public void setElectionGroupName(String electionGroupName) {
		this.electionGroupName = electionGroupName;
	}

	public ElectionPath getElectionPath() {
		return electionPath;
	}

	public void setElectionPath(ElectionPath electionPath) {
		this.electionPath = electionPath;
	}

	public GenericElectionType getGenericElectionType() {
		return genericElectionType;
	}

	public void setGenericElectionType(GenericElectionType genericElectionType) {
		this.genericElectionType = genericElectionType;
	}

	public Valgtype getValgtype() {
		return valgtype;
	}

	public void setValgtype(Valgtype valgtype) {
		this.valgtype = valgtype;
	}

	public int getAreaLevel() {
		return areaLevel;
	}

	public void setAreaLevel(int areaLevel) {
		this.areaLevel = areaLevel;
	}

	public boolean isSingleArea() {
		return singleArea;
	}

	public void setSingleArea(boolean singleArea) {
		this.singleArea = singleArea;
	}

	public boolean isRenumber() {
		return renumber;
	}

	public void setRenumber(boolean renumber) {
		this.renumber = renumber;
	}

	public boolean isRenumberLimit() {
		return renumberLimit;
	}

	public void setRenumberLimit(boolean renumberLimit) {
		this.renumberLimit = renumberLimit;
	}

	public boolean isWritein() {
		return writein;
	}

	public void setWritein(boolean writein) {
		this.writein = writein;
	}

	public boolean isStrikeout() {
		return strikeout;
	}

	public void setStrikeout(boolean strikeout) {
		this.strikeout = strikeout;
	}

	public boolean isPersonal() {
		return personal;
	}

	public void setPersonal(boolean personal) {
		this.personal = personal;
	}

	public boolean isPenultimateRecount() {
		return penultimateRecount;
	}

	public void setPenultimateRecount(boolean penultimateRecount) {
		this.penultimateRecount = penultimateRecount;
	}

	public boolean isCandidatesInContestArea() {
		return candidatesInContestArea;
	}

	public void setCandidatesInContestArea(boolean candidatesInContestArea) {
		this.candidatesInContestArea = candidatesInContestArea;
	}

	public LocalDate getEndDateOfBirth() {
		return endDateOfBirth;
	}

	public void setEndDateOfBirth(LocalDate endDateOfBirth) {
		this.endDateOfBirth = endDateOfBirth;
	}

	public BigDecimal getBaselineVoteFactor() {
		return baselineVoteFactor;
	}

	public void setBaselineVoteFactor(BigDecimal baselineVoteFactor) {
		this.baselineVoteFactor = baselineVoteFactor;
	}

	public BigDecimal getCandidateRankVoteShareThreshold() {
		return candidateRankVoteShareThreshold;
	}

	public void setCandidateRankVoteShareThreshold(BigDecimal candidateRankVoteShareThreshold) {
		this.candidateRankVoteShareThreshold = candidateRankVoteShareThreshold;
	}

	public BigDecimal getSettlementFirstDivisor() {
		return settlementFirstDivisor;
	}

	public void setSettlementFirstDivisor(BigDecimal settlementFirstDivisor) {
		this.settlementFirstDivisor = settlementFirstDivisor;
	}

	public BigDecimal getLevelingSeatsVoteShareThreshold() {
		return levelingSeatsVoteShareThreshold;
	}

	public void setLevelingSeatsVoteShareThreshold(BigDecimal levelingSeatsVoteShareThreshold) {
		this.levelingSeatsVoteShareThreshold = levelingSeatsVoteShareThreshold;
	}

	public int getLevelingSeats() {
		return levelingSeats;
	}

	public void setLevelingSeats(int levelingSeats) {
		this.levelingSeats = levelingSeats;
	}

	public ElectionPath getParentElectionPath() {
		return parentElectionPath;
	}

	public ElectionRef getElectionRef() {
		return electionRef;
	}

	public void setElectionRef(ElectionRef electionRef) {
		this.electionRef = electionRef;
	}

	public boolean isWriteinLocalOverride() {
		return writeinLocalOverride;
	}

	public void setWriteinLocalOverride(boolean writeinLocalOverride) {
		this.writeinLocalOverride = writeinLocalOverride;
	}

	public int getMaxCandidateNameLength() {
		return maxCandidateNameLength;
	}

	public void setMaxCandidateNameLength(int maxCandidateNameLength) {
		this.maxCandidateNameLength = maxCandidateNameLength;
	}

	public int getMaxCandidateResidenceProfessionLength() {
		return maxCandidateResidenceProfessionLength;
	}

	public void setMaxCandidateResidenceProfessionLength(int maxCandidateResidenceProfessionLength) {
		this.maxCandidateResidenceProfessionLength = maxCandidateResidenceProfessionLength;
	}

	public Integer getMinCandidates() {
		return minCandidates;
	}

	public void setMinCandidates(Integer minCandidates) {
		this.minCandidates = minCandidates;
	}

	public Integer getMinCandidatesAddition() {
		return minCandidatesAddition;
	}

	public void setMinCandidatesAddition(Integer minCandidatesAddition) {
		this.minCandidatesAddition = minCandidatesAddition;
	}

	public Integer getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(Integer maxCandidates) {
		this.maxCandidates = maxCandidates;
	}

	public Integer getMaxCandidatesAddition() {
		return maxCandidatesAddition;
	}

	public void setMaxCandidatesAddition(Integer maxCandidatesAddition) {
		this.maxCandidatesAddition = maxCandidatesAddition;
	}

	public boolean isAutoGenerateContests() {
		return isAutoGenerateContests;
	}

	public void setAutoGenerateContests(boolean autoGenerateContests) {
		isAutoGenerateContests = autoGenerateContests;
	}
}
