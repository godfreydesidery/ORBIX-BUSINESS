package com.orbix.api.modules.audit;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Background writing of sign-in and access-denied entries, so signing in never waits on the audit log
 */
@Configuration
@EnableAsync
public class AuditConfig {

	@Bean(name = "auditExecutor")
	public Executor auditExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("audit-");
		// If the queue is ever full, the entry is written on the calling thread instead of being dropped
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
