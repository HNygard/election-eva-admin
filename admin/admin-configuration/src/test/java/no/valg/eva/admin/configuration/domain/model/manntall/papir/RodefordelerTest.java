package no.valg.eva.admin.configuration.domain.model.manntall.papir;

import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.test.TestGroups;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class RodefordelerTest {

	private static final String BORDER_NAME = "Tor";
	private static final int FOUR = 4;
	private static final int THREE = 3;
	private static final int ZERO = 0;
	private static final int ONE = 1;
	private static final int TWO = 2;
	private static final int FIVE = 5;
	private static final int FOURTEEN = 14;
	private static final long ONE_LONG = 1L;
	private static final long TWO_LONG = 2L;
	private static final long THREE_LONG = 3L;
	private static final long FOUR_LONG = 4L;
	private static final long FIVE_LONG = 5L;

	private final Collator collatorNorsk = Collator.getInstance(Locale.forLanguageTag(EvoteConstants.DEFAULT_LOCALE));

	@Test
	public void testPutVotersInRangesTwoLetterDivisionOneLetterNamePutInFirst() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "SÅ"), new Rode("TA", "ÅÅ"));

		psd.tellAntallVelgerePerRode(divisionList);

		int personsInSecondRange = divisionList.get(ONE).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName("T").build());

		psd.tellAntallVelgerePerRode(divisionList);

		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ONE).getAntallVelgere(), personsInSecondRange + ONE);
	}

	@Test
	public void testPutVotersInRangesOneLetterDivisionOneLetterNamePutInFirst() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "S"), new Rode("T", "Å"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInSecondRange = divisionList.get(ONE).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName("T").build());

		psd.tellAntallVelgerePerRode(divisionList);

		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ONE).getAntallVelgere(), personsInSecondRange + ONE);
	}

	@Test
	public void testCalculateDivisionEmptyNamePutInFirst() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();

		Rodefordeler psd = new Rodefordeler(electoralRoll);

		List<Rode> roder = psd.calculateMostEvenDivision(FOUR);
		int personsInFirstRange = roder.get(ZERO).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName("").build());

		psd = new Rodefordeler(electoralRoll);
		psd.tellAntallVelgerePerRode(roder);
		int personsInFirstRangeWithNoNamePerson = roder.get(ZERO).getAntallVelgere();

		Assert.assertEquals(personsInFirstRangeWithNoNamePerson, personsInFirstRange + ONE);
	}

	@Test
	public void testCalculateDivisionNullNamePutInFirst() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();

		Rodefordeler psd = new Rodefordeler(electoralRoll);

		int personsInFirstRange = psd.calculateMostEvenDivision(FOUR).get(ZERO).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName(null).build());

		psd = new Rodefordeler(electoralRoll);

		int personsInFirstRangeWithNoNamePerson = psd.calculateMostEvenDivision(FOUR).get(ZERO).getAntallVelgere();

		Assert.assertEquals(personsInFirstRangeWithNoNamePerson, personsInFirstRange + ONE);
	}

	@Test
	public void testPutVotersInRangesTwoLetterDivisionNoNamePutInFirst() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "SÅ"), new Rode("TA", "ÅÅ"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInFirstRange = divisionList.get(ZERO).getAntallVelgere();
		electoralRoll.add(Voter.builder().lastName("").build());

		psd.tellAntallVelgerePerRode(divisionList);
		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ZERO).getAntallVelgere(), personsInFirstRange + ONE);
	}

	@Test
	public void testPutVotersInRangesTwoLetterDivisionNullNamePutInFirst() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "SÅ"), new Rode("TA", "ÅÅ"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInFirstRange = divisionList.get(ZERO).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName(null).build());
		psd.tellAntallVelgerePerRode(divisionList);

		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ZERO).getAntallVelgere(), personsInFirstRange + ONE);
	}

	@Test
	public void testcalculateVoterPerRangeBorderVoterIsInRangeTwoLettersPutInLastRange() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "TN"), new Rode("TO", "ÅÅ"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInLastRange = divisionList.get(ONE).getAntallVelgere();
		electoralRoll.add(Voter.builder().lastName(BORDER_NAME).build());

		int numberOfVoters = ZERO;
		psd.tellAntallVelgerePerRode(divisionList);

		for (Rode psddWithVoters : divisionList) {
			numberOfVoters += psddWithVoters.getAntallVelgere();
		}

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ONE).getAntallVelgere(), personsInLastRange + ONE);
	}

	@Test
	public void testcalculateVoterPerRangeBorderVoterIsInRangeOneLetterPutInFirstRange() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "T"), new Rode("U", "Å"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInFirstRange = divisionList.get(ZERO).getAntallVelgere();
		electoralRoll.add(Voter.builder().lastName(BORDER_NAME).build());

		psd.tellAntallVelgerePerRode(divisionList);

		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ZERO).getAntallVelgere(), personsInFirstRange + ONE);
	}

	@Test
	public void testcalculateVoterPerRangeBorderVoterIsInRangeOneLetterPutInLastRange() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "S"), new Rode("T", "Å"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInLastRange = divisionList.get(ONE).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName(BORDER_NAME).build());

		psd.tellAntallVelgerePerRode(divisionList);
		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ONE).getAntallVelgere(), personsInLastRange + ONE);
	}

	@Test
	public void testcalculateVoterPerRangeBorderVoterIsInRangeTwoLettersPutInFirstRange() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> divisionList = asList(new Rode("A", "TO"), new Rode("TP", "ÅÅ"));

		psd.tellAntallVelgerePerRode(divisionList);
		int personsInFirstRange = divisionList.get(ZERO).getAntallVelgere();

		electoralRoll.add(Voter.builder().lastName(BORDER_NAME).build());

		psd.tellAntallVelgerePerRode(divisionList);

		int numberOfVoters = summerAntallVelgere(divisionList);

		Assert.assertEquals(numberOfVoters, electoralRoll.size());
		Assert.assertEquals(divisionList.get(ZERO).getAntallVelgere(), personsInFirstRange + ONE);
	}

	@Test(groups = TestGroups.SLOW)
	public void testRangeWithOnlyOneLetterInTheLastRange() {
		Map<String, Integer> distributionMap = new HashMap<>();
		distributionMap.put("A", TWO);
		distributionMap.put("B", TWO);
		distributionMap.put("C", ONE);
		distributionMap.put("D", ONE);
		distributionMap.put("E", ONE);
		distributionMap.put("F", ONE);
		distributionMap.put("G", ZERO);
		distributionMap.put("H", ONE);
		distributionMap.put("I", FOUR);
		distributionMap.put("J", ZERO);
		distributionMap.put("K", THREE);
		distributionMap.put("L", ONE);
		distributionMap.put("M", ZERO);
		distributionMap.put("N", ZERO);
		distributionMap.put("O", ZERO);
		distributionMap.put("P", ONE);
		distributionMap.put("Q", TWO);
		distributionMap.put("R", ONE);
		distributionMap.put("S", ZERO);
		distributionMap.put("T", ONE);
		distributionMap.put("U", ZERO);
		distributionMap.put("V", ZERO);
		distributionMap.put("W", ONE);
		distributionMap.put("X", THREE);
		distributionMap.put("Y", ZERO);
		distributionMap.put("Z", ZERO);
		distributionMap.put("Å", ZERO);
		distributionMap.put("Æ", ZERO);
		distributionMap.put("Ø", THREE);

		List<Voter> electoralRoll = generateElectoralRollWithPredeterminedDistributionOneLetter(distributionMap);

		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> claimedBestRanges = psd.calculateMostEvenDivision(FIVE);
		calculateDivisionIsAsEqualAsPossible(FIVE, electoralRoll, claimedBestRanges);
	}

	@Test
	public void testCalculateDivisionOnlyOnePerson() {
		Map<String, Integer> distributionMap = lagDistribusjonForAlleBokstaver(ZERO);
		distributionMap.put("B", ONE);

		List<Voter> electoralRoll = generateElectoralRollWithPredeterminedDistributionOneLetter(distributionMap);
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> claimedBestRanges = psd.calculateMostEvenDivision(ONE);
		calculateDivisionIsAsEqualAsPossible(ONE, electoralRoll, claimedBestRanges);
	}

	@Test
	public void testCalculateDivisionEmptyElectoralRoll() {
		Map<String, Integer> distributionMap = lagDistribusjonForAlleBokstaver(ZERO);

		List<Voter> electoralRoll = generateElectoralRollWithPredeterminedDistributionOneLetter(distributionMap);
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> claimedBestRanges = psd.calculateMostEvenDivision(ONE);

		calculateDivisionIsAsEqualAsPossible(ONE, electoralRoll, claimedBestRanges);
	}

	private Map<String, Integer> lagDistribusjonForAlleBokstaver(int verdi) {
		return new HashMap<String, Integer>() {{
			put("A", verdi);
			put("B", verdi);
			put("C", verdi);
			put("D", verdi);
			put("E", verdi);
			put("F", verdi);
			put("G", verdi);
			put("H", verdi);
			put("I", verdi);
			put("J", verdi);
			put("K", verdi);
			put("L", verdi);
			put("M", verdi);
			put("N", verdi);
			put("O", verdi);
			put("P", verdi);
			put("Q", verdi);
			put("R", verdi);
			put("S", verdi);
			put("T", verdi);
			put("U", verdi);
			put("V", verdi);
			put("W", verdi);
			put("X", verdi);
			put("Y", verdi);
			put("Z", verdi);
			put("Å", verdi);
			put("Æ", verdi);
			put("Ø", verdi);
		}};
	}

	@Test
	public void testCalculateDivisionTestNoDuplicates() {
		Map<String, Integer> distributionMap = lagDistribusjonForAlleBokstaver(TWO);
		distributionMap.put("Y", ZERO);
		distributionMap.put("Z", ZERO);
		distributionMap.put("Å", ZERO);
		distributionMap.put("Æ", ZERO);

		List<Voter> electoralRoll = generateElectoralRollWithPredeterminedDistributionOneLetter(distributionMap);

		Rodefordeler psd = new Rodefordeler(electoralRoll);

		List<Rode> claimedBestRanges = psd.calculateMostEvenDivision(FOURTEEN);
		List<String> divisionLetterList = new ArrayList<>();
		for (Rode psdd : claimedBestRanges) {
			Assert.assertFalse(divisionLetterList.contains(psdd.getFra()));
			divisionLetterList.add(psdd.getFra());
			Assert.assertFalse(divisionLetterList.contains(psdd.getTil()));
			divisionLetterList.add(psdd.getTil());

		}
		calculateDivisionIsAsEqualAsPossible(FOURTEEN, electoralRoll, claimedBestRanges);
	}

	@Test
	public void testAssignVotersToPollingStationsOneLetter() {
		Map<String, Integer> distributionMap = new HashMap<>();
		distributionMap.put("A", TWO);
		distributionMap.put("B", TWO);
		distributionMap.put("C", ONE);
		distributionMap.put("D", ONE);
		distributionMap.put("E", ONE);
		distributionMap.put("F", ONE);
		distributionMap.put("G", ZERO);
		distributionMap.put("H", ONE);
		distributionMap.put("I", FOUR);
		distributionMap.put("J", ZERO);
		distributionMap.put("K", THREE);
		distributionMap.put("L", ONE);
		distributionMap.put("M", ZERO);
		distributionMap.put("N", ZERO);
		distributionMap.put("O", ZERO);
		distributionMap.put("P", ONE);
		distributionMap.put("Q", TWO);
		distributionMap.put("R", ONE);
		distributionMap.put("S", ZERO);
		distributionMap.put("T", ONE);
		distributionMap.put("U", ZERO);
		distributionMap.put("V", ZERO);
		distributionMap.put("W", ONE);
		distributionMap.put("X", THREE);
		distributionMap.put("Y", ZERO);
		distributionMap.put("Z", ZERO);
		distributionMap.put("Å", ZERO);
		distributionMap.put("Æ", ZERO);
		distributionMap.put("Ø", THREE);

		List<Voter> electoralRoll = generateElectoralRollWithPredeterminedDistributionOneLetter(distributionMap);

		List<PollingStation> roder = asList(
			new PollingStation("0", "A", "D"),
			new PollingStation("1", "E","H"),
			new PollingStation("2", "I","M"),
			new PollingStation("3", "N", "R"),
			new PollingStation("4", "S", "Å")
		);

		List<Pair<Voter, PollingStation>> mappings = new Rodefordeler(electoralRoll).distribuerVelgereTil(roder);
		for (Pair<Voter, PollingStation> mapping : mappings) {
			assertCorrectMapping(roder, mapping);
		}
	}

	private void assertCorrectMapping(List<PollingStation> roder, Pair<Voter, PollingStation> mapping) {
		if (mapping.getLeft().getLastName().toUpperCase().startsWith("AA")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("A")) {
			Assert.assertEquals(mapping.getRight(), roder.get(0));
		} else if (mapping.getLeft().getLastName().startsWith("B")) {
			Assert.assertEquals(mapping.getRight(), roder.get(0));
		} else if (mapping.getLeft().getLastName().startsWith("C")) {
			Assert.assertEquals(mapping.getRight(), roder.get(0));
		} else if (mapping.getLeft().getLastName().startsWith("D")) {
			Assert.assertEquals(mapping.getRight(), roder.get(0));
		} else if (mapping.getLeft().getLastName().startsWith("E")) {
			Assert.assertEquals(mapping.getRight(), roder.get(1));
		} else if (mapping.getLeft().getLastName().startsWith("F")) {
			Assert.assertEquals(mapping.getRight(), roder.get(1));
		} else if (mapping.getLeft().getLastName().startsWith("G")) {
			Assert.assertEquals(mapping.getRight(), roder.get(1));
		} else if (mapping.getLeft().getLastName().startsWith("H")) {
			Assert.assertEquals(mapping.getRight(), roder.get(1));
		} else if (mapping.getLeft().getLastName().startsWith("I")) {
			Assert.assertEquals(mapping.getRight(), roder.get(2));
		} else if (mapping.getLeft().getLastName().startsWith("J")) {
			Assert.assertEquals(mapping.getRight(), roder.get(2));
		} else if (mapping.getLeft().getLastName().startsWith("K")) {
			Assert.assertEquals(mapping.getRight(), roder.get(2));
		} else if (mapping.getLeft().getLastName().startsWith("L")) {
			Assert.assertEquals(mapping.getRight(), roder.get(2));
		} else if (mapping.getLeft().getLastName().startsWith("M")) {
			Assert.assertEquals(mapping.getRight(), roder.get(2));
		} else if (mapping.getLeft().getLastName().startsWith("N")) {
			Assert.assertEquals(mapping.getRight(), roder.get(3));
		} else if (mapping.getLeft().getLastName().startsWith("O")) {
			Assert.assertEquals(mapping.getRight(), roder.get(3));
		} else if (mapping.getLeft().getLastName().startsWith("P")) {
			Assert.assertEquals(mapping.getRight(), roder.get(3));
		} else if (mapping.getLeft().getLastName().startsWith("Q")) {
			Assert.assertEquals(mapping.getRight(), roder.get(3));
		} else if (mapping.getLeft().getLastName().startsWith("R")) {
			Assert.assertEquals(mapping.getRight(), roder.get(3));
		} else if (mapping.getLeft().getLastName().startsWith("S")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("T")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("U")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("V")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("X")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("Y")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("Z")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("Å")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("Æ")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		} else if (mapping.getLeft().getLastName().startsWith("Ø")) {
			Assert.assertEquals(mapping.getRight(), roder.get(4));
		}
	}

	@Test
	public void testGermanYIsPlacedInAPollingStation() {
		List<PollingStation> roder = asList(
			new PollingStation(ONE_LONG, "0", "A", "D"), 
			new PollingStation(TWO_LONG, "1", "E","H"),
			new PollingStation(THREE_LONG, "2", "I","M"),
			new PollingStation(FOUR_LONG, "3", "N", "X"),
			new PollingStation(FIVE_LONG, "4", "Y", "Å")
		);
		List<Voter> manntall = asList(Voter.builder().lastName("Über").build());

		List<Pair<Voter, PollingStation>> mappings = new Rodefordeler(manntall).distribuerVelgereTil(roder);

		Assert.assertEquals(mappings.get(ZERO).getRight(), roder.get(4));
	}

	@Test
	public void testAssignVotersToPollingStationsTwoLetter() {
		Map<String, Integer> distributionMap = new HashMap<>();
		distributionMap.put("A", TWO);
		distributionMap.put("B", TWO);
		distributionMap.put("C", ONE);
		distributionMap.put("D", ONE);
		distributionMap.put("E", ONE);
		distributionMap.put("F", ONE);
		distributionMap.put("G", ZERO);
		distributionMap.put("H", ONE);
		distributionMap.put("I", FOUR);
		distributionMap.put("J", ZERO);
		distributionMap.put("K", THREE);
		distributionMap.put("L", ONE);
		distributionMap.put("M", ZERO);
		distributionMap.put("N", ZERO);
		distributionMap.put("O", ZERO);
		distributionMap.put("P", ONE);
		distributionMap.put("Q", TWO);
		distributionMap.put("R", ONE);
		distributionMap.put("S", ZERO);
		distributionMap.put("T", ONE);
		distributionMap.put("U", ZERO);
		distributionMap.put("V", ZERO);
		distributionMap.put("W", ONE);
		distributionMap.put("X", THREE);
		distributionMap.put("Y", ZERO);
		distributionMap.put("Z", ZERO);
		distributionMap.put("Å", ZERO);
		distributionMap.put("Æ", ZERO);
		distributionMap.put("Ø", THREE);

		List<Voter> manntall = generateElectoralRollWithPredeterminedDistributionTwoLetters(distributionMap);

		List<PollingStation> roder = asList(
			new PollingStation("0", "A", "D"),
			new PollingStation("1", "E","H"),
			new PollingStation("2", "I","M"),
			new PollingStation("3", "N", "R"),
			new PollingStation("4", "S", "Å")
		);

		List<Pair<Voter, PollingStation>> mappings = new Rodefordeler(manntall).distribuerVelgereTil(roder);
		for (Pair<Voter, PollingStation> mapping : mappings) {
			assertCorrectMapping(roder, mapping);
		}
	}

	@Test
	public void testPutVotersInRangesSameAsRefTwoLetters() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
//		List<Rode> claimedBestRanges = psd.calculateMostEvenDivision(FIVE);
		List<Rode> claimedBestRanges = asList(new Rode("A", "EB"), new Rode("EC", "MA"), new Rode("MB", "SR"), new Rode("SS", "ÅÅ"));

		psd.tellAntallVelgerePerRode(claimedBestRanges);

		List<Rode> referenceDivisionList = asList(new Rode("A", "EB"), new Rode("EC", "MA"), new Rode("MB", "SR"), new Rode("SS", "ÅÅ"));

		calculateVoterPerRangeRef(referenceDivisionList, electoralRoll);

		for (int c = ZERO; c < claimedBestRanges.size(); c++) {
			Assert.assertEquals(claimedBestRanges.get(c).getFra(), referenceDivisionList.get(c).getFra());
			Assert.assertEquals(claimedBestRanges.get(c).getTil(), referenceDivisionList.get(c).getTil());
		}

	}

	@Test
	public void testPutVotersInRangesSameNumberOfVotersTwoLetters() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		int electoralRollOriginalSize = electoralRoll.size();
		Rodefordeler psd = new Rodefordeler(electoralRoll);

		List<Rode> claimedBestRanges = asList(new Rode("A", "EB"), new Rode("EC", "MA"), new Rode("MB", "SR"), new Rode("SS", "ÅÅ"));

		psd.tellAntallVelgerePerRode(claimedBestRanges);

		int numberOfVoters = summerAntallVelgere(claimedBestRanges);

		Assert.assertEquals(numberOfVoters, electoralRollOriginalSize);
	}

	@Test
	public void testPutVotersInRangesSameNumberOfVotersOneLetter() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		int electoralRollOriginalSize = electoralRoll.size();
		Rodefordeler psd = new Rodefordeler(electoralRoll);

		List<Rode> claimedBestRanges = asList(new Rode("A", "E"), new Rode("F", "M"), new Rode("N", "S"), new Rode("T", "Å"));

		psd.tellAntallVelgerePerRode(claimedBestRanges);

		int numberOfVoters = summerAntallVelgere(claimedBestRanges);

		Assert.assertEquals(numberOfVoters, electoralRollOriginalSize);
	}

	@Test(groups = TestGroups.SLOW)
	public void testCalculateDivisionIsAsEqualAsPossible() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> psdds = psd.calculateMostEvenDivision(THREE);

		calculateDivisionIsAsEqualAsPossible(THREE, electoralRoll, psdds);
	}

	private void calculateDivisionIsAsEqualAsPossible(final int numberOfGroups, final List<Voter> electoralRoll, final List<Rode> claimedBestRanges) {
		List<Rode> ranges = new ArrayList<>();

		for (int i = ZERO; i < numberOfGroups; i++) {
			ranges.add(tomRode());
		}
		List<Rode> bestRanges = new ArrayList<>();
		calculateBestRangeByTesting(ZERO, ZERO, ranges, bestRanges, electoralRoll);

		Assert.assertEquals(findRangeVariance(claimedBestRanges), findRangeVariance(bestRanges));
	}

	private Rode tomRode() {
		return new Rode("A", "A");
	}

	@Test
	public void testRightNumberOfRanges() {
		List<Voter> electoralRoll = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler psd = new Rodefordeler(electoralRoll);
		List<Rode> psdds = psd.calculateMostEvenDivision(THREE);

		Assert.assertEquals(THREE, psdds.size());
	}

	@Test
	public void testOnlyOneLetterInDivisions() {
		List<Voter> manntall = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler rodefordeler = new Rodefordeler(manntall);
		List<Rode> roder = rodefordeler.calculateMostEvenDivision(THREE);

		for (Rode rode : roder) {
			Assert.assertEquals(rode.getFra().length(), ONE);
			Assert.assertEquals(rode.getTil().length(), ONE);
		}
	}

	
	@Test
	public void testRangesCoversTheWholeAlphabet() {
		List<Voter> manntall = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler rodefordeler = new Rodefordeler(manntall);

		List<Rode> roder = rodefordeler.calculateMostEvenDivision(THREE);

		sjekkAtRodefordelingDekkerHeleAlfabetet(roder);
		for (int i = ZERO; i < roder.size(); i++) {
			sjekkAtFraErFoerTilIAlfabetet(roder.get(i));
			if (i < roder.size() - ONE) {
				sjekkAtEnRodeneErRettEtterHverandre(roder.get(i), roder.get(i + ONE));
			}
		}
	}

	@Test
	public void testOneLetterPollingStation() {
		List<Voter> manntall = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler rodefordeler = new Rodefordeler(manntall);
		List<Rode> roder = asList(new Rode("A", "B"), new Rode("B", "B"), new Rode("C", "Å"));

		rodefordeler.tellAntallVelgerePerRode(roder);

		int numberOfVoters = summerAntallVelgere(roder);
		Assert.assertEquals(numberOfVoters, manntall.size());
		Assert.assertEquals(roder.get(ONE).getAntallVelgere(), 33);
	}

	@Test(dataProvider = "etternavnMedAaTestData")
	public void tellAntallVelgerePerRode_etternavnMedAa(List<Rode> roder) {
		List<Voter> manntall = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler rodefordeler = new Rodefordeler(manntall);

		rodefordeler.tellAntallVelgerePerRode(roder);

		int personsInLastRange = roder.get(ONE).getAntallVelgere();
		manntall.add(Voter.builder().lastName("AA").build());

		rodefordeler.tellAntallVelgerePerRode(roder);

		int antallVelgere = summerAntallVelgere(roder);
		Assert.assertEquals(antallVelgere, manntall.size());
		Assert.assertEquals(roder.get(ONE).getAntallVelgere(), personsInLastRange + ONE);
	}

	private int summerAntallVelgere(List<Rode> roder) {
		int antallVelgere = ZERO;
		for (Rode rode : roder) {
			antallVelgere += rode.getAntallVelgere();
		}
		return antallVelgere;
	}

	@DataProvider
	private Object[][] etternavnMedAaTestData() {
		return new Object[][] {
			{ asList(new Rode("A", "TN"), new Rode("TO", "ÅÅ")) },
			{ asList(new Rode("A", "T"), new Rode("S", "Å")) }
		};
	}

	@Test
	public void testMapUsersOnlyOnePollingStation() {
		List<Voter> manntall = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler rodefordeler = new Rodefordeler(manntall);
		PollingStation rode = new PollingStation("A", "Å");
		List<PollingStation> roder = asList(rode);

		List<Pair<Voter, PollingStation>> mappings = rodefordeler.distribuerVelgereTil(roder);

		Assert.assertEquals(mappings.size(), manntall.size());
		for (Pair<Voter, PollingStation> mapping : mappings) {
			Assert.assertEquals(mapping.getRight(), rode);
			Assert.assertTrue(manntall.contains(mapping.getLeft()));
			manntall.remove(mapping.getLeft());
		}
	}

	@Test
	public void testCalculateVoterPerRangeOnlyOnePollingStation() {
		List<Voter> manntall = lagManntallMedToBokstaverIEtternavn();
		Rodefordeler rodefordeler = new Rodefordeler(manntall);
		List<Rode> roder = asList(new Rode("A", "Å"));

		rodefordeler.tellAntallVelgerePerRode(roder);

		Assert.assertEquals(manntall.size(), roder.get(0).getAntallVelgere());
	}

	/**
	 * Genererer et manntall. Kun etternavn (to bokstaver) og velger-ID er satt. Manntallet er ikke sortert.
	 */
	private static List<Voter> lagManntallMedToBokstaverIEtternavn() {
		Map<String, Integer> distributionMap = new HashMap<>();
		distributionMap.put("A", 35);
		distributionMap.put("B", 33);
		distributionMap.put("C", 41);
		distributionMap.put("D", 50);
		distributionMap.put("E", 65);
		distributionMap.put("F", 42);
		distributionMap.put("G", 87);
		distributionMap.put("H", 43);
		distributionMap.put("I", 30);
		distributionMap.put("J", 36);
		distributionMap.put("K", 43);
		distributionMap.put("L", 32);
		distributionMap.put("M", 44);
		distributionMap.put("N", 54);
		distributionMap.put("O", 32);
		distributionMap.put("P", 43);
		distributionMap.put("Q", 35);
		distributionMap.put("R", 53);
		distributionMap.put("S", 45);
		distributionMap.put("T", 32);
		distributionMap.put("U", 37);
		distributionMap.put("V", 32);
		distributionMap.put("W", 65);
		distributionMap.put("X", 43);
		distributionMap.put("Y", 76);
		distributionMap.put("Z", 32);
		distributionMap.put("Å", 64);
		distributionMap.put("Æ", 42);
		distributionMap.put("Ø", 33);

		return generateElectoralRollWithPredeterminedDistributionTwoLetters(distributionMap);
	}

	/**
	 * Generates a electoral roll based of the distribution of first letters in the distributionMap. The first letter of the last name is taken from the map.
	 * Only the first letter of the last name and the voter id are set. The electoral roll is not sorted.
	 */
	private static List<Voter> generateElectoralRollWithPredeterminedDistributionOneLetter(final Map<String, Integer> distrubutionMap) {
		List<Voter> electoralRoll = new ArrayList<>();

		for (String key : distrubutionMap.keySet()) {
			for (int i = ZERO; i < distrubutionMap.get(key); i++) {
				Voter v = new Voter();
				v.setLastName(key);
				v.setId("testid");
				electoralRoll.add(v);
			}
		}
		shuffle(electoralRoll);

		return electoralRoll;
	}

	/**
	 * Generates a electoral roll based of the distribution of first letters in the distributionMap. The first letter of the last name is taken from the map.
	 * The second is taken in sequence from the alphabet. Only the first and second letter of the last name and the voter id are set. The electoral roll is not
	 * sorted.
	 */
	private static List<Voter> generateElectoralRollWithPredeterminedDistributionTwoLetters(final Map<String, Integer> distrubutionMap) {
		List<Voter> electoralRoll = new ArrayList<>();

		int alphebetIndex = ZERO;
		for (String key : distrubutionMap.keySet()) {
			for (int i = ZERO; i < distrubutionMap.get(key); i++) {
				Voter v = new Voter();
				v.setLastName(key + Character.toString(EvoteConstants.ALPHABET.charAt(alphebetIndex)));
				alphebetIndex++;
				v.setId("testid");
				electoralRoll.add(v);

				if (alphebetIndex == EvoteConstants.ALPHABET.length()) {
					alphebetIndex = ZERO;
				}
			}
		}
		shuffle(electoralRoll);

		return electoralRoll;
	}

	private void sjekkAtEnRodeneErRettEtterHverandre(final Rode currentRange, final Rode nextRange) {
		Assert.assertEquals((nextRange.getFra().charAt(ZERO)), nesteBokstavIAlfabetet(currentRange.getTil().charAt(ZERO)).charValue());
	}

	private void sjekkAtFraErFoerTilIAlfabetet(final Rode range) {
		Assert.assertTrue((range.getFra().charAt(ZERO)) < (range.getTil().charAt(ZERO)));
	}

	private void sjekkAtRodefordelingDekkerHeleAlfabetet(final List<Rode> ranges) {
		Assert.assertEquals(ranges.get(ZERO).getFra(), Character.toString(EvoteConstants.ALPHABET.charAt(ZERO)));

		Assert.assertEquals(ranges.get(ranges.size() - ONE).getTil(),
				Character.toString(EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.length() - ONE)));
	}

	private Character nesteBokstavIAlfabetet(final Character c) {
		return EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.indexOf(c) + ONE);
	}

	/*
	 * Calculates all possible ranges and tries which one gives the most equal division of the electoral roll
	 */
	private void calculateBestRangeByTesting(final int startIndex, final int rangeIndex, final List<Rode> ranges,
											 final List<Rode> bestRanges, final List<Voter> electoralRoll) {
		int charsToLeaveAtTheEnd = ((ranges.size() - rangeIndex) - ONE) * TWO;
		for (int i = startIndex + ONE; i < EvoteConstants.ALPHABET.length() - charsToLeaveAtTheEnd; i++) {
			ranges.set(rangeIndex, beregnRode(startIndex, rangeIndex, ranges, i));
			
			if (rangeIndex < ranges.size() - ONE) {
				calculateBestRangeByTesting(i + ONE, rangeIndex + ONE, ranges, bestRanges, electoralRoll);
			} else {
				clearRangesNumbers(ranges);
				calculateVoterPerRangeRef(ranges, electoralRoll);
				if (isRangeMoreEvenThanTheBest(ranges, bestRanges)) {
					bestRanges.clear();
					for (Rode range : ranges) {
						bestRanges.add(new Rode(range.getFra(), range.getTil(), range.getAntallVelgere()));
					}
				}
			}
		}
	}

	private Rode beregnRode(int startIndex, int rangeIndex, List<Rode> ranges, int i) {
		return new Rode(beregnFra(startIndex), beregnTil(rangeIndex, ranges, i));
	}

	private String beregnFra(int startIndex) {
		return Character.toString(EvoteConstants.ALPHABET.charAt(startIndex));
	}

	private String beregnTil(int rangeIndex, List<Rode> ranges, int i) {
		if (rangeIndex == ranges.size() - ONE) {
			return Character.toString(EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.length() - ONE));
		} else {
			return Character.toString(EvoteConstants.ALPHABET.charAt(i));
		}
	}

	private void clearRangesNumbers(final List<Rode> ranges) {
		for (Rode psdd : ranges) {
			psdd.setAntallVelgere(ZERO);
		}
	}

	private void calculateVoterPerRangeRef(final List<Rode> ranges, final List<Voter> electoralRoll) {
		for (Voter v : electoralRoll) {
			for (Rode psdd : ranges) {
				if (isNameInRange(v.getLastName(), psdd.getFra(), psdd.getTil())) {
					psdd.setAntallVelgere(psdd.getAntallVelgere() + ONE);
					break;
				}

			}

		}
	}

	private boolean isNameInRange(final String lastName, final String rangeStart, final String rangeEnd) {
		String rangeStartUpper = rangeStart.toUpperCase();
		String rangeEndUpper = rangeEnd.toUpperCase();

		if (lastName == null || lastName.length() == ZERO) {
			return "A".equals(rangeStartUpper);
		}

		String lastNameUpper = lastName.toUpperCase();
		if (lastNameUpper.startsWith("AA")) {
			lastNameUpper = "Å" + lastNameUpper.substring(TWO);
		}
		boolean startsWithFirstDivider = lastNameUpper.startsWith(rangeStartUpper);
		boolean startsWithLastDivider = lastNameUpper.startsWith(rangeEndUpper);
		boolean isSomewhereBetween = collatorNorsk.compare(lastNameUpper, rangeStartUpper) > ZERO
				&& collatorNorsk.compare(lastNameUpper, rangeEndUpper) < ZERO;

		if (lastNameUpper.length() == ONE) {
			return (rangeStartUpper.startsWith(lastNameUpper) || rangeEndUpper.startsWith(lastNameUpper)) || isSomewhereBetween;
		}

		return startsWithFirstDivider || startsWithLastDivider || isSomewhereBetween;
	}

	private boolean isRangeMoreEvenThanTheBest(final List<Rode> ranges, final List<Rode> bestRanges) {
		if (bestRanges.isEmpty()) {
			return true;
		}
		double rangesVariance = findRangeVariance(ranges);
		double bestRangesVariance = findRangeVariance(bestRanges);

		return rangesVariance < bestRangesVariance;
	}

	private double findRangeVariance(final List<Rode> ranges) {
		Variance variance = new Variance();
		double[] valueArray = new double[ranges.size()];
		for (int i = ZERO; i < ranges.size(); i++) {
			valueArray[i] = ranges.get(i).getAntallVelgere();
		}

		return variance.evaluate(valueArray);
	}
}

