package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.model.VersionedEntity;
import no.evote.persistence.AntiSamyEntityListener;
import no.evote.security.ContextSecurable;
import no.evote.validation.AntiSamy;
import no.evote.validation.ID;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.configuration.application.MunicipalityConfigStatusMapper;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.APPROVED_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;

@Entity
@Table(name = "municipality", uniqueConstraints = @UniqueConstraint(columnNames = {"county_pk", "municipality_id"}))
@AttributeOverride(name = "pk", column = @Column(name = "municipality_pk"))
@EntityListeners({AntiSamyEntityListener.class})
@AntiSamy
@NamedQueries({
        @NamedQuery(name = "Municipality.findByPkWithScanningConfig", query = "select m from Municipality m LEFT JOIN FETCH m.scanningConfig WHERE m.pk = :pk"),
        @NamedQuery(name = "Municipality.findById", query = "select m from Municipality m WHERE m.county.pk = :countyPk AND m.id = :id"),
        @NamedQuery(
                name = "Municipality.findByElectionEventAndId",
                query = "select m from Municipality m WHERE m.county.country.electionEvent.pk = :electionEventPk AND m.id = :id"),
        @NamedQuery(name = "Municipality.findCountByCounty", query = "select COUNT(m) from Municipality m WHERE m.county.pk = :countyPk"),
        @NamedQuery(name = "Municipality.findLocale", query = "select m.locale from Municipality m WHERE m.pk = :municipalityPk"),
        @NamedQuery(name = "Municipality.findByCountry", query = "select m from Municipality m WHERE m.county.country.pk = :countryPk"),
        @NamedQuery(name = "Municipality.findByCounty", query = "select m from Municipality m WHERE m.county.pk = :countyPk"),
        @NamedQuery(name = "Municipality.findByElectionEventAndStatus", query = "select m from Municipality m, County county, Country country "
                + "where m.county.pk = county.pk and county.country.pk = country.pk and m.municipalityStatus.id = :municipalityStatusId and "
                + "country.electionEvent.pk = :electionEventPk"),
        @NamedQuery(name = "Municipality.findByElectionEventWithScanningConfig", query = "select m from Municipality m " +
                "LEFT JOIN FETCH m.scanningConfig sc " +
                "WHERE m.county.country.electionEvent.pk = :electionEventPk AND sc.scanning = true"),
        @NamedQuery(name = "Municipality.findWithoutEncompassingBoroughs", query = "select m from Municipality m, County county, Country country "
                + "where m.county.pk = county.pk and county.country.pk = country.pk and country.electionEvent.pk = :electionEventPk and "
                + "not exists(select 1 from Borough b where b.municipality = m and b.municipality1 = true)"),

        @NamedQuery(name = "Municipality.findWithoutEncompassingPollingDistricts", query = "select m from Municipality m, County county, Country country "
                + "where m.county.pk = county.pk and county.country.pk = country.pk and country.electionEvent.pk = :electionEventPk and "
                + "not exists(select 1 from PollingDistrict pd, Borough b where b = pd.borough and b.municipality = m and pd.municipality = true)"),
        @NamedQuery(name = "Municipality.withoutBoroughsByCountry", query = "SELECT m from Municipality m, County c WHERE m.county.pk = c.pk "
                + "AND c.country.pk = :countryPk AND NOT EXISTS (SELECT b FROM Borough b WHERE b.municipality.pk = m.pk)")})
@NamedNativeQueries({@NamedNativeQuery(
        name = "Municipality.findWihoutConfiguredPollingStations",
        query = "SELECT DISTINCT m.* FROM "
                + "(SELECT * FROM mv_area mva WHERE text2ltree(mva.area_path) <@ text2ltree(:areaPath) AND mva.area_level = 6 ORDER BY mva.area_path) mva " + "JOIN "
                + "(SELECT * FROM  polling_place pp WHERE pp.using_polling_stations = TRUE) pp " + "ON pp.polling_place_pk = mva.polling_place_pk "
                + "JOIN municipality m " + "ON m.municipality_pk = mva.municipality_pk " + "WHERE pp.election_day_voting = TRUE "
                + "AND m.electronic_markoffs = FALSE "
                + "AND NOT EXISTS (SELECT * FROM polling_station WHERE polling_station.polling_place_pk = pp.polling_place_pk)",
        resultClass = Municipality.class)})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Municipality extends VersionedEntity implements Serializable, ContextSecurable {

    static final String SAMI_FICTITIOUS_COUNTY_ID = "00";
    private static final int MAX_NUMBER_OF_OPENING_HOURS = 4;
    private static final long serialVersionUID = 2623966768975063793L;

    @Setter
    private Locale locale;
    @Setter
    private County county;
    @Setter
    private MunicipalityStatus municipalityStatus;
    @Setter
    private MunicipalityLocalConfigStatus localConfigStatus;
    @Setter
    private ScanningConfig scanningConfig;
    @Setter
    private String id;
    @Setter
    private String name;
    @Setter
    private boolean electronicMarkoffs;
    @Setter
    private boolean requiredProtocolCount;
    @Setter
    private boolean technicalPollingDistrictsAllowed;
    @Setter
    private Boolean handleElectionCardCentrally;
    @Setter
    private Set<Borough> boroughs = new HashSet<>();
    @Setter
    private String electionCardText;
    @Setter
    private boolean avkrysningsmanntallKjort;
    @Setter
    private Set<MunicipalityOpeningHour> openingHours = new HashSet<>();

    public Municipality(final String id, final String name, final County county) {
        this.id = id;
        this.name = name;
        this.county = county;
    }

    @Column(name = "electronic_markoffs", nullable = false)
    public boolean isElectronicMarkoffs() {
        return electronicMarkoffs;
    }

    public boolean papirmanntall() {
        return !electronicMarkoffs;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "locale_pk", nullable = false)
    @NotNull
    public Locale getLocale() {
        return locale;
    }

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "county_pk", nullable = false)
    @NotNull
    public County getCounty() {
        return county;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "municipality_status_pk", nullable = false)
    public MunicipalityStatus getMunicipalityStatus() {
        return municipalityStatus;
    }

    @OneToOne(mappedBy = "municipality", cascade = CascadeType.ALL)
    public MunicipalityLocalConfigStatus getLocalConfigStatus() {
        return localConfigStatus;
    }

    @OneToOne(mappedBy = "municipality", fetch = LAZY, cascade = CascadeType.ALL)
    public ScanningConfig getScanningConfig() {
        return scanningConfig;
    }

    @Transient
    private MunicipalityStatusEnum getMunicipalityStatusEnum() {
        return getMunicipalityStatus().toEnumValue();
    }

    @Column(name = "municipality_id", nullable = false, length = 4)
    @ID(size = 4)
    public String getId() {
        return id;
    }

    @Column(name = "municipality_name", nullable = false, length = 50)
    @LettersOrDigits
    @StringNotNullEmptyOrBlanks
    @Size(max = 50)
    public String getName() {
        return name;
    }

    @Column(name = "election_card_text", length = 200)
    @Pattern(regexp = ElectionCardConfig.REGEX_INFO_TEXT, message = "@config.local.election_card.infoText_invalid")
    public String getElectionCardText() {
        return electionCardText;
    }

    @NotNull
    @Column(name = "avkrysningsmanntall_kjort", nullable = false)
    public boolean isAvkrysningsmanntallKjort() {
        return avkrysningsmanntallKjort;
    }

    @Column(name = "required_protocol_count", nullable = false)
    public boolean isRequiredProtocolCount() {
        return requiredProtocolCount;
    }

    @Column(name = "technical_polling_districts_allowed", nullable = false)
    @NotNull
    public boolean isTechnicalPollingDistrictsAllowed() {
        return technicalPollingDistrictsAllowed;
    }

    @Column(name = "handle_election_card_centrally")
    public Boolean getHandleElectionCardCentrally() {
        return handleElectionCardCentrally;
    }

    @OneToMany(mappedBy = "municipality", fetch = LAZY)
    public Set<Borough> getBoroughs() {
        return boroughs;
    }

    public void add(Borough borough) {
        getBoroughs().add(borough);
    }

    @OneToMany(mappedBy = "municipality", fetch = LAZY, cascade = ALL, orphanRemoval = true)
    public Set<MunicipalityOpeningHour> getOpeningHours() {
        return openingHours;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        switch (level) {
            case MUNICIPALITY:
                return this.getPk();
            case COUNTY:
                return county.getPk();
            default:
                return null;
        }
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        return null;
    }

    @Transient
    public Borough getMunicipalityBorough() {
        for (Borough borough : boroughs) {
            if (borough.isMunicipality1()) {
                return borough;
            }
        }
        throw new EntityNotFoundException("Could not find municipality borough for municipality " + areaPath().path());
    }

    @Transient
    public PollingDistrict getMunicipalityPollingDistrict() {
        return getMunicipalityBorough().getMunicipalityPollingDistrict();
    }

    /**
     * @return pollingDistricts in this municipality
     */
    public Collection<PollingDistrict> pollingDistricts() {
        Collection<PollingDistrict> pollingDistricts = new HashSet<>();
        for (Borough borough : getBoroughs()) {
            pollingDistricts.addAll(borough.getPollingDistricts());
        }
        return pollingDistricts;
    }

    /**
     * @return technical pollingDistricts in this municipality
     */
    public Collection<PollingDistrict> technicalPollingDistricts() {
        Collection<PollingDistrict> pollingDistricts = new HashSet<>();
        for (Borough borough : getBoroughs()) {
            if (borough.isMunicipality1()) {
                return borough.technicalPollingDistricts();
            }
        }
        return pollingDistricts;
    }

    public PollingDistrict technicalPollingDistrictById(String id) {
        for (PollingDistrict district : technicalPollingDistricts()) {
            if (district.getId().equals(id)) {
                return district;
            }
        }
        return null;
    }

    /**
     * Regular districts is defined as regular or child.
     *
     * @param includeParents Also includes parents.
	 *
     * @return regular pollingDistricts in this municipality
     */
    public Collection<PollingDistrict> regularPollingDistricts(boolean includeParents, boolean includeChildren) {
        return pollingDistricts()
                .stream()
                .filter(includePredicate(includeParents, includeChildren))
                .sorted(PollingDistrict.sortById())
                .collect(toList());
    }

    public PollingDistrict regularPollingDistrictById(String id, boolean includeParents, boolean includeChildren) {
        for (PollingDistrict district : regularPollingDistricts(includeParents, includeChildren)) {
            if (district.getId().equals(id)) {
                return district;
            }
        }
        return null;
    }

    private Predicate<PollingDistrict> includePredicate(boolean includeParents, boolean includeChildren) {
        if (includeChildren && includeParents) {
            return this::includeRegularChildrenAndParents;
        }
        if (includeChildren) {
            return this::includeRegularAndChildren;
        }
        if (includeParents) {
            return this::includeRegularAndParents;
        } else {
            return this::includeRegular;
        }
    }

    private boolean includeRegularChildrenAndParents(PollingDistrict pollingDistrict) {
        return pollingDistrict.type() == REGULAR || pollingDistrict.type() == CHILD || pollingDistrict.type() == PARENT;
    }

    private boolean includeRegularAndChildren(PollingDistrict pollingDistrict) {
        return pollingDistrict.type() == REGULAR || pollingDistrict.type() == CHILD;
    }

    private boolean includeRegularAndParents(PollingDistrict pollingDistrict) {
        return pollingDistrict.type() == REGULAR || pollingDistrict.type() == PARENT;
    }

    private boolean includeRegular(PollingDistrict pollingDistrict) {
        return pollingDistrict.type() == REGULAR;
    }

    /**
     * @return pollingPlaces in this municipality
     */
    public Collection<PollingPlace> pollingPlaces() {
        Collection<PollingPlace> pollingPlaces = new HashSet<>();
        for (PollingDistrict pollingDistrict : pollingDistricts()) {
            pollingPlaces.addAll(pollingDistrict.getPollingPlaces());
        }
        return pollingPlaces;
    }

    public Collection<PollingPlace> pollingPlacesAdvance() {
        return pollingPlaces()
                .stream()
                .filter(place -> !place.isElectionDayVoting())
                .sorted(PollingPlace.sortById())
                .collect(toList());
    }

    public PollingPlace pollingPlacesAdvanceById(String id) {
        for (PollingPlace place : pollingPlacesAdvance()) {
            if (place.getId().equals(id)) {
                return place;
            }
        }
        return null;
    }

    public AreaPath areaPath() {
        return getCounty().areaPath().add(getId());
    }

    public KommuneSti kommuneSti() {
        return ValggeografiSti.kommuneSti(areaPath());
    }

    /**
     * @return true if any of the polling districts for this municipality is a parent polling district (tellekrets), else false
     */
    @Transient
    public boolean hasParentPollingDistricts() {
        return !parentPollingDistricts().isEmpty();
    }

    public Collection<PollingDistrict> parentPollingDistricts() {
        return pollingDistricts()
                .stream()
                .filter(PollingDistrict::isParentPollingDistrict)
                .collect(toList());
    }

    public Collection<PollingDistrict> childPollingDistricts() {
        return pollingDistricts()
                .stream()
                .filter(PollingDistrict::hasParentPollingDistrict)
                .collect(toList());
    }

    @Transient
    public boolean isLocalConfigurationStatus() {
        return getMunicipalityStatus() != null && getMunicipalityStatusEnum() == LOCAL_CONFIGURATION;
    }

    @Transient
    public boolean isCentralConfigurationStatus() {
        return getMunicipalityStatus() != null && getMunicipalityStatusEnum() == CENTRAL_CONFIGURATION;
    }

    @Transient
    public boolean isApprovedConfigurationStatus() {
        return getMunicipalityStatus() != null && getMunicipalityStatusEnum() == APPROVED_CONFIGURATION;
    }

    PollingDistrict pollingDistrictOfId(String id) {
        for (PollingDistrict pollingDistrict : pollingDistricts()) {
            if (id.equals(pollingDistrict.getId())) {
                return pollingDistrict;
            }
        }
        return null;
    }

    /**
     * Samlekommune is a property of a special municipality type used in the sami election. These municipalities are fictitious and are located under a
     * fictitious county with id 00. All this is invented to handle counting of advance votes for municipalities with less than 30 voters in the sami election.
     * (It could be avoided by making the sami election adopt a normal geographical hierarchy like other elections, see EVA-584.)
     *
     * @return true if samlekommune, else false
     */
    @Transient
    public boolean isSamlekommune() {
        return getCounty().getId().equals(SAMI_FICTITIOUS_COUNTY_ID);
    }

    public void updateStatus(MunicipalityConfigStatus status) {
        if (localConfigStatus == null) {
            localConfigStatus = MunicipalityConfigStatusMapper.toMunicipalityLocalConfigStatus(new MunicipalityLocalConfigStatus(this), status);
        } else {
            localConfigStatus.checkVersion(status);
            localConfigStatus = MunicipalityConfigStatusMapper.toMunicipalityLocalConfigStatus(localConfigStatus, status);
        }
    }

    public boolean hasBoroughs() {
        return getBoroughs().size() > 1;
    }

    public void addOpeningHours(MunicipalityOpeningHour newOpeningHours) {
        if (this.getOpeningHours().size() >= MAX_NUMBER_OF_OPENING_HOURS) {
            throw new EvoteException("Municipality [ " + this + "] already has max number of opening hours - cannot add new [ " + newOpeningHours + "] " +
                    "- you need to update the existing ones by using their pk's[" + this.getOpeningHours() + "]");
        }

        newOpeningHours.setMunicipality(this);
        this.getOpeningHours().add(newOpeningHours);
    }

    @Transient
    public String electionEventId() {
        return getCounty().electionEventId();
    }

    /**
     * @return The existing scanning config, or a new blank one if it does not exist
     */
    @Transient
    public ScanningConfig getOrCreateScanningConfig() {
        if (scanningConfig == null) {
            return createBlankScanningConfig();
        } else {
            return scanningConfig;
        }
    }

    private ScanningConfig createBlankScanningConfig() {
        scanningConfig = new ScanningConfig();
        scanningConfig.setMunicipality(this);
        return scanningConfig;
    }

    @Transient
    public LocalDate dateForFirstElectionDay() {
        LocalDate initialDate = new LocalDate(Long.MAX_VALUE, DateTimeZone.UTC);
        LocalDate firstElectionDate = initialDate;
        List<OpeningHours> pollingPlaceOpeningHours = new ArrayList<>();
        for (Borough borough : getBoroughs()) {
            for (PollingDistrict pollingDistrict : borough.getPollingDistricts()) {
                for (PollingPlace pollingPlace : pollingDistrict.getPollingPlaces()) {
                    pollingPlaceOpeningHours.addAll(pollingPlace.getOpeningHours());
                }
            }
        }

        for (OpeningHours openingHour : pollingPlaceOpeningHours) {
            LocalDate electionDate = openingHour.getElectionDay().getDate();
            if (electionDate.isBefore(firstElectionDate)) {
                firstElectionDate = electionDate;
            }
        }

        return initialDate == firstElectionDate ? null : firstElectionDate;
    }
}
