package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.event.ManntallsimportFullfortEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.manntall.OmraadeMapping;
import no.valg.eva.admin.configuration.repository.ManntallsimportMappingRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ManntallsimportMappingDomainServiceTest extends MockUtilsTestCase {
	private ManntallsimportMappingDomainService manntallsimportMappingDomainService;
	private VoterRepository voterRepository;
	private MvAreaRepository mvAreaRepository;
	private AuditLogServiceBean auditLogService;
	private UserData userData;
	private ElectionEvent electionEvent;
	private OmraadeMapping omraadeMapping;
	private ManntallsimportFullfortEvent manntallsimportFullfortEvent;
	private Voter velger;

	@BeforeTest
	public void setUp() throws Exception {
		manntallsimportMappingDomainService = initializeMocks(ManntallsimportMappingDomainService.class);
		voterRepository = getInjectMock(VoterRepository.class);
		mvAreaRepository = getInjectMock(MvAreaRepository.class);
		auditLogService = getInjectMock(AuditLogServiceBean.class);

		userData = createMock(UserData.class);
		electionEvent = createMock(ElectionEvent.class);
		manntallsimportFullfortEvent = new ManntallsimportFullfortEvent(userData, electionEvent);

		omraadeMapping = new OmraadeMapping("123456.47.07.0722.072200.0001", "123456.47.07.0722.072200.0002");
		velger = new Voter();
	}

	@Test
    public void flyttVelgereTilKonfigurertKrets_gittUserDataOgElectionEvent_mappesManntallsimporten() {
		stub_finnMappingForValghendelse(electionEvent, omraadeMapping);
		stub_finnByOmraadesti();

		manntallsimportMappingDomainService.flyttVelgereTilKonfigurertKrets(manntallsimportFullfortEvent);

		Voter forventetResultat = forventetManntallsoppdatering();
		assertThat(velger.getCountyId()).isEqualTo(forventetResultat.getCountyId());
		assertThat(velger.getMunicipalityId()).isEqualTo(forventetResultat.getMunicipalityId());
		assertThat(velger.getPollingDistrictId()).isEqualTo(forventetResultat.getPollingDistrictId());
		assertThat(velger.getMvArea().areaPath()).isEqualTo(forventetResultat.getMvArea().areaPath());
	}

	private void stub_finnMappingForValghendelse(ElectionEvent electionEvent, OmraadeMapping omraadeMapping) {
		ArrayList<OmraadeMapping> omraadeMappinger = new ArrayList<>();
		omraadeMappinger.add(omraadeMapping);
		when(getInjectMock(ManntallsimportMappingRepository.class).finnForValghendelse(electionEvent)).thenReturn(omraadeMappinger);
	}

	private void stub_finnByOmraadesti() {
		ArrayList<Voter> velgere = new ArrayList<>();
		velgere.add(velger);
		when(voterRepository.findByOmraadesti(any(AreaPath.class))).thenReturn(velgere);
	}

	private Voter forventetManntallsoppdatering() {
		Voter forventetManntallsoppdatering = new Voter();
		AreaPath omraadesti = omraadeMapping.getTilOmraadesti();
		forventetManntallsoppdatering.setCountryId(omraadesti.getCountryId());
		forventetManntallsoppdatering.setCountyId(omraadesti.getCountyId());
		forventetManntallsoppdatering.setMunicipalityId(omraadesti.getMunicipalityId());
		forventetManntallsoppdatering.setBoroughId(omraadesti.getBoroughId());
		forventetManntallsoppdatering.setPollingDistrictId(omraadesti.getPollingDistrictId());
		forventetManntallsoppdatering.setMvArea(mvAreaRepository.findSingleByPath(omraadesti));
		return forventetManntallsoppdatering;
	}

	@Test
    public void flyttVelgereTilKonfigurertKrets_gittUserDataOgElectionEventUtenMappingForValghendelse_gjoeresIngenting() {
		stub_finnMappingForValghendelse(electionEvent);

		manntallsimportMappingDomainService.flyttVelgereTilKonfigurertKrets(manntallsimportFullfortEvent);

		verifyZeroInteractions(voterRepository);
		verifyZeroInteractions(auditLogService);
	}

	private void stub_finnMappingForValghendelse(ElectionEvent electionEvent) {
		ArrayList<OmraadeMapping> omraadeMappinger = new ArrayList<>();
		when(getInjectMock(ManntallsimportMappingRepository.class).finnForValghendelse(electionEvent)).thenReturn(omraadeMappinger);
	}

	@Test
	public void flyttVelgereTilKonfigurertKrets_gittUserDataOgElectionEvent_oppdateresManntallet() {
		stub_finnMappingForValghendelse(electionEvent, omraadeMapping);
		reset(voterRepository);

		manntallsimportMappingDomainService.flyttVelgereTilKonfigurertKrets(manntallsimportFullfortEvent);

		verify(voterRepository).updateVoters(eq(userData), any());
	}

	@Test
	public void flyttVelgereTilKonfigurertKrets_gittUserDataOgElectionEvent_oppdateresAuditLog() {
		stub_finnMappingForValghendelse(electionEvent, omraadeMapping);
		stub_finnByOmraadesti();
		reset(auditLogService);

		manntallsimportMappingDomainService.flyttVelgereTilKonfigurertKrets(manntallsimportFullfortEvent);

		verify(auditLogService).addToAuditTrail(any());
	}

}
