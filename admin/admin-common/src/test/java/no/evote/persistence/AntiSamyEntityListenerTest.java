package no.evote.persistence;

import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = TestGroups.RESOURCES)
public class AntiSamyEntityListenerTest {

	@Test
	public void doPrePersist_withVoter_shouldStripInvalidCharacters() throws Exception {
		AntiSamyEntityListener listener = new AntiSamyEntityListener();
		Voter voter = new Voter();
		voter.setAdditionalInformation("text_with_line_\rseperator");

		listener.doPrePersist(voter);

		assertThat(voter.getAdditionalInformation()).isEqualTo("text_with_line_ seperator");
	}

	@Test
	public void doPreUpdate_withMunicipality_shouldNotStripNewLineOnElectionCardText() throws Exception {
		AntiSamyEntityListener listener = new AntiSamyEntityListener();
		Municipality municipality = new Municipality();
		municipality.setElectionCardText("text_with_line_\rseperator");

		listener.doPreUpdate(municipality);

		assertThat(municipality.getElectionCardText()).isEqualTo("text_with_line_\rseperator");
	}

	@Test
	public void doPostLoad_withPollingPlace_shouldNotStripNewLineOnElectionCardText() throws Exception {
		AntiSamyEntityListener listener = new AntiSamyEntityListener();
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setInfoText("text_with_line_\rseperator");

		listener.doPostLoad(pollingPlace);

		assertThat(pollingPlace.getInfoText()).isEqualTo("text_with_line_\rseperator");
	}

}
