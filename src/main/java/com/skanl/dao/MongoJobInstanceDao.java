package com.skanl.dao;

import com.mongodb.client.MongoClient;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MongoJobInstanceDao implements JobInstanceDao {

    private MongoTemplate mongoTemplate;

    public MongoJobInstanceDao(MongoClient mongoClient, String databaseName) {
        this.mongoTemplate = new MongoTemplate(mongoClient, databaseName);
    }


    @Override
    public JobInstance createJobInstance(String jobName, JobParameters jobParameters) {
        JobInstance jobInstance = new JobInstance(System.currentTimeMillis(), jobName);
        mongoTemplate.save(jobInstance, "jobInstance");
        return jobInstance;

    }

    @Override
    public JobInstance getJobInstance(String jobName, JobParameters jobParameters) {
        return mongoTemplate.findOne(query(where("jobName").is(jobName)), JobInstance.class, "jobInstance");
    }

    @Override
    public JobInstance getJobInstance(Long instanceId) {
        return mongoTemplate.findOne(query(where("instanceId").is(instanceId)), JobInstance.class, "jobInstance");
    }

    @Override
    public JobInstance getJobInstance(JobExecution jobExecution) {
        return mongoTemplate.findOne(query(where("jobName").is(jobExecution.getJobInstance().getJobName())), JobInstance.class, "jobInstance");
    }

    @Override
    public List<JobInstance> getJobInstances(String jobName, int start, int count) {
        return mongoTemplate.find(query(where("jobName").is(jobName)).skip(start).limit(count), JobInstance.class, "jobInstance");
    }

    @Override
    public List<String> getJobNames() {
        return mongoTemplate.find(query(where("id").not().isNull()).with(Sort.by(Sort.Direction.DESC, "id")), JobInstance.class, "jobInstance").stream().map(JobInstance::getJobName).toList();
    }

    @Override
    public List<JobInstance> findJobInstancesByName(String jobName, int start, int count) {
        return mongoTemplate.find(query(where("jobName").is(jobName)).skip(start).limit(count), JobInstance.class, "jobInstance");
    }

    @Override
    public long getJobInstanceCount(String jobName) throws NoSuchJobException {
        return (int) mongoTemplate.count(query(where("jobName").is(jobName)), "jobInstance");
    }
}
