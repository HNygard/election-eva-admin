package no.valg.eva.admin.frontend.area;

import java.util.ArrayList;
import java.util.List;

import no.evote.presentation.filter.MvAreaFilter;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;

import org.primefaces.event.UnselectEvent;

/**
 * This class represents a column in the election picker/editor. A click in the column will invoke the setSelectedMvElection method, which populates the next
 * column.
 * 
 * @see no.valg.eva.admin.frontend.election.MvElectionPickerTable
 */
public class MvAreaPickerTable {

	private final List<MvAreaFilter> mvAreaFilters;
	private int level;
	private List<MvArea> mvAreas = new ArrayList<MvArea>();
	private List<MvArea> allowedMvArea;
	private MvArea selectedMvArea;
	private MvElection selectedMvElection;
	private MvAreaPickerTable childTable;
	private MvAreaService mvAreaService;

	/**
	 * @param mvAreaFilters
	 *            custom filter
	 *
	 */
	public MvAreaPickerTable(final List<MvAreaFilter> mvAreaFilters) {
		this.mvAreaFilters = mvAreaFilters;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(final int level) {
		this.level = level;
	}

	public List<MvArea> getMvAreas() {
		return mvAreas;
	}

	/**
	 * Populate the list of mvAreas in this column. If list contains only one, set this as selected.
	 */
	public void setMvAreas(final List<MvArea> mvAreas) {
		this.mvAreas = mvAreas;
		if (mvAreaFilters != null && this.mvAreas != null && !mvAreaFilters.isEmpty()) {
			for (MvAreaFilter filter : mvAreaFilters) {
				this.mvAreas = filter.filter(this.mvAreas, level);
			}
		}

		if (this.mvAreas != null && this.mvAreas.size() == 1) {
			setSelectedMvArea(this.mvAreas.get(0));
		}
	}

	public Integer getSize() {
		return (mvAreas == null ? 0 : mvAreas.size());
	}

	public MvArea getSelectedMvArea() {
		return selectedMvArea;
	}

	/**
	 * Select an mvArea. Either done by user or implicitly by picker/editor.
	 */
	public void setSelectedMvArea(final MvArea mvArea) {
		this.selectedMvArea = mvArea;
		if (childTable != null) {
			if (mvArea == null) {
				childTable.setMvAreas(null);
			} else {
				List<MvArea> allAreas = null;
				allAreas = mvAreaService.findByPathAndChildLevel(selectedMvArea);
				childTable.setMvAreas(allAreas);
			}
			int childTableSize = childTable.getMvAreas() != null ? childTable.getMvAreas().size() : 0;
			if (childTableSize != 1) {
				childTable.setSelectedMvArea(null);
			}
		}
	}

	public void deSelectMvArea(final UnselectEvent event) {
		// this must be present, onRowUnselectUpdate won't fire unless a rowUnselectListener is present (PF bug?). See contextPicker.xhtml.
	}

	public MvAreaPickerTable getChildTable() {
		return childTable;
	}

	public void setChildTable(final MvAreaPickerTable childTable) {
		this.childTable = childTable;
	}

	public MvAreaService getMvAreaService() {
		return mvAreaService;
	}

	public void setMvAreaService(final MvAreaService mvAreaService) {
		this.mvAreaService = mvAreaService;
	}

	public MvElection getSelectedMvElection() {
		return selectedMvElection;
	}

	public void setSelectedMvElection(final MvElection selectedMvElection) {
		this.selectedMvElection = selectedMvElection;
	}

	public List<MvArea> getAllowedMvArea() {
		return allowedMvArea;
	}

	public void setAllowedMvArea(final List<MvArea> allowedMvArea) {
		this.allowedMvArea = allowedMvArea;
	}
}
