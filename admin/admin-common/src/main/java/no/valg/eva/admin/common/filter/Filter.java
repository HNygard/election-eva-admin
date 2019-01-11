package no.valg.eva.admin.common.filter;

import java.io.Serializable;

@FunctionalInterface
public interface Filter<T> extends Serializable {
	boolean filter(T t);
}
