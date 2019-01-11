package no.valg.eva.admin.frontend.counting.view;

import lombok.EqualsAndHashCode;
import no.valg.eva.admin.frontend.counting.ctrls.PreliminaryCountController;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = true)
public class LateValidationCoversModel extends ArrayList<ModelRow> {

    private final PreliminaryCountController ctrl;

    public LateValidationCoversModel(final PreliminaryCountController ctrl) {
        this.ctrl = ctrl;
        super.add(new ModelRow() {

            @Override
            public String getAft() {
                return "markoff_count";
            }

            @Override
            public String getRowStyleClass() {
                return "row_markoff_count";
            }

            @Override
            public String getTitle() {
                return "@count.ballot.electoral_roll";
            }

            @Override
            public int getCount() {
                return ctrl.getCounts().getMarkOffCount();
            }

            @Override
            public void setCount(int count) {
                //Hvorfor er denne tom???
            }

            @Override
            public boolean isCountInput() {
                return false;
            }
        });
        super.add(new ModelRow() {

            @Override
            public String getAft() {
                return "late_validation_covers";
            }

            @Override
            public String getRowStyleClass() {
                return "row_late_validation_covers";
            }

            @Override
            public String getTitle() {
                return "@count.ballot.late_validation_covers";
            }

            @Override
            public int getCount() {
                return ctrl.getCount().getLateValidationCovers();
            }

            @Override
            public void setCount(int count) {
                ctrl.getPreliminaryCount().setLateValidationCovers(count);
            }

            @Override
            public boolean isCountInput() {
                return false;
            }
        });
    }

    public int getTotalMarkOffCount() {
        return ctrl.getTotalMarkOffCount();
    }

    public String getCountCategory() {
        return ctrl.getCountContext().getCategory().getId();
    }

}
