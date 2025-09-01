package com.prs.dao;

import com.prs.documents.TransactionRequests;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRequestRepository extends MongoRepository<TransactionRequests, String> {
}
