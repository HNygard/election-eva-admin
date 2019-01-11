package no.valg.eva.admin.common.configuration.model.local;

import java.io.Serializable;
import java.util.Comparator;

public class PlaceSortById<T extends Place> implements Comparator<T>, Serializable {
	@Override
	public int compare(T place1, T place2) {
		return place1.getId().compareTo(place2.getId());
	}
}
