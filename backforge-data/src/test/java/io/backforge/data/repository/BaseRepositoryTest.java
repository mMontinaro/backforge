package io.backforge.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import io.backforge.data.model.BaseModel;

import org.junit.jupiter.api.Test;
import org.springframework.data.repository.NoRepositoryBean;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

class BaseRepositoryTest {

    @Test
    void shouldBeAReusableJpaRepositoryContract() {
        assertThat(BaseRepository.class.isAnnotationPresent(NoRepositoryBean.class)).isTrue();
        assertThat(Arrays.stream(BaseRepository.class.getGenericInterfaces())
                .map(Type::getTypeName)
                .anyMatch(type -> type.contains(JpaRepository.class.getName())))
                .isTrue();
        assertThat(Arrays.stream(BaseRepository.class.getGenericInterfaces())
                .map(Type::getTypeName)
                .anyMatch(type -> type.contains(JpaSpecificationExecutor.class.getName())))
                .isTrue();
        assertThat(BaseRepository.class.getTypeParameters()[0].getBounds()[0])
                .isEqualTo(BaseModel.class);
    }
}
