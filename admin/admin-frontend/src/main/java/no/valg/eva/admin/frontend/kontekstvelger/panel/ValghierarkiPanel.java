package no.valg.eva.admin.frontend.kontekstvelger.panel;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.valgbareValghierarkiNivaaerFra;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.valghierarkiTjeneste;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.DEFAULT;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.felles.valghierarki.service.ListeforslagValghierarkiService;
import no.valg.eva.admin.felles.valghierarki.service.SlettValgoppgjoerValghierarkiService;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValgTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValgdistrikterTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValggrupperTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghendelseTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste;

@ViewScoped
public class ValghierarkiPanel implements KontekstvelgerPanel<ValghierarkiTabell, ValghierarkiSti> {
	private KontekstvelgerController controller;
	private OpptellingskategoriPanel opptellingskategoriPanel;
	private UserData userData;
	private ValghierarkiService valghierarkiService;
	private ValghierarkiService slettValgoppgjoerValghierarkiService;
	private ValghierarkiService listeforslagValghierarkiService;
	private ValghierarkiTjeneste valghierarkiTjeneste = DEFAULT;
	private Set<ValghierarkiNivaa> valgbareValghierarkiNivaaer;
	private List<ValghierarkiNivaa> valghierarkiNivaaer;

	private ValghendelseTabell valghendelseTabell;
	private ValggrupperTabell valggrupperTabell;
	private ValgTabell valgTabell;
	private ValgdistrikterTabell valgdistrikterTabell;

	private boolean valgbar;
	private ValghierarkiSti valgtValghierarkiSti;

	public ValghierarkiPanel() {
		// CDI
	}

	@Inject
	public ValghierarkiPanel(KontekstvelgerController controller,
							 OpptellingskategoriPanel opptellingskategoriPanel,
							 UserData userData,
							 ValghierarkiService valghierarkiService,
							 @SlettValgoppgjoerValghierarkiService ValghierarkiService slettValgoppgjoerValghierarkiService,
							 @ListeforslagValghierarkiService ValghierarkiService listeforslagValghierarkiService) {
		this.controller = controller;
		this.opptellingskategoriPanel = opptellingskategoriPanel;
		this.userData = userData;
		this.valghierarkiService = valghierarkiService;
		this.slettValgoppgjoerValghierarkiService = slettValgoppgjoerValghierarkiService;
		this.listeforslagValghierarkiService = listeforslagValghierarkiService;
	}

	public static String navn(ValghierarkiNivaa valgbartNivaa) {
		return "@election_level[" + valgbartNivaa.nivaa() + "].name";
	}

	@Override
	public void initOppsett(KontekstvelgerOppsett oppsett) {
		valghierarkiTjeneste = valghierarkiTjeneste(oppsett);
		List<ValghierarkiNivaa> valgbareValghierarkiNivaaerFraOppsett = valgbareValghierarkiNivaaerFra(oppsett);
		valgbar = valgbareValghierarkiNivaaerFraOppsett != null;
		if (!valgbar) {
			return;
		}
		this.valgbareValghierarkiNivaaer = EnumSet.copyOf(valgbareValghierarkiNivaaerFraOppsett);
	}

	@Override
	public void initTabeller(Kontekst kontekst) {
		if (valghierarkiStiFra(kontekst) == null) {
			this.valghierarkiNivaaer = valghierarkiNivaerForVising(valgbareValghierarkiNivaaer);
			initTabeller();
            valgtValghierarkiSti = lastSelectedElectionPath();
		} else {
			valgtValghierarkiSti = valghierarkiStiFra(kontekst);
		}
	}

	private ValghierarkiSti valghierarkiStiFra(Kontekst kontekst) {
		return kontekst != null ? kontekst.getValghierarkiSti() : null;
	}

	private List<ValghierarkiNivaa> valghierarkiNivaerForVising(Set<ValghierarkiNivaa> selectableElectionHierarchyLevels) {
		ValghierarkiNivaa lastSelectableElectionHierarchyLevel = selectableElectionHierarchyLevels
				.stream()
				.skip(selectableElectionHierarchyLevels.size() - 1L)
				.findFirst()
				.orElse(null);

		return ValghierarkiNivaa.listIncluding(lastSelectableElectionHierarchyLevel);
	}

	private void initTabeller() {
		if (valghierarkiNivaaer.contains(VALGHENDELSE)) {
			valghendelseTabell = new ValghendelseTabell(this, getValghierarkiService(), userData);
		}
		if (valghierarkiNivaaer.contains(VALGGRUPPE)) {
			valggrupperTabell = new ValggrupperTabell(this, getValghierarkiService(), userData);
		}
		if (valghierarkiNivaaer.contains(VALG)) {
			CountCategory selectedCountCategory = opptellingskategoriPanel.valgtVerdi();
			valgTabell = new ValgTabell(this, getValghierarkiService(), userData, selectedCountCategory);
		}
		if (valghierarkiNivaaer.contains(VALGDISTRIKT)) {
			valgdistrikterTabell = new ValgdistrikterTabell(this, getValghierarkiService(), userData);
		}

		getTabeller().get(0).oppdater();
	}

    private ValghierarkiSti lastSelectedElectionPath() {
		List<ValghierarkiTabell> tabeller = getTabeller();
		return tabeller.get(tabeller.size() - 1).valgtSti();
	}

	@Override
	public ValghierarkiSti valgtVerdi() {
		return valgtValghierarkiSti;
	}

	public Valghierarki valgtValghierarki() {
		return valgtValghierarkiSti != null ? getValghierarkiService().valghierarki(valgtValghierarkiSti) : null;
	}

	@Override
	public boolean erValgbar() {
		return valgbar;
	}

	private ValghierarkiService getValghierarkiService() {
		if (valghierarkiTjeneste == ValghierarkiTjeneste.SLETT_VALGOPPGJOER) {
			return slettValgoppgjoerValghierarkiService;
		}
		if (valghierarkiTjeneste == ValghierarkiTjeneste.LISTEFORSLAG) {
			return listeforslagValghierarkiService;
		}
		return valghierarkiService;
	}

	@Override
	public String getId() {
		return "valghierarkiPanel";
	}

	@Override
	public String getNavn() {
		if (valgbareValghierarkiNivaaer.size() != 1) {
			return "@election.common.election_level";
		}
		ValghierarkiNivaa valgbartNivaa = valgbareValghierarkiNivaaer.stream().findFirst().orElse(null);
		return navn(valgbartNivaa);
	}

	@Override
	public List<ValghierarkiTabell> getTabeller() {
		return valghierarkiNivaaer.stream()
				.map(this::valghierarkiTabell)
				.collect(toList());
	}

	@Override
	public void velg(Object tabellId) {
		valgtValghierarkiSti = valghierarkiTabell((ValghierarkiNivaa) tabellId).valgtSti();
		controller.redirectTilUrlEllerInitNestePanel();
	}

	private ValghierarkiTabell valghierarkiTabell(ValghierarkiNivaa valghierarkiNivaa) {
		switch (valghierarkiNivaa) {
			case VALGHENDELSE:
				return valghendelseTabell;
			case VALGGRUPPE:
				return valggrupperTabell;
			case VALG:
				return valgTabell;
			case VALGDISTRIKT:
				return valgdistrikterTabell;
			default:
				throw new IllegalArgumentException(format("Ukjent nivå: %s", valghierarkiNivaa));
		}
	}

	public ValggrupperTabell getValggrupperTabell() {
		return valggrupperTabell;
	}

	public ValgTabell getValgTabell() {
		return valgTabell;
	}

	public boolean visKnapp(ValghierarkiNivaa valghierarkiNivaa) {
		return valgbareValghierarkiNivaaer.contains(valghierarkiNivaa);
	}

	public void oppdaterValggrupperTabell() {
		oppdaterTabell(valggrupperTabell);
	}

	public void oppdaterValgTabell() {
		oppdaterTabell(valgTabell);
	}

	public void oppdaterValgdistrikterTabell() {
		oppdaterTabell(valgdistrikterTabell);
	}

	private void oppdaterTabell(ValghierarkiTabell tabell) {
		Optional.ofNullable(tabell).ifPresent(ValghierarkiTabell::oppdater);
	}
}
