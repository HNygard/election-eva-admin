package no.evote.service.backendmock;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used by {@link no.evote.service.backendmock.BackendWirer}.
 * <p/>
 * Specify either {@link #impl()} or {@link #producer()}. The behaviour is unspecified if you specify both.
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface Wired {
	/**
	 * Specifies which implementation class should be used for the wiring.
	 * Default value is the type of the annotated field.
	 */
	Class impl() default Unassigned.class;

	/**
	 * Specifies which producer should be used for the wiring.
	 */
	Class producer() default Unassigned.class;

	/**
	 * Default value that flags that no value is assigned to {@link #impl()} or {@link #producer()}.
	 */
	class Unassigned {
	};
}
