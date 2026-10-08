package io.backforge.data.mapping;

import io.backforge.data.model.BaseModel;

/** Explicit model/DTO mapping contract implemented by generated entity mappers. */
public interface ModelMapper<D, M extends BaseModel> {

    M toModel(D dto);

    void updateModel(D dto, M model);

    D toDto(M model);
}
