package no.evote.security;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.net.InetAddress;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Typed;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.util.EvoteProperties;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.apache.log4j.Logger;

@Typed()
/**
 * UserData contains info about user such as id, selected role, all available roles and accesses and so on. Currently, it has two main concerns (should it be
 * split..?): - security check on server side - usability: only render menu items user has access to, navigate to correct page given from context without user
 * interaction
 */
@Default
@ApplicationScoped
public class UserData implements Serializable {

	private static final Logger LOG = Logger.getLogger(UserData.class);
	private String uid;
	private SecurityLevel securityLevel;
	private OperatorRole operatorRole;
	private Locale locale;
	private InetAddress clientAddress;
	private Long electionEventPk;
	private String nameLine;
	private transient java.util.Locale javaLocale;
	/**
	 * contains all accesses for user - this is the one that matters for security
	 */
	private AccessCache accessCache;

	public UserData(String uid, SecurityLevel securityLevel, Locale locale, InetAddress clientAddress) {
		this.uid = uid;
		this.securityLevel = requireNonNull(securityLevel);
		this.locale = locale;
		this.clientAddress = clientAddress;
	}

	/**
	 * For testing purposes
	 */
	public UserData() {
	}

	public boolean isElectionEventAdminUser() {
		return hasAccess(Accesses.Admin);
	}

	/**
	 * Clears information related to role selection. Must be done when operator is switching roles.
	 */
	public void invalidateRoleSelection() {
		setOperatorRole(null);
		setAccessCache(null);
	}

	public boolean hasAccess(Accesses... accesses) {
		if (accessCache == null) {
			LOG.info("Missing AccessCache in UserData " + uid);
			return false;
		}
		return accessCache.hasAccess(accesses);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public OperatorRole getOperatorRole() {
		return operatorRole;
	}

	public void setOperatorRole(final OperatorRole operatorRole) {
		this.operatorRole = operatorRole;
		if (operatorRole == null) {
			electionEventPk = null;
		} else {
			electionEventPk = operatorRole.getOperator().getElectionEvent().getPk();
		}
	}

	public AreaPath getOperatorAreaPath() {
		if (operatorRole == null) {
			return null;
		}
		return AreaPath.from(operatorRole.getMvArea().getAreaPath());
	}

	public ElectionPath getOperatorElectionPath() {
		if (operatorRole == null) {
			return null;
		}
		return ElectionPath.from(operatorRole.getMvElection().getElectionPath());
	}

	public AreaLevelEnum getOperatorAreaLevel() {
		return operatorRole.getMvArea().getActualAreaLevel();
	}

	public MvArea getOperatorMvArea() {
		return operatorRole.getMvArea();
	}

	public MvElection getOperatorMvElection() {
		return operatorRole.getMvElection();
	}

	public Long getElectionEventPk() {
		return electionEventPk;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
		javaLocale = null;
	}

	public java.util.Locale getJavaLocale() {
		if (javaLocale == null) {
			if (locale == null) {
				// this should only happen before user is logged in
				String[] localeParts = EvoteProperties.getProperty(EvoteProperties.NO_EVOTE_I18N_DEFAULT_LOCALE, EvoteConstants.DEFAULT_LOCALE).split("-");
				return new java.util.Locale(localeParts[0], localeParts[1]);
			}
			javaLocale = locale.toJavaLocale();
		}
		return javaLocale;
	}

	public Integer getSecurityLevel() {
		return securityLevel.getLevel();
	}

	public void setSecurityLevel(final Integer securityLevel) {
		this.securityLevel = SecurityLevel.fromLevel(securityLevel);
	}

	public Operator getOperator() {
		if (operatorRole == null) {
			return null;
		}
		return operatorRole.getOperator();
	}

	public Role getRole() {
		if (operatorRole == null) {
			return null;
		}
		return operatorRole.getRole();
	}

	public String getRoleId() {
		if (getRole() == null) {
			return null;
		}
		return getRole().getId();
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(final InetAddress remoteAddr) {
		clientAddress = remoteAddr;
	}

	public AccessCache getAccessCache() {
		return accessCache;
	}

	public void setAccessCache(final AccessCache accessCache) {
		this.accessCache = accessCache;
	}

	public ElectionEvent electionEvent() {
		if (getOperator() == null) {
			return null;
		}
		return getOperator().getElectionEvent();
	}

	public String getElectionEventId() {
		if (electionEvent() == null) {
			return null;
		}
		return electionEvent().getId();
	}

	public SecurityLevel getSecurityLevelEnum() {
		return securityLevel;
	}

	public boolean isMunicipalityLevelUser() {
		return getOperatorMvArea().isMunicipalityLevel();
	}

	public boolean isCountyLevelUser() {
		return getOperatorMvArea().isCountyLevel();
	}

	public boolean isSamiElectionCountyUser() {
		return getOperatorAreaPath().getLevel() == AreaLevelEnum.ROOT && getOperatorElectionPath().getLevel() == ElectionLevelEnum.CONTEST;
	}

	public boolean isOpptellingsvalgstyret() {
		MvElection operatorMvElection = getOperatorMvElection();
		MvArea operatorMvArea = getOperatorMvArea();
		return operatorMvElection.getContest() != null && operatorMvArea.isRoot();
	}

	public boolean isFylkesvalgstyret() {
		MvElection operatorMvElection = getOperatorMvElection();
		MvArea operatorMvArea = getOperatorMvArea();
		return operatorMvElection.isOnElectionEventLevel() && operatorMvArea.isCountyLevel();
	}

	public boolean isValgstyret() {
		MvElection operatorMvElection = getOperatorMvElection();
		MvArea operatorMvArea = getOperatorMvArea();
		return operatorMvElection.isOnElectionEventLevel() && operatorMvArea.isMunicipalityLevel();
	}

	public void sjekkAtBrukerTilhørerKommune(AreaPath municipalityPath) {
		municipalityPath.assertMunicipalityLevel();
		AreaPath operatorAreaPath = operatorRole.getMvArea().areaPath();
		if (!operatorAreaPath.toMunicipalityPath().equals(municipalityPath)) {
			throw new IllegalArgumentException(format("user not in municipality: %s", municipalityPath.toString()));
		}
	}

	public ValggeografiNivaa valggeografiNivaaForBruker() {
		return ValggeografiNivaa.fra(getOperatorAreaLevel());
	}

	public ValggeografiSti operatorValggeografiSti() {
		return ValggeografiSti.fra(getOperatorAreaPath());
	}

	public ValghierarkiSti operatorValghierarkiSti() {
		return ValghierarkiSti.fra(getOperatorElectionPath());
	}

	public void setNameLine(String nameLine) {
		this.nameLine = nameLine;
	}

	public String getNameLine() {
		return nameLine;
	}
}
