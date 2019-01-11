package no.valg.eva.admin.configuration.domain.service;

import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.AreaLevelEnum.POLLING_STATION;
import static no.valg.eva.admin.configuration.domain.model.filter.MvAreaDigestFilter.valggeografiMatcherValgeografiSti;
import static no.valg.eva.admin.configuration.domain.model.filter.MvAreaDigestFilter.valggeografiMatcherValghierarkiSti;
import static no.valg.eva.admin.configuration.domain.model.mapper.Mapper.map;

import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.BydelMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.FylkeskommuneMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.KommuneMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.LandMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.RodeMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.StemmekretsMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.StemmestedMapper;
import no.valg.eva.admin.configuration.domain.model.mapper.valggeografi.ValghendelseMapper;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.felles.sti.valggeografi.BydelSti;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.LandSti;
import no.valg.eva.admin.felles.sti.valggeografi.RodeSti;
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

public class ValggeografiDomainService {
	private final MvElectionRepository mvElectionRepository;
	private final MvAreaRepository mvAreaRepository;

	@Inject
	public ValggeografiDomainService(MvElectionRepository mvElectionRepository, MvAreaRepository mvAreaRepository) {
		this.mvElectionRepository = mvElectionRepository;
		this.mvAreaRepository = mvAreaRepository;
	}

	public Valghendelse valghendelse(ValghendelseSti sti) {
		return valggeografi(sti, ValghendelseMapper::valghendelse);
	}

	public Land land(ValghendelseSti valghendelseSti) {
		MvAreaDigest mvArea = mvAreaRepository.findFirstDigestByPathAndLevel(valghendelseSti.areaPath(), COUNTRY);
		return LandMapper.land(mvArea);
	}

	public Land land(LandSti sti) {
		return valggeografi(sti, LandMapper::land);
	}

	public Fylkeskommune fylkeskommune(FylkeskommuneSti sti) {
		return valggeografi(sti, FylkeskommuneMapper::fylkeskommune);
	}

	public List<Fylkeskommune> fylkeskommuner(ValghendelseSti valghendelseSti, ValghierarkiSti valghierarkiSti, ValggeografiSti operatorValggeografiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(valghendelseSti.areaPath(), COUNTY);
		return map(mvAreaDigests, FylkeskommuneMapper::fylkeskommune,
				valggeografiMatcherValghierarkiSti(mvElectionRepository, valghierarkiSti), valggeografiMatcherValgeografiSti(operatorValggeografiSti));
	}

	public List<Fylkeskommune> fylkeskommuner(ValghendelseSti valghendelseSti, ValghierarkiSti valghierarkiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(valghendelseSti.areaPath(), COUNTY);
		return map(mvAreaDigests, FylkeskommuneMapper::fylkeskommune, valggeografiMatcherValghierarkiSti(mvElectionRepository, valghierarkiSti));
	}

	public Kommune kommune(KommuneSti sti) {
		return valggeografi(sti, KommuneMapper::kommune);
	}

	public List<Kommune> kommuner(FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti, ValggeografiSti operatorValggeografiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(fylkeskommuneSti.areaPath(), MUNICIPALITY);
		return map(mvAreaDigests, KommuneMapper::kommune,
				valggeografiMatcherValghierarkiSti(mvElectionRepository, valghierarkiSti), valggeografiMatcherValgeografiSti(operatorValggeografiSti));
	}

	public List<Kommune> kommuner(FylkeskommuneSti fylkeskommuneSti, ValghierarkiSti valghierarkiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(fylkeskommuneSti.areaPath(), MUNICIPALITY);
		return map(mvAreaDigests, KommuneMapper::kommune, valggeografiMatcherValghierarkiSti(mvElectionRepository, valghierarkiSti));
	}

	public List<Kommune> kommuner(ValghendelseSti valghendelseSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(valghendelseSti.areaPath(), MUNICIPALITY);
		return map(mvAreaDigests, KommuneMapper::kommune);
	}

	public Bydel bydel(BydelSti sti) {
		return valggeografi(sti, BydelMapper::bydel);
	}

	public List<Bydel> bydeler(KommuneSti kommuneSti, ValghierarkiSti valghierarkiSti, ValggeografiSti operatorValggeografiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(kommuneSti.areaPath(), BOROUGH);
		return map(mvAreaDigests, BydelMapper::bydel,
				valggeografiMatcherValghierarkiSti(mvElectionRepository, valghierarkiSti), valggeografiMatcherValgeografiSti(operatorValggeografiSti));
	}

	public Stemmekrets stemmekrets(StemmekretsSti sti) {
		return valggeografi(sti, StemmekretsMapper::stemmekrets);
	}

	public List<Stemmekrets> stemmekretser(BydelSti bydelSti, ValggeografiSti operatorValggeografiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(bydelSti.areaPath(), POLLING_DISTRICT);
		return map(mvAreaDigests, StemmekretsMapper::stemmekrets, valggeografiMatcherValgeografiSti(operatorValggeografiSti));
	}

	public List<Stemmekrets> stemmekretser(KommuneSti kommuneSti, PollingDistrictType... types) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(kommuneSti.areaPath(), POLLING_DISTRICT);
		if (types != null && types.length > 0) {
			return map(mvAreaDigests, StemmekretsMapper::stemmekrets, mvAreaDigest -> include(mvAreaDigest, types));
		}
		return map(mvAreaDigests, StemmekretsMapper::stemmekrets);
	}

	private boolean include(MvAreaDigest mvAreaDigest, PollingDistrictType... types) {
		for (PollingDistrictType type : types) {
			if (mvAreaDigest.getPollingDistrictDigest().type() == type) {
				return true;
			}
		}
		return false;
	}

	public Stemmested stemmested(StemmestedSti sti) {
		return valggeografi(sti, StemmestedMapper::stemmested);
	}

	public List<Stemmested> stemmesteder(StemmekretsSti stemmekretsSti, ValggeografiSti operatorValggeografiSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(stemmekretsSti.areaPath(), POLLING_PLACE);
		return map(mvAreaDigests, StemmestedMapper::stemmested, valggeografiMatcherValgeografiSti(operatorValggeografiSti));
	}

	public Rode rode(RodeSti sti) {
		return valggeografi(sti, RodeMapper::rode);
	}

	public List<Rode> roder(StemmestedSti stemmestedSti) {
		List<MvAreaDigest> mvAreaDigests = mvAreaRepository.findDigestsByPathAndLevel(stemmestedSti.areaPath(), POLLING_STATION);
		return map(mvAreaDigests, RodeMapper::rode);
	}

	private <T extends Valggeografi> T valggeografi(ValggeografiSti sti, Function<MvAreaDigest, T> mapper) {
		MvAreaDigest mvAreaDigest = mvAreaRepository.findSingleDigestByPath(sti.areaPath());
		return mapper.apply(mvAreaDigest);
	}
}
