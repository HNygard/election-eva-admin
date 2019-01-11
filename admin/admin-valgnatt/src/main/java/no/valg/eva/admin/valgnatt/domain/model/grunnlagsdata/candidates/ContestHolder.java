package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates;

import java.util.Collection;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * Contains contests (for a given election) to report for
 */
public class ContestHolder {
	
	private final Collection<Contest> contests;
	private final Map<Long, MvArea> contestMvAreaMap;
	
	public ContestHolder(Collection<Contest> contests, Map<Long, MvArea> contestMvAreaMap) {
		this.contests = contests;
		this.contestMvAreaMap = contestMvAreaMap;
	}

	public Collection<Contest> getContests() {
		return contests;
	}

	public MvArea getMvArea(final Long contestPk) {
		return contestMvAreaMap.get(contestPk);
	}
}
