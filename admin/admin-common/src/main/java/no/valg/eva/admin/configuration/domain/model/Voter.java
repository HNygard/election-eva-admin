package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.persistence.AntiSamyEntityListener;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import no.valg.eva.admin.util.StringUtil;
import no.evote.validation.AntiSamy;
import no.evote.validation.FoedselsNummer;
import no.evote.validation.Letters;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.evote.validation.ValideringVedManuellRegistrering;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Foedselsnummer;
import no.valg.eva.admin.common.validator.PastLocalDate;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.AttributeOverride;
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
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Voter information
 */
@Entity
@Table(name = "voter", uniqueConstraints = { @UniqueConstraint(columnNames = { "voter_number" }),
		@UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "voter_id" }) })
@AttributeOverride(name = "pk", column = @Column(name = "voter_pk"))
@EntityListeners({ AntiSamyEntityListener.class })
@NamedQueries({
		@NamedQuery(name = "Voter.findById", query = "SELECT v FROM Voter v WHERE v.id = :id AND v.electionEvent.pk = :electionEventPk"),
		@NamedQuery(name = "Voter.findByAreaAndId", query = "SELECT v FROM Voter v WHERE v.id = :id AND v.electionEvent.pk = :electionEventPk "
				+ "AND v.countryId = '47' AND v.countyId = :countyId AND (v.municipalityId = :municipalityId OR '' = :municipalityId)"),
		@NamedQuery(name = "Voter.findByVoterNumber", query = "SELECT v FROM Voter v WHERE v.number = :number AND v.electionEvent.pk = :electionEventPk"),
		@NamedQuery(
				name = "Voter.findVoterByNr",
				query = "SELECT v FROM Voter v WHERE v.dateOfBirth = :dateOfBirth AND v.postalCode = :postalCode AND v.firstName "
						+ "LIKE :fName AND v.lastName LIKE :lName AND v.electionEvent.pk = :electionEventPk"),
		@NamedQuery(name = "Voter.countByMvArea", query = "SELECT COUNT(v) FROM Voter v WHERE v.mvArea.pk = :mvAreaPk"),
		@NamedQuery(name = "Voter.deleteWhereMvAreaIsNull", query = "DELETE From Voter v WHERE v.mvArea is null and v.electionEvent.pk = :electionEventPk"),
		@NamedQuery(name = "Voter.findForValgkortgrunnlag", 
			query = "SELECT v FROM Voter v "
				+ " LEFT JOIN FETCH v.mvArea mva"
				+ " LEFT JOIN FETCH mva.pollingDistrict pd"
				+ " LEFT JOIN FETCH pd.pollingPlaces "
			    + " WHERE v.electionEvent.pk = :valghendelsePk "
				+ " ORDER by v.municipalityId,v.pollingDistrictId,v.nameLine,v.id "),
		@NamedQuery(name = "Voter.findByOmraadesti", query = "SELECT v FROM Voter v WHERE v.mvArea.areaPath = :omraadesti") })
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "Voter.updateLineNumber",
				query = "UPDATE line_number SET last_page = :lastPage, last_line = :lastLine WHERE polling_district_pk = :pollingDistrictPk",
				resultSetMapping = "voidDummy"),
		@NamedNativeQuery(
				name = "Voter.flyttVelgere",
				query = "UPDATE line_number SET last_page = :lastPage, last_line = :lastLine WHERE polling_district_pk = :pollingDistrictPk",
				resultSetMapping = "voidDummy"),
		@NamedNativeQuery(
			name = "Voter.flyttVelgereFraOmraade",
			query = "UPDATE voter"
				+ " SET mv_area_pk = mva.mv_area_pk,"
				+ "     country_id = mva.country_id,"
				+ "     county_id = mva.county_id,"
				+ "     municipality_id = mva.municipality_id,"
				+ "     borough_id = mva.borough_id,"
				+ "     polling_district_id = mva.polling_district_id,"
				+ "     polling_station_pk = NULL"
				+ " FROM mv_area mva"
				+ " WHERE mva.mv_area_pk = ?1"
				+ "   AND voter.mv_area_pk IN ("
				+ "     SELECT DISTINCT mva2.mv_area_pk FROM voter"
				+ "     LEFT JOIN mv_area mva2 USING (mv_area_pk)"
				+ "     WHERE text2ltree(mva2.area_path) <@ text2ltree(?2)"
				+ "   )"),
		@NamedNativeQuery(
				name = "Voter.findByName",
				query = "SELECT * FROM voter v WHERE v.election_event_pk = :electionEventPk "
						+ "AND soundex_tsvector(v.election_event_pk, v.name_line) @@ soundex_tsquery(:electionEventPk ,:nameLine) LIMIT 1000",
				resultClass = Voter.class),
		@NamedNativeQuery(
				name = "Voter.electoralRollByPollingDistrict",
				query = "SELECT * FROM voter v join mv_area a on a.mv_area_pk = v.mv_area_pk "
						+ "WHERE a.polling_district_pk = :pollingDistrictPk "
						+ "AND v.eligible = true "
						+ "AND v.approved = true AND exists ( "
						+ "SELECT 1 FROM Eligibility e WHERE e.mv_Area_Pk = v.mv_Area_pk AND e.end_Date_Of_Birth >= v.date_of_birth) "
						+ "ORDER BY UPPER (v.last_Name), UPPER(v.first_Name), UPPER(v.middle_Name), v.voter_id",
				resultClass = Voter.class) })
@SqlResultSetMappings({ @SqlResultSetMapping(name = "voidDummy") })
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voter extends VersionedEntity implements java.io.Serializable {

	public static final char ENDRINGSTYPE_AVGANG = 'A';
	public static final char ENDRINGSTYPE_ENDRING = 'E';
	public static final char ENDRINGSTYPE_TILGANG = 'T';
	public static final char ENDRINGSTYPE_INITIELL = ' ';

	private MvArea mvArea;
	@Deprecated
	private ElectionEvent electionEvent;
	private String id;
	private LocalDate dateOfBirth;
	private Long number;
	private Integer importBatchNumber;
	private String countryId;
	private String countyId;
	private String municipalityId;
	private String boroughId;
	private String pollingDistrictId;
	private boolean eligible;
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
	private boolean mailingAddressSpecified;
	private String mailingAddressLine1;
	private String mailingAddressLine2;
	private String mailingAddressLine3;
	private String mailingCountryCode;
	private String approvalRequest;
	private boolean approved;
	/**
	 * This field is exceptionally not using JodaTime because microsecond precision is needed. This inconsistency problem will be solved when migrating to Java8
	 */
	private Timestamp dateTimeSubmitted;
	private String aarsakskode;
	private Character endringstype;
	private Character statuskode;
	private LocalDate regDato;
	private Character spesRegType;
	private Integer electoralRollPage;
	private Integer electoralRollLine;
	private boolean votingCardReturned;
	private int temporaryCredentialsCount;
	private DateTime temporaryCredentialsTimestamp;
	private PollingPlace temporaryCredentialsPollingPlace;
	private PollingStation pollingStation;
	@Builder.Default private boolean fictitious = false;

	@AntiSamy
	private String additionalInformation;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mv_area_pk")
	public MvArea getMvArea() {
		return mvArea;
	}

	public void setMvArea(final MvArea mvArea) {
		this.mvArea = mvArea;
	}

	@Transient
	public boolean erTilknyttetStemmekrets() {
		return getMvArea() != null;
	}

	@Transient
	public boolean erTilknyttetKretsenForHeleKommunen() {
		return getMvArea() != null && mvArea.isZeroDistrict();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@Column(name = "voter_id", nullable = false, length = 11)
	@FoedselsNummer (groups = { ValideringVedManuellRegistrering.class })
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	@Column(name = "date_of_birth", length = 13)
	@PastLocalDate(groups = { ValideringVedManuellRegistrering.class })
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setDateOfBirthFromFodselsnummerIfMissing() {
		if (getDateOfBirth() == null) {
			setDateOfBirth(new Foedselsnummer(getId()).dateOfBirth());
		}
	}
	
	@Transient
	public Integer getFodselsaar() {
		return dateOfBirth != null ? dateOfBirth.getYear() : null;
	}

	@Column(name = "voter_number")
	public Long getNumber() {
		return number;
	}
	
	@Transient
	public boolean harManntallsnummer() {
		return number != null;
	}

	public void setNumber(final Long number) {
		this.number = number;
	}

	@Column(name = "import_batch_number")
	public Integer getImportBatchNumber() {
		return importBatchNumber;
	}

	public void setImportBatchNumber(final Integer importBatchNumber) {
		this.importBatchNumber = importBatchNumber;
	}

	@Column(name = "country_id", nullable = false, length = 2)
	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(final String countryId) {
		this.countryId = countryId;
	}

	@Column(name = "county_id", nullable = false, length = 2)
	public String getCountyId() {
		return countyId;
	}

	public void setCountyId(final String countyId) {
		this.countyId = countyId;
	}

	@Column(name = "municipality_id", nullable = false, length = 4)
	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Column(name = "borough_id", nullable = false, length = 6)
	public String getBoroughId() {
		return boroughId;
	}

	public void setBoroughId(final String boroughId) {
		this.boroughId = boroughId;
	}

	@Column(name = "polling_district_id", nullable = false, length = 4)
	public String getPollingDistrictId() {
		return pollingDistrictId;
	}

	public void setPollingDistrictId(final String pollingDistrictId) {
		this.pollingDistrictId = pollingDistrictId;
	}

	@Column(name = "eligible", nullable = false)
	public boolean isEligible() {
		return eligible;
	}

	public void setEligible(final boolean eligible) {
		this.eligible = eligible;
	}

	@Column(name = "name_line", nullable = false, length = 152)
	@Size(max = 152)
	public String getNameLine() {
		return nameLine;
	}

	public void setNameLine(final String nameLine) {
		this.nameLine = nameLine;
	}

	@Column(name = "first_name", nullable = false, length = 50)
	@Letters(extraChars = " .-'", groups = ValideringVedManuellRegistrering.class)
	@StringNotNullEmptyOrBlanks(groups = { ValideringVedManuellRegistrering.class })
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "middle_name", length = 50)
	@Letters(extraChars = " .-'", groups = ValideringVedManuellRegistrering.class)
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(final String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "last_name", nullable = false, length = 50)
	@Letters(extraChars = " .-'", groups = ValideringVedManuellRegistrering.class)
	@StringNotNullEmptyOrBlanks(groups = { ValideringVedManuellRegistrering.class })
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	@Transient
	public String getFullName() {
		return StringUtil.joinOnlyNonNullAndNonEmpty(firstName, middleName, lastName);
	}

	@Column(name = "address_line1", length = 50)
	@LettersOrDigits(groups = ValideringVedManuellRegistrering.class)
	@Length(min = 0, max = 50, message = "{@validation.streetAddress.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(final String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "address_line2", length = 50)
	@LettersOrDigits(groups = ValideringVedManuellRegistrering.class)
	@Length(min = 0, max = 50, message = "{@validation.coAddress.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(final String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(name = "address_line3", length = 50)
	@LettersOrDigits(groups = ValideringVedManuellRegistrering.class)
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(final String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Column(name = "postal_code", length = 4)
	@Pattern(regexp = "([0-9]{4})?", message = "{@validation.postalCode.regex}", groups = { ValideringVedManuellRegistrering.class })
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	@Column(name = "post_town", length = 50)
	@Letters(groups = ValideringVedManuellRegistrering.class)
	@Size(max = 50, groups = { ValideringVedManuellRegistrering.class })
	public String getPostTown() {
		return postTown;
	}

	public void setPostTown(final String postTown) {
		this.postTown = postTown;
	}

	@Column(name = "email", length = 129)
	@Email(message = "{@validation.email}", groups = { ValideringVedManuellRegistrering.class })
	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	@Column(name = "telephone_number", length = 35)
	@Length(min = 0, max = 35, message = "{@validation.tlf.length}", groups = { ValideringVedManuellRegistrering.class })
	@Pattern(regexp = "\\+?([0-9]{3,34})?", message = "{@validation.tlf.regex}", groups = { ValideringVedManuellRegistrering.class })
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(final String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	@Column(name = "mailing_address_specified")
	public boolean isMailingAddressSpecified() {
		return mailingAddressSpecified;
	}

	public void setMailingAddressSpecified(final boolean mailingAddressSpecified) {
		this.mailingAddressSpecified = mailingAddressSpecified;
	}

	@Column(name = "mailing_address_line1", length = 50)
	@Length(min = 0, max = 50, message = "{@validation.addressLine1.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getMailingAddressLine1() {
		return mailingAddressLine1;
	}

	public void setMailingAddressLine1(final String mailingAddressLine1) {
		this.mailingAddressLine1 = mailingAddressLine1;
	}

	@Column(name = "mailing_address_line2", length = 50)
	@Length(min = 0, max = 50, message = "{@validation.addressLine2.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getMailingAddressLine2() {
		return mailingAddressLine2;
	}

	public void setMailingAddressLine2(final String mailingAddressLine2) {
		this.mailingAddressLine2 = mailingAddressLine2;
	}

	@Column(name = "mailing_address_line3", length = 50)
	@Length(min = 0, max = 50, message = "{@validation.addressLine3.length}", groups = { ValideringVedManuellRegistrering.class })
	public String getMailingAddressLine3() {
		return mailingAddressLine3;
	}

	public void setMailingAddressLine3(final String mailingAddressLine3) {
		this.mailingAddressLine3 = mailingAddressLine3;
	}

	@Column(name = "mailing_country_code")
	public String getMailingCountryCode() {
		return mailingCountryCode;
	}

	public void setMailingCountryCode(final String mailingCountryCode) {
		this.mailingCountryCode = mailingCountryCode;
	}

	@Transient
	public boolean erBosattINorge() {
		return !mailingAddressSpecified || "000".equals(mailingCountryCode);
	}

	@Column(name = "approval_request", length = 150)
	@Size(max = 150, groups = { ValideringVedManuellRegistrering.class })
	public String getApprovalRequest() {
		return approvalRequest;
	}

	public void setApprovalRequest(final String approvalRequest) {
		this.approvalRequest = approvalRequest;
	}

	@Column(name = "approved", nullable = false)
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(final boolean approved) {
		this.approved = approved;
	}

	@Column(name = "date_time_submitted", nullable = false, length = 29)
	public Timestamp getDateTimeSubmitted() {
		Timestamp returnDate = null;
		if (dateTimeSubmitted != null) {
			returnDate = new Timestamp(dateTimeSubmitted.getTime());
			returnDate.setNanos(dateTimeSubmitted.getNanos());
		}
		return returnDate;
	}

	public void setDateTimeSubmitted(final Date dateTimeSubmitted) {
		if (dateTimeSubmitted != null) {
			this.dateTimeSubmitted = new Timestamp(dateTimeSubmitted.getTime());
			if (dateTimeSubmitted instanceof Timestamp) {
				this.dateTimeSubmitted.setNanos(((Timestamp) dateTimeSubmitted).getNanos());
			}
		} else {
			this.dateTimeSubmitted = null;
		}
	}

	@Column(name = "aarsakskode", length = 2)
	public String getAarsakskode() {
		return aarsakskode;
	}

	public void setAarsakskode(final String aarsakskode) {
		this.aarsakskode = aarsakskode;
	}

	@Column(name = "endringstype", length = 1)
	public Character getEndringstype() {
		return endringstype;
	}

	public void setEndringstype(final Character endringstype) {
		this.endringstype = endringstype;
	}

	@Column(name = "statuskode", length = 1)
	public Character getStatuskode() {
		return statuskode;
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
		return spesRegType;
	}

	public void setSpesRegType(final Character spesRegType) {
		this.spesRegType = spesRegType;
	}

	@Column(name = "electoral_roll_page")
	public Integer getElectoralRollPage() {
		return electoralRollPage;
	}

	public void setElectoralRollPage(final Integer electoralRollPage) {
		this.electoralRollPage = electoralRollPage;
	}

	@Column(name = "electoral_roll_line")
	public Integer getElectoralRollLine() {
		return electoralRollLine;
	}

	public void setElectoralRollLine(final Integer electoralRollLine) {
		this.electoralRollLine = electoralRollLine;
	}

	@Transient
	public boolean harSideOgLinje() {
		return (electoralRollLine != null && electoralRollLine != 0)
			&& (electoralRollPage != null && electoralRollPage != 0);
	}
	
	@Column(name = "voting_card_returned", nullable = false)
	public boolean isVotingCardReturned() {
		return votingCardReturned;
	}

	public void setVotingCardReturned(final boolean votingCardReturned) {
		this.votingCardReturned = votingCardReturned;
	}

	@Transient
	public String getFormattedDateOfBirth() {
		return DateUtil.getFormattedShortDate(getDateOfBirth());
	}

	@Transient
	public void setFormattedDateOfBirth(final String dateOfBirth) {
		setDateOfBirth(DateUtil.parseLocalDate(dateOfBirth));
	}

	@Transient
	public String getAgeInYears() {
		return DateUtil.getAgeInYears(getDateOfBirth());
	}

	@Column(name = "temporary_credentials_count", nullable = false)
	public int getTemporaryCredentialsCount() {
		return temporaryCredentialsCount;
	}

	public void setTemporaryCredentialsCount(final int temporaryCredentialsCount) {
		this.temporaryCredentialsCount = temporaryCredentialsCount;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "temporary_credentials_timestamp", length = 29)
	public DateTime getTemporaryCredentialsTimestamp() {
		return temporaryCredentialsTimestamp;
	}

	public void setTemporaryCredentialsTimestamp(DateTime temporaryCredentialsTimestamp) {
		this.temporaryCredentialsTimestamp = temporaryCredentialsTimestamp;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "temporary_credentials_polling_place_pk")
	public PollingPlace getTemporaryCredentialsPollingPlace() {
		return temporaryCredentialsPollingPlace;
	}

	public void setTemporaryCredentialsPollingPlace(final PollingPlace temporaryCredentialsPollingPlace) {
		this.temporaryCredentialsPollingPlace = temporaryCredentialsPollingPlace;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "polling_station_pk")
	public PollingStation getPollingStation() {
		return pollingStation;
	}

	public void setPollingStation(final PollingStation pollingStation) {
		this.pollingStation = pollingStation;
	}

	public void updateNameLine() {
		StringBuilder newNameLine = new StringBuilder("");
		if (getFirstName() != null) {
			newNameLine.append(getFirstName());
			newNameLine.append(" ");
		}
		if (getMiddleName() != null) {
			newNameLine.append(getMiddleName());
			newNameLine.append(" ");
		}
		if (getLastName() != null) {
			newNameLine.append(getLastName());
		}
		setNameLine(newNameLine.toString());
	}

	@Column(name = "additional_information", length = 200)
	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(final String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	@Column(name = "fictitious", nullable = false)
	public boolean isFictitious() {
		return fictitious;
	}

	public void setFictitious(final boolean fictitious) {
		this.fictitious = fictitious;
	}

	@Transient
	public AreaPath getElectoralRollAreaPath() {
		if (mvArea == null) {
			return null;
		} else {
			return AreaPath.from(getMvArea().getElectionEventId(), getCountryId(), getCountyId(), getMunicipalityId(), getBoroughId(), getPollingDistrictId());
		}
	}

	@Transient
	public boolean isStemmerettOgsaVedSametingsvalg() {
		return getAdditionalInformation() != null && getAdditionalInformation().contains("@electoralRoll.eligigbleInSamiElection");
	}

	public Character getCorrectEndringsType(final Voter oldVoter) {
		Character endringType = 'E';
		if (!oldVoter.isApproved() && this.isApproved()) {
			endringType = 'T';
		} else if (oldVoter.isApproved() && !this.isApproved()) {
			endringType = 'A';
		}

		return endringType;
	}

	public void oppdaterStemmerett() {
		eligible = approved;
	}

	@Transient
	public boolean isNotInElectoralRollAnymore() {
		return getEndringstype() != null && ENDRINGSTYPE_AVGANG == getEndringstype();
	}
	
	@Transient
	public boolean isInMunicipalityWithElectronicMarkoffs() {
		return getMvArea().getMunicipality().isElectronicMarkoffs();
	}

	@Override
	public int hashCode() {
		return EqualsHashCodeUtil.genericHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsHashCodeUtil.genericEquals(this, obj);
	}

}
