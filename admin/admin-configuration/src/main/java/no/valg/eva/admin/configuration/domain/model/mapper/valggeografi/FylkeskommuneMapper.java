package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.fylkeskommuneSti;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;

public final class FylkeskommuneMapper {
	private FylkeskommuneMapper() {
	}

	public static Fylkeskommune fylkeskommune(MvAreaDigest mvAreaDigest) {
		return new Fylkeskommune(fylkeskommuneSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName());
	}

	public static Fylkeskommune fylkeskommune(MvArea mvArea) {
		return new Fylkeskommune(fylkeskommuneSti(mvArea.areaPath()), mvArea.getAreaName());
	}
}
