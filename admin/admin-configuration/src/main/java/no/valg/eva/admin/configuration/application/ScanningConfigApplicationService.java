package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.ScanningConfigService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.service.ScanningConfigDomainService;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Eksporter_ScanningConfig;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

@Remote(ScanningConfigService.class)
@Stateless(name = "ScanningConfigService")
public class ScanningConfigApplicationService implements ScanningConfigService {
    
    @Inject
    private ScanningConfigDomainService scanningConfigDomainService;
    
    @Override
    @Security(accesses = Konfigurasjon_Eksporter_ScanningConfig, type = READ)
    public byte[] exportScanningConfigAsExcelFile(UserData userData, Long electionEventPk) {
        return scanningConfigDomainService.exportScanningConfigAsExcelFile(electionEventPk);
    }
}
