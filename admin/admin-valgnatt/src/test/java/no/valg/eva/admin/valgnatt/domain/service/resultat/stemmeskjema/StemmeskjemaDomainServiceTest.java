package no.valg.eva.admin.valgnatt.domain.service.resultat.stemmeskjema;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.valgnatt.domain.model.resultat.Resultatskjema;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Stemmeseddelstatistikk;
import no.valg.eva.admin.valgnatt.domain.model.resultat.statistikk.Valgnattstatistikk;
import no.valg.eva.admin.valgnatt.domain.model.resultat.stemmetall.Stemmetall;
import no.valg.eva.admin.valgnatt.domain.service.resultat.RapporteringsområdeDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.statistikk.ValgnattstatistikkDomainService;
import no.valg.eva.admin.valgnatt.domain.service.resultat.stemmetall.StemmetallDomainService;
import no.valg.eva.admin.voting.domain.model.Stemmegivningsstatistikk;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StemmeskjemaDomainServiceTest extends MockUtilsTestCase {

	private static final BigInteger GODKJENTE_FHSG = BigInteger.valueOf(14);
	private static final BigInteger GODKJENTE_VTSG = BigInteger.valueOf(5);
	private static final BigInteger FORKASTEDE_FHSG = BigInteger.valueOf(0);
	private static final BigInteger FORKASTEDE_VTSG = BigInteger.valueOf(4);

	private static final int GODKJENTE_FHS = 1877;
	private static final int GODKJENTE_VTS = 149;
	private static final int FORKASTEDE_FHS = 8;
	private static final int FORKASTEDE_VTS = 2;

	private static final int A_FHS_FORELØPIG = 2;
	private static final int A_VTS_FORELØPIG = 6;
	private static final int A_FHS_ENDELIG = 3;
	private static final int A_VTS_ENDELIG = 7;

	private static final int H_FHS_FORELØPIG = 4;
	private static final int H_VTS_FORELØPIG = 10;
	private static final int H_FHS_ENDELIG = 5;
	private static final int H_VTS_ENDELIG = 11;

	@Test
	public void fraMøtebok_ballotCountsMedForhåndsOgValgtingsstemmer_stemmeskjemaInneholderTallene() throws Exception {
		StemmeskjemaDomainService service = initializeMocks(StemmeskjemaDomainService.class);

		MvElection mvElectionContest = createMock(MvElection.class);
		when(mvElectionContest.getActualAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		MvArea reportForArea = createMock(MvArea.class);
		AreaPath municipalityPath = AreaPath.from("150001.47.01.0101");
		when(reportForArea.areaPath().toMunicipalityPath()).thenReturn(municipalityPath);
		List<ContestReport> contestReports = makeContestReports();

		when(getInjectMock(ContestReportRepository.class).byContestInArea(any(Contest.class), eq(municipalityPath)))
				.thenReturn(contestReports);

		List<Stemmetall> stemmetallList = makeStemmetallList();
		when(getInjectMock(StemmetallDomainService.class).hentStemmetall(any(List.class))).thenReturn(stemmetallList);

		Valgnattstatistikk valgnattstatistikk = makeValgnattstatistikk();
		when(getInjectMock(ValgnattstatistikkDomainService.class).lagStatistikk(any(MvElection.class), anyList(), any(MvArea.class)))
				.thenReturn(valgnattstatistikk);

		Resultatskjema stemmeskjema = service.fraMøtebok(mvElectionContest, reportForArea);

		String json = stemmeskjema.toJson();
		assertThat(json).contains("\"A\",\"fhs-endelig\":" + A_FHS_ENDELIG + ",\"vts-endelig\":" + A_VTS_ENDELIG);
		assertThat(json).contains("\"H\",\"fhs-foreløpig\":" + H_FHS_FORELØPIG + ",\"vts-foreløpig\":" + H_VTS_FORELØPIG);

		assertThat(json).contains("\"stg-forhånd-forkastede\":" + FORKASTEDE_FHSG);
		assertThat(json).contains("\"stg-forhånd-godkjente\":" + GODKJENTE_FHSG);
		assertThat(json).contains("\"stg-valgting-forkastede\":" + FORKASTEDE_VTSG);
		assertThat(json).contains("\"stg-valgting-godkjente\":" + GODKJENTE_VTSG);
		assertThat(json).contains("\"sts-forhånd-forkastede\":" + FORKASTEDE_FHS);
		assertThat(json).contains("\"sts-forhånd-godkjente\":" + GODKJENTE_FHS);
		assertThat(json).contains("\"sts-valgting-forkastede\":" + FORKASTEDE_VTS);
		assertThat(json).contains("\"sts-valgting-godkjente\":" + GODKJENTE_VTS);
	}

	private List<ContestReport> makeContestReports() {
		List<ContestReport> contestReports = new ArrayList<>();
		contestReports.add(makeContestReport());
		return contestReports;
	}

	private ContestReport makeContestReport() {
		ContestReport fakeContestReport = createMock(ContestReport.class);
		List<BallotCount> fakeBallotCounts = makeBallotCounts();
        when(fakeContestReport.tellingerForRapportering(anySet(), anySet(), anySet(), any(), any())).thenReturn(fakeBallotCounts);
		return fakeContestReport;
	}

	private List<BallotCount> makeBallotCounts() {
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(createMock(BallotCount.class));
		return ballotCounts;
	}

	private List<Stemmetall> makeStemmetallList() {
		List<Stemmetall> stemmetallList = new ArrayList<>();
		stemmetallList.add(new Stemmetall("A", A_FHS_FORELØPIG, A_VTS_FORELØPIG, true, true, A_FHS_ENDELIG, A_VTS_ENDELIG, true, true, null));
		stemmetallList.add(new Stemmetall("H", H_FHS_FORELØPIG, H_VTS_FORELØPIG, true, true, H_FHS_ENDELIG, H_VTS_ENDELIG, false, false, null));
		return stemmetallList;
	}

	private Valgnattstatistikk makeValgnattstatistikk() {
		return new Valgnattstatistikk(makeStemmegivningsstatistikk(), makeStemmeseddelstatistikk());
	}

	private Stemmegivningsstatistikk makeStemmegivningsstatistikk() {
		return new Stemmegivningsstatistikk(GODKJENTE_FHSG.intValue(), GODKJENTE_VTSG.intValue(), FORKASTEDE_FHSG.intValue(), FORKASTEDE_VTSG.intValue());
	}

	private Stemmeseddelstatistikk makeStemmeseddelstatistikk() {
		return new Stemmeseddelstatistikk(GODKJENTE_FHS, GODKJENTE_VTS, FORKASTEDE_FHS, FORKASTEDE_VTS);
	}

	@Test
	public void fraMøtebok_0000krets_kretserHentesFraOmrådeTjeneste() throws Exception {
		StemmeskjemaDomainService service = initializeMocks(StemmeskjemaDomainService.class);

		MvElection mvElectionContest = createMock(MvElection.class);
		MvArea reportForArea = createMock(MvArea.class);
		when(reportForArea.areaPath().isMunicipalityPollingDistrict()).thenReturn(true);
		List<ContestReport> contestReports = makeContestReports();
		when(getInjectMock(ContestReportRepository.class).byContestInArea(any(Contest.class), any(AreaPath.class)))
				.thenReturn(contestReports);

		service.fraMøtebok(mvElectionContest, reportForArea);

		verify(getInjectMock(RapporteringsområdeDomainService.class), times(1))
				.kretserForRapporteringAvForhåndsstemmerOgSentralValgting(any(Municipality.class), any(MvElection.class));
	}
}
