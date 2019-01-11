package no.valg.eva.admin.valgnatt.repository;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.domain.model.Stemmegivningsstatistikk;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = TestGroups.REPOSITORY)
public class StemmegivningsstatistikkRepositoryTest extends AbstractJpaTestBase {


	private static final int VO_VALG2007_MOSS = 25;



	@Test
	public void forOmrådeOgValg_Stemmegivningsstatistikk() {
		StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository = new StemmegivningsstatistikkRepository(getEntityManager());
		ValggeografiSti valggeografiSti = ValggeografiSti.fra(AreaPath.from("200701"));
		Stemmegivningsstatistikk stemmegivningsstatistikk = stemmegivningsstatistikkRepository.finnForOmrådeOgValg(valggeografiSti, false);
		assertThat(stemmegivningsstatistikk.getGodkjenteFhsg()).isGreaterThan(0);
	}

	@Test
	public void finnVOStemmegivinger_25() {

		StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository = new StemmegivningsstatistikkRepository(getEntityManager());
		ValggeografiSti valggeografiSti = ValggeografiSti.fra(AreaPath.from("200701"));
		Contest contest = new Contest();
		long contestPkForMoss = 21; // Moss-contest er linje 21 i fila contest_area.txt -> pk=21
		int voStemmegivinger = stemmegivningsstatistikkRepository.finnVOStemmegivninger(valggeografiSti, contestPkForMoss);
		assertThat(voStemmegivinger).isEqualTo(VO_VALG2007_MOSS);

	}
	
	@Test(dataProvider = "areasWithRejectedVotings")
	public void numberOfRejectedVotings_givenAreaPathAndRejectionType_rejectedVotings(String areaPath, String votingRejectionId, int expectedRejections) {
		ValggeografiSti valggeografiSti = createMock(ValggeografiSti.class);
		Mockito.when(valggeografiSti.areaPath().path()).thenReturn(areaPath);
		StemmegivningsstatistikkRepository stemmegivningsstatistikkRepository = new StemmegivningsstatistikkRepository(getEntityManager());

		int numberOfRejectedVotes = stemmegivningsstatistikkRepository.numberOfRejectedVotings(valggeografiSti, votingRejectionId);
		
		assertThat(numberOfRejectedVotes).isEqualTo(expectedRejections);
	}

	@DataProvider
	public static Object[][] areasWithRejectedVotings() {
		return new Object[][] {
				{"200701.47.01.0101.010100.0001", "F0", 2},
				{"200701.47.01.0111.011100.0000", "VA", 2},
				{"200701.47.04.0419.041900.0000", "VA", 1},
		};
	}
	
	
}
