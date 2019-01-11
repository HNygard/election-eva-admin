package no.valg.eva.admin.frontend.counting.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import javax.faces.context.FacesContext;

import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.frontend.counting.ctrls.CountController;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;
import no.valg.eva.admin.frontend.counting.ctrls.ProtocolAndPreliminaryCountController;

import org.joda.time.format.DateTimeFormat;

public class DailyMarkOffCountsModel extends ArrayList<ModelRow> implements Serializable {

	private static final String DATE_PATTERN = "EEEE d MMM. yyyy";

	private final CountController ctrl;

	public DailyMarkOffCountsModel(final CountController ctrl) {
		this.ctrl = ctrl;
		for (final DailyMarkOffCount dailyMarkOffCount : ctrl.getDailyMarkOffCounts()) {
			add(new ModelRow() {

				@Override
				public String getAft() {
					return "daily_markoff_count";
				}

				@Override
				public String getRowStyleClass() {
					return "row_daily_markoff_count";
				}

				@Override
				public String getTitle() {
					return DateTimeFormat.forPattern(DATE_PATTERN).withLocale(getLocale()).print(dailyMarkOffCount.getDate());
				}

				@Override
				public int getCount() {
					return dailyMarkOffCount.getMarkOffCount();
				}

				@Override
				public void setCount(int count) {
					if (isCountInput()) {
						dailyMarkOffCount.setMarkOffCount(count);
					}
				}

				@Override
				public boolean isCountInput() {
					return !ctrl.isElectronicMarkOffs();
				}
			});
		}
	}

	public long getSumMarkOffCount() {
		return ctrl.getDailyMarkOffCounts().getMarkOffCount();
	}

	public String getPreviousTabTitle() {
		if (ctrl instanceof ProtocolAndPreliminaryCountController || ctrl instanceof PreliminaryCountController) {
			return "@count.tab.type[P].approved";
		}
		return ctrl.getPreviousTab().getTitle();
	}

	Locale getLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}
}
