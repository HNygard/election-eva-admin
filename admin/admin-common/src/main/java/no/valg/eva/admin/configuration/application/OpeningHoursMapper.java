package no.valg.eva.admin.configuration.application;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class OpeningHoursMapper {

    OpeningHoursMapper() {
    }
    
    public static List<no.valg.eva.admin.configuration.domain.model.OpeningHours>
    toDomainModelList(List<no.valg.eva.admin.common.configuration.model.OpeningHours> openingHourPojoList) {
        return openingHourPojoList
                .stream()
                .map(OpeningHoursMapper::toDomainModel).collect(toList());
    }

    public static List<no.valg.eva.admin.common.configuration.model.OpeningHours>
    toDtoList(List<no.valg.eva.admin.configuration.domain.model.OpeningHours> domainOpeningHours) {
        return domainOpeningHours.stream()
                .map(OpeningHoursMapper::toDto).collect(toList());
    }

    private static no.valg.eva.admin.common.configuration.model.OpeningHours toDto(no.valg.eva.admin.configuration.domain.model.OpeningHours domainOpeningHours) {
        return no.valg.eva.admin.common.configuration.model.OpeningHours.builder()
                .electionDay(ElectionDayMapper.toDto(domainOpeningHours.getElectionDay()))
                .startTime(domainOpeningHours.getStartTime())
                .endTime(domainOpeningHours.getEndTime())
                .build();
    }

    public static no.valg.eva.admin.configuration.domain.model.OpeningHours toDomainModel(no.valg.eva.admin.common.configuration.model.OpeningHours openingHours) {
        return no.valg.eva.admin.configuration.domain.model.OpeningHours.builder()
                .endTime(openingHours.getEndTime())
                .startTime(openingHours.getStartTime())
                .electionDay(ElectionDayMapper.toDomainModel(openingHours.getElectionDay()))
                .build();
    }

    public static List<no.valg.eva.admin.common.configuration.model.OpeningHours> sort(List<no.valg.eva.admin.common.configuration.model.OpeningHours> openingHourList) {
        openingHourList.sort((o1, o2) -> {
            if (!o1.getElectionDay().getDate().equals(o2.getElectionDay().getDate())) {
                return o1.getElectionDay().getDate().compareTo(o2.getElectionDay().getDate());
            }

            return o1.getStartTime().compareTo(o2.getStartTime());
        });

        return openingHourList;
    }
}
