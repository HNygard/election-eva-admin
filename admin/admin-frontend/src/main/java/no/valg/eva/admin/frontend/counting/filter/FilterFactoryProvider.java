package no.valg.eva.admin.frontend.counting.filter;

import no.valg.eva.admin.common.filter.FilterFactory;

public interface FilterFactoryProvider<F extends FilterFactory, P> {
	F provideFilterFactory(P previousPath);
}
