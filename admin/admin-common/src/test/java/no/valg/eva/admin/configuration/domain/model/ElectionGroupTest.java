package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

public class ElectionGroupTest {

    private static final String ID = "id";

    @Test
    public void hasElectionWithId_groupHasElectionWithGivenId_returnsTrue() {
        ElectionGroup electionGroup = new ElectionGroup();
        Set<Election> elections = new HashSet<>();
        Election e = new Election();
        elections.add(e);
        e.setId(ID);
        electionGroup.setElections(elections);

        assertThat(electionGroup.hasElectionWithId(ID)).isTrue();
    }

    @Test
    public void hasElectionWithId_groupHasNotElectionWithGivenId_returnsFalse() {
        assertThat(new ElectionGroup().hasElectionWithId(ID)).isFalse();
    }
}
