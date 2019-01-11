package no.valg.eva.admin.configuration.domain.model;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurableDynamicArea;
import no.evote.security.ContextSecurableDynamicElection;
import no.evote.validation.Letters;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.evote.validation.ValideringVedManuellRegistrering;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

/**
 * Officers in reporting units
 */
@Entity
@Table(name = "responsible_officer")
@AttributeOverride(name = "pk", column = @Column(name = "responsible_officer_pk"))
@NamedNativeQueries({
        @NamedNativeQuery(name = "ResponsibleOfficer.findResponsibleOfficersMatchingNameByContest", query = 
                "SELECT ro.* " +
                "FROM mv_area mva " +
                "JOIN mv_area mva2 ON (text2ltree(mva.area_path) @> text2ltree(mva2.area_path) AND mva.election_event_pk = mva2.election_event_pk) " +
                "JOIN reporting_unit ru ON (ru.mv_area_pk = mva2.mv_area_pk) " +
                "JOIN reporting_unit_type rut USING (reporting_unit_type_pk) " +
                "JOIN responsible_officer ro USING (reporting_unit_pk) " +
                "WHERE mva.mv_area_pk = :areaPk AND rut.reporting_unit_type_id = 6 " +
                "AND soundex_tsvector(mva2.election_event_pk, ro.name_line) @@ soundex_tsquery(mva2.election_event_pk, :nameLine);", 
				resultClass = ResponsibleOfficer.class)
})
@NamedQueries({
		@NamedQuery(name = "ResponsibleOfficer.findResponsibleOfficersForReportingUnit", query = "SELECT ro FROM ResponsibleOfficer ro"
				+ " WHERE ro.reportingUnit.pk = :reportingUnitPk ORDER BY ro.displayOrder"),
		@NamedQuery(name = "ResponsibleOfficer.countResponsibleOfficersForReportingUnit", query = "SELECT count(ro.pk) FROM ResponsibleOfficer ro"
		+ " WHERE ro.reportingUnit.pk = :reportingUnitPk"),
		@NamedQuery(name = "ResponsibleOfficer.findNextDisplayOrder", query = "SELECT max(ro.displayOrder) FROM ResponsibleOfficer ro"
				+ " WHERE ro.reportingUnit.pk = :reportingUnitPk") })
public class ResponsibleOfficer extends VersionedEntity implements java.io.Serializable, ContextSecurableDynamicElection, ContextSecurableDynamicArea {

	private ReportingUnit reportingUnit;
	private Responsibility responsibility;
	private int displayOrder;
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

	public ResponsibleOfficer() {
		super();
	}

	public ResponsibleOfficer(ResponsibleOfficer respOfficer) {

		super();
		this.reportingUnit = respOfficer.getReportingUnit();
		this.responsibility = respOfficer.getResponsibility();
		this.displayOrder = respOfficer.getDisplayOrder();
		this.nameLine = respOfficer.getNameLine();
		this.firstName = respOfficer.getFirstName();
		this.middleName = respOfficer.getMiddleName();
		this.lastName = respOfficer.getLastName();
		this.addressLine1 = respOfficer.getAddressLine1();
		this.addressLine2 = respOfficer.getAddressLine2();
		this.addressLine3 = respOfficer.getAddressLine3();
		this.postalCode = respOfficer.getPostalCode();
		this.postTown = respOfficer.getPostTown();
		this.email = respOfficer.getEmail();
		this.telephoneNumber = respOfficer.getTelephoneNumber();

	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "reporting_unit_pk", nullable = false)
	@NotNull(groups = { Default.class, ValideringVedManuellRegistrering.class })
	public ReportingUnit getReportingUnit() {
		return this.reportingUnit;
	}

	public void setReportingUnit(final ReportingUnit reportingUnit) {
		this.reportingUnit = reportingUnit;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "responsibility_pk", nullable = false)
	@NotNull(groups = { Default.class, ValideringVedManuellRegistrering.class })
	public Responsibility getResponsibility() {
		return this.responsibility;
	}

	public void setResponsibility(final Responsibility responsibility) {
		this.responsibility = responsibility;
	}

	@Column(name = "display_order", nullable = false)
	@Min(1)
	@Max(9999)
	public int getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(final int displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "name_line", nullable = false, length = 152)
	@StringNotNullEmptyOrBlanks
	@Size(max = 152)
	public String getNameLine() {
		return this.nameLine;
	}

	public void setNameLine(final String nameLine) {
		this.nameLine = nameLine;
	}

	@Column(name = "first_name", nullable = false, length = 50)
	@Letters(extraChars = " .-'", groups = ValideringVedManuellRegistrering.class)
	@StringNotNullEmptyOrBlanks(groups = { ValideringVedManuellRegistrering.class })
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "middle_name", length = 50)
	@Letters(extraChars = " .-'", groups = ValideringVedManuellRegistrering.class)
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(final String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "last_name", nullable = false, length = 50)
	@Letters(extraChars = " .-'", groups = ValideringVedManuellRegistrering.class)
	@StringNotNullEmptyOrBlanks(groups = { ValideringVedManuellRegistrering.class })
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "address_line1", length = 50)
	@LettersOrDigits(groups = ValideringVedManuellRegistrering.class)
	@Length(min = 0, max = 50, message = "{@validation.streetAddress.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getAddressLine1() {
		return this.addressLine1;
	}

	public void setAddressLine1(final String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "address_line2", length = 50)
	@LettersOrDigits(groups = ValideringVedManuellRegistrering.class)
	@Length(min = 0, max = 50, message = "{@validation.coAddress.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getAddressLine2() {
		return this.addressLine2;
	}

	public void setAddressLine2(final String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(name = "address_line3", length = 50)
	@LettersOrDigits(groups = ValideringVedManuellRegistrering.class)
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getAddressLine3() {
		return this.addressLine3;
	}

	public void setAddressLine3(final String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Column(name = "postal_code", length = 4)
	@Pattern(regexp = "([0-9]{4})?", message = "{@validation.postalCode.regex}", groups = { ValideringVedManuellRegistrering.class })
	public String getPostalCode() {
		return this.postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	@Column(name = "post_town", length = 50)
	@Letters(groups = ValideringVedManuellRegistrering.class)
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getPostTown() {
		return this.postTown;
	}

	public void setPostTown(final String postTown) {
		this.postTown = postTown;
	}

	@Column(name = "email", length = 129)
	@Email(message = "{@validation.email}", groups = { ValideringVedManuellRegistrering.class })
	@Size(max = 129)
	public String getEmail() {
		return this.email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	@Column(name = "telephone_number", length = 35)
	@Length(min = 0, max = 35, message = "{@validation.tlf.length}", groups = { ValideringVedManuellRegistrering.class })
	@Pattern(regexp = "\\+?([0-9]{3,34})?", message = "{@validation.tlf.regex}", groups = { ValideringVedManuellRegistrering.class })
	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}

	public void setTelephoneNumber(final String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return reportingUnit.getAreaPk(level);
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		return reportingUnit.getElectionPk(level);
	}

	@Override
	@Transient
	public AreaLevelEnum getActualAreaLevel() {
		return reportingUnit.getActualAreaLevel();
	}

	@Override
	@Transient
	public ElectionLevelEnum getActualElectionLevel() {
		return reportingUnit.getActualElectionLevel();
	}

	/**
	 * Builds and sets name line with last, first and middle name
	 */
	public void updateNameLine() {
		this.nameLine = getLastName() + " " + getFirstName() + (StringUtils.isEmpty(getMiddleName()) ? "" : " " + getMiddleName());
		this.nameLine = this.nameLine.trim();
	}
}
