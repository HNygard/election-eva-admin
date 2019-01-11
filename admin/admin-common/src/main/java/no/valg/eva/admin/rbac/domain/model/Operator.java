package no.valg.eva.admin.rbac.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import no.valg.eva.admin.util.StringUtil;
import no.evote.validation.FoedselsNummer;
import no.evote.validation.Letters;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.OperatorValidationManual;
import no.valg.eva.admin.common.rbac.service.ContactInfo;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * RBAC: Application user
 */
@Entity
@Table(name = "operator", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "operator_id" }))
@AttributeOverride(name = "pk", column = @Column(name = "operator_pk"))
@NamedQueries({
		@NamedQuery(name = "Operator.findAll", query = "SELECT o FROM Operator o ORDER BY o.lastName, o.firstName, o.middleName"),
		@NamedQuery(name = "Operator.findById", query = "SELECT o FROM Operator o WHERE o.id = :id ORDER BY o.lastName, o.firstName, o.middleName"),
		@NamedQuery(name = "Operator.findByElectionEventAndId", query = "SELECT o FROM Operator o WHERE o.electionEvent.pk = :eventPk AND o.id = :operatorId"),
		@NamedQuery(name = "Operator.findElectionEvents", query = "SELECT e FROM Operator o, IN( o.electionEvent ) e WHERE o.id = :operatorId"),
		@NamedQuery(
				name = "Operator.findOperatorsWithRolesIn",
				query = "select distinct o from Operator o, OperatorRole oro, Role r where oro.operator = o and oro.role = r and r in(:roles)") })
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "Operator.findByName",
				query = "SELECT * FROM Operator WHERE election_event_pk = :electionEventPk"
						+ " AND soundex_tsvector(election_event_pk, name_line) @@ soundex_tsquery(:electionEventPk, :nameLine) LIMIT 1000",
				resultClass = Operator.class),
		@NamedNativeQuery(name = "Operator.findByNameAndArea", 
			query = "SELECT DISTINCT op.* FROM operator op "
				  + "  LEFT JOIN operator_role opr USING (operator_pk) "
				  +	"WHERE opr.mv_area_pk IN ( "
				  +	"  SELECT DISTINCT mva.mv_area_pk "
				  +	"  FROM mv_area mva "
				  +	"    LEFT JOIN mv_area mva2 ON (public.text2ltree(mva2.area_path) OPERATOR(public.@>) public.text2ltree(mva.area_path)) "
				  +	"  WHERE mva2.mv_area_pk = ?1 AND mva2.election_event_pk = ?2) "
				  + " AND soundex_tsvector(election_event_pk, name_line) @@ soundex_tsquery(?2, ?3) LIMIT 20",
			resultClass = Operator.class),
		@NamedNativeQuery(name = "Operator.findAllDistinctId", query = "SELECT DISTINCT ON (o.operator_id) * FROM operator o", resultClass = Operator.class) })
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operator extends VersionedEntity implements java.io.Serializable {

	private static final String REGEX_KEY_SERIAL_NUMBER = "(\\d{4}-\\d{4}-\\d{9})?";

	@Setter private ElectionEvent electionEvent;
	@Setter private String id;
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
	@Setter private String infoText;
	@Setter private boolean active;
	@Setter private boolean contactInfoConfirmed;
	@Setter private String keySerialNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	@FoedselsNummer(groups = { OperatorValidationManual.class })
	@Column(name = "operator_id", nullable = false, length = 11)
	public String getId() {
		return id;
	}

	@Column(name = "name_line", nullable = false, length = 152)
	@Size(max = 152)
	public String getNameLine() {
		return nameLine;
	}

	@NotEmpty(message = "{@validation.name.first.notEmpty}", groups = { OperatorValidationManual.class })
	@Size(max = 50, groups = { OperatorValidationManual.class })
	@Letters(extraChars = " .-'", groups = { OperatorValidationManual.class })
	@Column(name = "first_name", nullable = false, length = 50)
	public String getFirstName() {
		return firstName;
	}

	@Column(name = "middle_name", length = 50)
	@Size(max = 50, groups = { OperatorValidationManual.class })
	@Letters(extraChars = " .-'", groups = { OperatorValidationManual.class })
	public String getMiddleName() {
		return middleName;
	}

	@NotEmpty(message = "{@validation.name.last.notEmpty}", groups = { OperatorValidationManual.class })
	@Size(max = 50, groups = { OperatorValidationManual.class })
	@Letters(extraChars = " .-'", groups = { OperatorValidationManual.class })
	@Column(name = "last_name", nullable = false, length = 50)
	public String getLastName() {
		return lastName;
	}

	@Length(min = 0, max = 50, message = "{@validation.streetAddress.length}", groups = { OperatorValidationManual.class })
	@LettersOrDigits(groups = { OperatorValidationManual.class })
	@Column(name = "address_line1", length = 50)
	public String getAddressLine1() {
		return addressLine1;
	}

	@Length(min = 0, max = 50, message = "{@validation.streetAddress.length}", groups = { OperatorValidationManual.class })
	@LettersOrDigits(groups = { OperatorValidationManual.class })
	@Column(name = "address_line2", length = 50)
	public String getAddressLine2() {
		return addressLine2;
	}

	@Length(min = 0, max = 50, message = "{@validation.streetAddress.length}", groups = { OperatorValidationManual.class })
	@LettersOrDigits(groups = { OperatorValidationManual.class })
	@Column(name = "address_line3", length = 50)
	public String getAddressLine3() {
		return addressLine3;
	}

	@Pattern(regexp = "([0-9]{4})?", message = "{@validation.postalCode.regex}", groups = { OperatorValidationManual.class })
	@Column(name = "postal_code", length = 4)
	public String getPostalCode() {
		return postalCode;
	}

	@Letters(groups = { OperatorValidationManual.class })
	@Column(name = "post_town", length = 50)
	@Size(max = 50, groups = { OperatorValidationManual.class })
	public String getPostTown() {
		return postTown;
	}

	@Email(message = "{@validation.email}", groups = { OperatorValidationManual.class })
	@Column(name = "email", length = 129)
	public String getEmail() {
		return email;
	}

	@Length(min = 0, max = 14, message = "{@validation.tlf.length}", groups = { OperatorValidationManual.class })
	@Pattern(regexp = "\\+?([0-9]{3,14})?", message = "{@validation.tlf.regex}", groups = { OperatorValidationManual.class })
	@Column(name = "telephone_number", length = 35)
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	@Column(name = "info_text", length = 150)
	@Size(max = 150, groups = { OperatorValidationManual.class })
	public String getInfoText() {
		return infoText;
	}

	@Column(name = "active", nullable = false)
	public boolean isActive() {
		return active;
	}

	@Column(name = "contact_info_confirmed", nullable = false)
	public boolean isContactInfoConfirmed() {
		return contactInfoConfirmed;
	}

	@Column(name = "key_serial_number", length = 19)
	@Length(max = 19)
	@Pattern(regexp = REGEX_KEY_SERIAL_NUMBER)
	public String getKeySerialNumber() {
		return keySerialNumber;
	}

	public void updateContactInfo(ContactInfo contactInfo) {
		setContactInfoConfirmed(true);
		setTelephoneNumber(contactInfo.getPhone());
		setEmail(contactInfo.getEmail());
	}

	@Transient
	public String getFullName() {
		return StringUtil.joinOnlyNonNullAndNonEmpty(firstName, middleName, lastName);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Operator) {
			if (getElectionEvent() != null) {
				if (getElectionEvent().getPk() != null) {
					return (EqualsHashCodeUtil.genericEquals(obj, this) && getElectionEvent().getPk().equals(((Operator) obj).getElectionEvent().getPk()));
				} else {
					return (EqualsHashCodeUtil.genericEquals(obj, this) && (EqualsHashCodeUtil
							.genericEquals(((Operator) obj).getElectionEvent(), getElectionEvent())));
				}
			}
			return EqualsHashCodeUtil.genericEquals(obj, this);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (getElectionEvent() != null && getElectionEvent().getPk() != null) {
			return EqualsHashCodeUtil.genericHashCode(this) + getElectionEvent().getPk().hashCode();
		}
		return EqualsHashCodeUtil.genericHashCode(this);

	}
}
