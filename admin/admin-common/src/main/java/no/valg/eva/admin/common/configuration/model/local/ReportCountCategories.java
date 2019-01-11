package no.valg.eva.admin.common.configuration.model.local;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.counting.domain.model.CountCategory;

/**
 * <p>
 * Contains central default settings mixed with local (municipality) settings for report count categories. Local settings may override central settings if
 * category is editable. If not, values from the central category will be used.
 * </p>
 * <p>
 * Categories read from the table election_vote_count_category are referred to as central categories since these are configured on a central level. Categories
 * from the table report_count_category are referred to as local categories because they may contain configuration made locally by the municipalities if they
 * are editable.
 * </p>
 */
public class ReportCountCategories implements Serializable {

	private final Map<String, ReportCountCategory> map;

	/**
	 * Validates input and creates instance. There must at least exist as many central categories as there are local ones.
	 * @param localCategories
	 *            list of ReportCountCategory representing local changes from the municipalities
	 * @param centralCategories
	 *            list of ElectionVoteCountCategory representing default values and flags for enabling categories or making categories editable
	 */
	public ReportCountCategories(final List<ReportCountCategory> localCategories, final List<ElectionVoteCountCategory> centralCategories) {
		if (!valid(localCategories, centralCategories)) {
			throw new IllegalArgumentException(
					"Categories are not valid. No of centralCategories must be higher than or equal to "
							+ "no of localCategories and each local category must exist in list of central");
		}

		map = merge(categoryMap(centralCategories), localCategories);
	}

	private Map<String, ReportCountCategory> merge(final Map<String, ElectionVoteCountCategory> centralMap, final List<ReportCountCategory> localCategories) {
		Map<String, ReportCountCategory> resultMap = new HashMap<>();

		for (ReportCountCategory localCat : localCategories) {
			ElectionVoteCountCategory centralCat = centralMap.remove(localCat.key());
			localCat.setEnabled(centralCat.isCountCategoryEnabled());
			localCat.setTechnicalPollingDistrictCountConfigurable(centralCat.isTechnicalPollingDistrictCountConfigurable());
			if (centralCat.isCountCategoryEditable()) {
				localCat.setEditable(true);
				resultMap.put(localCat.key(), localCat);
			} else {
				resultMap.put(localCat.key(), withCentralValues(localCat, centralCat));
			}
		}

		for (ElectionVoteCountCategory centralCat : centralMap.values()) {
			resultMap.put(centralCat.key(), new ReportCountCategory(centralCat));
		}

		return resultMap;
	}

	private ReportCountCategories(final Map<String, ReportCountCategory> newMap) {
		map = newMap;
	}

	private ReportCountCategory withCentralValues(final ReportCountCategory localCat, final ElectionVoteCountCategory centralCat) {
		localCat.setCentralPreliminaryCount(centralCat.isCentralPreliminaryCount());
		localCat.setPollingDistrictCount(centralCat.isPollingDistrictCount());
		localCat.setSpecialCover(centralCat.isSpecialCover());
		localCat.setEditable(centralCat.isCountCategoryEditable());
		return localCat;
	}

	private Map<String, ElectionVoteCountCategory> categoryMap(final List<ElectionVoteCountCategory> categories) {
		Map<String, ElectionVoteCountCategory> catMap = new HashMap<>();
		for (ElectionVoteCountCategory cat : categories) {
			catMap.put(cat.key(), cat);
		}
		return catMap;
	}

	private boolean valid(final List<ReportCountCategory> localCategories, final List<ElectionVoteCountCategory> centralCategories) {
		return localCategories.size() <= centralCategories.size() && voteCountCatIds(centralCategories).containsAll(voteCountCatIds(localCategories));
	}

	private Set<String> voteCountCatIds(final List<? extends CountCategory> cats) {
		Set<String> idSet = new HashSet<>();
		for (CountCategory cat : cats) {
			idSet.add(cat.key());
		}
		return idSet;
	}

	/**
	 * @return list of enabled categories
	 */
	public List<ReportCountCategory> list() {
		List<ReportCountCategory> enabledCats = new ArrayList<>();
		for (ReportCountCategory cat : map.values()) {
			if (cat.isEnabled()) {
				enabledCats.add(cat);
			}
		}
		return enabledCats;
	}

	/**
	 * Filters count category based on criteria.
	 * @param criteria
	 *            filtering criteria
	 * @return ReportCountCategories without count categories removed by filter
	 */
	public ReportCountCategories filter(final Criteria criteria) {
		Map<String, ReportCountCategory> newMap;
		if (criteria.isSamiParentMunicipality()) {
			newMap = new HashMap<>();
			if (map.containsKey(FO.getId())) {
				newMap.put(FO.getId(), map.get(FO.getId()));
			}
			if (map.containsKey(FS.getId())) {
				newMap.put(FS.getId(), map.get(FS.getId()));
			}
		} else {
			newMap = new HashMap<>(map);
		}
		if (criteria.isElectronicMarkOffs()) {
			newMap.remove(VF.getId());
		} else {
			newMap.remove(VB.getId());
		}

		return new ReportCountCategories(newMap);
	}

	/**
	 * Contains filtering criteria.
	 */
	public static class Criteria {
		private final boolean electronicMarkOffs;
		private final boolean samiParentMunicipality;

		public Criteria(final boolean electronicMarkOffs, boolean samiParentMunicipality) {
			this.electronicMarkOffs = electronicMarkOffs;
			this.samiParentMunicipality = samiParentMunicipality;
		}

		public boolean isElectronicMarkOffs() {
			return electronicMarkOffs;
		}

		public boolean isSamiParentMunicipality() {
			return samiParentMunicipality;
		}
	}
}
