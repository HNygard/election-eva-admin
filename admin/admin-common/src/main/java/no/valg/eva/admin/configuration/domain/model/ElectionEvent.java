package no.valg.eva.admin.configuration.domain.model;

import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parents all data for one or more elections run within the same time frame
 */
@Setter
@Entity
@Table(name = "election_event", uniqueConstraints = @UniqueConstraint(columnNames = "election_event_id"))
@AttributeOverride(name = "pk", column = @Column(name = SQLConstants.ELECTION_EVENT_PK))
@NamedQueries({
        @NamedQuery(name = "ElectionEvent.findById", query = "SELECT ev FROM ElectionEvent ev WHERE ev.id = :id"),
        @NamedQuery(name = "ElectionEvent.getElectionGroupSorted", query = "SELECT eg FROM ElectionGroup eg" + " WHERE eg.electionEvent.pk = :electionEventPk"
                + " ORDER BY eg.id"),
        @NamedQuery(
                name = "ElectionEvent.findAllActive",
                query = "SELECT ee FROM ElectionEvent ee WHERE ee.electionEventStatus.id != 9 AND ee.id != :adminEventId")})
public class ElectionEvent extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	public static final String JADIRA_LOCAL_DATE = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate";
    private static final String JADIRA_LOCAL_TIME = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime";

	private Locale locale;
	private ElectionEventStatus electionEventStatus;
	private String id;
	private String name;
	private LocalDate electoralRollCutOffDate;
	private Integer electoralRollLinesPerPage;
	private LocalDate votingCardElectoralRollDate;
	private LocalDate votingCardDeadline;
	private LocalDate voterNumbersAssignedDate;
	private String voterImportDirName;
	private boolean voterImportMunicipality;
	private boolean demoElection;
	private String theme;
	private Set<ElectionDay> electionDays = new HashSet<>();
    private Set<ElectionGroup> electionGroups = new HashSet<>();
    private LocalDate earlyAdvanceVotingStartDate;
    private LocalDate advanceVotingStartDate;
    private LocalDate electionEndDate;
	private LocalTime electionEndTime;

	public ElectionEvent() {
	}

	public ElectionEvent(final String id, final String name, final Locale locale) {
		this.id = id;
		this.name = name;
		this.locale = locale;
	}

	public ElectionEvent(final Long electionEventPk) {
		setPk(electionEventPk);
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "locale_pk", nullable = false)
	@NotNull
	public Locale getLocale() {
		return this.locale;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "election_event_status_pk", nullable = false)
	@NotNull
	public ElectionEventStatus getElectionEventStatus() {
		return this.electionEventStatus;
	}

	@Transient
	public ElectionEventStatusEnum getElectionEventStatusEnum() {
		return getElectionEventStatus().toEnumValue();
	}

	@Transient
	public boolean isCentralConfiguration() {
		return getElectionEventStatusEnum() == ElectionEventStatusEnum.CENTRAL_CONFIGURATION;
	}

	@Transient
	public boolean isLocalConfiguration() {
		return getElectionEventStatusEnum() == ElectionEventStatusEnum.LOCAL_CONFIGURATION;
	}

	@Column(name = "election_event_id", nullable = false, length = 8)
	@ID(size = 6)
	public String getId() {
		return this.id;
	}

	@Column(name = "election_event_name", nullable = false, length = 100)
	@LettersOrDigits
	@StringNotNullEmptyOrBlanks
	@Size(max = 100)
	public String getName() {
		return this.name;
	}

	@Type(type = JADIRA_LOCAL_DATE)
	@Column(name = "electoral_roll_cut_off_date", nullable = false, length = 13)
	public LocalDate getElectoralRollCutOffDate() {
		return electoralRollCutOffDate;
	}

	@Column(name = "electoral_roll_lines_per_page")
	@Min(0)
	@Max(9999)
	public Integer getElectoralRollLinesPerPage() {
		return this.electoralRollLinesPerPage;
	}

	@Type(type = JADIRA_LOCAL_DATE)
	@Column(name = "voting_card_electoral_roll_date", nullable = false, length = 13)
	public LocalDate getVotingCardElectoralRollDate() {
		return votingCardElectoralRollDate;
	}

	@Type(type = JADIRA_LOCAL_DATE)
	@Column(name = "voting_card_deadline", nullable = false, length = 13)
	public LocalDate getVotingCardDeadline() {
		return votingCardDeadline;
	}

	@Type(type = JADIRA_LOCAL_DATE)
	@Column(name = "voter_numbers_assigned_date", length = 13)
	public LocalDate getVoterNumbersAssignedDate() {
		return voterNumbersAssignedDate;
	}

	@Column(name = "voter_import_dir_name", length = 226)
	@Size(max = 226)
	@Pattern(regexp = "(^/.+)?", message = "{@validation.absolutePath}")
	public String getVoterImportDirName() {
		return this.voterImportDirName;
	}

	@Column(name = "voter_import_municipality", nullable = false)
	public boolean isVoterImportMunicipality() {
		return this.voterImportMunicipality;
	}

	@Column(name = "demo_election", nullable = false)
	public boolean isDemoElection() {
		return this.demoElection;
	}

	@Column(name = "theme", length = 125)
	@Size(max = 125)
	public String getTheme() {
		return theme;
	}

    @Type(type = JADIRA_LOCAL_DATE)
    @Column(name = "early_advance_voting_start_date", length = 13)
    public LocalDate getEarlyAdvanceVotingStartDate() {
        return earlyAdvanceVotingStartDate;
    }

    @Type(type = JADIRA_LOCAL_DATE)
    @Column(name = "advance_voting_start_date", length = 13)
    public LocalDate getAdvanceVotingStartDate() {
        return advanceVotingStartDate;
    }

    @Type(type = JADIRA_LOCAL_DATE)
    @Column(name = "election_end_date", length = 13)
	public LocalDate getElectionEndDate() {
		return electionEndDate;
	}

	@Type(type = JADIRA_LOCAL_TIME)
	@Column(name = "election_end_time", length = 15)
	public LocalTime getElectionEndTime() {
		return electionEndTime;
	}

	@OneToMany(mappedBy = "electionEvent", fetch = FetchType.LAZY)
	public Set<ElectionDay> getElectionDays() {
		return electionDays;
	}

    @OneToMany(mappedBy = "electionEvent", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    public Set<ElectionGroup> getElectionGroups() {
        return electionGroups;
    }

    @Override
	public int hashCode() {
		return EqualsHashCodeUtil.genericHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsHashCodeUtil.genericEquals(this, obj);
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.ROOT)) {
			return getPk();
		}
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_EVENT)) {
			return this.getPk();
		}
		return null;
	}

	public AreaPath areaPath() {
		return AreaPath.from(getId());
	}

	public ElectionPath electionPath() {
		return ElectionPath.from(getId());
	}

	public Collection<Election> elections() {
		return getElectionGroups()
				.stream()
				.flatMap(electionGroup -> electionGroup.getElections().stream())
				.collect(Collectors.toList());
	}

	public boolean hasElectionGroupsWithId(String id) {
		for (ElectionGroup election : electionGroups) {
			if (election.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasElectionOnBoroughLevel() {
		return getElectionGroups().stream().anyMatch(ElectionGroup::hasElectionOnBoroughLevel);
	}
	
	@Transient
	public boolean isScanningEnabledInElectionGroup() { return getElectionGroups().stream().anyMatch(ElectionGroup::isScanningPermitted); } 	
}
