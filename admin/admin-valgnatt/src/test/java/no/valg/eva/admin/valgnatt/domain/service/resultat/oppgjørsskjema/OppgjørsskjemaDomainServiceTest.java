package no.valg.eva.admin.valgnatt.domain.service.resultat.oppgjørsskjema;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.service.ContestReportDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.resultat.Resultatskjema;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

public class OppgjørsskjemaDomainServiceTest extends MockUtilsTestCase {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void fraMøtebokOgValgoppgjør_ugyldigOmråde_girException() throws Exception {
		OppgjørsskjemaDomainService oppgjørsskjemaDomainService = initializeMocks(OppgjørsskjemaDomainService.class);

		oppgjørsskjemaDomainService.fraMøtebokOgValgoppgjør(createMock(MvElection.class), createMock(MvArea.class));
	}

	@Test
	public void fraMøtebokOgValgoppgjør_gyldigOmråde_lagerOppgjørsskjema() throws Exception {
		OppgjørsskjemaDomainService oppgjørsskjemaDomainService = initializeMocks(OppgjørsskjemaDomainService.class);

		MvArea fakeMvArea = createMock(MvArea.class);
		when(fakeMvArea.getActualAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		List<ContestReport> contestReports = makeFakeContestReports();
		when(getInjectMock(ContestReportDomainService.class).findFinalContestReportsByContest(any(Contest.class))).thenReturn(contestReports);
		Set<Municipality> municipalities = makeFakeMunicipalities();
		when(getInjectMock(RapporteringsområdeDomainService.class).kommunerForRapportering(any(Contest.class))).thenReturn(municipalities);
		List<Resultatskjema> oppgjørsskjemaList = oppgjørsskjemaDomainService.fraMøtebokOgValgoppgjør(createMock(MvElection.class), fakeMvArea);

		assertThat(oppgjørsskjemaList).hasSize(1);
	}

	private List<ContestReport> makeFakeContestReports() {
		List<ContestReport> contestReports = new ArrayList<>();
		ContestReport fakeContestReport = createMock(ContestReport.class);
        when(fakeContestReport.tellingerForRapportering(anySet(), anySet(), anySet(), any(), any())).thenReturn(Collections
				.emptyList());
		contestReports.add(fakeContestReport);
		return contestReports;
	}

	private Set<Municipality> makeFakeMunicipalities() {
		Set<Municipality> municipalities = new HashSet<>();
		municipalities.add(createMock(Municipality.class));
		return municipalities;
	}

}
