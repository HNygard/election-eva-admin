package no.valg.eva.admin.frontend.common.search;

import java.util.List;

public interface SearchHandler<T> {
    void onSearchCallback(List<T> entities);
}
