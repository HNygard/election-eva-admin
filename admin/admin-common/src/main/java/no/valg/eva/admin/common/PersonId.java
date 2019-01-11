package no.valg.eva.admin.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object for personal Id number, typically a Fødselsnummer in Norway (and throughout the EVA Admin system),
 * Personnummer in Sweden, CPR-nummer in Denmark, Personbeteckning in Finland,
 * National Insurence number in Great Britain or Social Security number in the US.
 */
public class PersonId implements Serializable {
	protected final String id;

	public PersonId(String id) {
		this.id = Objects.requireNonNull(id);
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
            return true;
        }
		if (!(o instanceof PersonId)) {
            return false;
        }

		PersonId personId = (PersonId) o;

		if (!id.equals(personId.id)) {
            return false;
        }

		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
