/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping.model;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.SampleMappingContext;
import org.springframework.data.mapping.context.SamplePersistentProperty;

/**
 * Unit tests for {@link PersistentPropertyAccessor} through {@link BeanWrapper} and
 * {@link ClassGeneratingPropertyAccessorFactory}.
 *
 * @author Mark Paluch
 */
@RunWith(Parameterized.class)
public class PersistentPropertyAccessorTests {

	private final static SampleMappingContext MAPPING_CONTEXT = new SampleMappingContext();

	private final Function<Object, PersistentPropertyAccessor<?>> propertyAccessorFunction;

	public PersistentPropertyAccessorTests(Function<Object, PersistentPropertyAccessor<?>> propertyAccessor,
			String displayName) {

		this.propertyAccessorFunction = propertyAccessor;
	}

	@Parameters(name = "{1}")
	@SuppressWarnings("unchecked")
	public static List<Object[]> parameters() {

		List<Object[]> parameters = new ArrayList<>();

		ClassGeneratingPropertyAccessorFactory factory = new ClassGeneratingPropertyAccessorFactory();
		BeanWrapperPropertyAccessorFactory beanWrapperFactory = BeanWrapperPropertyAccessorFactory.INSTANCE;

		Function<Class<?>, PersistentEntity<Object, SamplePersistentProperty>> entity = it -> MAPPING_CONTEXT
				.getRequiredPersistentEntity(it);

		Function<Object, PersistentPropertyAccessor<?>> beanWrapper = it -> beanWrapperFactory
				.getPropertyAccessor(entity.apply(it.getClass()), it);
		Function<Object, PersistentPropertyAccessor<?>> classGenerating = it -> factory
				.getPropertyAccessor(entity.apply(it.getClass()), it);

		parameters.add(new Object[] { beanWrapper, "BeanWrapper" });
		parameters.add(new Object[] { classGenerating, "ClassGeneratingPropertyAccessorFactory" });

		return parameters;
	}

	@Test // DATACMNS-1322
	public void shouldSetProperty() {

		DataClass bean = new DataClass();
		PersistentPropertyAccessor accessor = propertyAccessorFunction.apply(bean);
		SamplePersistentProperty property = getProperty(bean, "id");

		accessor.setProperty(property, "value");

		assertThat(accessor.getBean()).hasFieldOrPropertyWithValue("id", "value");
		assertThat(accessor.getBean()).isSameAs(bean);
		assertThat(accessor.getProperty(property)).isEqualTo("value");
	}

	@Test // DATACMNS-1322
	public void shouldSetKotlinDataClassProperty() {

		DataClassKt bean = new DataClassKt("foo");
		PersistentPropertyAccessor accessor = propertyAccessorFunction.apply(bean);
		SamplePersistentProperty property = getProperty(bean, "id");

		accessor.setProperty(property, "value");

		assertThat(accessor.getBean()).hasFieldOrPropertyWithValue("id", "value");
		assertThat(accessor.getBean()).isNotSameAs(bean);
		assertThat(accessor.getProperty(property)).isEqualTo("value");
	}

	@Test // DATACMNS-1322
	public void shouldSetExtendedKotlinDataClassProperty() {

		ExtendedDataClassKt bean = new ExtendedDataClassKt("foo", "bar");
		PersistentPropertyAccessor accessor = propertyAccessorFunction.apply(bean);
		SamplePersistentProperty property = getProperty(bean, "id");

		accessor.setProperty(property, "value");

		assertThat(accessor.getBean()).hasFieldOrPropertyWithValue("id", "value");
		assertThat(accessor.getBean()).isNotSameAs(bean);
		assertThat(accessor.getProperty(property)).isEqualTo("value");
	}

	@Test // DATACMNS-1322
	public void shouldWitherProperty() {

		ValueClass bean = new ValueClass("foo", "bar");
		PersistentPropertyAccessor accessor = propertyAccessorFunction.apply(bean);
		SamplePersistentProperty property = getProperty(bean, "id");

		accessor.setProperty(property, "value");

		assertThat(accessor.getBean()).hasFieldOrPropertyWithValue("id", "value");
		assertThat(accessor.getBean()).isNotSameAs(bean);
		assertThat(accessor.getProperty(property)).isEqualTo("value");
	}

	@Test // DATACMNS-1322
	public void shouldRejectImmutablePropertyUpdate() {

		ValueClass bean = new ValueClass("foo", "bar");
		PersistentPropertyAccessor accessor = propertyAccessorFunction.apply(bean);
		SamplePersistentProperty property = getProperty(bean, "immutable");

		assertThatThrownBy(() -> accessor.setProperty(property, "value")).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test // DATACMNS-1322
	public void shouldRejectImmutableKotlinClassPropertyUpdate() {

		ValueClassKt bean = new ValueClassKt("foo");
		PersistentPropertyAccessor accessor = propertyAccessorFunction.apply(bean);
		SamplePersistentProperty property = getProperty(bean, "immutable");

		assertThatThrownBy(() -> accessor.setProperty(property, "value")).isInstanceOf(UnsupportedOperationException.class);
	}

	private static SamplePersistentProperty getProperty(Object bean, String propertyName) {
		return MAPPING_CONTEXT.getRequiredPersistentEntity(bean.getClass()).getRequiredPersistentProperty(propertyName);
	}

	@Data
	static class DataClass {
		String id;
	}

	@Value

	static class ValueClass {
		@Wither String id;
		String immutable;
	}

}
