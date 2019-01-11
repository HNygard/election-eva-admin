package no.valg.eva.admin.voting.domain.electoralroll;

import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.BORN_IN_1999;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUNICIPALITY_ID_NOT_EXIST;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUNICIPALITY_ID_PORSGRUNN;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUNICIPALITY_ID_SVALBARD;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUST_BE_BORN_BEFORE_PORSGRUNN;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUST_BE_BORN_BEFORE_SVALBARD;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.VALGHENDELSESID;
import static org.assertj.core.api.Assertions.assertThat;

import no.evote.util.EvoteProperties;
import no.evote.util.EvotePropertiesTestUtil;
import no.valg.eva.admin.common.MunicipalityId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class StemmerettIKommuneTest {

	@Test
	public void forVelger_hvisVelgerenIkkeErGammelNok_retunererFalse() throws Exception {
		StemmerettIKommune stemmerettIKommune = buildStemmerettIKommune();

		Voter voterSvalbardNotEligible = getVelger(MUNICIPALITY_ID_SVALBARD, BORN_IN_1999);

		assertThat(stemmerettIKommune.forVelger(voterSvalbardNotEligible)).isFalse();
	}

	@Test
	public void forVelger_hvisVelgerenErGammelNok_returnererTrue() throws Exception {
		StemmerettIKommune stemmerettIKommune = buildStemmerettIKommune();

		Voter voterPorsgrunnEligible = getVelger(MUNICIPALITY_ID_PORSGRUNN, BORN_IN_1999);

		assertThat(stemmerettIKommune.forVelger(voterPorsgrunnEligible)).isTrue();
	}
	
	@Test
	public void forVelger_hvisValgdistriktIkkeFinnesForKommuneOgKonfigurasjonsparameterForAaIgnorereErSatt_returnererTrue() {
		EvotePropertiesTestUtil.setProperty(EvoteProperties.MANNTALLSIMPORT_IGNORER_MANGLENDE_STEMMERETTSALDER_FOR, ElectoralRollObjectMother.VALGHENDELSESID);
		StemmerettIKommune stemmerettIKommune = buildStemmerettIKommune();
		EvotePropertiesTestUtil.reinitializeProperties();

		Voter voterNotExistingMunicipality = getVelger(MUNICIPALITY_ID_NOT_EXIST, BORN_IN_1999);

		assertThat(stemmerettIKommune.forVelger(voterNotExistingMunicipality)).isTrue();
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void forVelger_hvisStemmerettskriterierIkkeErKjentForKommunen_throwsException() throws Exception {
		StemmerettIKommune stemmerettIKommune = buildStemmerettIKommune();

		Voter voterNotExistingMunicipality = getVelger(MUNICIPALITY_ID_NOT_EXIST, BORN_IN_1999);

		stemmerettIKommune.forVelger(voterNotExistingMunicipality);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void forVelger_hvisStemmerettskriterierIkkeErKjentForKommunenOgAnnenValghendelseErKonfigurertForÅIgnorere_throwsException() throws Exception {
		EvotePropertiesTestUtil.setProperty(EvoteProperties.MANNTALLSIMPORT_IGNORER_MANGLENDE_STEMMERETTSALDER_FOR, ElectoralRollObjectMother.VALGHENDELSESID_ANNEN);
		StemmerettIKommune stemmerettIKommune = buildStemmerettIKommune();
		Voter voterNotExistingMunicipality = getVelger(MUNICIPALITY_ID_NOT_EXIST, BORN_IN_1999);
		EvotePropertiesTestUtil.reinitializeProperties();

		stemmerettIKommune.forVelger(voterNotExistingMunicipality);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void forVelger_hvisFødselsdatoIkkeErSpesifisert_throwsException() throws Exception {
		StemmerettIKommune stemmerettIKommune = buildStemmerettIKommune();

		Voter voterNotExistingMunicipality = getVelger(MUNICIPALITY_ID_NOT_EXIST, null);

		stemmerettIKommune.forVelger(voterNotExistingMunicipality);
	}

	private StemmerettIKommune buildStemmerettIKommune() {
		StemmerettIKommune stemmerettIKommune = new StemmerettIKommune(new ElectionEvent(VALGHENDELSESID, "", null));
		stemmerettIKommune.put(new MunicipalityId(MUNICIPALITY_ID_PORSGRUNN), MUST_BE_BORN_BEFORE_PORSGRUNN);
		stemmerettIKommune.put(new MunicipalityId(MUNICIPALITY_ID_SVALBARD), MUST_BE_BORN_BEFORE_SVALBARD);
		return stemmerettIKommune;
	}

	private Voter getVelger(String municipalityId, LocalDate dateOfBirth) {
		Voter velger = new Voter();

		velger.setMunicipalityId(municipalityId);
		velger.setDateOfBirth(dateOfBirth);

		return velger;
	}

}
