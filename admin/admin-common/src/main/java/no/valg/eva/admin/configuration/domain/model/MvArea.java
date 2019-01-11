package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.evote.constants.SQLConstants;
import no.evote.exception.ValidateException;
import no.evote.model.BaseEntity;
import no.evote.security.ContextSecurableDynamicArea;
import no.evote.util.Treeable;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;
import no.valg.eva.admin.configuration.domain.model.valgnatt.IntegerWrapper;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import org.hibernate.annotations.Cache;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
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
import javax.persistence.QueryHint;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static java.lang.String.format;
import static no.evote.constants.EvoteConstants.BALLOT_STATUS_APPROVED;
import static no.evote.constants.EvoteConstants.BALLOT_STATUS_PENDING;
import static no.evote.constants.EvoteConstants.BALLOT_STATUS_REJECTED;
import static no.evote.constants.EvoteConstants.BALLOT_STATUS_UNDERCONSTRUCTION;
import static no.evote.constants.EvoteConstants.BALLOT_STATUS_WITHDRAWN;
import static no.valg.eva.admin.common.AreaPath.MUNICIPALITY_POLLING_DISTRICT_ID;
import static org.hibernate.annotations.CacheConcurrencyStrategy.NONSTRICT_READ_WRITE;

/**
 * Materialized view containing all nodes in the hierarchy formed by the tables country, county, municipality, borough, polling_district, and polling_place,
 * facilitating RBAC access to any level in the hierarchy via a single field/pointer
 */

@Entity
@Cacheable
@Cache(usage = NONSTRICT_READ_WRITE, region = "no.valg.eva.admin.configuration.domain.model.MvArea")
@Table(
        name = "mv_area",
        uniqueConstraints = {@UniqueConstraint(columnNames = {
                "election_event_pk", "country_pk", "county_pk", "municipality_pk", "borough_pk", "polling_district_pk", "polling_place_pk"})})
@AttributeOverride(name = "pk", column = @Column(name = "mv_area_pk"))
@NamedQueries({
        @NamedQuery(
                name = "MvArea.findRoot",
                query = "SELECT mva "
                        + "FROM MvArea mva "
                        + "WHERE mva.country IS NULL AND mva.electionEvent.pk = :eepk"),
        @NamedQuery(
                name = "MvArea.findSingleByPath",
                query = "SELECT mva "
                        + "FROM MvArea mva "
                        + "WHERE mva.areaPath = :path",
                hints = {@QueryHint(name = "org.hibernate.cacheable", value = "true")}),
        @NamedQuery(
                name = "MvArea.findSingleByElectionAndPath",
                query = "SELECT mva "
                        + "FROM MvArea mva "
                        + "WHERE mva.areaPath = :path AND mva.electionEventId = :electionEventId",
                hints = {@QueryHint(name = "org.hibernate.cacheable", value = "true")}),
        @NamedQuery(
                name = "MvArea.findByPollingDistrict",
                query = "SELECT mva.pk "
                        + "FROM MvArea mva "
                        + "WHERE mva.areaLevel = 5 AND mva.pollingDistrict.pk = :pollingDistrictPk"),
        @NamedQuery(
                name = "MvArea.findByPollingDistrictIdAndMunicipalityPk",
                query = "SELECT mva "
                        + "FROM MvArea mva "
                        + "WHERE mva.areaLevel = 5 AND mva.pollingDistrict.id = :pollingDistrictId AND mva.municipality.pk = :municipalityPk"),
        @NamedQuery(
                name = "MvArea.findByPollingPlaceIdAndMunicipalityPk",
                query = "SELECT mva "
                        + "FROM MvArea mva "
                        + "WHERE mva.areaLevel = 6 AND mva.pollingDistrict.id = '0000' "
                        + "AND mva.pollingPlace.id = :pollingPlaceId AND mva.municipality.pk = :municipalityPk")})
@NamedNativeQueries(value = {
        @NamedNativeQuery(
                name = "MvArea.findByPathAndLevel",
                query = "SELECT * "
                        + "FROM mv_area mva "
                        + "WHERE text2ltree(mva.area_path) <@ text2ltree(?1) AND mva.area_level = ?2 "
                        + "ORDER BY mva.area_path",
                resultClass = MvArea.class),
        @NamedNativeQuery(
                name = "MvArea.findFirstByPathAndLevel",
                query = "SELECT * "
                        + "FROM mv_area mva "
                        + "WHERE text2ltree(mva.area_path) <@ text2ltree(?1) AND mva.area_level = ?2 "
                        + "ORDER BY mva.area_path LIMIT 1",
                resultClass = MvArea.class),
        @NamedNativeQuery(
                name = "MvArea.hasAccessToPkOnLevel",
                query = "SELECT ra.* "
                        + "FROM mv_area ra "
                        + "JOIN mv_area oa ON (text2ltree(oa.area_path) <@ text2ltree(ra.area_path) AND oa.area_level = ?1 "
                        + "AND CASE WHEN ?1 = 0 THEN oa.election_event_pk WHEN ?1 = 1 THEN oa.country_pk WHEN ?1 = 2 THEN oa.county_pk "
                        + "WHEN ?1 = 3 THEN oa.municipality_pk WHEN ?1 = 4 THEN oa.borough_pk WHEN ?1 = 5 THEN oa.polling_district_pk "
                        + "WHEN ?1 = 6 THEN oa.polling_place_pk WHEN ?1 = 7 THEN oa.polling_station_pk END = ?2) "
                        + "WHERE ra.area_path = ?3",
                resultClass = MvArea.class),
        @NamedNativeQuery(
                name = "MvArea.findContestsByElectionWhereAllBallotsAreProcessed",
                query = "SELECT DISTINCT c.* "
                        + "FROM Mv_Area mva, contest_area ca, contest c "
                        + "WHERE mva.mv_area_pk = ca.mv_area_pk AND ca.contest_pk = c.contest_pk "
                        + "AND c.election_pk = :electionPk "
                        + "AND NOT EXISTS (SELECT * "
                        + "                FROM ballot b, ballot_status bs "
                        + "                WHERE b.contest_pk = c.contest_pk AND b.ballot_status_pk = bs.ballot_status_pk "
                        + "                AND bs.ballot_status_id IN (" + BALLOT_STATUS_PENDING + ", " + BALLOT_STATUS_UNDERCONSTRUCTION + ")) "
                        + "AND EXISTS (SELECT * "
                        + "            FROM ballot b, ballot_status bs "
                        + "            WHERE b.contest_pk = c.contest_pk AND b.ballot_status_pk = bs.ballot_status_pk "
                        + "            AND bs.ballot_status_id IN (" + BALLOT_STATUS_APPROVED + ", " + BALLOT_STATUS_WITHDRAWN
                        + ", " + BALLOT_STATUS_REJECTED + "))",
                resultClass = Contest.class),
        @NamedNativeQuery(
                name = "MvArea.reportConfigurationQuery",
                query = "SELECT "
                        + "   a.polling_district_pk, "
                        + "   a.polling_district_id, "
                        + "   a.polling_district_name, "
                        + "   a.municipality_id, "
                        + "   a.municipality_name, "
                        + "   pd.parent_polling_district, "
                        + "   (NOT rcc.polling_district_count AND vcc.vote_count_category_id = 'VO') AS municipality, "
                        + "   cast(CASE WHEN c.area_level = 2 "
                        + "		THEN ac.county_id "
                        + "		  ELSE (CASE WHEN caa.parent_area IS TRUE "
                        + "			THEN aac.municipality_id "
                        + "				ELSE ac.county_id END) END AS TEXT) AS county_id, "
                        + "   cast(CASE WHEN c.area_level = 2 "
                        + "   	THEN ac.county_name "
                        + "       ELSE (CASE WHEN caa.parent_area IS TRUE "
                        + "         THEN aac.municipality_name "
                        + "             ELSE ac.county_name END) END AS TEXT) AS county_name, "
                        + "   a.borough_id, "
                        + "   a.borough_name, "
                        + "   cast(CASE WHEN c.area_level = 2 "
                        + "		THEN ac.county_id "
                        + "		  ELSE (CASE WHEN caa.parent_area IS TRUE "
                        + "			THEN aac.municipality_id "
                        + "				ELSE ac.municipality_id END) END AS TEXT) AS valgdistrikt_id, "
                        + "   cast(CASE WHEN c.area_level = 2 "
                        + "   	THEN ac.county_name "
                        + "       ELSE (CASE WHEN caa.parent_area IS TRUE "
                        + "         THEN aac.municipality_name "
                        + "             ELSE ac.municipality_name END) END AS TEXT) AS valgdistrikt_name, "
                        + "   a.mv_area_pk, "
                        + "   a.municipality_pk, "
                        + "   c.contest_pk, "
                        + "   c.election_pk "
                        + "FROM mv_election c "
                        + "  JOIN contest_area ca ON ca.contest_pk = c.contest_pk"
                        + "  JOIN mv_area ac ON ac.mv_area_pk = ca.mv_area_pk"
                        + "  JOIN mv_area a ON text2ltree(a.area_path) <@ text2ltree(ac.area_path)"
                        + "  JOIN polling_district pd ON pd.polling_district_pk = a.polling_district_pk  "
                        + "  JOIN report_count_category rcc ON rcc.municipality_pk = a.municipality_pk  "
                        + "  JOIN vote_count_category vcc ON vcc.vote_count_category_pk = rcc.vote_count_category_pk "
                        + "  LEFT JOIN contest_area caa ON caa.contest_pk = c.contest_pk AND caa.parent_area IS TRUE "
                        + "  LEFT JOIN mv_area aac ON aac.mv_area_pk = caa.mv_area_pk "
                        + "WHERE c.election_level = 3 AND c.election_pk = ?1 "
                        + "  AND a.area_level = 5 "
                        + "	 AND (vcc.vote_count_category_id  = 'VO' OR (vcc.vote_count_category_id = 'FO' AND ac.county_id = '00'))"
                        + "  AND (rcc.polling_district_count IS TRUE OR a.polling_district_id = '0000') "
                        + "  AND pd.parent_polling_district_pk IS NULL AND pd.technical_polling_district IS FALSE "
                        + "  AND (c.area_level = ?2 OR a.municipality_id = '0301')"
                        + "  AND (?3 = 0 OR a.municipality_pk = ?3) "
                        + "ORDER BY a.municipality_id, a.polling_district_id",
                resultSetMapping = "ReportConfigurationForPollingDistrict"),
        @NamedNativeQuery(
                name = "MvArea.electoralRollForPollingDistrictQuery",
                query = "SELECT mva.municipality_id, "
                        + "   mva.municipality_name, "
                        + "   mva.county_name, "
                        + "   mva.county_id, "
                        + "   mva.polling_district_id, "
                        + "   mva.polling_district_name, "
                        + "   sum(1) AS voter_total, "
                        + "   pd.parent_polling_district, "
                        + "   pd.polling_district_pk, "
                        + "   mva.borough_id, "
                        + "   mva.borough_name, "
                        + "   cast('' AS TEXT) AS valgdistrikt_id, "
                        + "   cast('' AS TEXT) AS valgdistrikt_name,  "
                        + "   cast (0 AS INTEGER) AS contest_pk "
                        + "FROM mv_area mva "
                        + "JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk "
                        + "JOIN voter v "
                        + "  ON (v.mv_area_pk = mva.mv_area_pk "
                        + "      AND v.election_event_pk = mva.election_event_pk "
                        + "      AND v.eligible IS TRUE "
                        + "      AND (NOT v.fictitious OR v.fictitious AND EXISTS (SELECT voting_pk FROM voting "
                        + "WHERE voting.voter_pk = v.voter_pk AND voting.approved))) "
                        + "WHERE mva.country_id = '47' "
                        + "  AND mva.election_event_id = ?1 "
                        + "  AND mva.area_level = 5 "
                        + "  AND exists (SELECT 1 FROM eligibility e WHERE e.mv_area_pk = mva.mv_area_pk AND e.end_date_of_birth >= v.date_of_birth) "
                        + "GROUP BY mva.municipality_id, mva.municipality_name, mva.county_name, mva.county_id, "
                        + "mva.polling_district_id, mva.polling_district_name, pd.parent_polling_district, pd.parent_polling_district_pk, "
                        + "pd.polling_district_pk, mva.borough_id, mva.borough_name "
                        + "ORDER BY mva.municipality_id, mva.polling_district_id ",
                resultSetMapping = "ElectoralRollForPollingDistrict"),
        @NamedNativeQuery(
                name = "MvArea.pollingDistrictPkListForContestAreaChild",
                query = "SELECT p.polling_district_pk "
                        + "FROM contest_area ca "
                        + "  JOIN mv_area mva ON ca.mv_area_pk = mva.mv_area_pk "
                        + "  JOIN (SELECT municipality_pk, polling_district_pk FROM mv_area WHERE election_event_pk = ?1 AND area_level = 5) p ON p.municipality_pk = " +
                        "mva.municipality_pk "
                        + "WHERE ca.child_area = TRUE AND mva.municipality_pk IS NOT NULL AND ca.contest_pk = ?2",
                resultSetMapping = "PollingdistrictPk"),
        @NamedNativeQuery(
                name = "MvArea.findParentAreaByPk",
                query = "select parent.* " +
                        "from mv_area mva " +
                        "join mv_area parent on (text2ltree(parent.area_path) @> text2ltree(mva.area_path) AND parent.area_level = mva.area_level - 1) " +
                        "where mva.mv_area_pk = :areaPk",
                resultClass = MvArea.class)
})
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "ReportConfigurationForPollingDistrict",
                classes = {
                        @ConstructorResult(
                                targetClass = ReportConfiguration.class,
                                columns = {
                                        @ColumnResult(name = "polling_district_pk"),
                                        @ColumnResult(name = "polling_district_id"),
                                        @ColumnResult(name = "polling_district_name"),
                                        @ColumnResult(name = "municipality_id"),
                                        @ColumnResult(name = "municipality_name"),
                                        @ColumnResult(name = "parent_polling_district"),
                                        @ColumnResult(name = "municipality"),
                                        @ColumnResult(name = "county_id"),
                                        @ColumnResult(name = "county_name"),
                                        @ColumnResult(name = "borough_id"),
                                        @ColumnResult(name = "borough_name"),
                                        @ColumnResult(name = "valgdistrikt_id"),
                                        @ColumnResult(name = "valgdistrikt_name"),
                                        @ColumnResult(name = "mv_area_pk"),
                                        @ColumnResult(name = "contest_pk")
                                })
                }),
        @SqlResultSetMapping(
                name = "ElectoralRollForPollingDistrict",
                classes = {
                        @ConstructorResult(
                                targetClass = ElectoralRollCount.class,
                                columns = {
                                        @ColumnResult(name = "municipality_id"),
                                        @ColumnResult(name = "municipality_name"),
                                        @ColumnResult(name = "county_name"),
                                        @ColumnResult(name = "county_id"),
                                        @ColumnResult(name = "polling_district_id"),
                                        @ColumnResult(name = "polling_district_name"),
                                        @ColumnResult(name = "voter_total"),
                                        @ColumnResult(name = "parent_polling_district"),
                                        @ColumnResult(name = "polling_district_pk"),
                                        @ColumnResult(name = "borough_id"),
                                        @ColumnResult(name = "borough_name"),
                                        @ColumnResult(name = "valgdistrikt_id"),
                                        @ColumnResult(name = "valgdistrikt_name"),
                                        @ColumnResult(name = "contest_pk")
                                })
                }),
        @SqlResultSetMapping(
                name = "PollingdistrictPk",
                classes = {
                        @ConstructorResult(
                                targetClass = IntegerWrapper.class,
                                columns = {
                                        @ColumnResult(name = "polling_district_pk"),
                                })
                })
})

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MvArea extends BaseEntity implements Serializable, Treeable, ContextSecurableDynamicArea {
    private static final long serialVersionUID = 4747543654495970865L;
    private static final String ROOT = "Root";

    @Setter private PollingDistrict pollingDistrictByParentPollingDistrictPk;
	@Setter private PollingDistrict pollingDistrict;
	@Setter private ElectionEvent electionEvent;
	@Setter private Country country;
	@Setter private County county;
	@Setter private Municipality municipality;
	@Setter private Borough borough;
	@Setter private PollingPlace pollingPlace;
	@Setter private String areaPath;
	@Setter private int areaLevel;
	@Setter private String electionEventId;
	@Setter private String countryId;
	@Setter private String countyId;
	@Setter private String municipalityId;
	@Setter private String boroughId;
	@Setter private String pollingDistrictId;
	@Setter private String pollingPlaceId;
	@Setter private String electionEventName;
	@Setter private String countryName;
	@Setter private String countyName;
	@Setter private String municipalityName;
	@Setter private String boroughName;
	@Setter private String pollingDistrictName;
	@Setter private String pollingPlaceName;
	@Setter private Boolean parentPollingDistrict;

    @Transient
    public Borough getMunicipalityBorough() {
        return getMunicipality().getMunicipalityBorough();
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "municipality_pk")
    public Municipality getMunicipality() {
        return this.municipality;
    }

    @Transient
    public PollingDistrict getMunicipalityPollingDistrict() {
        return getMunicipality().getMunicipalityPollingDistrict();
    }

    @Column(name = "area_level", nullable = false)
    public int getAreaLevel() {
        return this.areaLevel;
    }

    @Column(name = "election_event_id", nullable = false, length = 6)
    @StringNotNullEmptyOrBlanks
    @Size(max = 6)
    public String getElectionEventId() {
        return this.electionEventId;
    }

    @Column(name = "country_id", length = 2)
    public String getCountryId() {
        return this.countryId;
    }

    @Column(name = "county_id", length = 2)
    public String getCountyId() {
        return this.countyId;
    }

    @Column(name = "municipality_id", length = 4)
    public String getMunicipalityId() {
        return this.municipalityId;
    }

    @Column(name = "borough_id", length = 6)
    public String getBoroughId() {
        return this.boroughId;
    }

    @Column(name = "polling_district_id", length = 4)
    public String getPollingDistrictId() {
        return this.pollingDistrictId;
    }

    @Column(name = "polling_place_id", length = 4)
    public String getPollingPlaceId() {
        return this.pollingPlaceId;
    }

    @Column(name = "election_event_name", nullable = false, length = 100)
    @StringNotNullEmptyOrBlanks
    @Size(max = 50)
    public String getElectionEventName() {
        return this.electionEventName;
    }

    @Column(name = "country_name", length = 50)
    @Size(max = 50)
    public String getCountryName() {
        return this.countryName;
    }

    @Column(name = "county_name", length = 50)
    @Size(max = 50)
    public String getCountyName() {
        return this.countyName;
    }

    @Column(name = "municipality_name", length = 50)
    @Size(max = 50)
    public String getMunicipalityName() {
        return this.municipalityName;
    }

    @Column(name = "borough_name", length = 50)
    @Size(max = 50)
    public String getBoroughName() {
        return this.boroughName;
    }

    @Column(name = "polling_district_name", length = 50)
    @Size(max = 50)
    public String getPollingDistrictName() {
        return this.pollingDistrictName;
    }

    @Column(name = "polling_place_name", length = 50)
    @Size(max = 50)
    public String getPollingPlaceName() {
        return this.pollingPlaceName;
    }

    @Transient
    public boolean erForelderstemmekrets() {
        return getParentPollingDistrict() != null && getParentPollingDistrict();
    }

    @Column(name = "parent_polling_district")
    public Boolean getParentPollingDistrict() {
        return this.parentPollingDistrict;
    }

    @Transient
    public boolean isChildPollingDistrict() {
        return getPollingDistrictByParentPollingDistrictPk() != null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_polling_district_pk")
    public PollingDistrict getPollingDistrictByParentPollingDistrictPk() {
        return this.pollingDistrictByParentPollingDistrictPk;
    }

    @Override
    @Transient
    public String getPath() {
        return areaPath;
    }

    @Transient
    public boolean isRoot() {
        return areaLevel == AreaLevelEnum.ROOT.getLevel();
    }

    @Transient
    public boolean isCountyLevel() {
        return areaLevel == AreaLevelEnum.COUNTY.getLevel();
    }

    @Transient
    public boolean isMunicipalityLevel() {
        return areaLevel == AreaLevelEnum.MUNICIPALITY.getLevel();
    }

    @Transient
    public boolean isBoroughLevel() {
        return areaLevel == AreaLevelEnum.BOROUGH.getLevel();
    }

    @Transient
    public boolean isPollingDistrictLevel() {
        return areaLevel == AreaLevelEnum.POLLING_DISTRICT.getLevel();
    }

    @Transient
    public boolean isPollingPlaceLevel() {
        return areaLevel == AreaLevelEnum.POLLING_PLACE.getLevel();
    }

    @Transient
    public String getAreaLevelString() {
        return "@area_level[" + areaLevel + "].name";
    }

    @Transient
    public boolean isZeroDistrict() {
        return MUNICIPALITY_POLLING_DISTRICT_ID.equals(getPollingDistrictId());
    }

    @Override
    public String toString() {
        return getAreaName();
    }

    @Transient
    public String getAreaName() {
        return getAreaName(getActualAreaLevel());
    }

    @Transient
    public String getAreaName(AreaLevelEnum areaLevelEnum) {
        switch (areaLevelEnum) {
            case COUNTRY:
                return countryName;
            case COUNTY:
                return countyName;
            case MUNICIPALITY:
                return municipalityName;
            case BOROUGH:
                return boroughName;
            case POLLING_DISTRICT:
                return pollingDistrictName;
            case POLLING_PLACE:
                return pollingPlaceName;
            default:
                return electionEventName;
        }
    }

    @Override
    @Transient
    public AreaLevelEnum getActualAreaLevel() {
        return AreaLevelEnum.getLevel(areaLevel);
    }

    @Transient
    public String getAreaId() {
        switch (getActualAreaLevel()) {
            case COUNTRY:
                return countryId;
            case COUNTY:
                return countyId;
            case MUNICIPALITY:
                return municipalityId;
            case BOROUGH:
                return boroughId;
            case POLLING_DISTRICT:
                return pollingDistrictId;
            case POLLING_PLACE:
                return pollingPlaceId;
            default:
                return electionEventId;
        }
    }

    public String id() {
        AreaLevelEnum areaLevelEnum = AreaLevelEnum.getLevel(areaLevel);
        switch (areaLevelEnum) {
            case COUNTRY:
                return countryId;
            case COUNTY:
                return countyId;
            case MUNICIPALITY:
                return municipalityId;
            case BOROUGH:
                return boroughId;
            case POLLING_DISTRICT:
                return pollingDistrictId;
            case POLLING_PLACE:
                return pollingPlaceId;
            default:
                return ROOT;
        }
    }

    @Transient
    public String getNamedPath() {
        StringBuilder path = new StringBuilder();
        if (country != null) {
            path = new StringBuilder(countryName);
        }
        if (county != null) {
            path = new StringBuilder(countyName);
        }
        if (municipality != null) {
            path.append('.');
            path.append(municipalityName);
        }
        if (borough != null) {
            path.append('.');
            path.append(boroughName);
        }
        if (pollingDistrict != null) {
            path.append('.');
            path.append(pollingDistrictName);
        }
        if (pollingPlace != null) {
            path.append('.');
            path.append(pollingPlaceName);
        }
        return path.toString();
    }

    public Long getPkForLevel(final int level) {
        AreaLevelEnum areaLevelEnum = AreaLevelEnum.getLevel(level);
        switch (areaLevelEnum) {
            case ROOT:
                return electionEventPk();
            case COUNTRY:
                return countryPk();
            case COUNTY:
                return countyPk();
            case MUNICIPALITY:
                return municipalityPk();
            case BOROUGH:
                return boroughPk();
            case POLLING_DISTRICT:
                return pollingDistrictPk();
            case POLLING_PLACE:
                return pollingPalcePk();
            default:
                return null;
        }
    }

    private Long electionEventPk() {
        return electionEvent != null ? electionEvent.getPk() : null;
    }

    private Long countryPk() {
        return country != null ? country.getPk() : null;
    }

    private Long countyPk() {
        return county != null ? county.getPk() : null;
    }

    private Long municipalityPk() {
        return municipality != null ? municipality.getPk() : null;
    }

    private Long boroughPk() {
        return borough != null ? borough.getPk() : null;
    }

    private Long pollingDistrictPk() {
        return pollingDistrict != null ? pollingDistrict.getPk() : null;
    }

    private Long pollingPalcePk() {
        return pollingPlace != null ? pollingPlace.getPk() : null;
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        if (level.equals(AreaLevelEnum.ROOT)) {
            return getElectionEvent().getPk();
        } else if (level.equals(AreaLevelEnum.COUNTRY)) {
            return getCountry().getPk();
        } else if (level.equals(AreaLevelEnum.COUNTY)) {
            return getCounty().getPk();
        } else if (level.equals(AreaLevelEnum.MUNICIPALITY)) {
            return getMunicipality().getPk();
        } else if (level.equals(AreaLevelEnum.BOROUGH)) {
            return getBorough().getPk();
        } else if (level.equals(AreaLevelEnum.POLLING_DISTRICT)) {
            return getPollingDistrict().getPk();
        } else if (level.equals(AreaLevelEnum.POLLING_PLACE)) {
            return getPollingPlace().getPk();
        }
        return null;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "polling_district_pk")
    public PollingDistrict getPollingDistrict() {
        return this.pollingDistrict;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
    public ElectionEvent getElectionEvent() {
        return this.electionEvent;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_pk")
    public Country getCountry() {
        return this.country;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "county_pk")
    public County getCounty() {
        return this.county;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "borough_pk")
    public Borough getBorough() {
        return this.borough;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "polling_place_pk")
    public PollingPlace getPollingPlace() {
        return this.pollingPlace;
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        return null;
    }

    public void validateAreaLevel(final AreaLevelEnum areaLevelEnum) {
        if (getActualAreaLevel() != areaLevelEnum) {
            throw new ValidateException(format(
                    "expected the MvArea area level to be <%s>, but was <%s>",
                    areaLevelEnum,
                    getActualAreaLevel()));
        }
    }

    public PollingPlaceArea toViewObject() {
        AreaPath path = AreaPath.from(areaPath);
        if (pollingDistrict == null) {
            return new PollingPlaceArea(path, getAreaName());
        }
        PollingPlaceType type = path.isMunicipalityPollingDistrict() ? PollingPlaceType.ADVANCE_VOTING : PollingPlaceType.ELECTION_DAY_VOTING;
        return new PollingPlaceArea(AreaPath.from(areaPath), getAreaName(), type);
    }

    public PollingDistrictType pollingDistrictType() {
        return pollingDistrict.type();
    }

    @Transient
    public boolean isNotMunicipalityBorough() {
        return !getBorough().isMunicipality1();
    }

    public ValggeografiSti valggeografiSti() {
        return ValggeografiSti.fra(areaPath());
    }

    public AreaPath areaPath() {
        return AreaPath.from(getAreaPath());
    }

    @Column(name = "area_path", nullable = false, length = 37)
    @StringNotNullEmptyOrBlanks
    @Size(max = 37)
    public String getAreaPath() {
        return this.areaPath;
    }

    @Transient
    public boolean hasMunicipalityPathId(String areaPathId) {
        return municipalityId != null && municipalityId.equals(areaPathId);
    }
}
