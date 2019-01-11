package no.valg.eva.admin.configuration.domain.model;

import static no.valg.eva.admin.configuration.domain.model.Voter.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/* Se VoterValideringTest for tester relatert til validering */
public class VoterTest {

	@Test
	public void oppdaterStemmerett_voterApproved_eligibleTrue() {
		Voter velger = velger(true, false);

		velger.oppdaterStemmerett();

		assertThat(velger.isEligible()).isTrue();
	}

	@Test
	public void oppdaterStemmerett_voterNotApproved_eligibleFalse() {
		Voter velger = velger(false, true);

		velger.oppdaterStemmerett();

		assertThat(velger.isEligible()).isFalse();
	}

	@Test
	public void isStemmerettOgsaVedSametingsvalg_medFlagg_returnsTrue() {
		Voter velger = velger(false, true);
		velger.setAdditionalInformation("@electoralRoll.eligigbleInSamiElection");

		assertThat(velger.isStemmerettOgsaVedSametingsvalg()).isTrue();
	}

	@Test
	public void isStemmerettOgsaVedSametingsvalg_utenFlagg_returnsFalse() {
		Voter velger = velger(false, true);
		velger.setAdditionalInformation(null);

		assertThat(velger.isStemmerettOgsaVedSametingsvalg()).isFalse();
	}

	private Voter velger(boolean godkjent, boolean stemmerett) {
		Voter velger = new Voter();
		velger.setApproved(godkjent);
		velger.setEligible(stemmerett);
		return velger;
	}

	private Voter velger() {
		Voter velger = new Voter();
		velger.setElectionEvent(new ElectionEvent());
		velger.setId("25038014008");
		velger.setFirstName("Firstname");
		velger.setLastName("Lastname");
		velger.setNameLine("Lastname Firstname");
		return velger;
	}


	@Test
	public void updateNameLineShouldConcatenateFirstMiddleAndLastName() {
		Voter velger = new Voter();
		velger.setFirstName("First");
		velger.setMiddleName("Middle");
		velger.setLastName("Last");
		velger.updateNameLine();
		assertEquals(velger.getNameLine(), "First Middle Last");
	}

	@Test
	public void updateNameLineShouldOmitMiddleNameIfNotSet() {
		Voter velger = new Voter();
		velger.setFirstName("First");
		velger.setLastName("Last");
		velger.updateNameLine();
		assertEquals(velger.getNameLine(), "First Last");
	}

	@Test
	public void setDateOfBirthFromFodselsnummerIfMissing_ifIdIsValid_birthDateIsUpdated() {
		Voter velger = velger();
		assertThat(velger.getDateOfBirth()).isNull();
		LocalDate voterBirthDate = new LocalDate(1980, 3, 25);

		velger.setDateOfBirthFromFodselsnummerIfMissing();

		assertThat(velger.getDateOfBirth()).isEqualTo(voterBirthDate);
	}

	@Test(dataProvider = "velgereMedForskjelligeBosteder")
	public void erBosattINorge(Voter velger, boolean erBosattINorge) {
		assertThat(velger.erBosattINorge()).isEqualTo(erBosattINorge);
	}

	@DataProvider
	private Object[][] velgereMedForskjelligeBosteder() {
		return new Object[][] {
			{ velger(false, null, "000"), true },
			{ velger(true, "Adresse i Norge", "000"), true }	,
			{ velger(true, "Adresse i Sverige", "046"), false }
		};
	}

	private Voter velger(boolean harPostadresse, String postadresse, String landskode) {
		Voter velger = velger();
		velger.setMailingAddressSpecified(harPostadresse);
		velger.setMailingAddressLine1(postadresse);
		velger.setMailingCountryCode(landskode);
		velger.setFictitious(false);
		return velger;
	}

	@Test(dataProvider = "velgereForTestAvTilknytningTilStemmekrets")
	public void erTilknyttetStemmekrets(Voter velger, boolean forventetResultat) throws Exception {
		assertThat(velger.erTilknyttetStemmekrets()).isEqualTo(forventetResultat);
	}

	@DataProvider
	private Object[][] velgereForTestAvTilknytningTilStemmekrets() {
		return new Object[][]{
			{ velger(false, null), false },
			{ velger(true, "1234"), true }
		};
	}

	private Voter velger(boolean erTilknyttetStemmekrets, String stemmekretsId) {
		Voter velger = new Voter();
		if (erTilknyttetStemmekrets) {
			velger.setMvArea(new MvArea());
			velger.getMvArea().setPollingDistrictId(stemmekretsId);
		}
		return velger;
	}

	@Test(dataProvider = "velgereForTestAvHeleKretsenTilknytning")
	public void erTilknyttetKretsenForHeleKommunen(Voter velger, boolean forventetResultat) throws Exception {
		assertThat(velger.erTilknyttetKretsenForHeleKommunen()).isEqualTo(forventetResultat);
	}

	@DataProvider
	private Object[][] velgereForTestAvHeleKretsenTilknytning() {
		return new Object[][]{
			{ velger(false, null), false },
			{ velger(true, "1234"), false },
			{ velger(true, "0000"), true }
		};
	}

	@Test(dataProvider = "velgereMedOgUtenManntallsnummer")
	public void harManntallsnummer(Voter velger, boolean forventetResultat) {
		assertThat(velger.harManntallsnummer()).isEqualTo(forventetResultat);
	}

	@DataProvider
	private Object[][] velgereMedOgUtenManntallsnummer() {
		return new Object[][]{
			{ builder().number(null).build(), 		false },
			{ builder().number(1L).build(), 		true },
			{ builder().number(1234567L).build(), 	true }
		};
	}

	@Test(dataProvider = "velgereMedOgUtenSideOgLinje")
	public void harSideOgLinje(Voter velger, boolean forventetResultat) {
		assertThat(velger.harSideOgLinje()).isEqualTo(forventetResultat);
	}

	@DataProvider
	private Object[][] velgereMedOgUtenSideOgLinje() {
		return new Object[][]{
			{ velger(null, null), false },
			{ velger(1, null), 	 false },
			{ velger(null, 1), 	 false },
			{ velger(0, 0), 		 false },
			{ velger(1, 0), 		 false },
			{ velger(0, 1), 		 false },
			{ velger( 1, 1), 	 true }
		};
	}

	private Voter velger(Integer side, Integer linje) {
		return builder().electoralRollPage(side).electoralRollLine(linje).build();
	}
	
	@Test(dataProvider = "fullNameTestData")
	public void fullName(Voter voter, String expectedFullName) {
		assertThat(voter.getFullName()).isEqualTo(expectedFullName);
	}
	
	@DataProvider
	private Object[][] fullNameTestData() {
		return new Object[][] {
			{ builder().firstName("First").middleName("Middle").lastName("Last").build(), 	"First Middle Last" },	
			{ builder().firstName("First").lastName("Last").build(), 						"First Last" },	
			{ builder().firstName("First").build(), 										"First" },	
			{ builder().lastName("Last").build(), 											"Last" },	
		};
	}
	
}
