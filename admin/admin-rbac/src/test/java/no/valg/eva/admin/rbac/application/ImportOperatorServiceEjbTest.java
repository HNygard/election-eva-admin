package no.valg.eva.admin.rbac.application;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.rbac.EarlyVoteReceiver;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.VoteReceiver;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.rbac.service.ImportOperatorServiceEjb;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ImportOperatorServiceEjbTest {

	private static final String AREA_PATH_MUNICIPALITY = "201500.47.02.0201";
	private static final String AREA_PATH_POLLING_DISTRICT = "201500.47.02.0201.020101.0104";
	private static final String AREA_PATH_POLLING_PLACE = "201500.47.02.0201.020100.0000.0002";
	private static final String POLLING_PLACE_ID = "0002";
	private static final String POLLING_DISTRICT_ID = "0104";
	private static final Long MUNICIPALITY_PK = 102L;

	@Mock
	private MvAreaRepository mvAreaRepository;
	@Mock
	private RoleRepository roleRepository;

	@Mock
	private OperatorRoleRepository operatorRoleRepository;

	@Mock
	private OperatorRepository operatorRepository;

	@Mock
	private ElectionEventRepository electionEventRepository;

	@Mock
	private VoterRepository voterRepository;

	@Mock
	private UserData userData;

	private ImportOperatorServiceEjb importOperatorServiceEjb;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void init() {
		importOperatorServiceEjb = new ImportOperatorServiceEjb();

		mvAreaRepository = mock(MvAreaRepository.class);
		importOperatorServiceEjb.setMvAreaService(mvAreaRepository);

		roleRepository = mock(RoleRepository.class);
		importOperatorServiceEjb.setRoleRepository(roleRepository);

		operatorRoleRepository = mock(OperatorRoleRepository.class);
		importOperatorServiceEjb.setOperatorRoleRepository(operatorRoleRepository);

		operatorRepository = mock(OperatorRepository.class);
		importOperatorServiceEjb.setOperatorRepository(operatorRepository);

		electionEventRepository = mock(ElectionEventRepository.class);
		importOperatorServiceEjb.setElectionEventRepository(electionEventRepository);

		voterRepository = mock(VoterRepository.class);
		importOperatorServiceEjb.setVoterRepository(voterRepository);

		userData = mockUserData();
		mockServices();
	}

	@Test
	public void testImportEarlyVoteReceiverOperator() {
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = mockAdvancedVotingOperators();
		importOperatorServiceEjb.importEarlyVoteReceiverOperator(userData, earlyVoteReceiverList);
		verify(operatorRoleRepository, times(1)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(1)).create(any(UserData.class), any(Operator.class));
		verify(mvAreaRepository, times(1)).findSingleByPollingPlaceIdAndMunicipalityPk(POLLING_PLACE_ID, MUNICIPALITY_PK);
	}

	@Test
	public void testImportEarlyVoteReceiverOperatorPollingPlaceNotGiven() {
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = mockAdvancedVotingOperatorsPPNotGiven();
		importOperatorServiceEjb.importEarlyVoteReceiverOperator(userData, earlyVoteReceiverList);
		verify(operatorRoleRepository, times(1)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(1)).create(any(UserData.class), any(Operator.class));
		verify(mvAreaRepository, times(0)).findSingleByPollingPlaceIdAndMunicipalityPk(anyString(), anyLong());
		verify(mvAreaRepository, times(1)).findByPathAndLevel(AREA_PATH_MUNICIPALITY, AreaLevelEnum.MUNICIPALITY.getLevel());
	}

	@Test
	public void testImportEarlyVoteReceiverOperatorWithAlreadyExistingOperatorRole() {
		mockExistingOperatorRoleService();
		mockExistingOperatorService();
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = mockAdvancedVotingOperators();
		importOperatorServiceEjb.importEarlyVoteReceiverOperator(userData, earlyVoteReceiverList);
		verify(operatorRoleRepository, times(0)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(0)).create(any(UserData.class), any(Operator.class));
		verify(mvAreaRepository, times(1)).findSingleByPollingPlaceIdAndMunicipalityPk(POLLING_PLACE_ID, MUNICIPALITY_PK);
	}

	@Test
	public void testImportEarlyVoteReceiverOperatorWithAlreadyExistingOperator() {
		mockExistingOperatorService();
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = mockAdvancedVotingOperators();
		importOperatorServiceEjb.importEarlyVoteReceiverOperator(userData, earlyVoteReceiverList);
		verify(operatorRoleRepository, times(1)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(0)).create(any(UserData.class), any(Operator.class));
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testImportEarlyVoteReceiverOperatorWithoutName() {
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = mockAdvancedVotingOperatorsWithoutSureName();
		importOperatorServiceEjb.importEarlyVoteReceiverOperator(userData, earlyVoteReceiverList);
		verify(operatorRepository, times(0)).create(any(UserData.class), any(Operator.class));
	}

	@Test
	public void testImportEarlyVoteReceiverOperatorWithElectoralRollEntry() {
		mockExistingVoterService();
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = mockAdvancedVotingOperatorsWithoutSureName();
		importOperatorServiceEjb.importEarlyVoteReceiverOperator(userData, earlyVoteReceiverList);
		verify(operatorRepository, times(1)).create(any(UserData.class), any(Operator.class));
	}

	@Test
	public void testImportVotingAndPollingPlaceResponsibleOperator() {
		List<VoteReceiver> votingOperatorList = mockVotingOperators();
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = mockPollingPlaceResponsibleOperators();
		importOperatorServiceEjb.importVotingAndPollingPlaceResponsibleOperators(userData, votingOperatorList, pollingPlaceResponsibleList);
		verify(operatorRoleRepository, times(2)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(2)).create(any(UserData.class), any(Operator.class));
		verify(mvAreaRepository, times(2)).findSingleByPollingDistrictIdAndMunicipalityPk(POLLING_DISTRICT_ID, MUNICIPALITY_PK);
	}

	@Test
	public void testImportVotingAndPollingPlaceResponsibleOperatorWithAlreadyExistingOperatorRole() {
		mockExistingOperatorRoleService();
		mockExistingOperatorService();
		List<VoteReceiver> votingOperatorList = mockVotingOperators();
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = mockPollingPlaceResponsibleOperators();
		importOperatorServiceEjb.importVotingAndPollingPlaceResponsibleOperators(userData, votingOperatorList, pollingPlaceResponsibleList);
		verify(operatorRoleRepository, times(0)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(0)).create(any(UserData.class), any(Operator.class));
	}

	@Test
	public void testImportPollingPlaceResponsibleOperatorWithAlreadyExistingOperator() {
		mockExistingOperatorService();
		List<VoteReceiver> votingOperatorList = mockVotingOperators();
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = mockPollingPlaceResponsibleOperators();
		importOperatorServiceEjb.importVotingAndPollingPlaceResponsibleOperators(userData, votingOperatorList, pollingPlaceResponsibleList);
		verify(operatorRoleRepository, times(2)).create(any(UserData.class), any(OperatorRole.class));
		verify(operatorRepository, times(0)).create(any(UserData.class), any(Operator.class));
	}

	@Test(expectedExceptions = EvoteException.class)
	public void testImportPollingPlaceResponsibleOperatorWithoutName() {
		List<VoteReceiver> votingOperatorList = mockVotingOperators();
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = mockPollingPlaceResponsibleOperatorsWithoutFirstname();
		importOperatorServiceEjb.importVotingAndPollingPlaceResponsibleOperators(userData, votingOperatorList, pollingPlaceResponsibleList);
	}

	@Test
	public void testImportPollingPlaceResponsibleOperatorWithElectoralRollEntry() {
		mockExistingVoterService();
		List<VoteReceiver> votingOperatorList = mockVotingOperators();
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = mockPollingPlaceResponsibleOperatorsWithoutFirstname();
		importOperatorServiceEjb.importVotingAndPollingPlaceResponsibleOperators(userData, votingOperatorList, pollingPlaceResponsibleList);
		verify(operatorRepository, times(2)).create(any(UserData.class), any(Operator.class));
	}

	private void mockExistingOperatorRoleService() {
		when(operatorRoleRepository.findUnique(any(Role.class), any(Operator.class), any(MvArea.class), any(MvElection.class))).thenReturn(mockOperatorRole());
	}

	private void mockExistingOperatorService() {
		when(operatorRepository.findByElectionEventsAndId(anyLong(), anyString())).thenReturn(mockOperator());
	}

	private void mockExistingVoterService() {
		when(voterRepository.voterOfId(anyString(), anyLong())).thenReturn(mockVoter());
	}

	private void mockServices() {
		when(voterRepository.voterOfId(anyString(), anyLong())).thenReturn(null);
		when(mvAreaRepository.findByPathAndLevel(anyString(), anyInt())).thenReturn(mockMvAreaList());
		when(mvAreaRepository.findSingleByPollingPlaceIdAndMunicipalityPk(anyString(), anyLong())).thenReturn(mockMvArea(AREA_PATH_POLLING_PLACE,
				AreaLevelEnum.POLLING_PLACE.getLevel()));
		when(mvAreaRepository.findSingleByPollingDistrictIdAndMunicipalityPk(anyString(), anyLong())).thenReturn(mockMvArea(AREA_PATH_POLLING_DISTRICT,
				AreaLevelEnum.POLLING_DISTRICT.getLevel()));
		when(electionEventRepository.findByPk(anyLong())).thenReturn(mockElectionEvent());
		when(operatorRepository.findByElectionEventsAndId(anyLong(), anyString())).thenReturn(null);
		when(operatorRoleRepository.findUnique(any(Role.class), any(Operator.class), any(MvArea.class), any(MvElection.class))).thenReturn(null);
		when(operatorRepository.create(any(UserData.class), any(Operator.class))).thenReturn(mockOperator());
		when(operatorRoleRepository.create(any(UserData.class), any(OperatorRole.class))).thenReturn(mockOperatorRole());
		when(roleRepository.findByElectionEventAndId(any(ElectionEvent.class), anyString())).thenReturn(mockRole());
	}

	private UserData mockUserData() {
		UserData userData = new UserData();
		userData.setSecurityLevel(4);
		userData.setUid("01027812345");
		OperatorRole userDataOperatorRole = mockOperatorRole();
		userData.setOperatorRole(userDataOperatorRole);
		return userData;
	}

	private Voter mockVoter() {
		Voter electoralRollVoter = new Voter();
		electoralRollVoter.setFirstName("Donald");
		electoralRollVoter.setLastName("Duck");
		return electoralRollVoter;
	}

	private Role mockRole() {
		return new Role();
	}

	private List<MvArea> mockMvAreaList() {
		List<MvArea> mvaList = new ArrayList<>();
		mvaList.add(mockMvArea(AREA_PATH_MUNICIPALITY, AreaLevelEnum.MUNICIPALITY.getLevel()));
		return mvaList;
	}

	private OperatorRole mockOperatorRole() {
		OperatorRole opr = new OperatorRole();
		opr.setMvElection(mockMvElection());
		opr.setMvArea(mockMvArea(AREA_PATH_MUNICIPALITY, AreaLevelEnum.MUNICIPALITY.getLevel()));
		opr.setRole(mockRole());
		opr.setOperator(mockOperator());
		return opr;
	}

	private MvArea mockMvArea(String path, int areaLevel) {
		MvArea mva = new MvArea();
		mva.setPk(11L);
		mva.setMunicipalityId("0201");
		mva.setMunicipality(mockMunicipality());
		mva.setAreaPath(path);
		mva.setAreaLevel(areaLevel);
		return mva;
	}

	private Municipality mockMunicipality() {
		Municipality mun = new Municipality();
		mun.setId("0201");
		mun.setPk(MUNICIPALITY_PK);
		return mun;
	}

	private MvElection mockMvElection() {
		MvElection mve = new MvElection();
		mve.setPk(12L);
		return mve;
	}

	private ElectionEvent mockElectionEvent() {
		ElectionEvent ee = new ElectionEvent();
		ee.setPk(13L);
		ee.setId("13");
		return ee;
	}

	private Operator mockOperator() {
		Operator operator = new Operator();
		operator.setElectionEvent(mockElectionEvent());
		operator.setId("05056512345");
		return operator;
	}

	private List<ImportOperatorRoleInfo> mockAdvancedVotingOperators() {
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = new ArrayList<>();
		EarlyVoteReceiver avo = new EarlyVoteReceiver(
				"05056512345",
				"Albert",
				"Aaberg",
				"aa@barnetv.no",
				"12345678",
				POLLING_PLACE_ID);
		earlyVoteReceiverList.add(avo);
		return earlyVoteReceiverList;
	}

	private List<ImportOperatorRoleInfo> mockAdvancedVotingOperatorsPPNotGiven() {
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = new ArrayList<>();
		EarlyVoteReceiver avo = new EarlyVoteReceiver(
				"05056512345",
				"Albert",
				"Aaberg",
				"aa@barnetv.no",
				"12345678",
				"");
		earlyVoteReceiverList.add(avo);
		return earlyVoteReceiverList;
	}

	private List<ImportOperatorRoleInfo> mockAdvancedVotingOperatorsWithoutSureName() {
		List<ImportOperatorRoleInfo> earlyVoteReceiverList = new ArrayList<>();
		EarlyVoteReceiver avo = new EarlyVoteReceiver(
				"05056512345",
				"Albert",
				"",
				"aa@barnetv.no",
				"12345678",
				POLLING_PLACE_ID);
		earlyVoteReceiverList.add(avo);
		return earlyVoteReceiverList;
	}

	private List<PollingPlaceResponsibleOperator> mockPollingPlaceResponsibleOperators() {
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = new ArrayList<>();
		PollingPlaceResponsibleOperator pprOp = new PollingPlaceResponsibleOperator(
				"05056512345",
				"Albert",
				"Aaberg",
				"aa@barnetv.no",
				"12345678",
				POLLING_DISTRICT_ID);
		pollingPlaceResponsibleList.add(pprOp);
		return pollingPlaceResponsibleList;
	}

	private List<PollingPlaceResponsibleOperator> mockPollingPlaceResponsibleOperatorsWithoutFirstname() {
		List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleList = new ArrayList<>();
		PollingPlaceResponsibleOperator pprOp = new PollingPlaceResponsibleOperator(
				"05056512345",
				"",
				"Aaberg",
				"aa@barnetv.no",
				"12345678",
				POLLING_DISTRICT_ID);
		pollingPlaceResponsibleList.add(pprOp);
		return pollingPlaceResponsibleList;
	}

	private List<VoteReceiver> mockVotingOperators() {
		List<VoteReceiver> voList = new ArrayList<>();
		VoteReceiver vo = new VoteReceiver(
				"05056512345",
				"Albert",
				"Aaberg",
				"aa@barnetv.no",
				"12345678",
				POLLING_DISTRICT_ID);
		voList.add(vo);
		return voList;
	}

}

