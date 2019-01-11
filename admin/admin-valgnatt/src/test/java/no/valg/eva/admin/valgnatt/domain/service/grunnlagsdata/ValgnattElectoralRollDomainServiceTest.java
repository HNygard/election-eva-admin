package no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.valgnatt.ValgnattElectoralRollRepository;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.ElectoralRollCountReport;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValgnattElectoralRollDomainServiceTest {

	private static final ElectionPath AN_ELECTION_PATH = ElectionPath.from("150001.01.01");
	private static final int VOTER_TOTAL = 596;
	private static final long POLLING_DISTRICT_PK = 2846;
	private static final long ANOTHER_POLLING_DISTRICT_PK = 10000;
	private static final String POLLING_DISTRICT_ID = "0001";
	private static final String POLLING_DISTRICT_NAME = "Stemmekrets";
	private static final String MUNICIPALITY_ID = "0101";
	private static final String MUNICIPALITY_NAME = "Halden";
	private static final String COUNTY_NAME = "Østfold";
	private static final String COUNTY_ID = "01";
	private static final String BOROUGH_ID = "010100";
	private static final long POLLING_DISTRICT_PK_CHILD1 = 5L;
	private static final String POLLING_DISTRICT_ID_CHILD1 = "0005";
	private static final String POLLING_DISTRICT_NAME_CHILD1 = "barnekrets1";
	private static final long POLLING_DISTRICT_PK_CHILD2 = 6L;
	private static final String POLLING_DISTRICT_ID_CHILD2 = "0006";
	private static final String POLLING_DISTRICT_NAME_CHILD2 = "barnekrets2";
	private static final long POLLING_DISTRICT_PK_PARENT = 7L;
	private static final String POLLING_DISTRICT_ID_PARENT = "0007";
	private static final String POLLING_DISTRICT_NAME_PARENT = "tellekrets";
	private static final long POLLING_DISTRICT_PK_MUNICIPALITY = 8L;
	private static final String POLLING_DISTRICT_ID_MUNICIPALITY = "0000";
	private static final String POLLING_DISTRICT_NAME_MUNICIPALITY = "Hele kommunen";
	private static final String BOROUGH_NAME = "bydelsnavn";
	private static final String VALGDISTRIKT_ID = "01";
	private static final String VALGDISTRIKT_NAVN = "Østfold";

	@Test
    public void findVotersAndReportingAreas_returnsElectoralRollForValgnatt() {

		ValgnattElectoralRollRepository fakeValgnattElectoralRollRepository = mock(ValgnattElectoralRollRepository.class);
		when(fakeValgnattElectoralRollRepository.valgnattElectoralRoll(AN_ELECTION_PATH)).thenReturn(makeElectoralRollForPollingDistrictList());
		when(fakeValgnattElectoralRollRepository.valgnattReportConfiguration(any(MvElection.class)))
				.thenReturn(makeReportConfigurationForPollingDistrictList());
		PollingDistrictRepository fakePollingDistrictRepository = mock(PollingDistrictRepository.class);

		ValgnattElectoralRollDomainService valgnattElectoralRollDomainService = new ValgnattElectoralRollDomainService(fakeValgnattElectoralRollRepository,
				fakePollingDistrictRepository);

		MvElection mvElection = makeFakeMvElection();
		ElectoralRollCountReport electoralRollCountReport = valgnattElectoralRollDomainService.findVotersAndReportingAreas(mvElection);

		assertThat(electoralRollCountReport.getVoterTotal()).isEqualTo(VOTER_TOTAL);
	}

	private MvElection makeFakeMvElection() {
		MvElection fakeMvElection = mock(MvElection.class);
		ElectionEvent fakeElectionEvent = mock(ElectionEvent.class);
		when(fakeElectionEvent.getElectionDays()).thenReturn(makeFakeElectionDays());
		when(fakeMvElection.getElectionEvent()).thenReturn(fakeElectionEvent);
		Election fakeElection = mock(Election.class);
		when(fakeElection.electionPath()).thenReturn(AN_ELECTION_PATH);
		when(fakeElection.getValgtype()).thenReturn(Valgtype.STORTINGSVALG);
		when(fakeMvElection.getElection()).thenReturn(fakeElection);
		return fakeMvElection;
	}

	private Set<ElectionDay> makeFakeElectionDays() {
		Set<ElectionDay> fakeElectionDaySet = new HashSet<>();
		fakeElectionDaySet.add(mock(ElectionDay.class));
		return fakeElectionDaySet;
	}

	private List<ReportConfiguration> makeReportConfigurationForPollingDistrictList() {
		List<ReportConfiguration> list = new ArrayList<>();
		list.add(makeReportConfigurationForPollingDistrict(POLLING_DISTRICT_PK, POLLING_DISTRICT_ID, POLLING_DISTRICT_NAME, false, false));
		list.add(makeReportConfigurationForPollingDistrict(ANOTHER_POLLING_DISTRICT_PK, AreaPath.MUNICIPALITY_POLLING_DISTRICT_ID, POLLING_DISTRICT_NAME,
				false, false));
		return list;
	}

	private ReportConfiguration makeReportConfigurationForPollingDistrict(Long pollingDistrictPk, String pollingDistrictId,
																		  String pollingDistrictName, boolean parent, boolean byMunicipality) {
		return new ReportConfiguration(pollingDistrictPk.intValue(), pollingDistrictId, pollingDistrictName, MUNICIPALITY_ID, MUNICIPALITY_NAME, parent,
				byMunicipality, COUNTY_ID, COUNTY_NAME, BOROUGH_ID, BOROUGH_NAME, VALGDISTRIKT_ID, VALGDISTRIKT_NAVN, 1, null);
	}

	private List<ElectoralRollCount> makeElectoralRollForPollingDistrictList() {
		List<ElectoralRollCount> electoralRollCountList = new ArrayList<>();
		electoralRollCountList.add(makeElectoralRollForPollingDistrict(POLLING_DISTRICT_PK, POLLING_DISTRICT_ID, POLLING_DISTRICT_NAME));
		return electoralRollCountList;
	}

	private ElectoralRollCount makeElectoralRollForPollingDistrict(Long pollingDistrictPk, String pollingDistrictId, String pollingDistrictName) {
		return new ElectoralRollCount(MUNICIPALITY_ID, MUNICIPALITY_NAME, COUNTY_NAME, COUNTY_ID, pollingDistrictId, pollingDistrictName,
				BigInteger.valueOf(VOTER_TOTAL), false, pollingDistrictPk.intValue(), BOROUGH_ID, BOROUGH_NAME, "", "", null);
	}

	@Test
	public void findVotersAndReportingAreas_reportConfigurationOnParent_electoralRollForChildrenIsAggregatedOnParent() {

		ValgnattElectoralRollRepository fakeValgnattElectoralRollRepository = mock(ValgnattElectoralRollRepository.class);
		when(fakeValgnattElectoralRollRepository.valgnattElectoralRoll(AN_ELECTION_PATH)).thenReturn(makeElectoralRollForChildPollingDistrictList());
		when(fakeValgnattElectoralRollRepository.valgnattReportConfiguration(any(MvElection.class)))
				.thenReturn(makeReportConfigurationForParentPollingDistrictList());

		PollingDistrictRepository fakePollingDistrictRepository = mock(PollingDistrictRepository.class);
		PollingDistrict parentPollingDistrict = new PollingDistrict();
		parentPollingDistrict.setChildPollingDistricts(makeChildPollingDistrictSet());
		when(fakePollingDistrictRepository.findByPk(POLLING_DISTRICT_PK_PARENT)).thenReturn(parentPollingDistrict);

		ValgnattElectoralRollDomainService valgnattElectoralRollDomainService = new ValgnattElectoralRollDomainService(fakeValgnattElectoralRollRepository,
				fakePollingDistrictRepository);

		MvElection mvElection = makeFakeMvElection();
		ElectoralRollCountReport electoralRollCountReport = valgnattElectoralRollDomainService.findVotersAndReportingAreas(mvElection);

		assertThat(electoralRollCountReport.toJson()).contains(POLLING_DISTRICT_NAME_PARENT);
		assertThat(electoralRollCountReport.getVoterTotal()).isEqualTo(VOTER_TOTAL + VOTER_TOTAL);
	}

	private Set<PollingDistrict> makeChildPollingDistrictSet() {
		Set<PollingDistrict> children = new HashSet<>();
		children.add(makePollingDistrict(POLLING_DISTRICT_PK_CHILD1));
		children.add(makePollingDistrict(POLLING_DISTRICT_PK_CHILD2));
		return children;
	}

	private PollingDistrict makePollingDistrict(Long pk) {
		PollingDistrict pd = new PollingDistrict();
		pd.setPk(pk);
		return pd;
	}

	private List<ReportConfiguration> makeReportConfigurationForParentPollingDistrictList() {
		List<ReportConfiguration> list = new ArrayList<>();
		list.add(makeReportConfigurationForPollingDistrict(POLLING_DISTRICT_PK_PARENT, POLLING_DISTRICT_ID_PARENT, POLLING_DISTRICT_NAME_PARENT, true, false));
		return list;
	}

	private List<ElectoralRollCount> makeElectoralRollForChildPollingDistrictList() {
		List<ElectoralRollCount> list = new ArrayList<>();
		list.add(makeElectoralRollForPollingDistrict(POLLING_DISTRICT_PK_CHILD1, POLLING_DISTRICT_ID_CHILD1, POLLING_DISTRICT_NAME_CHILD1));
		list.add(makeElectoralRollForPollingDistrict(POLLING_DISTRICT_PK_CHILD2, POLLING_DISTRICT_ID_CHILD2, POLLING_DISTRICT_NAME_CHILD2));
		return list;
	}

	@Test
	public void findVotersAndReportingAreas_reportConfigurationOnMunicipality_electoralRollForPollingDistrictsInMunicipalityIsAggregated() {

		ValgnattElectoralRollRepository fakeValgnattElectoralRollRepository = mock(ValgnattElectoralRollRepository.class);
		when(fakeValgnattElectoralRollRepository.valgnattElectoralRoll(AN_ELECTION_PATH)).thenReturn(makeElectoralRollForChildPollingDistrictList());
		when(fakeValgnattElectoralRollRepository.valgnattReportConfiguration(any(MvElection.class)))
				.thenReturn(makeReportConfigurationForMunicipalityPollingDistrictList());

		PollingDistrictRepository fakePollingDistrictRepository = mock(PollingDistrictRepository.class);
		PollingDistrict municipalityPollingDistrict = mock(PollingDistrict.class);
		Borough fakeBorough = mock(Borough.class);
		Municipality fakeMunicipality = mock(Municipality.class);
		when(fakeMunicipality.pollingDistricts()).thenReturn(makeChildPollingDistrictSet());
		when(fakeBorough.getMunicipality()).thenReturn(fakeMunicipality);
		when(municipalityPollingDistrict.getBorough()).thenReturn(fakeBorough);
		municipalityPollingDistrict.setChildPollingDistricts(makeChildPollingDistrictSet());
		when(fakePollingDistrictRepository.findByPk(POLLING_DISTRICT_PK_MUNICIPALITY)).thenReturn(municipalityPollingDistrict);

		ValgnattElectoralRollDomainService valgnattElectoralRollDomainService = new ValgnattElectoralRollDomainService(fakeValgnattElectoralRollRepository,
				fakePollingDistrictRepository);

		MvElection mvElection = makeFakeMvElection();
		ElectoralRollCountReport electoralRollCountReport = valgnattElectoralRollDomainService.findVotersAndReportingAreas(mvElection);

		assertThat(electoralRollCountReport.toJson()).contains(POLLING_DISTRICT_NAME_MUNICIPALITY);
		assertThat(electoralRollCountReport.getVoterTotal()).isEqualTo(VOTER_TOTAL + VOTER_TOTAL);
	}

	private List<ReportConfiguration> makeReportConfigurationForMunicipalityPollingDistrictList() {
		List<ReportConfiguration> list = new ArrayList<>();
		list.add(makeReportConfigurationForPollingDistrict(POLLING_DISTRICT_PK_MUNICIPALITY, POLLING_DISTRICT_ID_MUNICIPALITY,
				POLLING_DISTRICT_NAME_MUNICIPALITY, false, true));
		return list;
	}

	@Test
	public void findVotersAndReportingAreas_pollingDistrict0000hasNoVoters_emptyEntryForPollingDistrict0000IsAdded() {
		ValgnattElectoralRollRepository fakeValgnattElectoralRollRepository = mock(ValgnattElectoralRollRepository.class);
		when(fakeValgnattElectoralRollRepository.valgnattElectoralRoll(AN_ELECTION_PATH)).thenReturn(makeElectoralRollForPollingDistrictList());
		when(fakeValgnattElectoralRollRepository.valgnattReportConfiguration(any(MvElection.class)))
				.thenReturn(makeReportConfigurationForPollingDistrictList());
		PollingDistrictRepository fakePollingDistrictRepository = mock(PollingDistrictRepository.class);
		ValgnattElectoralRollDomainService valgnattElectoralRollDomainService = new ValgnattElectoralRollDomainService(fakeValgnattElectoralRollRepository,
				fakePollingDistrictRepository);

		MvElection mvElection = makeFakeMvElection();
		ElectoralRollCountReport electoralRollCountReport = valgnattElectoralRollDomainService.findVotersAndReportingAreas(mvElection);

		assertThat(electoralRollCountReport.toJson()).contains("\"kretsnummer\":\"0000\"");
	}
}
