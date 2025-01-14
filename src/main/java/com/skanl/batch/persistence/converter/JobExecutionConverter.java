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

import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import com.skanl.batch.persistence.ExecutionContext;
import com.skanl.batch.persistence.ExitStatus;
import com.skanl.batch.persistence.JobExecution;
import com.skanl.batch.persistence.JobParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mahmoud Ben Hassine
 */
public class JobExecutionConverter {

	private final JobParameterConverter jobParameterConverter = new JobParameterConverter();

	private final StepExecutionConverter stepExecutionConverter = new StepExecutionConverter();

	public org.springframework.batch.core.JobExecution toJobExecution(JobExecution source, JobInstance jobInstance) {
		Map<String, org.springframework.batch.core.JobParameter<?>> parameterMap = new HashMap<>();
		source.getJobParameters()
			.forEach((key, value) -> parameterMap.put(key, this.jobParameterConverter.toJobParameter(value)));
		org.springframework.batch.core.JobExecution jobExecution = new org.springframework.batch.core.JobExecution(
				jobInstance, source.getJobExecutionId(), new JobParameters(parameterMap));
		jobExecution.addStepExecutions(source.getStepExecutions()
			.stream()
			.map(stepExecution -> this.stepExecutionConverter.toStepExecution(stepExecution, jobExecution))
			.toList());
		jobExecution.setStatus(source.getStatus());
		jobExecution.setStartTime(source.getStartTime());
		jobExecution.setCreateTime(source.getCreateTime());
		jobExecution.setEndTime(source.getEndTime());
		jobExecution.setLastUpdated(source.getLastUpdated());
		jobExecution.setExitStatus(new org.springframework.batch.core.ExitStatus(source.getExitStatus().exitCode(),
				source.getExitStatus().exitDescription()));
		jobExecution.setExecutionContext(
				new org.springframework.batch.item.ExecutionContext(source.getExecutionContext().map()));
		return jobExecution;
	}

	public JobExecution fromJobExecution(org.springframework.batch.core.JobExecution source) {
		JobExecution jobExecution = new JobExecution();
		jobExecution.setJobExecutionId(source.getId());
		jobExecution.setJobInstanceId(source.getJobInstance().getInstanceId());
		Map<String, JobParameter<?>> parameterMap = new HashMap<>();
		source.getJobParameters()
			.getParameters()
			.forEach((key, value) -> parameterMap.put(key, this.jobParameterConverter.fromJobParameter(value)));
		jobExecution.setJobParameters(parameterMap);
		jobExecution.setStepExecutions(
				source.getStepExecutions().stream().map(this.stepExecutionConverter::fromStepExecution).toList());
		jobExecution.setStatus(source.getStatus());
		jobExecution.setStartTime(source.getStartTime());
		jobExecution.setCreateTime(source.getCreateTime());
		jobExecution.setEndTime(source.getEndTime());
		jobExecution.setLastUpdated(source.getLastUpdated());
		jobExecution.setExitStatus(
				new ExitStatus(source.getExitStatus().getExitCode(), source.getExitStatus().getExitDescription()));
		// TODO use ExecutionContext#toMap introduced in v5.1
		Map<String, Object> map = Map.ofEntries(source.getExecutionContext().entrySet().toArray(new Map.Entry[0]));
		jobExecution.setExecutionContext(new ExecutionContext(map, source.getExecutionContext().isDirty()));
		return jobExecution;
	}

}
