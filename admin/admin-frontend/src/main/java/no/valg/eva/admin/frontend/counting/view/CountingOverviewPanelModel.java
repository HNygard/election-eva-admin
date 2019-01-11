package no.valg.eva.admin.frontend.counting.view;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.List;
import java.util.stream.Stream;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.primefaces.model.TreeNode;

public class CountingOverviewPanelModel {
	private final CountingOverviewRoot countingOverviewRoot;
	private final TreeNode treeRoot;
	private final List<CountingOverviewColumnModel> columns;

	public CountingOverviewPanelModel(CountingOverviewRoot countingOverviewRoot, TreeNode treeRoot, CountingOverviewColumnModel... columns) {
		this.countingOverviewRoot = countingOverviewRoot;
		this.treeRoot = treeRoot;
		this.columns = asList(columns);
	}

	public CountingOverviewRoot getCountingOverviewRoot() {
		return countingOverviewRoot;
	}

	public String getTitle() {
		return countingOverviewRoot.getName();
	}

	public String getTitleStyle() {
		return countingOverviewRoot.getStatus().getPanelStyle();
	}

	public TreeNode getTreeRoot() {
		return treeRoot;
	}

	public List<CountingOverviewColumnModel> getColumns() {
		return columns;
	}

	public AreaPath getAreaPath() {
		return countingOverviewRoot.getAreaPath();
	}

	public boolean includesAreaPath(AreaPath areaPath) {
		return areaPath.isSubpathOf(countingOverviewRoot.getAreaPath());
	}

	public void expandTreeIfMatched(CountCategory category, AreaPath areaPath) {
		flattenedTree(treeRoot)
				.filter(treeNode -> isMatching(treeNode, category, areaPath))
				.findFirst()
				.ifPresent(this::expandTreeNodeParents);
	}

	private Stream<TreeNode> flattenedTree(TreeNode treeNode) {
		return Stream.concat(
				Stream.of(treeNode),
				treeNode.getChildren()
						.stream()
						.flatMap(this::flattenedTree));
	}

	private boolean isMatching(TreeNode treeNode, CountCategory category, AreaPath areaPath) {
		CountingOverview countingOverview = (CountingOverview) treeNode.getData();
		return countingOverview != null && category == countingOverview.getCategory() && areaPath.equals(countingOverview.getAreaPath());
	}

	private void expandTreeNodeParents(TreeNode treeNode) {
		TreeNode parentTreeNode = treeNode.getParent();
		if (parentTreeNode != null) {
			parentTreeNode.setExpanded(true);
			expandTreeNodeParents(parentTreeNode);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CountingOverviewPanelModel)) {
			return false;
		}
		CountingOverviewPanelModel that = (CountingOverviewPanelModel) o;
		return new EqualsBuilder()
				.append(countingOverviewRoot, that.countingOverviewRoot)
				.append(treeRoot, that.treeRoot)
				.append(columns, that.columns)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(countingOverviewRoot)
				.append(treeRoot)
				.append(columns)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("countingOverviewRoot", countingOverviewRoot)
				.append("treeRoot", treeRoot)
				.append("columns", columns)
				.toString();
	}
}
