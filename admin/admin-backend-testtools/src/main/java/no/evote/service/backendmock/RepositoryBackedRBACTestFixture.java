package no.evote.service.backendmock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.GenericTestRepository;

public class RepositoryBackedRBACTestFixture extends RBACTestFixture {
	protected static final String LOCALE_NB_NO = "nb-NO";
	private EntityManager entityManager;
	private GenericTestRepository genericTestRepository;
	private UserData userData;
	private UserData sysAdminUserData;

	public RepositoryBackedRBACTestFixture(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void init() {
		genericTestRepository = new GenericTestRepository(entityManager);
		setupRbacEnvironment();
		userData = mock(UserData.class);
		OperatorRole mockOperatorRole = mock(OperatorRole.class);
		Role mockRole = mock(Role.class);
		when(mockRole.isUserSupport()).thenReturn(false);
		when(mockOperatorRole.getRole()).thenReturn(mockRole);
		when(userData.getOperatorRole()).thenReturn(mockOperatorRole);
		sysAdminUserData = mock(UserData.class);
		when(sysAdminUserData.getOperatorRole()).thenReturn(getOperatorRoleOperatorRoot());
	}

	@Override
	public ElectionEvent getElectionEventById(String electionEventId) {
		return genericTestRepository.findEntityByProperty(ElectionEvent.class, "id", electionEventId);
	}

	@Override
	public void createRole(Role role) {
		genericTestRepository.createEntity(role);
	}

	@Override
	public Access getAccessByPath(String path) {
		return genericTestRepository.findEntityByProperty(Access.class, "path", path);
	}

	@Override
	public Operator createOperator(UserData userData, Operator operator) {
		return genericTestRepository.createEntity(operator);
	}

	@Override
	public MvElection getSingleMvElectionByPath(String path) {
		return genericTestRepository.findEntityByProperty(MvElection.class, "electionPath", path);
	}

	@Override
	public MvArea getSingleMvAreaByPath(String path) {
		return genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", path);
	}

	@Override
	public void doCreateOperatorRole(UserData userData, OperatorRole operatorRole) {
		genericTestRepository.createEntity(operatorRole);
	}

	@Override
	protected Locale getLocale(String locale) {
		return genericTestRepository.findEntityByProperty(Locale.class, "id", locale);
	}

	public final UserData getUserData() {
		return userData;
	}

	public final UserData getSysAdminUserData() {
		return sysAdminUserData;
	}
}
