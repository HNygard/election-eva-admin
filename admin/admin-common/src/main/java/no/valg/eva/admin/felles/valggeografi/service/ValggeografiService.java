package no.valg.eva.admin.felles.valggeografi.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.Land;
import no.valg.eva.admin.felles.valggeografi.model.Rode;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.Valghendelse;

public interface ValggeografiService extends Serializable {
	Valggeografi valggeografi(ValggeografiSti valggeografiSti);
	
	Valghendelse valghendelse(UserData userData);

	Land land(UserData userData);

	List<Fylkeskommune> fylkeskommuner(UserData userData, ValghierarkiSti valghierarkiSti, CountCategory countCategory);

	Kommune kommune(UserData userData, KommuneSti kommuneSti);

	List<Kommune> kommuner(UserData userData, FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory);

	List<Kommune> kommunerForValghendelse(UserData userData);

	List<Bydel> bydeler(UserData userData, KommuneSti kommuneSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory);

	Stemmekrets stemmekrets(StemmekretsSti stemmekretsSti);

	List<Stemmekrets> stemmekretser(UserData userData, BydelSti bydelSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory);

	List<Stemmekrets> stemmekretser(KommuneSti kommuneSti, PollingDistrictType... types);

	List<Stemmested> stemmesteder(UserData userData, StemmekretsSti stemmekretsSti);

	List<Rode> roder(StemmestedSti stemmestedSti);

    Bydel bydel(BydelSti bydelSti);
}
