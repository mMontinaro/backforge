package io.backforge.data.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;
import org.junit.jupiter.api.Test;

class BaseModelTest {

    @Test
    void shouldExposeFrameworkManagedPersistenceFields() {
        BaseModel model = new BaseModel();
        Instant deletedAt = Instant.parse("2026-01-02T03:04:05Z");

        model.markDeleted(deletedAt);

        assertThat(model.getId()).isNull();
        assertThat(model.getCreatedAt()).isNull();
        assertThat(model.getUpdatedAt()).isNull();
        assertThat(model.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    void shouldExcludeSoftDeletedRowsAtTheEntityMapping() {
        SQLRestriction restriction = BaseModel.class.getAnnotation(SQLRestriction.class);

        assertThat(restriction).isNotNull();
        assertThat(restriction.value()).isEqualTo("deleted_at IS NULL");
    }
}
