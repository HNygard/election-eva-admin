package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.LegacyPollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

public class VelgerDomainService {

    @Inject
    private LegacyPollingDistrictRepository legacyPollingDistrictRepository;
    @Inject
    private VoterRepository voterRepository;

    /**
     * Nyttig for å kunne committe flere velgere samtidig (gir som regel bedre ytelse)
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateVoterInNewTransaction(UserData userData, List<Voter> voters, List<LegacyPollingDistrict> legacyPollingDistricts) {
        List<Voter> persistedVoters = updateVoters(userData, voters);
        updateLegacyPollingDistricts(userData, legacyPollingDistricts, persistedVoters);
    }

    private List<Voter> updateVoters(UserData userData, List<Voter> voters) {
        return voterRepository.updateVoters(userData, voters);
    }

    private void updateLegacyPollingDistricts(UserData userData, List<LegacyPollingDistrict> legacyPollingDistricts, List<Voter> persistedVoters) {
        legacyPollingDistricts.removeIf(this::isEmpty);

        legacyPollingDistricts.forEach(legacyPollingDistrict -> persistedVoters.stream()
                .filter(v -> isSameVoter(legacyPollingDistrict, v))
                .findFirst()
                .ifPresent(legacyPollingDistrict::setVoter));

        legacyPollingDistrictRepository.updateLegacyPollingDistricts(userData, legacyPollingDistricts);
    }

    private boolean isEmpty(LegacyPollingDistrict legacyPollingDistrict) {
        return legacyPollingDistrict.getVoter() == null || legacyPollingDistrict.getLegacyMunicipalityId().isEmpty() || legacyPollingDistrict.getLegacyPollingDistrictId().isEmpty();
    }

    private boolean isSameVoter(LegacyPollingDistrict legacyPollingDistrict, Voter v) {
        return v.getId().equals(legacyPollingDistrict.getVoter().getId());
    }

    public void updateVoter(UserData userData, Voter voter, LegacyPollingDistrict legacyPollingDistrict) {
        voterRepository.update(userData, voter);
        if (!isEmpty(legacyPollingDistrict)) {
            legacyPollingDistrictRepository.create(userData, legacyPollingDistrict);
        }
    }

    public void updateVoter(UserData userData, Voter voter) {
        voterRepository.update(userData, voter);
    }
}
