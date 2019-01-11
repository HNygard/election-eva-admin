package no.valg.eva.admin.felles.valghierarki.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;

public interface ValghierarkiService extends Serializable {
	Valghierarki valghierarki(ValghierarkiSti valghierarkiSti);

	Valghendelse valghendelse(UserData userData);

	List<Valggruppe> valggrupper(UserData userData);

	Valg valg(ValgSti valgSti);

	List<Valg> valg(UserData userData, ValggruppeSti valggruppeSti, CountCategory countCategory);

	List<Valgdistrikt> valgdistrikter(UserData userData, ValgSti valgSti);

	List<Valgdistrikt> valgdistrikter(UserData userData, ValgSti valgSti, ValggeografiSti valggeografiSti);
}
