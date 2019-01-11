package no.valg.eva.admin.frontend.rbac.ctrls;

import lombok.Data;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.common.UserAgent;
import no.valg.eva.admin.frontend.common.UserAgentParser;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.rbac.OperatorImportHelper;
import no.valg.eva.admin.frontend.rbac.SpreadSheetValidationException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

/**
 * Main operator admin controller. Responsible for page flow and and Excel upload.
 */
@Named
@ViewScoped
@Data
public class OperatorAdminController extends KontekstAvhengigController {

    static final String EARLY_VOTE_RECEIVER_TEMPLATE_PATH = "/resources/files/ImportAvForhandsstemmemottakere.xlsx";
    static final String VOTE_RECEIVER_AND_PP_RESPONSIBLE_TEMPLATE_PATH = "/resources/files/ImportAvStemmemottakerValgtingOgAnsvarligValglokale.xlsx";
    private static final int MAX_EXCEL_ERRORS = 10;

    @Inject
    private PageTitleMetaBuilder pageTitleMetaBuilder;
    @Inject
    private OperatorImportHelper importHelper;
    @Inject
    private OperatorListController listController;
    @Inject
    private UserAgentParser userAgentParser;
    @Inject
    private MessageProvider messageProvider;

    private UserAgent userAgent;
    private RbacView view;
    private AreaPath currentAreaPath;

    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        final KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();

        // No need to always load all users in the system    
        if (getUserData().isElectionEventAdminUser()) {
            oppsett.leggTil(geografi(LAND, FYLKESKOMMUNE, KOMMUNE));
        }
        return oppsett;
    }

    @Override
    public void initialized(Kontekst kontekst) {
        currentAreaPath = kontekst.getValggeografiSti().areaPath();
        userAgent = userAgentParser.parse(getFacesContext());
        initOperators();
        setView(RbacView.LIST);
    }

    private void initOperators() {
        listController.initOperatorListsInArea(currentAreaPath);
    }

    public void uploadAdvanceVoteReceivers(final FileUploadEvent fileUploadEvent) {
        if (execute(() -> {
            try {
                importHelper.importAdvanceVoteReceivers(new OperatorImportHelper.InputStreamWrapper() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return fileUploadEvent.getFile().getInputstream();
                    }
                });
            } catch (SpreadSheetValidationException e) {
                handleSpreadSheetValidationException(e);
                return;
            }
        })) {
            initOperators();
        }
    }

    public void uploadVoteReceiverAndPollingPlaceResponsibles(final FileUploadEvent fileUploadEvent) {
        if (execute(() -> {
            try {
                importHelper.importVoteReceiverAndPollingPlaceResponsibles(new OperatorImportHelper.InputStreamWrapper() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return fileUploadEvent.getFile().getInputstream();
                    }
                });
            } catch (SpreadSheetValidationException e) {
                handleSpreadSheetValidationException(e);
                return;
            }
        })) {
            initOperators();
        }
    }

    public StreamedContent getAdvanceVoteReceiversTemplate() {
        InputStream templateInputStream = getFacesContext().getExternalContext().getResourceAsStream(EARLY_VOTE_RECEIVER_TEMPLATE_PATH);
        return streamedContent(templateInputStream, messageProvider.get("@rbac.uploadAdvanceVoteReceivers.templateFileName") + ".xlsx");
    }

    public StreamedContent getVoteReceiverAndPollingPlaceResponsibleTemplate() {
        InputStream templateInputStream = getFacesContext().getExternalContext().getResourceAsStream(VOTE_RECEIVER_AND_PP_RESPONSIBLE_TEMPLATE_PATH);
        return streamedContent(templateInputStream, messageProvider.get("@rbac.uploadVoteReceiversAndPollingPlaceResponsible.templateName") + ".xlsx");
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        return pageTitleMetaBuilder.area(getUserData().getOperatorMvArea());
    }

    public boolean isMsieBelow10() {

        return userAgent.isMSIE() && (userAgent.isVersionHigherThan(1) && userAgent.isVersionLowerThan(10));

    }

    private void handleSpreadSheetValidationException(SpreadSheetValidationException e) {
        for (int i = 0; i < e.getErrors().size(); i++) {
            MessageUtil.buildFacesMessage(getFacesContext(), null, e.getErrors().get(i), null, FacesMessage.SEVERITY_ERROR);
            if (i == (MAX_EXCEL_ERRORS - 1)) {
                return;
            }
        }
    }

    private StreamedContent streamedContent(InputStream stream, String name) {
        return new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", name);
    }
}
