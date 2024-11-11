package com.skanl.dao;

import com.skanl.model.BatchInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchInfoRepository extends MongoRepository<BatchInfo, String> {
}
