package com.skanl.controller;

import com.skanl.service.BatchService;
import org.springframework.batch.core.JobInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/batch")
@RestController
public class BatchController {

    @Autowired
    private BatchService batchService;

    @PostMapping(value = "/run", produces = MediaType.APPLICATION_JSON_VALUE)
    public JobInstance runBatch() {
        return batchService.runBatch();
    }
}
