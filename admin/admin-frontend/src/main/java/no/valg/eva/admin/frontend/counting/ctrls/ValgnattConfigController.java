package no.valg.eva.admin.frontend.counting.ctrls;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.common.counting.service.valgnatt.ValgnattReportService;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.picker.ctrls.ContestPickerController2;

@Named
@ViewScoped
public class ValgnattConfigController extends BaseController implements ContestPickerController2.TabChangeListener {

	@Inject
	private UserData userData;
	@Inject
	private MessageProvider messageProvider;
	@Inject
	private ContestPickerController2 contestPickerController;
	@Inject
	private PageTitleMetaBuilder pageTitleMetaBuilder;
	@Inject
	private ValgnattReportService valgnattReportService;

	private List<Valgnattrapportering> electionDataList;
	private ElectionPath contestPath;

	public ValgnattConfigController() {
		// CDI
	}

	public ContestPickerController2 getContestPickerController() {
		return contestPickerController;
	}

	@PostConstruct
	protected void doInit() {
		contestPickerController.initWithElectionsFromElectionEvent(ElectionPath.from(userData.getElectionEventId()));
		contestPickerController.setTabChangeListener(this);
		onTabChange(contestPickerController.getContestInfo());
	}

	public String getPageTitle() {
		return "@menu.administration.valgnatt_config";
	}

	@Override
	public void onTabChange(ContestInfo contestInfo) {
		initSsbReportLists(contestInfo);
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return pageTitleMetaBuilder.area(userData.getOperatorMvArea());
	}

	public void exportGeographyAndVoters() {
		if (execute(() -> {
			valgnattReportService.exportGeographyAndVoters(userData, contestPath);
		})) {
			electionDataList.stream().filter(Valgnattrapportering::isGeografiStemmeberettigede).forEach(Valgnattrapportering::oppdaterStatusSendt);
			electionDataList = refreshValgnattskjemaList(contestPath);
		}
	}

	public void exportPartiesAndCandidates() {
		ElectionPath electionPath = contestPath.toElectionPath();
		if (execute(() -> {
			valgnattReportService.exportPartiesAndCandidates(userData, electionPath);
		})) {
			electionDataList = refreshValgnattskjemaList(contestPath);
		}
	}

	/**
	 * This method is triggered once the user is done picking area and election.
	 */
	private void initSsbReportLists(ContestInfo contestInfo) {
		if (contestInfo != null) {
			contestPath = contestInfo.getElectionPath();

			electionDataList = refreshValgnattskjemaList(contestPath);
		}
	}

	private List<Valgnattrapportering> refreshValgnattskjemaList(ElectionPath contestElectionPath) {
		return valgnattReportService.rapporteringerForGrunnlagsdata(userData, contestElectionPath);
	}

	public List<Valgnattrapportering> getElectionDataList() {
		return electionDataList;
	}

	public String reportTypeName(ReportType reportType) {
		return messageProvider.get(reportType.textId());
	}
}
