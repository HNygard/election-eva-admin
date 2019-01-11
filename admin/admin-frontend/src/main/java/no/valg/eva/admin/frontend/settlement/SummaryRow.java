package no.valg.eva.admin.frontend.settlement;

public interface SummaryRow {

	String getStyleClass();

	String getName();

	String getId();

	String getRowStyleClass();

	boolean isHasCategory(int index);

	String getCategoryName(int index);

	String getModifiedCount(int index);

	String getUnmodifiedCount(int index);

	String getCount(int index);

	String getTotalModifiedCount();

	String getTotalUnmodifiedCount();

	String getTotalCount();
}
