package com.skanl.controller;

import com.skanl.dto.BatchResponse;
import com.skanl.service.AsyncBatchService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;


@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    private AsyncBatchService asyncBatchService;

    @PostMapping("/run")
    public BatchResponse load() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        asyncBatchService.runBatchJob();
        return new BatchResponse("COMPLETED", "Batch job has been invoked successfully at " + Calendar.getInstance().getTime());
    }

}
