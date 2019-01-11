package no.valg.eva.admin.frontend.configuration.converters;

import java.util.List;

import no.valg.eva.admin.common.configuration.model.local.Place;

public interface PlaceConverterSource<T extends Place> {

	PlaceConverter getPlaceConverter();

	List<T> getPlaces();
}
