package no.valg.eva.admin.backend.configuration.local.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.evote.exception.EvoteException;
import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import org.hibernate.annotations.Type;
import org.joda.time.LocalTime;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "municipality_opening_hour", uniqueConstraints = @UniqueConstraint(columnNames = {"municipality_pk", "election_day_pk", "start_time"}))
@AttributeOverride(name = "pk", column = @Column(name = "municipality_opening_hour_pk"))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
@Setter
public class MunicipalityOpeningHour extends VersionedEntity implements Serializable {

    @PrePersist
    public void prePersist() {
        if (municipality == null) {
            throw new EvoteException("Both municipality and polling place cannot be null for opening hour[" + this + "] - one of those fields must be set!");
        }
    }

    private ElectionDay electionDay;

    private LocalTime startTime;

    private LocalTime endTime;

    private Municipality municipality;

    @NotNull
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "election_day_pk", nullable = false)
    public ElectionDay getElectionDay() {
        return electionDay;
    }

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    @Column(name = "start_time", nullable = false, length = 15)
    public LocalTime getStartTime() {
        return startTime;
    }

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    @Column(name = "end_time", nullable = false, length = 15)
    public LocalTime getEndTime() {
        return endTime;
    }

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "municipality_pk")
    public Municipality getMunicipality() {
        return municipality;
    }

    @Transient
    private boolean sameEndTime(OpeningHours otherOpeningHours) {
        return this.getEndTime() != null
                && otherOpeningHours.getEndTime() != null
                && this.getEndTime().equals(otherOpeningHours.getEndTime());
    }

    @Transient
    private boolean sameStartTime(OpeningHours otherOpeningHours) {
        return this.getStartTime().equals(otherOpeningHours.getStartTime());
    }

    @Transient
    public boolean isValid() {
        return this.getStartTime() != null
                && this.getEndTime() != null;
    }

    public boolean sameDayAndTime(OpeningHours otherOpeningHours) {
        return sameElectionDay(otherOpeningHours)
                && sameStartTime(otherOpeningHours)
                && sameEndTime(otherOpeningHours);
    }

    private boolean sameElectionDay(OpeningHours otherOpeningHours) {
        return this.getElectionDay().sameElectionDay(otherOpeningHours.getElectionDay());
    }
}
