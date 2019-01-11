package no.valg.eva.admin.common.configuration.model.election;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;

import org.testng.annotations.Test;

public class ElectionTest {

    private static final ElectionPath ANY_PATH = new ElectionPath("150001.01");

    @Test
    public void isRenumberLogicAllowed_notRenumberNorPersonalOnElection_returnsTrue() {
        Election election = new Election(ANY_PATH);
        election.setRenumber(false);
        election.setPersonal(false);
        assertThat(election.isRenumberLogicAllowed()).isTrue();
    }

    @Test
    public void isRenumberLogicAllowed_notRenumberAndPersonalNotRenumberLimitElection_returnsTrue() {
        Election election = new Election(ANY_PATH);
        election.setRenumber(false);
        election.setPersonal(true);
        election.setRenumberLimit(false);
        assertThat(election.isRenumberLogicAllowed()).isTrue();
    }

    @Test
    public void isRenumberLogicAllowed_renumberAndPersonalNotRenumberLimitElection_returnsFalse() {
        Election election = new Election(ANY_PATH);
        election.setRenumber(true);
        election.setPersonal(true);
        election.setRenumberLimit(false);
        assertThat(election.isRenumberLogicAllowed()).isFalse();
    }
}
