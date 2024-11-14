package com.skanl.service.impl;

import com.skanl.service.BatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BatchServiceImpl implements BatchService {

    private ApplicationContext context;

    @Autowired
    public BatchServiceImpl(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public JobInstance runBatch() {
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job importVisitorsJob = context.getBean("importVisitorsJob", Job.class);
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(importVisitorsJob, jobParameters);
            log.info("Job instance {} ", jobExecution.getJobInstance());
            return jobExecution.getJobInstance();
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new RuntimeException(e);
        } catch (JobExecutionAlreadyRunningException e) {
            throw new RuntimeException(e);
        } catch (JobParametersInvalidException e) {
            throw new RuntimeException(e);
        } catch (JobRestartException e) {
            throw new RuntimeException(e);
        }
    }
}
