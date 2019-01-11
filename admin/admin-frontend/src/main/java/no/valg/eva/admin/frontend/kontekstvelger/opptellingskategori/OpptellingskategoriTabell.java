package no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori;

import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad.kontekstvelgerRader;

import java.util.List;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.panel.OpptellingskategoriPanel;

public class OpptellingskategoriTabell extends KontekstvelgerTabell<OpptellingskategoriPanel, OpptellingskategoriRad, String> {
	private final ValgSti valgSti;

	public OpptellingskategoriTabell(OpptellingskategoriPanel panel, ValgSti valgSti) {
		super(panel, false);
		this.valgSti = valgSti;
	}

	@Override
	protected void valgtRadSatt() {
		// gjør ingenting
	}

	@Override
	public void oppdater() {
		List<CountCategory> countCategories;
		if (valgSti != null) {
			countCategories = getPanel().getOpptellingskategoriService().countCategoriesForValgSti(getPanel().getUserData(), valgSti);
		} else {
			countCategories = getPanel().getOpptellingskategoriService().countCategories(getPanel().getUserData());
		}
		setRader(kontekstvelgerRader(countCategories, OpptellingskategoriRad::new));
	}

	@Override
	public String getId() {
		return "OPPTELLINGSKATEGORI";
	}

	@Override
	public String getNavn() {
		return "@count.ballot.approve.rejected.category";
	}

	@Override
	public boolean isVisKnapp() {
		return true;
	}

	public CountCategory valgtCountCategory() {
		if (isRadValgt()) {
			return getValgtRad().getCountCategory();
		}
		return null;
	}
}
