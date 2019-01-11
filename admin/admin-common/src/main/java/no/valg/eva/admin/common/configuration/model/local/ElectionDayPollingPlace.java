package no.valg.eva.admin.common.configuration.model.local;

import lombok.Data;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.OpeningHours;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ElectionDayPollingPlace extends PollingPlace {

    private long parentPk;
    private String parentName;
    private boolean usePollingStations;
    private boolean openingHoursDiffsFromMunicipality;
    private List<OpeningHours> openingHours = new ArrayList<>();
    private String infoText;
    private boolean hasPollingStations;

    public ElectionDayPollingPlace(AreaPath path) {
        this(path, 0);
    }

    public ElectionDayPollingPlace(AreaPath path, int version) {
        super(path, version);
    }

    public List<OpeningHours> electionDayOpeningHours() {
        return openingHours.stream().sorted((openingHours1, openingHours2) -> {
            if (openingHours1.getElectionDay().sameDate(openingHours2.getElectionDay())) {
                return openingHours1.getStartTime().compareTo(openingHours2.getStartTime());
            }

            return openingHours1.getElectionDay().getDate().compareTo(openingHours2.getElectionDay().getDate());
        }).collect(Collectors.toList());

    }
}
