package no.valg.eva.admin.frontend.configuration.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ElectionDayViewModel implements Serializable {

    private static final int MAX_OPENING_HOURS_LIST_SIZE = 2;
    private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
    private static final DateTimeFormatter timeFormat = DateTimeFormat.forPattern("HH:mm");
    private static final long serialVersionUID = 4574296351417824272L;

    private boolean activated;
    private ElectionDay electionDay;
    private List<OpeningHours> openingHours;

    void addOpeningHour(OpeningHours openingHours) {
        if (getOpeningHours() == null) {
            setOpeningHours(new ArrayList<>());
        }
        getOpeningHours().add(openingHours);
    }

    public void addBlankOpeningHour(ElectionDay electionDay) {
        if (canAddOpeningHours()) {
            addOpeningHour(new OpeningHours(electionDay, null, null));
        }
    }

    public boolean canAddOpeningHours() {
        return openingHours.size() < MAX_OPENING_HOURS_LIST_SIZE;
    }

    public void deleteOpeningHour(OpeningHours openingHours) {
        if (isNotFirstOpeningHour(openingHours)) {
            getOpeningHours().remove(openingHours);
        }
    }

    private boolean isNotFirstOpeningHour(OpeningHours openingHours) {
        return getOpeningHours().indexOf(openingHours) > 0;

    }

    public void validate() {
        validateOpeningHours();
        checkNoneOpeningHoursAreOverlapping();
    }

    private void validateOpeningHours() {
        openingHours.forEach(current -> {
            if (current.getStartTime() == null || current.getEndTime() == null) {
                throw new EvoteException("@config.local.election_day_polling_place.validate.starttime_or_endtime_empty");
            } else if (current.getStartTime().isAfter(current.getEndTime())) {
                String[] args = {timeFormat.print(current.getStartTime()), timeFormat.print(current.getEndTime())};
                throw new EvoteException("@config.local.election_day_polling_place.validate.start_after_end", args);
            } else if (current.getStartTime().isBefore(electionDay.getStartTime())) {
                String[] args = {timeFormat.print(electionDay.getStartTime()), dateFormat.print(electionDay.getDate())};
                throw new EvoteException("@config.local.election_day_polling_place.validate.start_time_before_start", args);
            } else if (current.getEndTime().isAfter(electionDay.getEndTime())) {
                String[] args = {timeFormat.print(electionDay.getEndTime()), dateFormat.print(electionDay.getDate())};
                throw new EvoteException("@config.local.election_day_polling_place.validate.end_time_after_end", args);
            }
        });
    }

    private void checkNoneOpeningHoursAreOverlapping() {
        for (int i = 0; i < openingHours.size(); i++) {
            OpeningHours current = openingHours.get(i);

            if (isNotLast(i)) {
                OpeningHours next = openingHours.get(i + 1);
                if (current.getEndTime().isAfter(next.getStartTime())) {
                    throw new EvoteException("@config.local.election_day_polling_place.validate.starttime2_before_endtime1");
                }
            }
        }
    }

    private boolean isNotLast(int index) {
        return index < openingHours.size() - 1;
    }
}
