package no.valg.eva.admin.felles.test.data.sti.valggeografi;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;

public final class ValggeografiStiTestData {
	private ValggeografiStiTestData() {
	}

	public static ValggeografiSti valggeografiSti() {
		return mock(ValggeografiSti.class, RETURNS_DEEP_STUBS);
	}
}
