package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.util.DateUtil;
import no.evote.validation.FoedselsNummerValidator;
import no.evote.validation.ProposalValidationManual;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.validator.PastLocalDate;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Candidates on candidate lists
 */
@Entity
@Table(name = "candidate", uniqueConstraints = {@UniqueConstraint(columnNames = {"ballot_pk", "display_order"}),
        @UniqueConstraint(columnNames = {"ballot_pk", "candidate_id"})})
@AttributeOverride(name = "pk", column = @Column(name = "candidate_pk"))
@NamedQueries({
        @NamedQuery(name = "Candidate.findByAffiliation", query = "select c from Candidate c where c.affiliation.pk = :pk ORDER BY c.displayOrder"),
        @NamedQuery(name = "Candidate.findByBelowDisplayOrder", query = "select c from Candidate c where c.ballot.pk = :ballotPk AND"
                + " c.displayOrder > :displayOrder ORDER BY c.displayOrder"),
        @NamedQuery(name = "Candidate.findByIdInOtherBallot", query = "select c from Candidate c where " + "c.ballot.ballotStatus.id <> "
                + EvoteConstants.BALLOT_STATUS_WITHDRAWN + " and c.ballot.ballotStatus.id <> " + EvoteConstants.BALLOT_STATUS_REJECTED
                + "and c.id = :id and c.affiliation.ballot.pk <> :bpk and c.affiliation.ballot.contest.election.pk = :epk"),
        @NamedQuery(name = "Candidate.findByIdInAnotherElection", query = "select c from Candidate c where " + "c.ballot.ballotStatus.id <> "
                + EvoteConstants.BALLOT_STATUS_WITHDRAWN + " and c.ballot.ballotStatus.id <> " + EvoteConstants.BALLOT_STATUS_REJECTED
                + "and c.id = :id and c.affiliation.ballot.pk <> :bpk and c.affiliation.ballot.contest.election.electionGroup.pk = :egpk"),
        @NamedQuery(name = "Candidate.findByBallotAndOrder", query = "select c from Candidate c where c.ballot.pk = :bpk AND c.displayOrder = :order"),
        @NamedQuery(name = "Candidate.findPkByBallotAndOrder", query = "select c.pk from Candidate c where c.ballot.pk = :bpk AND c.displayOrder = :order"),
        @NamedQuery(name = "Candidate.findByBallotAndId", query = "select c from Candidate c where c.ballot.pk = :bpk AND c.id = :id"),
        @NamedQuery(name = "Candidate.findCandidateByBallotAndDisplayOrderRange",
                query = "select c from Candidate c where c.ballot.pk = :bpk AND"
                        + " c.displayOrder >= :displayOrderFrom AND c.displayOrder <= :displayOrderTo ORDER BY c.displayOrder"),
        @NamedQuery(name = "Candidate.findCandidatesForOtherBallotsInSameContest",
                query = "select c from Candidate c, Ballot b "
                        + "where b.pk = :ballotPk and c.ballot.contest.pk = b.contest.pk and c.ballot.pk != :ballotPk and c.ballot.ballotStatus.id = "
                        + EvoteConstants.BALLOT_STATUS_APPROVED)})
@NamedNativeQueries({
        @NamedNativeQuery(name = "Candidate.findCandidatesMatchingName",
                query = "SELECT candidate.* FROM mv_area mva_higher_levels "
                        + "  JOIN mv_area mva ON (mva_higher_levels.election_event_pk = mva.election_event_pk AND "
                        + "    ((mva_higher_levels.area_level > 2 AND mva_higher_levels.municipality_pk = mva.municipality_pk) "
                        + "    OR (mva_higher_levels.area_level = 2 AND mva_higher_levels.county_pk = mva.county_pk))) "
                        + "  JOIN contest_area on (contest_area.mv_area_pk = mva_higher_levels.mv_area_pk) "
                        + "  JOIN ballot USING (contest_pk) "
                        + "  JOIN candidate USING (ballot_pk) "
                        + "WHERE mva.area_path = ?1 "
                        + "  AND mva_higher_levels.area_level IN (2, 3, 4) " // -- Valgdistrikter på kommune-, fylkes- og bydelsnivå 
                        + "  AND soundex_tsvector(mva_higher_levels.election_event_pk, name_line) @@ soundex_tsquery(mva_higher_levels.election_event_pk, ?2);",
                resultClass = Candidate.class),
        @NamedNativeQuery(name = "Candidate.findCandidateAtOrBelowArea",
                query = "select can.* from mv_area mva " +
                        "join mv_area mva2 on (text2ltree(mva.area_path) <@ text2ltree(mva2.area_path)) " +
                        "join contest_area ca on mva.mv_area_pk = ca.mv_area_pk " +
                        "join contest con on ca.contest_pk = con.contest_pk " +
                        "join ballot b on con.contest_pk = b.contest_pk " +
                        "join candidate can on b.ballot_pk = can.ballot_pk " +
                        "where can.candidate_id = :candidateId and mva2.area_path = :areaPath",
                resultClass = Candidate.class)
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate extends ProposalPerson implements java.io.Serializable, ContextSecurable {

	@Setter private MaritalStatus maritalStatus;
	@Setter private Affiliation affiliation;
	@Setter private Ballot ballot;
	@Setter private int displayOrder;
	@Setter private String id;
	@Setter private LocalDate dateOfBirth;
	@Setter private boolean baselineVotes;
	@Setter private String nameLine;
	@Setter private String firstName;
	@Setter private String middleName;
	@Setter private String lastName;
	@Setter private String addressLine1;
	@Setter private String addressLine2;
	@Setter private String addressLine3;
	@Setter private String postalCode;
	@Setter private String postTown;
	@Setter private String email;
	@Setter private String telephoneNumber;
	@Setter private String residence;
	@Setter private String profession;
	@Setter private String infoText;
	@Setter private boolean approved;

    private List<UserMessage> validationMessages = new ArrayList<>();
    private String partyName;
    private VoteCategory.VoteCategoryValues voteCategoryValue;
	@Setter private Boolean elected;

    public Candidate(Candidate candidate) {
        super();
        affiliation = candidate.getAffiliation();
        ballot = candidate.getBallot();
        maritalStatus = candidate.getMaritalStatus();
        displayOrder = candidate.getDisplayOrder();
        id = candidate.getId();
        dateOfBirth = candidate.getDateOfBirth();
        baselineVotes = candidate.isBaselineVotes();
        nameLine = candidate.getNameLine();
        firstName = candidate.getFirstName();
        middleName = candidate.getMiddleName();
        lastName = candidate.getLastName();
        addressLine1 = candidate.getAddressLine1();
        addressLine2 = candidate.getAddressLine2();
        addressLine3 = candidate.getAddressLine3();
        postalCode = candidate.getPostalCode();
        postTown = candidate.getPostTown();
        email = candidate.getEmail();
        telephoneNumber = candidate.getTelephoneNumber();
        residence = candidate.getResidence();
        profession = candidate.getProfession();
        infoText = candidate.getInfoText();
        approved = candidate.isApproved();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marital_status_pk", nullable = false)
    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliation_pk")
    public Affiliation getAffiliation() {
        return affiliation;
    }

    @Override
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ballot_pk", nullable = false)
    public Ballot getBallot() {
        return ballot;
    }

    @Override
    @Column(name = "display_order", nullable = false)
    @Min(value = 0, groups = {ProposalValidationManual.class})
    @Max(value = EvoteConstants.MAX_CANDIDATES_IN_AFFILIATION, message = "@validation.candidate.max", groups = {ProposalValidationManual.class})
    public int getDisplayOrder() {
        return displayOrder;
    }

    @Override
    @Column(name = "candidate_id", nullable = false, length = 11)
    public String getId() {
        return id;
    }

    @Override
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @Column(name = "date_of_birth", length = 13)
    @NotNull(message = "@validation.dataOfBirth.notNull", groups = {ProposalValidationManual.class})
    @PastLocalDate(message = "@validation.dataOfBirth.past", groups = {ProposalValidationManual.class})
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Column(name = "baseline_votes", nullable = false)
    public boolean isBaselineVotes() {
        return baselineVotes;
    }

    @Column(name = "name_line", nullable = false, length = 152)
    @Size(max = 152, groups = {ProposalValidationManual.class})
    public String getNameLine() {
        return nameLine;
    }

    @Override
    @Column(name = "first_name", nullable = false, length = 50)
    @NotEmpty(message = "@validation.name.first.notEmpty", groups = {ProposalValidationManual.class})
    @Size(max = 50, message = "@validation.name.length", groups = {ProposalValidationManual.class})
    public String getFirstName() {
        return firstName;
    }

    @Override
    @Column(name = "middle_name", length = 50)
    @Size(max = 50, groups = {ProposalValidationManual.class})
    public String getMiddleName() {
        return middleName;
    }

    @Override
    @Column(name = "last_name", nullable = false, length = 50)
    @NotEmpty(message = "@validation.name.last.notEmpty", groups = {ProposalValidationManual.class})
    @Size(max = 50, message = "@validation.name.length", groups = {ProposalValidationManual.class})
    public String getLastName() {
        return lastName;
    }

    @Column(name = "address_line1", length = 50)
    @Length(min = 0, max = 50, message = "@validation.address.length", groups = {ProposalValidationManual.class})
    public String getAddressLine1() {
        return addressLine1;
    }

    @Column(name = "address_line2", length = 50)
    @Size(max = 50, groups = {ProposalValidationManual.class})
    public String getAddressLine2() {
        return addressLine2;
    }

    @Column(name = "address_line3", length = 50)
    @Size(max = 50, groups = {ProposalValidationManual.class})
    public String getAddressLine3() {
        return addressLine3;
    }

    @Override
    @Column(name = "postal_code", length = 4)
    @Pattern(regexp = "([0-9]{4})?", message = "@validation.postalCode.regex", groups = {ProposalValidationManual.class})
    public String getPostalCode() {
        return postalCode;
    }

    @Column(name = "post_town", length = 50)
    @Length(min = 0, max = 20, message = "@validation.postalTown.length", groups = {ProposalValidationManual.class})
    public String getPostTown() {
        return postTown;
    }

    @Column(name = "email", length = 129)
    @Length(min = 0, max = 129, message = "@validation.email.length", groups = {ProposalValidationManual.class})
    @Email(message = "@validation.email", groups = {ProposalValidationManual.class})
    public String getEmail() {
        return email;
    }

    @Column(name = "telephone_number", length = 35)
    @Length(min = 0, max = 35, message = "@validation.tlf.length", groups = {ProposalValidationManual.class})
    @Pattern(regexp = "\\+?([0-9]{3,14})?", message = "@validation.tlf.regex", groups = {ProposalValidationManual.class})
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    @Column(name = "residence", length = 50)
    @Length(min = 0, max = 50, message = "@validation.residence.length", groups = {ProposalValidationManual.class})
    public String getResidence() {
        return residence;
    }

    @Column(name = "profession", length = 50)
    @Size(max = 50, message = "@validation.profession.length", groups = {ProposalValidationManual.class})
    public String getProfession() {
        return profession;
    }

    @Column(name = "info_text", length = 150)
    @Size(max = 150, message = "@validation.infoText.length", groups = {ProposalValidationManual.class})
    public String getInfoText() {
        return infoText;
    }

    @Column(name = "approved", nullable = false)
    public boolean isApproved() {
        return approved;
    }

    @Transient
    public String getFormattedDateOfBirth() {
        return DateUtil.getFormattedShortDate(getDateOfBirth());
    }

    @Transient
    public void setFormattedDateOfBirth(final String dateOfBirth) {
        setDateOfBirth(DateUtil.parseLocalDate(dateOfBirth));
    }

    @Override
    @Transient
    public boolean isInvalid() {
        return !validationMessages.isEmpty();
    }

    @Override
    @Transient
    public void addValidationMessage(UserMessage validationMessage) {
        validationMessages.add(validationMessage);
    }

    @Override
    @Transient
    public void clearValidationMessages() {
        validationMessages.clear();
    }

    @Override
    @Transient
    public List<UserMessage> getValidationMessageList() {
        return validationMessages;
    }

    public void setValidationMessageList(final List<UserMessage> validationMessages) {
        this.validationMessages = validationMessages;
    }

    @Override
    @Transient
    public boolean isIdSet() {
        return FoedselsNummerValidator.isFoedselsNummerValid(id);
    }

    @Transient
    public String getPartyName() {
        return partyName;
    }

    @Transient
    public void setPartyName(final String partyName) {
        this.partyName = partyName;
    }

    @Transient
    public VoteCategory.VoteCategoryValues getVoteCategoryValue() {
        return voteCategoryValue;
    }

    @Transient
    public void setVoteCategoryValue(final VoteCategory.VoteCategoryValues voteCategoryValue) {
        this.voteCategoryValue = voteCategoryValue;
    }

    @Transient
    public String getInformationString(final boolean showCandidateProfession, final boolean showCandidateResidence) {
        StringBuilder candidateInfo = new StringBuilder();

        candidateInfo.append(toString()).append(", ");

        if (!StringUtils.isEmpty(getFormattedDateOfBirth())) {
            candidateInfo.append(DateTimeFormat.forPattern("yyyy").withLocale(Locale.getDefault()).print(getDateOfBirth())).append(", ");
        }
        if (showCandidateProfession && !StringUtils.isEmpty(getProfession())) {
            candidateInfo.append(getProfession()).append(", ");
        }
        if (showCandidateResidence && !StringUtils.isEmpty(getResidence())) {
            candidateInfo.append(getResidence()).append(", ");
        }

        return candidateInfo.toString().substring(0, candidateInfo.length() - 2);
    }

    @Transient
    public Boolean getElected() {
        return elected;
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        return null;
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        if (level.equals(ElectionLevelEnum.CONTEST)) {
            return ballot.getContest().getPk();
        }
        return null;
    }

    @Transient
    public MvArea getMvArea() {
        return getBallot().getContest().getContestAreaList().stream()
                .map(ContestArea::getMvArea)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Contest har ingen ContestArea"));
    }

    @Transient
    public boolean isMale() {
        return getGender().equals(Gender.MALE.getValue());
    }

    @Transient
    public boolean isFemale() {
        return getGender().equals(Gender.FEMALE.getValue());
    }

    @Transient
    public String getGender() {
        return !FoedselsNummerValidator.isFoedselsNummerValid(getId()) ? "" : isOdd(ninthDigit(getId())) ? Gender.MALE.getValue() : Gender.FEMALE.getValue();
    }

    private boolean isOdd(final int i) {
        return i % 2 != 0;
    }

    private Integer ninthDigit(final String fnr) {

        return Integer.valueOf(fnr.substring(8, 9));

    }

    public void accept(ConfigurationVisitor configurationVisitor) {
        if (configurationVisitor.include(this)) {
            configurationVisitor.visit(this);
        }
    }

    public enum Gender {
        FEMALE("F"), MALE("M");

        private final String value;

        Gender(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
