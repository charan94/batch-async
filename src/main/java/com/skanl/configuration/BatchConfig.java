package com.skanl.configuration;

import com.mongodb.client.MongoClient;
import com.skanl.batch.processor.VisitorsItemProcessor;
import com.skanl.dao.VisitorsRepo;
import com.skanl.model.Visitors;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig extends DefaultBatchConfiguration {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private VisitorsItemProcessor visitorsItemProcessor;

    @Autowired
    private ItemReader<Visitors> visitorsItemReader;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private VisitorsRepo visitorsRepo;

   @Bean
   public JobRepository mongoJobRepository() {
       return (JobRepository) new MongoJobRepository(mongoClient, "batch-async");
   }


    // item reader
    @Bean
    public FlatFileItemReader<Visitors> flatFileItemReader(@Value("${visitor.filePath: classpath:/visitors.csv}") Resource inputFile){
        FlatFileItemReader<Visitors> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("DEVAL");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setResource(inputFile);
        flatFileItemReader.setLineMapper(linMapper());
        return flatFileItemReader;
    }

    // line mapper
    @Bean
    public LineMapper<Visitors> linMapper() {
        DefaultLineMapper<Visitors> defaultLineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setNames("firstName","lastName","email","phone","address","strVisitDate");
        lineTokenizer.setStrict(false); // Set strict property to false
        defaultLineMapper.setLineTokenizer(lineTokenizer);
        BeanWrapperFieldSetMapper fieldSetMapper = new BeanWrapperFieldSetMapper();
        fieldSetMapper.setTargetType(Visitors.class);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);
        return defaultLineMapper;
    }

    // item processor
    @Bean
    public AsyncItemProcessor<Visitors, Visitors> itemProcessor() {
        AsyncItemProcessor<Visitors, Visitors> processor = new AsyncItemProcessor<>();
        processor.setDelegate(visitorsItemProcessor);
        processor.setTaskExecutor(taskExecutor);
        return processor;
    }

    @Bean
    public AsyncItemWriter<Visitors> asyncItemWriter() {
        AsyncItemWriter<Visitors> writer = new AsyncItemWriter<>();
        writer.setDelegate(visitorsRepo::saveAll);
        return writer;
    }


    // actual job
    @Bean
    public Job importVisitorsJob() {
       JobRepository jobRepository = mongoJobRepository();
        return new JobBuilder("importVisitorsJob", jobRepository)
                .start(importVisitorsStep(jobRepository, platformTransactionManager))
                .build();
    }

    // job step
    @Bean
    public Step importVisitorsStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("importVisitorsStep", jobRepository)
                .<Visitors, Future<Visitors>>chunk(100, platformTransactionManager)
                .reader(visitorsItemReader)
                .processor(itemProcessor())
                .writer(asyncItemWriter())
                .taskExecutor(taskExecutor)
                .build();
    }
}
