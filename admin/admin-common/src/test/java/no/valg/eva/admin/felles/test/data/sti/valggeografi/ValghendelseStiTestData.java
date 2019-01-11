package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_ID_111111;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;

public final class ValghendelseStiTestData {
	public static final ValghendelseSti VALGHENDELSE_STI_111111 = new ValghendelseSti(VALGHENDELSE_ID_111111);
	public static final ValghendelseSti VALGHENDELSE_STI = VALGHENDELSE_STI_111111;

	private ValghendelseStiTestData() {
	}

	public static ValghendelseSti valghendelseSti() {
		return mock(ValghendelseSti.class, RETURNS_DEEP_STUBS);
	}
}
