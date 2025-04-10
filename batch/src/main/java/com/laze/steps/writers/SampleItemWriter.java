package com.laze.steps.writers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 처리된 문자열 메시지 청크(Chunk)를 로그로 출력하는 샘플 ItemWriter
 */
@Slf4j
@Component
public class SampleItemWriter implements ItemWriter<String> { // 처리된 데이터 타입: String

    /**
     * 전달받은 데이터 청크(Chunk)를 처리합니다. (여기서는 로그 출력)
     * @param chunk 처리할 데이터 목록
     */
    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        log.info("Writing chunk of size: {}", chunk.getItems().size());
        for (String item : chunk.getItems()) {
            log.info(" > Writing item: {}", item);
            // 실제 구현에서는 여기서 DB 저장, 파일 쓰기 등을 수행
        }
    }
}
