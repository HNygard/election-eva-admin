package no.evote.model.views;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;

import no.evote.constants.SQLConstants;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

@Entity
@Table(name = "voter_audit")
public class VoterAudit implements java.io.Serializable {
	private VoterAuditId id;
	private String auditUser;
	private String auditOperator;
	private Character auditOperation;
	private Long electionEventPk;
	private String voterId;
	private LocalDate dateOfBirth;
	private Long voterNumber;
	private Integer importBatchNumber;
	private String countryId;
	private String countyId;
	private String municipalityId;
	private String boroughId;
	private String pollingDistrictId;
	private Boolean eligible;
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
	private Boolean mailingAddressSpecified;
	private String mailingAddressLine1;
	private String mailingAddressLine2;
	private String mailingAddressLine3;
	private String mailingCountryCode;
	private String approvalRequest;
	private Boolean approved;
	private DateTime dateTimeSubmitted;
	private String aarsakskode;
	private Character endringstype;
	private Character statuskode;
	private LocalDate regDato;
	private Character spesRegType;

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "voterPk", column = @Column(name = "voter_pk", nullable = false)),
			@AttributeOverride(name = "auditOplock", column = @Column(name = "audit_oplock", nullable = false)),
			@AttributeOverride(name = "auditTimestamp", column = @Column(name = "audit_timestamp", nullable = false, length = 29)) })
	public VoterAuditId getId() {
		return this.id;
	}

	public void setId(final VoterAuditId id) {
		this.id = id;
	}

	@Column(name = "audit_user")
	public String getAuditUser() {
		return this.auditUser;
	}

	public void setAuditUser(final String auditUser) {
		this.auditUser = auditUser;
	}

	@Column(name = "audit_operator")
	public String getAuditOperator() {
		return this.auditOperator;
	}

	public void setAuditOperator(final String auditOperator) {
		this.auditOperator = auditOperator;
	}

	@Column(name = "audit_operation", length = 1)
	public Character getAuditOperation() {
		return this.auditOperation;
	}

	public void setAuditOperation(final Character auditOperation) {
		this.auditOperation = auditOperation;
	}

	@Column(name = SQLConstants.ELECTION_EVENT_PK)
	public Long getElectionEventPk() {
		return this.electionEventPk;
	}

	public void setElectionEventPk(final Long electionEventPk) {
		this.electionEventPk = electionEventPk;
	}

	@Column(name = "voter_id", length = 11)
	public String getVoterId() {
		return this.voterId;
	}

	public void setVoterId(final String voterId) {
		this.voterId = voterId;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "date_of_birth", length = 13)
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@Column(name = "voter_number")
	public Long getVoterNumber() {
		return this.voterNumber;
	}

	public void setVoterNumber(final Long voterNumber) {
		this.voterNumber = voterNumber;
	}

	@Column(name = "import_batch_number")
	public Integer getImportBatchNumber() {
		return this.importBatchNumber;
	}

	public void setImportBatchNumber(final Integer importBatchNumber) {
		this.importBatchNumber = importBatchNumber;
	}

	@Column(name = "country_id", length = 2)
	public String getCountryId() {
		return this.countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	@Column(name = "county_id", length = 2)
	public String getCountyId() {
		return this.countyId;
	}

	public void setCountyId(final String countyId) {
		this.countyId = countyId;
	}

	@Column(name = "municipality_id", length = 4)
	public String getMunicipalityId() {
		return this.municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Column(name = "borough_id", length = 6)
	public String getBoroughId() {
		return this.boroughId;
	}

	public void setBoroughId(final String boroughId) {
		this.boroughId = boroughId;
	}

	@Column(name = "polling_district_id", length = 4)
	public String getPollingDistrictId() {
		return this.pollingDistrictId;
	}

	public void setPollingDistrictId(final String pollingDistrictId) {
		this.pollingDistrictId = pollingDistrictId;
	}

	@Column(name = "eligible")
	public Boolean getEligible() {
		return this.eligible;
	}

	public void setEligible(final Boolean eligible) {
		this.eligible = eligible;
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

	@Column(name = "mailing_address_specified")
	public Boolean getMailingAddressSpecified() {
		return this.mailingAddressSpecified;
	}

	public void setMailingAddressSpecified(final Boolean mailingAddressSpecified) {
		this.mailingAddressSpecified = mailingAddressSpecified;
	}

	@Column(name = "mailing_address_line1")
	public String getMailingAddressLine1() {
		return this.mailingAddressLine1;
	}

	public void setMailingAddressLine1(final String mailingAddressLine1) {
		this.mailingAddressLine1 = mailingAddressLine1;
	}

	@Column(name = "mailing_address_line2")
	public String getMailingAddressLine2() {
		return this.mailingAddressLine2;
	}

	public void setMailingAddressLine2(final String mailingAddressLine2) {
		this.mailingAddressLine2 = mailingAddressLine2;
	}

	@Column(name = "mailing_address_line3")
	public String getMailingAddressLine3() {
		return this.mailingAddressLine3;
	}

	public void setMailingAddressLine3(final String mailingAddressLine3) {
		this.mailingAddressLine3 = mailingAddressLine3;
	}

	@Column(name = "mailing_country_code")
	public String getMailingCountryCode() {
		return this.mailingCountryCode;
	}

	public void setMailingCountryCode(final String mailingCountryCode) {
		this.mailingCountryCode = mailingCountryCode;
	}

	@Column(name = "approval_request")
	public String getApprovalRequest() {
		return this.approvalRequest;
	}

	public void setApprovalRequest(final String approvalRequest) {
		this.approvalRequest = approvalRequest;
	}

	@Column(name = "approved")
	public Boolean getApproved() {
		return this.approved;
	}

	public void setApproved(final Boolean approved) {
		this.approved = approved;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "date_time_submitted", length = 29)
	public DateTime getDateTimeSubmitted() {
		return dateTimeSubmitted;
	}

	public void setDateTimeSubmitted(DateTime dateTimeSubmitted) {
		this.dateTimeSubmitted = dateTimeSubmitted;
	}

	@Column(name = "aarsakskode", length = 2)
	public String getAarsakskode() {
		return this.aarsakskode;
	}

	public void setAarsakskode(final String aarsakskode) {
		this.aarsakskode = aarsakskode;
	}

	@Column(name = "endringstype", length = 1)
	public Character getEndringstype() {
		return this.endringstype;
	}

	public void setEndringstype(final Character endringstype) {
		this.endringstype = endringstype;
	}

	@Column(name = "statuskode", length = 1)
	public Character getStatuskode() {
		return this.statuskode;
	}

	public void setStatuskode(final Character statuskode) {
		this.statuskode = statuskode;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "reg_dato", length = 13)
	public LocalDate getRegDato() {
		return regDato;
	}

	public void setRegDato(LocalDate regDato) {
		this.regDato = regDato;
	}

	@Column(name = "spes_reg_type", length = 1)
	public Character getSpesRegType() {
		return this.spesRegType;
	}

	public void setSpesRegType(final Character spesRegType) {
		this.spesRegType = spesRegType;
	}

}
