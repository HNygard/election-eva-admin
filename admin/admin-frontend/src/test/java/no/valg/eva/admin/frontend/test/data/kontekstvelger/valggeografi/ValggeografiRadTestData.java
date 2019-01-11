package no.valg.eva.admin.frontend.test.data.kontekstvelger.valggeografi;

import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_111111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_111111;

import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.LandSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiRad;

public final class ValggeografiRadTestData {
	public static final ValggeografiRad<ValghendelseSti> VALGHENDELSE_RAD_111111 = new ValggeografiRad<>(VALGHENDELSE_111111);
	public static final ValggeografiRad<LandSti> LAND_RAD_111111_11 = new ValggeografiRad<>(LAND_111111_11);
	public static final ValggeografiRad<FylkeskommuneSti> FYLKESKOMMUNE_RAD_111111_11_11 = new ValggeografiRad<>(FYLKESKOMMUNE_111111_11_11);
	public static final ValggeografiRad<KommuneSti> KOMMUNE_RAD_111111_11_11_1111 = new ValggeografiRad<>(KOMMUNE_111111_11_11_1111);
	public static final ValggeografiRad<BydelSti> BYDEL_RAD_111111_11_11_1111_111111 = new ValggeografiRad<>(BYDEL_111111_11_11_1111_111111);
	public static final ValggeografiRad<StemmekretsSti> STEMMEKRETS_RAD_111111_11_11_1111_111111_1111 =
			new ValggeografiRad<>(STEMMEKRETS_111111_11_11_1111_111111_1111);

	private ValggeografiRadTestData() {
	}
}
