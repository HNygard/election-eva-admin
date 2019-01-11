package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.configuration.model.OpeningHours;

import java.util.List;

import static java.util.stream.Collectors.toList;
 
class MunicipalityOpeningHourMapper {

    private MunicipalityOpeningHourMapper() {
    }

    public static List<MunicipalityOpeningHour> toDomainModelList(List<OpeningHours> openingHours) {
        return openingHours
                .stream()
                .map(MunicipalityOpeningHourMapper::toDomainModel).collect(toList());
    }


    private static MunicipalityOpeningHour toDomainModel(no.valg.eva.admin.common.configuration.model.OpeningHours openingHours) {
        return MunicipalityOpeningHour.builder()
                .endTime(openingHours.getEndTime())
                .startTime(openingHours.getStartTime())
                .electionDay(ElectionDayMapper.toDomainModel(openingHours.getElectionDay()))
                .build();
    }


    public static List<OpeningHours>
    toDtoList(List<MunicipalityOpeningHour> domainOpeningHours) {
        return domainOpeningHours.stream()
                .map(MunicipalityOpeningHourMapper::toDto)
                .collect(toList());
    }

    private static OpeningHours toDto(MunicipalityOpeningHour domainOpeningHour) {
        return no.valg.eva.admin.common.configuration.model.OpeningHours.builder()
                .electionDay(ElectionDayMapper.toDto(domainOpeningHour.getElectionDay()))
                .startTime(domainOpeningHour.getStartTime())
                .endTime(domainOpeningHour.getEndTime())
                .build();
    }


}
