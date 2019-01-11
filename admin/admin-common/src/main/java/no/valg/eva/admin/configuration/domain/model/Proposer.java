package no.valg.eva.admin.configuration.domain.model;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.util.DateUtil;
import no.evote.validation.FoedselsNummerValidator;
import no.evote.validation.ProposalValidationManual;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.validator.PastLocalDate;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;

/**
 * Proposers (signers) of candidate lists
 */
@Entity
@Table(name = "proposer", uniqueConstraints = { @UniqueConstraint(columnNames = { "ballot_pk", "proposer_id" }),
		@UniqueConstraint(columnNames = { "ballot_pk", "display_order" }) })
@AttributeOverride(name = "pk", column = @Column(name = "proposer_pk"))
@NamedQueries({
		@NamedQuery(name = "Proposer.findByBallot", query = "select p from Proposer p where p.ballot.pk = :pk ORDER BY p.displayOrder"),
		@NamedQuery(name = "Proposer.findByBallotAndOrder", query = "select p from Proposer p where p.ballot.pk = :bpk AND p.displayOrder = :order"),
		@NamedQuery(
				name = "Proposer.findByBelowDisplayOrder",
				query = "select p from Proposer p where p.ballot.pk = :ballotPk AND p.displayOrder > :displayOrder ORDER BY p.displayOrder"),
		@NamedQuery(name = "Proposer.findByBallotAndId", query = "select p from Proposer p where p.ballot.pk = :bpk AND p.id = :id"),
		@NamedQuery(name = "Proposer.findByIdInOtherBallot", query = "select p from Proposer p where "
				+ " p.ballot.pk <> :bpk and p.id = :id and p.ballot.contest.election.pk = :epk and" + " p.ballot.ballotStatus.id <> "
				+ EvoteConstants.BALLOT_STATUS_WITHDRAWN + " and p.ballot.ballotStatus.id <> " + EvoteConstants.BALLOT_STATUS_REJECTED),
		@NamedQuery(name = "Proposer.findProposerByBallotAndDisplayOrderRange",
				query = "select p from Proposer p where p.ballot.pk = :bpk AND"
						+ " p.displayOrder >= :displayOrderFrom AND p.displayOrder <= :displayOrderTo ORDER BY p.displayOrder") })
public class Proposer extends ProposalPerson implements java.io.Serializable, ContextSecurable {

	private ProposerRole proposerRole;
	private Ballot ballot;
	private int displayOrder;
	private String id;
	private LocalDate dateOfBirth;
	private String nameLine;
	private String firstName;
	private String middleName;
	private String lastName;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String postalCode;
	private String postTown;
	private String email;
	private String telephoneNumber;
	private boolean approved;

	private List<UserMessage> validationMessages = new ArrayList<>();

	public Proposer() {
		super();
	}
	
	public Proposer(String firstName, String lastName, LocalDate dateOfBirth) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
	}

	public Proposer(Proposer proposer) {
		super();
		ballot = proposer.getBallot();
		displayOrder = proposer.getDisplayOrder();
		id = proposer.getId();
		proposerRole = proposer.getProposerRole();
		dateOfBirth = proposer.getDateOfBirth();
		nameLine = proposer.getNameLine();
		firstName = proposer.getFirstName();
		middleName = proposer.getMiddleName();
		lastName = proposer.getLastName();
		addressLine1 = proposer.getAddressLine1();
		addressLine2 = proposer.getAddressLine2();
		addressLine3 = proposer.getAddressLine3();
		postalCode = proposer.getPostalCode();
		postTown = proposer.getPostTown();
		email = proposer.getEmail();
		telephoneNumber = proposer.getTelephoneNumber();
		approved = proposer.isApproved();
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "proposer_role_pk")
	public ProposerRole getProposerRole() {
		return proposerRole;
	}

	public void setProposerRole(final ProposerRole proposerRole) {
		this.proposerRole = proposerRole;
	}

	@Override
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ballot_pk", nullable = false)
	public Ballot getBallot() {
		return ballot;
	}

	public void setBallot(final Ballot ballot) {
		this.ballot = ballot;
	}

	@Override
	@Column(name = "display_order", nullable = false)
	public int getDisplayOrder() {
		return displayOrder;
	}

	@Override
	public void setDisplayOrder(final int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Override
	@Column(name = "proposer_id", nullable = false, length = 11)
	public String getId() {
		return id;
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	@Override
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "date_of_birth", length = 13)
	@NotNull(message = "@validation.dataOfBirth.notNull", groups = { ProposalValidationManual.class })
	@PastLocalDate(message = "@validation.dataOfBirth.past", groups = { ProposalValidationManual.class })
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@Column(name = "name_line", nullable = false, length = 152)
	@Size(max = 152, groups = { ProposalValidationManual.class })
	public String getNameLine() {
		return nameLine;
	}

	@Override
	public void setNameLine(final String nameLine) {
		this.nameLine = nameLine;
	}

	@Override
	@Column(name = "first_name", nullable = false, length = 50)
	@NotEmpty(message = "@validation.name.first.notEmpty", groups = { ProposalValidationManual.class })
	@Size(max = 50, message = "@validation.name.length", groups = { ProposalValidationManual.class })
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	@Override
	@Column(name = "middle_name", length = 50)
	@Size(max = 50, groups = { ProposalValidationManual.class })
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(final String middleName) {
		this.middleName = middleName;
	}

	@Override
	@Column(name = "last_name", nullable = false, length = 50)
	@NotEmpty(message = "@validation.name.last.notEmpty", groups = { ProposalValidationManual.class })
	@Size(max = 50, message = "@validation.name.length", groups = { ProposalValidationManual.class })
	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "address_line1", length = 50)
	@Length(min = 0, max = 50, message = "@validation.address.length", groups = { ProposalValidationManual.class })
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(final String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "address_line2", length = 50)
	@Size(max = 50, groups = { ProposalValidationManual.class })
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(final String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(name = "address_line3", length = 50)
	@Size(max = 50, groups = { ProposalValidationManual.class })
	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(final String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Override
	@Pattern(regexp = "([0-9]{4})?", message = "@validation.postalCode.regex", groups = { ProposalValidationManual.class })
	@Column(name = "postal_code", length = 4)
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	@Column(name = "post_town", length = 50)
	@Length(min = 0, max = 20, message = "@validation.postalTown.length", groups = { ProposalValidationManual.class })
	public String getPostTown() {
		return postTown;
	}

	@Override
	public void setPostTown(final String postTown) {
		this.postTown = postTown;
	}

	@Column(name = "email", length = 129)
	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	@Column(name = "telephone_number", length = 35)
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(final String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	@Column(name = "approved", nullable = false)
	public boolean isApproved() {
		return approved;
	}

	@Override
	public void setApproved(final boolean approved) {
		this.approved = approved;
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

	@Transient
	/**
	 * Ment brukt for å sjekke om en underskrift kan ansees å være reelt fylt inn.
	 * Dette vil utelukke automatisk genererte underskrifter som ikke har navn, etternavn og fødselsdato fylt inn
	 */
	public boolean isUtfyltUnderskrift() {
		return isNotBlank(getFirstName())
			&& isNotBlank(getLastName())
			&& getDateOfBirth() != null;
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

	public void setValidationMessageList(List<UserMessage> validationMessages) {
		this.validationMessages = validationMessages;
	}

	@Override
	@Transient
	public boolean isIdSet() {
		return FoedselsNummerValidator.isFoedselsNummerValid(id);
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

}
