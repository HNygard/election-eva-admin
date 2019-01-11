package no.evote.service.security;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static no.valg.eva.admin.common.auditlog.SimpleAuditEventType.AccessDeniedInBackend;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteSecurityException;
import no.evote.exception.ReadOnlyPrivilegeException;
import no.evote.security.AccessCache;
import no.evote.security.ContextSecurable;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.evote.service.CryptoServiceBean;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.backend.rbac.RBACAuthenticator;
import no.valg.eva.admin.common.auditlog.auditevents.SimpleAuditEvent;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.common.rbac.SecurityType;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

import org.apache.log4j.Logger;

public class SecurityInterceptor implements Serializable {
	public static final String USER_DATA_KEY = "userData";
	private static final long serialVersionUID = 1L;
	private static final String ACCESS_CACHE_VERIFIED = "accessCacheVerified";
	private final transient Logger log = Logger.getLogger(SecurityInterceptor.class);

	@Resource
	private TransactionSynchronizationRegistry registry;

	@Inject
	private RBACAuthenticator authenticator;

	@Inject
	private CryptoServiceBean cryptoService;

	@Inject
	private ElectionEventRepository electionEventRepository;

	@Inject
	private AuditLogServiceBean auditLogService;

	@PersistenceContext(unitName = "evotePU")
	private EntityManager em;

	@AroundInvoke
	/**
	 * Interception method that controls that the user has access to the right securable objects and has the right election and area context
	 */
	public Object intercept(InvocationContext ctx) throws Exception {
		if (shouldCheckSecurableObjects(ctx.getMethod())) {
			checkAccess(ctx);
			controlContext(ctx);
		}
		return ctx.proceed();
	}

	private void checkAccess(InvocationContext ctx) {
		Security security = getAnnotation(ctx, Security.class);
		if (security == null) {
			SecurityNone none = getAnnotation(ctx, SecurityNone.class);
			if (none == null) {
				throw new EvoteSecurityException("Missing 'Security' annotation for " + getMethodInfo(ctx));
			}
			return;
		}

		UserData userData = getValidUserData(ctx);
		if (security.type() == SecurityType.WRITE && userData.getRole().isUserSupport()) {
			throw new ReadOnlyPrivilegeException("User " + userData.getUid() + " with support role does not have write access to " + getMethodInfo(ctx));
		}
		if (transactionIsActive()) {
			registry.putResource(USER_DATA_KEY, userData);
		}

		if (userData.getAccessCache() != null) {
			verifyAccessCacheFromUserData(userData);
		}

		boolean hasAccess = authenticator.hasAccess(userData, security.accesses());
		if (!hasAccess) {
			List<String> annotated = new ArrayList<>();
			for (Accesses accesses : security.accesses()) {
				Collections.addAll(annotated, accesses.paths());
			}
			StringBuilder msg = new StringBuilder("User " + userData.getUid() + " does not have access to ");
			msg.append(getMethodInfo(ctx));
			msg.append(". Method requires ").append(annotated);
			if (userData.getAccessCache() != null) {
				msg.append(". User has ").append(userData.getAccessCache().getSecurableObjectsAsString());
			}

			auditLogService.addToAuditTrail(SimpleAuditEvent.from(userData).ofType(AccessDeniedInBackend).withDetail(msg.toString()).build());

			throw new EvoteSecurityException(msg.toString());
		}
	}

	private String getMethodInfo(InvocationContext ctx) {
		StringBuilder builder = new StringBuilder();
		builder.append("Method ").append(ctx.getMethod().getName()).append(" in ").append(ctx.getTarget().getClass().getName());
		return builder.toString();
	}

	private <A extends Annotation> A getAnnotation(InvocationContext ctx, Class<A> cls) {
		A annot = ctx.getTarget().getClass().getAnnotation(cls);
		A methodAnnot = ctx.getMethod().getAnnotation(cls);
		if (methodAnnot != null) {
			annot = methodAnnot;
		}
		return annot;
	}

	private UserData getValidUserData(InvocationContext ctx) {
		UserData userData = getUserDataFromParameter(ctx);
		if (userData == null) {
			throw new EvoteSecurityException("UserData required as first argument to " + getMethodInfo(ctx));
		}

		// Operator and role must be active
		if (!userData.getOperator().isActive()) {
			throw new EvoteSecurityException("Operator " + userData.getOperator().getId() + " is disabled");
		}

		if (!userData.getRole().isActive()) {
			throw new EvoteSecurityException("Role " + userData.getRole().getId() + " is disabled");
		}
		return userData;
	}

	private UserData getUserDataFromParameter(final InvocationContext ctx) {
		// Get UserData object, should be first parameter:
		Object[] parameters = ctx.getParameters();
		if (parameters == null || parameters.length == 0 || !(parameters[0] instanceof UserData)) {
			return null;
		}

		return (UserData) parameters[0];
	}

	private boolean shouldCheckSecurableObjects(final Method method) {
		return Modifier.isPublic(method.getModifiers());
	}

	private void verifyAccessCacheFromUserData(final UserData userData) {
		Boolean hasBeenVerifiedWithinTransaction = (Boolean) registry.getResource(ACCESS_CACHE_VERIFIED);
		if (hasBeenVerifiedWithinTransaction != null && hasBeenVerifiedWithinTransaction) {
			return;
		}

		AccessCache accessCache = userData.getAccessCache();
		if (accessCache.getSignature() == null) {
			if (!electionEventRepository.findByPk(userData.getElectionEventPk()).getId().equals(ROOT_ELECTION_EVENT_ID)) {
				throw new EvoteSecurityException("Unable to find access cache signature.");
			} else {
				return;
			}
		}
		try {
			if (cryptoService.verifyAdminElectionEventSignature(userData, accessCache.getSecurableObjectsAsString().getBytes("UTF-8"),
					accessCache.getSignature(),
					userData.getElectionEventPk())) {
				registry.putResource(ACCESS_CACHE_VERIFIED, Boolean.TRUE);
				return;
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}

		throw new EvoteSecurityException("Access cache signature mismatch.");
	}

	/**
	 * Verify that the user has access to the specific entities, as specified by the roles placement in the area and election hierarchies.
	 */
	private void controlContext(final InvocationContext ctx) {
		UserData userData;
		Method method = ctx.getMethod();

		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();

		for (int i = 0; i < parameterAnnotations.length; i++) {
			Annotation[] annotations = parameterAnnotations[i];
			Class<?> parameterType = parameterTypes[i];

			userData = getUserDataFromParameter(ctx);
			for (Annotation annotation : annotations) {
				if (annotation instanceof SecureEntity) {
					SecureEntity secureAnnotation = (SecureEntity) annotation;
					if (userData == null) {
						throw new EvoteSecurityException("UserData required as first argument to " + getMethodInfo(ctx));
					}

					Object methodParam = ctx.getParameters()[i];
					// It's a list, make sure we check each parameter
					if (methodParam instanceof List<?>) {
						// If a list of long, lookup pk
						if (((List<?>) methodParam).get(0) instanceof Long) {
							for (Object parameter : (List<?>) methodParam) {
								ContextSecurable entity = getEntityForContextSecurity(ctx, method, (Long) parameter, secureAnnotation);
								authenticator.verifyAccessToLevels(userData, method.toString(), parameterType, secureAnnotation, entity, registry);
							}
						} else {
							for (Object parameter : (List<?>) methodParam) {
								authenticator
										.verifyAccessToLevels(userData, method.toString(), parameterType, secureAnnotation, (ContextSecurable) parameter,
												registry);
							}
						}
					} else if (methodParam != null) {
						// Find the entity: the parameter is either the entity we want, or its primary key
						ContextSecurable entity = null;
						if (methodParam instanceof ContextSecurable) {
							entity = (ContextSecurable) methodParam;
						} else if (methodParam instanceof Long) {
							if (((Long) methodParam) != 0) {
								entity = getEntityForContextSecurity(ctx, method, (Long) ctx.getParameters()[i], secureAnnotation);
							}
						} else {
							throw new EvoteSecurityException("Parameter does not implement ContextSecurable, or is not a primary key: "
									+ parameterType.getName() + " (" + method + ")");
						}

						// If parameter is null, there should be no entity to check
						if (entity != null) {
							authenticator.verifyAccessToLevels(userData, method.toString(), parameterType, secureAnnotation, entity, registry);
						}
					}

					log.debug("Security check passed: " + secureAnnotation + " for method " + method);
				}
			}
		}
	}

	private ContextSecurable getEntityForContextSecurity(final InvocationContext ctx, final Method method, final Long primaryKeyParameter,
			final SecureEntity secureAnnotation) {
		ContextSecurable parameter = null;

		if (secureAnnotation.entity().equals(Object.class)) {
			// Get either the area or election entity

			// If both areaLevel and electionLevel has been specified, we can't really know which entity to get, so the configuration must be wrong
			if (secureAnnotation.areaLevel() != AreaLevelEnum.NONE && secureAnnotation.electionLevel() != ElectionLevelEnum.NONE) {
				throw new EvoteSecurityException(
						"Trying to retrieve entity, but unable to continue because both area and election level has been defined. Check configuration for "
								+ ctx.getClass() + "." + method.getName());
			}

			if (secureAnnotation.areaLevel() != AreaLevelEnum.NONE) {
				parameter = getAreaEntity(secureAnnotation.areaLevel(), primaryKeyParameter);
			} else if (secureAnnotation.electionLevel() != ElectionLevelEnum.NONE) {
				parameter = getElectionEntity(secureAnnotation.electionLevel(), primaryKeyParameter);
			}
		} else {
			// Get the entity specified in the annotation
			parameter = findByPk(secureAnnotation.entity(), primaryKeyParameter);
		}

		if (parameter == null) {
			throw new EvoteSecurityException("Unable to find entity for level and primary key: " + secureAnnotation.areaLevel() + ", " + primaryKeyParameter
					+ " when checking " + method);
		}
		return parameter;
	}

	private ContextSecurable getElectionEntity(final ElectionLevelEnum electionLevel, final Long pk) {
		switch (electionLevel) {
		case ELECTION_EVENT:
			return findByPk(ElectionEvent.class, pk);
		case ELECTION_GROUP:
			return findByPk(ElectionGroup.class, pk);
		case ELECTION:
			return findByPk(Election.class, pk);
		case CONTEST:
			return findByPk(Contest.class, pk);
		default:
			return null;
		}
	}

	private ContextSecurable getAreaEntity(final AreaLevelEnum areaLevel, final Long pk) {
		switch (areaLevel) {
		case COUNTRY:
			return findByPk(Country.class, pk);
		case COUNTY:
			return findByPk(County.class, pk);
		case MUNICIPALITY:
			return findByPk(Municipality.class, pk);
		case BOROUGH:
			return findByPk(Borough.class, pk);
		case POLLING_DISTRICT:
			return findByPk(PollingDistrict.class, pk);
		case POLLING_PLACE:
			return findByPk(PollingPlace.class, pk);
		default:
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T findByPk(final Class clazz, final Long pk) {
		return (T) em.find(clazz, pk);
	}

	private boolean transactionIsActive() {
		return registry.getTransactionKey() != null;
	}
}
