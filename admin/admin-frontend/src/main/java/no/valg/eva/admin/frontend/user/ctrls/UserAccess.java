package no.valg.eva.admin.frontend.user.ctrls;

import static no.valg.eva.admin.common.rbac.Accesses.Admin;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Godkjenne;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Oppheve;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsvalgstyrer;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Overstyre;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valg;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valgdistrikt;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valggruppe;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valghendelse_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Last_Ned_Flettefil;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Historikk;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Rediger_Person;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Søk;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Søk_Kommune;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Valgkort;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgoppgjør_Gjennomføre;
import static no.valg.eva.admin.common.rbac.Accesses.Resultat_Rapporter;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Prøving_Forhånd_Enkelt;
import static no.valg.eva.admin.common.rbac.Accesses.Stemmegiving_Prøving_Valgting_Enkelt;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Importer_Forhånd;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Importer_Valgting;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Accesses;

@Named
@SessionScoped
public class UserAccess implements Serializable {

	private UserData userData;

	public UserAccess() {
		// CDI
	}

	@Inject
	public UserAccess(UserData userData) {
		this.userData = userData;
	}

	public boolean isOverrideAccess() {
		return hasAccess(Konfigurasjon_Overstyre);
	}

	public boolean hasAccess(Accesses... accesses) {
		return userData.hasAccess(accesses);
	}

	public boolean isManntallSøkKommune() {
		return hasAccess(Manntall_Søk_Kommune);
	}

	public boolean isKonfigurasjonGrunnlagsdataRedigere() {
		return hasAccess(Konfigurasjon_Grunnlagsdata_Redigere);
	}

	public boolean isKonfigurasjonOpptellingsvalgstyrer() {
		return hasAccess(Konfigurasjon_Opptellingsvalgstyrer);
	}

	public boolean isKonfigurasjonGrunnlagsdataGodkjenne() {
		return hasAccess(Konfigurasjon_Grunnlagsdata_Godkjenne);
	}

	public boolean isKonfigurasjonGrunnlagsdataOppheve() {
		return hasAccess(Konfigurasjon_Grunnlagsdata_Oppheve);
	}

	public boolean isKonfigurasjonGeografi() {
		return hasAccess(Konfigurasjon_Geografi);
	}

	public boolean isOpptellingValgoppgjørGjennomføre() {
		return hasAccess(Opptelling_Valgoppgjør_Gjennomføre);
	}

	public boolean isKonfigurasjonValgValggruppe() {
		return hasAccess(Konfigurasjon_Valg_Valggruppe);
	}

	public boolean isKonfigurasjonValgValg() {
		return hasAccess(Konfigurasjon_Valg_Valg);
	}

	public boolean isKonfigurasjonValgValgdistrikt() {
		return hasAccess(Konfigurasjon_Valg_Valgdistrikt);
	}

	public boolean isKonfigurasjonValghendelseRediger() {
		return hasAccess(Konfigurasjon_Valghendelse_Rediger);
	}

	public boolean isResultatRapporter() {
		return hasAccess(Resultat_Rapporter);
	}

	public boolean isListeforslagRediger() {
		return hasAccess(Listeforslag_Rediger);
	}

	public boolean isStemmegivingPrøvingForhåndEnkelt() {
		return hasAccess(Stemmegiving_Prøving_Forhånd_Enkelt);
	}

	public boolean isStemmegivingPrøvingValgtingEnkelt() {
		return hasAccess(Stemmegiving_Prøving_Valgting_Enkelt);
	}

	public boolean isManntallRedigerPerson() {
		return hasAccess(Manntall_Rediger_Person);
	}

	public boolean isManntallSøk() {
		return hasAccess(Manntall_Søk);
	}

	public boolean isManntallHistorikk() {
		return hasAccess(Manntall_Historikk);
	}

	public boolean isManntallValgkort() {
		return hasAccess(Manntall_Valgkort);
	}

	public boolean isListeforslagLastNedFlettefil() {
		return hasAccess(Listeforslag_Last_Ned_Flettefil);
	}

	public boolean isBrukereImporterForhand() {
		return hasAccess(Tilgang_Brukere_Importer_Forhånd);
	}

	public boolean isBrukereImporterValgting() {
		return hasAccess(Tilgang_Brukere_Importer_Valgting);
	}

	public boolean isAdmin() {
		return hasAccess(Admin);
	}

}
