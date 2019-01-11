package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpeningHoursSorter {

    private OpeningHoursSorter() {
    }

    public static List<OpeningHours> toSortedList(Set<OpeningHours> openingHourSet) {
        if (openingHourSet == null || openingHourSet.isEmpty()) {
            return new ArrayList<>();
        }

        List<no.valg.eva.admin.configuration.domain.model.OpeningHours> sortedOpeningHours = new ArrayList<>(openingHourSet);
        sortedOpeningHours.sort((o1, o2) -> {
            if (!o1.getElectionDay().getPk().equals(o2.getElectionDay().getPk())) {
                return o1.getElectionDay().getPk().compareTo(o2.getElectionDay().getPk());
            }

            return o1.getStartTime().compareTo(o2.getStartTime());
        });

        return sortedOpeningHours;
    }

    public static List<MunicipalityOpeningHour> toSortedMunicipalityOpeningHourList(Set<MunicipalityOpeningHour> openingHourSet) {
        if (openingHourSet == null || openingHourSet.isEmpty()) {
            return new ArrayList<>();
        }

        List<MunicipalityOpeningHour> sortedOpeningHours = new ArrayList<>(openingHourSet);
        sortedOpeningHours.sort((o1, o2) -> {
            if (!o1.getElectionDay().getPk().equals(o2.getElectionDay().getPk())) {
                return o1.getElectionDay().getPk().compareTo(o2.getElectionDay().getPk());
            }

            return o1.getStartTime().compareTo(o2.getStartTime());
        });

        return sortedOpeningHours;
    }
}
