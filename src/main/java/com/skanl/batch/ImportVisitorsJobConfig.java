package com.skanl.batch;

import com.skanl.batch.explore.MongoJobExplorerFactoryBean;
import com.skanl.batch.support.MongoJobRepositoryFactoryBean;
import com.skanl.model.Visitor;
import com.skanl.repository.VisitorsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.policy.AlwaysRetryPolicy;

@Configuration
public class ImportVisitorsJobConfig {

    @Autowired
    private TaskExecutor taskExecutor;

    @Bean
    @Primary
    public JobRepository jobRepository(MongoTemplate mongoTemplate, MongoTransactionManager mongoTransactionManager) throws Exception {
        MongoJobRepositoryFactoryBean jobRepositoryFactoryBean = new MongoJobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setMongoOperations(mongoTemplate);
        jobRepositoryFactoryBean.setTransactionManager(mongoTransactionManager);
        jobRepositoryFactoryBean.afterPropertiesSet();
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public JobExplorer jobExplorer(MongoTemplate mongoTemplate, MongoTransactionManager mongoTransactionManager) throws Exception {
        MongoJobExplorerFactoryBean jobExplorerFactoryBean = new MongoJobExplorerFactoryBean();
        jobExplorerFactoryBean.setMongoOperations(mongoTemplate);
        jobExplorerFactoryBean.setTransactionManager(mongoTransactionManager);
        jobExplorerFactoryBean.afterPropertiesSet();
        return jobExplorerFactoryBean.getObject();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        try {
            jobLauncher.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jobLauncher;
    }

    @Profile("manager")
    @Configuration
    @EnableBatchIntegration
    @RequiredArgsConstructor
    public static class ManagerConfig {
        private final RemoteChunkingManagerStepBuilderFactory remoteChunkingManagerStepBuilderFactory;

        private final KafkaTemplate<String, Visitor> kafkaTemplate;

        @Autowired
        private MongoTransactionManager transactionManager;

        @Bean
        public Job importVisitorsJob(JobRepository jobRepository, Step importVisitorsStep){
            return new JobBuilder("importVisitorsJob",jobRepository)
                    .start(importVisitorsStep)
                    .build();
        }

        @Bean
        public TaskletStep importVisitorsStep(){
            return this.remoteChunkingManagerStepBuilderFactory.get("importVisitorsStep")
                    .<Visitor,Visitor>chunk(100)
                    .reader(visitorReader())
                    .outputChannel(outboundRequests()) // requests sent to workers
                    .inputChannel(inboundReplies()) // replies received from workers
                    .allowStartIfComplete(Boolean.TRUE)
                    .transactionManager(transactionManager)
                    .retry(Exception.class)
                    .retryLimit(100)
                    .retryPolicy(new AlwaysRetryPolicy())
                    .build();
        }




        public QueueChannel inboundReplies(){
            return new QueueChannel();
        }

        @Bean
        public IntegrationFlow inboundFlow(ConsumerFactory<String, Visitor> cf) {
            return IntegrationFlow
                    .from(Kafka.messageDrivenChannelAdapter(cf, "visitor-chunkReplies")) //consuming from kafka
                    .log(LoggingHandler.Level.WARN)
                    .channel(inboundReplies())
                    .get();
        }


        @Bean
        public DirectChannel outboundRequests() {
            return new DirectChannel();
        }


        @Bean
        public IntegrationFlow outboundFlow() {
            var messageHandler = new KafkaProducerMessageHandler<>(kafkaTemplate);
            messageHandler.setTopicExpression(new LiteralExpression("visitor-chunkRequests"));
            return IntegrationFlow.from(outboundRequests())
                    .log(LoggingHandler.Level.WARN)
                    .handle(messageHandler)
                    .get();
        }

        public FlatFileItemReader<Visitor> visitorReader(){
            return new FlatFileItemReaderBuilder<Visitor>()
                    .resource(new ClassPathResource("visitors.csv"))
                    .name("visitorReader")
                    .delimited()
                    .delimiter(",")
                    .names("firstName","lastName","email","phoneNumber","address","strVisitDate")
                    .linesToSkip(1)//skipping the header of file
                    .targetType(Visitor.class)
                    .build();
        }

    }

    @Profile("worker")
    @Configuration
    @EnableBatchIntegration
    @RequiredArgsConstructor
    @Slf4j
    public static class WorkerConfig {
        private final RemoteChunkingWorkerBuilder<Visitor,Visitor> remoteChunkingWorkerBuilder;
        private final KafkaTemplate<String, Visitor> kafkaTemplate;

        @Autowired
        private VisitorsRepo visitorsRepo;

        @Bean
        public IntegrationFlow salesWorkerStep(){
            return this.remoteChunkingWorkerBuilder
                    .inputChannel(inboundRequests())
                    .itemProcessor(visitor -> {
                        log.info("processing data on worker: {}", visitor);
                        return visitor;
                    })
                    .itemWriter(items -> visitorsRepo.saveAll(items))
                    .outputChannel(outboundRequests())
                    .build();
        }

        @Bean
        public QueueChannel inboundRequests() {
            return new QueueChannel();
        }

        @Bean
        public IntegrationFlow inboundFlow(ConsumerFactory<String, Visitor> cf) {
            return IntegrationFlow
                    .from(Kafka.messageDrivenChannelAdapter(cf, "visitor-chunkRequests"))
                    .channel(inboundRequests())
                    .get();
        }



        @Bean
        public DirectChannel outboundRequests() {
            return new DirectChannel();
        }

        @Bean
        public IntegrationFlow outboundFlow() {
            var messageHandler = new KafkaProducerMessageHandler<String,Visitor>(kafkaTemplate);
            messageHandler.setTopicExpression(new LiteralExpression("visitor-chunkReplies"));
            return IntegrationFlow.from(outboundRequests())
                    .log(LoggingHandler.Level.WARN)
                    .handle(messageHandler)
                    .get();
        }
    }

}
