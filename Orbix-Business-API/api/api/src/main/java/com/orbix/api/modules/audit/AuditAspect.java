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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
	private static final String[] REFERENCE_COLUMNS = {"no", "code", "username", "name", "chasisNo"};

	// Looked up when first needed, so creating this aspect does not create the services early
	private final ObjectProvider<AuditLogService> auditLogService;
	private final ObjectProvider<ObjectMapper> objectMapper;

	@PersistenceContext
	private EntityManager entityManager;

	@Around(value = "@annotation(audited)", argNames = "audited")
	public Object recordAction(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
		Map<String, Object> values = new HashMap<>();
		Map<String, Object> before = null;
		try {
			String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
			Object[] args = joinPoint.getArgs();
			for(int i = 0; names != null && i < names.length && i < args.length; i++) {
				values.put(names[i], args[i]);
			}
			before = columns(audited.changeOf(), resolve(values, audited.changeId()));
		}catch(Exception e) {
			log.error("Could not read the record before audited action {}: {}", audited.action(), e.getMessage());
		}

		Object result = joinPoint.proceed();

		try {
			values.put("result", result instanceof ResponseEntity ? ((ResponseEntity<?>) result).getBody() : result);
			Map<String, Object> details = details(values, audited.details());
			String action = audited.action();
			String reference = text(resolve(values, audited.entityRef()));

			if(audited.changeOf() != void.class && before != null) {
				Map<String, Object> after = columns(audited.changeOf(), resolve(values, audited.changeId()));
				if(after == null) {
					// The record was deleted: keep everything it held
					details.put("before", before);
				}else {
					Map<String, Object> changedBefore = new LinkedHashMap<>();
					Map<String, Object> changedAfter = new LinkedHashMap<>();
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
						action = audited.changedAction();
					}
				}
				if(reference == null) {
					reference = reference(before);
				}
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
			// {ref} in a summary names the record
			values.put("ref", reference);

			AuditLog auditLog = new AuditLog();
			auditLog.setCategory(audited.category());
			auditLog.setAction(action);
			auditLog.setEntityType(audited.entityType().isEmpty() ? null : audited.entityType());
			String entityId = text(resolve(values, audited.entityId()));
			if(entityId == null && audited.changeOf() != void.class) {
				entityId = text(resolve(values, audited.changeId()));
			}
			auditLog.setEntityId(AuditRequests.truncate(entityId, 40));
			auditLog.setEntityRef(AuditRequests.truncate(reference, 100));
			auditLog.setSummary(AuditRequests.truncate(fill(values, audited.summary()), 255));
			auditLog.setDetails(details.isEmpty() ? null : json(details));
			auditLogService.getObject().recordAction(auditLog);
		}catch(Exception e) {
			log.error("Could not record audit log entry {}: {}", audited.action(), e.getMessage());
		}
		return result;
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
