package no.valg.eva.admin.frontend.listproposal.models;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.frontend.listproposal.ctrls.LastNedStemmeseddelfilController;


public class LastNedStemmeseddelfilPanel {

	private List<LastNedStemmeseddelfilTabell> tabeller = new ArrayList<>();
	private Map<ValgSti, Set<ValgdistriktSti>> indikatorCache = new HashMap<>();
	private LastNedStemmeseddelfilController controller;

	public LastNedStemmeseddelfilPanel(LastNedStemmeseddelfilController controller) {
		this.controller = controller;
		initValgGrupper();
	}

	public boolean isIndikatorOK(Valgdistrikt valgdistrikt) {
		if (valgdistrikt == null) {
			return false;
		}
		Set<ValgdistriktSti> approved;
		if (!indikatorCache.containsKey(getValgSti())) {
			approved = new HashSet<>(
					controller.getMvAreaService().findValgdistriktStierByValgStiWhereAllListProposalsAreApproved(controller.getUserData(), getValgSti()));
			indikatorCache.put(getValgSti(), approved);
		} else {
			approved = indikatorCache.get(getValgSti());
		}
		return approved.contains(valgdistrikt.sti());
	}

	private void initValgGrupper() {
		tabeller = new ArrayList<>();
		List<Valggruppe> valgGrupper = controller.getValghierarkiService().valggrupper(controller.getUserData());
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(this, VALGGRUPPE);
		tabell.setRader(valgGrupper);
		tabeller.add(tabell);
		if (valgGrupper.size() == 1) {
			tabell.setValgtRad(valgGrupper.get(0));
		}
	}

	private ValggruppeSti getValggruppeSti() {
		return ((Valggruppe) tabeller.get(0).getValgtRad()).sti();
	}

	void initValg() {
		while (tabeller.size() > 1) {
			tabeller.remove(tabeller.size() - 1);
		}

		List<Valg> valg = controller.getValghierarkiService().valg(controller.getUserData(), getValggruppeSti(), null);
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(this, VALG);
		tabell.setRader(valg);
		tabeller.add(tabell);
		if (valg.size() == 1) {
			tabell.setValgtRad(valg.get(0));
		}
	}

	public Valg getValg() {
		return (Valg) tabeller.get(1).getValgtRad();
	}

	private ValgSti getValgSti() {
		return getValg().sti();
	}

	boolean isValgPaaKommuneNiva() {
		return getValg().valggeografiNivaa() == KOMMUNE && getValg().isEnkeltOmrade();
	}

	void initFylker() {
		if (isValgPaaKommuneNiva()) {
			while (tabeller.size() > 2) {
				tabeller.remove(tabeller.size() - 1);
			}
			List<Fylkeskommune> fylker = controller.getValggeografiService().fylkeskommuner(controller.getUserData(), getValggruppeSti().valghendelseSti(), null);
			fylker = fylker
					.stream()
					.sorted((fylke1, fylke2) -> fylke1.id().compareTo(fylke2.id()))
					.collect(Collectors.toList());
			LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(this, FYLKESKOMMUNE);
			tabell.setRader(fylker);
			tabeller.add(tabell);
			if (fylker.size() == 1) {
				tabell.setValgtRad(fylker.get(0));
			}
		}
	}

	private Fylkeskommune getFylkeskommune() {
		return (Fylkeskommune) tabeller.get(2).getValgtRad();
	}

	void initValgdistrikt() {
		int sizeLimit = isValgPaaKommuneNiva() ? 3 : 2;
		while (tabeller.size() > sizeLimit) {
			tabeller.remove(tabeller.size() - 1);
		}
		List<Valgdistrikt> valgdistrikter;
		if (isValgPaaKommuneNiva()) {
			valgdistrikter = controller.getValghierarkiService().valgdistrikter(controller.getUserData(), getValgSti(), getFylkeskommune().sti());
		} else {
			valgdistrikter = controller.getValghierarkiService().valgdistrikter(controller.getUserData(), getValgSti());
		}
		valgdistrikter = valgdistrikter
				.stream()
				.sorted((valg1, valg2) -> valg1.id().compareTo(valg2.id()))
				.collect(Collectors.toList());
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(this, VALGDISTRIKT);
		tabell.setRader(valgdistrikter);
		tabeller.add(tabell);
		if (valgdistrikter.size() == 1) {
			tabell.setValgtRad(valgdistrikter.get(0));
		}
	}

	public Valgdistrikt getValgdistrikt() {
		if (isValgPaaKommuneNiva()) {
			return (Valgdistrikt) tabeller.get(3).getValgtRad();
		} else {
			return (Valgdistrikt) tabeller.get(2).getValgtRad();
		}
	}

	public List<LastNedStemmeseddelfilTabell> getTabeller() {
		return tabeller;
	}

}

