package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingStationRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/* DEV-NOTE: Tester funksjonaliteten relatert til å få trigget papirmanntall
 *           Merk at detaljert testing av feks rodefordeling gjøres i testene til disse klassene
 *           (se feks RodefordelerTest) */
public class PapirmanntallDomainServiceTest extends MockUtilsTestCase {

	private  static final int ELECTORAL_ROLL_LINES_PER_PAGE = 30;
	private static final long ELECTION_EVENT_PK = 1L;
	
	private PapirmanntallDomainService papirmanntallDomainService;
	private UserData userData;
	private List<PollingStation> roder;
	private Map<Long, Voter> velgereIKrets;
	private PollingDistrict stemmekrets;

	@BeforeMethod(alwaysRun = true)
	public void init() throws Exception {
		papirmanntallDomainService = initializeMocks(PapirmanntallDomainService.class);
		userData = createMock(UserData.class);
		roder = roder();
		velgereIKrets = velgereIKrets();
		stemmekrets = stemmekrets();
		
		when(getInjectMock(PollingDistrictRepository.class).findPollingDistrictsUsingPollingStation(ELECTION_EVENT_PK)).thenReturn(singletonList(stemmekrets));
		when(getInjectMock(VoterRepository.class).getElectoralRollForPollingDistrict(stemmekrets)).thenReturn(new ArrayList<>(velgereIKrets.values()));
		when(getInjectMock(PollingStationRepository.class).findByPollingPlace(any())).thenReturn(roder);
		when(getInjectMock(ElectionEventRepository.class).findByPk(ELECTION_EVENT_PK)).thenReturn(electionEvent());
	}

	@Test
    public void regenererSideOgLinjeForRoder_velgerneTilordnesTilRiktigRode() {
		Map<Long, PollingStation> forventetRodefordeling = kobleVelgereOgRoder(roder);
		
		papirmanntallDomainService.regenererSideOgLinjeForRoder(userData, ELECTION_EVENT_PK);
		
		for(Voter velger : velgereIKrets.values()) {
			PollingStation forventetRode = forventetRodefordeling.get(velger.getPk());
			assertThat(velger.getPollingStation()).isEqualTo(forventetRode);
		}
	}

	@Test
    public void regenererSideOgLinjeForRoder_velgerneFaarSideOgLinjeIHenholdTilRodeOgNavn() {
		papirmanntallDomainService.regenererSideOgLinjeForRoder(userData, ELECTION_EVENT_PK);

		sjekkSideOgLinje(velgereIKrets.get(1L), 1, 1);
		sjekkSideOgLinje(velgereIKrets.get(2L), 1, 2);
		sjekkSideOgLinje(velgereIKrets.get(3L), 1, 3);
		sjekkSideOgLinje(velgereIKrets.get(4L), 1, 4);
		sjekkSideOgLinje(velgereIKrets.get(5L), 2, 1);
		sjekkSideOgLinje(velgereIKrets.get(6L), 2, 2);
		sjekkSideOgLinje(velgereIKrets.get(7L), 2, 3);
		sjekkSideOgLinje(velgereIKrets.get(8L), 3, 1);
		sjekkSideOgLinje(velgereIKrets.get(9L), 3, 2);
		sjekkSideOgLinje(velgereIKrets.get(10L), 4, 1);
		sjekkSideOgLinje(velgereIKrets.get(11L), 4, 2);
	}

	@Test
    public void regenererSideOgLinjeForRoder_sideOgLinjeTabellenOppdateresMedNesteNummerSomSkalBrukes() {
		ArgumentCaptor<PollingDistrict> stemmekretsCaptor = ArgumentCaptor.forClass(PollingDistrict.class);
		ArgumentCaptor<Integer> nesteLinjenummerCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> nesteSidenummerCaptor = ArgumentCaptor.forClass(Integer.class);
			
		papirmanntallDomainService.regenererSideOgLinjeForRoder(userData, ELECTION_EVENT_PK);

		verify(getInjectMock(VoterRepository.class), atLeastOnce()).updateLastLineLastPageNumber(
			stemmekretsCaptor.capture(),
			nesteLinjenummerCaptor.capture(),
			nesteSidenummerCaptor.capture());
		
		assertThat(stemmekretsCaptor.getValue()).isEqualTo(stemmekrets);
		assertThat(nesteLinjenummerCaptor.getValue()).isEqualTo(0);
		assertThat(nesteSidenummerCaptor.getValue()).isEqualTo(5);
	}
	
	private ElectionEvent electionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(ELECTION_EVENT_PK);
		electionEvent.setElectoralRollLinesPerPage(ELECTORAL_ROLL_LINES_PER_PAGE);
		return electionEvent;
	}

	private List<PollingStation> roder() {
		return asList(rode("A", "G"), rode("H", "M"), rode("N", "R"), rode("S", "Å"));
	}

	private PollingStation rode(String first, String last) {
		return new PollingStation(first, last);
	}

	private Map<Long, Voter> velgereIKrets() {
		Map<Long, Voter> map = new HashMap<>();
		leggTilVelger(map, 1L, "AIRode1");
		leggTilVelger(map, 2L, "BIRode1");
		leggTilVelger(map, 3L, "CIRode1");
		leggTilVelger(map, 4L, "GIRode1");
		leggTilVelger(map, 5L, "HIRode2");
		leggTilVelger(map, 6L, "IIRode2");
		leggTilVelger(map, 7L, "MIRode2");
		leggTilVelger(map, 8L, "NIRode3");
		leggTilVelger(map, 9L, "RIRode3");
		leggTilVelger(map, 10L, "SIRode4");
		leggTilVelger(map, 11L, "ÅIRode4");
		return map;
	}

	private void leggTilVelger(Map<Long, Voter> map, Long pk, String etternavn) {
		Voter velger = new Voter();
		velger.setPk(pk);
		velger.setLastName(etternavn);
		map.put(pk, velger);
	}

	private PollingDistrict stemmekrets() {
		return new PollingDistrict();
	}

	private Map<Long,PollingStation> kobleVelgereOgRoder(List<PollingStation> roder) {
		Map<Long, PollingStation> map = new HashMap<>();
		map.put(1L, roder.get(0));
		map.put(2L, roder.get(0));
		map.put(3L, roder.get(0));
		map.put(4L, roder.get(0));
		map.put(5L, roder.get(1));
		map.put(6L, roder.get(1));
		map.put(7L, roder.get(1));
		map.put(8L, roder.get(2));
		map.put(9L, roder.get(2));
		map.put(10L, roder.get(3));
		map.put(11L, roder.get(3));
		return map;
	}

	private void sjekkSideOgLinje(Voter velger, int forventetSide, int forventetLinje) {
		assertThat(velger.getElectoralRollPage()).isEqualTo(forventetSide);
		assertThat(velger.getElectoralRollLine()).isEqualTo(forventetLinje);
	}
}
