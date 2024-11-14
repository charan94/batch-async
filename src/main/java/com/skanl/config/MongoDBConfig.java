package com.skanl.config;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
@Slf4j
public class MongoDBConfig {

    @Value("${primary.mongodb.uri: ''}")
    String connectionString;

    @Value("${mongodb.skanl.database: ''}")
    String databaseName;


    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MongoClient mongoClient() {

        final ConnectionString connString = new ConnectionString(connectionString);
        log.info("MongoDB Connection String: {}", connString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .readPreference(ReadPreference.primary())
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        MongoTemplate template = new MongoTemplate(mongoDatabaseFactory);
        MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
        converter.setMapKeyDotReplacement(".");
        return template;
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
        MongoTransactionManager mongoTransactionManager = new MongoTransactionManager(mongoDatabaseFactory);
        mongoTransactionManager.setDatabaseFactory(mongoDatabaseFactory);
        mongoTransactionManager.afterPropertiesSet();
        return mongoTransactionManager;
    }

}
