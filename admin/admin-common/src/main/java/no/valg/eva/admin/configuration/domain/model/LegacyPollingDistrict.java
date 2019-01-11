package no.valg.eva.admin.configuration.domain.model;

import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.model.VersionedEntity;
import no.evote.persistence.AntiSamyEntityListener;
import no.evote.validation.AntiSamy;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "legacy_polling_district", uniqueConstraints = @UniqueConstraint(columnNames = {"voter_pk", "legacy_municipality_id", "legacy_polling_district_id"}))
@AttributeOverride(name = "pk", column = @Column(name = "legacy_polling_district_pk"))
@EntityListeners(AntiSamyEntityListener.class)
@AntiSamy
@NoArgsConstructor
@Setter
public class LegacyPollingDistrict extends VersionedEntity implements java.io.Serializable {
    private Voter voter;
    private String legacyMunicipalityId;
    private String legacyPollingDistrictId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_pk")
    public Voter getVoter() {
        return voter;
    }
    
    @Column(name = "legacy_municipality_id", nullable = false, length = 4)
    public String getLegacyMunicipalityId() {
        return legacyMunicipalityId;
    }
    
    @Column(name = "legacy_polling_district_id", nullable = false, length = 4)
    public String getLegacyPollingDistrictId() {
        return legacyPollingDistrictId;
    }
}
