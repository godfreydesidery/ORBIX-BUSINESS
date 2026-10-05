package com.orbix.api.modules.audit;

import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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
 * Building the entry never fails the action: a value that cannot be read is left empty.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)\\}");
	private static final String PRESENT = "present:";

	// Looked up when first needed, so creating this aspect does not create the services early
	private final ObjectProvider<AuditLogService> auditLogService;
	private final ObjectProvider<ObjectMapper> objectMapper;

	@AfterReturning(pointcut = "@annotation(audited)", returning = "result", argNames = "audited,result")
	public void recordAction(JoinPoint joinPoint, Audited audited, Object result) {
		try {
			Map<String, Object> values = new HashMap<>();
			String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
			Object[] args = joinPoint.getArgs();
			for(int i = 0; names != null && i < names.length && i < args.length; i++) {
				values.put(names[i], args[i]);
			}
			values.put("result", result instanceof ResponseEntity ? ((ResponseEntity<?>) result).getBody() : result);

			AuditLog auditLog = new AuditLog();
			auditLog.setCategory(audited.category());
			auditLog.setAction(audited.action());
			auditLog.setEntityType(audited.entityType().isEmpty() ? null : audited.entityType());
			auditLog.setEntityId(AuditRequests.truncate(text(resolve(values, audited.entityId())), 40));
			auditLog.setEntityRef(AuditRequests.truncate(text(resolve(values, audited.entityRef())), 100));
			auditLog.setSummary(AuditRequests.truncate(fill(values, audited.summary()), 255));
			auditLog.setDetails(details(values, audited.details()));
			auditLogService.getObject().recordAction(auditLog);
		}catch(Exception e) {
			log.error("Could not record audit log entry {}: {}", audited.action(), e.getMessage());
		}
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

	private String details(Map<String, Object> values, String[] expressions) {
		if(expressions.length == 0) {
			return null;
		}
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
