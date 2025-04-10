package com.laze.config.jobs;

import com.laze.jobs.listeners.SampleJobListener;
import com.laze.steps.processors.SampleItemProcessor;
import com.laze.steps.readers.SampleItemReader;
import com.laze.steps.writers.SampleItemWriter;
import com.laze.tasklets.SampleTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 'sampleJob' 이라는 이름의 배치 Job을 정의하는 설정 클래스.
 * Chunk 기반 Step과 Tasklet 기반 Step을 포함합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (Lombok)
public class SampleJobConfig {

    // Spring Batch 및 컴포넌트 Bean 자동 주입
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SampleItemReader sampleItemReader;
    private final SampleItemProcessor sampleItemProcessor;
    private final SampleItemWriter sampleItemWriter;
    private final SampleTasklet sampleTasklet;
    private final SampleJobListener sampleJobListener; // 리스너 주입

    /**
     * 'sampleJob' Bean을 생성합니다.
     * @param sampleChunkStep 아래 정의된 Chunk Step Bean
     * @param sampleTaskletStep 아래 정의된 Tasklet Step Bean
     * @return 생성된 Job 객체
     */
    @Bean
    public Job sampleJob(
        @Qualifier("sampleChunkStep") Step sampleChunkStep, // 이름으로 Step Bean 주입
        @Qualifier("sampleTaskletStep") Step sampleTaskletStep
    ) {
        log.info(">>>> Creating 'sampleJob' Bean");
        return new JobBuilder("sampleJob", jobRepository) // Job 이름: sampleJob
            .incrementer(new RunIdIncrementer())   // 동일 파라미터 재실행 허용
            .listener(sampleJobListener)           // Job 리스너 등록
            .start(sampleChunkStep)                // 첫 번째 Step: sampleChunkStep
            .next(sampleTaskletStep)               // 다음 Step: sampleTaskletStep
            .build();
    }

    /**
     * 'sampleChunkStep' Bean을 생성합니다. (Chunk 기반)
     * @return 생성된 Step 객체
     */
    @Bean
    public Step sampleChunkStep() {
        log.info(">>>> Creating 'sampleChunkStep' Bean");
        return new StepBuilder("sampleChunkStep", jobRepository) // Step 이름: sampleChunkStep
            .<String, String>chunk(3, transactionManager) // Chunk 크기: 3, 트랜잭션 매니저 지정
            .reader(sampleItemReader)        // ItemReader 지정
            .processor(sampleItemProcessor)  // ItemProcessor 지정
            .writer(sampleItemWriter)        // ItemWriter 지정
            // .allowStartIfComplete(true) // 완료된 Step도 재시작 허용 (선택 사항)
            .build();
    }

    /**
     * 'sampleTaskletStep' Bean을 생성합니다. (Tasklet 기반)
     * @return 생성된 Step 객체
     */
    @Bean
    public Step sampleTaskletStep() {
        log.info(">>>> Creating 'sampleTaskletStep' Bean");
        return new StepBuilder("sampleTaskletStep", jobRepository) // Step 이름: sampleTaskletStep
            .tasklet(sampleTasklet, transactionManager) // Tasklet 지정, 트랜잭션 매니저 지정
            .build();
    }
}
