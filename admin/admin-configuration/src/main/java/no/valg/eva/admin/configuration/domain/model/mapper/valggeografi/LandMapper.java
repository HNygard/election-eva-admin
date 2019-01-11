package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.landSti;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Land;

public final class LandMapper {
	private LandMapper() {
	}

	public static Land land(MvAreaDigest mvArea) {
		return new Land(landSti(mvArea.areaPath()), mvArea.areaName());
	}
}
