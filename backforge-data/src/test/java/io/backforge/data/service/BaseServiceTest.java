package io.backforge.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import io.backforge.data.model.BaseModel;
import io.backforge.data.repository.BaseRepository;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class BaseServiceTest {

    private static final UUID MODEL_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");

    @Test
    void shouldCreateModel() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        TestModel model = org.mockito.Mockito.mock(TestModel.class);
        BaseService<TestModel> service = new BaseService<>(repository);
        when(repository.save(model)).thenReturn(model);

        assertThat(service.create(model)).isSameAs(model);

        verify(repository).save(model);
    }

    @Test
    void shouldRejectCreateWithExistingId() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        TestModel model = org.mockito.Mockito.mock(TestModel.class);
        when(model.getId()).thenReturn(MODEL_ID);
        BaseService<TestModel> service = new BaseService<>(repository);

        assertThatThrownBy(() -> service.create(model))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetModelByIdAndFailWhenMissing() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        TestModel model = org.mockito.Mockito.mock(TestModel.class);
        BaseService<TestModel> service = new BaseService<>(repository);
        when(repository.findById(MODEL_ID)).thenReturn(Optional.of(model));

        assertThat(service.getById(MODEL_ID)).isSameAs(model);

        when(repository.findById(MODEL_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(MODEL_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldListModelsWithPageable() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        BaseService<TestModel> service = new BaseService<>(repository);
        PageRequest pageable = PageRequest.of(1, 10);
        Page<TestModel> page = new PageImpl<>(java.util.List.of());
        when(repository.findAll(pageable)).thenReturn(page);

        assertThat(service.list(pageable)).isSameAs(page);
        verify(repository).findAll(pageable);
    }

    @Test
    void shouldUpdateExistingActiveModel() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        TestModel model = org.mockito.Mockito.mock(TestModel.class);
        BaseService<TestModel> service = new BaseService<>(repository);
        when(model.getId()).thenReturn(MODEL_ID);
        when(repository.findById(MODEL_ID)).thenReturn(Optional.of(model));
        when(repository.save(model)).thenReturn(model);

        assertThat(service.update(model)).isSameAs(model);

        verify(repository).save(model);
    }

    @Test
    void shouldSoftDeleteExistingModel() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        TestModel model = org.mockito.Mockito.mock(TestModel.class);
        BaseService<TestModel> service = new BaseService<>(repository,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(repository.findById(MODEL_ID)).thenReturn(Optional.of(model));
        when(repository.save(model)).thenReturn(model);

        assertThat(service.softDelete(MODEL_ID)).isSameAs(model);

        verify(model).markDeleted(NOW);
        verify(repository).save(model);
    }

    @Test
    void shouldRejectUpdateOfSoftDeletedModel() {
        BaseRepository<TestModel> repository = org.mockito.Mockito.mock(BaseRepository.class);
        TestModel model = org.mockito.Mockito.mock(TestModel.class);
        BaseService<TestModel> service = new BaseService<>(repository);
        when(model.getId()).thenReturn(MODEL_ID);
        when(model.getDeletedAt()).thenReturn(NOW);

        assertThatThrownBy(() -> service.update(model))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any());
    }

    private static final class TestModel extends BaseModel {
    }
}
