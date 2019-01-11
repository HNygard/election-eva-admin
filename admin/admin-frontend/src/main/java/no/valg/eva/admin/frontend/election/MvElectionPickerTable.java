package no.valg.eva.admin.frontend.election;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import no.evote.dto.MvElectionMinimal;
import no.evote.security.UserData;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.configuration.domain.model.ElectionType;

import org.primefaces.event.UnselectEvent;

/**
 * This class represents a column in the election picker/editor. A click in the column will invoke the setSelectedMvElection method, which populates the next
 * column.
 */
public class MvElectionPickerTable implements Serializable {

	private static final long serialVersionUID = 3007668592611452825L;
	private final int level;
	private final boolean includeContestsAboveMyLevel;
	private final MvElectionService mvElectionService;
	private final UserData userData;
	private List<MvElectionMinimal> mvElections = new ArrayList<>();
	private MvElectionMinimal selectedMvElection;
	private MvElectionPickerTable childTable;
	private ElectionType electionTypeFilter;

	public MvElectionPickerTable(final int level, final MvElectionService mvElectionService, final UserData userData, final boolean includeContestsAboveMyLevel) {
		this.level = level;
		this.mvElectionService = mvElectionService;
		this.userData = userData;
		this.includeContestsAboveMyLevel = includeContestsAboveMyLevel;
	}

	public int getLevel() {
		return level;
	}

	public List<MvElectionMinimal> getMvElections() {
		return mvElections;
	}

	/**
	 * Populate the list of mvElections in this column. Apply electionTypeFilter if set. If list contains only one, set this as selected.
	 */
	public void setMvElections(final List<MvElectionMinimal> mvElections) {
		this.mvElections = mvElections;
		if (mvElections != null) {
			this.mvElections = new ArrayList<>();
			for (MvElectionMinimal mvElection : mvElections) {
				if (electionTypeFilter != null) {
					if (mvElectionService.hasElectionsWithElectionTypeMinimal(mvElection, electionTypeFilter)) {
						this.mvElections.add(mvElection);
					}
				} else {
					this.mvElections.add(mvElection);
				}
			}
			if (this.mvElections.size() == 0) {
				this.mvElections = null;
			} else if (this.mvElections.size() == 1) {
				setSelectedMvElection(this.mvElections.get(0));
			}
		} else {
			this.mvElections = null;
		}
	}

	public Integer getSize() {
		return (mvElections == null ? 0 : mvElections.size());
	}

	public MvElectionMinimal getSelectedMvElection() {
		return selectedMvElection;
	}

	/**
	 * Select an mvElection. Either done by user or implicitly by picker/editor.
	 */
	public void setSelectedMvElection(final MvElectionMinimal mvElection) {
		this.selectedMvElection = mvElection;
		if (childTable != null) {
			// Populate next column
			if (mvElection == null) {
				childTable.setMvElections(null);
			} else {
				childTable.setMvElections(mvElectionService.findByPathAndChildLevelMinimal(userData, selectedMvElection.getPk(), includeContestsAboveMyLevel));
			}
			// Deselect in the next column unless it has only one option
			int childTableSize = childTable.getMvElections() != null ? childTable.getMvElections().size() : 0;
			if (childTableSize != 1) {
				childTable.setSelectedMvElection(null);
			}
		}
	}

	public void deSelectMvElection(final UnselectEvent event) {
		// this must be present, onRowUnselectUpdate won't fire unless a rowUnselectListener is present (PF bug?). See contextPicker.xhtml.
	}

	public MvElectionPickerTable getChildTable() {
		return childTable;
	}

	public void setChildTable(final MvElectionPickerTable childTable) {
		this.childTable = childTable;
	}

	public void setElectionTypeFilter(final ElectionType electionTypeFilter) {
		this.electionTypeFilter = electionTypeFilter;
	}

}
