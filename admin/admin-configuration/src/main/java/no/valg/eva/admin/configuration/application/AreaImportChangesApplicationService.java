package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent.addAuditEvent;
import static no.valg.eva.admin.common.rbac.Accesses.Import_Områder_Endringer;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.util.CSVUtil;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.PollingDistrictAuditEvent;
import no.valg.eva.admin.common.configuration.service.AreaImportChangesService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "AreaImportChangesService")


@Default
@Remote(AreaImportChangesService.class)
public class AreaImportChangesApplicationService implements AreaImportChangesService {

	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private PollingPlaceRepository pollingPlaceRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;

	private static final String SPACE = " ";
	private static final String DOT = ".";
	private static final String COUNTRY_ID = "47";
	private static final String COLON_SPACE = ": ";

	@Override
	@Security(accesses = Import_Områder_Endringer, type = WRITE)
	@AuditLog(eventClass = CompositeAuditEvent.class, eventType = AuditEventTypes.ImportDistrictsChanges, objectSource = AuditedObjectSource.Collected)
	public byte[] importAreaHierarchyChanges(UserData userData, byte[] contents) throws IOException {

		List<List<String>> rows = getImportFileRows(contents);

		StringBuilder logFileUpdate = new StringBuilder();
		StringBuilder logFileUpdateFailed = new StringBuilder();
		StringBuilder logFileCreate = new StringBuilder();
		StringBuilder logFileCreateFailed = new StringBuilder();
		StringBuilder logFileDelete = new StringBuilder();
		StringBuilder logFileDeleteFailed = new StringBuilder();

		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
		String countryAreaPath = retrieveAreaPath(electionEvent, null);

		Collection<MvArea> allExistingPollingDistrictMvAreas = mvAreaRepository.findByPathAndLevel(countryAreaPath, AreaLevelEnum.POLLING_DISTRICT.getLevel());

		int line = 1;

		for (List<String> row : rows) {
			String rowContent = row.get(0);
			if (isNotEmptyLine(rowContent) && !isHeadingOfImportFile(rowContent)) {

				String pollingDistrictAreaPath = retrieveAreaPath(electionEvent, rowContent);
				List<MvArea> existingPollingDistricMvAreas = mvAreaRepository.findByPathAndLevel(pollingDistrictAreaPath,
						AreaLevelEnum.POLLING_DISTRICT.getLevel());

				if (isNotEmptyMvAreaList(existingPollingDistricMvAreas)) {

					updateDistrictIfDBNameDifferentiateToImportName(userData, logFileUpdate, logFileUpdateFailed, allExistingPollingDistrictMvAreas, line,
							rowContent, existingPollingDistricMvAreas.get(0));

				} else {
					createNewDistrict(userData, electionEvent, logFileCreate, logFileCreateFailed, line, rowContent);
				}
			}
			++line;
		}

		deleteAllDistrictsNotInImportFile(userData, logFileDelete, logFileDeleteFailed, allExistingPollingDistrictMvAreas);

		StringBuilder logFile = new StringBuilder();
		logFile.append(logFileUpdate.toString());
		logFile.append(logFileUpdateFailed.toString());
		logFile.append(logFileCreate.toString());
		logFile.append(logFileCreateFailed.toString());
		logFile.append(logFileDelete.toString());
		logFile.append(logFileDeleteFailed.toString());
		return logFile.toString().getBytes();
	}

	private boolean isNotParentDistrict(MvArea pollingDistrictMvArea) {
		PollingPlace pp = pollingPlaceRepository.findFirstPollingPlace(pollingDistrictMvArea.getPollingDistrict().getPk());
		return pp == null && !pollingDistrictMvArea.getPollingDistrict().isParentPollingDistrict();
	}

	private boolean isNotEmptyLine(String rowContent) {
		return !rowContent.trim().equals("");
	}

	private void createNewDistrict(UserData userData, ElectionEvent electionEvent, StringBuilder logFileCreate, StringBuilder logFileCreateFailed, int line,
			String contentImportFile) {

		
		String municipalityId = contentImportFile.substring(0, 4);
		String pollingDistrictId = retrievePollingDistrictId(contentImportFile.substring(5, 15).trim());
		String pollingDistrictName = formatNameToFirstLetterUppercaseTheRestLowercase(contentImportFile.substring(16).trim());

		String pollingDistrictAreaPath = retrieveAreaPath(electionEvent, contentImportFile);

		List<MvArea> mvAreas = mvAreaRepository.findByPathAndLevel(pollingDistrictAreaPath.substring(0, 24), AreaLevelEnum.BOROUGH.getLevel());
		
		if (isNotEmptyMvAreaList(mvAreas)) {
			PollingDistrict newDistrict = newPollingDistrict(pollingDistrictId, pollingDistrictName, mvAreas.get(0).getBorough());
			pollingDistrictRepository.create(userData, newDistrict);
			PollingDistrictAuditEvent pollingDistrictAuditEvent = new PollingDistrictAuditEvent(userData, newDistrict,
					AuditEventTypes.ImportDistrictsChangesCreate, Outcome.Success, null);
			addAuditEvent(pollingDistrictAuditEvent);
			logFileCreate.append(importLineMsg("CREATED LINE " + line, municipalityId, pollingDistrictId, pollingDistrictName)).append(System.lineSeparator());
		} else {
			logFileCreateFailed.append(importLineMsg("CREATE FAILED AT LINE " + line, municipalityId, pollingDistrictId, pollingDistrictName)).append(
					System.lineSeparator());
		}
	}

	private boolean isNotEmptyMvAreaList(List<MvArea> mvAreas) {
		return !mvAreas.isEmpty() && mvAreas.get(0) != null;
	}

	private void updateDistrictIfDBNameDifferentiateToImportName(UserData userData, StringBuilder logFileUpdate, StringBuilder logFileUpdateFailed,
			Collection<MvArea> existingPollingDistrictMvAreas, int line, String contentImportFile,
			MvArea pollingDistrictMvAreaFromDb) {

		
		String municipalityId = contentImportFile.substring(0, 4);
		String pollingDistrictId = retrievePollingDistrictId(contentImportFile.substring(5, 15).trim());
		String pollingDistrictName = formatNameToFirstLetterUppercaseTheRestLowercase(contentImportFile.substring(16).trim());
		

		existingPollingDistrictMvAreas.remove(pollingDistrictMvAreaFromDb);
		if (isNotKretsNull(pollingDistrictId) && isPollingDistrictNameChanged(pollingDistrictMvAreaFromDb.getPollingDistrictName(), pollingDistrictName)) {
			try {
				PollingDistrict pollingDistrictToBeUpdated = pollingDistrictMvAreaFromDb.getPollingDistrict();
				pollingDistrictToBeUpdated.setName(pollingDistrictName);
				pollingDistrictRepository.update(userData, pollingDistrictToBeUpdated);
				PollingDistrictAuditEvent pollingDistrictAuditEvent = new PollingDistrictAuditEvent(userData, pollingDistrictToBeUpdated,
						AuditEventTypes.ImportDistrictsChangesUpdate, Outcome.Success, null);
				addAuditEvent(pollingDistrictAuditEvent);
				logFileUpdate.append(
						updateMsg("UPDATED AT LINE " + line, municipalityId, pollingDistrictId,
								pollingDistrictMvAreaFromDb.getPollingDistrictName(), pollingDistrictName))
						.append(System.lineSeparator());
			} catch (EvoteException | PersistenceException | ConstraintViolationException updateException) {
				errorAtLine(line, updateException);
				logFileUpdateFailed.append(
						updateMsg("UPDATE FAILED AT LINE " + line, municipalityId, pollingDistrictId,
								pollingDistrictMvAreaFromDb.getPollingDistrictName(), pollingDistrictName))
						.append(
								System.lineSeparator());
			}
		}
	}

	private boolean isNotKretsNull(String pollingDistrictId) {
		return !"0000".equals(pollingDistrictId.trim());
	}

	private void deleteAllDistrictsNotInImportFile(UserData userData, StringBuilder logFileDelete, StringBuilder logFileDeleteFailed,
			Collection<MvArea> existingPollingDistrictMvAreas) {
		for (MvArea pollingDistrictToBeDeleted : existingPollingDistrictMvAreas) {
			if (isPollingDistrictOrdinary(pollingDistrictToBeDeleted)) {
				if (voterRepository.hasVoters(pollingDistrictToBeDeleted.getPk())) {
					logFileDelete.append(
							importLineMsg("DELETE FAILED (DISTRICT HAS VOTERS)", pollingDistrictToBeDeleted.getMunicipalityId(),
									pollingDistrictToBeDeleted.getPollingDistrictId(),
									pollingDistrictToBeDeleted.getPollingDistrictName()))
							.append(System.lineSeparator());
				} else {
					try {
						pollingDistrictRepository.delete(userData, pollingDistrictToBeDeleted.getPollingDistrict().getPk());
						PollingDistrictAuditEvent pollingDistrictAuditEvent = new PollingDistrictAuditEvent(userData,
								pollingDistrictToBeDeleted.getPollingDistrict(), AuditEventTypes.ImportDistrictsChangesDelete, Outcome.Success, null);
						addAuditEvent(pollingDistrictAuditEvent);
						logFileDelete.append(
								importLineMsg("DELETED", pollingDistrictToBeDeleted.getMunicipalityId(), pollingDistrictToBeDeleted.getPollingDistrictId(),
										pollingDistrictToBeDeleted.getPollingDistrictName()))
								.append(System.lineSeparator());
					} catch (EvoteException | PersistenceException | ConstraintViolationException exception) {
						logFileDeleteFailed.append(
								importLineMsg("DELETION FAILED", pollingDistrictToBeDeleted.getMunicipalityId(),
										pollingDistrictToBeDeleted.getPollingDistrictId(),
										pollingDistrictToBeDeleted.getPollingDistrictName()))
								.append(System.lineSeparator());
					}
				}
			}
		}
	}

	private boolean isPollingDistrictOrdinary(MvArea pollingDistrictToBeDeleted) {
		return isNotParentDistrict(pollingDistrictToBeDeleted)
				&& !pollingDistrictToBeDeleted.getPollingDistrict().isTechnicalPollingDistrict();
	}

	private List<List<String>> getImportFileRows(byte[] contents) throws IOException {
		// Check if there actually is data to import
		if (contents == null || contents.length == 0) {
			emptyFileError();
		}
		List<List<String>> rows = CSVUtil.getRowsFromFile(new ByteArrayInputStream(contents), 0, ";", EvoteConstants.CHARACTER_SET_ISO);
		if (rows.isEmpty()) {
			emptyFileError();
		}
		return rows;
	}

	private PollingDistrict newPollingDistrict(String id, String name, Borough borough) {
		return new PollingDistrict(id, name, borough);
	}

	private String formatNameToFirstLetterUppercaseTheRestLowercase(String nameToFormat) {
		String name = nameToFormat.toLowerCase();
		name = setFirstLetterAfterSplitToUpperCase(name, SPACE);
		name = setFirstLetterAfterSplitToUpperCase(name, "-");
		name = setFirstLetterAfterSplitToUpperCase(name, "/");
		return name;
	}

	private String setFirstLetterAfterSplitToUpperCase(String name, String split) {
		StringBuilder formattedName = new StringBuilder();
		String[] partialNames = name.split(split);
		for (int i = 0; i < partialNames.length; i++) {
			String partialName = partialNames[i];
			if (split.equals(SPACE) && isPartialNameNotProperName(partialName)) {
				formattedName.append(partialName);
			} else {
				if (partialName.length() > 0) {
					formattedName.append(partialName.substring(0, 1).toUpperCase());
					if (partialName.length() > 1) {
						formattedName.append(partialName.substring(1));
					}
				}
			}
			if ((i + 1) != partialNames.length) {
				formattedName.append(split);
			}
		}
		return formattedName.toString();
	}

	private boolean isHeadingOfImportFile(String rowContent) {
		
		return rowContent.length() > 4 && !rowContent.trim().substring(0, 4).matches("[0-9]{4}");
		
	}

	private String retrieveAreaPath(ElectionEvent electionEvent, String rowContent) {

		StringBuilder areaPath = new StringBuilder();
		areaPath.append(electionEvent.getId());
		areaPath.append(DOT).append(COUNTRY_ID);
		if (rowContent != null) {
			
			String municipalityId = rowContent.substring(0, 4);
			areaPath.append(DOT).append(municipalityId.substring(0, 2));
			areaPath.append(DOT).append(municipalityId);
			String pollingDistrictId = retrievePollingDistrictId(rowContent.substring(5, 15).trim());
			
			areaPath.append(DOT).append(retrieveBoroughId(municipalityId, pollingDistrictId));
			areaPath.append(DOT).append(pollingDistrictId);
		}
		return areaPath.toString();
	}

	private String retrieveBoroughId(String municipalityId, String pollingDistrictId) {
		return municipalityId + pollingDistrictId.substring(0, 2);
	}

	private String retrievePollingDistrictId(String valgkrets) {
		
		while (valgkrets.length() < 4) {
			valgkrets = "0" + valgkrets;
		}
		
		return valgkrets;
	}

	private boolean isPollingDistrictNameChanged(String pollingDistrictNameFromDB, String pollingDistrictNameFromImportFile) {
		return !pollingDistrictNameFromDB.equalsIgnoreCase(pollingDistrictNameFromImportFile);
	}

	private boolean isPartialNameNotProperName(String paritalName) {
		return notProperName.contains(paritalName);
	}

	private void errorAtLine(int line, Exception e) {
		throw new EvoteException(new UserMessage("@area.import.error_line", line, e.getMessage()));
	}

	private void emptyFileError() {
		throw new EvoteException("@area.import.error_empty");
	}

	private String updateMsg(String action, String municipalityId, String pollingDistrictId, String pollingDistrictName, String newPollingDistrictName) {
		return importLineMsg(action, municipalityId, pollingDistrictId, pollingDistrictName) + " WITH NEW NAME "
				+ newPollingDistrictName;
	}

	private String importLineMsg(String action, String municipalityId, String pollingDistrictId, String pollingDistrictName) {
		return action + COLON_SPACE + municipalityId + SPACE + pollingDistrictId + SPACE + pollingDistrictName;
	}

	private static Set<String> notProperName = new HashSet<>(Arrays.asList("bibl.", "bibliotek", "bibliotehka", "biire", "folkebibliotek", "festivitetsh.",
			"festivitetshus",
			"gymnasium", "gymnas", "handelsgymnas", "høyskole", "høgskule", "idrettshus",
			"katedralskole", "kino", "krets", "krins", "m", "menighet", "meinigheit", "menighetshus", "og", "rådhus", "sk.idrettsh.", "skole", "skuvla",
			"samfunnshus",
			"ungdomsskole", "v", "válgabiire",
			"videregående", "vg", "valgk", "valgkr", "valgkrets", "valkrins"));

	public void setElectionEventRepository(ElectionEventRepository electionEventRepository) {
		this.electionEventRepository = electionEventRepository;
	}

	public void setMvAreaRepository(MvAreaRepository mvAreaRepository) {
		this.mvAreaRepository = mvAreaRepository;
	}

	public void setPollingDistrictRepository(PollingDistrictRepository pollingDistrictRepository) {
		this.pollingDistrictRepository = pollingDistrictRepository;
	}

	public void setPollingPlaceRepository(PollingPlaceRepository pollingPlaceRepository) {
		this.pollingPlaceRepository = pollingPlaceRepository;
	}

	public void setVoterRepository(VoterRepository voterRepository) {
		this.voterRepository = voterRepository;
	}
}
