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

import org.springframework.batch.core.JobExecution;
import com.skanl.batch.persistence.ExecutionContext;
import com.skanl.batch.persistence.ExitStatus;
import com.skanl.batch.persistence.StepExecution;

import java.util.Map;

/**
 * @author Mahmoud Ben Hassine
 */
public class StepExecutionConverter {

	public org.springframework.batch.core.StepExecution toStepExecution(StepExecution source,
			JobExecution jobExecution) {
		org.springframework.batch.core.StepExecution stepExecution = new org.springframework.batch.core.StepExecution(
				source.getName(), jobExecution, source.getStepExecutionId());
		stepExecution.setStatus(source.getStatus());
		stepExecution.setReadCount(source.getReadCount());
		stepExecution.setWriteCount(source.getWriteCount());
		stepExecution.setCommitCount(source.getCommitCount());
		stepExecution.setRollbackCount(source.getRollbackCount());
		stepExecution.setReadSkipCount(source.getReadSkipCount());
		stepExecution.setProcessSkipCount(source.getProcessSkipCount());
		stepExecution.setWriteSkipCount(source.getWriteSkipCount());
		stepExecution.setFilterCount(source.getFilterCount());
		stepExecution.setStartTime(source.getStartTime());
		stepExecution.setCreateTime(source.getCreateTime());
		stepExecution.setEndTime(source.getEndTime());
		stepExecution.setLastUpdated(source.getLastUpdated());
		stepExecution.setExitStatus(new org.springframework.batch.core.ExitStatus(source.getExitStatus().exitCode(),
				source.getExitStatus().exitDescription()));
		stepExecution.setExecutionContext(
				new org.springframework.batch.item.ExecutionContext(source.getExecutionContext().map()));
		if (source.isTerminateOnly()) {
			stepExecution.setTerminateOnly();
		}
		return stepExecution;
	}

	public StepExecution fromStepExecution(org.springframework.batch.core.StepExecution source) {
		StepExecution stepExecution = new StepExecution();
		stepExecution.setStepExecutionId(source.getId());
		stepExecution.setJobExecutionId(source.getJobExecutionId());
		stepExecution.setName(source.getStepName());
		stepExecution.setJobExecutionId(source.getJobExecutionId());
		stepExecution.setStatus(source.getStatus());
		stepExecution.setReadCount(source.getReadCount());
		stepExecution.setWriteCount(source.getWriteCount());
		stepExecution.setCommitCount(source.getCommitCount());
		stepExecution.setRollbackCount(source.getRollbackCount());
		stepExecution.setReadSkipCount(source.getReadSkipCount());
		stepExecution.setProcessSkipCount(source.getProcessSkipCount());
		stepExecution.setWriteSkipCount(source.getWriteSkipCount());
		stepExecution.setFilterCount(source.getFilterCount());
		stepExecution.setStartTime(source.getStartTime());
		stepExecution.setCreateTime(source.getCreateTime());
		stepExecution.setEndTime(source.getEndTime());
		stepExecution.setLastUpdated(source.getLastUpdated());
		stepExecution.setExitStatus(
				new ExitStatus(source.getExitStatus().getExitCode(), source.getExitStatus().getExitDescription()));
		// TODO use ExecutionContext#toMap introduced in v5.1
		Map<String, Object> map = Map.ofEntries(source.getExecutionContext().entrySet().toArray(new Map.Entry[0]));
		stepExecution.setExecutionContext(new ExecutionContext(map, source.getExecutionContext().isDirty()));
		stepExecution.setTerminateOnly(source.isTerminateOnly());
		return stepExecution;
	}

}
