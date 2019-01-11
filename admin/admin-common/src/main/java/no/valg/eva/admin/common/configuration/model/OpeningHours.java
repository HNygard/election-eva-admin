package no.valg.eva.admin.common.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.Date;

@Builder
@AllArgsConstructor
@ToString
public class OpeningHours implements Serializable {

    @Getter
    @Setter
    private ElectionDay electionDay;

    @Getter
    @Setter
    private LocalTime startTime;

    @Getter
    @Setter
    private LocalTime endTime;

    public void setStartTimeAsDate(Date startTime) {
        this.startTime = startTime != null ? new LocalTime(startTime) : null;
    }

    public Date getStartTimeAsDate() {
        return getStartTime() != null ? getStartTime().toDateTimeToday().toDate() : null;
    }

    public void setEndTimeAsDate(Date endTime) {
        this.endTime = endTime != null ? new LocalTime(endTime) : null;
    }

    public Date getEndTimeAsDate() {
        return getEndTime() != null ? getEndTime().toDateTimeToday().toDate() : null;
    }
}
