package no.valg.eva.admin.test;

import org.assertj.core.api.AbstractAssert;

public class ObjectAssert extends AbstractAssert<ObjectAssert, Object> {

	public static ObjectAssert assertThat(Object actual) {
		return new ObjectAssert(actual);
	}

	protected ObjectAssert(final Object actual) {
		super(actual, ObjectAssert.class);
	}
}
