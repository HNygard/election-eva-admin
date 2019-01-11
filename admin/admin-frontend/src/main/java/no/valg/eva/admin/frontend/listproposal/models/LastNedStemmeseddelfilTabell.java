package no.valg.eva.admin.frontend.listproposal.models;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;

public class LastNedStemmeseddelfilTabell {

	private LastNedStemmeseddelfilPanel panel;
	private Object nivaa;
	private List<? extends Object> rader = new ArrayList<>();
	private Object valgtRad;

	public LastNedStemmeseddelfilTabell(LastNedStemmeseddelfilPanel panel, Object nivaa) {
		this.panel = panel;
		this.nivaa = nivaa;
	}

	public void setValgtRad(Object valgtRad) {
		if (valgtRad != null && valgtRad.equals(this.valgtRad)) {
			return;
		}
		this.valgtRad = valgtRad;
		if (isHierarki()) {
			if (hierarki(valgtRad).nivaa() == VALGGRUPPE) {
				panel.initValg();
			} else if (hierarki(valgtRad).nivaa() == VALG) {
				if (panel.isValgPaaKommuneNiva()) {
					panel.initFylker();
				} else {
					panel.initValgdistrikt();
				}
			}
		} else {
			if (geografi(valgtRad).nivaa() == FYLKESKOMMUNE) {
				panel.initValgdistrikt();
			}
		}
	}

	public String getNavn() {
		if (isHierarki()) {
			return ((ValghierarkiNivaa) nivaa).visningsnavn();
		}
		return ((ValggeografiNivaa) nivaa).visningsnavn();
	}

	public Object getValgtRad() {
		return valgtRad;
	}

	public boolean isVisTabell() {
		return nivaa == VALGDISTRIKT || getRader().size() > 1;
	}

	public boolean isVisKnapp() {
		return nivaa == VALGDISTRIKT;
	}

	public boolean isKnappDeaktivert() {
		return !isVisKnapp() || valgtRad == null || !panel.isIndikatorOK((Valgdistrikt) valgtRad);
	}

	public Object getId() {
		return nivaa;
	}

	public void setRader(List<? extends Object> rader) {
		this.rader = rader;
	}

	public List<? extends Object> getRader() {
		return rader;
	}

	private boolean isHierarki() {
		return nivaa instanceof ValghierarkiNivaa;
	}

	private Valghierarki hierarki(Object o) {
		return (Valghierarki) o;
	}

	private Valggeografi geografi(Object o) {
		return (Valggeografi) o;
	}

}
