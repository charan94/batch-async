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

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import com.skanl.batch.persistence.converter.JobInstanceConverter;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Mahmoud Ben Hassine
 */
@Repository
public class MongoJobInstanceDao implements JobInstanceDao {

	private static final String COLLECTION_NAME = "BATCH_JOB_INSTANCE";
	private static final String SEQUENCE_NAME = "BATCH_JOB_INSTANCE_SEQ";

	private final MongoOperations mongoOperations;

	private JobKeyGenerator<JobParameters> jobKeyGenerator = new DefaultJobKeyGenerator();
	private DataFieldMaxValueIncrementer jobInstanceIncrementer;

	private final JobInstanceConverter jobInstanceConverter = new JobInstanceConverter();

	public MongoJobInstanceDao(MongoOperations mongoOperations) {
		Assert.notNull(mongoOperations, "mongoOperations must not be null.");
		this.mongoOperations = mongoOperations;
		this.jobInstanceIncrementer = new MongoSequenceIncrementer(mongoOperations, SEQUENCE_NAME);
	}

	public void setJobInstanceIncrementer(DataFieldMaxValueIncrementer jobInstanceIncrementer) {
		this.jobInstanceIncrementer = jobInstanceIncrementer;
	}

	@Override
	public JobInstance createJobInstance(String jobName, JobParameters jobParameters) {
		Assert.notNull(jobName, "Job name must not be null.");
		Assert.notNull(jobParameters, "JobParameters must not be null.");

		Assert.state(getJobInstance(jobName, jobParameters) == null, "JobInstance must not already exist");

		com.skanl.batch.persistence.JobInstance jobInstanceToSave = new com.skanl.batch.persistence.JobInstance();
		jobInstanceToSave.setJobName(jobName);
		String key = this.jobKeyGenerator.generateKey(jobParameters);
		jobInstanceToSave.setJobKey(key);
		long instanceId = jobInstanceIncrementer.nextLongValue();
		jobInstanceToSave.setJobInstanceId(instanceId);
		this.mongoOperations.insert(jobInstanceToSave, COLLECTION_NAME);

		JobInstance jobInstance = new JobInstance(instanceId, jobName);
		jobInstance.incrementVersion(); // TODO is this needed?
		return jobInstance;
	}

	@Override
	public JobInstance getJobInstance(String jobName, JobParameters jobParameters) {
		String key = this.jobKeyGenerator.generateKey(jobParameters);
		Query query = query(where("jobName").is(jobName).and("jobKey").is(key));
		com.skanl.batch.persistence.JobInstance jobInstance = this.mongoOperations
			.findOne(query, com.skanl.batch.persistence.JobInstance.class, COLLECTION_NAME);
		return jobInstance != null ? this.jobInstanceConverter.toJobInstance(jobInstance) : null;
	}

	@Override
	public JobInstance getJobInstance(Long instanceId) {
		Query query = query(where("jobInstanceId").is(instanceId));
		com.skanl.batch.persistence.JobInstance jobInstance = this.mongoOperations
				.findOne(query, com.skanl.batch.persistence.JobInstance.class, COLLECTION_NAME);
		return jobInstance != null ? this.jobInstanceConverter.toJobInstance(jobInstance) : null;
	}

	@Override
	public JobInstance getJobInstance(JobExecution jobExecution) {
		return getJobInstance(jobExecution.getJobId());
	}

	@Override
	public List<JobInstance> getJobInstances(String jobName, int start, int count) {
		Query query = query(where("jobName").is(jobName));
		Sort.Order sortOrder = Sort.Order.desc("jobInstanceId");
		List<com.skanl.batch.persistence.JobInstance> jobInstances = this.mongoOperations
			.find(query.with(Sort.by(sortOrder)),
					com.skanl.batch.persistence.JobInstance.class, COLLECTION_NAME)
			.stream()
			.toList();
		return jobInstances.subList(start, jobInstances.size())
			.stream()
			.map(this.jobInstanceConverter::toJobInstance)
			.limit(count)
			.toList();
	}

	@Override
	public JobInstance getLastJobInstance(String jobName) {
		Query query = query(where("jobName").is(jobName));
		Sort.Order sortOrder = Sort.Order.desc("jobInstanceId");
		com.skanl.batch.persistence.JobInstance jobInstance = this.mongoOperations.findOne(
				query.with(Sort.by(sortOrder)), com.skanl.batch.persistence.JobInstance.class,
				COLLECTION_NAME);
		return jobInstance != null ? this.jobInstanceConverter.toJobInstance(jobInstance) : null;
	}

	@Override
	public List<String> getJobNames() {
		return this.mongoOperations
			.findAll(com.skanl.batch.persistence.JobInstance.class, COLLECTION_NAME)
			.stream()
			.map(com.skanl.batch.persistence.JobInstance::getJobName)
			.toList();
	}

	@Override
	public List<JobInstance> findJobInstancesByName(String jobName, int start, int count) {
		Query query = query(where("jobName").alike(Example.of(jobName)));
		Sort.Order sortOrder = Sort.Order.desc("jobInstanceId");
		List<com.skanl.batch.persistence.JobInstance> jobInstances = this.mongoOperations
			.find(query.with(Sort.by(sortOrder)),
					com.skanl.batch.persistence.JobInstance.class, COLLECTION_NAME)
			.stream()
			.toList();
		return jobInstances.subList(start, jobInstances.size())
			.stream()
			.map(this.jobInstanceConverter::toJobInstance)
			.limit(count)
			.toList();
	}

	@Override
	public long getJobInstanceCount(String jobName) throws NoSuchJobException {
		if (!getJobNames().contains(jobName)) {
			throw new NoSuchJobException("Job not found " + jobName);
		}
		Query query = query(where("jobName").is(jobName));
		return this.mongoOperations.count(query, COLLECTION_NAME);
	}

	public void setJobKeyGenerator(JobKeyGenerator<JobParameters> jobKeyGenerator) {
		this.jobKeyGenerator = jobKeyGenerator;
	}

}
