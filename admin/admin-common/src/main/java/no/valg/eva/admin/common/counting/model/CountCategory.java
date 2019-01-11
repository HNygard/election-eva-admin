package no.valg.eva.admin.common.counting.model;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import java.util.HashSet;
import java.util.Set;

import static no.valg.eva.admin.common.AreaPath.MUNICIPALITY_POLLING_DISTRICT_ID;

/**
 * Kategorier for telling.
 */
public enum CountCategory {
	/** Forhåndsstemmer, ordinære */
	FO(true),
	/** Sent innkomne/lagt til side */
	FS(true),
	/** Valgtingsstemmer, ordinære */
	VO(false),
	/** Fremmedstemmer */
	VF(false),
	/** Stemmer i beredskapskonvolutt */
	VB(false),
	/** Stemmer i særskilt omslag */
	VS(false),
    /**
     * Valg til bydelsutvalg, stemmer mottatt i andre bydeler)
     */
	BF(false);

	private final boolean earlyVoting;

	CountCategory(boolean earlyVoting) {
		this.earlyVoting = earlyVoting;
	}

	/**
     * Hjelpemetode som bruker standard valueOf metode.
     *
     * @return CountCategory som matcher den gitte id'en
	 */
	public static CountCategory fromId(String id) {
		return CountCategory.valueOf(id);
	}

	/*
	 * Lister opptellingskategorier for kombinasjonen av foreløpig/endelig + forhånd/valgting for en gitt stemmekrets (PollingDistrict)
	 *
	 *                    0000 i samlet komm.        | 0000 i komm med VO-kretsfordelt  |  !0000 i komm med VO-kretsfordelt
	 *                  | Foreløpig   Endelig        | Foreløpig   Endelig              |  Foreløpig   Endelig
	 *                  |----------------------------|----------------------------------|-----------------------------------
	 * Forhånd          | FO          FO, FS         | FO          FO, FS               |
	 * Valgting XiM     | VO          VO, VS, VB     |             VS, VB               |  VO          VO
	 * Valgting papir   | VO, VF      VO, VS, VF*    |             VS, VF*              |  VO, VF*     VO, VF*
	 *
	 * Unntak1: For tekniske kretser gjelder at man kun får FO, og da får 0000-kretsen (i samme kommune) ikke FO
	 * Unntak2: VF kan være kretsfordelt. Da kommer den bare i !0000-kretser, ellers bare i 0000-kretsen
	 * Unntak3: Valgkretsene for sametinget har ikke valgtingsstemmer. 
	 *
	 * Brukstilfeller:
	 * 1) Avgjøre hvilke foreløpige tellinger som maksimalt KAN rapporteres
	 * 2) Avgjøre hvilke endelige tellinger som MÅ være med før man kan rapportere (man kan altså rapportere flere kategorier av endelige om det finnes!)
	 *
	 * Dette er i samsvar med https://confluence.valg.no/display/EA/Modul+for+rapportering+til+EVA+Resultat
	 *
	 * NB! Jeg har ikke håndtert BF-kategorien spesifikt her. Først og fremst fordi denne logikken kun benyttes til å avgjøre hvilke
     * tellinger som skal sendes til EVA Resultat/Valgnatt, og BF (stemmer fra en bydel til en annen) gjelder bare i bydelsvalg som
	 * ikke håndteres i EVA Resultat uansett. Likevel så blir BF til slutt sendt med i de endelige tellingene (se logikken i
	 * ContestReport.foreløpigeEllerEndelige()) dersom en slik telling finnes.
	 */
	public static Set<CountCategory> finnOpptellingskategorier(PollingDistrict pollingDistrict, boolean forhaand, boolean forelopige,
			CountingMode voCountingMode, CountingMode vfCountingMode) {
		boolean erKommunekretsen = MUNICIPALITY_POLLING_DISTRICT_ID.equals(pollingDistrict.getId());
		boolean erTekniskKrets = pollingDistrict.isTechnicalPollingDistrict();
		boolean harKommunenTekniskeKretser = pollingDistrict.getBorough().getPollingDistricts().stream()
				.filter(PollingDistrict::isTechnicalPollingDistrict)
				.count() > 1;
		Boolean voSentraltSamlet = voCountingMode == null ? null : voCountingMode == CountingMode.CENTRAL;
		Boolean vfSentraltSamlet = vfCountingMode == null ? null : vfCountingMode == CountingMode.CENTRAL;
		boolean erXim = pollingDistrict.getBorough().getMunicipality().isElectronicMarkoffs();
		return finnOpptellingskategorier(erKommunekretsen, voSentraltSamlet, vfSentraltSamlet, forelopige, forhaand, erXim, erTekniskKrets,
				harKommunenTekniskeKretser);
	}

	protected static Set<CountCategory> finnOpptellingskategorier(boolean erKommunekretsen, Boolean voSentraltSamlet, Boolean vfSentraltSamlet,
			boolean forelopige, boolean forhaand, boolean erXim, boolean erTekniskKrets, boolean harKommunenTekniskeKretser) {
		sjekkGyldigKonfigurasjonskombinasjon(erKommunekretsen, erTekniskKrets, harKommunenTekniskeKretser);
		Set<CountCategory> opptellingskategorier = new HashSet<>();
		if (forhaand) {
			opptellingskategorier = opptellingskategorierForForhaand(forelopige, erKommunekretsen, harKommunenTekniskeKretser, erTekniskKrets);
		} else if (!erTekniskKrets && (voSentraltSamlet != null || vfSentraltSamlet != null)) { // valgting, men aldri tekniske kretser - de får ikke valgtingopptellinger
			opptellingskategorier = opptellingskategorierForValgting(forelopige, erKommunekretsen, voSentraltSamlet, vfSentraltSamlet, erXim);
		}
		return opptellingskategorier;
	}

	private static void sjekkGyldigKonfigurasjonskombinasjon(boolean erKommunekretsen, boolean erTekniskKrets, boolean harKommunenTekniskeKretser) {
		if (erTekniskKrets && !harKommunenTekniskeKretser) {
			throw new IllegalArgumentException("Det kan ikke være en teknisk krets i en kommune som ikke har tekniske kretser.");
		}
		if (erKommunekretsen && erTekniskKrets) {
			throw new IllegalArgumentException("Kommunekretsen " + MUNICIPALITY_POLLING_DISTRICT_ID + " kan ikke være en teknisk krets.");
		}
	}

	private static Set<CountCategory> opptellingskategorierForForhaand(boolean forelopige, boolean erKommunekretsen, boolean harKommunenTekniskeKretser,
			boolean erTekniskKrets) {
		Set<CountCategory> opptellingskategorier = new HashSet<>();
		if (harKommunenTekniskeKretser) {
			if (erTekniskKrets && !erKommunekretsen) {
				opptellingskategorier.add(FO);
			} else if (!erTekniskKrets && erKommunekretsen && !forelopige) {
				opptellingskategorier.add(FS);
			}
		} else {
			if (erKommunekretsen && forelopige) {
				opptellingskategorier.add(FO);
			} else if (erKommunekretsen) {
				opptellingskategorier.add(FO);
				opptellingskategorier.add(FS);
			}
		}
		return opptellingskategorier;
	}

	private static Set<CountCategory> opptellingskategorierForValgting(boolean forelopige, boolean erKommunekretsen, Boolean voSentraltSamlet,
			Boolean vfSentraltSamlet, boolean erXim) {
		Set<CountCategory> opptellingskategorier = new HashSet<>();
		if (forelopige) {
			if (!erKommunekretsen || voSentraltSamlet) { // treffer alle unntatt 0000-krets i kretsfordelt kommune
				opptellingskategorier.add(VO);
			}
		} else { // endelige
			if (tilfredstillerKravTilVOellerVFRapportering(erKommunekretsen, voSentraltSamlet)) {
				opptellingskategorier.add(VO);
			}
			if (erKommunekretsen) {
				opptellingskategorier.add(VS);
				if (erXim) {
					opptellingskategorier.add(VB);
				}
			}
		}
		if (!erXim && tilfredstillerKravTilVOellerVFRapportering(erKommunekretsen, vfSentraltSamlet) && !opptellingskategorier.isEmpty()) {
			opptellingskategorier.add(VF);
		}
		return opptellingskategorier;
	}

	private static boolean tilfredstillerKravTilVOellerVFRapportering(boolean erKommunekretsen, boolean sentraltSamlet) {
		return (erKommunekretsen && sentraltSamlet) || (!erKommunekretsen && !sentraltSamlet);
	}

	public boolean isEarlyVoting() {
		return earlyVoting;
	}

    /**
     * @return message text id for denne kategorien til bruk i GUI
     */
    public String messageProperty() {
        return "@vote_count_category[" + name() + "].name";
    }

	/**
     * Hjelpemetode for å hente kategoriens id ved hjelp av standard name() metode.
     *
     * @return kategoriens id som er mappet i databasen
	 */
	public String getId() {
		return name();
	}

}
