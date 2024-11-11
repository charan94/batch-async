package com.skanl.dao;

import com.mongodb.client.MongoClient;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.repository.dao.JobExecutionDao;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MongoJobExecutionDao implements JobExecutionDao {

    private MongoTemplate mongoTemplate;

    public MongoJobExecutionDao(MongoClient mongoClient, String databaseName) {
        this.mongoTemplate = new MongoTemplate(mongoClient, databaseName);
    }

    @Override
    public void saveJobExecution(JobExecution jobExecution) {
        mongoTemplate.save(jobExecution, "jobExecution");
    }

    @Override
    public void updateJobExecution(JobExecution jobExecution) {
        mongoTemplate.save(jobExecution, "jobExecution");
    }

    @Override
    public List<JobExecution> findJobExecutions(JobInstance jobInstance) {
        return mongoTemplate.find(Query.query(Criteria.where("jobInstanceId").is(jobInstance.getId())), JobExecution.class, "jobExecution");
    }

    @Override
    public JobExecution getLastJobExecution(JobInstance jobInstance) {
        return mongoTemplate.findOne(Query.query(Criteria.where("jobInstanceId").is(jobInstance.getId())).limit(1).with(Sort.by(Sort.Order.desc("createTime"))), JobExecution.class, "jobExecution");
    }

    @Override
    public Set<JobExecution> findRunningJobExecutions(String jobName) {
        return mongoTemplate.find(Query.query(Criteria.where("jobInstance.jobName").is(jobName)), JobExecution.class, "jobExecution").stream().map(r -> r).collect(Collectors.toSet());
    }

    @Override
    public JobExecution getJobExecution(Long executionId) {
        return mongoTemplate.findById(executionId, JobExecution.class, "jobExecution");
    }

    @Override
    public void synchronizeStatus(JobExecution jobExecution) {
        mongoTemplate.save(jobExecution, "jobExecution");
    }
}
