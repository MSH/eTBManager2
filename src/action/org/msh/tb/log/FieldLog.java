package org.msh.tb.log;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotation that gives extra information about the field being logged. By default all fields in the entity
 * will be logged if changed, but additional information may be included depending on the situation
 * @author Ricardo Memoria
 *
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface FieldLog {

	/**
	 * field value will be ignored and its value won't be logged
	 * @return
	 */
	boolean ignore() default false;

	/**
	 * Force log value to use this key when logging value. This key must match a key in the messages file
	 * @return
	 */
	String key() default "";

	/**
	 * If field is another entity, it indicates if log values of the entity will also be logged (true) or
	 * the value of the entity as a string (using toString) will be used for logging (false). Default value is false
	 * @return
	 */
	boolean logFields() default false;


	/**
	 * Extra message key to be displayed at the left side of the original message key
	 * @return
	 */
	String extraKey() default "";
}
