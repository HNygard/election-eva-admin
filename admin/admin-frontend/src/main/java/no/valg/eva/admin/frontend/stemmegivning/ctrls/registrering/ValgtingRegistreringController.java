package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

/**
 * Superklasse for alle valgting stemmegivning controllere med felles funksjonalitet.
 */
public abstract class ValgtingRegistreringController extends RegistreringController {

	@Override
	public void kontekstKlar() {

	}

	boolean isVelgerSammeKommuneMenIkkeSammeStemmekrets() {
		boolean sammeKommune = getVelger().getMunicipalityId().equals(getStemmested().getMunicipalityId());
		boolean sammeStemmekrets = getVelger().getPollingDistrictId().equals(getStemmested().getPollingDistrictId());
		return sammeKommune && !sammeStemmekrets;
	}

}
