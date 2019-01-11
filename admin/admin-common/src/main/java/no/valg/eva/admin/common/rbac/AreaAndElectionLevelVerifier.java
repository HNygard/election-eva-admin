package no.valg.eva.admin.common.rbac;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;

import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

public final class AreaAndElectionLevelVerifier {

	private static final ElectionPath ROOT_ELECTION_EVENT_PATH = ElectionPath.from(ROOT_ELECTION_EVENT_ID);

	private AreaAndElectionLevelVerifier() {
	}

	/** Bill Pugh's initialization-on-demand holder idiom */
	private static class LazyHolder {
		private static final AreaAndElectionLevelVerifier INSTANCE = new AreaAndElectionLevelVerifier();
	}

	public static AreaAndElectionLevelVerifier getInstance() {
		return LazyHolder.INSTANCE;
	}

	/**
	 * Verify that the user (represented by UserData) has access rights to modify the OperatorRole. This means that the role that is created or modified is at a
	 * level equal to, or below the user's own level.
	 * <p/>
	 * If the user has access to the root election event, he/she is always allowed.
	 * 
	 * @throws EvoteSecurityException
	 *             if access is denied
	 */
	public void verifyAreaAndElectionLevels(final UserData userData, final OperatorRole operatorRole) {
		AreaPath userAreaPath = AreaPath.from(userData.getOperatorRole().getMvArea().getAreaPath());
		ElectionPath userElectionPath = ElectionPath.from(userData.getOperatorRole().getMvElection().getElectionPath());

		AreaPath operatorAreaPath = AreaPath.from(operatorRole.getMvArea().getAreaPath());
		ElectionPath operatorElectionPath = ElectionPath.from(operatorRole.getMvElection().getElectionPath());

		verifyAreaAndElectionLevels(userAreaPath, userElectionPath, operatorAreaPath, operatorElectionPath);
	}

	/**
	 * Verifies that the area and election paths for the user executing an operation authorizes him/her to modify some resource for an operator (typically a
	 * role association, a.k.a an OperatorRole).
	 * 
	 * @throws EvoteSecurityException
	 *             if access is denied
	 * @see #verifyAreaAndElectionLevels(no.evote.security.UserData, OperatorRole)
	 */
	public void verifyAreaAndElectionLevels(AreaPath userAreaPath, ElectionPath userElectionPath, AreaPath operatorAreaPath, ElectionPath operatorElectionPath) {
		if (userElectionPath.equals(ROOT_ELECTION_EVENT_PATH)) {
			return;
		}

		if (!operatorAreaPath.isSubpathOf(userAreaPath)) {
			throw new EvoteSecurityException(
					"Access denied, area path is not equal to or beneath users own area (" + userAreaPath + ", " + operatorAreaPath + ")");
		}

		if (!operatorElectionPath.isSubpathOf(userElectionPath)) {
			throw new EvoteSecurityException(
					"Access denied, election path is not equal to or beneath users own area (" + userElectionPath + ", " + operatorElectionPath + ")");
		}
	}
}
