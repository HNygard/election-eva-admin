package no.evote.security;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SecureEntity {
	AreaLevelEnum areaLevel() default AreaLevelEnum.NONE;

	ElectionLevelEnum electionLevel() default ElectionLevelEnum.NONE;

	boolean areaLevelDynamic() default false;

	boolean electionLevelDynamic() default false;

	Class<?> entity() default Object.class;
}
