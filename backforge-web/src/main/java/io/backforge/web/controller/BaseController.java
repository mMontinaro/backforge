package io.backforge.web.controller;

import java.util.UUID;

import io.backforge.data.facade.BaseFacade;
import io.backforge.data.model.BaseModel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Reusable HTTP CRUD boundary for generated resource controllers. */
public abstract class BaseController<D, M extends BaseModel> {

    private final BaseFacade<D, M> facade;

    protected BaseController(BaseFacade<D, M> facade) {
        this.facade = facade;
    }

    @PostMapping
    public ResponseEntity<D> create(@RequestBody D dto) {
        D created = facade.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public Page<D> list(@PageableDefault Pageable pageable) {
        return facade.list(pageable);
    }

    @GetMapping("/{id}")
    public D getById(@PathVariable UUID id) {
        return facade.getById(id);
    }

    @PutMapping("/{id}")
    public D update(@PathVariable UUID id, @RequestBody D dto) {
        return facade.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        facade.softDelete(id);
        return ResponseEntity.noContent().build();
    }

}
