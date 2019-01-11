package no.valg.eva.admin.common.configuration.service;

import no.evote.security.UserData;

import java.io.Serializable;

public interface ScanningConfigService extends Serializable {
    byte[] exportScanningConfigAsExcelFile(UserData userData, Long electionEventPk);
}
