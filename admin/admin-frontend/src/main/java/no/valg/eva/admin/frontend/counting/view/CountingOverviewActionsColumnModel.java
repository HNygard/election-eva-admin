package no.valg.eva.admin.frontend.counting.view;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class CountingOverviewActionsColumnModel extends CountingOverviewColumnModel {
	static final String COUNTING_BASE_URL = "/secure/counting/startCounting.xhtml";
	static final String COUNTING_URL_PARAMETERS = "category=%s&contestPath=%s&areaPath=%s&pickerElectionPath=%s&pickerAreaPath=%s";
	static final String MANUAL_REJECTED_BASE_URL = "/secure/counting/approveManualRejectedCount.xhtml";
	static final String SCANNED_REJECTED_BASE_URL = "/secure/counting/approveScannedRejectedCount.xhtml";
	static final String REJECTED_URL_PARAMETERS = "fromOverview=true&" + COUNTING_URL_PARAMETERS;

	private final ReportingUnitTypeId reportingUnitTypeId;
	private final AreaLevelEnum pickerAreaLevel;

	public CountingOverviewActionsColumnModel(ReportingUnitTypeId reportingUnitTypeId, AreaLevelEnum pickerAreaLevel) {
        super("@common.action");
		this.reportingUnitTypeId = reportingUnitTypeId;
		this.pickerAreaLevel = pickerAreaLevel;
		if (pickerAreaLevel != COUNTY && pickerAreaLevel != MUNICIPALITY) {
			throw new IllegalArgumentException(
					format("forventet at pickerAreaLevel <%s> skal være enten <%s> eller <%s>", pickerAreaLevel, COUNTY, MUNICIPALITY));
		}
	}

	@Override
	public String getStyle() {
		return "width: 200px;";
	}

	@Override
	public List<ColumnOverviewItemModel> itemsFor(CountingOverview countingOverview) {
		if (!countingOverview.hasCount()) {
			return emptyList();
        }
		if (!countingOverview.isRejectedBallotsPending()) {
			return singletonList(goToCount(countingOverview));
		}
		if (countingOverview.isManualRejectedBallotsPending()) {
			return asList(goToCount(countingOverview), goToRejectedManual(countingOverview));
		}
		return asList(goToCount(countingOverview), goToRejectedScanned(countingOverview));
	}

	private ColumnOverviewItemModel goToCount(CountingOverview countingOverview) {
		String url = buildUrl(countingOverview, COUNTING_BASE_URL, COUNTING_URL_PARAMETERS);
        return new ColumnOverviewLinkItemModel(url, "@common.view");
	}

	private ColumnOverviewItemModel goToRejectedManual(CountingOverview countingOverview) {
		String url = buildUrl(countingOverview, MANUAL_REJECTED_BASE_URL, REJECTED_URL_PARAMETERS);
		return new ColumnOverviewLinkItemModel(url, "@count.overview.rejected");
	}

	private ColumnOverviewItemModel goToRejectedScanned(CountingOverview countingOverview) {
		String url = buildUrl(countingOverview, SCANNED_REJECTED_BASE_URL, REJECTED_URL_PARAMETERS);
		return new ColumnOverviewLinkItemModel(url, "@count.overview.rejected.scanned");
	}

	private String buildUrl(CountingOverview countingOverview, String baseUrl, String urlParameters) {
		CountCategory category = countingOverview.getCategory();
		ElectionPath contestPath = countingOverview.getContestPath();
		AreaPath areaPath = countingOverview.getAreaPath();
		ElectionPath pickerElectionPath = contestPath.toElectionPath();
		if (reportingUnitTypeId != null) {
			AreaPath pickerAreaPath = areaPath.toCountyPath();
			return format(url(baseUrl, urlParameters) + "&reportingUnitType=%s", category, contestPath, areaPath, pickerElectionPath, pickerAreaPath,
					reportingUnitTypeId);
		}
		AreaPath pickerAreaPath;
		if (pickerAreaLevel == COUNTY) {
			pickerAreaPath = areaPath.toCountyPath();
		} else {
			pickerAreaPath = areaPath.toMunicipalityPath();
		}
		return format(url(baseUrl, urlParameters), category, contestPath, areaPath, pickerElectionPath, pickerAreaPath);
	}

	private String url(String baseUrl, String urlParameters) {
		return format("%s?%s", baseUrl, urlParameters);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingOverviewActionsColumnModel)) {
			return false;
		}
		CountingOverviewActionsColumnModel that = (CountingOverviewActionsColumnModel) o;
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(reportingUnitTypeId, that.reportingUnitTypeId)
				.append(pickerAreaLevel, that.pickerAreaLevel)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(reportingUnitTypeId)
				.append(pickerAreaLevel)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("reportingUnitTypeId", reportingUnitTypeId)
				.append("pickerAreaLevel", pickerAreaLevel)
				.toString();
	}
}
