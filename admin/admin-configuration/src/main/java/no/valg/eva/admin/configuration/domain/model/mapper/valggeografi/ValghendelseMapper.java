package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.valghendelseSti;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Valghendelse;

public final class ValghendelseMapper {
	private ValghendelseMapper() {
	}

	public static Valghendelse valghendelse(MvAreaDigest mvAreaDigest) {
		return new Valghendelse(valghendelseSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName());
	}
}
