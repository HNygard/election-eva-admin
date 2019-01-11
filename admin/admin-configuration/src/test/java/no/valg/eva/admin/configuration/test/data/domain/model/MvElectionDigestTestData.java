package no.valg.eva.admin.configuration.test.data.domain.model;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111112;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111113;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_12;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_13;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_12;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_13;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_NAVN_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_NAVN_111111_11_12;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_NAVN_111111_11_13;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_NAVN_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_NAVN_111111_11_11_111112;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_NAVN_111111_11_11_111113;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_12;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_13;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_NAVN_111111;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.MvElectionDigest;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public final class MvElectionDigestTestData {
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111 = mvElectionDigest(ELECTION_PATH_111111, VALGHENDELSE_NAVN_111111);

	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11 = mvElectionDigest(ELECTION_PATH_111111_11, VALGGRUPPE_NAVN_111111_11);
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_12 = mvElectionDigest(ELECTION_PATH_111111_12, VALGGRUPPE_NAVN_111111_12);
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_13 = mvElectionDigest(ELECTION_PATH_111111_13, VALGGRUPPE_NAVN_111111_13);

	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11_11 = mvElectionDigest(ELECTION_PATH_111111_11_11, VALG_NAVN_111111_11_11, FYLKESKOMMUNE);
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11_12 = mvElectionDigest(ELECTION_PATH_111111_11_12, VALG_NAVN_111111_11_12, KOMMUNE);
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11_13 = mvElectionDigest(ELECTION_PATH_111111_11_13, VALG_NAVN_111111_11_13, BYDEL);

	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11_11_111111 =
			mvElectionDigest(ELECTION_PATH_111111_11_11_111111, VALGDISTRIKT_NAVN_111111_11_11_111111, FYLKESKOMMUNE);
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11_11_111112 =
			mvElectionDigest(ELECTION_PATH_111111_11_11_111112, VALGDISTRIKT_NAVN_111111_11_11_111112, FYLKESKOMMUNE);
	public static final MvElectionDigest MV_ELECTION_DIGEST_111111_11_11_111113 =
			mvElectionDigest(ELECTION_PATH_111111_11_11_111113, VALGDISTRIKT_NAVN_111111_11_11_111113, FYLKESKOMMUNE);

	public static final List<MvElectionDigest> MV_ELECTION_DIGESTS_111111_1X =
			asList(MV_ELECTION_DIGEST_111111_11, MV_ELECTION_DIGEST_111111_12, MV_ELECTION_DIGEST_111111_13);
	public static final List<MvElectionDigest> MV_ELECTION_DIGESTS_111111_11_1X =
			asList(MV_ELECTION_DIGEST_111111_11_11, MV_ELECTION_DIGEST_111111_11_12, MV_ELECTION_DIGEST_111111_11_13);
	public static final List<MvElectionDigest> MV_ELECTION_DIGESTS_111111_11_11_11111X =
			asList(MV_ELECTION_DIGEST_111111_11_11_111111, MV_ELECTION_DIGEST_111111_11_11_111112, MV_ELECTION_DIGEST_111111_11_11_111113);


	private MvElectionDigestTestData() {
	}

	private static MvElectionDigest mvElectionDigest(ElectionPath electionPath, String navn) {
		return mvElectionDigest(electionPath, navn, null);
	}

	public static MvElectionDigest mvElectionDigest(ElectionPath electionPath, String navn, ValggeografiNivaa valggeografiNivaa) {
		MvElectionDigest mvElectionDigest = mock(MvElectionDigest.class, RETURNS_DEEP_STUBS);
		when(mvElectionDigest.electionPath()).thenReturn(electionPath);
		when(mvElectionDigest.electionHierarchyName()).thenReturn(navn);
		if (valggeografiNivaa != null) {
			when(mvElectionDigest.valggeografiNivaa()).thenReturn(valggeografiNivaa);
		}
		when(mvElectionDigest.getElectionDigest().isEnkeltOmrade()).thenReturn(true);
		return mvElectionDigest;
	}
}
