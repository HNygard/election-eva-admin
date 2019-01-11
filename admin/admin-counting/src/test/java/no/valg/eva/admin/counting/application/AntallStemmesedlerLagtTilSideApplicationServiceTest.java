package no.valg.eva.admin.counting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AntallStemmesedlerLagtTilSideApplicationServiceTest extends MockUtilsTestCase {
	private static final AreaPath NOT_MUNICIPALITY_PATH = AreaPath.from("111111.11.11");
	private static final AreaPath MUNICIPALITY_PATH = AreaPath.from("111111.11.11.1111");

	private AntallStemmesedlerLagtTilSideApplicationService service;
	private AntallStemmesedlerLagtTilSideDomainService domainService;
	private MvAreaRepository mvAreaRepository;

	private UserData userData;
	private MvArea mvArea;
	private Municipality municipality;

	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(AntallStemmesedlerLagtTilSideApplicationService.class);
		domainService = getInjectMock(AntallStemmesedlerLagtTilSideDomainService.class);
		mvAreaRepository = getInjectMock(MvAreaRepository.class);

		userData = createMock(UserData.class);
		municipality = createMock(Municipality.class);
		mvArea = mvArea(municipality);

		antallStemmesedlerLagtTilSide = createMock(AntallStemmesedlerLagtTilSide.class);
	}

	@Test
	public void hentAntallStemmesedlerLagtTilSide_givenMunicipalityUser_returnAntallStemmesedlerLagtTilSide() throws Exception {
		when(userData.getOperatorAreaPath()).thenReturn(MUNICIPALITY_PATH);
		when(mvAreaRepository.findSingleByPath(MUNICIPALITY_PATH)).thenReturn(mvArea);
		when(domainService.hentAntallStemmesedlerLagtTilSide(municipality)).thenReturn(antallStemmesedlerLagtTilSide);

		AntallStemmesedlerLagtTilSide result = service.hentAntallStemmesedlerLagtTilSide(userData, ValggeografiSti.kommuneSti(MUNICIPALITY_PATH));

		assertThat(result).isEqualTo(antallStemmesedlerLagtTilSide);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void hentAntallStemmesedlerLagtTilSide_givenNotMunicipalityUser_throwsException() throws Exception {
		when(userData.getOperatorAreaPath()).thenReturn(NOT_MUNICIPALITY_PATH);
		service.hentAntallStemmesedlerLagtTilSide(userData, ValggeografiSti.kommuneSti(NOT_MUNICIPALITY_PATH));
	}

	@Test
	public void hentAntallStemmesedlerLagtTilSide_givenElectionEventAdminAndMunicipalityPath_returnAntallStemmesedlerLagtTilSide() throws Exception {
		when(userData.isElectionEventAdminUser()).thenReturn(true);
		when(mvAreaRepository.findSingleByPath(MUNICIPALITY_PATH)).thenReturn(mvArea);
		when(domainService.hentAntallStemmesedlerLagtTilSide(municipality)).thenReturn(antallStemmesedlerLagtTilSide);

		AntallStemmesedlerLagtTilSide result = service.hentAntallStemmesedlerLagtTilSide(userData, ValggeografiSti.kommuneSti(MUNICIPALITY_PATH));

		assertThat(result).isEqualTo(antallStemmesedlerLagtTilSide);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void hentAntallStemmesedlerLagtTilSide_givenElectionEventAdminAndNotMunicipalityPath_throwsException() throws Exception {
		when(userData.isElectionEventAdminUser()).thenReturn(true);
		service.hentAntallStemmesedlerLagtTilSide(userData, ValggeografiSti.kommuneSti(NOT_MUNICIPALITY_PATH));
	}

	@Test
	public void lagreAntallStemmesedlerLagtTilSide_givenInput_callsDomainService() throws Exception {
		when(antallStemmesedlerLagtTilSide.getMunicipalityPath()).thenReturn(MUNICIPALITY_PATH);
		when(mvAreaRepository.findSingleByPath(MUNICIPALITY_PATH)).thenReturn(mvArea);

		service.lagreAntallStemmesedlerLagtTilSide(userData, antallStemmesedlerLagtTilSide);

		verify(domainService).lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void lagreAntallStemmesedlerLagtTilSide_givenNotMunicipalityPath_throwsException() throws Exception {
		when(antallStemmesedlerLagtTilSide.getMunicipalityPath()).thenReturn(NOT_MUNICIPALITY_PATH);
		service.lagreAntallStemmesedlerLagtTilSide(userData, antallStemmesedlerLagtTilSide);
	}

	private MvArea mvArea(Municipality municipality) {
		MvArea mvArea = createMock(MvArea.class);
		when(mvArea.getMunicipality()).thenReturn(municipality);
		return mvArea;
	}
}
