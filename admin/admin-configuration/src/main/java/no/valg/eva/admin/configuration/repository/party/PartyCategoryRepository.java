package no.valg.eva.admin.configuration.repository.party;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.PartyCategory;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public class PartyCategoryRepository extends BaseRepository {
	public PartyCategory findPartyCategoryByPk(long pk) {
		return super.findEntityByPk(PartyCategory.class, pk);
	}

	public PartyCategory findById(String id) {
		return findEntityById(PartyCategory.class, id);
	}
}
