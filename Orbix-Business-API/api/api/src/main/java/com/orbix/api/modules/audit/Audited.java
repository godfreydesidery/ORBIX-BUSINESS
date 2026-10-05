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

	/**
	 * For a change to an existing record: the record's entity class. Its columns are read before and after the action,
	 * and those that changed are written to the details as "before" and "after" (all of them as "before" when the record
	 * was deleted). Passwords, tokens, secrets and binary data such as logos are never read.
	 */
	Class<?> changeOf() default void.class;

	/** Expression giving the id of that record, read before the action, e.g. "productRequest.id" */
	String changeId() default "";

	/**
	 * For a record the request names by other keys than its id: a JPQL query giving the ids of the matching records,
	 * with ?1, ?2 ... set from changeKeys. Used when changeId gives no id. When several records match (e.g. the same
	 * product in several branches), the one whose id the action returns is used.
	 * e.g. "select p.id from ShopProduct p where p.shop.id = ?1 and p.product.id = ?2"
	 */
	String changeQuery() default "";

	/** Expressions giving the values of changeQuery's parameters, in order, e.g. {"shopProductRequest.shopId", "shopProductRequest.productId"} */
	String[] changeKeys() default {};

	/** When a column whose name matches this pattern changed, the entry is recorded as changedAction instead of action */
	String changedFieldPattern() default "";

	/** e.g. PRICE_CHANGED */
	String changedAction() default "";
}
