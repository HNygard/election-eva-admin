package no.valg.eva.admin.frontend.counting.view;

import java.io.Serializable;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.frontend.common.MarkupUtils;

public class CompareBallotCountView implements Serializable {

	public enum BallotCountViewType {
		UNKNOWN, BALLOT_COUNT, REJECTED_BALLOT_COUNT;
	}

	private String nameKey;
	private String id;
	private Integer count;
	private Integer modifiedCount;
	private Integer unmodifiedCount;
	private Integer diff;
	private String styleClass = "";
	private BallotCountViewType type = BallotCountViewType.UNKNOWN;

	public CompareBallotCountView() {
		;
	}

	public CompareBallotCountView(BallotCount ballotCount) {
		this(ballotCount.getName(), ballotCount.getId(), ballotCount.getCount(), ballotCount.getModifiedCount(), ballotCount.getUnmodifiedCount());
		this.type = BallotCountViewType.BALLOT_COUNT;
	}

	public CompareBallotCountView(String nameKey) {
		this(nameKey, null);
		markBold();
	}

	public CompareBallotCountView(Integer count) {
		this(null, null, count);
	}

	public CompareBallotCountView(String nameKey, Integer count) {
		this(nameKey, null, count);
	}

	public CompareBallotCountView(String nameKey, String id, Integer count) {
		this(nameKey, id, count, null, null);
	}

	public CompareBallotCountView(String nameKey, String id, Integer count, Integer modifiedCount, Integer unmodifiedCount) {
		this.nameKey = nameKey;
		this.id = id;
		this.count = count;
		this.modifiedCount = modifiedCount;
		this.unmodifiedCount = unmodifiedCount;
	}

	public final CompareBallotCountView markBold() {
		styleClass = (styleClass + " bold").trim();
		return this;
	}

	public String getNameKey() {
		return nameKey;
	}

	public String getId() {
		return id;
	}

	public Integer getCount() {
		return count;
	}

	public Integer getModifiedCount() {
		return modifiedCount;
	}

	public Integer getUnmodifiedCount() {
		return unmodifiedCount;
	}

	public void setDiff(Integer diff) {
		this.diff = diff;
	}

	public Integer getDiff() {
		return diff;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public BallotCountViewType getType() {
		return type;
	}

	public void setType(BallotCountViewType type) {
		this.type = type;
	}

	public boolean isRejectedBallotCountType() {
		return type == BallotCountViewType.REJECTED_BALLOT_COUNT;
	}

	public String getStyleClassForDiff() {
		int d = diff == null ? 0 : diff;
		StringBuilder sb = new StringBuilder();
		sb.append(styleClass);
		sb.append(" ");
		sb.append(MarkupUtils.getClass(d));
		return sb.toString().trim();
	}
}
