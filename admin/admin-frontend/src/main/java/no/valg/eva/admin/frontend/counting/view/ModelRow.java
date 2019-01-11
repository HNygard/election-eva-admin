package no.valg.eva.admin.frontend.counting.view;

public interface ModelRow {
	String getAft();

	String getRowStyleClass();

	String getTitle();

	int getCount();

	void setCount(int count);

	boolean isCountInput();
}
