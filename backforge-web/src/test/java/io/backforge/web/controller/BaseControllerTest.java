package io.backforge.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import io.backforge.data.facade.BaseFacade;
import io.backforge.data.model.BaseModel;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

class BaseControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private BaseFacade<TestDto, TestModel> facade;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        facade = org.mockito.Mockito.mock(BaseFacade.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController(facade))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new io.backforge.web.error.BackforgeApiExceptionHandler())
                .build();
    }

    @Test
    void shouldDelegateCreateAndReturnCreatedResponse() throws Exception {
        TestDto response = new TestDto("created");
        when(facade.create(any(TestDto.class))).thenReturn(response);

        mockMvc.perform(post("/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"input\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("created"));

        verify(facade).create(new TestDto("input"));
    }

    @Test
    void shouldPassPaginationAndSortingToFacade() throws Exception {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(1, 5);
        when(facade.list(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new TestDto("one")), pageable, 6));

        mockMvc.perform(get("/resources")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("one"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(facade).list(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("name").isDescending()).isTrue();
    }

    @Test
    void shouldDelegateGetUpdateAndDelete() throws Exception {
        TestDto response = new TestDto("result");
        when(facade.getById(ID)).thenReturn(response);
        when(facade.update(ID, new TestDto("input"))).thenReturn(response);

        mockMvc.perform(get("/resources/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("result"));
        mockMvc.perform(put("/resources/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"input\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/resources/{id}", ID))
                .andExpect(status().isNoContent());

        verify(facade).getById(ID);
        verify(facade).update(ID, new TestDto("input"));
        verify(facade).softDelete(ID);
    }

    @Test
    void shouldTranslateNotFoundToProblemDetail() throws Exception {
        when(facade.getById(ID)).thenThrow(new EntityNotFoundException("resource not found"));

        mockMvc.perform(get("/resources/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("resource not found"));
    }

    @Test
    void shouldRejectMalformedIdentifier() throws Exception {
        mockMvc.perform(get("/resources/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    private record TestDto(String name) {
    }

    private static class TestModel extends BaseModel {
    }

    @RequestMapping("/resources")
    @RestController
    private static class TestController extends BaseController<TestDto, TestModel> {

        private TestController(BaseFacade<TestDto, TestModel> facade) {
            super(facade);
        }
    }
}
