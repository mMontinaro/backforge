package io.backforge.data.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.backforge.data.model.BaseModel;
import io.backforge.data.repository.BaseRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Generic persistence-oriented service for generated entities. */
public class BaseService<T extends BaseModel> {

    private final BaseRepository<T> repository;
    private final Clock clock;

    public BaseService(BaseRepository<T> repository) {
        this(repository, Clock.systemUTC());
    }

    BaseService(BaseRepository<T> repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public T create(T model) {
        Objects.requireNonNull(model, "model must not be null");
        if (model.getId() != null) {
            throw new IllegalArgumentException("cannot create a model with an existing id");
        }

        beforeCreate(model);
        return repository.save(model);
    }

    @Transactional(readOnly = true)
    public T getById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("model not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<T> list(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        return repository.findAll(pageable);
    }

    @Transactional
    public T update(T model) {
        Objects.requireNonNull(model, "model must not be null");
        if (model.getId() == null) {
            throw new IllegalArgumentException("cannot update a model without an id");
        }
        if (model.getDeletedAt() != null) {
            throw new IllegalArgumentException("cannot update a soft-deleted model");
        }

        getById(model.getId());
        beforeUpdate(model);
        return repository.save(model);
    }

    @Transactional
    public T softDelete(UUID id) {
        T model = getById(id);
        beforeSoftDelete(model);
        model.markDeleted(Instant.now(clock));
        return repository.save(model);
    }

    protected void beforeCreate(T model) {
    }

    protected void beforeUpdate(T model) {
    }

    protected void beforeSoftDelete(T model) {
    }
}
