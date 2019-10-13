package no.valg.eva.admin.configuration.domain.model.factory;

import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.filter.PollingDistrictFilterEnum;
import no.valg.eva.admin.configuration.domain.service.CountingModeDomainService;

@Default
@ApplicationScoped
public class PollingDistrictFilterFactory {

    @Inject
    private CountingModeDomainService countingModeDomainService;

    public PollingDistrictFilterFactory() {

    }

    public PollingDistrictFilterFactory(CountingModeDomainService countingModeDomainService) {
        this.countingModeDomainService = countingModeDomainService;
    }

    public PollingDistrictFilterEnum build(
            final UserData userData,
            final CountCategory countCategory,
            final ElectionPath selectedElectionPath,
            final AreaPath boroughPath) {

        CountingMode countingMode = countingModeDomainService.findCountingMode(countCategory, selectedElectionPath, boroughPath);
        switch (countingMode) {
            case BY_TECHNICAL_POLLING_DISTRICT:
                return PollingDistrictFilterEnum.FOR_BY_TECHNICAL_POLLING_DISTRICT;
            case CENTRAL:
                if (userData.getOperatorAreaLevel() != POLLING_DISTRICT) {
                    return PollingDistrictFilterEnum.FOR_CENTRAL_AND_OPERATOR_NOT_ON_POLLING_DISTRICT;
                }
                return PollingDistrictFilterEnum.DEFAULT;
            default:
                if (userData.getOperatorAreaLevel() != POLLING_DISTRICT) {
                    return PollingDistrictFilterEnum.FOR_OPERATOR_NOT_ON_POLLING_DISTRICT;
                }
                return PollingDistrictFilterEnum.DEFAULT;
        }
    }
}
