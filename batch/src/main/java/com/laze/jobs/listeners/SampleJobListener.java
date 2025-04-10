package com.laze.jobs.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Job 실행 전후에 로그를 남기는 샘플 JobExecutionListener
 */
@Slf4j
@Component
public class SampleJobListener implements JobExecutionListener {

    /**
     * Job 실행 전에 호출됩니다.
     * @param jobExecution Job 실행 정보
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">>>> ===================================================== <<<<");
        log.info(">>>> Starting Job: {} with Job ID: {}",
            jobExecution.getJobInstance().getJobName(),
            jobExecution.getJobId());
        log.info(">>>> Job Parameters: {}", jobExecution.getJobParameters());
        log.info(">>>> ===================================================== <<<<");
    }

    /**
     * Job 실행 완료 후 (성공 또는 실패 시 모두) 호출됩니다.
     * @param jobExecution Job 실행 정보
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("<<<< ===================================================== >>>>");
        log.info("<<<< Finished Job: {} with Job ID: {}",
            jobExecution.getJobInstance().getJobName(),
            jobExecution.getJobId());
        log.info("<<<< Exit Status: {}", jobExecution.getExitStatus());
        log.info("<<<< ===================================================== >>>>");
    }
}
