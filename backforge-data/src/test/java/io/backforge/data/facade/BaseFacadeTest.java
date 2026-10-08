package io.backforge.data.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import io.backforge.data.mapping.ModelMapper;
import io.backforge.data.model.BaseModel;
import io.backforge.data.service.BaseService;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class BaseFacadeTest {

    private static final UUID MODEL_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldMapAndDelegateCreate() {
        TestDto dto = new TestDto("created");
        TestModel model = new TestModel();
        TestDto response = new TestDto("response");
        BaseService<TestModel> service = mock(BaseService.class);
        ModelMapper<TestDto, TestModel> mapper = mock(ModelMapper.class);
        BaseFacade<TestDto, TestModel> facade = new BaseFacade<>(service, mapper);
        when(mapper.toModel(dto)).thenReturn(model);
        when(service.create(model)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(response);

        assertThat(facade.create(dto)).isEqualTo(response);

        InOrder order = inOrder(mapper, service);
        order.verify(mapper).toModel(dto);
        order.verify(service).create(model);
        order.verify(mapper).toDto(model);
    }

    @Test
    void shouldMapGetAndListResults() {
        TestModel model = new TestModel();
        TestDto response = new TestDto("response");
        PageRequest pageable = PageRequest.of(0, 20);
        Page<TestModel> models = new PageImpl<>(List.of(model), pageable, 1);
        BaseService<TestModel> service = mock(BaseService.class);
        ModelMapper<TestDto, TestModel> mapper = mock(ModelMapper.class);
        BaseFacade<TestDto, TestModel> facade = new BaseFacade<>(service, mapper);
        when(service.getById(MODEL_ID)).thenReturn(model);
        when(service.list(pageable)).thenReturn(models);
        when(mapper.toDto(model)).thenReturn(response);

        assertThat(facade.getById(MODEL_ID)).isEqualTo(response);
        assertThat(facade.list(pageable).getContent()).containsExactly(response);

        verify(service).getById(MODEL_ID);
        verify(service).list(pageable);
    }

    @Test
    void shouldMapAndDelegateUpdate() {
        TestDto dto = new TestDto("updated");
        TestDto response = new TestDto("response");
        TestModel model = new TestModel();
        BaseService<TestModel> service = mock(BaseService.class);
        ModelMapper<TestDto, TestModel> mapper = mock(ModelMapper.class);
        BaseFacade<TestDto, TestModel> facade = new BaseFacade<>(service, mapper);
        when(service.getById(MODEL_ID)).thenReturn(model);
        when(service.update(model)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(response);

        assertThat(facade.update(MODEL_ID, dto)).isEqualTo(response);

        InOrder order = inOrder(service, mapper);
        order.verify(service).getById(MODEL_ID);
        order.verify(mapper).updateModel(dto, model);
        order.verify(service).update(model);
        order.verify(mapper).toDto(model);
    }

    @Test
    void shouldDelegateSoftDeleteAndMapResult() {
        TestModel model = new TestModel();
        TestDto response = new TestDto("deleted");
        BaseService<TestModel> service = mock(BaseService.class);
        ModelMapper<TestDto, TestModel> mapper = mock(ModelMapper.class);
        BaseFacade<TestDto, TestModel> facade = new BaseFacade<>(service, mapper);
        when(service.softDelete(MODEL_ID)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(response);

        assertThat(facade.softDelete(MODEL_ID)).isEqualTo(response);

        verify(service).softDelete(MODEL_ID);
        verify(mapper).toDto(model);
    }

    @Test
    void shouldRejectNullCreateDto() {
        BaseFacade<TestDto, TestModel> facade = new BaseFacade<>(mock(BaseService.class),
                mock(ModelMapper.class));

        assertThatThrownBy(() -> facade.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldInvokeEntitySpecificHooks() {
        HookedFacade facade = new HookedFacade(mock(BaseService.class), mock(ModelMapper.class));
        TestDto dto = new TestDto("value");
        TestModel model = new TestModel();
        BaseService<TestModel> service = facade.service;
        ModelMapper<TestDto, TestModel> mapper = facade.mapper;
        when(mapper.toModel(dto)).thenReturn(model);
        when(service.create(model)).thenReturn(model);
        when(service.getById(MODEL_ID)).thenReturn(model);
        when(service.update(model)).thenReturn(model);
        when(service.softDelete(MODEL_ID)).thenReturn(model);
        when(mapper.toDto(model)).thenReturn(dto);

        facade.create(dto);
        facade.update(MODEL_ID, dto);
        facade.softDelete(MODEL_ID);

        assertThat(facade.hooks).containsExactly("create", "update", "delete");
    }

    private record TestDto(String name) {
    }

    private static final class TestModel extends BaseModel {
    }

    private static final class HookedFacade extends BaseFacade<TestDto, TestModel> {

        private final BaseService<TestModel> service;
        private final ModelMapper<TestDto, TestModel> mapper;
        private final java.util.List<String> hooks = new java.util.ArrayList<>();

        private HookedFacade(BaseService<TestModel> service, ModelMapper<TestDto, TestModel> mapper) {
            super(service, mapper);
            this.service = service;
            this.mapper = mapper;
        }

        @Override
        protected void beforeCreate(TestDto dto) {
            hooks.add("create");
        }

        @Override
        protected void beforeUpdate(TestDto dto, TestModel model) {
            hooks.add("update");
        }

        @Override
        protected void beforeSoftDelete(UUID id) {
            hooks.add("delete");
        }
    }
}
