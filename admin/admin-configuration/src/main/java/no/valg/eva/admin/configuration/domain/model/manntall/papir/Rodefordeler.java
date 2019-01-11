package no.valg.eva.admin.configuration.domain.model.manntall.papir;

import static no.evote.constants.EvoteConstants.ALPHABET;
import static no.evote.constants.EvoteConstants.DEFAULT_LOCALE;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

public class Rodefordeler {

	private static final Locale LOCALE = Locale.forLanguageTag(DEFAULT_LOCALE);
	private static final Collator COLLATOR = Collator.getInstance(LOCALE);
	private final List<Voter> manntall;
	private final Map<String, Integer> manntallDistribusjonAvForbokstavIEtternavn;

	public Rodefordeler(final List<Voter> manntall) {
		this.manntall = manntall;
		manntallDistribusjonAvForbokstavIEtternavn = new HashMap<>();
	}

	static boolean erEtternavnIIntervall(final String etternavnInput, final String intervallStartInput, final String nesteIntervallStartInput) {
		String etternavn = storeBokstaver(etternavnInput);
		String intervallStart = justerHvisKortEtternavn(storeBokstaver(intervallStartInput), etternavn);
		String nesteIntervallStart = justerHvisKortEtternavn(storeBokstaver(nesteIntervallStartInput), etternavn);
		boolean erFoersteIntervall = intervallStartInput == null;
		boolean erSisteIntervall = nesteIntervallStartInput == null;

		if (erFoersteIntervall && erSisteIntervall) {
			return true;
		}
		
		if (etternavn == null || etternavn.length() == 0) {
			return erFoersteIntervall;
		}

		if (erSisteIntervall) {
			return COLLATOR.compare(etternavn, intervallStart) >= 0;
		} else if (erFoersteIntervall) {
			return COLLATOR.compare(etternavn, nesteIntervallStart) < 0;
		} else {
			return COLLATOR.compare(etternavn, intervallStart) >= 0 && COLLATOR.compare(etternavn, nesteIntervallStart) < 0;
		}
	}

	private static String storeBokstaver(String tekst) {
		if (tekst == null) {
			return null;
		} else {
			return tekst.toUpperCase(LOCALE);
		}
	}

	private static String justerHvisKortEtternavn(String intervallgrense, String etternavn) {
		if (etternavn != null && etternavn.length() == 1 && erToBokstaver(intervallgrense)) {
			return intervallgrense.substring(0, 0);
		} else {
			return intervallgrense;
		}
	}

	private static boolean erToBokstaver(String streng) {
		return streng != null && streng.length() == 2;
	}

	public static void sorterRoder(final List<Rode> roder) {
		roder.sort((o1, o2) -> COLLATOR.compare(o1.getFra(), o2.getFra()));
	}

	private static void normalizePollingStationList(final List<PollingStation> pollingStations) {
		for (PollingStation pollingStation : pollingStations) {
			pollingStation.setFirst(pollingStation.getFirst().toUpperCase());
			pollingStation.setLast(pollingStation.getLast().toUpperCase());
		}

		pollingStations.sort((o1, o2) -> COLLATOR.compare(o1.getFirst(), o2.getFirst()));
	}

	/*
	 * Beregner distribusjon av forbokstav i etternavn. Velgere uten etternavn blir telt i Voters without last names are counted as starting on A
	 * 
	 * DEV-NOTE: Denne funksjonen burde vært gjort om til en sideeffektfri funksjon
	 * DEV-NOTE: Denne funksjonen er forøvrig O(n^2), og derfor svært ineffektiv ved store datamengder. Det er muligens ikke noe problem her
	 */
	private void beregnDistribusjonForForbokstavIEtternavn() {
		for (int bokstavIndex = 0; bokstavIndex < ALPHABET.length(); bokstavIndex++) {
			String bokstav = Character.toString(ALPHABET.charAt(bokstavIndex));
			int antallVelgere = 0;
			String fra = bokstavIndex == 0 ? null : Character.toString(ALPHABET.charAt(bokstavIndex));
			String til = bokstavIndex == ALPHABET.length() - 1 ? null : Character.toString(ALPHABET.charAt(bokstavIndex + 1));
			for (Voter velger : manntall) {
				if (erEtternavnIIntervall(velger.getLastName(), fra, til)) {
					antallVelgere++;
				}
			}
			manntallDistribusjonAvForbokstavIEtternavn.put(bokstav, antallVelgere);
		}
	}

	/**
	 * Calculates the most even division of polling stations
	 * 
	 * @param numberOfStations
	 *            to be created
	 * @return A list of polling station division DTO objects representing he division of polling stations
	 */
	public List<Rode> calculateMostEvenDivision(final Integer numberOfStations) {
		beregnDistribusjonForForbokstavIEtternavn();
		List<String> distrubutionMapKeys = new ArrayList<>(manntallDistribusjonAvForbokstavIEtternavn.keySet());

		distrubutionMapKeys.sort(COLLATOR);
		List<List<Rode>> allRanges = new ArrayList<>();

		splitSet(distrubutionMapKeys, numberOfStations, new ArrayList<>(), allRanges);

		return findTheBestRange(allRanges);

	}

	/*
	 * Finds and returns the range with the least variance
	 */
	private List<Rode> findTheBestRange(final List<List<Rode>> allRanges) {
		List<Rode> bestRanges = allRanges.get(0);
		for (List<Rode> ranges : allRanges) {
			double vaiance = findRangeVariance(ranges);
			if (vaiance < findRangeVariance(bestRanges)) {
				bestRanges = ranges;
			}
		}
		return bestRanges;

	}

	/*
	 * Recursively find possible divisions
	 */
	private void splitSet(final List<String> distrubutionMapKeys, final int numberOfGroups, final List<Rode> workingRanges,
			final List<List<Rode>> allRanges) {
		if (rangeWillEndInDuplicate(distrubutionMapKeys)) {
			return;
		}

		if (numberOfGroups > 1) {
			int numberOfPeoplePerGroup = countPeopleInSet(distrubutionMapKeys) / numberOfGroups;
			int numberOfPeopleInGroup = manntallDistribusjonAvForbokstavIEtternavn.get(distrubutionMapKeys.get(0));
			int numberOfLettersLeft;
			int keyIndex = 0;

			do {
				keyIndex++;
				numberOfPeopleInGroup += manntallDistribusjonAvForbokstavIEtternavn.get(distrubutionMapKeys.get(keyIndex));
				numberOfLettersLeft = distrubutionMapKeys.size() - (keyIndex + 1);
			} while (addMorePeople(numberOfPeopleInGroup, numberOfLettersLeft, numberOfGroups, numberOfPeoplePerGroup));

			Rode psddBefore = createGroupToIndex(distrubutionMapKeys, keyIndex - 1, numberOfPeopleInGroup
					- manntallDistribusjonAvForbokstavIEtternavn.get(distrubutionMapKeys.get(keyIndex)));

			List<Rode> workingRangesBefore = new ArrayList<>();
			workingRangesBefore.addAll(workingRanges);
			workingRangesBefore.add(psddBefore);

			Rode psddAfter = createGroupToIndex(distrubutionMapKeys, keyIndex, numberOfPeopleInGroup);
			List<Rode> workingRangesAfter = new ArrayList<>();
			workingRangesAfter.addAll(workingRanges);
			workingRangesAfter.add(psddAfter);

			if (!rangeHasSameStartAndEnd(psddBefore)) {
				splitSet(distrubutionMapKeys.subList(keyIndex, distrubutionMapKeys.size()), numberOfGroups - 1, workingRangesBefore, allRanges);
			}

			splitSet(distrubutionMapKeys.subList(keyIndex + 1, distrubutionMapKeys.size()), numberOfGroups - 1, workingRangesAfter, allRanges);
		} else {
			Rode psddLast = createGroupToIndex(distrubutionMapKeys, distrubutionMapKeys.size() - 1, countPeopleInSet(distrubutionMapKeys));
			List<Rode> workingRangesLast = new ArrayList<>();
			workingRangesLast.addAll(workingRanges);
			workingRangesLast.add(psddLast);
			allRanges.add(workingRangesLast);
		}
	}

	/*
	 * Create a psdd from the beginning to the index
	 */
	private Rode createGroupToIndex(final List<String> distrubutionMapKeys, final int index, final int numberOfPeopleInGroup) {
		return new Rode(distrubutionMapKeys.get(0), distrubutionMapKeys.get(index), numberOfPeopleInGroup);
	}

	/*
	 * If a range starts and ends at the same letter it is invalid
	 */
	private boolean rangeHasSameStartAndEnd(final Rode psdd) {
		return psdd.getFra().equals(psdd.getTil());
	}

	/*
	 * Calculates if more people can be added to the group
	 */
	private boolean addMorePeople(final int numberOfPeopleInGroup, final int numberOfLettersLeft, final int numberOfGroups, final int numberOfPeoplePerGroup) {
		return numberOfPeopleInGroup <= numberOfPeoplePerGroup && numberOfLettersLeft / 2 > numberOfGroups - 1;
	}

	/*
	 * If there is fewer than two remaining the range will be like Å-Å and be illegal
	 */
	private boolean rangeWillEndInDuplicate(final List<String> distrubutionMapKeys) {
		return distrubutionMapKeys.size() < 2;
	}

	// Get the sum of people beginning with the letters in set paramater
	private int countPeopleInSet(final List<String> set) {
		int count = 0;
		for (String key : set) {
			count += manntallDistribusjonAvForbokstavIEtternavn.get(key);
		}
		return count;
	}

	/**
	 * Beregner antallet velgere per rode.
	 * Forutsetter at rodene har blitt validert av PollingStationsDivisionValidator
	 */
	public void tellAntallVelgerePerRode(final List<Rode> roder) {
		sorterRoder(roder);

		for (Rode rode : roder) {
			rode.setAntallVelgere(0);
		}

		for (Voter velger : manntall) {
			for (int i = 0; i < roder.size(); i++) {
				if (erVelgerIRode(roder, i, velger)) {
					roder.set(i, roder.get(i).leggTilVelgere(1));
					break;
				}
			}
		}
	}

	// men pga. bruk av forskjellig rode-klasse, kan den ikke gjenbrukes uten ytterligere endringer
	private boolean erVelgerIRode(List<Rode> roder, int rodeIndex, Voter velger) {
		String fra = rodeIndex == 0 ? null : roder.get(rodeIndex).getFra();
		String til = rodeIndex == roder.size() - 1 ? null : roder.get(rodeIndex + 1).getFra();
		return erEtternavnIIntervall(velger.getLastName(), fra, til);
	}
	
	private double findRangeVariance(final List<Rode> ranges) {
		Variance variance = new Variance();
		double[] valueArray = new double[ranges.size()];
		for (int i = 0; i < ranges.size(); i++) {
			valueArray[i] = ranges.get(i).getAntallVelgere();
		}

		return variance.evaluate(valueArray);
	}

	public List<Pair<Voter, PollingStation>> distribuerVelgereTil(final List<PollingStation> pollingStations) {
		normalizePollingStationList(pollingStations);
		List<Pair<Voter, PollingStation>> assignmentList;
		assignmentList = new ArrayList<>();

		for (PollingStation pollingstation : pollingStations) {
			for (Voter v : manntall) {
				if (erVelgerIRode(pollingStations, pollingstation, v)) {
					assignmentList.add(Pair.of(v, pollingstation));
				}
			}
		}
		return assignmentList;

	}

	private boolean erVelgerIRode(List<PollingStation> roder, PollingStation rode, Voter velger) {
		int rodeIndex = roder.indexOf(rode);
		String fra = rodeIndex == 0 ? null : rode.getFirst();
		String til = rodeIndex == roder.size() - 1 ? null : roder.get(rodeIndex + 1).getFirst();
		return erEtternavnIIntervall(velger.getLastName(), fra, til);

	}
}
