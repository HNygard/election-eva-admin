package no.valg.eva.admin.common.counting.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

public interface AntallStemmesedlerLagtTilSideService {

	AntallStemmesedlerLagtTilSide hentAntallStemmesedlerLagtTilSide(UserData userData, KommuneSti kommuneSti);

	void lagreAntallStemmesedlerLagtTilSide(UserData userData, AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide);
}
