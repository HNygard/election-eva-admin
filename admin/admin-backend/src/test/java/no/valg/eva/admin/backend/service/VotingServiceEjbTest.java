package no.valg.eva.admin.backend.service;

import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.model.views.ForeignEarlyVoting;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRepository;
import no.valg.eva.admin.voting.service.VotingServiceBean;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class VotingServiceEjbTest extends MockUtilsTestCase {

    private VotingServiceEjb votingServiceEjb;
    
    private MvElectionRepository mvElectionRepository;
    private VotingServiceBean votingServiceBean;
    private VotingRepository votingRepository;
    private MvAreaRepository mvAreaRepository;
    private ElectionGroup electionGroup;
    private ValggruppeSti valgGruppeSti;
    private PollingPlace pollingPlace;
    private KommuneSti kommuneSti;
    private MvElection mvElection;
    private UserData userData;
    private MvArea mvArea;
    private StemmegivningsType stemmegivningsType;
    private ElectionPath electionPath;
    private AreaPath areaPath;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        votingServiceEjb = initializeMocks(VotingServiceEjb.class);

        mvElectionRepository = getInjectMock(MvElectionRepository.class);
        stemmegivningsType = createMock(StemmegivningsType.class);
        votingServiceBean = getInjectMock(VotingServiceBean.class);
        mvAreaRepository = getInjectMock(MvAreaRepository.class);
        votingRepository = getInjectMock(VotingRepository.class);
        electionGroup = createMock(ElectionGroup.class);
        valgGruppeSti = createMock(ValggruppeSti.class);
        electionPath = createMock(ElectionPath.class);
        pollingPlace = createMock(PollingPlace.class);
        mvElection = createMock(MvElection.class);
        kommuneSti = createMock(KommuneSti.class);
        areaPath = createMock(AreaPath.class);
        userData = createMock(UserData.class);
        mvArea = createMock(MvArea.class);

    }

    @Test
    public void testCountUnapprovedAdvanceVotings_givenAreaPath_verifiesRepositoryCall(){
        AreaPath areaPath = createMock(AreaPath.class);
        long unapprovedVotings = 1L;

        when(votingRepository.countUnapprovedAdvanceVotings(areaPath)).thenReturn(unapprovedVotings);
        assertEquals(votingServiceEjb.countUnapprovedAdvanceVotings(userData, areaPath), unapprovedVotings);
        verify(votingRepository).countUnapprovedAdvanceVotings(areaPath);
    }

    @Test
    public void testUpdate_givenVoting_verifiesRepositoryCall() {
        Voting voting = voting();

        when(votingRepository.update(any(UserData.class), any(Voting.class))).thenReturn(voting);
        assertEquals(votingServiceEjb.update(userData, voting), voting);
        verify(votingRepository).update(userData, voting);
    }

    private Voting voting() {
        return Voting.builder().build();
    }

    @Test
    public void testUpdateAdvanceVotingApproved_givenVoting_verifiesServiceBeanCall(){
        Voting voting = voting();
        
        when(votingServiceBean.updateAdvanceVotingApproved(any(UserData.class), any(Voting.class))).thenReturn(voting);
        assertEquals(votingServiceEjb.updateAdvanceVotingApproved(userData, voting), voting);
        
        verify(votingServiceBean).updateAdvanceVotingApproved(userData, voting);
    }

    @Test
    public void testGetVotingsByElectionGroupAndVoter_givenVoterAndElectionGroupPk_verifiesRepositoryCall() {
        when(votingRepository.getVotingsByElectionGroupAndVoter(anyLong(), anyLong())).thenReturn(Collections.emptyList());
        List<Voting> votingsByElectionGroupAndVoter = votingServiceEjb.getVotingsByElectionGroupAndVoter(userData, 1L, 1L);
        assertEmptyList(votingsByElectionGroupAndVoter);
        verify(votingRepository).getVotingsByElectionGroupAndVoter(1L, 1L);
    }

    private void assertEmptyList(List<Voting> votings) {
        assertEquals(votings, Collections.emptyList());
    }

    @Test
    public void testGetVotingsByElectionGroupVoterAndMunicipality_givenVoterElectionGroupAndMunicipality_verifiesRepositoryCall() {
        when(votingRepository.getVotingsByElectionGroupVoterAndMunicipality(anyLong(), anyLong(), anyLong())).thenReturn(Collections.emptyList());
        List<Voting> votings = votingServiceEjb.getVotingsByElectionGroupVoterAndMunicipality(userData, 1L, 1L, 1L);
        assertEmptyList(votings);
        verify(votingRepository).getVotingsByElectionGroupVoterAndMunicipality(1L, 1L, 1L);
    }

    @Test
    public void testGetRejectedVotingsByElectionGroupAndMunicipality_givenElectionGroupAndMunicipalityPaths_verifiesRepositoryCall() {
        when(mvElectionRepository.finnEnkeltMedSti(any(ValggruppeSti.class))).thenReturn(mvElection);
        when(votingRepository.getRejectedVotingsByElectionGroupAndMunicipality(anyString(), anyLong())).thenReturn(Collections.emptyList());

        List<Voting> votings = votingServiceEjb.getRejectedVotingsByElectionGroupAndMunicipality(userData, valgGruppeSti, kommuneSti);
        assertEmptyList(votings);
        verify(votingRepository).getRejectedVotingsByElectionGroupAndMunicipality(kommuneSti.kommuneId(), mvElection.getElectionGroup().getPk());
    }

    @Test
    public void testDelete_givenVotingPk_verifiesRepositoryCall() {
        doNothing().when(votingRepository).delete(any(UserData.class), anyLong());
        votingServiceEjb.delete(userData, 1L);
        verify(votingRepository).delete(userData, 1L);
    }

    @Test
    public void testDelete_givenVoting_verifiesRepositoryCall() {
        Voting voting = voting();
        
        doNothing().when(votingRepository).delete(any(UserData.class), anyLong());
        votingServiceEjb.delete(userData, voting);
        verify(votingRepository).delete(userData, voting.getPk());
    }

    @Test
    public void testFindVotingStatistics_givenFilters_verifiesServiceBeanCall() {
        String[] votingCategories = new String[0];
        LocalDate now = LocalDate.now();

        when(votingServiceBean.findVotingStatistics(anyLong(), anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt(), anyBoolean(), any(), anyBoolean()))
                .thenReturn(Collections.emptyList());
        List<VotingDto> statistics = votingServiceEjb.findVotingStatistics(userData, 1L, 1L, 1L, now, now, 0, 100, false, votingCategories, false);
        assertEquals(statistics, Collections.emptyList());
        
        verify(votingServiceBean).findVotingStatistics(1L, 1L, 1L, now, now, 0, 100, false, votingCategories, false);
    }

    @Test
    public void testFindAdvanceVotingPickList_givenContext_verifiesServiceCall() {
        LocalDate now = LocalDate.now();
        
        when(votingServiceBean.findAdvanceVotingPickList(anyLong(), anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class),anyInt(), anyInt())).thenReturn(Collections.emptyList());
        List<PickListItem> advanceVotingPickList = votingServiceEjb.findAdvanceVotingPickList(userData, 1L, 1L, 1L, now, now, 0, 100);
        assertEquals(advanceVotingPickList, Collections.emptyList());
        verify(votingServiceBean).findAdvanceVotingPickList(1L, 1L, 1L, now, now, 0, 100);
    }

    @Test
    public void testFindElectionDayVotingPickList_givenContext_verifiesServiceCall() {
        when(votingServiceBean.findElectionDayVotingPickList(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        List<PickListItem> electionDayVotingPickList = votingServiceEjb.findElectionDayVotingPickList(userData, 1L, 1L, 0, 100);
        assertEquals(electionDayVotingPickList, Collections.emptyList());
        verify(votingServiceBean).findElectionDayVotingPickList(1L, 1L,0 , 100);
    }

    @Test
    public void testUpdateAdvanceVotingsApproved_givenContext_verifiesServiceCall() {
        LocalDate now = LocalDate.now();
        int expectedResult = 1;
        
        when(votingServiceBean.updateAdvanceVotingsApproved(anyLong(), anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())).thenReturn(expectedResult);
       
        assertEquals(votingServiceEjb.updateAdvanceVotingsApproved(userData, 1L, 1L, 1L, now, now, 0, 100), expectedResult);
        verify(votingServiceBean).updateAdvanceVotingsApproved(1L, 1L, 1L, now, now, 0, 100);
    }

    @Test
    public void testUpdateElectionDayVotingsApproved_givenContext_verifiesServiceCall() {
        int expectedResult = 1;
        when(votingServiceBean.updateElectionDayVotingsApproved(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(expectedResult);
        
        assertEquals(votingServiceEjb.updateElectionDayVotingsApproved(userData, 1L, 1L, 0, 100), expectedResult);
        verify(votingServiceBean).updateElectionDayVotingsApproved(1L, 1L,0, 100);
    }

    
    @Test
    public void testFindVotingByVotingNumber_givenContext_verifiesRepositoryCall() {
        Voting voting = voting();
        when(mvElectionRepository.finnEnkeltMedSti(any(ValggruppeSti.class))).thenReturn(mvElection);
        when(mvAreaRepository.findSingleByPath(any(AreaPath.class))).thenReturn(mvArea);
        when(votingRepository.findVotingByVotingNumber(anyLong(), anyLong(), anyLong(), anyBoolean())).thenReturn(voting);
        
        Voting votingByVotingNumber = votingServiceEjb.findVotingByVotingNumber(userData, valgGruppeSti, kommuneSti, 1L, false);
        assertEquals(votingByVotingNumber,voting);
        verify(votingRepository).findVotingByVotingNumber(0L, 0L, 1L, false);
    }

    @Test
    public void testFindVotingCategoryById_givenCategoryId_verifiesRepositoryCall() {
        no.valg.eva.admin.common.voting.VotingCategory votingCategory = no.valg.eva.admin.common.voting.VotingCategory.VS;
        
        VotingCategory expectedVotingCategory = new VotingCategory();
        expectedVotingCategory.setId(votingCategory.getId());
        when(votingRepository.findVotingCategoryById(anyString())).thenReturn(expectedVotingCategory);
        
        VotingCategory votingCategoryById = votingServiceEjb.findVotingCategoryById(userData, expectedVotingCategory.getId());
        assertEquals(votingCategoryById, expectedVotingCategory);
        verify(votingRepository).findVotingCategoryById(expectedVotingCategory.getId());
    }

    @Test
    public void testFindAdvanceVotingCategories_verifiesRepositoryCall() {
        when(votingRepository.findAdvanceVotingCategories()).thenReturn(Collections.emptyList());
        List<VotingCategory> advanceVotingCategories = votingServiceEjb.findAdvanceVotingCategories(userData);
        assertEquals(advanceVotingCategories, Collections.emptyList());
        verify(votingRepository).findAdvanceVotingCategories();
    }

    @Test
    public void testFindForeignEarlyVotingsSentFromMunicipality() {
        when(votingRepository.findForeignEarlyVotingsSentFromMunicipality(anyLong(), anyString())).thenReturn(Collections.emptyList());
        List<ForeignEarlyVoting> foreignEarlyVotingsSentFromMunicipality = votingServiceEjb.findForeignEarlyVotingsSentFromMunicipality(userData, valgGruppeSti, kommuneSti);
        assertEquals(foreignEarlyVotingsSentFromMunicipality, Collections.emptyList());
        verify(votingRepository).findForeignEarlyVotingsSentFromMunicipality(0L, null);
    }

    @Test
    public void testDeleteVotings() {
        doNothing().when(votingRepository).deleteVotings(any(MvElection.class), any(MvArea.class), anyInt());
        votingServiceEjb.deleteVotings(userData, mvElection, mvArea, 1);
        verify(votingRepository).deleteVotings(mvElection, mvArea, 1);
    }

    @Test
    public void testDeleteSeqVotingNumber_givenContext_verifiesRepositoryCall() {
        doNothing().when(votingRepository).deleteSeqVotingNumber(any(mvElection.getClass()), any(MvArea.class));
        votingServiceEjb.deleteSeqVotingNumber(userData, mvElection, mvArea);
        verify(votingRepository).deleteSeqVotingNumber(mvElection, mvArea);
    }

    @Test
    public void testFindAllVotingCategories_verifiesRepositoryCall() {
        when(votingRepository.findAllVotingCategories()).thenReturn(Collections.emptyList());
        List<VotingCategory> allVotingCategories = votingServiceEjb.findAllVotingCategories(userData);
        assertEquals(allVotingCategories, Collections.emptyList());
        verify(votingRepository).findAllVotingCategories();
    }

    @Test
    public void testMarkOffVoterAdvance_givenVoter_verifiesServiceCall() {
        Voter voter = Voter.builder().build();
        Voting expectedVoting = voting();
        String votingCategoryId = no.valg.eva.admin.common.voting.VotingCategory.FI.getId();
        VotingPhase votingPhase = VotingPhase.ADVANCE;

        when(votingServiceBean.markOffVoterAdvance(any(UserData.class), any(PollingPlace.class), any(ElectionGroup.class), any(Voter.class), anyBoolean(), anyString(), anyString(), any(VotingPhase.class))).thenReturn(expectedVoting);

        Voting voting = votingServiceEjb.markOffVoterAdvance(userData, pollingPlace, electionGroup, voter, true, votingCategoryId, "ballotBoxId", votingPhase);
        
        assertEquals(voting, expectedVoting);
        verify(votingServiceBean).markOffVoterAdvance(userData, pollingPlace, electionGroup, voter, true, votingCategoryId, "ballotBoxId", votingPhase);
    }

    @Test
    public void testRegisterVoteCentrally_givenVoter_verifiesServiceCall() {
        Voter voter = Voter.builder().build();
        Voting expectedVoting = voting();
        VotingPhase votingPhase = VotingPhase.ADVANCE;
        String votingCategoryId = no.valg.eva.admin.common.voting.VotingCategory.FI.getId();
        
        when(votingServiceBean.registerVoteCentrally(any(UserData.class), any(ElectionGroup.class), any(Voter.class), anyString(), any(MvArea.class), any(VotingPhase.class))).thenReturn(expectedVoting);
        
        Voting voting = votingServiceEjb.registerVoteCentrally(userData, electionGroup, voter, votingCategoryId, mvArea, votingPhase);
        
        assertEquals(voting ,expectedVoting);
        verify(votingServiceBean).registerVoteCentrally(userData, electionGroup, voter, votingCategoryId, mvArea, votingPhase);
    }

    @Test
    public void testMarkOffVoter() {
        VotingPhase votingPhase = VotingPhase.ADVANCE;
        Voting expectedVoting = voting();
        Voter voter = Voter.builder().build();
        
        when(votingServiceBean.markOffVoter(any(UserData.class), any(PollingPlace.class), any(ElectionGroup.class), any(Voter.class), anyBoolean(), any(VotingPhase.class))).thenReturn(expectedVoting);
        Voting voting = votingServiceEjb.markOffVoter(userData, pollingPlace, electionGroup, voter, false, votingPhase);
        
        assertEquals(voting, expectedVoting);
        verify(votingServiceBean).markOffVoter(userData, pollingPlace, electionGroup, voter, false, votingPhase);
    }

    @Test
    public void testMarkOffVoterAdvanceVoteInBallotBox() {
        VotingPhase votingPhase = VotingPhase.ADVANCE;
        Voting expectedVoting = voting();
        Voter voter = Voter.builder().build();
        
        when(votingServiceBean.markOffVoterAdvanceVoteInBallotBox(any(UserData.class), any(PollingPlace.class), any(ElectionGroup.class), any(Voter.class), anyBoolean(), any(VotingPhase.class))).thenReturn(expectedVoting);
        
        Voting voting = votingServiceEjb.markOffVoterAdvanceVoteInBallotBox(userData, pollingPlace, electionGroup, voter, true, votingPhase);
        
        assertEquals(voting, expectedVoting);
        verify(votingServiceBean).markOffVoterAdvanceVoteInBallotBox(userData, pollingPlace, electionGroup, voter, true, votingPhase);
    }

    @Test
    public void testHentVelgerSomSkalStemme() {
        Voter voter = Voter.builder().build();
        VelgerSomSkalStemme expectedVelgerSomSkalStemme = createMock(VelgerSomSkalStemme.class);

        when(votingServiceBean.hentVelgerSomSkalStemme(any(StemmegivningsType.class), any(ElectionPath.class), any(AreaPath.class), any(Voter.class))).thenReturn(expectedVelgerSomSkalStemme);
        
        VelgerSomSkalStemme velgerSomSkalStemme = votingServiceEjb.hentVelgerSomSkalStemme(userData, stemmegivningsType, electionPath, areaPath, voter);
        assertEquals(velgerSomSkalStemme, expectedVelgerSomSkalStemme);
        verify(votingServiceBean).hentVelgerSomSkalStemme(stemmegivningsType, electionPath, areaPath, voter);
    }
}