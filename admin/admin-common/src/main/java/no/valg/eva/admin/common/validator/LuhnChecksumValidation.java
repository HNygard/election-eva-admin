package no.valg.eva.admin.common.validator;

public final class LuhnChecksumValidation {

	private static final int MULTIPLIKATOR_SJEKKSUM = 9;
	private static final int MODULOVERDI = 10;

	private LuhnChecksumValidation() {
		// For å unngå instansiering
	}

	public static boolean isGyldigKontrollsiffer(String tallsekvens) {
		int sum = beregnLuhnSum(tallsekvens);
		return sum % MODULOVERDI == 0;
	}

	private static int beregnLuhnSum(String tallsekvens) {
		int sum = 0;
		boolean annenHverGang = false;
		for (int i = tallsekvens.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(tallsekvens.substring(i, i + 1));
			if (annenHverGang) {
				n = tverrsum(n * 2);
			}
			sum += n;
			annenHverGang = !annenHverGang;
		}
		return sum;
	}

	private static int tverrsum(int n) {
		if (n > (MODULOVERDI - 1)) {
			return (n % MODULOVERDI) + 1;
		} else {
			return n;
		}
	}

	public static int beregnKontrollsiffer(String strengMedSifre) {
		int luhnSum = beregnLuhnSum(strengMedSifre + "0");
		return (luhnSum * MULTIPLIKATOR_SJEKKSUM) % MODULOVERDI;
	}
}
