package no.evote.presentation.filter;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * A custom filter can be applied to the area picker/editor. The filter method is invoked when the picker table is populated.
 */
@Deprecated
public interface MvAreaFilter {

	List<MvArea> filter(final List<MvArea> mvAreas, final int level);

}
