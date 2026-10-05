package com.orbix.api.modules.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Records a critical action in the audit log when the annotated method returns normally.
 * The entry is written right after the action's transaction commits, in a transaction of its own:
 * a rolled back action leaves no entry, and a failure to write the entry never fails the action.
 *
 * Values are written as expressions: a parameter name ("payRefNo"), a property of a parameter
 * ("discountRequest.id"), the method's result ("result", "result.id"; a ResponseEntity is unwrapped to its body),
 * or ".size" of a collection. A property of a collection gives the list of that property of its elements
 * ("billReceivableRequests.no").
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {

	/** SECURITY, FINANCE, OPERATIONS, INVENTORY, PROCUREMENT, SALES or SETTINGS */
	String category();

	/** e.g. PAYMENT_CONFIRMED */
	String action();

	/** The kind of record acted on, e.g. "BillReceivable" */
	String entityType() default "";

	/** Expression giving the record's id */
	String entityId() default "result.id";

	/** Expression giving a readable reference to the record, e.g. "result.username" */
	String entityRef() default "";

	/** One readable line; each {expression} is replaced by its value */
	String summary();

	/** Values copied into the entry's details, as "expression" or "name=expression"; "present:expression" gives true when the value is not empty */
	String[] details() default {};
}
