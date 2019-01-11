package no.valg.eva.admin.frontend.opptelling;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.opptelling.service.OpptellingService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ViewScoped
public class SlettOpptellingerController extends KontekstAvhengigController {
	private ValghierarkiService valghierarkiService;
	private OpptellingService opptellingService;

	private Valghierarki valgtValghierarki;
	private Valggeografi valgtValggeografi;
	private CountCategory[] valgteCountCategories;
	private Styretype[] valgteStyretyper;
	private boolean slettetOpptellinger;

	@SuppressWarnings("unused")
	public SlettOpptellingerController() {
		// CDI
	}

	@Inject
	public SlettOpptellingerController(ValghierarkiService valghierarkiService, OpptellingService opptellingService) {
		this.valghierarkiService = valghierarkiService;
		this.opptellingService = opptellingService;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE, VALG, VALGDISTRIKT));
		setup.leggTil(geografi(valggeografiNivaaer()));
		return setup;
	}

	private ValggeografiNivaa[] valggeografiNivaaer() {
		List<ValggeografiNivaa> result = new ArrayList<>(asList(BYDEL, STEMMEKRETS));
		int userLevel = getUserData().getOperatorAreaLevel().getLevel();
		if (userLevel <= KOMMUNE.nivaa()) {
			result.add(0, KOMMUNE);
		}
		if (userLevel <= FYLKESKOMMUNE.nivaa()) {
			result.add(0, FYLKESKOMMUNE);
		}
		if (userLevel <= LAND.nivaa()) {
			result.add(0, LAND);
		}
		if (getUserData().hasAccess(Accesses.Admin)) {
			result.add(0, VALGHENDELSE);
		}
		return result.toArray(new ValggeografiNivaa[result.size()]);
	}

	@Override
	public void initialized(Kontekst kontekst) {
		ValghierarkiSti valgtValghierarkiSti = kontekst.getValghierarkiSti();
		valgtValghierarki = valghierarkiService.valghierarki(valgtValghierarkiSti);
		ValggeografiSti valgtValggeografiSti = kontekst.getValggeografiSti();
		valgtValggeografi = valggeografiService.valggeografi(valgtValggeografiSti);
	}

	public boolean isSlettetOpptellinger() {
		return slettetOpptellinger;
	}

	public void slettOpptellinger() {
		execute(this::kallTjeneste);
	}

	private void kallTjeneste() {
		opptellingService.slettOpptellinger(getUserData(), valgtValghierarki.sti(), valgtValggeografi.sti(), valgteCountCategories, valgteStyretyper);
		MessageUtil.buildFacesMessage(getFacesContext(), null,
				"@delete.vote_counts.confirmation",
				new String[]{valgtValghierarki.navn(), valgtValggeografi.navn()},
				FacesMessage.SEVERITY_INFO);
		slettetOpptellinger = true;
	}

	public String getValghierakiNavn() {
		return valgtValghierarki.navn();
	}

	public String getValggeografiNavn() {
		return valgtValggeografi.navn();
	}

	public CountCategory[] getCountCategories() {
		return CountCategory.values();
	}

	public CountCategory[] getValgteCountCategories() {
		return valgteCountCategories;
	}

	public void setValgteCountCategories(CountCategory[] valgteCountCategories) {
		if (valgteCountCategories == null) {
			this.valgteCountCategories = null;
		} else {
			this.valgteCountCategories = copyOf(valgteCountCategories, valgteCountCategories.length);
		}
	}

	public Styretype[] getStyretyper() {
		return Styretype.values();
	}

	public Styretype[] getValgteStyretyper() {
		return valgteStyretyper;
	}

	public void setValgteStyretyper(Styretype[] valgteStyretyper) {
		if (valgteStyretyper == null) {
			this.valgteStyretyper = null;
		} else {
			this.valgteStyretyper = copyOf(valgteStyretyper, valgteStyretyper.length);
		}
	}
}
