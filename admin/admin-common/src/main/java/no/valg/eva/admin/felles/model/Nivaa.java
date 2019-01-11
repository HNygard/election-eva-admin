package no.valg.eva.admin.felles.model;

import static java.util.Arrays.stream;

public interface Nivaa {
	static String toString(Nivaa... nivaaer) {
		return stream(nivaaer).map(Nivaa::nivaa).reduce("", Nivaa::leggNivaaTilResultatTilNaa, Nivaa::kombinerResultater);
	}

	static String leggNivaaTilResultatTilNaa(String resultat, int nivaa) {
		if (resultat.length() == 0) {
			return String.valueOf(nivaa);
		}
		return resultat + "," + nivaa;
	}

	static String kombinerResultater(String r1, String r2) {
		return r1 + "," + r2;
	}

	int nivaa();
}
