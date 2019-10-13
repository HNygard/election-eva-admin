package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

/**
 * Helper class for getting election info related to elections with borough level
 */
@Default
@ApplicationScoped
public class BoroughElectionDomainService {

    @Inject
    private MvElectionRepository mvElectionRepository;

    public BoroughElectionDomainService() {
    }

    /**
     * This method checks if there exists an MvElection with area level borough and the given ElectionPath for the election level CONTEST,
     * and then if there exists any MvElection for the given ElectionPath and AreaPath with the area level borough.
     */
    @SecurityNone
    public boolean electionPathAndMvAreaHasAccessToBoroughs(ElectionPath electionPath, AreaPath areaPath) {
        return electionHasBoroughElection(electionPath)
                && hasContestsWithElectionPathAreaPathAndAreaLevel(electionPath, areaPath, AreaLevelEnum.BOROUGH);
    }

    private boolean electionHasBoroughElection(ElectionPath electionPath) {
        return mvElectionRepository.findByPathAndLevel(electionPath, ElectionLevelEnum.CONTEST)
                .stream()
                .anyMatch(mvElection -> mvElection.getActualAreaLevel() == AreaLevelEnum.BOROUGH);
    }

    private boolean hasContestsWithElectionPathAreaPathAndAreaLevel(ElectionPath electionPath, AreaPath areaPath, AreaLevelEnum areaLevelEnum) {
        return mvElectionRepository.findContestsForElectionAndArea(electionPath, areaPath)
                .stream()
                .anyMatch(mvElection -> areaLevelEnum == mvElection.getActualAreaLevel());
    }

    @SecurityNone
    public boolean electionPathAndMvAreaHasAccessToBoroughs(MvArea mvArea) {
        return electionPathAndMvAreaHasAccessToBoroughs(mvArea.getElectionEvent().electionPath(), mvArea.areaPath());
    }

    /**
     * I skrivende stund skal VO stemmer telles per krets, og alle andre tellekategorier telles sentralt per bydel.
     * Dette er avklart med valgfaglig per 19.01.2018
     *
     * @param countCategory Tellekategori man ønsker å finne tellested for
     * @return CountingMode Hvor stemmene skal telles, for eksempel sentralt på bydelsnivå eller per krets.
     */
    public CountingMode countingModeForBoroughWithCountCategory(CountCategory countCategory) {
        if (countCategory == CountCategory.VO) {
            return CountingMode.BY_POLLING_DISTRICT;
        } else {
            return CountingMode.CENTRAL;
        }
    }
}
