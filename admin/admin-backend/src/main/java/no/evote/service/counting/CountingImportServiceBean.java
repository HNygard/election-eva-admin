package no.evote.service.counting;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.VoteCountStatusEnum;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.model.BinaryData;
import no.evote.model.views.ContestRelArea;
import no.evote.security.UserData;
import no.evote.service.CryptoServiceBean;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.auditevents.counting.IgnoredWriteInCandidateVoteAuditEvent;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoteCategoryRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.util.XMLUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static no.evote.constants.VoteCountStatusEnum.APPROVED;
import static no.evote.constants.VoteCountStatusEnum.TO_SETTLEMENT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.configuration.domain.model.ElectionType.TYPE_PROPORTIONAL_REPRESENTATION;
import static no.valg.eva.admin.configuration.domain.model.ElectionType.TYPE_REFERENDUM;
import static no.valg.eva.admin.counting.domain.auditevents.ImportUploadedCountAuditEvent.saveVoteCountAuditDetails;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.REJECTED;
import static no.valg.eva.admin.util.TidtakingUtil.taTiden;
import static no.valg.eva.admin.util.XMLUtil.validateAgainstSchema;
import static no.valg.eva.admin.util.XMLUtil.value;

public class CountingImportServiceBean {
	private static final String RENUMBER = "renumber";
	private static final String STRIKEOUT = "strikeout";
	private static final String PERSONAL = "personal";
	private static final String WRITEIN = "writein";
	private static final String SHORT_CODE = "ShortCode";
	private static final String ID = "Id";
	private static final String AFFILIATION_IDENTIFIER = "AffiliationIdentifier";
	private static final Logger LOGGER = Logger.getLogger(CountingImportServiceBean.class);

	@Inject
	private CryptoServiceBean cryptoService;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoteCountCategoryRepository voteCountCategoryRepository;
	@Inject
	private ContestReportRepository contestReportRepository;
	@Inject
	private ContestRelAreaRepository contestRelAreaRepository;
	@Inject
	private LegacyCountingServiceBean legacyCountingService;
	@Inject
	private BinaryDataRepository binaryDataRepository;
	@Inject
	private BallotRepository ballotRepository;
	@Inject
	private BallotRejectionRepository ballotRejectionRepository;
	@Inject
	private ContestRepository contestRepository;
	@Inject
	private ElectionRepository electionRepository;
	@Inject
	private ElectionGroupRepository electionGroupRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private CountingCodeValueRepository countingCodeValueRepository;
	@Inject
	private VoteCategoryRepository voteCategoryRepository;
	@Inject
	private CandidateRepository candidateRepository;
	@Inject
	@SuppressWarnings("unused")
	private VoteCountService voteCountService;
	@Inject
	private AuditLogServiceBean auditLogService;
	@Inject
	private AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService;
	@PersistenceContext(unitName = "evotePU")
	private EntityManager em;

	private Map<String, VoteCategory> voteCategories = null;

	/**
	 * Get a child element, raise error message if it doesn't exist.
	 */
	public static Element child(Element parent, String childName, Namespace ns) {
		return child(parent, childName, ns, false);
	}

	/**
	 * Get a child element, raise error message if it doesn't exist.
	 */
	public static Element child(Element parent, String childName, Namespace ns, boolean allowNull) {
		Element child = XMLUtil.child(parent, childName, ns, null, true);
		if (!allowNull && child == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0310_XML_CHILD_ELEMENT_NOT_FOUND, null, childName, parent.getName());
		}
		return child;
	}

	/**
	 * Get an attribute value, raise an exception if it doesn't exist.
	 */
	public static String attribute(final Element element, final String attributeId) {
		String value = element.getAttributeValue(attributeId);
		if (value == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0311_XML_ATTRIBUTE_NOT_FOUND, null, attributeId, element.getName());
		}
		return value;
	}

	@PostConstruct
	public void init() {
		voteCategories = new HashMap<String, VoteCategory>() {
			{
				put(RENUMBER, findVoteCategoryById(RENUMBER));
				put(STRIKEOUT, findVoteCategoryById(STRIKEOUT));
				put(PERSONAL, findVoteCategoryById(PERSONAL));
				put(WRITEIN, findVoteCategoryById(WRITEIN));
			}
		};
	}

	public VoteCategory findVoteCategoryById(String id) {
		if (voteCategories != null && voteCategories.containsKey(id)) {
			return voteCategories.get(id);
		}
		VoteCategory voteCategory = voteCategoryRepository.findVoteCategoryById(id);
		if (voteCategories != null) {
			voteCategories.put(id, voteCategory);
		}
		return voteCategory;
	}

	/**
	 * Validates a count zip, checks signature, zip structure, election data and reporting unit. Throws EvoteNoRollbackException if anything goes wrong.
	 * EvoteNoRollbackException don't cause rollback in the calling method.
	 */
	public void validateCountEmlZip(UserData userData, File zipFile) {
		LOGGER.info("validateCountEmlZip " + zipFile.getName());

		File resultsZipFile;
		Map<String, File> innerFiles;
		File tempDirectory = IOUtil.createTemporaryDirectory();
		try {
			resultsZipFile = unZipAndValidate(userData, tempDirectory, zipFile);
			innerFiles = IOUtil.unZip(tempDirectory, resultsZipFile);

			getAndValidate(innerFiles, true, true);
			getAndValidate(innerFiles, false, true);

			importEml(userData, innerFiles, false);
		} catch (EvoteException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new EvoteException(ErrorCode.ERROR_CODE_0304_UNEXPECTED_COUNTING_IMPORT_ERROR, e);
		} finally {
			IOUtil.deleteDirectory(tempDirectory);
		}
	}

	/**
	 * Validates and import a count zip, checks signature and zip structure. Throws EvoteNoRollbackException if anything goes wrong. EvoteNoRollbackException
	 * don't cause rollback in the calling method.
	 */
	public void importCountEmlZip(UserData userData, File zipFile) {
		LOGGER.info("importCountEmlZip " + zipFile.getName());

		File resultsZipFile;
		Map<String, File> innerFiles;
		File tempDirectory = IOUtil.createTemporaryDirectory();
		try {
			resultsZipFile = unZipAndValidate(userData, tempDirectory, zipFile);
			innerFiles = IOUtil.unZip(tempDirectory, resultsZipFile);
			importEml(userData, innerFiles, true);
		} catch (EvoteException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} catch (NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
			throw new EvoteException("Missing content in EML.", e);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new EvoteException(e.getMessage(), e);
		} finally {
			IOUtil.deleteDirectory(tempDirectory);
		}
	}

	/**
	 * Validate EML files against xsd's
	 */
	private Map<String, File> getAndValidate(Map<String, File> innerFiles, boolean count, boolean validate) throws IOException {
		String schema = count ? "OASIS-EML-v50-OS/510-count-v5-0.xsd" : "OASIS-EML-v50-OS/460-votes-v5-0.xsd";
		String fileName = count ? "Count-" : "Votes-";

		Map<String, File> files = getFilesByStartWith(innerFiles, fileName);
		for (File file : files.values()) {
			String fileAsString = Files.toString(file, Charsets.UTF_8);
			if (validate) {
				try {
					validateAgainstSchema(fileAsString, schema);
				} catch (SAXException | ParserConfigurationException | IOException e) {
					throw new EvoteException(ErrorCode.ERROR_CODE_0303_XML_VALIDATE_FAILED, e, file.getName());
				}
			}
		}
		return files;
	}

	private Map<String, File> getFilesByStartWith(Map<String, File> allFiles, String start) {
		Map<String, File> files = new HashMap<>();
		for (Entry<String, File> entry : allFiles.entrySet()) {
			if (!entry.getValue().isDirectory() && entry.getKey().startsWith(start)) {
				files.put(entry.getKey(), entry.getValue());
			}
		}
		return files;
	}

	private Map<String, File> getFilesByExtension(Map<String, File> allFiles, String extension) {
		Map<String, File> files = new HashMap<>();
		for (Entry<String, File> entry : allFiles.entrySet()) {
			if (!entry.getValue().isDirectory() && entry.getKey().endsWith(extension)) {
				files.put(entry.getKey(), entry.getValue());
			}
		}
		return files;
	}

	/**
	 * Unzips the zipfile and validates it content against the pem file in the zip
	 */
	private File unZipAndValidate(UserData userData, File tempDirectory, File zipFile) throws IOException {
		Map<String, File> files = IOUtil.unZip(tempDirectory, zipFile);

		File resultsZipFile = files.get("Counts.zip");
		boolean skipSignatureCheck = EvoteProperties.getBooleanProperty(EvoteProperties.NO_VALG_EVA_ADMIN_COUNTING_UPLOAD_SKIP_SIGNATURE_CHECK, false);
		if (!skipSignatureCheck) {
			File resultsZipFileSignature = files.get("Counts.zip.signature");
			if (resultsZipFileSignature == null) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0301_NO_PEM);
			}
			verifySignature(userData, resultsZipFile, resultsZipFileSignature);

			LOGGER.info("Deleted " + resultsZipFileSignature.getName() + " " + resultsZipFileSignature.delete());
		} else {
			LOGGER.error("WARNING: Signature check was skipped for file uploaded by operator " + userData.getOperator().getId());
		}
		return resultsZipFile;
	}

	private void verifySignature(UserData userData, File zipFile, File signatureFile) throws IOException {
		byte[] countFileBytes = IOUtil.getBytes(zipFile);
		byte[] signatureFileBytes = IOUtil.getBytes(signatureFile);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, userData.getOperator());
	}

	/**
	 * Starts the import job. It first import one count eml and then 0 to many belonging votes eml
	 */
	private void importEml(UserData userData, Map<String, File> innerFiles, boolean importData) throws IOException, JDOMException {
		boolean validate = !importData; // We've already validated the files when we're importing the data
		Map<String, File> tiffFiles = getFilesByExtension(innerFiles, ".tiff");
		Map<String, File> countFiles = getAndValidate(innerFiles, true, validate);
		Map<String, File> allVotesFiles = getAndValidate(innerFiles, false, validate);
		Map<String, List<File>> votesFilesMap = toTransactionVotesFilesMap(allVotesFiles);

		taTiden(LOGGER, "Import EML", () -> {
			for (File countFile : countFiles.values()) {
				importCountFile(userData, importData, tiffFiles, votesFilesMap, countFile);
			}
		});
	}

	private void importCountFile(UserData userData, boolean importData, Map<String, File> tiffFiles, Map<String, List<File>> votesFilesMap, File countFile) {
		try {
			String transactionId = getTransactionId(countFile);
			LOGGER.info("import " + countFile.getName() + ", transaction id: " + transactionId);

			List<File> votesFiles = votesFilesMap.get(transactionId);

			// må ta tilbake en urnetelling som bare skal auditlogges
			// i tillegg til den andre tellinga som er foreløpig eller endelig
			Tellinger tellinger = parseCountFile(userData, countFile, importData);
			if (tellinger != null) {
				VoteCount voteCount = tellinger.getForelopigEllerEndeligTelling();
				parseAndImportVotes(userData, importData, tiffFiles, votesFiles, voteCount);
				addBallotsWithoutWoteCount(voteCount);
				saveAuditDetailsIfVoteCountExists(tellinger.getUrnetelling());
				saveAuditDetailsIfVoteCountExists(voteCount);
			}
		} catch (IOException | JDOMException e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0305_FAILED_TO_PARSE_COUNT_FILE, e, countFile.getName());
		}
	}

	private void parseAndImportVotes(UserData userData, boolean importData, Map<String, File> tiffFiles, List<File> votesFiles, VoteCount voteCount)
			throws IOException, JDOMException {
		if (importData && votesFiles != null) {
			for (File votes : votesFiles) {
				LOGGER.info("import " + votes.getName());
				parseVotes(userData, voteCount.getBallotCountList(), votes, tiffFiles);
			}
		}
	}

	private void addBallotsWithoutWoteCount(VoteCount voteCount) {
		List<Ballot> ballotsWithoutVotes = ballotRepository.findBallotsWithoutVotes(voteCount.getId());
		ballotsWithoutVotes.forEach(ballot -> voteCount.addNewBallotCount(ballot, 0, 0));
	}

	private void saveAuditDetailsIfVoteCountExists(VoteCount voteCount) {
		if (voteCount == null) {
			return;
		}
		saveVoteCountAuditDetails(voteCount, splitCount(voteCount), includeCastBallots(voteCount));
	}

	private boolean splitCount(VoteCount voteCount) {
		return FINAL.getId().equals(voteCount.getCountQualifierId());
	}

	private boolean includeCastBallots(VoteCount voteCount) {
		return FINAL.getId().equals(voteCount.getCountQualifierId());
	}

	/**
	 * Create a mapping from transaction ID to votes file
	 */
	private Map<String, List<File>> toTransactionVotesFilesMap(Map<String, File> allVotesFiles) throws JDOMException, IOException {
		Map<String, List<File>> votesFilesMap = new HashMap<>();

		for (File votesFile : allVotesFiles.values()) {
			String transactionId = getTransactionId(votesFile);

			LOGGER.debug("transactionId: " + transactionId + ", " + votesFile.getName());
			if (!votesFilesMap.containsKey(transactionId)) {
				votesFilesMap.put(transactionId, new ArrayList<>());
			}
			votesFilesMap.get(transactionId).add(votesFile);
		}

		return votesFilesMap;
	}

	/**
	 * Find the TransactionId attribute in an eml file
	 */
	private String getTransactionId(File file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();

		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			Document document = builder.build(fileInputStream);
			Element root = document.getRootElement();
			Namespace ns = root.getNamespace();

			Element transactionIdEl = child(root, "TransactionId", ns);
			return value(transactionIdEl);
		}
	}

	/**
	 * Parse Count file for election and area information
	 */
	private Tellinger parseCountFile(UserData userData, File countFile, boolean importData) throws IOException, JDOMException {
		LOGGER.info("parseCounts " + countFile.getName());
		SAXBuilder builder = new SAXBuilder();

		try (InputStream countsInputStream = new FileInputStream(countFile)) {
			Document document = builder.build(countsInputStream);
			Element root = document.getRootElement();
			Namespace ns = root.getNamespace();

			Element countEl = child(root, "Count", ns);

			Element eventIdentifierEl = child(countEl, "EventIdentifier", ns);
			ElectionEvent electionEvent = verifyAndGetElectionEvent(userData, eventIdentifierEl.getAttributeValue(ID));

			Element electionEl = child(countEl, "Election", ns);
			Element electionIdentifier = child(electionEl, "ElectionIdentifier", ns);

			ElectionGroup electionGroup = verifyAndGetElectionGroup(electionEvent, electionIdentifier.getChild("ElectionGroup", ns));

			Election election = verifyAndGetElection(electionEvent, electionIdentifier, electionGroup);

			String mvAreaPath = buildMvAreaPath(electionEvent, ns, eventIdentifierEl);
			MvArea mvAreaForReporting = mvAreaRepository.findSingleByPath(AreaPath.from(mvAreaPath));
			if (mvAreaForReporting == null || (mvAreaForReporting.getPollingDistrict() == null && mvAreaForReporting.getBorough() == null)) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0309_UNKNOWN_POLLING_DISTRICT, null, mvAreaPath);
			}

			LOGGER.info(String.format("Area path: %s Election path: %s.%s.%s", mvAreaPath, electionEvent.getId(), electionGroup.getId(), election.getId()));

			// Election is Calculated mandate and candidate distribution or referendum
			if (election.getElectionType().getId().equals(TYPE_PROPORTIONAL_REPRESENTATION) || election.getElectionType().getId().equals(TYPE_REFERENDUM)) {
				return contestCount(userData, electionEl, ns, election, mvAreaForReporting, importData);
			} else {
				throw new NotImplementedException("Ikke en støttet valgtype: " + election.getElectionType().getId());
			}
		}
	}

	private String buildMvAreaPath(ElectionEvent electionEvent, Namespace ns, Element eventIdentifierEl) {
		Element eventQualifierEl = eventIdentifierEl.getChild("EventQualifier", ns);
		String eventQualifierId = eventQualifierEl.getAttributeValue(ID);
		return electionEvent.getId() + "." + eventQualifierId;
	}

	private Election verifyAndGetElection(ElectionEvent electionEvent, Element electionIdentifier, ElectionGroup electionGroup) {
		String electionIdentifierId = electionIdentifier.getAttributeValue(ID);
		Election election = electionRepository.findElectionByElectionGroupAndId(electionGroup.getPk(), electionIdentifierId);
		if (election == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0307_UNKNOWN_ELECTION, null, electionIdentifierId, electionEvent.getId(), electionGroup.getId());
		}
		return election;
	}

	private ElectionGroup verifyAndGetElectionGroup(ElectionEvent electionEvent, Element electionGroupEl) {
		String electionGroupId = electionGroupEl.getAttributeValue(ID);

		ElectionGroup electionGroup = electionGroupRepository.findElectionGroupById(electionEvent.getPk(), electionGroupId);
		if (electionGroup == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0308_UNKNOWN_ELECTION_GROUP, null, electionGroupId, electionEvent.getId());
		}
		return electionGroup;
	}

	/**
	 * Verify that the election event is the same as the one the user is logged in on, it shouldn't be able to import for other events
	 */
	private ElectionEvent verifyAndGetElectionEvent(UserData userData, String electionEventId) {
		ElectionEvent electionEvent = electionEventRepository.findById(electionEventId);
		String userElectionEventId = electionEventRepository.findByPk(userData.getElectionEventPk()).getId();
		if (!userElectionEventId.equals(electionEventId)) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0306_WRONG_ELECTION_EVENT, null, electionEventId, userElectionEventId);
		}

		return electionEvent;
	}

	/**
	 * Parse Count file, validates and get reporting unit and category. Starts the import if importData parameter is true
	 */
	private Tellinger contestCount(
			UserData userData, Element electionEl, Namespace ns, Election election, MvArea mvAreaForReporting, boolean importData) {
		LOGGER.info("contestCount: " + election.getId() + " area: " + mvAreaForReporting.getAreaPath());

		ElectionEvent electionEvent = election.getElectionGroup().getElectionEvent();

		Element contests = child(electionEl, "Contests", ns);
		Element contestEl = child(contests, "Contest", ns);

		boolean shouldBeFinal = shouldBeFinal(ns, contestEl);

		Element reportingUnitVotes = child(contestEl, "ReportingUnitVotes", ns);

		VoteCountCategory voteCountCategory = getVoteCountCategory(ns, reportingUnitVotes);

		Element totalCounted = child(reportingUnitVotes, "TotalCounted", ns);
		String totalCount = value(totalCounted);

		Contest contest = verifyAndGetContest(ns, election, contestEl);
		MvElection mvElectionForContest = mvElectionRepository.findByContest(contest);

		CountingMode countingMode = voteCountService.countingMode(CountCategory.valueOf(voteCountCategory.getId()), mvAreaForReporting.getMunicipality(),
				mvElectionForContest);
		if (countingMode == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0312_UNABLE_TO_FIND_REPORT_COUNT_CATEGORY, null);
		}

		ReportingUnit valgEllerFylkesvalgstyret = verifyAndGetReportingUnit(userData, reportingUnitVotes, ns, electionEvent);
		ReportingUnit reportingUnit = utledReportingUnit(mvAreaForReporting, electionEvent, voteCountCategory, contest, countingMode,
				valgEllerFylkesvalgstyret);

		ContestReport contestReport = legacyCountingService.makeContestReport(contest, reportingUnit);
		contestReport = contestReportRepository.update(userData, contestReport);

		boolean couldBePreliminary = couldBePreliminary(reportingUnit, countingMode);
		legacyCountingService.validateCountingStatus(reportingUnit, mvElectionForContest.getPk(), voteCountCategory, contest, countingMode);
		validateVoteCount(contestReport, mvAreaForReporting, voteCountCategory, couldBePreliminary, shouldBeFinal);

		if (mvAreaForReporting.getPollingDistrict() != null) {
			legacyCountingService.validateSelectedPollingDistrictAndCategory(userData, mvAreaForReporting.getPollingDistrict(),
					mvAreaForReporting.getMunicipality(),
					voteCountCategory, countingMode, reportingUnit.getMvArea(), mvElectionForContest);
		}

		isAntallStemmesedlerLagtTilSideLagret(contestReport, mvAreaForReporting);

		if (importData) {
			return startImport(ns, mvAreaForReporting, reportingUnitVotes, voteCountCategory, couldBePreliminary, contestReport, totalCount);
		}
		return null;
	}

	private void isAntallStemmesedlerLagtTilSideLagret(ContestReport contestReport, MvArea mvAreaForReporting) {
		ValgdistriktSti valgdistriktSti = contestReport.getContest().valgdistriktSti();
		KommuneSti kommuneSti = mvAreaForReporting.getMunicipality().kommuneSti();
		if (!antallStemmesedlerLagtTilSideDomainService.isAntallStemmesedlerLagtTilSideLagret(valgdistriktSti.valggruppeSti(), kommuneSti)) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0331_ANTALL_LAGT_TIL_SIDE_IKKE_LAGRET);
		}
	}

	/**
	 * Finner reporting unit / styre basert på om Skanning har indikert at det er Fylke som teller eller om det er kommune/krets som teller Dersom det angis at
	 * kommunen teller og det er lokalt fordelt på krets uten at det finnes noen godkjente foreløpige tellinger, så er det stemmestyret som teller.
	 */
	private ReportingUnit utledReportingUnit(MvArea mvAreaForReporting, ElectionEvent electionEvent, VoteCountCategory voteCountCategory, Contest contest,
			CountingMode countingMode, ReportingUnit valgEllerFylkesvalgstyret) {
		if (valgEllerFylkesvalgstyret.reportingUnitTypeId() == FYLKESVALGSTYRET || countingMode != BY_POLLING_DISTRICT) {
			return valgEllerFylkesvalgstyret;
		}
		// finnes det en godkjent foreløpig telling så er tellingen som kommer endelig og da er det valgstyret...
		ReportingUnit stemmestyret = reportingUnitRepository.byAreaPathElectionPathAndType(mvAreaForReporting.areaPath(), electionEvent.electionPath(),
				ReportingUnitTypeId.STEMMESTYRET);
		ContestReport stemmestyretsmotebok = contestReportRepository.findByReportingUnitContest(stemmestyret.getPk(), contest.getPk());
		if (stemmestyretsmotebok == null) {
			return stemmestyret;
		}
		long godkjenteForelopigeTellinger = stemmestyretsmotebok.uniqueKindCount(forelopigTelling(), voteCountCategory, mvAreaForReporting, APPROVED);
		if (godkjenteForelopigeTellinger > 0) {
			return valgEllerFylkesvalgstyret;
		} else {
			return stemmestyret;
		}
	}

	private CountQualifier forelopigTelling() {
		return countingCodeValueRepository.findCountQualifierById(PRELIMINARY.getId());
	}

	private ReportingUnit verifyAndGetReportingUnit(UserData userData, Element reportingUnitVotes, Namespace ns, ElectionEvent electionEvent) {
		Element reportingUnitIndicator = child(reportingUnitVotes, "ReportingUnitIndicator", ns);
		String reportingUnitIndicatorId = attribute(reportingUnitIndicator, ID);

		return findReportingUnit(userData, electionEvent, reportingUnitIndicatorId);
	}

	private Contest verifyAndGetContest(Namespace ns, Election election, Element contestEl) {
		Element contestIdentifier = child(contestEl, "ContestIdentifier", ns);
		String contestIdentifierId = attribute(contestIdentifier, ID);
		Contest contest = contestRepository.findContestById(election.getPk(), contestIdentifierId);

		if (contest == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0313_UNKNOWN_CONTEST, null, contestIdentifierId, election.getId());
		}

		return contest;
	}

	private ReportingUnit findReportingUnit(UserData userData, ElectionEvent electionEvent, String reportingUnitIdentifierId) {
		ReportingUnit reportingUnit = findReportingUnitByPath(electionEvent, reportingUnitIdentifierId);
		isReportingUnitValid(userData, reportingUnit);
		return reportingUnit;
	}

	/**
	 * Finds a reporting unit through paths
	 */
	private ReportingUnit findReportingUnitByPath(ElectionEvent electionEvent, String reportingUnitIdentifierId) {
		String[] mv = reportingUnitIdentifierId.split("-", -1);
		StringBuilder mvElectionPath = new StringBuilder(electionEvent.getId());
		if (!"".equals(mv[0])) {
			mvElectionPath.append(".");
			mvElectionPath.append(mv[0]);
		}

		StringBuilder mvAreaPath = new StringBuilder(electionEvent.getId());
		if (!"".equals(mv[1])) {
			mvAreaPath.append(".");
			mvAreaPath.append(mv[1]);
		}

		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from(mvElectionPath.toString()).tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from(mvAreaPath.toString()));
		ReportingUnit reportingUnit;
		try {
			reportingUnit = reportingUnitRepository.findByMvElectionMvArea(mvElection.getPk(), mvArea.getPk());
		} catch (NoResultException e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0314_UNKNOWN_REPORTING_UNIT, null, reportingUnitIdentifierId, electionEvent.getId());
		} catch (NonUniqueResultException e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0315_FOUND_MORE_THAN_ONE_REPORTING_UNIT, e);
		} catch (Exception e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0304_UNEXPECTED_COUNTING_IMPORT_ERROR, e);
		}

		return reportingUnit;
	}

	/**
	 * Start the import
	 */
	private Tellinger startImport(Namespace ns, MvArea mvAreaForReporting, Element reportingUnitVotes,
			VoteCountCategory voteCountCategory, boolean couldBePreliminary, ContestReport contestReport, String totalCount) {

		int rejectedVotesValue = 0;
		List<Element> rejectedVotes = reportingUnitVotes.getChildren("RejectedVotes", ns);
		for (Element rejectedVote : rejectedVotes) {
			rejectedVotesValue += Integer.parseInt(rejectedVote.getValue());
		}

		int totalCountValue = Integer.parseInt(totalCount);
		int ordinaryVotes = totalCountValue - rejectedVotesValue;

		Tellinger tellinger = getVoteCount(contestReport, mvAreaForReporting, voteCountCategory, ordinaryVotes, rejectedVotesValue, couldBePreliminary);
		VoteCount voteCount = tellinger.getForelopigEllerEndeligTelling();
		voteCount.clearBallotCountSet();
		VoteCount urnetelling = tellinger.getUrnetelling();
		if (urnetelling != null) {
			urnetelling.clearBallotCountSet();
		}

		// flush entity manager to make sure that existing ballot counts are removed
		em.flush();

		if (totalCountValue == 0) {
			LOGGER.info("No valid votes, so nothing to import");
			voteCount.setInfoText("@count.info.zero_votes");
			return null;
		} else {
			if (ordinaryVotes > 0) { // Fixes issue #8748
				importSelectionCount(contestReport, ns, reportingUnitVotes, voteCount, urnetelling);
			}

			for (Element rejectedVote : rejectedVotes) {
				int rejectedVotesValue2 = Integer.parseInt(rejectedVote.getValue());

				String reasonCode = attribute(rejectedVote, "ReasonCode");
				BallotRejection ballotRejection = ballotRejectionRepository.findBallotRejectionById(reasonCode);

				validateReasonCodeForRejectedVote(voteCountCategory, ballotRejection);

				voteCount.addNewRejectedBallotCount(ballotRejection, rejectedVotesValue2);
			}
			return tellinger;
		}
	}

	private void validateReasonCodeForRejectedVote(VoteCountCategory voteCountCategory, BallotRejection ballotRejection) {
		if (ballotRejection.isEarlyVoting() == voteCountCategory.isEarlyVoting()) {
			return;
		}
		if (!ballotRejection.isEarlyVoting()) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0326_INVALID_REASON_CODE_FOR_REJECTED_VOTE_FOR_EARLY_VOTING, null, ballotRejection.getId());
		}
		throw new EvoteException(ErrorCode.ERROR_CODE_0327_INVALID_REASON_CODE_FOR_REJECTED_VOTE_FOR_ELECTION_DAY_VOTING, null, ballotRejection.getId());
	}

	private VoteCountCategory getVoteCountCategory(Namespace ns, Element reportingUnitVotes) {
		Element countMetric = child(reportingUnitVotes, "CountMetric", ns);
		String categoryId = attribute(countMetric, ID);
		VoteCountCategory vcc = voteCountCategoryRepository.findById(categoryId);

		if (vcc == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0316_UNKNOWN_VOTE_COUNT_CATEGORY, null, categoryId);
		}

		return vcc;
	}

	/**
	 * Validates that the a reporting unit is included in the reporting units that the operator role has access to.
	 */
	private void isReportingUnitValid(UserData userData, ReportingUnit reportingUnit) {
		OperatorRole operatorRole = userData.getOperatorRole();

		// Do not perform this test if the role is on top level i.e valghendelse_admin or counting_ecount
		if (!(operatorRole.getMvElection().getElectionLevel() == 0 && operatorRole.getMvArea().getAreaLevel() == 0)) {
			List<ContestRelArea> contestRelAreas = contestRelAreaRepository.findAllAllowed(operatorRole.getMvElection(), operatorRole.getMvArea());
			List<ReportingUnit> reportingUnits = new ArrayList<>();
			for (ContestRelArea contestRelArea : contestRelAreas) {
				reportingUnits.add(reportingUnitRepository.getReportingUnit(contestRelArea));
			}
			if (!reportingUnits.contains(reportingUnit)) {
				final String operatorRoleMvElection = operatorRole.getMvElection().toString();
				final String operatorRoleMvArea = operatorRole.getMvArea().toString();
				final String reportingUnitName = reportingUnit.getNameLine();
				throw new EvoteException(ErrorCode.ERROR_CODE_0317_MISMATCH_BETWEEN_REPORTING_UNITS, null, operatorRoleMvElection, operatorRoleMvArea,
						reportingUnitName);
			}
		}
	}

	/**
	 * Checks the configuration to see if a count could be preliminary
	 */
	private boolean couldBePreliminary(ReportingUnit reportingUnit, CountingMode countingMode) {
		return isPollingDistrictReportingUnitAndNotCentralPreliminaryCount(reportingUnit, countingMode)
				|| isMunicipalityReportingUnitAndCentralPreliminaryCount(reportingUnit, countingMode);

	}

	private boolean isMunicipalityReportingUnitAndCentralPreliminaryCount(ReportingUnit reportingUnit, CountingMode countingMode) {
		return reportingUnit.getMvArea().getAreaLevel() == AreaLevelEnum.MUNICIPALITY.getLevel() && countingMode.isCentralPreliminaryCount();
	}

	private boolean isPollingDistrictReportingUnitAndNotCentralPreliminaryCount(ReportingUnit reportingUnit, CountingMode countingMode) {
		return reportingUnit.getMvArea().getAreaLevel() == AreaLevelEnum.POLLING_DISTRICT.getLevel() && !countingMode.isCentralPreliminaryCount();
	}

	/**
	 * Get the final value from the count
	 */
	private boolean shouldBeFinal(Namespace ns, Element contestEl) {
		Element countQualifier = child(contestEl, "CountQualifier", ns);
		Element finalEl = child(countQualifier, "Final", ns);
		String finalValue = value(finalEl, true);
		return "yes".equals(finalValue);
	}

	/**
	 * Loop through the selections an import them
	 */
	private void importSelectionCount(ContestReport contestReport, Namespace ns, Element reportingUnitVotes, VoteCount voteCount, VoteCount urnetelling) {
		List<Element> selections = reportingUnitVotes.getChildren("Selection", ns);
		LOGGER.info("selectionCount: " + selections.size());
		for (Element selection : selections) {
			importBallotCount(contestReport, ns, voteCount, selection, urnetelling);
		}
	}

	/**
	 * Import a selection to a ballotCount
	 */
	private void importBallotCount(ContestReport contestReport, Namespace ns, VoteCount voteCount, Element selection, VoteCount urnetelling) {
		LOGGER.info("importBallotCount");
		Element ballotIdentifier;
		if (contestReport.getContest().getElection().getElectionType().getId().equals(TYPE_PROPORTIONAL_REPRESENTATION)) {
			ballotIdentifier = selection.getChild(AFFILIATION_IDENTIFIER, ns);
		} else {
			ballotIdentifier = selection.getChild("ReferendumOptionIdentifier", ns);
		}
		String ballotId = ballotIdentifier.getAttributeValue(ID);
		LOGGER.info("ballotId: " + ballotId);

		Ballot ballot = ballotRepository.findByContestAndId(contestReport.getContest().getPk(), ballotId);

		Element validVotesEl = selection.getChild("ValidVotes", ns);

		Element countMetric = selection.getChild("CountMetric", ns);
		int validVotes = Integer.parseInt(validVotesEl.getValue());
		int modified = 0;

		if (countMetric != null) {
			modified = Integer.parseInt(countMetric.getValue());
		}

		if (modified > 0 || validVotes > 0) {
			LOGGER.info("modified > 0 || validVotes > 0 (Modified:" + modified + " Valid:" + validVotes + ")");
			voteCount.addNewBallotCount(ballot, validVotes - modified, modified);
		}

		if (urnetelling != null && "BLANK".equals(ballotId)) {
			urnetelling.addNewBallotCount(ballot, validVotes, 0);
		}
	}

	/**
	 * Checks that EML is sync with the data in the database
	 */
	private void validateVoteCount(
			ContestReport contestReport, MvArea mvAreaForReporting, VoteCountCategory voteCountCategory, boolean couldBePreliminary, boolean shouldBeFinal) {

		CountQualifier countQualifierPreliminary = forelopigTelling();
		CountQualifier countQualifierFinal = endeligTelling();

		long preliminaryCounts = contestReport.uniqueKindCount(countQualifierPreliminary, voteCountCategory, mvAreaForReporting);
		long approvedPreliminaryCounts = contestReport.uniqueKindCount(countQualifierPreliminary, voteCountCategory, mvAreaForReporting, APPROVED);
		long approvedFinalCounts = contestReport.uniqueKindCount(countQualifierFinal, voteCountCategory, mvAreaForReporting, APPROVED);
		long finalCountsToSettlement = contestReport.uniqueKindCount(countQualifierFinal, voteCountCategory, mvAreaForReporting, TO_SETTLEMENT);

		final String infoString = buildErrorMsg(mvAreaForReporting, voteCountCategory);

		if (couldBePreliminary && ((preliminaryCounts == 0) || (approvedPreliminaryCounts == 0))) {
			if (shouldBeFinal) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0318_CANT_BE_FINAL, null, infoString);
			}
		} else {
			// Customized error message when local and pollingDistrict count.
			if (!shouldBeFinal) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0320_SHOULD_BE_FINAL);
			} else if (approvedFinalCounts > 0 || finalCountsToSettlement > 0) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0321_FINAL_ALREAD_APPROVED);
			}
		}
	}

	/**
	 * Builds an extra info string for the error message
	 */
	private String buildErrorMsg(MvArea countArea, VoteCountCategory voteCountCategory) {
		return voteCountCategory.getId() + " på området " + countArea.getAreaName() + ": ";
	}

	/**
	 * Get a voteCount, either a new one or a cleaned one that already exist depending on where in the counting cycle the polling district is
	 */
	private Tellinger getVoteCount(ContestReport contestReport, MvArea mvAreaForReporting, VoteCountCategory voteCountCategory,
			int ordinaryVotes, int rejectedVotes, boolean couldBePreliminary) {
		long preliminaryCounts = contestReport.uniqueKindCount(forelopigTelling(), voteCountCategory, mvAreaForReporting);
		long approvedPreliminaryCounts = contestReport.uniqueKindCount(forelopigTelling(), voteCountCategory, mvAreaForReporting, APPROVED);

		if (couldBePreliminary && preliminaryCounts == 0) {
			LOGGER.info("no preliminary counts");
			VoteCount urnetelling = null;
			if (lageUrnetelling(contestReport, mvAreaForReporting)) {
				urnetelling = createVoteCount(contestReport, mvAreaForReporting, voteCountCategory, urneTelling(), ordinaryVotes, rejectedVotes);
				contestReport.add(urnetelling);
			}
			VoteCount forelopigTelling = createVoteCount(contestReport, mvAreaForReporting, voteCountCategory, forelopigTelling(), ordinaryVotes, rejectedVotes);
			contestReport.add(forelopigTelling);
			return new Tellinger(urnetelling, forelopigTelling);
		}

		if (couldBePreliminary && approvedPreliminaryCounts == 0) {
			LOGGER.info("found not approved preliminary");
			Long mvAreaForReportingPk = mvAreaForReporting.getPk();
			CountCategory countCategory = kategori(voteCountCategory);
			VoteCount forelopigTelling = contestReport.findFirstVoteCountByMvAreaCountQualifierAndCategory(mvAreaForReportingPk, PRELIMINARY, countCategory);
			VoteCount urnetelling = contestReport.findFirstVoteCountByMvAreaCountQualifierAndCategory(mvAreaForReportingPk, PROTOCOL, countCategory);
			forelopigTelling = updateVoteCount(forelopigTelling, ordinaryVotes, rejectedVotes);
			if (urnetelling != null) {
				updateVoteCount(urnetelling, ordinaryVotes, rejectedVotes);
			}
			return new Tellinger(urnetelling, forelopigTelling);
		}

		// found approved preliminary make final
		LOGGER.info("found approved preliminary make final");
		VoteCount endeligTelling = createVoteCount(contestReport, mvAreaForReporting, voteCountCategory, endeligTelling(), ordinaryVotes, rejectedVotes);
		contestReport.add(endeligTelling);
		return new Tellinger(null, endeligTelling);
	}

	private CountCategory kategori(VoteCountCategory voteCountCategory) {
		return CountCategory.fromId(voteCountCategory.getId());
	}

	private CountQualifier endeligTelling() {
		return countingCodeValueRepository.findCountQualifierById(FINAL.getId());
	}

	private CountQualifier urneTelling() {
		return countingCodeValueRepository.findCountQualifierById(PROTOCOL.getId());
	}

	/**
	 * Lager en ekstra urnetelling når det er stemmestyret som teller (ved lokalt fordelt på krets). Uansett om det er XiM eller ikke, må bruker godkjenne
	 * tellingen og evt legge inn kryss - hvis det er tellekrets så skal vi ikke lage ekstra urnetelling
	 */
	private boolean lageUrnetelling(ContestReport contestReport, MvArea mvAreaForReporting) {
		return contestReport.getReportingUnit().isStemmestyret() && mvAreaForReporting.getPollingDistrict().isRegularPollingDistrict();
	}

	/**
	 * Parse a votes file and import the content
	 */
	private void parseVotes(UserData userData, List<BallotCount> ballotCounts, File votes, Map<String, File> tifFiles)
			throws IOException, JDOMException {
		LOGGER.info("parseVotes: " + votes.getName());
		VoteCount voteCount = ballotCounts.get(0).getVoteCount();

		SAXBuilder builder = new SAXBuilder();

		try (InputStream votesInputStream = new FileInputStream(votes)) {
			Document document = builder.build(votesInputStream);
			Element root = document.getRootElement();
			Namespace ns = root.getNamespace();

			Element votesEl = root.getChild("Votes", ns);

			ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
			Contest contest = voteCount.getContestReport().getContest();
			List<Element> castVotes = votesEl.getChildren("CastVote", ns);

			Comparator<Long> longComparator = (o1, o2) -> o1.compareTo(o2);

			Map<Long, BallotCount> ballotCountRejectionMap = new TreeMap<>(longComparator);
			Map<Pair<Long, String>, BallotCount> ballotCountMap = new TreeMap<>();
			createBallotCountMaps(ballotCounts, contest, ballotCountRejectionMap, ballotCountMap);

			for (Element castVote : castVotes) {
				Element election = castVote.getChild("Election", ns);
				Element contestEl = election.getChild("Contest", ns);
				makeCastVote(userData, electionEvent, contest, ns, castVote, ballotCountMap, ballotCountRejectionMap, contestEl, tifFiles);
			}
		}
	}

	private void createBallotCountMaps(
			List<BallotCount> ballotCounts, Contest contest, Map<Long, BallotCount> ballotCountRejectionMap,
			Map<Pair<Long, String>, BallotCount> ballotCountMap) {
		for (BallotCount bc : ballotCounts) {
			BallotRejection rejection = bc.getBallotRejection();
			if (rejection != null) {
				ballotCountRejectionMap.put(rejection.getPk(), bc);
			}

			Ballot ballot = bc.getBallot();
			if (ballot != null) {
				ballotCountMap.put(Pair.of(contest.getPk(), ballot.getId()), bc);
			}
		}
	}

	/**
	 * Make a modified ballot, either a rejected with image or modified ballot
	 */
	private void makeCastVote(
			UserData userData, ElectionEvent electionEvent, Contest contest, Namespace ns, Element castVoteEl,
			Map<Pair<Long, String>, BallotCount> ballotCountMap, Map<Long, BallotCount> ballotCountRejectionMap,
			Element contestEl, Map<String, File> tifFiles) throws IOException {
		Element ballotIdentifier = castVoteEl.getChild("BallotIdentifier", ns);
		String ballotId = ballotIdentifier.getAttributeValue(ID);

		Element eventIdentifierEl = castVoteEl.getChild("EventIdentifier", ns);
		String electionEventId = eventIdentifierEl.getAttributeValue(ID);

		electionEventIdsMustMatch(electionEvent, electionEventId);

		Element proposedRejection = castVoteEl.getChild("ProposedRejection", ns);
		List<Element> selections = contestEl.getChildren("Selection", ns);
		Element selection = selections.get(0);

		Element bIdentifier = getBallotIdentifier(contest, ns, selection);
		BallotCount ballotCount = selectBallotCount(contest, ballotCountMap, ballotCountRejectionMap, proposedRejection, bIdentifier);

		Element ballotName = ballotIdentifier.getChild("BallotName", ns);
		BinaryData binaryData = importScannedBallot(userData, electionEvent, tifFiles, ballotName);
		CastBallot castBallot = insertCastBallot(ballotId, ballotCount, binaryData);
		for (int i = 1; i < selections.size(); i++) {
			importCandidateVote(userData, ns, contest, castBallot, ballotCount.getBallot().getPk(), selections.get(i));
		}
	}

	private Element getBallotIdentifier(Contest contest, Namespace ns, Element selection) {
		Element bIdentifier;
		if (contest.getElection().getElectionType().getId().equals(TYPE_PROPORTIONAL_REPRESENTATION)) {
			bIdentifier = selection.getChild(AFFILIATION_IDENTIFIER, ns);
		} else {
			bIdentifier = selection.getChild("ReferendumOptionIdentifier", ns);
		}
		return bIdentifier;
	}

	private void electionEventIdsMustMatch(ElectionEvent electionEvent, String electionEventId) {
		if (electionEventId == null || !electionEventId.equals(electionEvent.getId())) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0322_WRONG_ELECTION_EVENT, null);
		}
	}

	private BinaryData importScannedBallot(UserData userData, ElectionEvent electionEvent, Map<String, File> tifFiles, Element ballotName) throws IOException {
		if (ballotName != null) {
			return setBinaryData(userData, tifFiles, electionEvent, ballotName);
		}
		return null;
	}

	private BallotCount selectBallotCount(Contest contest, Map<Pair<Long, String>, BallotCount> ballotCountMap,
			Map<Long, BallotCount> ballotCountRejectionMap, Element proposedRejection, Element bIdentifier) {
		BallotCount ballotCount;
		if (proposedRejection != null && "yes".equals(proposedRejection.getValue())) {
			String reasonCode = proposedRejection.getAttributeValue("ReasonCode");
			BallotRejection ballotRejection = ballotRejectionRepository.findBallotRejectionById(reasonCode);
			ballotCount = ballotCountRejectionMap.get(ballotRejection.getPk());
		} else {
			String bId = bIdentifier.getAttributeValue(ID);
			if (bId == null) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0323_NO_ID_ATTRIBUTE_ON_ELEMENT, null, AFFILIATION_IDENTIFIER);
			}

			ballotCount = ballotCountMap.get(Pair.of(contest.getPk(), bId));
		}
		return ballotCount;
	}

	private CastBallot insertCastBallot(String ballotId, BallotCount ballotCount, BinaryData binaryData) {
		CastBallot castBallot = new CastBallot();
		castBallot.setId(ballotId);
		castBallot.setBinaryData(binaryData);
		if (ballotCount.getBallot() != null) {
			castBallot.setType(MODIFIED);
		} else {
			castBallot.setType(REJECTED);
		}
		ballotCount.addCastBallot(castBallot);
		return castBallot;
	}

	/**
	 * Upload a scanned vote and return the primary key
	 */
	private BinaryData setBinaryData(UserData userData, Map<String, File> tifFiles, ElectionEvent electionEvent, Element ballotName) throws IOException {
		String ballotImagePath = ballotName.getValue();
		File file = tifFiles.get(ballotImagePath);

		if (file == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0324_NO_TIFF_FILE_FOUND, null, ballotImagePath);
		}

		byte[] bytes = IOUtil.getBytes(file);
		if (bytes != null) {
			return createBinaryData(userData, file, bytes, electionEvent, "cast_vote");
		}
		return null;
	}

	/**
	 * Import the modifications for a ballot (cast vote)
	 */
	private void importCandidateVote(UserData userData, Namespace ns, Contest contest, CastBallot castBallot, Long ballotPk, Element selection) {
		Element candidateIdentifier = selection.getChild("CandidateIdentifier", ns);
		int candidateShortCode = Integer.parseInt(candidateIdentifier.getAttributeValue(SHORT_CODE));

		VoteCategory voteCategory = null;
		Integer renumber = null;
		Pair<Long, Integer> candidateByBallotAndOrderParams;
		if (selection.getAttributeValue(SHORT_CODE).equals(WRITEIN)) {
			voteCategory = findVoteCategoryById(WRITEIN);

			Element affiliationIdentifier = selection.getChild(AFFILIATION_IDENTIFIER, ns);
			String affiliationIdentifierId = affiliationIdentifier.getAttributeValue(ID);
			if (castBallot.getBallotCount().getBallotId().equals(affiliationIdentifierId)) {
				// if candidate vote is a write in and to the same party, audit log and ignore candidate vote
				String candidateId = candidateIdentifier.getAttributeValue(ID);
				IgnoredWriteInCandidateVoteAuditEvent auditEvent = new IgnoredWriteInCandidateVoteAuditEvent(userData, candidateId,
						"Ignored write in candidate from same party");
				auditLogService.addToAuditTrail(auditEvent);
				return;
			}
			Long ballotWriteinPk = ballotRepository.findPkByContestAndId(contest.getPk(), affiliationIdentifierId);
			candidateByBallotAndOrderParams = Pair.of(ballotWriteinPk, candidateShortCode);
		} else {
			candidateByBallotAndOrderParams = Pair.of(ballotPk, candidateShortCode);

			if (selection.getAttributeValue(SHORT_CODE).equals(PERSONAL)) {
				voteCategory = findVoteCategoryById(PERSONAL);
			} else if (selection.getAttributeValue(SHORT_CODE).equals(STRIKEOUT)) {
				voteCategory = findVoteCategoryById(STRIKEOUT);
			} else if (selection.getAttributeValue(SHORT_CODE).equals(RENUMBER)) {
				voteCategory = findVoteCategoryById(RENUMBER);
				renumber = Integer.valueOf(selection.getAttributeValue("Value"));
			}
		}

		insertCandidateVote(castBallot, candidateByBallotAndOrderParams, voteCategory, renumber);
	}

	private void insertCandidateVote(CastBallot castBallot, Pair<Long, Integer> candidateByBallotAndOrderParams, VoteCategory voteCategory, Integer renumber) {
		Long ballotPk = candidateByBallotAndOrderParams.getLeft();
		int displayOrder = candidateByBallotAndOrderParams.getRight();
		Candidate candidate = candidateRepository.findCandidateByBallotAndOrder(ballotPk, displayOrder);
		if (candidate == null) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0325_UNABLE_TO_FIND_CANDIDATE, null);
		}
		castBallot.addNewCandidateVote(candidate, voteCategory, renumber);
	}

	private BinaryData createBinaryData(UserData userData, File file, byte[] bytes, ElectionEvent electionEvent, String tableName) {
		String mimeType = new MimetypesFileTypeMap().getContentType(file);
		BinaryData binaryData = new BinaryData();
		binaryData.setMimeType(mimeType);
		binaryData.setElectionEvent(electionEvent);
		binaryData.setBinaryData(bytes);
		binaryData.setTableName(tableName);
		binaryData.setColumnName("scan_binary_data_pk");
		binaryData.setFileName(file.getName());
		binaryData = binaryDataRepository.createBinaryData(userData, binaryData);
		return binaryData;
	}

	/**
	 * Creates a votecount with default values
	 */
	private VoteCount createVoteCount(
			ContestReport contestReport, MvArea mvAreaForReporting, VoteCountCategory votingCountCategory,
			CountQualifier countQualifier, int ordinaryVotes, int rejectedVotes) {
		VoteCount voteCount = new VoteCount();
		voteCount.setContestReport(contestReport);
		voteCount.setPollingDistrict(mvAreaForReporting.getPollingDistrict());
		voteCount.setMvArea(mvAreaForReporting);
		voteCount.setVoteCountCategory(votingCountCategory);
		voteCount.setCountQualifier(countQualifier);
		voteCount.setApprovedBallots(ordinaryVotes);
		voteCount.setRejectedBallots(rejectedVotes);
		voteCount.setManualCount(false);
		voteCount.setModifiedBallotsProcessed(true);

		VoteCountStatus voteCountStatus = countingCodeValueRepository.findVoteCountStatusById(VoteCountStatusEnum.COUNTING.getStatus());
		voteCount.setVoteCountStatus(voteCountStatus);
		return voteCount;
	}

	private VoteCount updateVoteCount(VoteCount voteCount, int ordinaryVotes, int rejectedVotes) {
		voteCount.setApprovedBallots(ordinaryVotes);
		voteCount.setRejectedBallots(rejectedVotes);
		voteCount.setManualCount(false);
		voteCount.setModifiedBallotsProcessed(true);

		VoteCountStatus voteCountStatus = countingCodeValueRepository.findVoteCountStatusById(VoteCountStatusEnum.COUNTING.getStatus());
		voteCount.setVoteCountStatus(voteCountStatus);
		return voteCount;
	}
}
