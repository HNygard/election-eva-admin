package no.valg.eva.admin.felles.test.data.valggeografi;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValghendelseStiTestData.VALGHENDELSE_STI_111111;

import no.valg.eva.admin.felles.valggeografi.model.Valghendelse;

public final class ValghendelseTestData {
	public static final String VALGHENDELSE_ID_111111 = "111111";
	public static final String VALGHENDELSE_ID_111112 = "111112";
	public static final String VALGHENDELSE_NAVN_111111 = "VALGHENDELSE_111111";
	public static final Valghendelse VALGHENDELSE_111111 = new Valghendelse(VALGHENDELSE_STI_111111, VALGHENDELSE_NAVN_111111);

	private ValghendelseTestData() {
	}
}
