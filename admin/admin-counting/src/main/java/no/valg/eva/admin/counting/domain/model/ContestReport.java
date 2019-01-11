package no.valg.eva.admin.counting.domain.model;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static no.evote.exception.ErrorCode.ERROR_CODE_0504_STALE_OBJECT;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.VoteCountStatusEnum;
import no.evote.exception.EvoteException;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.persistence.EntityWriteListener;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents contest protocols with optional counts. The reporting is performed for a contest within the authority of the reporting unit
 */
@Entity
@EntityListeners({EntityWriteListener.class})
@Table(name = "contest_report", uniqueConstraints = {@UniqueConstraint(columnNames = {"reporting_unit_pk", "contest_pk"})})
@AttributeOverride(name = "pk", column = @Column(name = "contest_report_pk"))
@NamedQueries({
		@NamedQuery(name = "ContestReport.findByReportingUnitContest", query = "SELECT cr FROM ContestReport cr WHERE cr.reportingUnit.pk = :rupk "
				+ "AND cr.contest.pk = :cpk"),
		@NamedQuery(name = "ContestReport.findByContest", query = "SELECT cr FROM ContestReport cr WHERE cr.contest.pk = :cpk"),
		@NamedQuery(
				name = "ContestReport.findByContestAndMunicipality",
				query = "SELECT cr "
						+ "FROM ContestReport cr "
						+ "WHERE cr.contest.pk = :contestPk AND cr.reportingUnit.mvArea.municipality.pk = :municipalityPk"),
		@NamedQuery(
				name = "ContestReport.findByElectionGroupAndMunicipality",
				query = "SELECT cr "
						+ "FROM ContestReport cr "
						+ "WHERE cr.contest.election.electionGroup.pk = :electionGroupPk AND cr.reportingUnit.mvArea.municipality.pk = :municipalityPk"),
		@NamedQuery(
				name = "ContestReport.findByContestAndMvArea",
				query = "SELECT cr "
						+ "FROM ContestReport cr "
						+ "WHERE cr.contest.pk = :contestPk AND cr.reportingUnit.mvArea.pk = :mvAreaPk"),
		@NamedQuery(name = "ContestReport.countByContest", query = "SELECT count(cr.pk) FROM ContestReport cr WHERE cr.contest.pk = :cpk"),
		@NamedQuery(
				name = "ContestReport.findByCountPk",
				query = "SELECT cr FROM ContestReport cr, VoteCount vc WHERE vc in elements(cr.voteCountSet) and vc.pk = :cpk"),
		@NamedQuery(
				name = "ContestReport.findByBallotCount",
				query = "select cr from ContestReport cr, BallotCount bc, VoteCount vc "
						+ "WHERE bc.pk = :ballotCountPk and bc in elements(vc.ballotCountSet) and vc in elements(cr.voteCountSet)")})
@NamedNativeQueries({
		@NamedNativeQuery(name = "ContestReport.byContestInArea",
				query = "select cr.* from contest_report cr"
						+ "  join admin.reporting_unit ru on ru.reporting_unit_pk = cr.reporting_unit_pk "
						+ "  join admin.mv_area rua on rua.mv_area_pk = ru.mv_area_pk "
						+ " where cr.contest_pk = :contestPk  and text2ltree(rua.area_path) <@ text2ltree(:areaPath) ",
				resultClass = ContestReport.class),
		@NamedNativeQuery(name = "ContestReport.finnForValghendelseIdOgStyretype",
				query = "SELECT cr.* "
						+ "FROM contest_report cr "
						+ "JOIN mv_election mve using (contest_pk) "
						+ "JOIN reporting_unit ru using (reporting_unit_pk) "
						+ "JOIN reporting_unit_type rut using (reporting_unit_type_pk) "
						+ "WHERE mve.election_event_id = :election_event_id AND rut.reporting_unit_type_id = :reporting_unit_type_id",
				resultClass = ContestReport.class)})
public class ContestReport extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private ReportingUnit reportingUnit;
	private Contest contest;
	private Set<VoteCount> voteCountSet = new HashSet<>();

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return contest.getPk();
		}
		return null;
	}

	/**
	 * Find first vote count by area and count qualifier
	 *
	 * @return first vote count if it exists, null otherwise
	 */
	public VoteCount findFirstVoteCountByMvAreaCountQualifierAndCategory(
			final long mvAreaPk,
			final no.valg.eva.admin.common.counting.model.CountQualifier countQualifier,
			final CountCategory category) {

		for (final VoteCount voteCount : getVoteCountSet()) {
			if (hasArea(mvAreaPk, voteCount)
					&& hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)) {
				return voteCount;
			}
		}
		return null;
	}

	@OneToMany(mappedBy = "contestReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Set<VoteCount> getVoteCountSet() {
		return voteCountSet;
	}

	public void setVoteCountSet(final Set<VoteCount> voteCountSet) {
		this.voteCountSet = voteCountSet;
	}

	private boolean hasArea(long mvAreaPk, VoteCount voteCount) {
		return voteCount.getMvArea().getPk() == mvAreaPk;
	}

	private boolean hasCategory(CountCategory category, VoteCount voteCount) {
		return voteCount.getVoteCountCategory().getId().equalsIgnoreCase(category.getId());
	}

	private boolean hasQualifier(no.valg.eva.admin.common.counting.model.CountQualifier countQualifier, VoteCount voteCount) {
		return voteCount.getCountQualifier().getId().equalsIgnoreCase(countQualifier.getId());
	}

	/**
	 * Finds first vote count by area path, count qualifier and category.
	 *
	 * @return first vote count if it exists, null otherwise
	 */
	public VoteCount findFirstVoteCountByAreaPathQualifierAndCategory(
			AreaPath areaPath,
			no.valg.eva.admin.common.counting.model.CountQualifier countQualifier,
			CountCategory category) {

		for (final VoteCount voteCount : getVoteCountSet()) {
			if (hasArea(areaPath, voteCount)
					&& hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)) {
				return voteCount;
			}
		}
		return null;
	}

	private boolean hasArea(AreaPath areaPath, VoteCount voteCount) {
		return AreaPath.from(voteCount.getMvArea().getAreaPath()).equals(areaPath);
	}

	public List<VoteCount> findVoteCountsByAreaPathQualifierAndCategory(
			AreaPath areaPath,
			no.valg.eva.admin.common.counting.model.CountQualifier countQualifier,
			CountCategory category) {

		List<VoteCount> voteCounts = new ArrayList<>();
		for (final VoteCount voteCount : getVoteCountSet()) {
			if (hasArea(areaPath, voteCount)
					&& hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)) {
				voteCounts.add(voteCount);
			}
		}
		return voteCounts;
	}

	public List<VoteCount> findVoteCountsByAreaQualifierAndCategory(
			MvArea mvArea,
			no.valg.eva.admin.common.counting.model.CountQualifier countQualifier,
			CountCategory category) {

		List<VoteCount> voteCounts = new ArrayList<>();
		for (final VoteCount voteCount : getVoteCountSet()) {
			if (hasArea(mvArea.getPk(), voteCount)
					&& hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)) {
				voteCounts.add(voteCount);
			}
		}
		return voteCounts;
	}

	/**
	 * @return first vote count if it exists, null otherwise
	 */
	public VoteCount findFirstVoteCountByCountQualifierAndCategory(
			final no.valg.eva.admin.common.counting.model.CountQualifier countQualifier,
			final CountCategory category) {

		for (final VoteCount voteCount : getVoteCountSet()) {
			if (hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)) {
				return voteCount;
			}
		}
		return null;
	}

	public List<VoteCount> findVoteCountsByMvAreaCountQualifierAndCategory(
			long mvAreaPk, no.valg.eva.admin.common.counting.model.CountQualifier countQualifier, CountCategory category) {
		List<VoteCount> result = new ArrayList<>();
		for (VoteCount voteCount : getVoteCountSet()) {
			if (hasArea(mvAreaPk, voteCount)
					&& hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)) {
				result.add(voteCount);
			}
		}
		result.sort(Comparator.comparing(VoteCount::getId));
		return result;
	}

	public VoteCount findApprovedVoteCountByMvAreaCountQualifierAndCategory(
			long mvAreaPk, no.valg.eva.admin.common.counting.model.CountQualifier countQualifier, CountCategory category) {
		for (VoteCount voteCount : getVoteCountSet()) {
			if (hasArea(mvAreaPk, voteCount)
					&& hasQualifier(countQualifier, voteCount)
					&& hasCategory(category, voteCount)
					&& isApproved(voteCount)) {
				return voteCount;
			}
		}
		return null;
	}

	private boolean isApproved(VoteCount voteCount) {
		return voteCount.getCountStatus() == APPROVED || voteCount.getCountStatus() == TO_SETTLEMENT;
	}

	public List<VoteCount> findVoteCountsByCountQualifierAndStatus(
			final no.valg.eva.admin.common.counting.model.CountQualifier countQualifier,
			final CountStatus countStatus) {

		List<VoteCount> result = new ArrayList<>();
		for (final VoteCount voteCount : getVoteCountSet()) {
			String countQualifierId = voteCount.getCountQualifierId();
			int voteCountStatusId = voteCount.getVoteCountStatusId();
			if (countQualifierId.equalsIgnoreCase(countQualifier.getId()) && voteCountStatusId == countStatus.getId()) {
				result.add(voteCount);
			}
		}

		return result;
	}

	public List<VoteCount> findVoteCountsByCategoryAndQualifier(CountCategory category, no.valg.eva.admin.common.counting.model.CountQualifier countQualifier) {
		List<VoteCount> result = new ArrayList<>();
		for (final VoteCount voteCount : getVoteCountSet()) {
			String voteCountCategoryId = voteCount.getVoteCountCategoryId();
			String countQualifierId = voteCount.getCountQualifierId();
			if (voteCountCategoryId.equalsIgnoreCase(category.getId()) && countQualifierId.equalsIgnoreCase(countQualifier.getId())) {
				result.add(voteCount);
			}
		}
		return result;
	}

	/**
	 * Find vote count by counting area and id.
	 *
	 * @param countingArea the area, usually a polling district, the count is performed at
	 * @param id identifies the count within an area (eg. PVO1), composed of the count qualifier id (eg. P), count category id (eg. VO) and an index (eg. 1)
	 * @return the vote count if found, null otherwise
	 */
	public VoteCount findVoteCountByCountingAreaAndId(MvArea countingArea, String id) {
		for (VoteCount voteCount : getVoteCountSet()) {
			String voteCountId = voteCount.getId();
			MvArea voteCountArea = voteCount.getMvArea();
			if (voteCountId.equals(id) && voteCountArea.equals(countingArea)) {
				return voteCount;
			}
		}
		return null;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ContestReport rhs = (ContestReport) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.reportingUnit, rhs.reportingUnit)
				.append(this.contest, rhs.contest)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(reportingUnit)
				.append(contest)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("reportingUnit", reportingUnit)
				.append("contest", contest)
				.toString();
	}

	public int uniqueKindCount(CountQualifier countQualifier, VoteCountCategory countCategory, MvArea countArea, VoteCountStatusEnum countStatus) {
		int count = 0;
		for (final VoteCount voteCount : getVoteCountSet()) {
			if (voteCount.belongsTo(countArea) && voteCount.getCountQualifierId().equals(countQualifier.getId())
					&& voteCount.getVoteCountCategoryId().equals(countCategory.getId()) && countStatus.getStatus() == voteCount.getVoteCountStatusId()) {
				count += 1;
			}
		}
		return count;
	}

	/**
	 * Adds vote count to this and assigns an id based on this contest report.
	 *
	 * @param voteCount count to add
	 */
	public void add(VoteCount voteCount) {
		assignIdToVoteCount(voteCount);
		voteCountSet.add(voteCount);
		voteCount.setContestReport(this);
	}

	private void assignIdToVoteCount(VoteCount voteCount) {
		CountQualifier countQualifier = voteCount.getCountQualifier();
		VoteCountCategory voteCountCategory = voteCount.getVoteCountCategory();
		MvArea mvArea = voteCount.getMvArea();
		int newIndex = uniqueKindCount(countQualifier, voteCountCategory, mvArea) + 1;
		if (isProtocolOrPreliminaryQualifier(countQualifier) && newIndex != 1) {
			throw new EvoteException(
					ERROR_CODE_0504_STALE_OBJECT, new IllegalStateException(format("Illegal index for protocol/preliminary count: %d", newIndex)));
		}
		voteCount.setId(countQualifier.getId() + voteCountCategory.getId() + newIndex);
	}

	/**
	 * Finds the number of vote counts that meets the unique criteria
	 *
	 * @return unique kind count
	 */
	public int uniqueKindCount(final CountQualifier countQualifier, final VoteCountCategory countCategory, final MvArea countArea) {
		int count = 0;
		for (final VoteCount voteCount : getVoteCountSet()) {
			if (voteCount.belongsTo(countArea) && voteCount.getCountQualifierId().equals(countQualifier.getId())
					&& voteCount.getVoteCountCategoryId().equals(countCategory.getId())) {
				count += 1;
			}
		}
		return count;
	}

	private boolean isProtocolOrPreliminaryQualifier(CountQualifier countQualifier) {
		return no.valg.eva.admin.common.counting.model.CountQualifier.fromId(countQualifier.getId()) == PROTOCOL
				|| no.valg.eva.admin.common.counting.model.CountQualifier.fromId(countQualifier.getId()) == PRELIMINARY;
	}

	/**
	 * ContestReport (protokoll, "møtebok") is created by/reported by a reporting unit (et styre).
	 *
	 * @param reportingUnit "styre"
	 * @return true if contest report is reported by the given reporting unit
	 */
	@Transient
	public boolean isReportedBy(final ReportingUnit reportingUnit) {
		return reportingUnit.equals(getReportingUnit());
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporting_unit_pk", nullable = false)
	public ReportingUnit getReportingUnit() {
		return reportingUnit;
	}

	public void setReportingUnit(final ReportingUnit reportingUnit) {
		this.reportingUnit = reportingUnit;
	}

	@Transient
	public BallotCount getBallotCount(BallotCountRef ballotCountRef) {
		for (VoteCount voteCount : getVoteCountSet()) {
			for (BallotCount count : voteCount.getBallotCountList()) {
				if (count.getPk() == ballotCountRef.getPk()) {
					return count;
				}
			}
		}
		return null;
	}

	/**
	 * Get vote count in this contest report with given primary key
	 *
	 * @param voteCountPk
	 */
	public VoteCount getVoteCount(Long voteCountPk) {
		for (VoteCount voteCount : getVoteCountSet()) {
			if (voteCount.getPk().equals(voteCountPk)) {
				return voteCount;
			}
		}
		throw new EvoteException("Could not find final count " + voteCountPk);
	}

	public void accept(CountingVisitor countingVisitor) {
		if (countingVisitor.include(this)) {
			countingVisitor.visit(this);
			for (VoteCount voteCount : getVoteCountSet()) {
				voteCount.accept(countingVisitor);
			}
		}
	}

	public Collection<VoteCount> voteCountsFor(MvArea mvArea) {
		return getVoteCountSet()
				.stream()
				.filter(voteCount -> isVoteCountInMvArea(voteCount, mvArea))
				.collect(toList());
	}

	private boolean isVoteCountInMvArea(VoteCount voteCount, MvArea mvArea) {
		AreaPath voteCountAreaPath = AreaPath.from(voteCount.getMvArea().getAreaPath());
		AreaPath areaPath = AreaPath.from(mvArea.getAreaPath());
		return voteCountAreaPath.isSubpathOf(areaPath);
	}

	public boolean finnesRapporterbareTellingerForAlle(Set<MvArea> tekniskeKretser, no.valg.eva.admin.common.counting.model.CountQualifier countQualifier) {
		return getVoteCountSet()
				.stream()
				.filter(voteCount -> countQualifier.equals(no.valg.eva.admin.common.counting.model.CountQualifier.fromId(voteCount.getCountQualifierId())))
				.filter(voteCount -> APPROVED == voteCount.getCountStatus() || TO_SETTLEMENT == voteCount.getCountStatus())
				.filter(voteCount -> tekniskeKretser.contains(voteCount.getMvArea()))
				.collect(toSet())
				.size() == tekniskeKretser.size();
	}

	/*
	 * Avgjør hvilke tellinger som skal rapporteres. Forhånd og valgting håndeteres separat, dvs. man kan få endelige tellinger for den ene
	 * sorten og foreløpige for den andre. Logikken som avgjør hva som kommer med avgjøres i CountCategory.finnOpptellingskategorier() -
	 * se kommentar der.
	 */
	public List<BallotCount> tellingerForRapportering(Set<no.valg.eva.admin.common.counting.model.CountQualifier> countQualifiers, Set<MvArea> mvAreas,
													  Set<CountStatus> statuses, CountingMode voCountingMode, CountingMode vfCountingMode) {
		List<BallotCount> ballotCounts = new ArrayList<>();
		List<BallotCount> ballotCountsForhaand = new ArrayList<>();
		List<BallotCount> ballotCountsValgting = new ArrayList<>();
		splittTilForhaandOgValgting(countQualifiers, mvAreas, statuses, ballotCountsForhaand, ballotCountsValgting);
		ballotCounts.addAll(foreløpigeEllerEndelige(ballotCountsForhaand, mvAreas, voCountingMode, vfCountingMode));
		ballotCounts.addAll(foreløpigeEllerEndelige(ballotCountsValgting, mvAreas, voCountingMode, vfCountingMode));
		return ballotCounts;
	}

	private void splittTilForhaandOgValgting(Set<no.valg.eva.admin.common.counting.model.CountQualifier> countQualifiers, Set<MvArea> mvAreas,
											 Set<CountStatus> statuses, List<BallotCount> ballotCountsForhaand, List<BallotCount> ballotCountsValgting) {
		getVoteCountSet()
				.stream()
				.filter(voteCount -> mvAreas.contains(voteCount.getMvArea()))
				.filter(voteCount -> countQualifiers.contains(no.valg.eva.admin.common.counting.model.CountQualifier.fromId(voteCount.getCountQualifierId())))
				.filter(voteCount -> statuses.contains(voteCount.getCountStatus()))
				.forEach(voteCount -> (voteCount.isEarlyVoting() ? ballotCountsForhaand : ballotCountsValgting).addAll(voteCount.getBallotCountSet()));
	}

	private List<BallotCount> foreløpigeEllerEndelige(List<BallotCount> ballotCounts, Set<MvArea> mvAreas, CountingMode voCountingMode, CountingMode vfCountingMode) {
		if (ballotCounts.isEmpty()) {
			return ballotCounts; // spesialtilfelle, skal vel ikke kunne skje i praksis
		}
		boolean forhaand = ballotCounts.get(0).getVoteCount().isEarlyVoting(); // Enten er alle ballotCount forhånd eller så er de valgting

		List<BallotCount> foreløpigeBallotCount = new ArrayList<>();
		List<BallotCount> endeligeBallotCount = new ArrayList<>();
		splittTilForeløpigeOgEndelige(ballotCounts, foreløpigeBallotCount, endeligeBallotCount, voCountingMode, vfCountingMode);
		return alleEndeligeTellingerFinnes(mvAreas, forhaand, ballotCounts, voCountingMode, vfCountingMode) ? endeligeBallotCount : foreløpigeBallotCount;
	}

	/*
	 * Deler foreløpige og endelige ballotCounts i 2 lister. ALLE endelige tellinger blitt tatt med, mens for foreløpige tellinger blir bare de som
	 * tilfredsstiller kravene i CountCategory.finnOpptellingskategorier() tatt med.
	 */
	private void splittTilForeløpigeOgEndelige(List<BallotCount> ballotCounts, List<BallotCount> foreløpigeBallotCount, List<BallotCount> endeligeBallotCount,
											   CountingMode voCountingMode, CountingMode vfCountingMode) {
		for (BallotCount ballotCount : ballotCounts) {
			if (ballotCount.isEndelig()) {
				endeligeBallotCount.add(ballotCount);
			} else {
				Set<CountCategory> foreløpigeKategorier = CountCategory.finnOpptellingskategorier(
						ballotCount.getVoteCount().getMvArea().getPollingDistrict(), ballotCount.isForhånd(), true, voCountingMode, vfCountingMode);
				if (foreløpigeKategorier.contains(CountCategory.valueOf(ballotCount.getVoteCount().getVoteCountCategory().getId()))) {
					foreløpigeBallotCount.add(ballotCount);
				}
			}
		}
	}

	/*
	 * Avklarer hvorvidt vi kan sende fra oss de endelige tellingene - da må alle nødvendige endelige tellinger finnes, hvis ikke så kan vi bare benytte de
	 * foreløpige tellingene
	 */
	private boolean alleEndeligeTellingerFinnes(Set<MvArea> mvAreas, boolean forhånd, List<BallotCount> ballotCounts, CountingMode voCountingMode,
												CountingMode vfCountingMode) {
		final boolean forelopige = false;
		Set<String> ballotCountPrTypePrKategoriPrKrets = getBallotCountPrTypePrKategoriPrKrets(ballotCounts);
		for (MvArea mvArea : mvAreas) {
			Set<CountCategory> endeligeKategorierSomMåFinnes = CountCategory.finnOpptellingskategorier(mvArea.getPollingDistrict(), forhånd,
					forelopige, voCountingMode, vfCountingMode);
			for (CountCategory ek : endeligeKategorierSomMåFinnes) {
				String typeOgKategoriOgKrets = lagTypeOgKategoriOgKretsNokkel(forelopige, ek, mvArea);
				if (!ballotCountPrTypePrKategoriPrKrets.contains(typeOgKategoriOgKrets)) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * Lager et sett av de varianter av tellinger vi finner.
	 */
	private Set<String> getBallotCountPrTypePrKategoriPrKrets(List<BallotCount> ballotCounts) {
		Set<String> ballotCountPrTypePrKategoriPrKrets = new HashSet<>();
		for (BallotCount bc : ballotCounts) {
			VoteCount vc = bc.getVoteCount();
			String typeOgKategoriOgKrets = lagTypeOgKategoriOgKretsNokkel(bc.isForeløpig(), vc.getCountCategory(), vc.getMvArea());
			ballotCountPrTypePrKategoriPrKrets.add(typeOgKategoriOgKrets);
		}
		return ballotCountPrTypePrKategoriPrKrets;
	}

	private String lagTypeOgKategoriOgKretsNokkel(boolean forelopinge, CountCategory countCategory, MvArea mvArea) {
		return forelopinge + countCategory.getId() + mvArea.getPollingDistrict().getId();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk", nullable = false)
	public Contest getContest() {
		return contest;
	}

	public void setContest(final Contest contest) {
		this.contest = contest;
	}

	public List<VoteCount> godkjenteEndeligeVoteCounts() {
		return getVoteCountSet().stream()
				.filter(VoteCount::isFinalCount)
				.filter(VoteCount::isApproved)
				.collect(toList());
	}
}
