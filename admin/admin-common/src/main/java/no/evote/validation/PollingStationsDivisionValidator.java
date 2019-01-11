package no.evote.validation;

import java.util.Comparator;
import java.util.List;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.configuration.model.local.Rode;

public class PollingStationsDivisionValidator {

	static final String VALIDATION_FEEDBACK_NOT_WHOLE_ALPHABET = "@config.polling_stations.divisionList.notWholeAlphabet";
	static final String VALIDATION_FEEDBACK_DOENST_START_WITH_A = "@config.polling_stations.divisionList.doesntStartWithA";
	static final String VALIDATION_FEEDBACK_RANGES_NOT_FOLLOWING = "@config.polling_stations.divisionList.rangesNotFollowing";
	static final String VALIDATION_FEEDBACK_ILLEGAL_SIGNS = "@config.polling_stations.divisionList.illegalSigns";
	static final String VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS = "@config.polling_stations.divisionList.oneOrTwoLetters";
	static final String VALIDATION_FEEDBACK_DIVISION_LIST_EMPTY = "@config.polling_stations.divisionList.empty";

	private String validationFeedback;

	public boolean isValid(final List<Rode> divisionList) {
		validationFeedback = null;
		if (divisionList == null) {
			validationFeedback = VALIDATION_FEEDBACK_DIVISION_LIST_EMPTY;
			return false;
		} else if (divisionList.isEmpty()) {
			validationFeedback = VALIDATION_FEEDBACK_DIVISION_LIST_EMPTY;
			return false;
		}

		sorterRoder(divisionList);
		if (!checkListDontContainsAnythingButTheAlphabet(divisionList)) {
			validationFeedback = VALIDATION_FEEDBACK_ILLEGAL_SIGNS;
			return false;
		} else if (!"A".equals(divisionList.get(0).getFra())) {
			validationFeedback = VALIDATION_FEEDBACK_DOENST_START_WITH_A;
			return false;
		} else if (!checkDividorLength(divisionList)) {
			validationFeedback = VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS;
			return false;
		} else if (!checkNoMixtureOfDividorLength(divisionList)) {
			validationFeedback = VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS;
			return false;
		} else if (!doesRangesFollowEachother((divisionList))) {
			validationFeedback = VALIDATION_FEEDBACK_RANGES_NOT_FOLLOWING;
			return false;
		} else if (!divisionCoversTheWholeAlphabet(divisionList)) {
			validationFeedback = VALIDATION_FEEDBACK_NOT_WHOLE_ALPHABET;
			return false;
		}

		return true;
	}

	private boolean checkDividorLength(final List<Rode> divisionList) {
		for (Rode psdd : divisionList) {
			if (psdd.getFra().length() < 1 || psdd.getFra().length() > 2 || psdd.getTil().length() < 1 || psdd.getTil().length() > 2) {
				return false;
			}
		}

		return true;
	}

	private boolean checkNoMixtureOfDividorLength(final List<Rode> divisionList) {
		int lengthOnFirst = divisionList.get(0).getTil().length();
		for (Rode psdd : divisionList) {

			if (!psdd.getFra().equals("A") && psdd.getFra().length() != lengthOnFirst) {
				return false;
			}
			if (psdd.getTil().length() != lengthOnFirst) {
				return false;
			}

		}

		return true;
	}

	private void sorterRoder(final List<Rode> roder) {
		roder.sort(Comparator.comparing(Rode::getFra));
	}

	private boolean checkListDontContainsAnythingButTheAlphabet(final List<Rode> divisionList) {
		for (Rode psdd : divisionList) {
			for (char c : psdd.getFra().toCharArray()) {
				if (!EvoteConstants.ALPHABET.contains(Character.toString(c))) {
					return false;
				}

			}
			for (char c : psdd.getTil().toCharArray()) {
				if (!EvoteConstants.ALPHABET.contains(Character.toString(c))) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean divisionCoversTheWholeAlphabet(final List<Rode> divisionList) {
		if (divisionList.get(0).getTil().length() == 2) {
			return divisionCoversTheWholeAlphabetTwoLetter(divisionList);
		} else if (divisionList.get(0).getTil().length() == 1) {
			return divisionCoversTheWholeAlphabetOneLetter(divisionList);
		} else {
			return false;
		}

	}

	private boolean divisionCoversTheWholeAlphabetOneLetter(final List<Rode> divisionList) {
		// Does the last range end with the last letter of the alphabet?
		return Character.toString(EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.length() - 1)).equals(
				divisionList.get(divisionList.size() - 1).getTil());

	}

	// Test that the ranges are in right order ant the and the every letter is covered
	private boolean doesRangesFollowEachother(final List<Rode> divisionList) {
		if (divisionList.get(0).getTil().length() == 2) {
			return doesRangesFollowEachotherTwoLetter(divisionList);
		} else if (divisionList.get(0).getTil().length() == 1) {
			return doesRangesFollowEachotherOneLetter(divisionList);
		} else {
			return false;
		}
	}

	private boolean doesRangesFollowEachotherOneLetter(final List<Rode> divisionList) {
		for (int i = 0; i < divisionList.size(); i++) {
			// Check that the next range starts with the letter after with which this one ends
			if ((i < divisionList.size() - 1) && (divisionList.get(i + 1).getFra().charAt(0) != getNextInAlphabet(divisionList.get(i).getTil().charAt(0)))) {
				return false;
			}
		}
		return true;
	}

	private boolean doesRangesFollowEachotherTwoLetter(final List<Rode> divisionList) {
		for (int i = 0; i < divisionList.size(); i++) {
			// Check that the next range starts with the letter after with which this one ends
			if ((i < divisionList.size() - 1) && (!divisionList.get(i + 1).getFra().equals(getNextAlphabeticCombination(divisionList.get(i).getTil())))) {
				return false;
			}
		}
		return true;
	}

	private boolean divisionCoversTheWholeAlphabetTwoLetter(final List<Rode> divisionList) {
		// Does the last range end with the last letter of the alphabet?
		return (Character.toString(EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.length() - 1)) + Character
				.toString(EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.length() - 1))).equals(divisionList.get(divisionList.size() - 1).getTil());

	}

	private String getNextAlphabeticCombination(final String s) {
		if (s.charAt(1) != EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.length() - 1)) {
			return Character.toString(s.charAt(0)) + getNextInAlphabet(s.charAt(1));
		} else {
			return Character.toString(getNextInAlphabet(s.charAt(0))) + Character.toString(EvoteConstants.ALPHABET.charAt(0));
		}

	}

	private Character getNextInAlphabet(final Character c) {
		return EvoteConstants.ALPHABET.charAt(EvoteConstants.ALPHABET.indexOf(c) + 1);
	}

	public String getValidationFeedback() {
		return validationFeedback;
	}
}
