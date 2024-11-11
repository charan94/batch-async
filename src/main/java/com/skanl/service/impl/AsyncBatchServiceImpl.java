package com.skanl.service.impl;

import com.skanl.service.AsyncBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Calendar;

@Service
@Slf4j
public class AsyncBatchServiceImpl implements AsyncBatchService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Override
    public void runBatchJob() {

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("timestamp", Calendar.getInstance().getTime())
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            log.info("STATUS :: "+jobExecution.getStatus());
        } catch(JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException ex) {
            log.error("Error while running batch job", ex);
        }
    }


}
