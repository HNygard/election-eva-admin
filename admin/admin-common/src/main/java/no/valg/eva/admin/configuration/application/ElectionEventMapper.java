package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.ElectionEvent;

public class ElectionEventMapper {

    private ElectionEventMapper() {
    }

    public static ElectionEvent toDto(no.valg.eva.admin.configuration.domain.model.ElectionEvent domainElectionEvent) {
        return ElectionEvent.builder().pk(domainElectionEvent != null ? domainElectionEvent.getPk() : 0).build();
    }

    public static no.valg.eva.admin.configuration.domain.model.ElectionEvent toDomainModel(ElectionEvent electionEventDto) {
        no.valg.eva.admin.configuration.domain.model.ElectionEvent domainElectionEvent = new no.valg.eva.admin.configuration.domain.model.ElectionEvent();
        domainElectionEvent.setPk(electionEventDto.getPk());

        return domainElectionEvent;

    }
}
