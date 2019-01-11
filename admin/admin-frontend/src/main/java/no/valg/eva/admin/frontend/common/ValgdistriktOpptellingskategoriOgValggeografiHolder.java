package no.valg.eva.admin.frontend.common;

import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerHjelp.kontekstvelgerURL;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.side;
import static no.valg.eva.admin.frontend.opptelling.ForkastedeOpptellingerController.initKontekstvelgerOppsett;

import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

@Named
@ConversationScoped
public class ValgdistriktOpptellingskategoriOgValggeografiHolder extends ConversationScopedController {
	@Inject
	private ContestInfoService contestInfoService;
	@Inject
	private MvAreaService mvAreaService;
	@Inject
	private MvElectionService mvElectionService;
	@Inject
	private PageTitleMetaBuilder pageTitleMetaBuilder;
	@Inject
	private MessageProvider messageProvider;

	private ElectionPath selectedElectionPath;
	private MvElection selectedMvElection;
	private ContestInfo selectedContestInfo;
	private CountCategory selectedCountCategory;
	private AreaPath selectedAreaPath;
	private MvArea selectedMvArea;
	private boolean fromOverview;
	private String countingOverviewURL;

	@Override
	protected void doInit() {
		fromOverview = getRequestParameter("fromOverview") != null;
		countingOverviewURL = "countingOverview.xhtml?" + getQueryString();
		String contestPath = getRequestParameter("contestPath");
		if (contestPath != null) {
			selectedElectionPath = ElectionPath.from(contestPath);
			selectedContestInfo = contestInfoService.findContestInfoByPath(selectedElectionPath);
			selectedMvElection = mvElectionService.findSingleByPath(selectedElectionPath);
		}
		String countCategoryId = getRequestParameter("category");
		if (countCategoryId != null) {
			selectedCountCategory = CountCategory.fromId(countCategoryId);
		}
		String areaPaths = getRequestParameter("areaPath");
		if (areaPaths != null) {
			selectedAreaPath = AreaPath.from(areaPaths);
			selectedMvArea = mvAreaService.findSingleByPath(selectedAreaPath);
		}
	}

	public AreaPath getSelectedAreaPath() {
		return selectedAreaPath;
	}

	public ContestInfo getSelectedContestInfo() {
		return selectedContestInfo;
	}

	public CountCategory getSelectedCountCategory() {
		return selectedCountCategory;
	}

	public MvArea getSelectedMvArea() {
		return selectedMvArea;
	}

	public List<PageTitleMetaModel> getElectionPageTitleMeta() {
		List<PageTitleMetaModel> result = pageTitleMetaBuilder.election(selectedMvElection);
		result.addAll(pageTitleMetaBuilder.countCategory(selectedCountCategory));
		return result;
	}

	public String getElectionProviderHeader() {
		return messageProvider.get("@common.choose") + " " + messageProvider.get("@election_level[2].name").toLowerCase();
	}

	public String getCountCategoryProviderHeader() {
		return messageProvider.get("@common.choose") + " " + messageProvider.get("@count.ballot.approve.rejected.category").toLowerCase();
	}

	public String getAreaProviderHeader() {
		AreaLevelEnum areaLevelEnum = selectedMvArea.getActualAreaLevel();
		return messageProvider.get("@common.choose") + " " + messageProvider.get("@area_level[" + areaLevelEnum.getLevel() + "].name").toLowerCase();
	}

	public boolean isElectionPathSelected() {
		return selectedElectionPath != null;
	}

	public boolean isCountCategorySelected() {
		return selectedCountCategory != null;
	}

	public boolean isAreaPathSelected() {
		return selectedAreaPath != null;
	}

	public String getVelgValgUrl() {
		return kontekstvelgerURL(kontekstVelgerOppsett());
	}

	public String getVelgOpptelingskategoriUrl() {
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(valgtValgSti());
		return kontekstvelgerURL(kontekstVelgerOppsett(), kontekst);
	}

	public String getVelgValggeografiUrl() {
		Kontekst kontekst = new Kontekst();
		kontekst.setValghierarkiSti(valgtValgSti());
		kontekst.setCountCategory(selectedCountCategory);
		return kontekstvelgerURL(kontekstVelgerOppsett(), kontekst);
	}

	private ValgSti valgtValgSti() {
		return selectedElectionPath.toElectionPath().tilValghierarkiSti().tilValgSti();
	}

	private KontekstvelgerOppsett kontekstVelgerOppsett() {
		KontekstvelgerOppsett oppsett = initKontekstvelgerOppsett();
		String url;
		if ("/secure/counting/approveScannedRejectedCount.xhtml".equals(getRequestURI())) {
			url = "/secure/opptelling/behandleSkannetForkastede.xhtml";
		} else {
			url = "/secure/opptelling/behandleManueltForkastede.xhtml";
		}
		oppsett.leggTil(side(getPageAccess().getId(url)));
		return oppsett;
	}

	public boolean isFromOverview() {
		return fromOverview;
	}

	public String getCountingOverviewURL() {
		return countingOverviewURL;
	}
}
