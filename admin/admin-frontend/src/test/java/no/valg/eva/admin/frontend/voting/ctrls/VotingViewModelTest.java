package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingContentViewModel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class VotingViewModelTest {

    @Test(dataProvider = "equalsHashCodeTestData")
    public void testHashCode_givenViewModels_verifiesHashCodeEquality(VotingViewModel votingViewModel1, VotingViewModel votingViewModel2, boolean hashCodeExpectedToBeEqual) {
        assertEquals(votingViewModel1.hashCode() == votingViewModel2.hashCode(), hashCodeExpectedToBeEqual);
    }

    @Test(dataProvider = "equalsHashCodeTestData")
    public void testEquals_givenViewModels_verifiesEquality(VotingViewModel votingViewModel1, VotingViewModel votingViewModel2, boolean expectingEquality) {
        assertEquals(votingViewModel1.equals(votingViewModel2), expectingEquality);
    }

    @DataProvider
    public Object[][] equalsHashCodeTestData() {
        String personId = "personId";
        VotingViewModel votingViewModel1 = votingViewModel(personId);

        VotingViewModel votingViewModel2 = VotingViewModel.builder()
                .personId(personId)
                .voter(VoterDto.builder().build())
                .build();

        VotingViewModel votingViewModel3 = VotingViewModel.builder()
                .personId(null)
                .voter(VoterDto.builder().build())
                .build();

        VotingViewModel votingViewModel4 = VotingViewModel.builder()
                .personId("anotherPersonId")
                .voter(VoterDto.builder().build())
                .build();

        return new Object[][]{
                {votingViewModel1, votingViewModel2, true},
                {votingViewModel2, votingViewModel3, false},
                {votingViewModel2, votingViewModel4, false},
        };
    }

    @Test
    public void testVotingViewModel_givenViewModelWithNoArgsConstructor_verifiesNullFields() {
        VotingViewModel votingViewModel = new VotingViewModel();
        assertNull(votingViewModel.getNameLine());
        assertNull(votingViewModel.getPersonId());
        assertNull(votingViewModel.getSuggestedRejectionReason());
        assertNull(votingViewModel.getVotingDate());
    }

    @Test
    public void testGetters_() {
        List<VotingViewModel> votingList = new ArrayList<>();

        VotingViewModel votingViewModel = VotingViewModel.builder()
                .build();
        votingList.add(votingViewModel);

        String id = "123";
        int numberOfApprovedVotings = 10;
        int numberOfRejectedVotings = 11;
        int numberOfVotingsToConfirm = 1;

        ConfirmVotingContentViewModel result = ConfirmVotingContentViewModel.builder()
                .id(id)
                .numberOfApprovedVotings(numberOfApprovedVotings)
                .numberOfRejectedVotings(numberOfRejectedVotings)
                .numberOfVotingsToConfirm(numberOfVotingsToConfirm)
                .votingList(votingList)
                .build();

        assertEquals(result.getId(), id);
        assertEquals(result.getNumberOfApprovedVotings(), numberOfApprovedVotings);
        assertEquals(result.getNumberOfRejectedVotings(), numberOfRejectedVotings);
        assertEquals(result.getNumberOfVotingsToConfirm(), numberOfVotingsToConfirm);
        assertEquals(result.getVotingList(), votingList);
    }

    private VotingViewModel votingViewModel(String personId) {
        return VotingViewModel.builder()
                .nameLine("fullName")
                .voter(VoterDto.builder().build())
                .personId(personId)
                .suggestedRejectionReason("suggestedRejectionReason")
                .votingDate("2011-01-01")
                .build();
    }

    @Test
    public void testSearchableProperties() {
        List<String> expectedList = asList("firstName", "middleName", "lastName", "votingNumber");
        List<String> actualList = new VotingViewModel().getSearchableProperties();
        assertEquals(expectedList.size(), actualList.size());
        assertEquals(actualList, expectedList);
    }

    @Test
    public void testToVotingDto_withNull_shouldReturnNull() {
        assertNull(VotingViewModel.toVotingDto(null));
    }

    @Test
    public void testToVotingDto_withVotingViewModel_shouldReturnDto() {
        VotingViewModel vvm = VotingViewModel.builder()
                .votingNumber(1)
                .voter(VoterDto.builder().build())
                .electionGroup(mock(ElectionGroup.class))
                .votingCategory(mock(VotingCategory.class))
                .build();
        VotingDto dto = VotingViewModel.toVotingDto(vvm);
        assertEquals(vvm.getVotingNumber(), (int) dto.getVotingNumber());
        assertEquals(vvm.getVoter(), dto.getVoterDto());
        assertEquals(vvm.getElectionGroup(), dto.getElectionGroup());
        assertEquals(vvm.getVotingCategory(), dto.getVotingCategory());
    }

    @Test
    public void testNamesAndPersonId_whenFictitiousVoter_shouldBeBlank() {
        VotingViewModel vvm = votingViewModelWithNames(true);

        assertEquals(vvm.getPersonId(), "");
        assertEquals(vvm.getFirstName(), "");
        assertEquals(vvm.getMiddleName(), "");
        assertEquals(vvm.getLastName(), "");
        assertEquals(vvm.getFullName(), "");
    }

    private VotingViewModel votingViewModelWithNames(boolean isFictitious) {
        return VotingViewModel.builder()
                .personId("12345678900")
                .firstName("Per")
                .middleName("Karlstad")
                .lastName("Løkhue")
                .nameLine("Per Karlstad Løkhue")
                .voter(VoterDto.builder()
                        .fictitious(isFictitious)
                        .build())
                .votingDate("2011-01-01")
                .build();
    }

    @Test
    public void testNamesAndPersonId_whenNotFictitiousVoter_shouldNotBeBlank() {
        VotingViewModel vvm = votingViewModelWithNames(false);

        assertEquals(vvm.getPersonId(), "12345678900");
        assertEquals(vvm.getFirstName(), "Per");
        assertEquals(vvm.getMiddleName(), "Karlstad");
        assertEquals(vvm.getLastName(), "Løkhue");
        assertEquals(vvm.getFullName(), "Per Karlstad Løkhue");
    }

    @Test
    public void testNamesAndPersonId_whenVoterIsNull_shouldReturnNull() {
        VotingViewModel vvm = VotingViewModel.builder()
                .personId("12345678900")
                .firstName("Per")
                .middleName("Karlstad")
                .lastName("Løkhue")
                .nameLine("Per Karlstad Løkhue")
                .votingDate("2011-01-01")
                .build();

        assertNull(vvm.getPersonId());
        assertNull(vvm.getFirstName());
        assertNull(vvm.getMiddleName());
        assertNull(vvm.getLastName());
        assertNull(vvm.getFullName());
    }
}