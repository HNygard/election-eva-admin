package no.valg.eva.admin.configuration.application;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.BydelMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.FylkeskommuneMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.KommuneMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.StemmekretsMapper;
import no.valg.eva.admin.configuration.domain.service.OmraadehierarkiDomainService;
import no.valg.eva.admin.configuration.domain.service.ValggeografiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.LandSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmestedSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.Land;
import no.valg.eva.admin.felles.valggeografi.model.Rode;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.model.Stemmested;
import no.valg.eva.admin.felles.valggeografi.model.Valggeografi;
import no.valg.eva.admin.felles.valggeografi.model.Valghendelse;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ValggeografiService")


@Default
@Remote(ValggeografiService.class)
public class ValggeografiApplicationService implements ValggeografiService {
	@Inject
	private ValggeografiDomainService domainService;
	@Inject
	private OmraadehierarkiDomainService omraadehierarkiDomainService;

	@Override
	@SecurityNone
	public Valggeografi valggeografi(ValggeografiSti valggeografiSti) {
		switch (valggeografiSti.nivaa()) {
			case VALGHENDELSE:
				return domainService.valghendelse(valggeografiSti.tilValghendelseSti());
			case LAND:
				return domainService.land(valggeografiSti.tilLandSti());
			case FYLKESKOMMUNE:
				return domainService.fylkeskommune(valggeografiSti.tilFylkeskommuneSti());
			case KOMMUNE:
				return domainService.kommune(valggeografiSti.tilKommuneSti());
			case BYDEL:
				return domainService.bydel(valggeografiSti.tilBydelSti());
			case STEMMEKRETS:
				return domainService.stemmekrets(valggeografiSti.tilStemmekretsSti());
			case STEMMESTED:
				return domainService.stemmested(valggeografiSti.tilStemmestedSti());
			case RODE:
				return domainService.rode(valggeografiSti.tilRodeSti());
			default:
				throw new IllegalArgumentException(format("ukjent nivå: %s", valggeografiSti.nivaa()));
		}
	}

	@Override
	@SecurityNone
	public Valghendelse valghendelse(UserData userData) {
		return domainService.valghendelse(valghendelseSti(userData));
	}

	@Override
	@SecurityNone
	public Land land(UserData userData) {
		return domainService.land(valghendelseSti(userData));
	}

	@Override
	@SecurityNone
	public List<Fylkeskommune> fylkeskommuner(UserData userData, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		if (countCategory == null) {
			return domainService.fylkeskommuner(valghendelseSti(userData), valghierarkiSti, userData.operatorValggeografiSti());
		}
		AreaPath countryPath = countryPath(userData.operatorValggeografiSti());
		List<MvArea> mvAreas = omraadehierarkiDomainService.getCountiesFor(userData, valghierarkiSti.electionPath(), countryPath, countCategory);
		return mvAreas.stream()
				.map(FylkeskommuneMapper::fylkeskommune)
				.collect(toList());
	}

	private AreaPath countryPath(ValggeografiSti valggeografiSti) {
		return new LandSti(valggeografiSti.valghendelseSti(), "47").areaPath();
	}

	@Override
	@SecurityNone
	public Kommune kommune(UserData userData, KommuneSti kommuneSti) {
		return (Kommune) valggeografi(kommuneSti);
	}

	@Override
	@SecurityNone
	public List<Kommune> kommuner(UserData userData, FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		if (countCategory == null) {
			return domainService.kommuner(fylkeskommuneSti, valghierarkiSti, userData.operatorValggeografiSti());
		}
		List<MvArea> mvAreas = omraadehierarkiDomainService.getMunicipalitiesFor(userData, valghierarkiSti.electionPath(), fylkeskommuneSti.areaPath(), countCategory);
		return mvAreas.stream()
				.map(KommuneMapper::kommune)
				.collect(toList());
	}

	@Override
	@SecurityNone
	public List<Kommune> kommunerForValghendelse(UserData userData) {
		return domainService.kommuner(valghendelseSti(userData));
	}

	@Override
	@SecurityNone
	public List<Bydel> bydeler(UserData userData, KommuneSti kommuneSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		if (countCategory == null) {
			return domainService.bydeler(kommuneSti, valghierarkiSti, userData.operatorValggeografiSti());
		}
		List<MvArea> mvAreas = omraadehierarkiDomainService.getBoroughsFor(userData, countCategory, valghierarkiSti.electionPath(), kommuneSti.areaPath());
		return mvAreas.stream()
				.map(BydelMapper::bydel)
				.collect(toList());
	}

	@Override
	@SecurityNone
	public Stemmekrets stemmekrets(StemmekretsSti stemmekretsSti) {
		return domainService.stemmekrets(stemmekretsSti);
	}

	@Override
	@SecurityNone
	public List<Stemmekrets> stemmekretser(UserData userData, BydelSti bydelSti, ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
		if (countCategory == null) {
			return domainService.stemmekretser(bydelSti, userData.operatorValggeografiSti());
		}
		List<MvArea> mvAreas = omraadehierarkiDomainService.getPollingDistrictsFor(userData, countCategory, valghierarkiSti.electionPath(), bydelSti.areaPath());
		return mvAreas.stream()
				.map(StemmekretsMapper::stemmekrets)
				.collect(toList());
	}

	@Override
	@SecurityNone
	public List<Stemmekrets> stemmekretser(KommuneSti kommuneSti, PollingDistrictType... types) {
		return domainService.stemmekretser(kommuneSti, types);
	}

	@Override
	@SecurityNone
	public List<Stemmested> stemmesteder(UserData userData, StemmekretsSti stemmekretsSti) {
		return domainService.stemmesteder(stemmekretsSti, userData.operatorValggeografiSti());
	}

	@Override
	@SecurityNone
	public List<Rode> roder(StemmestedSti stemmestedSti) {
		return domainService.roder(stemmestedSti);
	}

    @Override
    @SecurityNone
    public Bydel bydel(BydelSti bydelSti) {
        return domainService.bydel(bydelSti);
    }

    ValghendelseSti valghendelseSti(UserData userData) {
		return userData.operatorValggeografiSti().valghendelseSti();
	}
}
