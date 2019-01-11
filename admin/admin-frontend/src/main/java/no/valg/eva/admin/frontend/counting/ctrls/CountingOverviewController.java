package no.valg.eva.admin.frontend.counting.ctrls;

import static com.codepoetics.protonpack.StreamUtils.zipWithIndex;
import static java.util.stream.Collectors.toList;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.APPROVED;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.service.CountingOverviewService;
import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewPanelModel;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewTabModel;
import no.valg.eva.admin.frontend.counting.view.mapper.CountingOverviewPanelModelMapper;
import no.valg.eva.admin.frontend.counting.view.mapper.CountingOverviewTabModelMapper;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

import org.primefaces.event.TabChangeEvent;

import com.codepoetics.protonpack.Indexed;

@Named
@ViewScoped
public class CountingOverviewController extends KontekstAvhengigController {
	private static final String REPORT_TO_MEDIA_URL = "/secure/reporting/statistics/evaResultatRapportering.xhtml";

	@Inject
	private CountingOverviewService countingOverviewService;
	@Inject
	private CountingOverviewTabModelMapper countingOverviewTabModelMapper;
	@Inject
	private CountingOverviewPanelModelMapper countingOverviewPanelModelMapper;
	@Inject
	private ValgnattReportService valgnattReportService;
	@Inject
	protected MessageProvider messageProvider;
	
	private FromRequest fromRequest;
	private Kontekst data;
	private AreaPath pickerAreaPath;
	private List<CountingOverviewTabModel> tabs;
	private int activeTabIndex;
	private List<CountingOverviewPanelModel> panels;
	private String activePanelIndeces;
	private AreaPath areaPath;
	private String tekstForRapporteringsknapp;
	private Boolean altErRapportert;
	private Long antallRapporterbare;
	private Boolean ingenTellingerForDetteOmraadet;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		if (getUserData().isElectionEventAdminUser()) {
			setup.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		}
		return setup;
	}

	@Override
	public void initialized(Kontekst data) {
		this.data = data;
		this.pickerAreaPath = pickerAreaPath();
		initTabs();
		initPanels();
	}

	@Override
	@PostConstruct
	public void init() {
		this.fromRequest = fromRequest();
		this.pickerAreaPath = pickerAreaPath();
		if (fromRequest == null) {
			super.init();
		} else {
			initTabs();
			initPanels();
		}
		sjekkOmSidenErTom();
	}

	private void sjekkOmSidenErTom() {
		this.ingenTellingerForDetteOmraadet = activeTab() == null;
		
		if (ingenTellingerForDetteOmraadet) {
			String sti = data != null ? data.getValggeografiSti().toString() : "";
			MessageUtil.buildDetailMessage(messageProvider.get("@count.overview.ingenTellingerSkalRegistreresForOmraade", sti), SEVERITY_WARN);
		}
	}

	private FromRequest fromRequest() {
		CountCategory category = getRequestParameter("category", CountCategory::valueOf);
		areaPath = getRequestParameter("areaPath", AreaPath::from);
		ElectionPath pickerElectionPath = getRequestParameter("pickerElectionPath", ElectionPath::from);
		AreaPath pickerAreaPathFromRequest = getRequestParameter("pickerAreaPath", AreaPath::from);
		if (category != null && areaPath != null && pickerElectionPath != null && pickerAreaPathFromRequest != null) {
			return new FromRequest(category, areaPath, pickerElectionPath, pickerAreaPathFromRequest);
		}
		return null;
	}

	private AreaPath pickerAreaPath() {
		if (data != null) {
			return data.getValggeografiSti().areaPath();
		}
		return fromRequest != null ? fromRequest.pickerAreaPath : null;
	}

	private void initTabs() {
		this.tabs = tabs();
		this.activeTabIndex = activeTabIndex();
	}

	public List<CountingOverviewTabModel> tabs() {
		return countingOverviewService
				.electionsFor(getUserData(), pickerAreaPath())
				.stream()
				.map(countingOverviewTabModelMapper::countingOverviewTabModel)
				.collect(toList());
	}

	private int activeTabIndex() {
		return (int) zipWithIndex(tabs.stream())
				.filter(activeTabPredicate())
				.mapToLong(Indexed::getIndex)
				.findFirst()
				.orElse(0);
	}

	private Predicate<? super Indexed<CountingOverviewTabModel>> activeTabPredicate() {
		if (fromRequest != null) {
			return tab -> tab.getValue().matchesElectionPath(fromRequest.pickerElectionPath);
		}
		return tab -> false;
	}

	private void initPanels() {
		this.panels = panels();
		this.activePanelIndeces = activePanelIndeces();
		expandTreeFromRequest();
	}

	public List<CountingOverviewPanelModel> panels() {
		CountingOverviewTabModel activeTab = activeTab();
		if (activeTab == null) {
			return Collections.emptyList();	
		}
		
		ElectionPath electionPath = activeTab.getElectionPath();
		ReportingUnitTypeId reportingUnitTypeId = getUserData().isElectionEventAdminUser() && pickerAreaPath.isCountyLevel() ? FYLKESVALGSTYRET : null;
		return countingOverviewService.countingOverviewsFor(getUserData(), electionPath, pickerAreaPath)
				.stream()
				.map(countingOverviewRoot -> countingOverviewPanelModelMapper
						.countingOverviewPanelModel(countingOverviewRoot, reportingUnitTypeId, pickerAreaLevel()))
				.collect(toList());
	}

	private AreaLevelEnum pickerAreaLevel() {
		AreaLevelEnum pickerAreaLevel;
		if (getUserData().isFylkesvalgstyret() || getUserData().isElectionEventAdminUser() && pickerAreaPath.isCountyLevel()) {
			pickerAreaLevel = COUNTY;
		} else {
			pickerAreaLevel = MUNICIPALITY;
		}
		return pickerAreaLevel;
	}

	private String activePanelIndeces() {
		if (panels.size() == 1) {
			return "0";
		}
		if (fromRequest != null && activeTab().matchesElectionPath(fromRequest.pickerElectionPath)) {
			return zipWithIndex(panels.stream())
					.filter(indexedPanel -> indexedPanel.getValue().includesAreaPath(fromRequest.areaPath))
					.map(panel -> String.valueOf(panel.getIndex()))
					.findFirst()
					.orElse("");
		}
		return "";
	}

	private void expandTreeFromRequest() {
		if (fromRequest != null && activeTab().matchesElectionPath(fromRequest.pickerElectionPath)) {
			panels.forEach(this::expandPanelIfMatched);
		}
	}

	private CountingOverviewTabModel activeTab() {
		if (tabs == null || tabs.isEmpty()) {
			return null;
		}
		return tabs.get(activeTabIndex);
	}

	private void expandPanelIfMatched(CountingOverviewPanelModel panel) {
		panel.expandTreeIfMatched(fromRequest.category, fromRequest.areaPath);
	}

	public int getActiveTabIndex() {
		return activeTabIndex;
	}

	public void setActiveTabIndex(int activeTabIndex) {
		this.activeTabIndex = activeTabIndex;
	}

	public List<CountingOverviewTabModel> getTabs() {
		return tabs;
	}

	public void onTabChange(TabChangeEvent event) {
		CountingOverviewTabModel tab = (CountingOverviewTabModel) event.getData();
		this.activeTabIndex = tabs.indexOf(tab);
		initPanels();
		tekstForRapporteringsknapp = null;
		altErRapportert = null;
		antallRapporterbare = null;
	}

	public List<CountingOverviewPanelModel> getPanels() {
		return panels;
	}

	public String getActivePanelIndeces() {
		return activePanelIndeces;
	}

	public void setActivePanelIndeces(String activePanelIndeces) {
		this.activePanelIndeces = activePanelIndeces;
	}

	public String getReportToMediaUrl() {
		return addRedirect(REPORT_TO_MEDIA_URL + "?areaPath=" + areaPath);
	}

	public boolean isStatusSummaryRenderedFor(CountingOverviewRoot countingOverviewRoot) {
		if (panels.size() == 1) {
			return false;
		}
		Status status = countingOverviewRoot.getStatus();
		return !(status instanceof CountingStatus) || ((CountingStatus) status).getValue() != APPROVED;
	}

	/**
	 * Viser knapp hvis:
	 * <ul>
	 * <li>det ikke er bydelsvalg, fylket kan rapportere og fylket er valgt</li>
	 * <li>ikke fylket kan rapportere og kommune er valgt og det ligger noe</li>
	 * <li>klart som kan rapporteres</li>
	 * </ul>
	 */
	boolean aktiverRapporteringsknapp() {
		if (tabs == null) {
			return false;
		}
		if (!skalRapportere()) {
			return false;
		}
		boolean kanFylketRapportere = valgnattReportService.kanFylketRapportere(getUserData(), activeTab().getElectionPath());
		if (pickerAreaLevel() == COUNTY) {
			return kanFylketRapportere;
		} else if (kanFylketRapportere) {
			return false;
		}
		return antallRapporterbare() > 0 || altErRapportert();
	}

	private long antallRapporterbare() {
		AreaPath reportingAreaPath = reportingAreaPath();
		if (reportingAreaPath != null && antallRapporterbare == null) {
			antallRapporterbare = valgnattReportService.antallRapporterbare(getUserData(), activeTab().getElectionPath(), reportingAreaPath);
			return antallRapporterbare;
		}
		return antallRapporterbare != null ? antallRapporterbare : 0;
	}

	private boolean altErRapportert() {
		AreaPath reportingAreaPath = reportingAreaPath();
		if (reportingAreaPath != null && altErRapportert == null) {
			altErRapportert = valgnattReportService.altErRapportert(getUserData(), activeTab().getElectionPath(), reportingAreaPath);
			return altErRapportert;
		}
		return altErRapportert != null ? altErRapportert : false;
	}

	private AreaPath reportingAreaPath() {
		return pickerAreaPath != null ? pickerAreaPath : getUserData().getOperatorAreaPath();
	}

	/**
	 * En særegenhet ved koden som henter ut contest info er at det er bydelsvalg som ikke rapporterer hvis ElectionPath er på valg nivå. Dette kan gjerne
	 * implementeres mer robust og tydelig i en senere versjon.
	 */
	private boolean skalRapportere() {
		CountingOverviewTabModel activeTab = activeTab();
		if (activeTab == null) {
			return false;
		}
		
		return activeTab.getElectionPath().getLevel() != ElectionLevelEnum.ELECTION;
	}

	public String getTekstForRapporteringsknapp() {
		if (tabs == null || tabs.isEmpty()) {
			return "";
		}
		if (tekstForRapporteringsknapp == null) {
			if (skalRapportere() && altErRapportert()) {
				tekstForRapporteringsknapp = getMessageProvider().get("@counting.ferdig_rapportert");
			} else {
				tekstForRapporteringsknapp = getMessageProvider().get("@counting.n_rapporter_til_media", antallRapporterbare());
			}
		}
		return tekstForRapporteringsknapp;
	}

	public String getRapporterKnappStil() {
		if (tabs == null || tabs.isEmpty()) {
			return "";
		}
		return skalRapportere() && altErRapportert() ? "btn-success" : "btn-primary";
	}

	private static class FromRequest implements Serializable {
		private final CountCategory category;
		private final AreaPath areaPath;
		private final ElectionPath pickerElectionPath;
		private final AreaPath pickerAreaPath;

		FromRequest(CountCategory category, AreaPath areaPath, ElectionPath pickerElectionPath, AreaPath pickerAreaPath) {
			this.category = category;
			this.areaPath = areaPath;
			this.pickerElectionPath = pickerElectionPath;
			this.pickerAreaPath = pickerAreaPath;
		}
	}

	public Boolean getIngenTellingerForDetteOmraadet() {
		return ingenTellingerForDetteOmraadet;
	}
}
