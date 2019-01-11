package no.evote.service.backendmock;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Se;
import static no.valg.eva.admin.common.rbac.Accesses.Parti;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

import org.apache.log4j.Logger;
import org.testng.Assert;

/**
 * Provides a test fixture to simplify testing with role-based access control (RBAC).
 */

public abstract class RBACTestFixture {
	public static final String OPERATOR_FIRST_NAME = "Operator";
	public static final String OPERATOR_MIDDLE_NAME = "Op";
	public static final String OPERATOR_LAST_NAME = "Operatorsen";
	public static final String OPERATOR_NAME_LINE = "Operatorsen Operator Op";
	public static final String OPERATOR_ADDRESS_LINE_1 = "Gateveien 123C";
	public static final String OPERATOR_ADDRESS_LINE_2 = "Adresselinje2";
	public static final String OPERATOR_ADDRESS_LINE_3 = "Adresselinje3";
	public static final String OPERATOR_EMAIL = "test@test.com";
	public static final String OPERATOR_POST_CODE = "0000";
	public static final String OPERATOR_POST_TOWN = "Stedberget";
	public static final String OPERATOR_TELEPHONE = "654456645";
	public static final String ROOT_LITERAL = "root";
	public static final String PATH_ADMIN_EVENT = "000000";
	public static final String PATH_ELECTION_EVENT_VALG07 = "200701";
	public static final String PATH_COUNTY_SORTRONDELAG = "200701.47.16";
	public static final String PATH_COUNTY_TROMS = "200701.47.19";
	public static final String PATH_MUNICIPALITY_TYDAL = "200701.47.16.1665";
	public static final String PATH_POLLING_DISTRICT_IN_TYDAL = "200701.47.16.1665.166500.0001";
	public static final String PATH_MUNICIPALITY_OSLO = "200701.47.03.0301";
	public static final String PATH_ELECTION_GROUP_01 = "200701.01";
	public static final String LOCALE_NB_NO = "nb-NO";
	public static final String OPERATOR_ID_09058611112 = "09058611112";
	public static final String ROLE_NAME_VOTING_COUNT_ELECTION_DAY_ALL = "vtngCntElctnDyAll";
	public static final String ROLE_NAME_VOTING_COUNT = "roleVotingCount";
	private final Logger log = Logger.getLogger(RBACTestFixture.class);
	private Operator operator;
	private Operator operatorRoot;
	private Operator operator1;
	private Operator operator11;
	private Operator operator1111;
	private Operator operator2;
	private Operator operator3;

	private OperatorRole operatorRoleRootVotingCount;
	private OperatorRole operatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag;

	private OperatorRole operatorRoleOperator11Empty;
	private OperatorRole operatorRoleOperator1111VotingCountElectionDayAllInTydalPollingDistrict;
	private OperatorRole operatorRoleOperator2VotingCountElectionDayApproveInTroms;
	private OperatorRole operatorRoleOperator3VotingCountElectionDayReadInSorTrondelag;
	private OperatorRole operatorRoleOperator2VotingCountElectionDayApproveInOslo;
	private OperatorRole operatorRoleOperator2VotingCountInTroms;

	private Access accessVotingCount;
	private Access accessVotingCountElectionDayRead;
	private Access accessVotingCountElectionDayApprove;
	private Access accessParty;

	private Role roleRoot;
	private Role roleVotingCount;
	private Role rolePartyTVoteCountVotingCountElectionDayAll;
	private Role roleVotingCountElectionDayAll;
	private Role roleVotingCountElectionDayRead;
	private Role roleVotingCountElectionDayApprove;
	private Role roleEmpty;

	private OperatorRole operatorRoleOperatorRoot;

	public RBACTestFixture() {
	}

	public void init() {
		setupRbacEnvironment();
	}

	protected void setupRbacEnvironment() {
		createOperators();
		createAccesses();
		createRoles();
		createOperatorRoles();
	}

	private void createRoles() {
		Set<Access> accesses = new HashSet<>();
		accesses.add(accessVotingCountElectionDayRead);
		Set<Role> includedRoles = new HashSet<>();
		roleVotingCountElectionDayRead = createRole("vtngCntLctnDyRd", "vtngCntLctnDyRd", getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses,
				includedRoles, 3, false);

		accesses = new HashSet<>();
		accesses.add(accessVotingCountElectionDayApprove);
		includedRoles = new HashSet<>();
		roleVotingCountElectionDayApprove = createRole("vtngCntLctnDyApprv", "vtngCntLctnDyApprv", getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses,
				includedRoles, 4, true);

		accesses = new HashSet<>();
		includedRoles = new HashSet<>();
		roleEmpty = createRole("roleEmpty", "roleEmpty", getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses, includedRoles, 3, false);

		accesses = new HashSet<>();
		includedRoles = new HashSet<>();
		includedRoles.add(roleVotingCountElectionDayApprove);
		includedRoles.add(roleVotingCountElectionDayRead);
		roleVotingCountElectionDayAll = createRole(ROLE_NAME_VOTING_COUNT_ELECTION_DAY_ALL, ROLE_NAME_VOTING_COUNT_ELECTION_DAY_ALL,
				getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses, includedRoles, 3, false);

		accesses = new HashSet<>();
		accesses.add(accessVotingCount);
		includedRoles = new HashSet<>();
		includedRoles.add(roleEmpty);
		roleVotingCount = createRole(ROLE_NAME_VOTING_COUNT, ROLE_NAME_VOTING_COUNT, getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses, includedRoles,
				3, false);

		accesses = new HashSet<>();
		accesses.add(accessParty);
		includedRoles = new HashSet<>();
		includedRoles.add(roleVotingCountElectionDayAll);
		rolePartyTVoteCountVotingCountElectionDayAll = createRole("prtyTVtCntAll", "prtyTVtCntAll", getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses,
				includedRoles, 3, false);

		accesses = new HashSet<>();
		includedRoles = new HashSet<>();
		includedRoles.add(roleVotingCount);
		includedRoles.add(rolePartyTVoteCountVotingCountElectionDayAll);
		roleRoot = createRole(ROOT_LITERAL, ROOT_LITERAL, getElectionEventById(PATH_ELECTION_EVENT_VALG07), accesses, includedRoles, 3, false);
	}

	protected abstract ElectionEvent getElectionEventById(String electionEventId);

	protected Role createRole(final String id, final String name, final ElectionEvent electionEvent, final Set<Access> accesses, final Set<Role> includedRoles,
			final Integer securityLevel, final Boolean mutuallyExclusive) {
		Role role = new Role();
		role.setId(id);
		role.setName(name);
		role.setActive(true);
		role.setElectionEvent(electionEvent);
		role.setAccesses(accesses);
		role.setIncludedRoles(includedRoles);
		role.setSecurityLevel(securityLevel);
		role.setMutuallyExclusive(mutuallyExclusive);
		createRole(role);
		return role;
	}

	protected abstract void createRole(Role role);

	private void createAccesses() {
		accessParty = getAccessByPath(Parti.paths()[0]);
		accessVotingCount = getAccessByPath(Opptelling.paths()[0]);
		accessVotingCountElectionDayApprove = getAccessByPath(Opptelling_Valgting_Rediger.paths()[0]);
		accessVotingCountElectionDayRead = getAccessByPath(Opptelling_Valgting_Se.paths()[0]);
	}

	protected abstract Access getAccessByPath(String path);

	private void createOperators() {
		operator = createOperator("12058711150", OPERATOR_FIRST_NAME, PATH_ELECTION_EVENT_VALG07);
		operatorRoot = createOperator("04058611137", OPERATOR_FIRST_NAME, PATH_ELECTION_EVENT_VALG07);
		operator1 = createOperator("05058611176", OPERATOR_FIRST_NAME, PATH_ELECTION_EVENT_VALG07);
		operator2 = createOperator(OPERATOR_ID_09058611112, "operatortva", PATH_ELECTION_EVENT_VALG07);
		operator3 = createOperator(OPERATOR_ID_09058611112, "operatortre", PATH_ADMIN_EVENT);
		operator11 = createOperator("06058611105", OPERATOR_FIRST_NAME, PATH_ELECTION_EVENT_VALG07);
		operator1111 = createOperator("07058611144", OPERATOR_FIRST_NAME, PATH_ELECTION_EVENT_VALG07);

		createOperator(getUserData(), operator);
		createOperator(getUserData(), operatorRoot);
		createOperator(getUserData(), operator1);
		createOperator(getUserData(), operator2);
		createOperator(getUserData(), operator3);
		createOperator(getUserData(), operator11);
		createOperator(getUserData(), operator1111);
	}

	public abstract UserData getUserData();

	public abstract Operator createOperator(UserData userData, Operator operator);

	public Operator createOperator(final String id, final String firstName, final String electionEventId) {
		Operator newOperator = new Operator();
		newOperator.setId(id);
		newOperator.setFirstName(firstName);
		newOperator.setActive(true);
		newOperator.setMiddleName(OPERATOR_MIDDLE_NAME);
		newOperator.setLastName(OPERATOR_LAST_NAME);
		newOperator.setNameLine(OPERATOR_NAME_LINE);
		newOperator.setAddressLine1(OPERATOR_ADDRESS_LINE_1);
		newOperator.setAddressLine2(OPERATOR_ADDRESS_LINE_2);
		newOperator.setAddressLine3(OPERATOR_ADDRESS_LINE_3);
		newOperator.setEmail(OPERATOR_EMAIL);
		newOperator.setPostalCode(OPERATOR_POST_CODE);
		newOperator.setPostTown(OPERATOR_POST_TOWN);
		newOperator.setTelephoneNumber(OPERATOR_TELEPHONE);
		newOperator.setElectionEvent(getElectionEventById(electionEventId));
		return newOperator;
	}

	private void createOperatorRoles() {
		operatorRoleOperatorRoot = createOperatorRole(getSingleMvAreaByPath(PATH_ADMIN_EVENT), getSingleMvElectionByPath(PATH_ADMIN_EVENT), operator, roleRoot);
		createOperatorRole(getSingleMvAreaByPath(PATH_ADMIN_EVENT), getSingleMvElectionByPath(PATH_ADMIN_EVENT), operator,
				roleVotingCount);
		createOperatorRole(getSingleMvAreaByPath(PATH_ADMIN_EVENT),
				getSingleMvElectionByPath(PATH_ADMIN_EVENT), operator, roleVotingCountElectionDayRead);

		operatorRoleRootVotingCount = createOperatorRole(getSingleMvAreaByPath(PATH_ELECTION_EVENT_VALG07),
				getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07), operatorRoot, roleVotingCount);
		operatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag = createOperatorRole(getSingleMvAreaByPath(PATH_COUNTY_SORTRONDELAG),
				getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07), operator1, rolePartyTVoteCountVotingCountElectionDayAll);
		operatorRoleOperator11Empty = createOperatorRole(getSingleMvAreaByPath(PATH_MUNICIPALITY_TYDAL), getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07),
				operator11, roleEmpty);
		operatorRoleOperator1111VotingCountElectionDayAllInTydalPollingDistrict = createOperatorRole(getSingleMvAreaByPath(PATH_POLLING_DISTRICT_IN_TYDAL),
				getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07), operator1111, roleVotingCountElectionDayAll);
		operatorRoleOperator3VotingCountElectionDayReadInSorTrondelag = createOperatorRole(getSingleMvAreaByPath(PATH_COUNTY_SORTRONDELAG),
				getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07), operator3, roleVotingCountElectionDayRead);

		operatorRoleOperator2VotingCountElectionDayApproveInTroms = createOperatorRole(getSingleMvAreaByPath(PATH_COUNTY_TROMS),
				getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07), operator2, roleVotingCountElectionDayApprove);
		operatorRoleOperator2VotingCountInTroms = createOperatorRole(getSingleMvAreaByPath(PATH_COUNTY_TROMS),
				getSingleMvElectionByPath(PATH_ELECTION_EVENT_VALG07), operator2, roleVotingCount);
		operatorRoleOperator2VotingCountElectionDayApproveInOslo = createOperatorRole(getSingleMvAreaByPath(PATH_MUNICIPALITY_OSLO),
				getSingleMvElectionByPath(PATH_ELECTION_GROUP_01), operator2, roleVotingCountElectionDayApprove);
	}

	protected abstract MvElection getSingleMvElectionByPath(String path);

	protected abstract MvArea getSingleMvAreaByPath(String path);

	protected OperatorRole createOperatorRole(final MvArea mvArea, final MvElection mvElection, final Operator operator, final Role role) {
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setMvArea(mvArea);
		operatorRole.setMvElection(mvElection);
		operatorRole.setOperator(operator);
		operatorRole.setRole(role);

		if (mvArea.getAreaPath().startsWith(PATH_ELECTION_EVENT_VALG07)) {
			doCreateOperatorRole(getUserData(), operatorRole);
		} else if (mvArea.getAreaPath().startsWith(PATH_ADMIN_EVENT)) {
			doCreateOperatorRole(getSysAdminUserData(), operatorRole);
		} else {
			Assert.assertTrue(false);
		}

		return operatorRole;
	}

	public abstract UserData getSysAdminUserData();

	protected abstract void doCreateOperatorRole(UserData userData, OperatorRole operatorRole);

	public Role getRolePartyTVoteCountVotingCountElectionDayAll() {
		return rolePartyTVoteCountVotingCountElectionDayAll;
	}

	public Role getRoleVotingCountElectionDayAll() {
		return roleVotingCountElectionDayAll;
	}

	public Role getRoleVotingCountElectionDayApprove() {
		return roleVotingCountElectionDayApprove;
	}

	public Role getRoleVotingCountElectionDayRead() {
		return roleVotingCountElectionDayRead;
	}

	public Role getRoleVotingCount() {
		return roleVotingCount;
	}

	public Operator getOperator2() {
		return operator2;
	}

	public Operator getOperator1() {
		return operator1;
	}

	public Operator getOperator() {
		return operator;
	}

	public Role getRoleEmpty() {
		return roleEmpty;
	}

	public Access getAccessVotingCount() {
		return accessVotingCount;
	}

	public Role getRoleRoot() {
		return roleRoot;
	}

	public OperatorRole getOperatorRoleOperatorRoot() {
		return operatorRoleOperatorRoot;
	}

	public OperatorRole getOperatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag() {
		return operatorRoleOperator1PartyTVoteCountElectionDayAllInSorTrondelag;
	}

	public OperatorRole getOperatorRoleOperator2VotingCountElectionDayApproveInTroms() {
		return operatorRoleOperator2VotingCountElectionDayApproveInTroms;
	}

	public OperatorRole getOperatorRoleOperator2VotingCountInTroms() {
		return operatorRoleOperator2VotingCountInTroms;
	}

	public OperatorRole getOperatorRoleOperator11Empty() {
		return operatorRoleOperator11Empty;
	}

	public OperatorRole getOperatorRoleOperator1111VotingCountElectionDayAllInTydalPollingDistrict() {
		return operatorRoleOperator1111VotingCountElectionDayAllInTydalPollingDistrict;
	}

	public OperatorRole getOperatorRoleOperator3VotingCountElectionDayReadInSorTrondelag() {
		return operatorRoleOperator3VotingCountElectionDayReadInSorTrondelag;
	}

	public OperatorRole getOperatorRoleOperator2VotingCountElectionDayApproveInOslo() {
		return operatorRoleOperator2VotingCountElectionDayApproveInOslo;
	}

	public OperatorRole getOperatorRoleRootVotingCount() {
		return operatorRoleRootVotingCount;
	}

	public final UserData getUserData(final String uid) {
		UserData userData = new UserData();
		OperatorRole operatorRole = getOperatorRoleOperatorRoot();
		userData.setOperatorRole(operatorRole);
		userData.setSecurityLevel(3);
		try {
			userData.setClientAddress(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			log.error(e);
		}
		userData.setLocale(getLocale(LOCALE_NB_NO));
		userData.setUid(uid);

		return userData;

	}

	protected abstract Locale getLocale(String locale);
}

