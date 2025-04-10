package com.laze.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 공통 설정 클래스.
 * Spring Boot AutoConfiguration 에 의해 대부분 자동 설정되므로,
 * 특별한 커스터마이징이 필요할 때 이 클래스를 사용합니다.
 * (예: JobRepository 테이블 접두사 변경, 커스텀 JobLauncher 설정 등)
 */
@Configuration
public class BatchBaseConfig {

    // 필요한 경우 여기에 커스텀 Bean 정의
    // 예: 테이블 접두사 변경
    /*
    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setTablePrefix("FW_BATCH_"); // 기본 'BATCH_' 대신 사용
        factory.afterPropertiesSet();
        return factory.getObject();
    }
    */
}
