package com.skanl.configuration;

import com.mongodb.client.MongoClient;
import com.skanl.dao.MongoExecutionContextDao;
import com.skanl.dao.MongoJobExecutionDao;
import com.skanl.dao.MongoJobInstanceDao;
import com.skanl.dao.MongoStepExecutionDao;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.AbstractJobRepositoryFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

@AllArgsConstructor
public class MongoJobRepository extends AbstractJobRepositoryFactoryBean implements InitializingBean {

    private MongoClient mongoClient;

    private String databaseName;

    public MongoJobRepository() {
        super();
    }

    @Override
    protected JobInstanceDao createJobInstanceDao() throws Exception {
        return new MongoJobInstanceDao(this.mongoClient, this.databaseName);
    }

    @Override
    protected JobExecutionDao createJobExecutionDao() throws Exception {
        return new MongoJobExecutionDao(this.mongoClient, this.databaseName);
    }

    @Override
    protected StepExecutionDao createStepExecutionDao() throws Exception {
        return new MongoStepExecutionDao(this.mongoClient, this.databaseName);
    }

    @Override
    protected ExecutionContextDao createExecutionContextDao() throws Exception {
        return new MongoExecutionContextDao(this.mongoClient, this.databaseName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.mongoClient, "MongoClient must not be null.");
        Assert.notNull(this.databaseName, "Database name must not be null.");
        super.afterPropertiesSet();
    }

}
