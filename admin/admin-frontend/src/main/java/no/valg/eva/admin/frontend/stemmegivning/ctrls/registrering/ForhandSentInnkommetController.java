package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import static no.valg.eva.admin.frontend.common.dialog.Dialogs.CONFIRM_LATE_VALIDATION_SEARCH;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;

/**
 * Controller for forhåndstemmer sent innkommet.
 */
@Named
@ViewScoped
public class ForhandSentInnkommetController extends ForhandKonvolutterSentraltController {

    private boolean dialogShown;

    @Override
    public StemmegivningsType getStemmegivningsType() {
        return FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;
    }

    public boolean isShowDialog() {
		return !dialogShown && !getStemmested().getMunicipality().isAvkrysningsmanntallKjort() && !getStemmested().getMunicipality().isElectronicMarkoffs();
    }

    public void openConfirmLateValidationSearchDialog() {
        if (isShowDialog()) {
            getConfirmLateValidationSearchModal().open();
            dialogShown = true;
        }
    }

    public Dialog getConfirmLateValidationSearchModal() {
        return CONFIRM_LATE_VALIDATION_SEARCH;
    }
}
