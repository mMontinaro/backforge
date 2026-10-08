package io.backforge.data.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

class BackforgeJpaConfigurationTest {

    @Test
    void shouldEnableJpaAuditing() {
        assertThat(BackforgeJpaConfiguration.class.isAnnotationPresent(EnableJpaAuditing.class))
                .isTrue();
    }
}
