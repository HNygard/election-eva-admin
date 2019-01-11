package no.valg.eva.admin.frontend.counting.ctrls;

import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.picker.ctrls.ContestPickerController2;

@Named
@ViewScoped
public class StatisticReportingController extends KontekstAvhengigController implements ContestPickerController2.TabChangeListener {

	@Inject
	private ContestPickerController2 contestPickerController;
	@Inject
	private ValgnattReportService valgnattReportService;

	private MvArea selectedArea;
	private ContestInfo contestInfo;
	private Contest contest;
	private MvElection mvElectionContest;

	private List<Valgnattrapportering> rapporteringerForStemmeskjema;
	private List<Valgnattrapportering> oppgjorsskjemaRapportering;

	public ContestPickerController2 getContestPickerController() {
		return contestPickerController;
	}

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		if (getUserData().isElectionEventAdminUser()) {
			setup.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		}
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		selectedArea = getMvAreaService().findSingleByPath(kontekst.getValggeografiSti());
		if (getUserData().isSamiElectionCountyUser()) {
			contestPickerController.initForSamiElectionCountyUser(getUserData().getOperatorElectionPath());
		} else {
			contestPickerController.init(getUserData().isCountyLevelUser() ? getUserData().getOperatorMvArea() : selectedArea, null, true);
		}
		contestPickerController.setTabChangeListener(this);
		onTabChange(contestPickerController.getContestInfo());
	}

	@Override
	public void onTabChange(ContestInfo contestInfo) {
		this.contestInfo = contestInfo;
		initValgnattrapporteringMetadata();
	}

	public String getPageTitle() {
		return "@menu.statistic.reporting";
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return getPageTitleMetaBuilder().area(selectedArea);
	}

	public void rapporterStemmetall(Valgnattrapportering valgnattrapportering) {
		ElectionPath contestPath = ElectionPath.from(mvElectionContest.getPath());
		execute(() -> valgnattReportService.rapporterStemmeskjema(getUserData(), contestPath, valgnattrapportering.getAreaPath(), valgnattrapportering));

		oppdaterStemmeskjemaRapportMetadataListe();
	}

	private void oppdaterStemmeskjemaRapportMetadataListe() {
		if (selectedArea.getMunicipality() != null) {
			rapporteringerForStemmeskjema.clear();
			rapporteringerForStemmeskjema.addAll(
					valgnattReportService.rapporteringerForStemmeskjema(getUserData(), mvElectionContest.electionPath(), selectedArea.areaPath()));
		}
	}

	public void rapporterOppgjorsskjema(Valgnattrapportering valgnattrapportering) {
		ElectionPath contestPath = mvElectionContest.electionPath();
		AreaPath areaPath = contestInfo.getAreaPath();
		execute(() -> valgnattReportService.rapporterOppgjørsskjema(getUserData(), contestPath, areaPath, valgnattrapportering));
		oppgjorsskjemaRapportering = singletonList(valgnattReportService.rapporteringerForOppgjorsskjema(getUserData(), contestPath, areaPath));
		oppdaterStemmeskjemaRapportMetadataListe();
	}

	private void initValgnattrapporteringMetadata() {
		if ((selectedArea != null || getUserData().isSamiElectionCountyUser()) && contestInfo != null) {
			mvElectionContest = getMvElectionService().findSingleByPath(contestInfo.getElectionPath());
			contest = mvElectionContest.getContest();

			if (contest != null && selectedArea.getMunicipality() != null && getUserDataController().getUserAccess().isResultatRapporter()) {

				rapporteringerForStemmeskjema = valgnattReportService.rapporteringerForStemmeskjema(getUserData(),
						contestInfo.getElectionPath(), selectedArea.areaPath());
			}

			if (skalHaOppgjorsskjemaRapportering()) {
				oppgjorsskjemaRapportering = singletonList(
						valgnattReportService.rapporteringerForOppgjorsskjema(getUserData(), contestInfo.getElectionPath(), selectedArea.areaPath()));
			}
		}
	}

	private boolean skalHaOppgjorsskjemaRapportering() {
		return getUserDataController().getUserAccess().isResultatRapporter()
				&& contest != null
				&& (valgdistriktetsOmraadeNivaaErLikMedBrukerensOmraadeNivaa() || valgdistriktForSametingsvalgetOgBrukerPaaValghendelse());
	}

	private boolean valgdistriktetsOmraadeNivaaErLikMedBrukerensOmraadeNivaa() {
		return contest.isSingleArea() && contest.getFirstContestArea().getActualAreaLevel() == getUserData().getOperatorAreaLevel();
	}

	private boolean valgdistriktForSametingsvalgetOgBrukerPaaValghendelse() {
		return !contest.isSingleArea() && getUserData().getOperatorAreaLevel() == ROOT;
	}

	public Contest getCurrentContest() {
		return contest;
	}

	public boolean isRenderCorrectedResult() {
		if (contestInfo != null && contestInfo.getAreaLevel() == AreaLevelEnum.COUNTY && getUserData().isMunicipalityLevelUser()) {
			return false;
		}
		if (!contest.isSingleArea() && !getUserData().isSamiElectionCountyUser()) {
			return false;
		}
		return getUserDataController().getUserAccess().isResultatRapporter() && oppgjorsskjemaRapportering != null
				&& oppgjorsskjemaRapportering.stream().anyMatch(Valgnattrapportering::isNotSendt);
	}

	public List<Valgnattrapportering> getRapporteringerForStemmeskjema() {
		return rapporteringerForStemmeskjema;
	}

	public List<Valgnattrapportering> getRapporteringerForStemmeskjemaForhand() {
		return rapporteringerForStemmeskjema.stream().filter(Valgnattrapportering::isNotSendt).filter(Valgnattrapportering::isForhand)
				.collect(Collectors.toList());
	}

	public List<Valgnattrapportering> getRapporteringerForStemmeskjemaValgting() {
		return rapporteringerForStemmeskjema.stream().filter(Valgnattrapportering::isNotSendt).filter(Valgnattrapportering::isValgting)
				.collect(Collectors.toList());
	}

	public List<Valgnattrapportering> getOppgjorsskjemaRapportering() {
		return oppgjorsskjemaRapportering;
	}

	public List<Valgnattrapportering> getRapporteringerForOppgjor() {
		return oppgjorsskjemaRapportering.stream().filter(Valgnattrapportering::isNotSendt).collect(Collectors.toList());
	}

	public boolean isFerdigRapportert() {
		if (oppgjorsskjemaRapportering != null && getRapporteringerForOppgjor().isEmpty()) {
			return true;
		}

		if (oppgjorsskjemaRapportering != null && mvElectionContest.getActualAreaLevel() == AreaLevelEnum.MUNICIPALITY) {
			return false;
		}

		return rapporteringerForStemmeskjema != null && getRapporteringerForStemmeskjemaForhand().isEmpty() && getRapporteringerForStemmeskjemaValgting().isEmpty();
	}

	public List<Valgnattrapportering> getLoggForStemmeskjema() {
		return logg(rapporteringerForStemmeskjema);
	}

	public List<Valgnattrapportering> getLoggForOppgjorsskjema() {
		return logg(oppgjorsskjemaRapportering);
	}

	private List<Valgnattrapportering> logg(List<Valgnattrapportering> rapporteringer) {
		return rapporteringer != null ? rapporteringer.stream().filter(Valgnattrapportering::isSendt).collect(Collectors.toList())
				: Collections.emptyList();
	}
}
