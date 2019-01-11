package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.stemmekretsSti;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;

public final class StemmekretsMapper {
	private StemmekretsMapper() {
	}

	public static Stemmekrets stemmekrets(MvAreaDigest mvAreaDigest) {
		return new Stemmekrets(stemmekretsSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName(),
				mvAreaDigest.getPollingDistrictDigest().isKommuneStemmekrets(), mvAreaDigest.getCountyName(),
				mvAreaDigest.getMunicipalityName(), mvAreaDigest.getBoroughName());
	}

	public static Stemmekrets stemmekrets(MvArea mvArea) {
		return new Stemmekrets(stemmekretsSti(mvArea.areaPath()), mvArea.getAreaName(), mvArea.getPollingDistrict().isMunicipality(),
				mvArea.getCountyName(), mvArea.getMunicipalityName(), mvArea.getBoroughName());
	}
}
