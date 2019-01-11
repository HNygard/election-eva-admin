package no.valg.eva.admin.configuration.domain.model.manntall.papir;

import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;


public class SideOgLinjefordelerForRoderTest {

	private static final int MAX_ANTALL_VELGERE_PER_SIDE = 10;
	private static final int LITT_MINDRE_ENN_ANTALL_PER_SIDE = 5;
	private static final int LITT_MER_ENN_ANTALL_PER_SIDE = 15;
	private static final int NOK_VELGERE_TIL_5_SIDER = 50;

	@Test
	public void tilordneSideOgLinjeTilVelgere_antalletVelgereErUendretEtterTilordning() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(LITT_MER_ENN_ANTALL_PER_SIDE, stemmekrets);

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		assertEquals(manntall.size(), LITT_MER_ENN_ANTALL_PER_SIDE);
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtManntallMed1RodeOgFlereVelgerePerSideEnnVelgere_erAlleVelgerePaaSide1() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(LITT_MINDRE_ENN_ANTALL_PER_SIDE, stemmekrets);
		settSammeRode(manntall);

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		Integer pageNumber = 1;
		for (Voter voter : manntall) {
			assertEquals(voter.getElectoralRollPage(), pageNumber);
		}
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtManntallMed1RodeOgFlereVelgereEnnVelgerePerSide_erVelgerneFordeltFortlopendePaaNyeSider() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(NOK_VELGERE_TIL_5_SIDER, stemmekrets);
		settSammeRode(manntall);

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		manntall.sort(Comparator.comparing(Voter::getElectoralRollPage));

		Integer sidenummer = 1;
		Integer linjenummer = 0;
		for (int i = 0; i < manntall.size(); i++, linjenummer++) {
			if (linjenummer == MAX_ANTALL_VELGERE_PER_SIDE) {
				sidenummer++;
				linjenummer = 0;
			}
			assertEquals(manntall.get(i).getElectoralRollPage(), sidenummer);
		}
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtManntallMedFlereRoder_forHverSideErVelgerneNummerertIStigendeRekkefolge() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(39, stemmekrets);
		sorterTilfeldig(manntall);

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		manntall.sort((o1, o2) -> {
			if (o1.getElectoralRollPage().equals(o2.getElectoralRollPage())) {
				return o1.getElectoralRollLine().compareTo(o2.getElectoralRollLine());
			} else {
				return o1.getElectoralRollPage().compareTo(o2.getElectoralRollPage());
			}
		});

		Integer sidenummer = 1;
		Integer linjenummer = 1;
		for (Voter voter : manntall) {
			if (!sidenummer.equals(voter.getElectoralRollPage())) {
				linjenummer = 1;
				sidenummer = voter.getElectoralRollPage();
			}
			assertEquals(voter.getElectoralRollLine(), linjenummer);
			linjenummer++;
		}
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtManntallMedFlereRoder_nySideForHverRode() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(MAX_ANTALL_VELGERE_PER_SIDE, stemmekrets);
		sorterTilfeldig(manntall);
		
		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		manntall.sort(Comparator.comparing(o -> o.getPollingStation().getId()));

		Integer sidenummer = 1;
		Integer antallVelgerePaaSiden = 0;
		String lastpollingStationId = manntall.get(0).getPollingStation().getId();
		for (int i = 0; i < manntall.size(); i++, antallVelgerePaaSiden++) {
			if (!lastpollingStationId.equals(manntall.get(i).getPollingStation().getId())) {
				sidenummer++;
				antallVelgerePaaSiden = 0;
				lastpollingStationId = manntall.get(i).getPollingStation().getId();
			}

			if (antallVelgerePaaSiden == 10) {
				sidenummer++;
				antallVelgerePaaSiden = 0;
			}

			assertEquals(manntall.get(i).getElectoralRollPage(), sidenummer);
		}
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtManntallMedFlereRoder_nesteLinjenummerEr0() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(39, stemmekrets);
		sorterTilfeldig(manntall);

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		assertEquals(sideOgLinjefordelerForRoder.getNesteLinje().intValue(), 0);
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtManntallMedFlereRoder_nesteSidenummerEr1MerEnnSisteSideMedVelger() {
		MvArea stemmekrets = stemmekrets();
		List<Voter> manntall = genererManntall(LITT_MINDRE_ENN_ANTALL_PER_SIDE, stemmekrets);
		sorterTilfeldig(manntall);

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		manntall.sort(Comparator.comparing(Voter::getElectoralRollPage));

		assertEquals(manntall.get(manntall.size() - 1).getElectoralRollPage(), (Integer)(sideOgLinjefordelerForRoder.getNesteSide() - 1));
	}

	@Test
	public void tilordneSideOgLinjeTilVelgere_forEtTomtManntall_gjoerIngenting() {
		List<Voter> manntall = emptyList();

		SideOgLinjefordelerForRoder sideOgLinjefordelerForRoder = new SideOgLinjefordelerForRoder(manntall, MAX_ANTALL_VELGERE_PER_SIDE);
		sideOgLinjefordelerForRoder.tilordneSideOgLinjeTilVelgere();

		assertTrue(manntall.isEmpty());
	}
	
	private MvArea stemmekrets() {
		MvArea mvArea = new MvArea();

		mvArea.setCountryId("1");
		mvArea.setCountyId("1");
		mvArea.setBoroughId("1");
		mvArea.setMunicipalityId("1");

		Municipality municipality = new Municipality();
		municipality.setPk(1L);
		mvArea.setMunicipality(municipality);

		mvArea.setPollingDistrictId("1");
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setPk(1L);
		mvArea.setPollingDistrict(pollingDistrict);

		return mvArea;
	}

	private List<Voter> genererManntall(final int antallVelgere, final MvArea stemmekrets) {
		List<Voter> manntall = genererManntall(antallVelgere);

		for (Voter velger : manntall) {
			velger.setMvArea(stemmekrets);
		}

		Rodefordeler rodefordeler = new Rodefordeler(manntall);
		List<Rode> rodefordelinger = rodefordeler.calculateMostEvenDivision(4);

		Random randomGenerator = new Random(System.currentTimeMillis());

		List<PollingStation> roder = new ArrayList<>();
		for (int i = 0; i < rodefordelinger.size(); i++) {
			Rode rode = rodefordelinger.get(i);
			PollingStation pollingStation = new PollingStation();
			pollingStation.setFirst(rode.getFra());
			pollingStation.setLast(rode.getTil());
			pollingStation.setId(Integer.toString(i + 1));
			pollingStation.setPk(randomGenerator.nextLong());
			roder.add(pollingStation);
		}

		for (Pair<Voter, PollingStation> pair : rodefordeler.distribuerVelgereTil(roder)) {
			pair.getLeft().setPollingStation(pair.getRight());
		}

		return manntall;
	}

	private List<Voter> genererManntall(final int antallVelgere) {
		List<Voter> manntall = new ArrayList<>();

		Random randomGenerator = new Random(System.currentTimeMillis());

		for (int i = 0; i < antallVelgere; i++) {
			Voter velger = new Voter();
			velger.setLastName(Character.toString(EvoteConstants.ALPHABET.charAt(randomGenerator.nextInt(EvoteConstants.ALPHABET.length()))));
			velger.setLastName(velger.getLastName() + Character.toString(EvoteConstants.ALPHABET.charAt(randomGenerator.nextInt(EvoteConstants.ALPHABET.length()))));
			velger.setId("12354552");
			manntall.add(velger);
		}

		manntall.sort(Comparator.comparing(Voter::getLastName));

		return manntall;
	}

	private void settSammeRode(List<Voter> manntall) {
		for (Voter velger : manntall) {
			velger.setPollingStation(manntall.get(0).getPollingStation());
		}
	}

	private void sorterTilfeldig(List<Voter> manntall) {
		shuffle(manntall);
	}
}

