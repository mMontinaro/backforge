package io.backforge.data.repository;

import java.util.UUID;

import io.backforge.data.model.BaseModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/** Shared repository contract for generated entities. */
@NoRepositoryBean
public interface BaseRepository<T extends BaseModel>
        extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
}
