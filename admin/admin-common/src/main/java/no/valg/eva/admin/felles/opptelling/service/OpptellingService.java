package no.valg.eva.admin.felles.opptelling.service;


import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public interface OpptellingService {
	void slettOpptellinger(
			UserData userData, ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, CountCategory[] countCategories, Styretype[] styretyper);
}
