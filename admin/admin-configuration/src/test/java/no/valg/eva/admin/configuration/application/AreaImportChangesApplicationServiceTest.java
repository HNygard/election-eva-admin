package no.valg.eva.admin.configuration.application;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent;
import no.valg.eva.admin.common.rbac.UserDataMockups;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.util.IOUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AreaImportChangesApplicationServiceTest extends MockUtilsTestCase {

	private static final String ID_0301 = AreaPath.OSLO_MUNICIPALITY_ID;
	private static final String ID_0101 = "0101";
	private static final String ELECTION_EVENT_ID = "950003";
	private static final String IMPORT_FILE = "valgkretser_areaImportChangeApplicationServiceTest.txt";
	private AreaImportChangesApplicationService areaImportChangesApplicationService;
	private ElectionEventRepository stubElectionEventRepository;
	private MvAreaRepository stubMvAreaRepository;
	private PollingDistrictRepository stubPollingDistrictRepository;
	private PollingPlaceRepository stubPollingPlaceRepository;
	private VoterRepository stubVoterRepository;
	private UserData userDataMock;
	private File file;

	@BeforeMethod
	public void setUp() throws Exception {
		stubElectionEventRepository = stub(ElectionEventRepository.class);
		stubVoterRepository = stub(VoterRepository.class);
		stubMvAreaRepository = stub(MvAreaRepository.class);
		stubPollingDistrictRepository = stub(PollingDistrictRepository.class);
		stubPollingPlaceRepository = stub(PollingPlaceRepository.class);
		userDataMock = UserDataMockups.userData("22067800005", AreaLevelEnum.COUNTRY);
		areaImportChangesApplicationService = initializeMocks(AreaImportChangesApplicationService.class);
		areaImportChangesApplicationService.setElectionEventRepository(stubElectionEventRepository);
		areaImportChangesApplicationService.setMvAreaRepository(stubMvAreaRepository);
		areaImportChangesApplicationService.setPollingDistrictRepository(stubPollingDistrictRepository);
		areaImportChangesApplicationService.setPollingPlaceRepository(stubPollingPlaceRepository);
		areaImportChangesApplicationService.setVoterRepository(stubVoterRepository);
		file = new File(this.getClass().getClassLoader().getResource(IMPORT_FILE).toURI());
		CompositeAuditEvent.initializeForThread();
	}

	@AfterMethod
    public void tearDown() {
		CompositeAuditEvent.clearCollectedEvents();
	}

	@Test
	public void importAreaHierarchyChanges_importFileAreasDifferentiateAreasInDB_createThreeDeleteTwoAndUpdateThreeDistricts() throws Exception {
        when(stubElectionEventRepository.findByPk(any())).thenReturn(buildElectionEvent());
		when(stubMvAreaRepository.findByPathAndLevel(anyString(), anyInt())).thenAnswer(getAnswer());
		when(stubPollingPlaceRepository.findFirstPollingPlace(anyLong())).thenReturn(null);
		when(stubVoterRepository.hasVoters(anyLong())).thenReturn(false);

		areaImportChangesApplicationService.importAreaHierarchyChanges(userDataMock, IOUtil.getBytes(file));

		
		verify(stubPollingDistrictRepository, times(3)).create(any(UserData.class), any(PollingDistrict.class));
		verify(stubPollingDistrictRepository, times(2)).delete(any(UserData.class), anyLong());
		verify(stubPollingDistrictRepository, times(3)).update(any(UserData.class), any(PollingDistrict.class));
		
	}

	private ElectionEvent buildElectionEvent() {
		ElectionEvent ee = new ElectionEvent();
		ee.setId(ELECTION_EVENT_ID);
		return ee;
	}

	private Answer<List<MvArea>> getAnswer() {
		return new Answer<List<MvArea>>() {
			@Override
            public List<MvArea> answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				List<MvArea> mvaList = new ArrayList<>();
				if (args[0].toString().equals("950003.47")) {
					return stubMvAreaList();
				} else if (args[1].equals(AreaLevelEnum.BOROUGH.getLevel())) {
					
					mvaList.add(findMvArea(args[0].toString().substring(0, 24)));
					
				}
				mvaList.add(findMvArea(args[0].toString()));
				return mvaList;
			}

			private MvArea findMvArea(String path) {

				switch (path) {

				case "950003.47.01.0101.010100.0000":
					return stubPollingDistrictMvArea(ID_0101, "0000", "Hele kommunen");

				case "950003.47.01.0101.010100.0001":
					return stubPollingDistrictMvArea(ID_0101, "0001", "Halden");

				case "950003.47.01.0101.010100.0002":
					return stubPollingDistrictMvArea(ID_0101, "0002", "Hjortsberg");

				case "950003.47.01.0101.010100":
					return stubBoroughMvArea(ID_0101, "010100");

				case "950003.47.03.0301.030101.0101":
					return stubPollingDistrictMvArea(ID_0301, "0101", "Vahl skole");

				case "950003.47.03.0301.030101.0102":
					return stubPollingDistrictMvArea(ID_0301, "0102", "Kampen skole");

				case "950003.47.03.0301.030101.0107":
					return stubPollingDistrictMvArea(ID_0301, "0107", "Etterstad vg skole");

				case "950003.47.03.0301.030101":
					return stubBoroughMvArea(ID_0301, "030101");

				case "950003.47.03.0301.030103.0301":
					return stubPollingDistrictMvArea(ID_0301, ID_0301, "Sagene samfunnshus");

				case "950003.47.03.0301.030103":
					return stubBoroughMvArea(ID_0301, "030103");

				case "950003.47.03.0301.030103.0303":
					return stubPollingDistrictMvArea(ID_0301, "0303", "Deichmanske bibliotek");

				case "950003.47.03.0301.030103.0304":
					return stubPollingDistrictMvArea(ID_0301, "0304", "Sagene festivitetsh.");

				default:
					break;
				}
				return null;
			}
		};
	}

	private MvArea stubMvArea(String knr, String boroughId) {
		MvArea mva = new MvArea();
		mva.setElectionEventId(ELECTION_EVENT_ID);
		mva.setCountryId("47");
		mva.setCountyId(knr.substring(0, 2));
		mva.setMunicipalityId(knr);
		mva.setBoroughId(boroughId);
		mva.setBorough(new Borough());
		return mva;
	}

	private MvArea stubBoroughMvArea(String knr, String boroughId) {
		return stubMvArea(knr, boroughId);
	}

	private MvArea stubPollingDistrictMvArea(String knr, String valgkrets, String navn) {
		MvArea mva = stubMvArea(knr, knr + valgkrets.substring(0, 2));
		mva.setPollingDistrictId(valgkrets);
		mva.setPollingDistrictName(navn);
		PollingDistrict pd = new PollingDistrict();
		
		pd.setPk(12L);
		
		mva.setPollingDistrict(pd);
		return mva;
	}

	private List<MvArea> stubMvAreaList() {
		List<MvArea> mvAreaList = new ArrayList<>();
		mvAreaList.add(stubPollingDistrictMvArea(ID_0101, "0000", "Uoppgitt"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0101, "0001", "Halden"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0101, "0002", "Hjortsberg Hagan"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0101", "Vahl skole"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0102", "Kampen skole"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0104", "Gamlebyen skole skal slettes"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0107", "Etterstad vg skole"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, ID_0301, "Sagene sammfunnshus"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0303", "Deichmanske bibliotek"));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0304", "Sagene festivitetsh."));
		mvAreaList.add(stubPollingDistrictMvArea(ID_0301, "0304", "Sagene skole skal slettes"));
		return mvAreaList;
	}
	
	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@area.import.error_empty")
	public void importAreaHierarchyChanges_withEmptyFile_throwsEmptyFileException() throws Exception {
		AreaImportChangesApplicationService service = initializeMocks(AreaImportChangesApplicationService.class);
		
		service.importAreaHierarchyChanges(createMock(UserData.class), new byte[0]);
	}

}
