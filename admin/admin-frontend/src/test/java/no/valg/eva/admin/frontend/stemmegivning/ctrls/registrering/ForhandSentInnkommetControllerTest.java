package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.dialog.Dialog;

import org.testng.annotations.Test;

public class ForhandSentInnkommetControllerTest extends BaseFrontendTest {

	@Test
	public void getStemmegivningsType_returnererSentInnkommet() throws Exception {
		ForhandSentInnkommetController ctrl = initializeMocks(ForhandSentInnkommetController.class);

		assertThat(ctrl.getStemmegivningsType()).isSameAs(FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER);
	}

	@Test
	public void isShowDialog_medPapirManntallOgIkkeAvkryssningsmanntallKjort_returnererTrue() throws Exception {
		ForhandSentInnkommetController ctrl = initializeMocks(ForhandSentInnkommetController.class);
		setIsShowDialog();

		assertThat(ctrl.isShowDialog()).isTrue();
	}

	@Test
	public void openConfirmLateValidationSearchDialog() throws Exception {
		ForhandSentInnkommetController ctrl = initializeMocks(new MyForhandSentInnkommetController());
		setIsShowDialog();

		ctrl.openConfirmLateValidationSearchDialog();

		verify_open(ctrl.getConfirmLateValidationSearchModal());
	}

	private class MyForhandSentInnkommetController extends ForhandSentInnkommetController {

		private Dialog dialog;

		@Override
		public Dialog getConfirmLateValidationSearchModal() {
			if (dialog == null) {
				dialog = createMock(Dialog.class);
			}
			return dialog;
		}
	}

	private void setIsShowDialog() throws Exception {
		MvArea stemmested = new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
		mockFieldValue("stemmested", stemmested);
		when(stemmested.getMunicipality().isElectronicMarkoffs()).thenReturn(false);
		when(stemmested.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(false);
	}

}
