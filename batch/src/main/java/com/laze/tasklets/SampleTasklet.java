package com.laze.tasklets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * 간단한 로그 메시지를 출력하는 샘플 Tasklet
 */
@Slf4j
@Component
public class SampleTasklet implements Tasklet {

    /**
     * Tasklet 작업을 수행합니다.
     * @param contribution Step 메타데이터 접근/수정 객체
     * @param chunkContext Chunk 관련 컨텍스트 정보 (Job Parameter 등 접근 가능)
     * @return 작업 완료 상태 (보통 FINISHED)
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(">>>>> Executing SampleTasklet <<<<<");

        // 예시: Job Parameter 접근
        // Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        // String paramValue = (String) jobParameters.get("yourParamName");
        // log.info("Job Parameter value: {}", paramValue);

        // 간단한 작업 수행 (여기서는 로그 출력)
        log.info("Sample Tasklet executed successfully.");

        // 작업 완료 후 FINISHED 반환 (더 이상 반복하지 않음)
        return RepeatStatus.FINISHED;
    }
}
