package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.bydelSti;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;

public final class BydelMapper {
	private BydelMapper() {
	}

	public static Bydel bydel(MvAreaDigest mvAreaDigest) {
        return new Bydel(bydelSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName(), mvAreaDigest.getBoroughDigest().isKommuneBydel(),
                mvAreaDigest.getCountyName(), mvAreaDigest.getMunicipalityName());		
	}

	public static Bydel bydel(MvArea mvArea) {
		return new Bydel(bydelSti(mvArea.areaPath()), mvArea.getAreaName(), mvArea.getBorough().isMunicipality1());
	}
}
