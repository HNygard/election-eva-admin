package no.evote.service.backendmock;

import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleServiceBean;
import no.evote.service.rbac.RoleServiceBean;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.test.GenericTestRepository;

public class ServiceBackedRBACTestFixture extends RBACTestFixture {
	private final BaseTestFixture baseTextFixture;
	private OperatorRoleServiceBean operatorRoleService;
	private OperatorRepository operatorRepository;
	private ElectionEventRepository electionEventRepository;
	private MvAreaRepository mvAreaRepository;
	private MvElectionRepository mvElectionRepository;
	private RoleServiceBean roleService;
	private AccessRepository accessRepository;
	private GenericTestRepository genericTestRepository;
	private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

	public ServiceBackedRBACTestFixture(
			LegacyUserDataServiceBean userDataService,
			OperatorRoleServiceBean operatorRoleService,
			OperatorRepository operatorRepository,
			ElectionEventRepository electionEventRepository,
			AccessRepository accessRepository,
			MvAreaRepository mvAreaRepository,
			MvElectionRepository mvElectionRepository,
			RoleServiceBean roleService,
			GenericTestRepository genericTestRepository,
			TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
		this.operatorRoleService = operatorRoleService;
		this.operatorRepository = operatorRepository;
		this.electionEventRepository = electionEventRepository;
		this.accessRepository = accessRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.mvElectionRepository = mvElectionRepository;
		this.roleService = roleService;
		this.genericTestRepository = genericTestRepository;
		this.baseTextFixture = new BaseTestFixture(userDataService, accessRepository);
		this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
	}

	public ServiceBackedRBACTestFixture(BackendContainer backend) {
		this(backend.getUserDataService(), backend.getOperatorRoleService(), backend.getOperatorRepository(),
				backend.getElectionEventRepository(), backend.getAccessRepository(), backend.getMvAreaRepository(), backend.getMvElectionRepository(),
				backend.getRoleService(), backend.getGenericTestRepository(), backend.getTransactionSynchronizationRegistry());
	}

	@Override
	public void init() {
		baseTextFixture.init();
		super.init();
	}

	@Override
	public ElectionEvent getElectionEventById(String electionEventId) {
		return electionEventRepository.findById(electionEventId);
	}

	@Override
	public void createRole(Role role) {
		roleService.create(getUserData(), role, false);
	}

	@Override
	public Access getAccessByPath(String path) {
		return accessRepository.findAccessByPath(path);
	}

	@Override
	public Operator createOperator(UserData userData, Operator operator) {
		return operatorRepository.create(userData, operator);
	}

	@Override
	public MvElection getSingleMvElectionByPath(String path) {
		if (path == null) {
			return null;
		}
		return mvElectionRepository.finnEnkeltMedSti(ElectionPath.from(path).tilValghierarkiSti());
	}

	@Override
	public MvArea getSingleMvAreaByPath(String path) {
		return mvAreaRepository.findSingleByPath(path != null ? AreaPath.from(path) : null);
	}

	@Override
	public void doCreateOperatorRole(UserData userData, OperatorRole operatorRole) {
		operatorRoleService.create(userData, operatorRole);
	}

	@Override
	protected Locale getLocale(String locale) {
		return genericTestRepository.findEntityByProperty(Locale.class, "id", locale);
	}

	public final UserData getUserData() {
		return baseTextFixture.getUserData();
	}

	public final UserData getSysAdminUserData() {
		return baseTextFixture.getSysAdminUserData();
	}

	public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
		return transactionSynchronizationRegistry;
	}
}
