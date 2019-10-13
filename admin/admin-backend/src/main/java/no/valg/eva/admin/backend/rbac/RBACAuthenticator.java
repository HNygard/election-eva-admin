package no.valg.eva.admin.backend.rbac;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static no.valg.eva.admin.common.auditlog.SimpleAuditEventType.AccessDeniedInBackend;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteSecurityException;
import no.evote.security.AccessCache;
import no.evote.security.ContextSecurable;
import no.evote.security.ContextSecurableDynamicArea;
import no.evote.security.ContextSecurableDynamicElection;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.evote.service.security.SecurityInterceptor;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rbac.service.AccessServiceBean;

import org.apache.log4j.Logger;

@Default
@ApplicationScoped
public class RBACAuthenticator {
	private static final Object CTX_SECURITY_CACHE = SecurityInterceptor.class.toString() + ".CTX_CACHE";
	private static final String AREA_KEY_PREFIX = "a-";
	private static final String EL_KEY_PREFIX = "e-";
	private static final Logger LOGGER = Logger.getLogger(RBACAuthenticator.class);

	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private AccessServiceBean accessService;
	@Inject
	private AuditLogServiceBean auditLogService;

	public RBACAuthenticator() {

	}

	public boolean hasAccess(UserData userData, Accesses...accesses) {
		if (userData.getOperatorRole() == null) {
			return false;
		}

		AccessCache accessCache = userData.getAccessCache();
		if (accessCache == null || accessCache.getSignature() == null) {
			// Get access cache again from database, if there's no access cache available, or if the signature is null (which it will be when we're accessing
			// the administration event).
			accessCache = accessService.findAccessCacheFor(userData);
		}
		return accessCache.hasAccess(accesses);
	}

	/**
	 * Will throw exception if user does not have access.
	 */
	public void verifyAccessToLevels(
			UserData userData, String method, Class<?> parameterType, SecureEntity secureAnnotation, ContextSecurable parameter,
			TransactionSynchronizationRegistry registry) {
		MvArea userDataMvArea = userData.getOperatorRole().getMvArea();
		MvElection userDataMvElection = userData.getOperatorRole().getMvElection();

		// Access to root level on admin event always means full access:
		if (userDataMvElection.getPath().equals(ROOT_ELECTION_EVENT_ID)) {
			return;
		}

		// Control MvArea
		if (secureAnnotation.areaLevel() != AreaLevelEnum.NONE && parameter.getAreaPk(secureAnnotation.areaLevel()) == null) {
			throw new EvoteSecurityException("Unable to find area at level " + secureAnnotation.areaLevel() + " for " + parameterType.getName()
					+ ". It might not be implemented, or the check might be wrong.");
		}

		if ((secureAnnotation.areaLevel() != AreaLevelEnum.NONE && !hasAccessOnArea(secureAnnotation, parameter, userDataMvArea, registry))
				|| (secureAnnotation.areaLevelDynamic() && !hasAccessOnDynamicArea(parameter, userDataMvArea, registry))) {
			String msg = "The current user does not have access to method " + method + " with area path " + userDataMvArea.getPath();

			auditLogService.addToAuditTrail(SimpleAuditEvent.from(userData).ofType(AccessDeniedInBackend).withDetail(msg).build());

			throw new EvoteSecurityException(msg);
		}

		// Control mvElection
		ElectionLevelEnum electionLevel = secureAnnotation.electionLevel();
		if (electionLevel != ElectionLevelEnum.NONE && parameter.getElectionPk(electionLevel) == null) {
			throw new EvoteSecurityException("Unable to find election at level " + electionLevel + " for " + parameterType.getName()
					+ ". It might not be implemented, or the check might be wrong.");
		}

		if ((electionLevel != ElectionLevelEnum.NONE && !hasAccessOnElection(userDataMvElection, parameter, electionLevel, registry))
				|| (secureAnnotation.electionLevelDynamic() && !hasAccessOnDynamicElection(parameter, userDataMvElection, registry))) {
			String msg = "The current user does not have access to method " + method + ". User election path " + userDataMvElection.getPath() + " does not match "
					+ mvElectionRepository.findByPkAndLevel(parameter.getElectionPk(electionLevel), electionLevel.getLevel()).getElectionPath();

			auditLogService.addToAuditTrail(SimpleAuditEvent.from(userData).ofType(AccessDeniedInBackend).withDetail(msg).build());

			throw new EvoteSecurityException(msg);
		}
	}

	private boolean hasAccessOnElection(
			MvElection userDataMvElection, ContextSecurable parameter, ElectionLevelEnum electionLevel, TransactionSynchronizationRegistry registry) {
		return hasAccessOnElectionPk(userDataMvElection, parameter.getElectionPk(electionLevel), electionLevel, registry);
	}

	private boolean hasAccessOnDynamicElection(ContextSecurable parameter, MvElection userDataMvElection, TransactionSynchronizationRegistry registry) {
		if (!(parameter instanceof ContextSecurableDynamicElection)) {
			return true;
		}

		ContextSecurableDynamicElection dynamicParameter = (ContextSecurableDynamicElection) parameter;
		return hasAccessOnElectionPk(
				userDataMvElection, parameter.getElectionPk(dynamicParameter.getActualElectionLevel()), dynamicParameter.getActualElectionLevel(), registry);
	}

	/**
	 * Used for manually checking access to an entity.
	 */
	public boolean hasAccess(UserData userData, ContextSecurableDynamicArea entity, TransactionSynchronizationRegistry registry) {
		return hasAccessOnDynamicArea(entity, userData.getOperatorRole().getMvArea(), registry);
	}

	private boolean hasAccessOnDynamicArea(ContextSecurable parameter, MvArea userDataMvArea, TransactionSynchronizationRegistry registry) {
		if (!(parameter instanceof ContextSecurableDynamicArea)) {
			return true;
		}

		ContextSecurableDynamicArea dynamicParameter = (ContextSecurableDynamicArea) parameter;

		// Exception to handle access to root level of the area hierarchy:
		if (dynamicParameter.getActualAreaLevel() == AreaLevelEnum.ROOT) {
			return userDataMvArea.getAreaLevel() == 0
					&& userDataMvArea.getElectionEvent().getPk().equals(parameter.getAreaPk(dynamicParameter.getActualAreaLevel()));
		}

		return hasAccessOnAreaPk(userDataMvArea, parameter.getAreaPk(dynamicParameter.getActualAreaLevel()), dynamicParameter.getActualAreaLevel(), registry);
	}

	private boolean hasAccessOnArea(SecureEntity secureAnnotation, ContextSecurable parameter, MvArea userDataMvArea,
									TransactionSynchronizationRegistry registry) {
		return hasAccessOnAreaPk(userDataMvArea, parameter.getAreaPk(secureAnnotation.areaLevel()), secureAnnotation.areaLevel(), registry);
	}

	private boolean hasAccessOnAreaPk(MvArea mvArea, Long areaPk, AreaLevelEnum areaLevel, TransactionSynchronizationRegistry registry) {
		Map<String, Boolean> contextSecurityCache = getContextSecurityCache(registry);

		String key = null;
		if (contextSecurityCache != null) {
			key = AREA_KEY_PREFIX + mvArea.getAreaPath() + "-" + areaPk + "-" + areaLevel;

			if (contextSecurityCache.containsKey(key)) {
				LOGGER.debug("Returning CACHED lookup for hasAccessOnAreaPk(" + key + ")");
				return contextSecurityCache.get(key);
			}
		}

		Boolean result = mvAreaRepository.hasAccessToPkOnLevel(mvArea, areaPk, areaLevel.getLevel());
		if (contextSecurityCache != null) {
			contextSecurityCache.put(key, result);
		}

		LOGGER.debug("Returning UNCACHED lookup for hasAccessOnAreaPk(" + key + ")");

		return result;
	}

	private Boolean hasAccessOnElectionPk(MvElection mvElection, Long electionPk, ElectionLevelEnum electionLevel, TransactionSynchronizationRegistry registry) {
		Map<String, Boolean> contextSecurityCache = getContextSecurityCache(registry);

		String key = null;
		if (contextSecurityCache != null) {
			key = EL_KEY_PREFIX + mvElection.getElectionPath() + "-" + electionPk + "-" + electionLevel;
			if (contextSecurityCache.containsKey(key)) {
				LOGGER.debug("Returning CACHED lookup for hasAccessOnElectionPk(" + key + ")");
				return contextSecurityCache.get(key);
			}
		}

		Boolean result = mvElectionRepository.hasAccessToPkOnLevel(mvElection, electionPk, electionLevel.getLevel());
		if (contextSecurityCache != null) {
			contextSecurityCache.put(key, result);
		}

		LOGGER.debug("Returning UNCACHED lookup for hasAccessOnElectionPk(" + key + ")");

		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Boolean> getContextSecurityCache(TransactionSynchronizationRegistry registry) {
		Map<String, Boolean> contextSecurityCache = (Map<String, Boolean>) registry.getResource(CTX_SECURITY_CACHE);
		if (contextSecurityCache == null && transactionIsActive(registry)) {
			contextSecurityCache = new HashMap<>();
			registry.putResource(CTX_SECURITY_CACHE, contextSecurityCache);
		}
		return contextSecurityCache;
	}

	private boolean transactionIsActive(TransactionSynchronizationRegistry registry) {
		return registry.getTransactionKey() != null;
	}
}
