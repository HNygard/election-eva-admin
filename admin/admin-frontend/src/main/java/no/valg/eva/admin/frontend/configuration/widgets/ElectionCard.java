package no.valg.eva.admin.frontend.configuration.widgets;

import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.configuration.models.ElectionCardModel;

public interface ElectionCard {

	boolean isAddressEditable();

	boolean isInfoTextEditable();

	boolean isRenderInfoText();

	ElectionCardModel getSelected();

	Dialog getEditAddressDialog();

	Dialog getConfirmElectionCardInfoTextOverwriteDialog();

	String getId();

	boolean isDoneStatus();
}
