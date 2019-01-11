package no.valg.eva.admin.configuration.domain.model;

import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;

import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.SQLConstants;
import no.evote.model.BaseEntity;
import no.evote.security.ContextSecurableDynamicElection;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import no.evote.util.Treeable;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

/**
 * Materialized view containing all nodes in the hierarchy formed by the tables election_group, election, and contest, facilitating RBAC access to any level in
 * the hierarchy via a single field/pointer
 */
@Entity
@Table(name = "mv_election", uniqueConstraints = {@UniqueConstraint(columnNames = {SQLConstants.ELECTION_EVENT_PK, "election_group_pk", "election_pk",
        "contest_pk"})})
@AttributeOverride(name = "pk", column = @Column(name = "mv_election_pk"))
@NamedQueries({
        @NamedQuery(name = "MvElection.findRoot", query = "SELECT mve FROM MvElection mve WHERE mve.electionGroup IS NULL AND "
                + "mve.electionEvent.pk = :eepk"),
        @NamedQuery(name = "MvElection.findByContest", query = "SELECT mve FROM MvElection mve WHERE mve.contest.pk = :contestPk"),
        @NamedQuery(name = "MvElection.findByPath", query = "SELECT mve FROM MvElection mve WHERE mve.electionPath = :path"),
        @NamedQuery(name = "MvElection.findByContestPk", query = "SELECT mve FROM MvElection mve WHERE mve.contest.pk = :contestPk"),
        @NamedQuery(
                name = "MvElection.findContestsByElectionEventAndAreas",
                query = "SELECT mve FROM MvElection mve, ContestArea ca "
                        + "WHERE mve.contest IS NOT NULL AND mve.contest.pk = ca.contest.pk "
                        + "AND mve.electionEvent.id = :electionEventId "
                        + "AND ca.mvArea.areaPath IN (:areaPaths)")})
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "MvElection.findFirstByPathAndLevel",
                query = "SELECT * FROM mv_election mve WHERE text2ltree(mve.election_path)"
                        + " <@ text2ltree(?1) AND mve.election_level = ?2 ORDER BY mve.election_path LIMIT 1",
                resultClass = MvElection.class),
        @NamedNativeQuery(
                name = "MvElection.findByPathAndLevel",
                query = "SELECT * FROM mv_election mve WHERE text2ltree(mve.election_path)"
                        + " <@ text2ltree(?1) AND mve.election_level = ?2 ORDER BY mve.election_path",
                resultClass = MvElection.class),
        @NamedNativeQuery(
                name = "MvElection.findByPathAndLevelAndAreaLevel",
                query = "SELECT * FROM mv_election mve WHERE text2ltree(mve.election_path)"
                        + " <@ text2ltree(?1) AND mve.election_level = ?2 AND mve.area_level = ?3 ORDER BY mve.election_path",
                resultClass = MvElection.class),
        @NamedNativeQuery(name = "MvElection.findElectionsByElectionType", query = "SELECT * FROM admin.mv_election mve LEFT JOIN admin.election e "
                + "ON mve.election_pk = e.election_pk WHERE text2ltree(mve.election_path) <@ text2ltree(?1) "
                + "AND e.election_type_pk = ?2 AND mve.election_level = 2", resultClass = MvElection.class),
        @NamedNativeQuery(name = MvElection.FIND_BY_PK_AND_LEVEL, query = "SELECT re.* FROM mv_election re JOIN mv_election oe"
                + " ON (text2ltree(oe.election_path) <@ text2ltree(re.election_path) AND oe.election_level = ?1"
                + " AND CASE WHEN ?1 = 0 THEN oe.election_event_pk WHEN ?1 = 1 THEN oe.election_group_pk"
                + " WHEN ?1 = 2 THEN oe.election_pk WHEN ?1 = 3 THEN oe.contest_pk END = ?2)", resultClass = MvElection.class),
        @NamedNativeQuery(
                name = MvElection.FIND_SINGLE_BY_PK_AND_LEVEL,
                query = "SELECT * FROM mv_election WHERE "
                        + "CASE WHEN (?1) = 1 THEN election_group_pk WHEN (?1) = 2 THEN election_pk WHEN (?1) = 3 THEN contest_pk END = (?2);",
                resultClass = MvElection.class),
        @NamedNativeQuery(name = MvElection.HAS_ACCESS_TO_PK_ON_LEVEL, query = "SELECT re.* FROM mv_election re JOIN mv_election oe"
                + " ON (text2ltree(oe.election_path) <@ text2ltree(re.election_path) AND oe.election_level = ?1"
                + " AND CASE WHEN ?1 = 0 THEN oe.election_event_pk WHEN ?1 = 1 THEN oe.election_group_pk"
                + " WHEN ?1 = 2 THEN oe.election_pk WHEN ?1 = 3 THEN oe.contest_pk END = ?2) WHERE re.election_path = ?3", resultClass = MvElection.class),
        @NamedNativeQuery(
                name = "MvElection.findFirstPkByElectionPathAndOperatorAreaPath",
                query = "SELECT mve.mv_election_pk "
                        + "FROM mv_election mve "
                        + "JOIN contest_area ca USING (contest_pk) "
                        + "JOIN mv_area mva_ca USING (mv_area_pk) "
                        + "JOIN mv_area mva ON (text2ltree(mva_ca.area_path) @> text2ltree(mva.area_path)) "
                        + "OR (mva.area_level = 3 AND mva_ca.area_level = 4 AND text2ltree(mva_ca.area_path) <@ text2ltree(mva.area_path)) "
                        + "OR (mva.area_level = 0 AND text2ltree(mva_ca.area_path) <@ text2ltree(mva.area_path))"
                        + "WHERE election_path LIKE ?1 AND election_level = 3 AND mva.area_path = ?2 LIMIT 1"),
        @NamedNativeQuery(
                name = "MvElection.findByElectionPathAndOperatorAreaPath",
                query = "SELECT DISTINCT mve.* "
                        + "FROM mv_election mve "
                        + "JOIN contest_area ca USING (contest_pk) "
                        + "JOIN mv_area mva_ca USING (mv_area_pk) "
                        + "JOIN mv_area mva ON (text2ltree(mva_ca.area_path) @> text2ltree(mva.area_path)) "
                        + "OR (mva.area_level = 3 AND mva_ca.area_level = 4 AND text2ltree(mva_ca.area_path) <@ text2ltree(mva.area_path)) "
                        + "OR (mva.area_level = 0 AND text2ltree(mva_ca.area_path) <@ text2ltree(mva.area_path))"
                        + "WHERE election_path LIKE ?1 AND election_level = 3 AND mva.area_path = ?2 "
                        + "ORDER BY mve.election_path",
                resultClass = MvElection.class),
        @NamedNativeQuery(
                name = "MvElection.contestInfoForSamiElection",
                query = "SELECT c.election_path AS election_path, c.election_name AS election_name, c.contest_name AS contest_name, ac.area_path AS area_path "
                        + "FROM mv_election c "
                        + "JOIN contest_area ca ON ca.contest_pk = c.contest_pk "
                        + "JOIN mv_area ac ON ac.mv_area_pk = ca.mv_area_pk "
                        + "WHERE c.contest_pk = ?1 AND ca.parent_area IS TRUE",
                resultClass = ContestInfo.class,
                resultSetMapping = "ContestInfo"),
        @NamedNativeQuery(
                name = "MvElection.matchElectionPathAndAreaPath",
                query = "SELECT count(mve.mv_election_pk) "
                        + "FROM mv_election mve "
                        + "JOIN contest_area USING (contest_pk) "
                        + "JOIN mv_area mva_ca USING (mv_area_pk) "
                        + "WHERE mve.election_path LIKE ?1 AND mve.election_level = 3 "
                        + "      AND EXISTS (SELECT mv_area_pk "
                        + "                  FROM mv_area mva "
                        + "                  WHERE mva.area_path = ?2 "
                        + "                        AND (mva.area_level >= mva_ca.area_level AND mva.area_path LIKE concat(mva_ca.area_path, '%') "
                        + "                             OR mva.area_level < mva_ca.area_level AND mva_ca.area_path LIKE concat(mva.area_path, '%')))")})
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "ContestInfo",
                classes = {
                        @ConstructorResult(
                                targetClass = ContestInfo.class,
                                columns = {
                                        @ColumnResult(name = "election_path"),
                                        @ColumnResult(name = "election_name"),
                                        @ColumnResult(name = "contest_name"),
                                        @ColumnResult(name = "area_path")
                                })
                })})

public class MvElection extends BaseEntity implements java.io.Serializable, Treeable, ContextSecurableDynamicElection {
    public static final String HAS_ACCESS_TO_PK_ON_LEVEL = "MvElection.hasAccessToPkOnLevel";
    public static final String FIND_BY_PK_AND_LEVEL = "MvElection.findByPkAndLevel";
    static final String FIND_SINGLE_BY_PK_AND_LEVEL = "MvElection.findSingleByPathAndLevel";
    private ElectionEvent electionEvent;
    private Contest contest;
    private ElectionGroup electionGroup;
    private Election election;
    private String electionPath;
    private int electionLevel;
    private String electionEventId;
    private String electionGroupId;
    private String electionId;
    private String contestId;
    private String electionEventName;
    private String electionGroupName;
    private String electionName;
    private String contestName;
    private Integer areaLevel;
    private Boolean singleArea;
    private LocalDate electionEndDateOfBirth;
    private LocalDate contestEndDateOfBirth;
    private boolean isReportingUnit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
    public ElectionEvent getElectionEvent() {
        return this.electionEvent;
    }

    public void setElectionEvent(final ElectionEvent electionEvent) {
        this.electionEvent = electionEvent;
    }

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contest_pk")
    public Contest getContest() {
        return this.contest;
    }

    public void setContest(final Contest contest) {
        this.contest = contest;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "election_group_pk")
    public ElectionGroup getElectionGroup() {
        return this.electionGroup;
    }

    public void setElectionGroup(final ElectionGroup electionGroup) {
        this.electionGroup = electionGroup;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "election_pk")
    public Election getElection() {
        return this.election;
    }

    public void setElection(final Election election) {
        this.election = election;
    }

    @Column(name = "election_path", nullable = false, length = 19)
    @StringNotNullEmptyOrBlanks
    @Size(max = 19)
    public String getElectionPath() {
        return this.electionPath;
    }

    public void setElectionPath(final String electionPath) {
        this.electionPath = electionPath;
    }

    @Column(name = "election_level", nullable = false)
    public int getElectionLevel() {
        return this.electionLevel;
    }

    public void setElectionLevel(final int electionLevel) {
        this.electionLevel = electionLevel;
    }

    @Column(name = "election_event_id", nullable = false, length = 8)
    public String getElectionEventId() {
        return this.electionEventId;
    }

    public void setElectionEventId(final String electionEventId) {
        this.electionEventId = electionEventId;
    }

    @Column(name = "election_group_id", length = 8)
    public String getElectionGroupId() {
        return this.electionGroupId;
    }

    public void setElectionGroupId(final String electionGroupId) {
        this.electionGroupId = electionGroupId;
    }

    @Column(name = "election_id", length = 8)
    public String getElectionId() {
        return this.electionId;
    }

    public void setElectionId(final String electionId) {
        this.electionId = electionId;
    }

    @Column(name = "contest_id", length = 8)
    public String getContestId() {
        return this.contestId;
    }

    public void setContestId(final String contestId) {
        this.contestId = contestId;
    }

    @Column(name = "election_event_name", nullable = false, length = 100)
    @StringNotNullEmptyOrBlanks
    @Size(max = 100)
    public String getElectionEventName() {
        return this.electionEventName;
    }

    public void setElectionEventName(final String electionEventName) {
        this.electionEventName = electionEventName;
    }

    @Column(name = "election_group_name", length = 100)
    @Size(max = 100)
    public String getElectionGroupName() {
        return this.electionGroupName;
    }

    public void setElectionGroupName(final String electionGroupName) {
        this.electionGroupName = electionGroupName;
    }

    @Column(name = "election_name", length = 100)
    @Size(max = 100)
    public String getElectionName() {
        return this.electionName;
    }

    public void setElectionName(final String electionName) {
        this.electionName = electionName;
    }

    @Column(name = "contest_name", length = 100)
    @Size(max = 100)
    public String getContestName() {
        return this.contestName;
    }

    public void setContestName(final String contestName) {
        this.contestName = contestName;
    }

    @Column(name = "area_level")
    public Integer getAreaLevel() {
        return this.areaLevel;
    }

    public void setAreaLevel(final Integer areaLevel) {
        this.areaLevel = areaLevel;
    }

    @Column(name = "single_area")
    public Boolean getSingleArea() {
        return this.singleArea;
    }

    public void setSingleArea(final Boolean singleArea) {
        this.singleArea = singleArea;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @Column(name = "election_end_date_of_birth", length = 13)
    public LocalDate getElectionEndDateOfBirth() {
        return electionEndDateOfBirth;
    }

    public void setElectionEndDateOfBirth(LocalDate electionEndDateOfBirth) {
        this.electionEndDateOfBirth = electionEndDateOfBirth;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @Column(name = "contest_end_date_of_birth", length = 13)
    public LocalDate getContestEndDateOfBirth() {
        return contestEndDateOfBirth;
    }

    public void setContestEndDateOfBirth(LocalDate contestEndDateOfBirth) {
        this.contestEndDateOfBirth = contestEndDateOfBirth;
    }

    @Override
    @Transient
    public String getPath() {
        return electionPath;
    }

    @Transient
    public String getElectionLevelString() {
        switch (ElectionLevelEnum.getLevel(electionLevel)) {
            case ELECTION_EVENT:
                return "@election_level[0].name";
            case ELECTION_GROUP:
                return "@election_level[1].name";
            case ELECTION:
                return "@election_level[2].name";
            case CONTEST:
                return "@election_level[3].name";

            default:
                return null;
        }

    }

    @Transient
    public String getElectionLevelId() {
        switch (ElectionLevelEnum.getLevel(electionLevel)) {
            case ELECTION_EVENT:
                return electionEventId;
            case ELECTION_GROUP:
                return electionGroupId;
            case ELECTION:
                return electionId;
            case CONTEST:
                return contestId;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        switch (ElectionLevelEnum.getLevel(electionLevel)) {
            case ELECTION_EVENT:
                return electionEventName;
            case ELECTION_GROUP:
                return electionGroupName;
            case ELECTION:
                return electionName;
            case CONTEST:
                return contestName;

            default:
                return "";
        }
    }

    @Transient
    public String electionLevelName(ElectionLevelEnum electionLevelEnum) {
        switch (electionLevelEnum) {
            case ELECTION_EVENT:
                return electionEventName;
            case ELECTION_GROUP:
                return electionGroupName;
            case ELECTION:
                return electionName;
            case CONTEST:
                return contestName;
            default:
                return "";
        }
    }

    @Transient
    public String getNamedPath() {
        StringBuilder path = new StringBuilder();
        if (electionEvent != null) {
            path = new StringBuilder(electionEventName);
        }
        if (electionGroup != null) {
            path = new StringBuilder(electionGroupName);
        }
        if (election != null) {
            path = new StringBuilder(electionName);
        }
        if (contest != null) {
            path.append('.');
            path.append(contestName);
        }

        return path.toString();
    }

    @Override
    public int hashCode() {
        return EqualsHashCodeUtil.genericHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsHashCodeUtil.genericEquals(this, obj);
    }

    @Transient
    public boolean isReportingUnit() {
        return isReportingUnit;
    }

    public void setReportingUnit(final boolean isReportingUnit) {
        this.isReportingUnit = isReportingUnit;
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        return null;
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        if (level.equals(ELECTION_EVENT)) {
            return getElectionEvent().getPk();
        }
        if (level.equals(ElectionLevelEnum.ELECTION_GROUP)) {
            return getElectionGroup().getPk();
        }
        if (level.equals(ElectionLevelEnum.ELECTION)) {
            return getElection().getPk();
        }
        if (level.equals(ElectionLevelEnum.CONTEST)) {
            return getContest().getPk();
        }

        return null;
    }

    @Override
    @Transient
    public ElectionLevelEnum getActualElectionLevel() {
        return ElectionLevelEnum.getLevel(electionLevel);
    }

    /**
     * @param operatorAreaPath areaPath to return if contestArea exists for this path
     * @return AreaPath matching operatorAreaPath if found, else default AreaPath for Contest
     */
    public AreaPath contestAreaPath(AreaPath operatorAreaPath) {
        this.electionPath().assertContestLevel();
        Optional<ContestArea> areaOptional = getContest().getContestAreaSet().stream().filter(contestArea -> contestArea.getAreaPath().equals(operatorAreaPath))
                .findFirst();
        if (areaOptional.isPresent()) {
            return operatorAreaPath;
        }
        return AreaPath.from(contestMvArea().getAreaPath());
    }

    public AreaPath contestAreaPath() {
        return AreaPath.from(contestMvArea().getAreaPath());
    }

    public MvArea contestMvArea() {
        ContestArea contestArea = contest.getContestAreaList().get(0);
        return contestArea.getMvArea();
    }

    @Transient
    public boolean isOnElectionEventLevel() {
        return getActualElectionLevel() == ELECTION_EVENT;
    }

    @Transient
    public AreaLevelEnum getActualAreaLevel() {
        return AreaLevelEnum.getLevel(areaLevel);
    }

    public String electionYear() {
        return getElectionEvent().getElectionDays().iterator().next().electionYear();
    }

    public ElectionPath electionPath() {
        return ElectionPath.from(getElectionPath());
    }

    public ValghierarkiSti valghierarkiSti() {
        return ValghierarkiSti.fra(electionPath());
    }
}
