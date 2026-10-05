package com.orbix.api.modules.audit;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Writes an audit log entry for each method marked @Audited that returns normally.
 * The method's result and exceptions pass through unchanged; building the entry never fails the action
 * (a value that cannot be read is left empty).
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)\\}");
	private static final String PRESENT = "present:";
	// Never read into the audit log
	private static final Pattern SENSITIVE = Pattern.compile("(?i).*(password|token|secret|logo).*");
	// Columns used as the readable reference of a changed record when the annotation names none
	private static final String[] REFERENCE_COLUMNS = {"no", "username", "code", "name", "chasisNo"};
	// Records read at most when a change's keys match several (e.g. the same product in several branches)
	private static final int MAX_MATCHING_RECORDS = 20;

	// Looked up when first needed, so creating this aspect does not create the services early
	private final ObjectProvider<AuditLogService> auditLogService;
	private final ObjectProvider<ObjectMapper> objectMapper;
	private final ObjectProvider<PlatformTransactionManager> transactionManager;

	@PersistenceContext
	private EntityManager entityManager;

	@Around(value = "@annotation(audited)", argNames = "audited")
	public Object recordAction(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
		Map<String, Object> values = new HashMap<>();
		// The changed record's columns before the action, by its id (more than one only when its keys match several records)
		Map<String, Map<String, Object>> recordsBefore = null;
		try {
			String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
			Object[] args = joinPoint.getArgs();
			for(int i = 0; names != null && i < names.length && i < args.length; i++) {
				values.put(names[i], args[i]);
			}
			recordsBefore = readRecordsBefore(audited, values);
		}catch(Exception e) {
			log.error("Could not read the record before audited action {}: {}", audited.action(), e.getMessage());
		}

		Object result = joinPoint.proceed();

		try {
			values.put("result", result instanceof ResponseEntity ? ((ResponseEntity<?>) result).getBody() : result);
			Map<String, Object> details = details(values, audited.details());

			String entityId = text(resolve(values, audited.entityId()));
			// The changed record: the only one read, or the one whose id the action returned
			String changeId = null;
			if(recordsBefore != null && recordsBefore.size() == 1) {
				changeId = recordsBefore.keySet().iterator().next();
			}else if(recordsBefore != null && entityId != null && recordsBefore.containsKey(entityId)) {
				changeId = entityId;
			}
			Map<String, Object> before = changeId == null ? null : recordsBefore.get(changeId);
			if(entityId == null) {
				entityId = changeId;
			}

			String reference = text(resolve(values, audited.entityRef()));
			if(reference == null && before != null) {
				reference = reference(before);
			}
			if(reference == null) {
				Object resultValue = values.get("result");
				for(int i = 0; i < REFERENCE_COLUMNS.length && reference == null && resultValue != null; i++) {
					reference = text(property(resultValue, REFERENCE_COLUMNS[i]));
					if(reference != null && reference.isEmpty()) {
						reference = null;
					}
				}
			}
			// {ref} in a summary names the record; by its id when it has no readable reference
			if(reference != null) {
				values.put("ref", reference);
			}else if(entityId != null) {
				values.put("ref", "#" + entityId);
			}

			AuditLog auditLog = new AuditLog();
			auditLog.setCategory(audited.category());
			auditLog.setAction(audited.action());
			auditLog.setEntityType(audited.entityType().isEmpty() ? null : audited.entityType());
			auditLog.setEntityId(AuditRequests.truncate(entityId, 40));
			auditLog.setEntityRef(AuditRequests.truncate(reference, 100));
			auditLog.setSummary(AuditRequests.truncate(fill(values, audited.summary()), 255));

			if(before != null) {
				// The values after the action are read once it has committed, so they are the stored ones
				String recordId = changeId;
				auditLogService.getObject().recordAction(auditLog, entry -> completeChange(entry, audited, details, before, recordId));
			}else {
				auditLog.setDetails(details.isEmpty() ? null : json(details));
				auditLogService.getObject().recordAction(auditLog, null);
			}
		}catch(Exception e) {
			log.error("Could not record audit log entry {}: {}", audited.action(), e.getMessage());
		}
		return result;
	}

	// Reads the changed record (by changeId, or else the records matching changeQuery) before the action
	private Map<String, Map<String, Object>> readRecordsBefore(Audited audited, Map<String, Object> values) {
		Object id = resolve(values, audited.changeId());
		if(audited.changeOf() == void.class || (id == null && audited.changeQuery().isEmpty())) {
			return null;
		}
		return readOnly(() -> {
			List<?> ids = id != null ? List.of(id) : changeIds(audited, values);
			Map<String, Map<String, Object>> records = new LinkedHashMap<>();
			for(Object recordId : ids) {
				Map<String, Object> columns = columns(audited.changeOf(), recordId);
				if(columns != null) {
					records.put(text(recordId), columns);
				}
			}
			return records;
		});
	}

	private List<?> changeIds(Audited audited, Map<String, Object> values) {
		Query query = entityManager.createQuery(audited.changeQuery());
		for(int i = 0; i < audited.changeKeys().length; i++) {
			query.setParameter(i + 1, resolve(values, audited.changeKeys()[i]));
		}
		return query.setMaxResults(MAX_MATCHING_RECORDS).getResultList();
	}

	// Reads in a short read-only transaction of its own, so that nothing here can affect the action's transaction or the entry's
	private <T> T readOnly(Reading<T> reading) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager.getObject());
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.setReadOnly(true);
		return transactionTemplate.execute(status -> {
			try {
				return reading.read();
			}catch(Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		});
	}

	private interface Reading<T> {
		T read() throws Exception;
	}

	// Adds what the change did to the entry. Runs after the action has committed, inside the entry's own transaction
	private void completeChange(AuditLog auditLog, Audited audited, Map<String, Object> details, Map<String, Object> before, String id) {
		try {
			Map<String, Object> after = readOnly(() -> columns(audited.changeOf(), id));
			if(after == null) {
				// The record was deleted: keep everything it held
				details.put("before", new TreeMap<>(before));
			}else {
				// By column name, so entries always list them in the same order
				Map<String, Object> changedBefore = new TreeMap<>();
				Map<String, Object> changedAfter = new TreeMap<>();
				Set<String> columns = new LinkedHashSet<>(before.keySet());
				columns.addAll(after.keySet());
				for(String column : columns) {
					if(!Objects.equals(before.get(column), after.get(column))) {
						changedBefore.put(column, before.get(column));
						changedAfter.put(column, after.get(column));
					}
				}
				details.put("before", changedBefore);
				details.put("after", changedAfter);
				if(!audited.changedFieldPattern().isEmpty() && !audited.changedAction().isEmpty()
						&& changedAfter.keySet().stream().anyMatch(column -> column.matches(audited.changedFieldPattern()))) {
					auditLog.setAction(audited.changedAction());
				}
			}
		}catch(Exception e) {
			log.error("Could not read the record after audited action {}: {}", audited.action(), e.getMessage());
		}
		auditLog.setDetails(details.isEmpty() ? null : json(details));
	}

	// The record's own columns (and the ids of the records it points to), or null when there is no such record
	private Map<String, Object> columns(Class<?> entityClass, Object id) throws Exception {
		if(entityClass == void.class || id == null) {
			return null;
		}
		EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
		Object entity = entityManager.find(entityClass, idOfType(id, entityType.getIdType().getJavaType()));
		if(entity == null) {
			return null;
		}
		// A lazy reference already in the persistence context is replaced by the record itself
		entity = Hibernate.unproxy(entity);
		Map<String, Object> columns = new LinkedHashMap<>();
		for(Attribute<?, ?> attribute : entityType.getAttributes()) {
			if(!(attribute instanceof SingularAttribute) || ((SingularAttribute<?, ?>) attribute).isId()
					|| ((SingularAttribute<?, ?>) attribute).isVersion() || SENSITIVE.matcher(attribute.getName()).matches()) {
				continue;
			}
			Class<?> type = attribute.getJavaType();
			if(type == byte[].class || type == Byte[].class || Blob.class.isAssignableFrom(type) || Clob.class.isAssignableFrom(type)) {
				continue;
			}
			switch(attribute.getPersistentAttributeType()) {
				case BASIC:
					columns.put(attribute.getName(), simple(read(entity, attribute.getJavaMember())));
					break;
				case MANY_TO_ONE:
				case ONE_TO_ONE:
					Object related = read(entity, attribute.getJavaMember());
					columns.put(attribute.getName() + "Id", related == null ? null
							: simple(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(related)));
					break;
				default:
					break;
			}
		}
		return columns;
	}

	private Object read(Object entity, Member member) throws Exception {
		if(member instanceof Field) {
			Field field = (Field) member;
			field.setAccessible(true);
			return field.get(entity);
		}
		if(member instanceof Method) {
			Method method = (Method) member;
			method.setAccessible(true);
			return method.invoke(entity);
		}
		return null;
	}

	private Object idOfType(Object id, Class<?> idType) {
		if(idType == Long.class || idType == long.class) {
			return id instanceof Number ? ((Number) id).longValue() : Long.valueOf(id.toString());
		}
		if(idType == Integer.class || idType == int.class) {
			return id instanceof Number ? ((Number) id).intValue() : Integer.valueOf(id.toString());
		}
		return id;
	}

	private String reference(Map<String, Object> columns) {
		for(String column : REFERENCE_COLUMNS) {
			Object value = columns.get(column);
			if(value != null && !value.toString().isEmpty()) {
				return value.toString();
			}
		}
		return null;
	}

	// Replaces each {expression} in the summary with its value
	private String fill(Map<String, Object> values, String template) {
		Matcher matcher = PLACEHOLDER.matcher(template);
		StringBuffer summary = new StringBuffer();
		while(matcher.find()) {
			String value = text(resolve(values, matcher.group(1)));
			matcher.appendReplacement(summary, Matcher.quoteReplacement(value == null ? "" : value));
		}
		matcher.appendTail(summary);
		return summary.toString();
	}

	private Map<String, Object> details(Map<String, Object> values, String[] expressions) {
		Map<String, Object> details = new LinkedHashMap<>();
		for(String expression : expressions) {
			String name = expression;
			String path = expression;
			int equals = expression.indexOf('=');
			if(equals > 0) {
				name = expression.substring(0, equals);
				path = expression.substring(equals + 1);
			}
			Object value;
			if(path.startsWith(PRESENT)) {
				String text = text(resolve(values, path.substring(PRESENT.length())));
				value = text != null && !text.isEmpty();
			}else {
				value = simple(resolve(values, path));
			}
			if(name.equals(path) && name.startsWith("result.")) {
				name = name.substring("result.".length());
			}
			details.put(name, value);
		}
		return details;
	}

	private String json(Map<String, Object> details) {
		try {
			return objectMapper.getObject().writeValueAsString(details);
		}catch(Exception e) {
			return details.toString();
		}
	}

	// Follows an expression such as "result.id", "discountRequest.comments" or "billReceivableRequests.no"
	private Object resolve(Map<String, Object> values, String expression) {
		if(expression == null || expression.isEmpty()) {
			return null;
		}
		String[] parts = expression.split("\\.");
		Object current = values.get(parts[0]);
		for(int i = 1; i < parts.length && current != null; i++) {
			current = property(current, parts[i]);
		}
		return current;
	}

	private Object property(Object target, String name) {
		try {
			if(target instanceof Collection) {
				Collection<?> collection = (Collection<?>) target;
				if(name.equals("size")) {
					return collection.size();
				}
				List<Object> list = new ArrayList<>();
				for(Object element : collection) {
					list.add(element == null ? null : property(element, name));
				}
				return list;
			}
			if(target.getClass().isArray()) {
				Object[] array = (Object[]) target;
				return name.equals("size") ? array.length : property(List.of(array), name);
			}
			BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
			return beanWrapper.isReadableProperty(name) ? beanWrapper.getPropertyValue(name) : null;
		}catch(Exception e) {
			return null;
		}
	}

	// Values kept in the details as they are (numbers, text, lists of these); anything else as text
	private Object simple(Object value) {
		if(value == null || value instanceof Number || value instanceof Boolean || value instanceof String) {
			return value;
		}
		if(value instanceof Collection) {
			List<Object> list = new ArrayList<>();
			for(Object element : (Collection<?>) value) {
				list.add(simple(element));
			}
			return list;
		}
		if(value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
			List<Object> list = new ArrayList<>();
			for(Object element : (Object[]) value) {
				list.add(element == null ? null : objectMapper.getObject().convertValue(element, Object.class));
			}
			return list;
		}
		return text(value);
	}

	private String text(Object value) {
		if(value == null) {
			return null;
		}
		if(value instanceof Double || value instanceof Float) {
			return BigDecimal.valueOf(((Number) value).doubleValue()).stripTrailingZeros().toPlainString();
		}
		if(value instanceof Enum || value instanceof Temporal) {
			return value.toString();
		}
		return String.valueOf(value);
	}
}
