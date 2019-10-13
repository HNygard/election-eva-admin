package no.evote.service.security;

import static no.evote.exception.ErrorCode.ERROR_CODE_0101_NO_OPERATOR;
import static no.evote.exception.ErrorCode.ERROR_CODE_0102_NO_ROLE;
import static no.evote.exception.ErrorCode.ERROR_CODE_0103_NO_OPERATOR_ROLE;
import static no.evote.security.SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC;
import static no.evote.util.EvoteProperties.EXPORT_TOKEN_EXPIRATION_TIME;
import static no.evote.util.EvoteProperties.getProperty;
import static no.valg.eva.admin.util.XMLUtil.child;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.security.UserData;
import no.evote.service.AccessTokenAndSignature;
import no.evote.service.CryptoServiceBean;
import no.evote.service.LegacyUserDataService;
import no.evote.service.configuration.ReportingUnitServiceBean;
import no.evote.service.rbac.LegacyAccessServiceBean;
import no.evote.service.rbac.OperatorRoleServiceBean;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.util.XMLUtil;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Default
@ApplicationScoped
public class LegacyUserDataServiceBean {
	private static final Logger LOGGER = Logger.getLogger(LegacyUserDataService.class);
	private static final Integer TOKEN_EXPIRATION_TIME_HOURS = Integer.valueOf(getProperty(EXPORT_TOKEN_EXPIRATION_TIME, "24"));

	@Inject
	private LegacyAccessServiceBean accessService;
	@Inject
	private OperatorRoleServiceBean operatorRoleService;
	@Inject
	private OperatorRepository operatorRepository;
	@Inject
	private RoleRepository roleRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private LocaleRepository localeRepository;
	@Inject
	private CryptoServiceBean cryptoService;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private ReportingUnitServiceBean reportingUnitService;
	@Inject
	private OperatorRoleRepository operatorRoleRepository;
	@Inject
	private ContestRepository contestRepository;
	@Inject
	private LocaleTextRepository localeTextRepository;

	public LegacyUserDataServiceBean() {

	}

	/**
	 * Generated a role XML element with information in RBAC linked to the OperatorRole
	 */
	private Element generateRole(final UserData userData, final OperatorRole operatorRole) {
		Element role = new Element(LegacyUserDataService.ROLE_TAG);
		Element roleName = new Element(LegacyUserDataService.ROLE_NAME_TAG);
		String translatedRoleName = localeTextRepository.findByElectionEventLocaleAndTextId(
				operatorRole.getRole().getElectionEvent().getPk(), userData.getLocale().getPk(), operatorRole.getRole().getName()).getLocaleText();
		roleName.setText(translatedRoleName);
		role.addContent(roleName);

		Element roleId = new Element(LegacyUserDataService.ROLE_ID_TAG);
		roleId.setText(operatorRole.getRole().getId());
		role.addContent(roleId);

		Element areaContextId = new Element(LegacyUserDataService.AREA_CONTEXT_ID_TAG);
		areaContextId.setText(operatorRole.getMvArea().getPath());
		role.addContent(areaContextId);

		Element areaContextName = new Element(LegacyUserDataService.AREA_CONTEXT_NAME_TAG);
		areaContextName.setText(operatorRole.getMvArea().toString());
		role.addContent(areaContextName);

		Element electionContextId = new Element(LegacyUserDataService.ELECTION_CONTEXT_ID_TAG);
		electionContextId.setText(operatorRole.getMvElection().getPath());
		role.addContent(electionContextId);

		Element electionContextName = new Element(LegacyUserDataService.ELECTION_CONTEXT_NAME_TAG);
		electionContextName.setText(operatorRole.getMvElection().toString());
		role.addContent(electionContextName);

		// Adding ReportingUnits
		role.addContent(generateReportingUnits(operatorRole));

		// Adding Access objects
		role.addContent(generateAccesses(operatorRole));
		return role;
	}

	/**
	 * Generates a XML element with the accesses linked to the operatorRole
	 */
	private Element generateAccesses(final OperatorRole operatorRole) {
		Element accesses = new Element(LegacyUserDataService.ACCESSES_TAG);

		for (Access a : accessService.getIncludedAccesses(operatorRole.getRole())) {
			Element access = new Element(LegacyUserDataService.ACCESS_TAG);
			access.setText(a.getPath());
			accesses.addContent(access);
		}
		return accesses;
	}

	/**
	 * Generates a XML element with the reporting units accessible to the operatorRole
	 */
	private Element generateReportingUnits(OperatorRole operatorRole) {
		Element reportingUnitsElement = new Element(LegacyUserDataService.REPORTING_UNITS_TAG);
		try {
			List<ReportingUnit> reportingUnits = reportingUnitService.getAccessibleReportingUnits(operatorRole.getMvElection(), operatorRole.getMvArea());

			for (ReportingUnit ru : reportingUnits) {
				Element reportingUnit = new Element(LegacyUserDataService.REPORTING_UNIT_TAG);
				reportingUnitsElement.addContent(reportingUnit);

				Element reportingUnitId = new Element(LegacyUserDataService.REPORTING_UNIT_ID_TAG);

				String mvElectionId = "";
				if (ru.getMvElection().getElectionLevel() > 0) {
					mvElectionId = ru.getMvElection().getPath();
					mvElectionId = mvElectionId.substring(mvElectionId.indexOf('.') + 1);
				}

				String mvAreaId = "";
				if (ru.getMvArea().getAreaLevel() > 0) {
					mvAreaId = ru.getMvArea().getPath();
					mvAreaId = mvAreaId.substring(mvAreaId.indexOf('.') + 1);
				}

				reportingUnitId.setText(mvElectionId + "-" + mvAreaId);
				reportingUnit.addContent(reportingUnitId);

				Element reportingUnitName = new Element(LegacyUserDataService.REPORTING_UNIT_NAME_TAG);
				reportingUnitName.setText(ru.getNameLine());
				reportingUnit.addContent(reportingUnitName);
			}
		} catch (EvoteNoRollbackException e) {
			if (!(e.getCause() instanceof javax.persistence.NoResultException || e.getCause() instanceof javax.persistence.NonUniqueResultException)) {
				throw e;
			}
		}
		return reportingUnitsElement;
	}

	/**
	 * Generates a XML document with information in RBAC linked to the Operator. This method includes all the operators roles The document can then be exported
	 * to a external system
	 */
	public Document exportAccessToken(final UserData userData, final Operator operator) {
		List<Operator> exportableOperators = operatorRepository.findOperatorsById(userData.getUid());

		if (!exportableOperators.contains(operator)) {
			return null;
		}

		ElectionEvent electionEvent = electionEventRepository.findByPk(operator.getElectionEvent().getPk());

		Element root = new Element(LegacyUserDataService.ROOT_TAG);

		Element uid = new Element(LegacyUserDataService.UID_TAG);
		uid.setText(operator.getId());
		root.addContent(uid);

		Element name = new Element(LegacyUserDataService.OPERATOR_NAME_TAG);
		name.setText(operator.getNameLine());
		root.addContent(name);

		Element electionEventIdElement = new Element(LegacyUserDataService.ELECTION_EVENT_ID_TAG);
		electionEventIdElement.setText(electionEvent.getId());
		root.addContent(electionEventIdElement);

		Element electionEventNameElement = new Element(LegacyUserDataService.ELECTION_EVENT_NAME_TAG);
		electionEventNameElement.setText(electionEvent.getName());
		root.addContent(electionEventNameElement);

		Element createdTimestamp = new Element(LegacyUserDataService.CREATED_TAG);
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy.MM.dd-HH:mm:ss").withLocale(new Locale("nb_NO"));
		createdTimestamp.setText(formatter.print(DateTime.now()));
		root.addContent(createdTimestamp);

		Element expTimestamp = new Element(LegacyUserDataService.EXPIRATION_TAG);
		DateTime expirationTime = DateTime.now().plusHours(TOKEN_EXPIRATION_TIME_HOURS);
		LOGGER.warn("Exporting token with expiration time: " + TOKEN_EXPIRATION_TIME_HOURS);
		expTimestamp.setText(formatter.print(expirationTime));
		root.addContent(expTimestamp);

		Element roles = new Element(LegacyUserDataService.ROLES_TAG);
		root.addContent(roles);
		for (OperatorRole r : getOperatorRolesWithAddedTransientOperatorRolesWhenBoroughElection(operator)) {
			if (userData.getSecurityLevel() >= roleRepository.getAccumulatedSecLevelFor(r.getRole()) && r.getRole().isActive()
					&& r.getOperator().isActive()) {
				roles.addContent(generateRole(userData, r));
			}
		}

		return new Document(root);
	}

	private List<OperatorRole> getOperatorRolesWithAddedTransientOperatorRolesWhenBoroughElection(Operator operator) {
		List<OperatorRole> originalOperatorRoles = operatorRoleService.getOperatorRoles(operator);
		List<OperatorRole> withTransientOperatorRoles = new ArrayList<>(originalOperatorRoles.size());

		for (OperatorRole originalOperatorRole : originalOperatorRoles) {
			withTransientOperatorRoles.add(originalOperatorRole);

			if (originalOperatorRole.getMvArea().isMunicipalityLevel()) {
				List<Contest> boroughContests = contestRepository.findBoroughContestsInMunicipality(originalOperatorRole.getMvArea().getMunicipality());

				for (Contest boroughContest : boroughContests) {
					for (ContestArea contestArea : boroughContest.getContestAreaSet()) { // Avoids hard-coding that Contest-MvArea is 1-1 for borough contests
						MvArea boroughMvArea = contestArea.getMvArea();
						OperatorRole transientOperatorRole = buildTransientOperatorRoleWithArea(originalOperatorRole, boroughMvArea);

						withTransientOperatorRoles.add(transientOperatorRole);
					}
				}
			}
		}

		return withTransientOperatorRoles;
	}

	private OperatorRole buildTransientOperatorRoleWithArea(OperatorRole from, MvArea mvArea) {
		OperatorRole to = new OperatorRole();

		to.setOperator(from.getOperator());
		to.setRole(from.getRole());
		to.setMvElection(from.getMvElection());
		to.setInOwnHierarchy(from.isInOwnHierarchy());

		to.setMvArea(mvArea); // this is the modification

		return to;
	}

	/**
	 * Creates and populates an UserData object based on the identifiers sent in as parameters
	 */
	public UserData getUserData(String operatorId, String roleId, String areaPath, String electionPath, InetAddress remoteAddr) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from(electionPath).tilValghierarkiSti());
		MvArea mvArea = mvAreaRepository.findSingleByPath(AreaPath.from(areaPath));

		Operator operator = operatorRepository.findByElectionEventsAndId(mvElection.getElectionEvent().getPk(), operatorId);
		if (operator == null) {
			throw new EvoteException(ERROR_CODE_0101_NO_OPERATOR, null, operatorId, electionPath);
		}

		Role role = roleRepository.findByElectionEventAndId(mvElection.getElectionEvent(), roleId);
		if (role == null) {
			throw new EvoteException(ERROR_CODE_0102_NO_ROLE, null, roleId, electionPath);
		}

		OperatorRole operatorRole = operatorRoleRepository.findUnique(role, operator, mvArea, mvElection);
		if (operatorRole == null) {
			throw new EvoteException(ERROR_CODE_0103_NO_OPERATOR_ROLE, null, operator.getId(), role.getId(), electionPath, areaPath);
		}

		UserData userData = new UserData(operatorRole.getOperator().getId(), TWO_FACTOR_OF_WHICH_ONE_DYNAMIC, localeRepository.findById("nb-NO"), remoteAddr);
		userData.setOperatorRole(operatorRole);

		return userData;
	}

	/**
	 * Used by web services called by scanning to verify the validity of the token with regards to the signature, user id, and expiration
	 */
	public boolean isFileUploadDownloadTokenValid(UserData userData, byte[] tokenZip, String userId) {

		ByteArrayInputStream tokenZipStream = new ByteArrayInputStream(tokenZip);
		try {
			Map<String, InputStream> fileStreams = IOUtil.unzipTokenAndCheckSize(tokenZipStream);

			InputStream tokenXmlStream = fileStreams.get("token.xml");
			InputStream tokenPemStream = fileStreams.get("token.pem");

			if (tokenPemStream == null) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0107_NO_PEM_FILE);
			}

			byte[] tokenXmlBytes = IOUtil.getBytes(tokenXmlStream);
			if (!cryptoService.verifyAdminElectionEventSignature(userData, tokenXmlBytes, IOUtil.getBytes(tokenPemStream),
					userData.getElectionEventPk())) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0106_SIGNATURE_VERIFICATION_ERROR);
			}

			// Validate expiration and userId
			LOGGER.info("Validate expiration and userId: token.xml");

			SAXBuilder builder = new SAXBuilder();

			Document document = builder.build(new ByteArrayInputStream(tokenXmlBytes));
			Element root = document.getRootElement();

			Namespace ns = root.getNamespace();

			Element expiration = child(root, LegacyUserDataService.EXPIRATION_TAG, ns);
			DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy.MM.dd-HH:mm:ss").withLocale(new Locale("nb_NO"));

			DateTime expirationDate = dateFormatter.parseDateTime(expiration.getValue());
			if (expirationDate.isBefore(DateTime.now())) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0108_TOKEN_EXPIRED, null);
			}

			Element uId = child(root, LegacyUserDataService.UID_TAG, ns);
			if (!uId.getValue().equals(userId)) {
				throw new EvoteException(ErrorCode.ERROR_CODE_0109_INVALID_USER, null);
			}
		} catch (IllegalStateException e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_9105_TOKEN_SIZE_TOO_BIG, e);
		} catch (IOException e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0104_UNEXPECTED_TOKEN_VALIDATION, e);
		} catch (JDOMException | IllegalArgumentException e) {
			throw new EvoteException(ErrorCode.ERROR_CODE_0105_TOKEN_XML_PARSING_ERROR, e, e.getMessage());
		}

		return true;
	}

	public AccessTokenAndSignature exportSignedAccessToken(UserData userData, Operator operator) {
		Document accessToken = exportAccessToken(userData, operator);
		byte[] signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, XMLUtil.documentToBytes(accessToken));

		return new AccessTokenAndSignature(accessToken, signature);
	}

}
