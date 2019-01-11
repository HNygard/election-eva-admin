package no.valg.eva.admin.common.configuration.service;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;

public interface OpptellingskategoriService {
	List<CountCategory> countCategoriesForValgSti(UserData userData, ValgSti valgSti);

	List<CountCategory> countCategories(UserData userData);
}
