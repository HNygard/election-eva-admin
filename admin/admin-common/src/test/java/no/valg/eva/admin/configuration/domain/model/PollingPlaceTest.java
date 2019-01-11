package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.AreaPath;

import org.testng.annotations.Test;

public class PollingPlaceTest {

	public static final String A_NORMAL_POLLING_PLACE_ID = "0101";

	@Test
	public void isEditable_returnsFalseIfPollingPlaceIsCentralEnvelopeRegistrationPollingPlace() {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setId(AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID);
		assertThat(pollingPlace.isEditable()).isFalse();
	}

	@Test
	public void isEditable_returnsTrueIfPollingPlaceIsNotCentralEnvelopeRegistrationPollingPlace() {
		PollingPlace pollingPlace = new PollingPlace();
		pollingPlace.setId(A_NORMAL_POLLING_PLACE_ID);
		assertThat(pollingPlace.isEditable()).isTrue();
	}

}
