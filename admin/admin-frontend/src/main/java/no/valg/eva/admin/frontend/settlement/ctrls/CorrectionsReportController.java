package no.valg.eva.admin.frontend.settlement.ctrls;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import no.evote.presentation.exceptions.ErrorPageRenderer;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.service.ExportCandidateVotesService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

@Named
@ViewScoped
@Log4j
@NoArgsConstructor
public class CorrectionsReportController extends KontekstAvhengigController {
    
    @Inject
    private UserData userData;
    @Inject
    private ExportCandidateVotesService exportCandidateVotesService;
    @Inject
    private MessageProvider messageProvider;
    @Getter
    private boolean exportButtonDisabled;
    
    private MvArea mvArea;
    private MvElection mvElection;
    
    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        if (getUserData().isElectionEventAdminUser()) {
            setup.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
        }
        setup.leggTil(hierarki(ValghierarkiNivaa.VALGDISTRIKT));

        return setup;
    }

    @Override
    public void initialized(Kontekst context) {
        mvArea = getMvAreaService().findSingleByPath(context.getValggeografiSti());
        mvElection = getMvElectionService().findSingleByPath(context.getValghierarkiSti());
        validateAreaAndElectionLevel();
    }

    private void validateAreaAndElectionLevel() {
        exportButtonDisabled = mvElection.getElection().getAreaLevel() != mvArea.getAreaLevel();
        if (exportButtonDisabled) {
            MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.invalid_level"), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void export() {
        execute(() -> {
            try {
                FacesUtil.sendFile("rettelser.xlsx", 
                        exportCandidateVotesService.exportCandidateVotes(userData, AreaPath.from(mvArea.getAreaPath()), ElectionPath.from(mvElection.getElectionPath())));
            } catch (IOException e) {
                String md5 = ErrorPageRenderer.md5(e.getMessage());
                log.warn("Corrections export failed #" + md5, e);
                MessageUtil.buildDetailMessage("@rbac.import_export.export_operators.ioexception", new String[] { md5 }, FacesMessage.SEVERITY_ERROR);
            }
        });        
    }
}
