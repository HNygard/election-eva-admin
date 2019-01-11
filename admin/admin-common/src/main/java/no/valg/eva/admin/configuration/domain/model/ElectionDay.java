package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Election event day(s)
 */
@Entity
@Table(name = "election_day", uniqueConstraints = {@UniqueConstraint(columnNames = {SQLConstants.ELECTION_EVENT_PK, "election_day_date"})})
@AttributeOverride(name = "pk", column = @Column(name = "election_day_pk"))
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "ElectionDay.findForPollingDistrict",
                query = "SELECT ed.* FROM election_day ed "
                        + "JOIN mv_area pda ON pda.polling_district_pk = ? AND pda.area_level = 5 "
                        + "JOIN mv_area ppa ON ppa.polling_district_pk = pda.polling_district_pk AND ppa.area_level = 6 "
                        + "WHERE EXISTS (SELECT opening_hours_pk FROM opening_hours oh WHERE oh.polling_place_pk = ppa.polling_place_pk "
                        + "AND oh.election_day_pk = ed.election_day_pk)"
                        + "ORDER BY ed.election_day_date ASC",
                resultClass = ElectionDay.class),
        @NamedNativeQuery(
                name = "ElectionDay.findForMunicipality",
                query = "SELECT DISTINCT ed.* FROM election_day ed "
                        + "JOIN mv_area pda ON pda.municipality_pk = ? AND pda.area_level = 5 "
                        + "JOIN polling_district pd ON pda.polling_district_pk = pd.polling_district_pk AND NOT pd.municipality "
                        + "JOIN mv_area ppa ON ppa.polling_district_pk = pda.polling_district_pk AND ppa.area_level = 6 "
                        + "WHERE EXISTS (SELECT opening_hours_pk FROM opening_hours oh WHERE oh.polling_place_pk = ppa.polling_place_pk "
                        + "AND oh.election_day_pk = ed.election_day_pk)"
                        + "ORDER BY ed.election_day_date ASC",
                resultClass = ElectionDay.class)
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElectionDay extends VersionedEntity implements java.io.Serializable {

    private ElectionEvent electionEvent;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
    public ElectionEvent getElectionEvent() {
        return this.electionEvent;
    }

    public void setElectionEvent(final ElectionEvent electionEvent) {
        this.electionEvent = electionEvent;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    @Column(name = "election_day_date", nullable = false, length = 13)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    @Column(name = "election_day_start_time", nullable = false, length = 15)
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalTime")
    @Column(name = "election_day_end_time", nullable = false, length = 15)
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String electionYear() {
        return date.year().getAsString();
    }

    public boolean sameElectionDay(ElectionDay otherElectionDay) {
        return this.getDate() != null && otherElectionDay.getDate() != null
                && this.getDate().equals(otherElectionDay.getDate());
    }
}
