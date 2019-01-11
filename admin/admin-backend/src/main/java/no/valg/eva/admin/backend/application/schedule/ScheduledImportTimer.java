package no.valg.eva.admin.backend.application.schedule;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.evote.service.configuration.IncrementalElectoralRollImporter;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.application.service.SystemPasswordApplicationService;
import no.evote.service.util.TaskLogger;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;
import no.valg.eva.admin.rbac.repository.OperatorRepository;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

@Stateless
public class ScheduledImportTimer {
	private static final String APOSTROPHE = "'";
	private static final Logger LOGGER = Logger.getLogger(ScheduledImportTimer.class);
	
	@Inject
	private TaskLogger taskLogger;
	@Inject
	private VoterImportBatchRepository voterImportBatchRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private OperatorRepository operatorRepository;
	@Inject
	private LegacyUserDataServiceBean userDataService;
	@Inject
	private IncrementalElectoralRollImporter incrementalElectoralRollImporter;
	@Inject
	private SystemPasswordApplicationService systemPasswordService;
	@Inject
	private SigningKeyRepository signingKeyRepository;
	@Resource
	private SessionContext context;

	/**
	 * Scheduled task that performs scheduled import of electoral roll files for eligible election vents
	 */
	@Schedules({ @Schedule(minute = "1", hour = "*") })
	public void scheduledImportOfElectionEvents() {
		try {
			if (!systemPasswordService.isPasswordSet()) {
				return;
			}

			if (!Boolean.valueOf(EvoteProperties.getProperty(EvoteProperties.ELECTORAL_ROLL_PERFORM_SCHEDULED_INCREMENTAL_IMPORT, "true"))) {
				LOGGER.info("scheduledImportOfElectionEvents - node is not configured to perform scheduled incremental import");
				return;
			}

			taskLogger.logTask(getClass().getName(), DateTime.now());

			LOGGER.info("scheduledImportOfElectionEvents start");
			List<ElectionEvent> electionEventList = electionEventRepository.findAllActiveElectionEvents();
			if (!electionEventList.isEmpty()) {
				LOGGER.info("number of active election events: '" + electionEventList.size() + APOSTROPHE);
			}
			for (ElectionEvent electionEvent : electionEventList) {
				try {
					VoterImportBatch voterImportBatch = voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
					if (voterImportBatch != null) {
						if (operatorRepository.findByElectionEventsAndId(electionEvent.getPk(), EvoteConstants.SCHEDULED_IMPORT_OPERATOR_ID) == null) {
							LOGGER.info("scheduled_import_operator_id " + EvoteConstants.SCHEDULED_IMPORT_OPERATOR_ID + " is not defined for '"
									+ electionEvent.getName() + APOSTROPHE);
							LOGGER.info("no scheduled import will be performed for '" + electionEvent.getName() + APOSTROPHE);
							continue;
						}
						UserData userData = userDataService.getUserData(EvoteConstants.SCHEDULED_IMPORT_OPERATOR_ID, EvoteConstants.SCHEDULED_IMPORT_ROLE,
								electionEvent.getId(), electionEvent.getId(), InetAddress.getLocalHost());
						if (signingKeyRepository.isSigningKeySetForElectionEvent(electionEvent)) {
							incrementalElectoralRollImporter.incrementalImportElectoralRoll(userData, electionEvent, context);
						} else {
							LOGGER.info("signing key is NOT set for '" + electionEvent.getName() + APOSTROPHE);
						}
					} else {
						LOGGER.info("no intitial import has been performed for '" + electionEvent.getName() + APOSTROPHE);
						LOGGER.info("no scheduled import will be performed for '" + electionEvent.getName() + APOSTROPHE);
					}
				} catch (RuntimeException | UnknownHostException e) {
					// In no way should any exception be propagated to the scheduled task, as it will cause the scheduled task to be removed from the
					// application server
					LOGGER.error(e.getMessage(), e);
				}
			}
			LOGGER.info("scheduledImportOfElectionEvents end");
		} catch (RuntimeException e) {
			// In no way should any exception be propagated to the scheduled task, as it will cause the scheduled task to be removed from the application server
			LOGGER.error(e.getMessage(), e);
		}
	}
}
