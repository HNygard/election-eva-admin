package no.valg.eva.admin.frontend.reports;

import static com.google.common.collect.Collections2.transform;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import no.valg.eva.admin.backend.reporting.jasperserver.GroupSeparator;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;

import com.google.common.base.Function;

public class SelectableValuesWrapper {
	public static final SelectableValuesWrapper EMPTY = new SelectableValuesWrapper(EMPTY_LIST);
	private final List<SelectItem> selectItems;
	private final boolean allSelectGroupsHaveOnlyOneItem;
	private final ReportParameter reportParameter;
	private Map<String, String> canonicalReportParameterParentIdMap;

	public SelectableValuesWrapper(Collection<SelectableReportParameterValue> selectItems, Map<String, String> canonicalReportParameterParentIdMap,
			ReportParameter reportParameter) {
		this.canonicalReportParameterParentIdMap = canonicalReportParameterParentIdMap;
		this.reportParameter = reportParameter;
		this.selectItems = selectableParametersToSelectItems(selectItems);
		this.allSelectGroupsHaveOnlyOneItem = isAllSelectGroupsHaveOnlyOneItem(selectItems);
	}

	private SelectableValuesWrapper(List emptyList) {
		selectItems = emptyList;
		allSelectGroupsHaveOnlyOneItem = false;
		reportParameter = null;
	}

	public static SelectableValuesWrapper empty(ReportParameter reportParameter) {
		return new SelectableValuesWrapper(EMPTY_LIST, EMPTY_MAP, reportParameter);
	}

	private List<SelectItem> selectableParametersToSelectItems(Collection<SelectableReportParameterValue> selectableValuesForParameter) {
		List<SelectItem> result;
		boolean isAllSelectGroupsHaveOnlyOneItem = isAllSelectGroupsHaveOnlyOneItem(selectableValuesForParameter);
		if (isAllSelectGroupsHaveOnlyOneItem) {
			result = new ArrayList<>();
			for (Iterator<SelectableReportParameterValue> iterator = selectableValuesForParameter.iterator(); iterator.hasNext();) {
				SelectableReportParameterValue group = iterator.next();
				SelectableReportParameterValue value = iterator.next();
				result.add(new SelectItem(value.getValueId(), group.getLabel()));
			}
		} else {
			result = new ArrayList<>(transform(selectableValuesForParameter,
					new Function<SelectableReportParameterValue, SelectItem>() {
						@Override
						public SelectItem apply(SelectableReportParameterValue input) {
							return !(input instanceof GroupSeparator)
									? new SelectItem(input.getValueId(), input.getLabel())
									: new SelectItemGroup(input.getLabel());
						}
					}));
			result = inflateSelectGroups(result);
		}
		return result;
	}

	private boolean isAllSelectGroupsHaveOnlyOneItem(Collection<SelectableReportParameterValue> selectableValuesForParameter) {
		if (selectableValuesForParameter.size() < 2) {
			return false;
		}
		int selectItemCounter = 0;
		boolean isAllSelectGroupsHaveOnlyOneItem = false;
		for (SelectableReportParameterValue parameterValue : selectableValuesForParameter) {
			if (parameterValue instanceof GroupSeparator) {
				selectItemCounter = 0;
			} else {
				if (selectItemCounter == 1) {
					isAllSelectGroupsHaveOnlyOneItem = false;
					break;
				} else {
					selectItemCounter++;
					isAllSelectGroupsHaveOnlyOneItem = true;
				}
			}
		}
		return isAllSelectGroupsHaveOnlyOneItem;
	}

	private List<SelectItem> inflateSelectGroups(List<SelectItem> selectItems) {
		LinkedList<SelectItem> retVal = new LinkedList<>();
		SelectItemGroup currentGroup = null;
		for (SelectItem selectItem : selectItems) {
			if (selectItem instanceof SelectItemGroup) {
				currentGroup = (SelectItemGroup) selectItem;
				retVal.add(currentGroup);
			} else if (currentGroup != null) {
				addSelectItemToCurrentGroup(selectItem, currentGroup);
			} else {
				retVal.add(selectItem);
			}
		}
		return retVal;
	}

	private void addSelectItemToCurrentGroup(SelectItem selectItem, SelectItemGroup currentGroup) {
		SelectItem[] selectItemArray = currentGroup.getSelectItems();
		if (selectItemArray == null) {
			selectItemArray = new SelectItem[1];
		} else {
			selectItemArray = Arrays.copyOf(selectItemArray, selectItemArray.length + 1);
		}
		selectItemArray[selectItemArray.length - 1] = selectItem;
		currentGroup.setSelectItems(selectItemArray);
	}

	public List<SelectItem> getSelectItems() {
		return selectItems;
	}

	public String getParameterId() {
		return allSelectGroupsHaveOnlyOneItem ? canonicalReportParameterParentIdMap.get(reportParameter.getId()) : reportParameter.getId();
	}
}
