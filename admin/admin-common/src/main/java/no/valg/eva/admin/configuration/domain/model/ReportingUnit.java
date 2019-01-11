package no.valg.eva.admin.configuration.domain.model;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurableDynamicArea;
import no.evote.security.ContextSecurableDynamicElection;
import no.evote.validation.Letters;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import org.hibernate.validator.constraints.Email;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Election board, with area(s) of authority specified by links into the election and area hierarchies. E.g. an election board for
 * "Kommunestyre- og fylkestingsvalg" at the municipality level ("Valgstyre") is linked to the election group representing both elections, and to the
 * municipality. In this case, counts for both elections may be performed by the same election board
 */
// @formatter:off
@Entity
@Table(name = "reporting_unit", uniqueConstraints = @UniqueConstraint(columnNames = { "mv_election_pk", "mv_area_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "reporting_unit_pk"))

// @formatter:off
@NamedNativeQueries({
		@NamedNativeQuery(name = "ReportingUnit.findAllForElectionEvent", query = "SELECT ru.* FROM reporting_unit ru "
				+ "JOIN mv_area mva ON ru.mv_area_pk = mva.mv_area_pk "
				+ "JOIN mv_election mve ON ru.mv_election_pk = mve.mv_election_pk "
				+ "WHERE mve.election_event_pk = ?1 "
				+ "ORDER BY mve.election_path, mva.area_path", resultClass = ReportingUnit.class),
		@NamedNativeQuery(name = "ReportingUnit.findClosestReportingUnitForElection", query = "select * from "
				+ "( select ru.*, dense_rank() over (order by a.area_level) as level "
				+ "from contest_rel_area ca "
				+ "join mv_election e on (text2ltree(e.election_path) @> text2ltree(ca.election_path)) "
				+ "join mv_area a on (text2ltree(a.area_path) <@ text2ltree(ca.area_path)) "
				+ "join reporting_unit ru on (ru.mv_election_pk = e.mv_election_pk and ru.mv_area_pk = a.mv_area_pk) "
				+ "where ca.mv_election_pk = ?1 and ca.mv_area_pk = ?2) as rus "
				+ "where level = 2;", resultClass = ReportingUnit.class),

		@NamedNativeQuery(name = "ReportingUnit.findReportingUnit", query = "SELECT ru.* FROM contest_rel_area ca JOIN mv_election e "
				+ "ON (text2ltree(e.election_path) @> text2ltree(ca.election_path)) JOIN reporting_unit ru ON (ru.mv_election_pk = e.mv_election_pk "
				+ "AND ru.mv_area_pk = ca.mv_area_pk) WHERE ca.election_path  = ?1 AND ca.area_path = ?2", resultClass = ReportingUnit.class)
})
// @formatter:on
@NamedQueries({
		@NamedQuery(name = "ReportingUnit.findByMvElectionMvArea", query = "SELECT ru FROM ReportingUnit ru WHERE ru.mvElection.pk = :mvElectionPk "
				+ "AND ru.mvArea.pk = :mvAreaPk"),
		@NamedQuery(name = "ReportingUnit.byAreaAndType", query = "SELECT ru FROM ReportingUnit ru WHERE ru.mvArea.areaPath = :areaPath "
				+ "AND ru.reportingUnitType.id = :typeId"),
		@NamedQuery(name = "ReportingUnit.byAreaElectionAndType", query = "SELECT ru FROM ReportingUnit ru WHERE ru.mvArea.areaPath = :areaPath "
				+ "AND ru.mvElection.electionPath = :electionPath AND ru.reportingUnitType.id = :typeId"),
		@NamedQuery(
			name = "ReportingUnit.findAlleValgstyrerIValghendelse",
			query = "SELECT ru FROM ReportingUnit ru "
				+ " WHERE ru.mvArea.electionEvent.pk = :electionEventPk"
				+ "   AND ru.mvArea.areaLevel = 3"),
		@NamedQuery(name = "ReportingUnit.finnAlleFylkesvalgstyrerForValghendelse",
				query = "SELECT ru FROM ReportingUnit ru "
						+ " WHERE ru.mvArea.electionEvent.id = :electionEventId"
						+ "   AND ru.mvArea.areaLevel = 2"),
		@NamedQuery(
				name = "ReportingUnit.findCountElectoralBoardByContest",
				query = "SELECT ru FROM ReportingUnit ru WHERE ru.mvElection.contest = :contest AND ru.reportingUnitType.id = 1"),
		@NamedQuery(
			name = "ReportingUnit.findOpptellingsvalgstyrer",
			query = "SELECT ru FROM ReportingUnit ru "
				+ " WHERE ru.reportingUnitType.id = 1 " // Opptellingsvalgstyre
				+ "   AND ru.mvElection.electionEvent.pk = :electionEventPk ")
})
// @formatter:on
public class ReportingUnit extends VersionedEntity implements java.io.Serializable, ContextSecurableDynamicArea, ContextSecurableDynamicElection {

	private MvArea mvArea;
	private ReportingUnitType reportingUnitType;
	private MvElection mvElection;
	private String nameLine;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String postalCode;
	private String postTown;
	private String email;
	private String telephoneNumber;

	public ReportingUnit() {
		super();
	}

	public ReportingUnit(final ReportingUnit reportingUnit, final MvArea mvArea, final MvElection mvElection) {
		super();
		this.mvArea = mvArea;
		this.mvElection = mvElection;

		nameLine = reportingUnit.getNameLine();
		addressLine1 = reportingUnit.getAddressLine1();
		addressLine2 = reportingUnit.getAddressLine2();
		addressLine3 = reportingUnit.getAddressLine3();
		postalCode = reportingUnit.getPostalCode();
		postTown = reportingUnit.getPostTown();
		email = reportingUnit.getEmail();
		telephoneNumber = reportingUnit.getTelephoneNumber();
		reportingUnitType = reportingUnit.getReportingUnitType();
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mv_area_pk", nullable = false)
	@NotNull
	public MvArea getMvArea() {
		return mvArea;
	}

	public void setMvArea(final MvArea mvArea) {
		this.mvArea = mvArea;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "reporting_unit_type_pk", nullable = false)
	public ReportingUnitType getReportingUnitType() {
		return reportingUnitType;
	}

	public void setReportingUnitType(final ReportingUnitType reportingUnitType) {
		this.reportingUnitType = reportingUnitType;
	}

	public ReportingUnitTypeId reportingUnitTypeId() {
		return reportingUnitType.reportingUnitTypeId();
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mv_election_pk", nullable = false)
	@NotNull
	public MvElection getMvElection() {
		return mvElection;
	}

	public void setMvElection(final MvElection mvElection) {
		this.mvElection = mvElection;
	}

	@Column(name = "name_line", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getNameLine() {
		return nameLine;
	}

	public void setNameLine(final String nameLine) {
		this.nameLine = nameLine;
	}

	@Column(name = "address_line1", length = 50)
	@LettersOrDigits
	@Size(max = 50)
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(final String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "address_line2", length = 50)
	@LettersOrDigits
	@Size(max = 50)
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(final String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(name = "address_line3", length = 50)
	@LettersOrDigits
	@Size(max = 50)
	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(final String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	@Column(name = "postal_code", length = 4)
	@Pattern(regexp = "([0-9]{4})?", message = "{@validation.postalCode.regex}")
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	@Letters
	@Column(name = "post_town", length = 50)
	@Size(max = 50)
	public String getPostTown() {
		return postTown;
	}

	public void setPostTown(final String postTown) {
		this.postTown = postTown;
	}

	@Column(name = "email", length = 129)
	@Email
	@Size(max = 129)
	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	@Column(name = "telephone_number", length = 35)
	@Pattern(regexp = "\\+?([0-9]{3,34})?", message = "{@validation.tlf.regex}")
	@Size(max = 35)
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(final String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	@Override
	public String toString() {
		return nameLine + "#" + getPk();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPk() == null) ? 0 : getPk().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReportingUnit other = (ReportingUnit) obj;
		if (getPk() == null) {
			return other.getPk() == null;
		} else {
			return getPk().equals(other.getPk());
		}
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.ROOT)) {
			return mvElection.getElectionEvent().getPk();
		}
		if (level.equals(AreaLevelEnum.COUNTRY)) {
			return mvArea.getCountry().getPk();
		}
		if (level.equals(AreaLevelEnum.COUNTY)) {
			return mvArea.getCounty().getPk();
		}
		if (level.equals(AreaLevelEnum.MUNICIPALITY)) {
			return mvArea.getMunicipality().getPk();
		}
		if (level.equals(AreaLevelEnum.BOROUGH)) {
			return mvArea.getBorough().getPk();
		}
		if (level.equals(AreaLevelEnum.POLLING_DISTRICT)) {
			return mvArea.getPollingDistrict().getPk();
		}

		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_EVENT)) {
			return mvElection.getElectionEvent().getPk();
		}
		if (level.equals(ElectionLevelEnum.ELECTION_GROUP)) {
			return mvElection.getElectionGroup().getPk();
		}
		if (level.equals(ElectionLevelEnum.ELECTION)) {
			return mvElection.getElection().getPk();
		}
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return mvElection.getContest().getPk();
		}

		return null;
	}

	@Override
	@Transient
	public AreaLevelEnum getActualAreaLevel() {
		return AreaLevelEnum.getLevel(mvArea.getAreaLevel());
	}

	@Override
	@Transient
	public ElectionLevelEnum getActualElectionLevel() {
		return ElectionLevelEnum.getLevel(mvElection.getElectionLevel());
	}

	@Transient
	public boolean isStemmestyret() {
		return reportingUnitTypeId() == ReportingUnitTypeId.STEMMESTYRET;
	}
}
