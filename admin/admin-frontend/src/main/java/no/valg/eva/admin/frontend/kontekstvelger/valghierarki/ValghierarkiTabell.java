package no.valg.eva.admin.frontend.kontekstvelger.valghierarki;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad.kontekstvelgerRader;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;

public abstract class ValghierarkiTabell<S extends ValghierarkiSti, V extends Valghierarki<S>>
		extends KontekstvelgerTabell<ValghierarkiPanel, ValghierarkiRad<S>, ValghierarkiNivaa> {
	private final ValghierarkiNivaa nivaa;
	private final ValghierarkiService valghierarkiService;
	private final UserData userData;

	public ValghierarkiTabell(ValghierarkiPanel panel, ValghierarkiNivaa nivaa, ValghierarkiService valghierarkiService, UserData userData) {
		super(panel, false);
		this.nivaa = nivaa;
		this.valghierarkiService = valghierarkiService;
		this.userData = userData;
	}

	@Override
	public ValghierarkiNivaa getId() {
		return nivaa;
	}

	@Override
	public String getNavn() {
		return nivaa.visningsnavn();
	}

	@Override
	public boolean isVisKnapp() {
		return getPanel().visKnapp(nivaa);
	}

	public S valgtSti() {
		ValghierarkiRad<S> valgtRad = getValgtRad();
		if (valgtRad != null) {
			return valgtRad.getSti();
		}
		return null;
	}

	protected ValghierarkiService getValghierarkiService() {
		return valghierarkiService;
	}

	protected UserData getUserData() {
		return userData;
	}

	void initIngenRader() {
		setRader(emptyList());
	}

	void initEnRad(V valghierarki) {
		setRader(singletonList(new ValghierarkiRad<>(valghierarki)));
	}

	void initFlereRader(List<V> valghierarkiListe) {
		setRader(kontekstvelgerRader(valghierarkiListe, ValghierarkiRad::new));
	}
}
