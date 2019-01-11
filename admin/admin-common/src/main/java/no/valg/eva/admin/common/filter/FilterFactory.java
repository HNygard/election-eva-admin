package no.valg.eva.admin.common.filter;

public interface FilterFactory<T> {
	Filter<T> buildFilter();
}
