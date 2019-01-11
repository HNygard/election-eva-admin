package no.valg.eva.admin.frontend.reports;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import no.valg.eva.admin.backend.reporting.jasperserver.GroupSeparator;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;

import org.testng.annotations.Test;

public class SelectableValuesWrapperTest {
	public static final String CONTEST_1 = "Contest 1";
	public static final String CONTEST_2 = "Contest 2";
	public static final String CONTEST_3 = "Contest 3";
	public static final String CONTEST_4 = "Contest 4";
	public static final String CONTEST_4_ID = "04";
	public static final String CONTEST_3_ID = "03";
	public static final String CONTEST_2_ID = "02";
	public static final String CONTEST_1_ID = "01";
	private Map<String, String> parameterIdMap = of("EE1.EG1.EL1", "EE1.EG1");
	private ReportParameter reportParameter = new ReportParameter("EE1.EG1.EL1", "Label 01", null, null);

	@Test
	public void getSelectItems_whenNotAllGroupsHaveOneMember_returnsGroupStructure() {
		List<SelectableReportParameterValue> selectableReportParameterValues = newArrayList(
				new GroupSeparator("Election 1"),
				new SelectableReportParameterValue(CONTEST_1_ID, CONTEST_1),
				new SelectableReportParameterValue(CONTEST_2_ID, CONTEST_2),
				new GroupSeparator("Election 2"),
				new SelectableReportParameterValue(CONTEST_3_ID, CONTEST_3),
				new SelectableReportParameterValue(CONTEST_4_ID, CONTEST_4));

		List<SelectItem> inflatedGroups = new SelectableValuesWrapper(selectableReportParameterValues, parameterIdMap, reportParameter).getSelectItems();
		assertThat(inflatedGroups).hasSize(2);
		assertThat(inflatedGroups.get(0)).isInstanceOf(SelectItemGroup.class);
		assertThat(inflatedGroups.get(1)).isInstanceOf(SelectItemGroup.class);
		assertThat(selectItemsOfGroup(inflatedGroups, 0).length).isEqualTo(2);
		assertThat(selectItemsOfGroup(inflatedGroups, 1)[0].getValue()).isEqualTo(CONTEST_3_ID);
		assertThat(selectItemsOfGroup(inflatedGroups, 1)[1].getValue()).isEqualTo(CONTEST_4_ID);
	}

	@Test
	public void getSelectItems_whenAllGroupsHaveOneMember_returnsOnlyGroupMembers() {
		List<SelectableReportParameterValue> selectableReportParameterValues = newArrayList(
				new GroupSeparator("Election 1"),
				new SelectableReportParameterValue(CONTEST_2_ID, CONTEST_2),
				new GroupSeparator("Election 2"),
				new SelectableReportParameterValue(CONTEST_4_ID, CONTEST_4));
		SelectableValuesWrapper selectableValuesWrapper = new SelectableValuesWrapper(selectableReportParameterValues, parameterIdMap, reportParameter);
		List<SelectItem> selectItems = selectableValuesWrapper.getSelectItems();
		assertThat(selectItems).hasSize(2);
		assertThat(selectItems.get(0)).isInstanceOf(SelectItem.class);
		assertThat(selectItems.get(1)).isInstanceOf(SelectItem.class);
		assertThat(selectableValuesWrapper.getParameterId()).isEqualTo("EE1.EG1");

		selectableReportParameterValues = newArrayList(
				new SelectableReportParameterValue(CONTEST_4_ID, CONTEST_4));
		selectItems = new SelectableValuesWrapper(selectableReportParameterValues, parameterIdMap, reportParameter).getSelectItems();
		assertThat(selectItems).hasSize(1);
		assertThat(selectItems.get(0)).isInstanceOf(SelectItem.class);
	}

	@Test
	public void getSelectItems_withOnlyOneMemberAndNoGroup_returnsOnlyThatMember() {
		List<SelectableReportParameterValue> selectableReportParameterValues = newArrayList(
				new SelectableReportParameterValue(CONTEST_4_ID, CONTEST_4));
		List<SelectItem> selectItems = new SelectableValuesWrapper(selectableReportParameterValues, parameterIdMap, reportParameter).getSelectItems();
		assertThat(selectItems).hasSize(1);
		assertThat(selectItems.get(0)).isInstanceOf(SelectItem.class);
	}

	private SelectItem[] selectItemsOfGroup(List<SelectItem> inflatedGroups, int groupIndex) {
		return ((SelectItemGroup) inflatedGroups.get(groupIndex)).getSelectItems();
	}
}
