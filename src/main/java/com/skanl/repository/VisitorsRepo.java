package com.skanl.repository;

import com.skanl.model.Visitor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorsRepo extends MongoRepository<Visitor, String> {
}
