package no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerRad;

public class OpptellingskategoriRad extends KontekstvelgerRad {
	private CountCategory countCategory;

	public OpptellingskategoriRad(CountCategory countCategory) {
		super(countCategory.getId(), false, countCategory.messageProperty());
		this.countCategory = countCategory;
	}

	public CountCategory getCountCategory() {
		return countCategory;
	}
}
