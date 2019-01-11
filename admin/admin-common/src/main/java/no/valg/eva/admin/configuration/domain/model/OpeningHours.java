package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "opening_hours", uniqueConstraints = @UniqueConstraint(columnNames = {"polling_place_pk", "election_day_pk", "start_time"}))
@AttributeOverride(name = "pk", column = @Column(name = "opening_hours_pk"))
@NamedQueries({
        @NamedQuery(name = "OpeningHours.findOpeningHoursForPollingPlace", query = "SELECT oh FROM OpeningHours oh WHERE oh.pollingPlace.pk = :pollingPlacePk "
                + "ORDER BY oh.electionDay.date ASC, oh.startTime ASC")})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OpeningHours extends VersionedEntity implements java.io.Serializable, ContextSecurable {

    private PollingPlace pollingPlace;
    private ElectionDay electionDay;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polling_place_pk")
    public PollingPlace getPollingPlace() {
        return this.pollingPlace;
    }

    public void setPollingPlace(final PollingPlace pollingPlace) {
        this.pollingPlace = pollingPlace;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "election_day_pk", nullable = false)
    @NotNull
    public ElectionDay getElectionDay() {
        return this.electionDay;
    }

    public void setElectionDay(final ElectionDay electionDay) {
        this.electionDay = electionDay;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    @Column(name = "start_time", nullable = false, length = 15)
    @NotNull
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    @Column(name = "end_time", nullable = false, length = 15)
    @NotNull
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Long getAreaPk(final AreaLevelEnum level) {
        if (level.equals(AreaLevelEnum.POLLING_PLACE)) {
            return pollingPlace.getPk();
        }
        return null;
    }

    @Override
    public Long getElectionPk(final ElectionLevelEnum level) {
        return null;
    }

    public boolean isSameDay(LocalDate date) {
        return electionDay != null && electionDay.getDate().compareTo(date) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        OpeningHours otherOpeningHours = (OpeningHours) o;

        return pkNullOrEqual(otherOpeningHours)
                && pollingPlaceIsNullOrEquals(otherOpeningHours)
                && sameDayAndTime(otherOpeningHours);
    }

    private boolean pollingPlaceIsNullOrEquals(OpeningHours that) {
        return pollingPlace != null ? pollingPlace.equals(that.pollingPlace) : that.pollingPlace == null;
    }

    private boolean pkNullOrEqual(OpeningHours that) {
        return this.getPk() == null || this.getPk().equals(that.getPk());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pollingPlace != null ? pollingPlace.hashCode() : 0);
        result = 31 * result + (electionDay != null ? electionDay.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        return result;
    }

    @Transient
    public boolean samePKWithChangedStartAndEndTime(OpeningHours otherOpeningHours) {
        return this.getPk().equals(otherOpeningHours.getPk())
                && (!sameStartTime(otherOpeningHours) || !sameEndTime(otherOpeningHours));
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

    private boolean sameDayAndTime(OpeningHours otherOpeningHours) {
        return sameElectionDay(otherOpeningHours)
                && sameStartTime(otherOpeningHours)
                && sameEndTime(otherOpeningHours);
    }

    private boolean sameElectionDay(OpeningHours otherOpeningHours) {
        return this.getElectionDay().sameElectionDay(otherOpeningHours.getElectionDay());
    }
}
