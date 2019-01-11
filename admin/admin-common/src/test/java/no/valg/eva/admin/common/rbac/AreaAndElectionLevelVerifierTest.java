package no.valg.eva.admin.common.rbac;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

import org.testng.annotations.Test;


public class AreaAndElectionLevelVerifierTest {
	@Test
	public void userMustBeAuthorizedToModifyOperator() {
		shallPass("424242.47", "424242.01", "424242.47", "424242.01");
		shallPass("424242.47", "424242.01", "424242.47.19", "424242.01");
		shallPass("424242.47", "424242.01", "424242.47", "424242.01.01");
		shallFail("424242.47", "424242.01", "424242", "424242.01");
		shallFail("424242.47", "424242.01", "424242.47", "424242");
	}

	@Test
	public void shallAlwaysAllowUserWithRootElectionEvent() {
		shallPass("424242.47", ROOT_ELECTION_EVENT_ID, "424242.47", "424242.01");
		shallPass("424242.47", ROOT_ELECTION_EVENT_ID, "424242.47.19", "424242.01");
		shallPass("424242.47", ROOT_ELECTION_EVENT_ID, "424242.47", "424242.01.01");
		shallPass("424242.47", ROOT_ELECTION_EVENT_ID, "424242", "424242.01");
		shallPass("424242.47", ROOT_ELECTION_EVENT_ID, "424242.47", "424242");
	}

	private void shallPass(String userAreaPath, String userElectionPath, String operatorAreaPath, String operatorElectionPath) {
		AreaAndElectionLevelVerifier verifier = AreaAndElectionLevelVerifier.getInstance();
		UserData userData = createUserData(AreaPath.from(userAreaPath), ElectionPath.from(userElectionPath));
		verifier.verifyAreaAndElectionLevels(userData, createOperatorRole(AreaPath.from(operatorAreaPath), ElectionPath.from(operatorElectionPath)));
	}

	private void shallFail(String userAreaPath, String userElectionPath, String operatorAreaPath, String operatorElectionPath) {
		AreaAndElectionLevelVerifier verifier = AreaAndElectionLevelVerifier.getInstance();
		UserData userData = createUserData(AreaPath.from(userAreaPath), ElectionPath.from(userElectionPath));
		try {
			verifier.verifyAreaAndElectionLevels(userData, createOperatorRole(AreaPath.from(operatorAreaPath), ElectionPath.from(operatorElectionPath)));
			fail("Expected EvoteSecurityException");
		} catch (EvoteSecurityException e) {
			assertNotNull(e);
		}
	}

	private UserData createUserData(AreaPath areaPath, ElectionPath electionPath) {
		UserData userData = new UserData();
		OperatorRole operatorRole = createOperatorRole(areaPath, electionPath);
		userData.setOperatorRole(operatorRole);
		return userData;
	}

	private OperatorRole createOperatorRole(AreaPath areaPath, ElectionPath electionPath) {
		OperatorRole operatorRole = new OperatorRole();

		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath.path());
		operatorRole.setMvArea(mvArea);

		MvElection mvElection = new MvElection();
		mvElection.setElectionPath(electionPath.path());
		operatorRole.setMvElection(mvElection);

		Operator operator = new Operator();
		
		ElectionEvent electionEvent = new ElectionEvent(42L);
		
		operator.setElectionEvent(electionEvent);
		operatorRole.setOperator(operator);

		return operatorRole;
	}

}
