package no.valg.eva.admin.frontend.configuration.ctrls.local;

import lombok.NoArgsConstructor;
import no.evote.presentation.exceptions.ErrorPageRenderer;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.ScanningConfigService;
import no.valg.eva.admin.frontend.BaseController;
import org.apache.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Named
@ViewScoped
@NoArgsConstructor
public class ExportScanningConfigController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(ExportScanningConfigController.class);
    
    private UserData userData;
    private ScanningConfigService scanningConfigService;

    @Inject
    public ExportScanningConfigController(UserData userData, ScanningConfigService scanningConfigService) {
        this.userData = userData;
        this.scanningConfigService = scanningConfigService;
    }
    
    public void export() {
        execute(() -> {
            try {
                byte[] output = scanningConfigService.exportScanningConfigAsExcelFile(userData, userData.getElectionEventPk());
                FacesUtil.sendFile("scanningconfig.xlsx", output);
            } catch (IOException e) {
                String md5 = ErrorPageRenderer.md5(e.getMessage());
                LOGGER.warn("Scanning Configuration export failed #" + md5, e);
                MessageUtil.buildDetailMessage("@rbac.import_export.export_operators.ioexception", new String[] { md5 }, FacesMessage.SEVERITY_ERROR);
            }
        });
    }
    
}
