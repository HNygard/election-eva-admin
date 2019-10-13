package no.valg.eva.admin.configuration.domain.model.mapper.valggeografi;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.stemmestedSti;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public final class StemmestedMapper {
	public StemmestedMapper() {
	}

	public static Stemmested stemmested(MvAreaDigest mvAreaDigest) {
		return new Stemmested(stemmestedSti(mvAreaDigest.areaPath()), mvAreaDigest.areaName(), mvAreaDigest.getPollingPlaceDigest().isValgting());
	}
}
