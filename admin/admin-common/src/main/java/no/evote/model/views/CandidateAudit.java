package no.evote.model.views;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

import no.valg.eva.admin.util.DateUtil;
import no.evote.validation.FoedselsNummerValidator;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

@Entity
@Table(name = "candidate_audit")
/* displayOrder 98 and 99 is used for swapping displayOrder */
@NamedQueries({@NamedQuery(name = "CandidateAudit.byBallot", query = "SELECT ca FROM CandidateAudit ca WHERE ca.ballotPk = :ballotPk "
		+ "AND ca.displayOrder != 98 AND ca.displayOrder !=99 ORDER BY ca.id.auditTimestamp DESC ")})
public class CandidateAudit implements java.io.Serializable {

	private String auditOperation;
	private CandidateAuditId id;
	private Integer affiliationPk;
	private Long ballotPk;
	private Integer maritalStatusPk;
	private Integer displayOrder;
	private String candidateId;
	private LocalDate dateOfBirth;
	private Boolean baselineVotes;
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
	private String residence;
	private String profession;
	private String infoText;
	private Boolean approved;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "candidatePk", column = @Column(name = "candidate_pk", nullable = false)),
			@AttributeOverride(name = "XOplock", column = @Column(name = "x_oplock", nullable = false)),
			@AttributeOverride(name = "XTimestamp", column = @Column(name = "x_timestamp", nullable = false, length = 29)) })
	public CandidateAuditId getId() {
		return this.id;
	}

	public void setId(final CandidateAuditId id) {
		this.id = id;
	}

	@Column(name = "affiliation_pk")
	public Integer getAffiliationPk() {
		return this.affiliationPk;
	}

	public void setAffiliationPk(final Integer affiliationPk) {
		this.affiliationPk = affiliationPk;
	}

	@Column(name = "ballot_pk")
	public Long getBallotPk() {
		return this.ballotPk;
	}

	public void setBallotPk(final Long ballotPk) {
		this.ballotPk = ballotPk;
	}

	@Column(name = "marital_status_pk")
	public Integer getMaritalStatusPk() {
		return this.maritalStatusPk;
	}

	public void setMaritalStatusPk(final Integer maritalStatusPk) {
		this.maritalStatusPk = maritalStatusPk;
	}

	@Column(name = "display_order")
	public Integer getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(final Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "candidate_id", length = 11)
	public String getCandidateId() {
		return this.candidateId;
	}

	public void setCandidateId(final String candidateId) {
		this.candidateId = candidateId;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "date_of_birth", length = 13)
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@Column(name = "baseline_votes")
	public Boolean getBaselineVotes() {
		return this.baselineVotes;
	}

	public void setBaselineVotes(final Boolean baselineVotes) {
		this.baselineVotes = baselineVotes;
	}

	@Column(name = "name_line")
	public String getNameLine() {
		return this.nameLine;
	}

	public void setNameLine(final String nameLine) {
		this.nameLine = nameLine;
	}

	@Column(name = "first_name")
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "middle_name")
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(final String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "last_name")
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "address_line1")
	public String getAddressLine1() {
		return this.addressLine1;
	}

	public void setAddressLine1(final String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "address_line2")
	public String getAddressLine2() {
		return this.addressLine2;
	}

	public void setAddressLine2(final String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(name = "address_line3")
	public String getAddressLine3() {
		return this.addressLine3;
	}

	public void setAddressLine3(final String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Column(name = "postal_code", length = 4)
	@Pattern(regexp = "([0-9]{4})?", message = "@validation.postalCode.regex")
	public String getPostalCode() {
		return this.postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	@Column(name = "post_town")
	public String getPostTown() {
		return this.postTown;
	}

	public void setPostTown(final String postTown) {
		this.postTown = postTown;
	}

	@Column(name = "email", length = 129)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	@Column(name = "telephone_number", length = 35)
	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public void setTelephoneNumber(final String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	@Column(name = "residence")
	public String getResidence() {
		return this.residence;
	}

	public void setResidence(final String residence) {
		this.residence = residence;
	}

	@Column(name = "profession")
	public String getProfession() {
		return this.profession;
	}

	public void setProfession(final String profession) {
		this.profession = profession;
	}

	@Column(name = "info_text")
	public String getInfoText() {
		return this.infoText;
	}

	public void setInfoText(final String infoText) {
		this.infoText = infoText;
	}

	@Column(name = "approved")
	public Boolean getApproved() {
		return this.approved;
	}

	public void setApproved(final Boolean approved) {
		this.approved = approved;
	}

	@Column(name = "audit_operation", length = 1)
	public String getAuditOperation() {
		return this.auditOperation;
	}

	public void setAuditOperation(final String auditOperation) {
		this.auditOperation = auditOperation;
	}

	@Transient
	public String getAuditOperationText() {
		switch (auditOperation) {
			case "I":
			return "@operation.created";
			case "U":
			return "@operation.updated";
			case "D":
			return "@operation.deleted";
		default:
			return "";
		}
	}

	@Transient
	public boolean isIdSet() {
		return FoedselsNummerValidator.isFoedselsNummerValid(candidateId);
	}

	@Transient
	public void setFormattedDateOfBirth(final String dateOfBirth) {
		setDateOfBirth(DateUtil.parseLocalDate(dateOfBirth));
	}

	@Transient
	public String getFormattedDateOfBirth() {
		return DateUtil.getFormattedShortDate(getDateOfBirth());
	}

}
