package no.valg.eva.admin.common.voting.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import no.valg.eva.admin.common.configuration.model.local.PollingPlace;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@ToString
public class VotingConfirmationDto implements Serializable {

    private static final long serialVersionUID = 1279407002778406641L;

    private List<VotingDto> votingDtoListToConfirm;

    private VotingCategory votingCategory;

    private PollingPlace pollingPlace;

    private Municipality municipality;

    private ElectionGroup electionGroup;

    private LocalDate startDate;

    private LocalDate endDate;

    public Long getPollingPlacePK() {
        return getPollingPlace() != null ? getPollingPlace().getPk() : 0L;
    }

    public Long getMunicipalityPK() {
        return getMunicipality() != null ? getMunicipality().getPk() : null;
    }
}
