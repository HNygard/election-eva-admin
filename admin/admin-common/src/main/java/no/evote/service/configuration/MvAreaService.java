package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.evote.service.cache.Cacheable;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;

public interface MvAreaService extends Serializable {
	MvArea findRoot(Long eepk);

	List<MvArea> findByPathAndChildLevel(MvArea mvArea);

	@Deprecated
	List<MvArea> findByPathAndLevel(String path, int level);

	List<MvArea> findByPathAndLevel(ValggeografiSti valggeografiSti, AreaLevelEnum level);

	/**
	 * @deprecated use MvAreaService#findSingleByPath(AreaPath) instead
	 */
	@Deprecated
	MvArea findSingleByPath(String path);

	@Deprecated
	MvArea findSingleByPath(AreaPath path);

	MvArea findSingleByPath(ValggeografiSti valggeografiSti);

	@Deprecated
	MvArea findSingleByPath(String electionEventId, AreaPath path);

	MvArea findByPk(UserData userData, Long pk);

	@Cacheable
	List<AreaLevel> findAllAreaLevels(UserData userData);

	MvArea findByMunicipalityAndPollingPlaceId(UserData userData, Long municipalityPk, String pollingPlaceId);

	List<ValgdistriktSti> findValgdistriktStierByValgStiWhereAllListProposalsAreApproved(UserData userData, ValgSti valgSti);
}
