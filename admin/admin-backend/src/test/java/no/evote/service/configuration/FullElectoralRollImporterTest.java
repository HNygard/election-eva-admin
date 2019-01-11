package no.evote.service.configuration;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.event.ManntallsimportFullfortEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import javax.ejb.SessionContext;
import javax.enterprise.event.Event;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FullElectoralRollImporterTest extends MockUtilsTestCase {

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.fileDoesNotExist")
	public void validateImportFile_withNonExistingFile_throwsError() throws Exception {
		FullElectoralRollImporter bean = initializeMocks(FullElectoralRollImporter.class);
		ElectionEvent electionEvent = createMock(ElectionEvent.class);

		bean.validateImportFile(electionEvent, "NoValidFile.txt");
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.fileNotInitBatch")
	public void validateImportFile_withNoInitialBatchFileFile_throwsError() throws Exception {
		FullElectoralRollImporter bean = initializeMocks(FullElectoralRollImporter.class);
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		URL resource = this.getClass().getResource("/electoralRoll/notIsFileInitialBatchFile.txt");

		bean.validateImportFile(electionEvent, resource.getPath());
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.alreadyImported")
	public void validateImportFile_withAlreadyImported_throwsError() throws Exception {
		FullElectoralRollImporter bean = initializeMocks(FullElectoralRollImporter.class);
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		URL resource = this.getClass().getResource("/electoralRoll/electoralRoll-demo.txt");

		bean.validateImportFile(electionEvent, resource.getPath());
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.votersForElectionEvent")
	public void validateImportFile_withAreVotersInElectionEvent_throwsError() throws Exception {
		FullElectoralRollImporter bean = initializeMocks(FullElectoralRollImporter.class);
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		URL resource = this.getClass().getResource("/electoralRoll/electoralRoll-demo.txt");
		when(getInjectMock(VoterImportBatchRepository.class).findSingleByElectionEvent(anyLong())).thenReturn(null);
		when(getInjectMock(VoterRepository.class).areVotersInElectionEvent(anyLong())).thenReturn(true);

		bean.validateImportFile(electionEvent, resource.getPath());
	}
	
	@Test
	public void fullImportElectoralRoll_withValidParameters_performsImport() throws Exception {
		FullElectoralRollImporter bean = initializeMocks(FullElectoralRollImporter.class);
		URL resource = this.getClass().getResource("/electoralRoll/electoralRoll-demo.txt");
		when(getInjectMock(VoterImportBatchRepository.class).findSingleByElectionEvent(anyLong())).thenReturn(null);
		ElectionEvent electionEvent = createMock(ElectionEvent.class);
		when(electionEvent.getId()).thenReturn("111111");
		
        Event manntallsimportFullfortEvent = mockField("manntallsimportFullfortEvent", Event.class);
        bean.fullImportElectoralRoll(createMock(UserData.class), electionEvent, resource.getPath(), createMock(SessionContext.class), false);
        verify((Event<ManntallsimportFullfortEvent>) manntallsimportFullfortEvent).fire(any());
	}
}
