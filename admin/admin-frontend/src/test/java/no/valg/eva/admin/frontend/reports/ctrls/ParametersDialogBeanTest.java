package no.valg.eva.admin.frontend.reports.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.voting.VotingService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.service.JasperReportService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.POLLING_STATION;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.frontend.reports.ctrls.ParametersDialogBean.RAPPORT_AVKRYSNINGSMANNTALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ParametersDialogBeanTest extends BaseFrontendTest {

	@Test(dataProvider = "getAreaLevelForReportAndUser")
	public void getAreaLevelForReportAndUser_withDataProvider_verifyExpected(AreaPath areaPath, int templateLevel, String expected) throws Exception {
		ParametersDialogBean bean = new ParametersDialogBean(ctrl(template(templateLevel), areaPath), createMock(ValghendelsesRapport.class));

		String title = bean.getAreaLevelForReportAndUser();

		assertThat(title).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] getAreaLevelForReportAndUser() {
		return new Object[][] {
				{ AREA_PATH_MUNICIPALITY, ROOT.getLevel(), "ElectionEvent 111111" },
				{ AREA_PATH_MUNICIPALITY, COUNTRY.getLevel(), "Country 22" },
				{ AREA_PATH_MUNICIPALITY, COUNTY.getLevel(), "County 33 areaLevel 2" },
				{ AREA_PATH_MUNICIPALITY, MUNICIPALITY.getLevel(), "Municipality 4444 areaLevel 3" },
				{ AREA_PATH_MUNICIPALITY, BOROUGH.getLevel(), "Municipality 4444 areaLevel 3" },
				{ AREA_PATH_POLLING_STATION, BOROUGH.getLevel(), "Borough 555555 areaLevel 4" },
				{ AREA_PATH_POLLING_STATION, POLLING_DISTRICT.getLevel(), "PollingDistrict 6666 areaLevel 5" },
				{ AREA_PATH_POLLING_STATION, POLLING_PLACE.getLevel(), "PollingPlace 7777 areaLevel 6" },
				{ AREA_PATH_POLLING_STATION, POLLING_STATION.getLevel(), "PollingPlace 7777 areaLevel 7" }
		};
	}

	@Test
	public void getArguments_verifyStateAfterInit() throws Exception {
		ParametersDialogBean bean = new ParametersDialogBean(ctrl(template()), createMock(ValghendelsesRapport.class));

		assertThat(bean.getArguments()).hasSize(1);
		assertThat(bean.getArguments().get("ONE")).isNotNull();
	}

	@Test
	public void getParameters_verifyStateAfterInit() throws Exception {
		ParametersDialogBean bean = new ParametersDialogBean(ctrl(template()), createMock(ValghendelsesRapport.class));

		assertThat(bean.getParameters()).hasSize(2);
		ReportParameter p1 = bean.getParameters().get(0);
		assertThat(p1.getId()).isEqualTo("ONE");
		// Verify deps for param1
		ReportParameter dep1 = p1.getDependentParameters().iterator().next();
		assertThat(p1.isInferred()).isTrue();
		assertThat(dep1.getParent()).isSameAs(p1);
		assertThat(dep1.getParentValue()).isEqualTo(p1.getDefaultValue().toString());

		ReportParameter p2 = bean.getParameters().get(1);
		assertThat(p2.getId()).isEqualTo("TWO");
		assertThat(p2.isInferred()).isFalse();
	}

	@Test
	public void isAvkryssningsmanntall_withAvkryssningsmanntallRapport_returnsTrue() throws Exception {
		ValghendelsesRapport rapport = createMock(ValghendelsesRapport.class);
		when(rapport.getRapportId()).thenReturn(RAPPORT_AVKRYSNINGSMANNTALL);

		ParametersDialogBean bean = new ParametersDialogBean(ctrl(template()), rapport);

		assertThat(bean.isAvkrysningsmanntall()).isTrue();
	}

	@Test(dataProvider = "isAvkryssningsmanntallReady")
	public void isAvkryssningsmanntallReady_withDataProvider_verifyExpected(AvkryssningsmanntallReady model, boolean expected) throws Exception {
		ValghendelsesRapport rapport = createMock(ValghendelsesRapport.class);
		when(rapport.getRapportId()).thenReturn(model.reportId);
		ReportLinksController reportLinksController = ctrl(template());
		stub_countUnapprovedAdvanceVotings(model.countUnapprovedAdvanceVotings);

		ParametersDialogBean bean = new ParametersDialogBean(reportLinksController, rapport);
		bean.setAvkrysningsmanntallTest(model.isAvkryssningsmanntallTest);

		assertThat(bean.isAvkrysningsmanntallReady()).isEqualTo(expected);
	}

	private static final AvkryssningsmanntallReady ANNEN_RAPPORT = new AvkryssningsmanntallReady("NOT", false, 0);
	private static final AvkryssningsmanntallReady RAPPORT_MED_TESTMANNTALL = new AvkryssningsmanntallReady(RAPPORT_AVKRYSNINGSMANNTALL, true, 0);
	private static final AvkryssningsmanntallReady RAPPORT_MED_ENEDELIG_OG_UPRØVEDE = new AvkryssningsmanntallReady(RAPPORT_AVKRYSNINGSMANNTALL, false, 1);
	private static final AvkryssningsmanntallReady RAPPORT_MED_ENEDELIG_OG_IKKE_UPRØVEDE = new AvkryssningsmanntallReady(RAPPORT_AVKRYSNINGSMANNTALL, false, 0);

	@DataProvider
	public Object[][] isAvkryssningsmanntallReady() {
		return new Object[][] {
				{ ANNEN_RAPPORT, true },
				{ RAPPORT_MED_TESTMANNTALL, true },
				{ RAPPORT_MED_ENEDELIG_OG_UPRØVEDE, false },
				{ RAPPORT_MED_ENEDELIG_OG_IKKE_UPRØVEDE, true }
		};
	}

	private static class AvkryssningsmanntallReady {
		private String reportId;
		private boolean isAvkryssningsmanntallTest;
		private long countUnapprovedAdvanceVotings;

		public AvkryssningsmanntallReady(String reportId, boolean isAvkryssningsmanntallTest, long countUnapprovedAdvanceVotings) {
			this.reportId = reportId;
			this.isAvkryssningsmanntallTest = isAvkryssningsmanntallTest;
			this.countUnapprovedAdvanceVotings = countUnapprovedAdvanceVotings;
		}
	}

	private ReportLinksController ctrl(ReportTemplate template) throws Exception {
		return ctrl(template, AREA_PATH_MUNICIPALITY);
	}

	private ReportLinksController ctrl(ReportTemplate template, AreaPath userAreaPath) throws Exception {
		ReportLinksController result = initializeMocks(ReportLinksController.class);
		MvArea mvArea = mvArea(userAreaPath);
		when(result.getUserData().getOperatorRole().getMvArea()).thenReturn(mvArea);
		when(result.getUserData().getOperatorMvArea()).thenReturn(mvArea);
		for (AreaLevelEnum levelEnum : AreaLevelEnum.values()) {
			when(result.getMessageProvider().get("@area_level[" + levelEnum.getLevel() + "].name")).thenReturn("AreaLevel " + levelEnum.getLevel());
		}
		when(getInjectMock(JasperReportService.class).getReportTemplate(eq(getUserDataMock()), any(ValghendelsesRapport.class))).thenReturn(template);
		return result;
	}

	private ReportTemplate template() {
		return template(AreaLevelEnum.MUNICIPALITY.getLevel());
	}

	private ReportTemplate template(int level) {
		ReportTemplate result = createMock(ReportTemplate.class);
		List<ReportParameter> params = asList(
				reportParameter("ONE"),
				reportParameter("TWO"));
		when(result.getParameters()).thenReturn(params);
		Set<Integer> levels = new HashSet<>(singletonList(level));
		when(result.getAreaLevels()).thenReturn(levels);
		return result;
	}

	private ReportParameter reportParameter(String id) {
		ReportParameter result = createMock(ReportParameter.class);
		when(result.getId()).thenReturn(id);
		when(result.getDefaultValue()).thenReturn(id);
		if ("ONE".equals(id)) {
			Set<ReportParameter> dependent = new HashSet<>(singletonList(
					new ReportParameter("", "", "", "")));
			when(result.getDependentParameters()).thenReturn(dependent);
			when(result.isInferred()).thenReturn(true);
		}
		return result;
	}

	private MvArea mvArea(AreaPath areaPath) {
		return new MvAreaBuilder(areaPath).getValue();
	}

	private void stub_countUnapprovedAdvanceVotings(long count) {
		when(getInjectMock(VotingService.class).countUnapprovedAdvanceVotings(eq(getUserDataMock()), any(AreaPath.class))).thenReturn(count);
	}

}
