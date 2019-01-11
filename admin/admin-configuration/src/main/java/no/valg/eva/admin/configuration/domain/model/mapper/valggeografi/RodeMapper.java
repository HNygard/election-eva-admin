package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.rodeSti;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Rode;

public final class RodeMapper {
	private RodeMapper() {
	}

	public static Rode rode(MvAreaDigest mvAreaDigest) {
		return new Rode(rodeSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName());
	}
}
