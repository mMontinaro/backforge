package io.backforge.data.facade;

import java.util.Objects;
import java.util.UUID;

import io.backforge.data.mapping.ModelMapper;
import io.backforge.data.model.BaseModel;
import io.backforge.data.service.BaseService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Generic DTO-facing application boundary over a model-focused service. */
public class BaseFacade<D, M extends BaseModel> {

    private final BaseService<M> service;
    private final ModelMapper<D, M> mapper;

    public BaseFacade(BaseService<M> service, ModelMapper<D, M> mapper) {
        this.service = Objects.requireNonNull(service, "service must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    public D create(D dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        beforeCreate(dto);
        return mapper.toDto(service.create(mapper.toModel(dto)));
    }

    public D getById(UUID id) {
        return mapper.toDto(service.getById(id));
    }

    public Page<D> list(Pageable pageable) {
        return service.list(pageable).map(mapper::toDto);
    }

    public D update(UUID id, D dto) {
        Objects.requireNonNull(dto, "dto must not be null");
        M model = service.getById(id);
        mapper.updateModel(dto, model);
        beforeUpdate(dto, model);
        return mapper.toDto(service.update(model));
    }

    public D softDelete(UUID id) {
        beforeSoftDelete(id);
        M model = service.softDelete(id);
        return mapper.toDto(model);
    }

    protected void beforeCreate(D dto) {
    }

    protected void beforeUpdate(D dto, M model) {
    }

    protected void beforeSoftDelete(UUID id) {
    }
}
