package com.skanl.dao;

import com.skanl.model.Visitors;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorsRepo extends MongoRepository<Visitors, String> {
}
