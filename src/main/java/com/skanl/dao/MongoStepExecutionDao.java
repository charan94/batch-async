package com.skanl.dao;

import com.mongodb.client.MongoClient;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.StepExecutionDao;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class MongoStepExecutionDao implements StepExecutionDao {

    private MongoTemplate mongoTemplate;

    public MongoStepExecutionDao(MongoClient mongoClient, String databaseName) {
        this.mongoTemplate = new MongoTemplate(mongoClient, databaseName);
    }

    @Override
    public void saveStepExecution(StepExecution stepExecution) {
        mongoTemplate.save(stepExecution, "stepExecution");
    }

    @Override
    public void saveStepExecutions(Collection<StepExecution> stepExecutions) {
        stepExecutions.stream().forEach(step -> mongoTemplate.save(step, "stepExecution"));
    }

    @Override
    public void updateStepExecution(StepExecution stepExecution) {
        mongoTemplate.save(stepExecution, "stepExecution");
    }

    @Override
    public StepExecution getStepExecution(JobExecution jobExecution, Long stepExecutionId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("jobExecutionId").is(jobExecution.getId()).and("id").is(stepExecutionId)), StepExecution.class, "stepExecution");
    }

    @Override
    public void addStepExecutions(JobExecution jobExecution) {
        jobExecution.getStepExecutions().stream().forEach(step -> mongoTemplate.save(step, "stepExecution"));
    }
}
