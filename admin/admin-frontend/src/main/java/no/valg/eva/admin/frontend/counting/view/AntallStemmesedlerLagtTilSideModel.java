package no.valg.eva.admin.frontend.counting.view;

import java.util.ArrayList;

import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSideForValg;
import no.valg.eva.admin.frontend.counting.ctrls.AntallStemmesedlerLagtTilSideController;

public class AntallStemmesedlerLagtTilSideModel extends ArrayList<ModelRow> {
	public AntallStemmesedlerLagtTilSideModel(AntallStemmesedlerLagtTilSideController ctrl) {
		initModel(ctrl);
	}

	private void initModel(AntallStemmesedlerLagtTilSideController ctrl) {
		AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = ctrl.getAntallStemmesedlerLagtTilSide();
		if (antallStemmesedlerLagtTilSide == null) {
			return;
		}
		for (AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg : antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgList()) {
			add(new ModelRow() {
				@Override
				public String getAft() {
					return "antallStemmesedler_" + antallStemmesedlerLagtTilSideForValg.getElectionPath().lastId();
				}

				@Override
				public String getRowStyleClass() {
					return "row_antall_stemmesedler_lagt_til_side";
				}

				@Override
				public String getTitle() {
					return antallStemmesedlerLagtTilSideForValg.getNavn();
				}

				@Override
				public int getCount() {
					return antallStemmesedlerLagtTilSideForValg.getAntallStemmesedler();
				}

				@Override
				public void setCount(int count) {
					antallStemmesedlerLagtTilSideForValg.setAntallStemmesedler(count);
				}

				@Override
				public boolean isCountInput() {
					return true;
				}
			});
		}
	}
}
