package com.skanl.dao;

import com.mongodb.client.MongoClient;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.item.ExecutionContext;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class MongoExecutionContextDao implements ExecutionContextDao {

    private MongoTemplate mongoTemplate;

    public MongoExecutionContextDao(MongoClient mongoClient, String databaseName) {
        this.mongoTemplate = new MongoTemplate(mongoClient, databaseName);
    }

    @Override
    public ExecutionContext getExecutionContext(JobExecution jobExecution) {
        return mongoTemplate.findById(jobExecution.getId(), ExecutionContext.class, "jobExecutionContext");
    }

    @Override
    public ExecutionContext getExecutionContext(StepExecution stepExecution) {
        return mongoTemplate.findById(stepExecution.getId(), ExecutionContext.class, "stepExecutionContext");
    }

    @Override
    public void saveExecutionContext(JobExecution jobExecution) {
        ExecutionContext context = jobExecution.getExecutionContext();
        mongoTemplate.save(context, "jobExecutionContext");
    }

    @Override
    public void saveExecutionContext(StepExecution stepExecution) {
        ExecutionContext context = stepExecution.getExecutionContext();
        mongoTemplate.save(context, "stepExecutionContext");
    }

    @Override
    public void saveExecutionContexts(Collection<StepExecution> stepExecutions) {
        stepExecutions.forEach(this::saveExecutionContext);
    }

    @Override
    public void updateExecutionContext(JobExecution jobExecution) {
        ExecutionContext context = jobExecution.getExecutionContext();
        mongoTemplate.save(context, "jobExecutionContext");
    }

    @Override
    public void updateExecutionContext(StepExecution stepExecution) {
        ExecutionContext context = stepExecution.getExecutionContext();
        mongoTemplate.save(context, "jobExecutionContext");
    }
}
