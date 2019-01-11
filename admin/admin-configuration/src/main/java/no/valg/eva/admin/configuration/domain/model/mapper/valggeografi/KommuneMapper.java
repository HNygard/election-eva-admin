package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.kommuneSti;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;

public final class KommuneMapper {
	private KommuneMapper() {
	}

	public static Kommune kommune(MvAreaDigest mvAreaDigest) {
		return new Kommune(kommuneSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName(), mvAreaDigest.getMunicipalityDigest().isElectronicMarkoffs());
	}

	public static Kommune kommune(MvArea mvArea) {
		return new Kommune(kommuneSti(mvArea.areaPath()), mvArea.getAreaName(), mvArea.getMunicipality().isElectronicMarkoffs());
	}
}
