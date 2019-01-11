package no.valg.eva.admin.common.configuration.model.election;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.ElectionEvent;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.io.Serializable;

@Getter
@Setter
@ToString(of = "pk")
public class ElectionDay extends VersionedObject implements Serializable {
    private Long pk;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private ElectionEvent electionEvent;

    public ElectionDay() {
        super(0);
    }

    public ElectionDay(int version) {
        super(version);
    }

    public ElectionDay(int version, Long pk, LocalDate date, LocalTime startTime, LocalTime endTime, ElectionEvent electionEvent) {
        super(version);
        this.pk = pk;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.electionEvent = electionEvent;
    }

    public boolean sameDate(ElectionDay other) {
        return this.getDate().equals(other.getDate());
    }
}
