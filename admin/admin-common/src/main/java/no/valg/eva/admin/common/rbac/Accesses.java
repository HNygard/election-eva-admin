package no.valg.eva.admin.common.rbac;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public enum Accesses {

	// @formatter:off

	// Admin
	Admin(new Access("admin")),
	
	// Konfigurasjon
	Konfigurasjon(new Access("konfig")),
	Konfigurasjon_Overstyre(Konfigurasjon.child("overstyre")),
	Konfigurasjon_EML(Konfigurasjon.child("eml")),
	Konfigurasjon_EML_Last_Ned(Konfigurasjon_EML.child("lastned")),
	Konfigurasjon_EML_Last_Ned_Siste(Konfigurasjon_EML.child("lastned_siste")),
	Konfigurasjon_EML_Behandle(Konfigurasjon_EML.child("behandle")),
	Konfigurasjon_Valghendelse(Konfigurasjon.child("valghendelse")),
	Konfigurasjon_Valghendelse_Liste(Konfigurasjon_Valghendelse.child("liste")),
	Konfigurasjon_Valghendelse_Opprett(Konfigurasjon_Valghendelse.child("opprett")),
	Konfigurasjon_Valghendelse_Rediger(Konfigurasjon_Valghendelse.child("rediger")),
	Konfigurasjon_Valghendelse_Slett(Konfigurasjon_Valghendelse.child("slett")),
	Konfigurasjon_Valghendelse_Sertifikater(Konfigurasjon_Valghendelse.child("sertifikater")),
	Konfigurasjon_Valg(Konfigurasjon.child("valg")),
	Konfigurasjon_Valg_Valggruppe(Konfigurasjon_Valg.child("valggruppe")),
	Konfigurasjon_Valg_Valg(Konfigurasjon_Valg.child("valg")),
	Konfigurasjon_Valg_Valgdistrikt(Konfigurasjon_Valg.child("valgdistrikt")),
	Konfigurasjon_Geografi(Konfigurasjon.child("geografi")),
	Konfigurasjon_Grunnlagsdata(Konfigurasjon.child("grunnlagsdata")),
	Konfigurasjon_Grunnlagsdata_Redigere(Konfigurasjon_Grunnlagsdata.child("redigere")),
	Konfigurasjon_Grunnlagsdata_Godkjenne(Konfigurasjon_Grunnlagsdata.child("godkjenne")),
	Konfigurasjon_Grunnlagsdata_Oppheve(Konfigurasjon_Grunnlagsdata.child("oppheve")),
	Konfigurasjon_Grunnlagsdata_Oversikt_Manntallsavvik(Konfigurasjon_Grunnlagsdata.child("oversikt_manntallsavvik")),
	Konfigurasjon_Opptellingsmåter(Konfigurasjon.child("opptellingsmåter")),
	Konfigurasjon_Oversikt(Konfigurasjon.child("oversikt")),
	Konfigurasjon_Styrer(Konfigurasjon.child("styrer")),
	Konfigurasjon_Opptellingsvalgstyrer(Konfigurasjon.child("opptellingsvalgstyrer")),
	Konfigurasjon_Oversettelser(Konfigurasjon.child("oversettelser")),
	Konfigurasjon_Adresseliste_Valgstyrer(Konfigurasjon.child("adresseliste_valgstyrer")),
	Konfigurasjon_Adresseliste_Samevalgstyrer(Konfigurasjon.child("adresseliste_samevalgstyrer")),
	Konfigurasjon_Eksporter_ScanningConfig(Konfigurasjon.child("scanningconfig")),

	// Opptelling
	Opptelling(new Access("opptelling")),
	Opptelling_Behandle(Opptelling.child("behandle")),
	Opptelling_Behandle_Valgstyrets_Møtebok(Opptelling_Behandle.child("valgstyrets_møtebok")),
	Opptelling_Lagt_Til_Side(Opptelling.child("lagt_til_side")),
	Opptelling_Lagt_Til_Side_Se(Opptelling_Lagt_Til_Side.child("se")),
	Opptelling_Lagt_Til_Side_Rediger(Opptelling_Lagt_Til_Side.child("rediger")),
	Opptelling_Forhånd(Opptelling.child("forhånd")),
	Opptelling_Forhånd_Se(Opptelling_Forhånd.child("se")),
	Opptelling_Forhånd_Rediger(Opptelling_Forhånd.child("rediger")),
	Opptelling_Valgting(Opptelling.child("valgting")),
	Opptelling_Valgting_Se(Opptelling_Valgting.child("se")),
	Opptelling_Valgting_Rediger(Opptelling_Valgting.child("rediger")),
	Opptelling_Rettelser(Opptelling.child("rettelser")),
	Opptelling_Rettelser_Se(Opptelling_Rettelser.child("se")),
	Opptelling_Rettelser_Rediger(Opptelling_Rettelser.child("rediger")),
	Opptelling_Opphev(Opptelling.child("opphev")),
	Opptelling_Opphev_Foreløpig_Telling(Opptelling_Opphev.child("foreløpig_telling")),
	Opptelling_Opphev_Endelig_Telling(Opptelling_Opphev.child("endelig_telling")),
	Opptelling_Utjevningsmandater(Opptelling.child("utjevningsmandater")),
	Opptelling_Utjevningsmandater_Se(Opptelling_Utjevningsmandater.child("se")),
	Opptelling_Utjevningsmandater_Gjennomføre(Opptelling_Utjevningsmandater.child("gjennomføre")),
	Opptelling_Valgoppgjør(Opptelling.child("valgoppgjør")),
	Opptelling_Valgoppgjør_Se(Opptelling_Valgoppgjør.child("se")),
	Opptelling_Valgoppgjør_Gjennomføre(Opptelling_Valgoppgjør.child("gjennomføre")),
	Opptelling_Importer(Opptelling.child("importer")),
	Opptelling_Strekkodelapper(Opptelling.child("strekkodelapper")),
	Opptelling_Kommunens_Tall(Opptelling.child("kommunens_tall")),
	Opptelling_Oversikt(Opptelling.child("oversikt")),
	Opptelling_Forkastelser(Opptelling.child("forkastelser")),
	Opptelling_Forkastelser_Manuelt(Opptelling_Forkastelser.child("manuelt")),
	Opptelling_Forkastelser_Skannet(Opptelling_Forkastelser.child("skannet")),
	Opptelling_Skanning(Opptelling.child("skanning")),
	Opptelling_Skanning_Importer_EML(Opptelling_Skanning.child("importer_EML")),
	Opptelling_Skanning_Skanne(Opptelling_Skanning.child("skanne")),
	Opptelling_Skanning_Verifisere(Opptelling_Skanning.child("verifisere")),
	Opptelling_Skanning_Administrere_Jobbstyring(Opptelling_Skanning.child("administrere_jobbstyring")),
	Opptelling_Skanning_Overføre_Admin(Opptelling_Skanning.child("overføre_admin")),

	// Parti
	Parti(new Access("parti")),
	Parti_Opprett(Parti.child("opprett")),
	Parti_Rediger(Parti.child("rediger")),
	Parti_Slett(Parti.child("slett")),

	// Manntall
	Manntall(new Access("manntall")),
	Manntall_Import(Manntall.child("import")),
	Manntall_Generer_Mannntallsnummer(Manntall.child("generer_mannntallsnummer")),
	Manntall_Generer_Valgkortgrunnlag(Manntall.child("generer_valgkortgrunnlag")),
	Manntall_Historikk(Manntall.child("historikk")),
	Manntall_Valgkort(Manntall.child("valgkort")),
	Manntall_Valgkort_Tomt(Manntall_Valgkort.child("tomt")),
	Manntall_Søk(Manntall.child("søk")),
	Manntall_Søk_Kommune(Manntall.child("søk_kommune")),
	Manntall_Rediger_Person(Manntall.child("rediger_person")),
	

	// Rapport
	Rapport(new Access("rapport")),
	Rapport_Grunnlagsdata(Rapport.child("grunnlagsdata")),
	Rapport_Grunnlagsdata_Stemmeberettigede_Pr_Krets(Rapport_Grunnlagsdata.child("stemmeberettigede_pr_krets")),
	Rapport_Grunnlagsdata_Generell_Kretsinformasjon(Rapport_Grunnlagsdata.child("generell_kretsinformasjon")),
	Rapport_Grunnlagsdata_Brukere_Og_Roller_Fylke(Rapport_Grunnlagsdata.child("brukere_og_roller_fylke")),
	Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune(Rapport_Grunnlagsdata.child("brukere_og_roller_kommune")),
	Rapport_Grunnlagsdata_Opptellingsmåter(Rapport_Grunnlagsdata.child("opptellingsmåter")),
	Rapport_Listeforslag(Rapport.child("listeforslag")),
	Rapport_Listeforslag_Lister_Og_Kandidater(Rapport_Listeforslag.child("lister_og_kandidater")),
	Rapport_Manntall(Rapport.child("manntall")),
	Rapport_Manntall_Avkrysningsmanntall(Rapport_Manntall.child("avkrysningsmanntall")),
	Rapport_Manntall_Avkrysningsmanntall_Minus30(Rapport_Manntall.child("avkrysningsmanntallminus30")),
	Rapport_Manntall_Utleggingsmanntall_Kretsvis(Rapport_Manntall.child("utleggingsmanntall_kretsvis")),
	Rapport_Manntall_Utleggingsmanntall_Alfabetisk(Rapport_Manntall.child("utleggingsmanntall_alfabetisk")),
	Rapport_Manntall_Partiservice_Fylke(Rapport_Manntall.child("partiservice_fylke")),
	Rapport_Manntall_Partiservice_Kommune(Rapport_Manntall.child("partiservice_kommune")),
	Rapport_Manntall_Partiservice_Valgkrets(Rapport_Manntall.child("partiservice_valgkrets")),
	Rapport_Forhåndsstemmeperiode(Rapport.child("forhåndsstemmeperiode")),
	Rapport_Forhåndsstemmeperiode_Til_Prøving(Rapport_Forhåndsstemmeperiode.child("til_prøving")),
	Rapport_Forhåndsstemmeperiode_Antall_Mottatte(Rapport_Forhåndsstemmeperiode.child("antall_mottatte")),
	Rapport_Forhåndsstemmeperiode_Andre_Kommuner(Rapport_Forhåndsstemmeperiode.child("andre_kommuner")),
	Rapport_Valgting(Rapport.child("valgting")),
	Rapport_Valgting_Til_Prøving(Rapport_Valgting.child("til_prøving")),
	Rapport_Opptelling_Admin(Rapport.child("opptelling_admin")),
	Rapport_Opptelling_Admin_Partifordeling_Pr_Krets(Rapport_Opptelling_Admin.child("partifordeling_pr_krets")),
	Rapport_Opptelling_Admin_Valgdeltakelse(Rapport_Opptelling_Admin.child("valgdeltakelse")),
	Rapport_Møtebøker(Rapport.child("møtebøker")),
	Rapport_Møtebøker_Stemmestyre(Rapport_Møtebøker.child("stemmestyre")),
	Rapport_Møtebøker_Valgstyre(Rapport_Møtebøker.child("valgstyre")),
	Rapport_Møtebøker_Fylkesvalgstyre_Fylke(Rapport_Møtebøker.child("fylkesvalgstyre_fylke")),
	Rapport_Møtebøker_Fylkesvalgstyre_Storting(Rapport_Møtebøker.child("fylkesvalgstyre_storting")),
	Rapport_Møtebøker_Bydelsutvalg(Rapport_Møtebøker.child("bydelsutvalg")),
	Rapport_Møtebøker_Samevalgstyre(Rapport_Møtebøker.child("samevalgstyre")),
	Rapport_Møtebøker_Valgstyre_Longyearbyen(Rapport_Møtebøker.child("valgstyre_longyearbyen")),
	Rapport_Møtebøker_Opptellingsvalgstyre(Rapport_Møtebøker.child("opptellingsvalgstyre")),
	Rapport_Resultat(Rapport.child("resultat")),
	Rapport_Resultat_Valgte_Representanter(Rapport_Resultat.child("valgte_representanter")),
	Rapport_Resultat_Mandatfordeling(Rapport_Resultat.child("mandatfordeling")),
	Rapport_Resultat_Kandidatrangering_Pr_Parti(Rapport_Resultat.child("kandidatrangering_pr_parti")),
	Rapport_Resultat_Utjevningsmandater(Rapport_Resultat.child("utjevningsmandater")),
	Rapport_Beregninger(Rapport.child("beregninger")),
	Rapport_Beregninger_Kandidatstemmetall_Kommune(Rapport_Beregninger.child("kandidatstemmetall_kommune")),
	Rapport_Beregninger_Kandidatstemmetall_Storting(Rapport_Beregninger.child("kandidatstemmetall_storting")),
	Rapport_Beregninger_Mandatberegning_Pr_Parti(Rapport_Beregninger.child("mandatberegning_pr_parti")),
	Rapport_Beregninger_Partisammendrag_Slengere(Rapport_Beregninger.child("partisammendrag_slengere")),
	Rapport_Valgoppgjør_Admin(Rapport.child("valgoppgjør_admin")),
	Rapport_Valgoppgjør_Admin_Flettefil_Valgte_Kandidater(Rapport_Valgoppgjør_Admin.child("flettefil_valgte_kandidater")),

	// Liste
	Listeforslag(new Access("liste")),
	Listeforslag_Opprett(Listeforslag.child("opprett")),
	Listeforslag_Opprett_Nytt_Parti(Listeforslag_Opprett.child("nytt_parti")),
	Listeforslag_Opprett_Eksisterende_Parti(Listeforslag_Opprett.child("eksisterende_parti")),
	Listeforslag_Rediger(Listeforslag.child("rediger")),
	Listeforslag_Last_Ned_Flettefil(Listeforslag.child("lastned_flettefil")),
	Listeforslag_Last_Ned_Underlag(Listeforslag.child("lastned_underlag")),
	Listeforslag_Importere_Kandidater(Listeforslag.child("importere_kandidater")),

	// Import
	Import(new Access("import")),
	Import_Områder(Import.child("områder")),
	Import_Områder_Hierarki(Import_Områder.child("hierarki")),
	Import_Områder_Endringer(Import_Områder.child("endringer")),
	Import_Buypass(Import.child("buypass")),

		// Resultat
	Resultat(new Access("resultat")),
	Resultat_Rapporter(Resultat.child("rapporter")),
	Resultat_Konfigurasjon(Resultat.child("konfigurasjon")),

	// Tilgang
	Tilgang(new Access("tilgang")),
	Tilgang_Brukere(Tilgang.child("brukere")),
	Tilgang_Brukere_Administrere(Tilgang_Brukere.child("administrere")),
	Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå(Tilgang_Brukere.child("administrere_brukere_underliggende_nivå")),
	Tilgang_Brukere_Importer(Tilgang_Brukere.child("importer")),
	Tilgang_Brukere_Importer_Sentralt(Tilgang_Brukere_Importer.child("sentralt")),
	Tilgang_Brukere_Importer_Forhånd(Tilgang_Brukere_Importer.child("forhånd")),
	Tilgang_Brukere_Importer_Valgting(Tilgang_Brukere_Importer.child("valgting")),
	Tilgang_Roller(Tilgang.child("roller")),
	Tilgang_Roller_Administrere(Tilgang_Roller.child("administrere")),
	Tilgang_Roller_Tilganger(Tilgang_Roller.child("tilganger")),
	Tilgang_Roller_Import(Tilgang_Roller.child("import")),
	Tilgang_Roller_Kopier(Tilgang_Roller.child("kopier")),

	// Stemmegiving
	Stemmegiving(new Access("stemmegiving")),
	Stemmegiving_Forhånd(Stemmegiving.child("forhånd")),
    Stemmegiving_Forhånd_Registrer_I_Konvolutt(Stemmegiving_Forhånd.child("registrer_i_konvolutt")),
	Stemmegiving_Forhånd_Registrer(Stemmegiving_Forhånd.child("registrer")),
	Stemmegiving_Forhånd_Registrer_Sent_Innkommet(Stemmegiving_Forhånd.child("registrer_sent_innkommet")),
	Stemmegiving_Forhånd_Registrer_Sentralt(Stemmegiving_Forhånd.child("registrer_sentralt")),
	Stemmegiving_Valgting(Stemmegiving.child("valgting")),
	Stemmegiving_Valgting_Registrer(Stemmegiving_Valgting.child("registrer")),
	Stemmegiving_Valgting_Registrer_Sentralt(Stemmegiving_Valgting.child("registrer_sentralt")),
	Stemmegiving_Prøving(Stemmegiving.child("prøving")),
	Stemmegiving_Prøving_Forhånd(Stemmegiving_Prøving.child("forhånd")),
	Stemmegiving_Prøving_Forhånd_Enkelt(Stemmegiving_Prøving_Forhånd.child("enkelt")),
	Stemmegiving_Prøving_Forhånd_Samlet(Stemmegiving_Prøving_Forhånd.child("samlet")),
	Stemmegiving_Prøving_Konvolutt(Stemmegiving_Prøving.child("konvolutt")),
	Stemmegiving_Prøving_Valgting(Stemmegiving_Prøving.child("valgting")),
	Stemmegiving_Prøving_Valgting_Enkelt(Stemmegiving_Prøving_Valgting.child("enkelt")),
	Stemmegiving_Prøving_Valgting_Samlet(Stemmegiving_Prøving_Valgting.child("samlet")),
	Stemmegiving_Oversikt(Stemmegiving.child("oversikt")),
	Stemmegiving_Oversikt_Til_Andre_Kommuner(Stemmegiving_Oversikt.child("til_andre_kommuner")),
	Stemmegiving_Oversikt_Forkastede(Stemmegiving_Oversikt.child("forkastede")),
	Stemmegiving_Oversikt_Status(Stemmegiving_Oversikt.child("status")),
	Stemmegiving_Oversikt_Konvolutt(Stemmegiving_Oversikt.child("konvolutt")),

	// Beskyttet
	Beskyttet(new Access("beskyttet")),
	Beskyttet_Slett_Utjevningsmandater(Beskyttet.child("slett_utjevningsmandater")),
	Beskyttet_Slett_Valgoppgjør(Beskyttet.child("slett_valgoppgjør")),
	Beskyttet_Slett_Stemmegiving(Beskyttet.child("slett_stemmegiving")),
	Beskyttet_Slett_Velgere_Uten_Område(Beskyttet.child("slett_velgere_uten_område")),
	Beskyttet_Slett_Velgere_Med_Område(Beskyttet.child("slett_velgere_med_område")),
	Beskyttet_List_Slett_Velgere(Beskyttet.child("list_slett_velgere")),
	Beskyttet_Ny_Manntallsinnlasting(Beskyttet.child("ny_manntallsinnlasting")),
	Beskyttet_Slett_Opptelling(Beskyttet.child("slett_opptelling")),

	// Aggregerte aksesser
	Aggregert_Valghendelse(Konfigurasjon_Valghendelse),
	
	Aggregert_Partier(Parti),
	
	Aggregert_Brukere_Roller(Tilgang_Brukere, Tilgang_Roller),

	Aggregert_Listeforslag(Listeforslag),

	Aggregert_Listeforslag_Opprett(Listeforslag_Opprett),
	
	Aggregert_Opptelling(Opptelling),

	Aggregert_Opptelling_Rediger(
		Opptelling_Forhånd_Rediger,
		Opptelling_Valgting_Rediger),

	Aggregert_Opptelling_Sett_Til_Valgoppgjør(
		Opptelling_Rettelser_Rediger,
		Aggregert_Opptelling_Rediger),

	Aggregert_Opptelling_Lagt_Til_Side(Opptelling_Lagt_Til_Side),
	
	Aggregert_Konfigurasjon(Konfigurasjon),
	
	Aggregert_Stemmegiving_Registrer(Stemmegiving_Forhånd, Stemmegiving_Valgting),
	
	Aggregert_Stemmegiving_Prøving(Stemmegiving_Prøving),
	
	Aggregert_Stemmegiving(Stemmegiving),

	Aggregert_Manntall(Manntall),

	Aggregert_Manntall_Søk(Manntall_Søk, Manntall_Søk_Kommune),

	Aggregert_Beskyttet(Beskyttet),
	
	Aggregert_Rapport(Rapport),

	Aggregert_Resultat(Resultat),

	Aggregert_Konfigurasjon_Grunnlagsdata(Konfigurasjon_Grunnlagsdata),
	
	Aggregert_Valghierarki(Konfigurasjon_Valg);

	// @formatter:on

	private Access access;
	private Accesses[] aggregated;
	private String[] paths;

	Accesses(Access access) {
		this.access = access;
		this.paths = new String[] { access.getPath() };
	}

	Accesses(Accesses... aggregated) {
		this.aggregated = aggregated;
	}

	public static Accesses fromAccess(Access access) {
		for (Accesses a : values()) {
			if (a.is(access.getPath())) {
				return a;
			}
		}
		throw new IllegalArgumentException("Access is not Accesses '" + access.getPath() + "'");
	}

	public boolean is(Accesses a) {
		if (!a.isTypeStandard()) {
			return false;
		}
		for (String p : paths()) {
			if (p.equals(a.access.getPath())) {
				return true;
			}
		}
		return false;
	}

	public boolean is(String access) {
		return paths().length == 1 && paths()[0].equals(access);
	}

	public String[] paths() {
		if (paths == null && isTypeAggregated()) {
			Set<String> aggregatedPaths = collectAggregatedPaths();
			paths = aggregatedPaths.toArray(new String[aggregatedPaths.size()]);
		}
		return paths;
	}

	public Access getAccess() {
		if (access == null) {
			throw new UnsupportedOperationException();
		}
		return access;
	}

	private boolean isTypeStandard() {
		return access != null;
	}

	private boolean isTypeAggregated() {
		return aggregated != null;
	}

	private Access child(String path) {
		if (!isTypeStandard()) {
			throw new IllegalArgumentException("Cannot add child to an aggregated Access");
		}
		return new Access(access, path);
	}

	private Set<String> collectAggregatedPaths() {
		Set<String> result = new TreeSet<>();
		for (Accesses a : aggregated) {
			result.addAll(collectTopDown(a));
		}
		return result;
	}

	private Set<String> collectTopDown(Accesses child) {
		Set<String> result = new HashSet<>();
		if (child.isTypeStandard()) {
			for (Accesses a : values()) {
				if (a.isTypeStandard() && a.access.getPath().startsWith(child.access.getPath())) {
					result.add(a.access.getPath());
				}
			}
		} else {
			for (Accesses childOfChild : child.aggregated) {
				result.addAll(collectTopDown(childOfChild));
			}
		}
		return result;
	}
}
