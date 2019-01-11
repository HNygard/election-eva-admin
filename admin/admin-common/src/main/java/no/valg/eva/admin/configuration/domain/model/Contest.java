package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.validator.PastLocalDate;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

/**
 * Entity representing Contest ("Konkurranse, delvalg"). There are normally several Contests per Election, e.g. "Oslo" or "Sør-Norge valgkrets".
 */
@Entity
@Table(name = "contest", uniqueConstraints = @UniqueConstraint(columnNames = {"election_pk", "contest_id"}))
@AttributeOverride(name = "pk", column = @Column(name = "contest_pk"))
@NamedQueries({
        @NamedQuery(name = "Contest.findById", query = "SELECT c FROM Contest c WHERE c.election.pk = :electionPk AND c.id = :id"),
        @NamedQuery(name = "Contest.findByElectionEventAndStatus", query = "select c from Contest c, ContestArea ca, "
                + "MvArea m WHERE ca.contest.pk = c.pk and ca.mvArea.pk = m.pk and m.electionEvent.pk = "
                + ":electionEventPk and c.contestStatus.id = :contestStatusId"),
        @NamedQuery(name = "Contest.countByElection", query = "select count(c) from Contest c where c.election.pk = :electionPk"),
        @NamedQuery(name = "Contest.findByElection", query = "select c from Contest c where c.election.pk = :electionPk order by c.name"),
        @NamedQuery(name = "Contest.findByElectionEventAndArea", query = "select c from Contest c, ContestArea ca WHERE ca.contest.pk = c.pk "
                + "AND ca.mvArea.electionEvent.pk = :electionEventPk AND ca.mvArea.areaPath = :areaPath"),
        @NamedQuery(
                name = "Contest.findByElectionAndArea",
                query = "select c from Contest c, ContestArea ca where ca.contest.pk = c.pk and ca.mvArea.pk = :mvAreaPk and c.election.pk = :electionPk")})
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "Contest.findBoroughContestsInMunicipality",
                query = "SELECT c.* FROM contest c "
                        + "JOIN contest_area ca ON ca.contest_pk = c.contest_pk "
                        + "JOIN mv_area mva ON ca.mv_area_pk = mva.mv_area_pk "
                        + "JOIN municipality m ON mva.municipality_pk = m.municipality_pk "
                        + "WHERE m.municipality_pk = ?1 AND mva.area_level = 4",
                resultClass = Contest.class),
        @NamedNativeQuery(
                name = "Contest.finnMultiomraadedistrikter",
                query = "SELECT c.* "
                        + "FROM contest c "
                        + "LEFT JOIN mv_election USING (contest_pk) "
                        + "WHERE mv_election.single_area IS FALSE "
                        + "  AND election_event_pk = :electionEventPk",
                resultClass = Contest.class)
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contest extends VersionedEntity implements java.io.Serializable, ContextSecurable {

    private ContestStatus contestStatus;
    private Election election;
    private String id;
    private String name;
    private Integer minProposersOldParty; // underskrivere
    private Integer minProposersNewParty;
    private Integer minCandidates;
    private Integer maxCandidates;
    private Integer maxVotes;
    private Integer minVotes;
    private Integer maxWriteIn;
    private Integer maxRenumber;
    private Integer numberOfPositions;
    private LocalDate endDateOfBirth;
    private Boolean penultimateRecount;
    private Set<Ballot> ballots = new HashSet<>();
    private Set<ContestArea> contestAreaSet = new HashSet<>();

    public Contest(Contest contest) {
        super();
        this.contestStatus = contest.getContestStatus();
        this.election = contest.getElection();
        this.endDateOfBirth = contest.getEndDateOfBirth();
        this.id = contest.getId();
        this.maxCandidates = contest.getMaxCandidates();
        this.maxVotes = contest.getMaxVotes();
        this.maxWriteIn = contest.getMaxWriteIn();
        this.minCandidates = contest.getMinCandidates();
        this.minProposersOldParty = contest.getMinProposersOldParty();
        this.minProposersNewParty = contest.getMinProposersNewParty();
        this.maxRenumber = contest.getMaxRenumber();
        this.minVotes = contest.getMinVotes();
        this.numberOfPositions = contest.getNumberOfPositions();
        this.penultimateRecount = contest.getPenultimateRecount();
        this.name = contest.getName();
    }

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "contest_status_pk", nullable = false)
    public ContestStatus getContestStatus() {
        return contestStatus;
    }

    public void setContestStatus(final ContestStatus contestStatus) {
        this.contestStatus = contestStatus;
    }

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "election_pk", nullable = false)
    @NotNull
    public Election getElection() {
        return election;
    }

    public void setElection(final Election election) {
        this.election = election;
    }

    @Column(name = "contest_id", nullable = false, length = 6)
    @ID(size = 6)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Column(name = "contest_name", nullable = false, length = 100)
    @StringNotNullEmptyOrBlanks
    @Size(max = 100)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Column(name = "min_proposers_old_party")
    public Integer getMinProposersOldParty() {
        return minProposersOldParty;
    }

    public void setMinProposersOldParty(final Integer minProposersOldParty) {
        this.minProposersOldParty = minProposersOldParty;
    }

    @Column(name = "min_proposers_new_party")
    public Integer getMinProposersNewParty() {
        return minProposersNewParty;
    }

    public void setMinProposersNewParty(final Integer minProposersNewParty) {
        this.minProposersNewParty = minProposersNewParty;
    }

    @Column(name = "min_candidates")
    @Min(0)
    @Max(9999)
    public Integer getMinCandidates() {
        return minCandidates;
    }

    public void setMinCandidates(final Integer minCandidates) {
        this.minCandidates = minCandidates;
    }

    @Column(name = "max_candidates")
    @Min(0)
    @Max(9999)
    public Integer getMaxCandidates() {
        return maxCandidates;
    }

    public void setMaxCandidates(final Integer maxCandidates) {
        this.maxCandidates = maxCandidates;
    }

    @Column(name = "max_votes")
    public Integer getMaxVotes() {
        return maxVotes;
    }

    public void setMaxVotes(final Integer maxVotes) {
        this.maxVotes = maxVotes;
    }

    @Column(name = "min_votes")
    public Integer getMinVotes() {
        return minVotes;
    }

    public void setMinVotes(final Integer minVotes) {
        this.minVotes = minVotes;
    }

    @Column(name = "max_write_in")
    @Min(0)
    @Max(9999)
    public Integer getMaxWriteIn() {
        return maxWriteIn;
    }

    public void setMaxWriteIn(final Integer maxWriteIn) {
        this.maxWriteIn = maxWriteIn;
    }

    @Column(name = "max_renumber")
    @Min(0)
    @Max(9999)
    public Integer getMaxRenumber() {
        return maxRenumber;
    }

    public void setMaxRenumber(final Integer maxRenumber) {
        this.maxRenumber = maxRenumber;
    }

    @Column(name = "number_of_positions")
    @Min(0)
    @Max(9999)
    public Integer getNumberOfPositions() {
        return numberOfPositions;
    }

    public void setNumberOfPositions(final Integer numberOfPositions) {
        this.numberOfPositions = numberOfPositions;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @Column(name = "end_date_of_birth", length = 13)
    @PastLocalDate
    public LocalDate getEndDateOfBirth() {
        return endDateOfBirth;
    }

    public void setEndDateOfBirth(final LocalDate endDateOfBirth) {
        this.endDateOfBirth = endDateOfBirth;
    }

    @OneToMany(mappedBy = "contest", fetch = LAZY)
    public Set<Ballot> getBallots() {
        return ballots;
    }

    public void setBallots(Set<Ballot> ballots) {
        this.ballots = ballots;
    }

    @OneToMany(mappedBy = "contest", fetch = LAZY)
    public Set<ContestArea> getContestAreaSet() {
        return contestAreaSet;
    }

    public void setContestAreaSet(Set<ContestArea> contestAreaSet) {
        this.contestAreaSet = contestAreaSet;
    }

    @Column(name = "penultimate_recount")
    public Boolean getPenultimateRecount() {
        return penultimateRecount;
    }

    public void setPenultimateRecount(final Boolean penultimateRecount) {
        this.penultimateRecount = penultimateRecount;
    }

    @Transient
    public Set<Ballot> getSortedApprovedBallots() {
        Set<Ballot> approvedBallots = new TreeSet<>();
        for (Ballot ballot : getBallots()) {
            if (ballot.isApproved()) {
                approvedBallots.add(ballot);
            }
        }
        return approvedBallots;
    }

    @Transient
    public ContestArea getFirstContestArea() {
        return getContestAreaList().stream().findFirst().orElse(null);
    }

    /**
     * @return sortert liste av contest areas der parent er først. For vanlige valg er det en-til-en mellom contest og område. For sametingsvalg er det mange
     * til en. For å støtte både vanlige valg og sametingsvalg er det nødvendig å legge parent først i lista.
     */
    @Transient
    public List<ContestArea> getContestAreaList() {
        Set<ContestArea> contestAreas = getContestAreaSet();
        if (contestAreas != null) {
            return contestAreas
                    .stream()
                    .sorted(parentAreaSortedFirstComparator())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Comparator<ContestArea> parentAreaSortedFirstComparator() {
        return (ca1, ca2) -> ca1.isParentArea() ? -1 : ca2.isParentArea() ? 1 : 0;
    }

    @Transient
    public int affiliationBaselineVotesFactor() {
        if (hasWriteIns()) {
            return getNumberOfPositions();
        }
        return 1;
    }

    @Transient
    public BigDecimal getCandidateRankVoteShareThreshold() {
        return getElection().getCandidateRankVoteShareThreshold();
    }

    @Transient
    public BigDecimal getSettlementFirstDivisor() {
        return getElection().getSettlementFirstDivisor();
    }

    @Transient
    public BigDecimal getBaselineVoteFactor() {
        return getElection().getBaselineVoteFactor();
    }

    @Transient
    public boolean isOnCountyLevel() {
        return isOnLevel(AreaLevelEnum.COUNTY);
    }

    @Transient
    public boolean isOnMunicipalityLevel() {
        return isOnLevel(AreaLevelEnum.MUNICIPALITY);
    }

    private boolean isOnLevel(AreaLevelEnum level) {
        return getFirstContestArea().getMvArea().getActualAreaLevel() == level;
    }

    @Transient
    public boolean isOnBoroughLevel() {
        return isOnLevel(AreaLevelEnum.BOROUGH);
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        return null;
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        switch (level) {
            case ELECTION:
                return election.getPk();
            case CONTEST:
                return getPk();
            default:
                return null;
        }
    }

    /**
     * Check if municipality shall count two times (both preliminary and final) (or just preliminary) before the last reporting unit recounts the final. In sami
     * election the municipality only counts preliminary before opptellingsvalgstyret counts final.
     *
     * @return true if municipality must count both preliminary and final.
     */
    @Transient
    public boolean isContestOrElectionPenultimateRecount() {
        if (this.getPenultimateRecount() != null) {
            return this.getPenultimateRecount();
        } else {
            return this.getElection().isPenultimateRecount();
        }
    }

    @Override
    public String toString() {
        return id + " " + name;
    }

    boolean hasContestAreaForAreaPath(AreaPath areaPath) {
        Set<ContestArea> contestAreas = getContestAreaSet();
        for (ContestArea contestArea : contestAreas) {
            AreaPath contestAreaPath = AreaPath.from(contestArea.getMvArea().getAreaPath());
            if (contestAreaPath.contains(areaPath)) {
                return true;
            }
            if (areaPath.isMunicipalityLevel() && contestAreaPath.isBoroughLevel() && areaPath.contains(contestAreaPath)) {
                return true;
            }
        }
        return false;
    }

    public ElectionPath electionPath() {
        return getElection().electionPath().add(getId());
    }

    @Transient
    public boolean isReferendum() {
        return getElection().isReferendum();
    }

    @Transient
    public boolean isSingleArea() {
        return election.isSingleArea();
    }

    /**
     * @return OPPTELLINGSVALGSTYRET if sametingsvalg, FYLKESVALGSTYRET if contest is on county, else VALGSTYRET
     */
    ReportingUnitTypeId finalReportingUnitTypeForArea() {
        if (!isSingleArea()) {
            return ReportingUnitTypeId.OPPTELLINGSVALGSTYRET;
        }
        if (contestIsforCounty()) {
            return ReportingUnitTypeId.FYLKESVALGSTYRET;
        }
        return ReportingUnitTypeId.VALGSTYRET;
    }

    private boolean contestIsforCounty() {
        return contestAreaSet.iterator().next().getActualAreaLevel().equals(AreaLevelEnum.COUNTY);
    }

    public boolean hasRenumbering() {
        return getElection().isRenumber();
    }

    private boolean hasWriteIns() {
        return getElection().isWritein();
    }

    public void accept(ConfigurationVisitor configurationVisitor) {
        if (configurationVisitor.include(this)) {
            configurationVisitor.visit(this);
            for (Ballot ballot : getBallots()) {
                ballot.accept(configurationVisitor);
            }
        }
    }

    public Integer minNumberOfProposersFor(Party party) {
        if (minProposersOldParty == null || minProposersNewParty == null) {
            throw new IllegalStateException("minProposersOldParty or minProposersNewParty not configured.");
        }
        return party.isForenkletBehandling() ? minProposersOldParty : minProposersNewParty;
    }

    @Transient
    public String getElectionName() {
        return election.getName();
    }

    @Transient
    public boolean isInMunicipality(Municipality municipality) {
        return getContestAreaSet().stream()
                .map(ContestArea::getMvArea)
                .map(MvArea::getMunicipality)
                .anyMatch(municipality::equals);
    }

    public String areaIdForLocalParties() {
        if (isOnBoroughLevel()) {
            return getFirstContestArea().getAreaPath().getMunicipalityId();
        }
        return getFirstContestArea().getAreaPath().getLeafId();
    }

    @Transient
    boolean isForArea(AreaPath areaPath) {
        return getContestAreaSet().stream()
                .map(ContestArea::getMvArea)
                .map(MvArea::areaPath)
                .anyMatch(areaPath::equals);
    }

    public ValgdistriktSti valgdistriktSti() {
        return ValgdistriktSti.fra(electionPath());
    }
}
