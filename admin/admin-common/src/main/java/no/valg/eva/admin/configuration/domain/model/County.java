package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.Letters;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.configuration.application.CountyConfigStatusMapper;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Counties
 */
@Entity
@Table(name = "county", uniqueConstraints = @UniqueConstraint(columnNames = {"country_pk", "county_id"}))
@NamedQueries({@NamedQuery(name = "County.findById", query = "SELECT c FROM County c WHERE c.country.pk = :countryPk AND c.id = :id"),
        @NamedQuery(name = "County.findByPkWithScanningConfig", query = "select c from County c LEFT JOIN FETCH c.scanningConfig WHERE c.pk = :pk"),
        @NamedQuery(name = "County.findByElectionEventWithScanningConfig", query = "select c from County c " +
                "LEFT JOIN FETCH c.scanningConfig sc " +
                "WHERE c.country.electionEvent.pk = :electionEventPk AND sc.scanning=true"),
        @NamedQuery(name = "County.findByMunicipality", query = "select m.county from Municipality m where m.pk = :municipalityPk"),
        @NamedQuery(name = "County.findByCountry", query = "SELECT c FROM County c WHERE c.country.pk = :countryPk"),
        @NamedQuery(name = "County.findByElectionEventAndId",
                query = "select c from County c WHERE c.country.electionEvent.pk = :electionEventPk AND c.id = :id"),
        @NamedQuery(name = "County.findByElectionEventAndStatus", query = "select c from County c, Country country "
                + "where c.country.pk = country.pk and c.countyStatus.id = :countyStatusId and "
                + "country.electionEvent.pk = :electionEventPk"),
        @NamedQuery(name = "County.findCountByCountry", query = "SELECT COUNT(c) FROM County c WHERE c.country.pk = :countryPk")})
@AttributeOverride(name = "pk", column = @Column(name = "county_pk"))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class County extends VersionedEntity implements java.io.Serializable, ContextSecurable {
    @Setter
    private Country country;
    @Setter
    private Locale locale;
    @Setter
    private String id;
    @Setter
    private String name;
    @Setter
    private CountyStatus countyStatus;
    @Setter
    private CountyLocalConfigStatus localConfigStatus;
    @Setter
    private ScanningConfig scanningConfig;
    @Setter
    private Set<Municipality> municipalities = new HashSet<>();

    public County(final String id, final String name, final Country country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_pk", nullable = false)
    @NotNull
    public Country getCountry() {
        return this.country;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "locale_pk", nullable = false)
    @NotNull
    public Locale getLocale() {
        return locale;
    }

    @Column(name = "county_id", nullable = false, length = 2)
    @ID(size = 2)
    public String getId() {
        return this.id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "county_status_pk", nullable = false)
    public CountyStatus getCountyStatus() {
        return countyStatus;
    }

    @OneToOne(mappedBy = "county", cascade = CascadeType.ALL)
    public CountyLocalConfigStatus getLocalConfigStatus() {
        return localConfigStatus;
    }

    @OneToOne(mappedBy = "county", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public ScanningConfig getScanningConfig() {
        return scanningConfig;
    }

    @Column(name = "county_name", nullable = false, length = 50)
    @Letters(extraChars = "-. ")
    @StringNotNullEmptyOrBlanks
    @Size(max = 50)
    public String getName() {
        return this.name;
    }

    @OneToMany(mappedBy = "county", fetch = FetchType.LAZY)
    public Set<Municipality> getMunicipalities() {
        return municipalities;
    }


    private CountyStatusEnum countyStatusEnum() {
        return getCountyStatus().toEnumValue();
    }

    @Transient
    public boolean isLocalConfigurationStatus() {
        return getCountyStatus() != null && countyStatusEnum() == CountyStatusEnum.LOCAL_CONFIGURATION;
    }

    @Transient
    public boolean isCentralConfigurationStatus() {
        return getCountyStatus() != null && countyStatusEnum() == CountyStatusEnum.CENTRAL_CONFIGURATION;
    }

    @Transient
    public boolean isApprovedConfigurationStatus() {
        return getCountyStatus() != null && countyStatusEnum() == CountyStatusEnum.APPROVED_CONFIGURATION;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        County other = (County) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            return other.name == null;
        } else {
            return name.equals(other.name);
        }
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        switch (level) {
            case COUNTRY:
                return country.getPk();
            case COUNTY:
                return getPk();
            default:
                return null;
        }
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        return null;
    }

    public AreaPath areaPath() {
        return getCountry().areaPath().add(getId());
    }

    public void updateStatus(CountyConfigStatus status) {
        if (localConfigStatus == null) {
            localConfigStatus = CountyConfigStatusMapper.toCountyLocalConfigStatus(new CountyLocalConfigStatus(this), status);
        } else {
            localConfigStatus.checkVersion(status);
            localConfigStatus = CountyConfigStatusMapper.toCountyLocalConfigStatus(localConfigStatus, status);
        }
    }

    @Transient
    public String electionEventId() {
        return getCountry().electionEventId();
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
        scanningConfig.setCounty(this);
        return scanningConfig;
    }
}
