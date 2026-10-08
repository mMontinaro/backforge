package io.backforge.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables framework-managed creation and update timestamps. */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class BackforgeJpaConfiguration {
}
