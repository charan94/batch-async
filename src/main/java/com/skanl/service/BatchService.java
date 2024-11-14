package com.skanl.service;

import org.springframework.batch.core.JobInstance;

public interface BatchService {

    JobInstance runBatch();
}
