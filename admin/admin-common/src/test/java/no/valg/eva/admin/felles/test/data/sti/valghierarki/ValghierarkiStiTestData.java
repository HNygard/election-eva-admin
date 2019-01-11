package no.valg.eva.admin.felles.test.data.sti.valghierarki;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public final class ValghierarkiStiTestData {
	private ValghierarkiStiTestData() {
	}

	public static ValghierarkiSti valghierarkiSti() {
		return mock(ValghierarkiSti.class, RETURNS_DEEP_STUBS);
	}
}
