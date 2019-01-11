package no.valg.eva.admin.common.voting.model;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VoterDtoTest {

    @Test
    public void testVoter_givenVoter_verifiesFields() {
        String id = "id";
        String lastName = "lastName";
        String firstName = "firstName";
        String middleName = "middleName";
        MvArea mvArea = MvArea.builder().build();

        String expectedNameLine = String.join(" ", firstName, lastName);
        VoterDto voterDto = VoterDto.builder()
                .lastName(lastName)
                .firstName(firstName)
                .middleName(middleName)
                .nameLine(expectedNameLine)
                .id(id)
                .middleName(middleName)
                .mvArea(mvArea)
                .build();

        assertEquals(voterDto.getId(), id);
        assertEquals(voterDto.getNameLine(), expectedNameLine);
        assertEquals(voterDto.getFirstName(), firstName);
        assertEquals(voterDto.getMiddleName(), middleName);
        assertEquals(voterDto.getLastName(), lastName);
        assertEquals(voterDto.getMvArea(), mvArea);
        assertEquals(voterDto.isApproved(), false);
        assertEquals(voterDto.isFictitious(), false);
        assertEquals(voterDto.isEligible(), false);
    }
}
