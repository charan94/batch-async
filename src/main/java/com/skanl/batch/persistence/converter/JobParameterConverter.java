/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skanl.batch.persistence.converter;

import com.skanl.batch.persistence.JobParameter;

/**
 * @author Mahmoud Ben Hassine
 */
public class JobParameterConverter {

	public <T> org.springframework.batch.core.JobParameter<T> toJobParameter(JobParameter<T> source) {
		try {
			return new org.springframework.batch.core.JobParameter<>(source.value(),
					(Class<T>) Class.forName(source.type()), source.identifying());
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> JobParameter<T> fromJobParameter(org.springframework.batch.core.JobParameter<T> source) {
		return new JobParameter<>(source.getValue(), source.getType().getName(), source.isIdentifying());
	}

}
