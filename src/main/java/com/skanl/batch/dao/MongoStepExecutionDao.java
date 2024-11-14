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
package com.skanl.batch.dao;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import com.skanl.batch.persistence.converter.JobExecutionConverter;
import com.skanl.batch.persistence.converter.StepExecutionConverter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.stereotype.Repository;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Mahmoud Ben Hassine
 */
@Repository
public class MongoStepExecutionDao implements StepExecutionDao {

	private static final String STEP_EXECUTIONS_COLLECTION_NAME = "BATCH_STEP_EXECUTION";
	private static final String STEP_EXECUTIONS_SEQUENCE_NAME = "BATCH_STEP_EXECUTION_SEQ";

	private static final String JOB_EXECUTIONS_COLLECTION_NAME = "BATCH_JOB_EXECUTION";

	private final StepExecutionConverter stepExecutionConverter = new StepExecutionConverter();

	private final JobExecutionConverter jobExecutionConverter = new JobExecutionConverter();

	private final MongoOperations mongoOperations;

	private DataFieldMaxValueIncrementer stepExecutionIncrementer;

	public MongoStepExecutionDao(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
		this.stepExecutionIncrementer = new MongoSequenceIncrementer(mongoOperations, STEP_EXECUTIONS_SEQUENCE_NAME);
	}

	public void setStepExecutionIncrementer(DataFieldMaxValueIncrementer stepExecutionIncrementer) {
		this.stepExecutionIncrementer = stepExecutionIncrementer;
	}

	@Override
	public void saveStepExecution(StepExecution stepExecution) {
		com.skanl.batch.persistence.StepExecution stepExecutionToSave = this.stepExecutionConverter
			.fromStepExecution(stepExecution);
		long stepExecutionId = this.stepExecutionIncrementer.nextLongValue();
		stepExecutionToSave.setStepExecutionId(stepExecutionId);
		this.mongoOperations.insert(stepExecutionToSave, STEP_EXECUTIONS_COLLECTION_NAME);
		stepExecution.setId(stepExecutionId);
	}

	@Override
	public void saveStepExecutions(Collection<StepExecution> stepExecutions) {
		for (StepExecution stepExecution : stepExecutions) {
			saveStepExecution(stepExecution);
		}
	}

	@Override
	public void updateStepExecution(StepExecution stepExecution) {
		Query query = query(where("stepExecutionId").is(stepExecution.getId()));
		com.skanl.batch.persistence.StepExecution stepExecutionToUpdate = this.stepExecutionConverter
				.fromStepExecution(stepExecution);
		this.mongoOperations.findAndReplace(query, stepExecutionToUpdate, STEP_EXECUTIONS_COLLECTION_NAME);
	}

	@Override
	public StepExecution getStepExecution(JobExecution jobExecution, Long stepExecutionId) {
		com.skanl.batch.persistence.StepExecution stepExecution = this.mongoOperations
			.findById(stepExecutionId, com.skanl.batch.persistence.StepExecution.class,
					STEP_EXECUTIONS_COLLECTION_NAME);
		return stepExecution != null ? this.stepExecutionConverter.toStepExecution(stepExecution, jobExecution) : null;
	}

	@Override
	public StepExecution getLastStepExecution(JobInstance jobInstance, String stepName) {
		// TODO optimize the query
		// get all step executions
		List<com.skanl.batch.persistence.StepExecution> stepExecutions = new ArrayList<>();
		Query query = query(where("jobInstanceId").is(jobInstance.getId()));
		List<com.skanl.batch.persistence.JobExecution> jobExecutions = this.mongoOperations
			.find(query, com.skanl.batch.persistence.JobExecution.class,
					JOB_EXECUTIONS_COLLECTION_NAME);
		for (com.skanl.batch.persistence.JobExecution jobExecution : jobExecutions) {
			stepExecutions.addAll(jobExecution.getStepExecutions());
		}
		// sort step executions by creation date then id (see contract) and return the
		// first one
		Optional<com.skanl.batch.persistence.StepExecution> lastStepExecution = stepExecutions
			.stream()
			.min(Comparator
				.comparing(com.skanl.batch.persistence.StepExecution::getCreateTime)
				.thenComparing(com.skanl.batch.persistence.StepExecution::getId));
		if (lastStepExecution.isPresent()) {
			com.skanl.batch.persistence.StepExecution stepExecution = lastStepExecution.get();
			JobExecution jobExecution = this.jobExecutionConverter.toJobExecution(jobExecutions.stream()
				.filter(execution -> execution.getJobExecutionId().equals(stepExecution.getJobExecutionId()))
				.findFirst()
				.get(), jobInstance);
			return this.stepExecutionConverter.toStepExecution(stepExecution, jobExecution);
		}
		else {
			return null;
		}
	}

	@Override
	public void addStepExecutions(JobExecution jobExecution) {
		Query query = query(where("jobExecutionId").is(jobExecution.getId()));
		List<StepExecution> stepExecutions = this.mongoOperations
			.find(query, com.skanl.batch.persistence.StepExecution.class,
					STEP_EXECUTIONS_COLLECTION_NAME)
			.stream()
			.map(stepExecution -> this.stepExecutionConverter.toStepExecution(stepExecution, jobExecution))
			.toList();
		jobExecution.addStepExecutions(stepExecutions);
	}

	@Override
	public long countStepExecutions(JobInstance jobInstance, String stepName) {
		long count = 0;
		// TODO optimize the count query
		Query query = query(where("jobInstanceId").is(jobInstance.getId()));
		List<com.skanl.batch.persistence.JobExecution> jobExecutions = this.mongoOperations
			.find(query, com.skanl.batch.persistence.JobExecution.class,
					JOB_EXECUTIONS_COLLECTION_NAME);
		for (com.skanl.batch.persistence.JobExecution jobExecution : jobExecutions) {
			List<com.skanl.batch.persistence.StepExecution> stepExecutions = jobExecution
				.getStepExecutions();
			for (com.skanl.batch.persistence.StepExecution stepExecution : stepExecutions) {
				if (stepExecution.getName().equals(stepName)) {
					count++;
				}
			}
		}
		return count;
	}

}
