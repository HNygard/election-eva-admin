package no.valg.eva.admin.configuration.domain.model.mapper.valghierarki;

import static no.valg.eva.admin.configuration.test.data.domain.model.MvElectionDigestTestData.MV_ELECTION_DIGEST_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_11;
import static no.valg.eva.admin.felles.test.valghierarki.model.ValghierarkiAssert.assertThat;

import no.valg.eva.admin.felles.valghierarki.model.Valg;

import org.testng.annotations.Test;

public class ValgMapperTest {
	@Test
	public void valg_gittMvElectionDigest_returnererValg() throws Exception {
		Valg resultat = ValgMapper.valg(MV_ELECTION_DIGEST_111111_11_11);
		assertThat(resultat).isEqualTo(VALG_111111_11_11);
	}

}
