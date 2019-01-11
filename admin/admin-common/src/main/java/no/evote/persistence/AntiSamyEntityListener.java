package no.evote.persistence;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.naming.NamingException;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import no.evote.model.BaseEntity;
import no.valg.eva.admin.util.AntiSamyFilter;
import no.evote.validation.AntiSamy;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

/**
 * Apply antiSamy filter when storing and retrieving data. See http://www.owasp.org/index.php/Category:OWASP_AntiSamy_Project for more information on AntiSamy.
 */
public class AntiSamyEntityListener implements Serializable {

	private static final String[] LINE_SEPARATORS = { "\r\n", "\r" };
	private static final String LINE_SEPARATOR_PLACEHOLDER = "__NEWLINE__";

	@PrePersist
	public void doPrePersist(final BaseEntity entity) throws NamingException, IllegalAccessException {
		applyAntiSamyFilter(entity);
	}

	@PreUpdate
	public void doPreUpdate(final BaseEntity entity) throws NamingException, IllegalAccessException {
		applyAntiSamyFilter(entity);
	}

	@PostLoad
	public void doPostLoad(final BaseEntity entity) throws IllegalAccessException {
		applyAntiSamyFilter(entity);
	}

	private void applyAntiSamyFilter(final BaseEntity entity) throws IllegalAccessException {
		if (handleTypeAnnotated(entity)) {
			return;
		}

		for (Field field : entity.getClass().getDeclaredFields()) {
			// Only filter fields annotated with @AntiSamy
			if (field.getAnnotation(AntiSamy.class) != null) {
				field.setAccessible(true);
				field.set(entity, AntiSamyFilter.filter((String) field.get(entity)));
			}
		}
	}

	private boolean handleTypeAnnotated(BaseEntity baseEntity) {
		if (baseEntity.getClass().getAnnotation(AntiSamy.class) == null) {
			return false;
		}
		if (baseEntity instanceof Municipality) {
			handleMunicipality((Municipality) baseEntity);
		}
		if (baseEntity instanceof PollingPlace) {
			handlePollingPlace((PollingPlace) baseEntity);
		}
		return true;
	}

	private void handleMunicipality(Municipality municipality) {
		// Allow newline (default stripped away by AntiSamy) on infoText if advance pollingplace.
		String actualLineSep = null;
		if (municipality.getElectionCardText() != null) {
			for (String lineSep : LINE_SEPARATORS) {
				if (municipality.getElectionCardText().contains(lineSep)) {
					actualLineSep = lineSep;
					municipality.setElectionCardText(municipality.getElectionCardText().replace(actualLineSep, LINE_SEPARATOR_PLACEHOLDER));
				}
			}
			municipality.setElectionCardText(AntiSamyFilter.filter(municipality.getElectionCardText()));
			if (actualLineSep != null) {
				municipality.setElectionCardText(municipality.getElectionCardText().replace(LINE_SEPARATOR_PLACEHOLDER, actualLineSep));
			}
		}
	}

	private void handlePollingPlace(PollingPlace pollingPlace) {
		// Allow newline (default stripped away by AntiSamy) on infoText if advance pollingplace.
		String actualLineSep = null;
		if (pollingPlace.getInfoText() != null) {
			for (String lineSep : LINE_SEPARATORS) {
				if (pollingPlace.getInfoText().contains(lineSep)) {
					actualLineSep = lineSep;
					pollingPlace.setInfoText(pollingPlace.getInfoText().replace(actualLineSep, LINE_SEPARATOR_PLACEHOLDER));
				}
			}
			pollingPlace.setInfoText(AntiSamyFilter.filter(pollingPlace.getInfoText()));
			if (actualLineSep != null) {
				pollingPlace.setInfoText(pollingPlace.getInfoText().replace(LINE_SEPARATOR_PLACEHOLDER, actualLineSep));
			}
		}
	}

}
