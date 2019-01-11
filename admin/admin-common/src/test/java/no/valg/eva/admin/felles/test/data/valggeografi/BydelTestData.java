package no.valg.eva.admin.felles.test.data.valggeografi;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.BydelStiTestData.BYDEL_STI_111111_11_11_1111_111113;

import java.util.List;

import no.valg.eva.admin.felles.valggeografi.model.Bydel;

public final class BydelTestData {
	public static final String BYDEL_ID_111111 = "111111";
	public static final String BYDEL_ID_111112 = "111112";
	public static final String BYDEL_ID_111113 = "111113";
	public static final String BYDEL_NAVN_111111_11_11_1111_111111 = "BYDEL_111111_11_11_1111_111111";
	public static final String BYDEL_NAVN_111111_11_11_1111_111112 = "BYDEL_111111_11_11_1111_111112";
	public static final String BYDEL_NAVN_111111_11_11_1111_111113 = "BYDEL_111111_11_11_1111_111113";
	public static final Bydel BYDEL_111111_11_11_1111_111111 = new Bydel(BYDEL_STI_111111_11_11_1111_111111, BYDEL_NAVN_111111_11_11_1111_111111, false);
	public static final Bydel BYDEL_111111_11_11_1111_111112 = new Bydel(BYDEL_STI_111111_11_11_1111_111112, BYDEL_NAVN_111111_11_11_1111_111112, false);
	public static final Bydel BYDEL_111111_11_11_1111_111113 = new Bydel(BYDEL_STI_111111_11_11_1111_111113, BYDEL_NAVN_111111_11_11_1111_111113, false);
	public static final List<Bydel> BYDELER_111111_11_11_1111_11111X =
			asList(BYDEL_111111_11_11_1111_111111, BYDEL_111111_11_11_1111_111112, BYDEL_111111_11_11_1111_111113);

	private BydelTestData() {
	}
}
