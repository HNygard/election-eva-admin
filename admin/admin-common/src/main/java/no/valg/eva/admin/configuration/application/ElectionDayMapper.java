package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.election.ElectionDay;

import java.util.List;
import java.util.stream.Collectors;

public class ElectionDayMapper {

    ElectionDayMapper() {
    }

    public static ElectionDay toDto(no.valg.eva.admin.configuration.domain.model.ElectionDay domainElectionDay) {
        ElectionDay electionDay = new ElectionDay(domainElectionDay.getAuditOplock());
        electionDay.setEndTime(domainElectionDay.getEndTime());
        electionDay.setStartTime(domainElectionDay.getStartTime());
        electionDay.setDate(domainElectionDay.getDate());
        electionDay.setPk(domainElectionDay.getPk());
        electionDay.setElectionEvent(ElectionEventMapper.toDto(domainElectionDay.getElectionEvent()));

        return electionDay;
    }

    static List<ElectionDay> toDtoList(List<no.valg.eva.admin.configuration.domain.model.ElectionDay> domainElectionDays) {
        return domainElectionDays.stream()
                .map(ElectionDayMapper::toDto)
                .collect(Collectors.toList());
    }

    public static no.valg.eva.admin.configuration.domain.model.ElectionDay toDomainModel(ElectionDay electionDayDto) {
        no.valg.eva.admin.configuration.domain.model.ElectionDay dbElectionDay = new no.valg.eva.admin.configuration.domain.model.ElectionDay();
        dbElectionDay.setDate(electionDayDto.getDate());
        dbElectionDay.setEndTime(electionDayDto.getEndTime());
        dbElectionDay.setPk(electionDayDto.getPk());
        dbElectionDay.setStartTime(electionDayDto.getStartTime());
        dbElectionDay.setAuditOplock(electionDayDto.getVersion());
        dbElectionDay.setElectionEvent(ElectionEventMapper.toDomainModel(electionDayDto.getElectionEvent()));

        return dbElectionDay;
    }

    public static List<no.valg.eva.admin.configuration.domain.model.ElectionDay> toDomainModelList(List<ElectionDay> electionDays) {
        return electionDays.stream()
                .map(ElectionDayMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
