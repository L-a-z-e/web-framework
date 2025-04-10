package com.laze.steps.readers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 간단한 문자열 메시지를 생성하여 읽는 샘플 ItemReader
 */
@Slf4j
@Component // Spring Bean으로 등록
public class SampleItemReader implements ItemReader<String> {

    private Queue<String> messageQueue;

    // 생성자에서 테스트 데이터 초기화
    public SampleItemReader() {
        messageQueue = new LinkedList<>();
        for (int i = 1; i <= 5; i++) { // 5개의 메시지 생성
            messageQueue.offer("Sample Message " + i);
        }
        log.info("SampleItemReader initialized with {} messages.", messageQueue.size());
    }

    /**
     * 큐에서 메시지를 하나씩 읽어 반환합니다. 더 이상 없으면 null을 반환합니다.
     * @return 읽은 메시지 문자열 또는 null
     */
    @Override
    public String read() throws Exception {
        // Thread.sleep(300); // 테스트를 위해 잠시 지연 (선택 사항)
        String message = messageQueue.poll();
        if (message != null) {
            log.info("Reading message: {}", message);
        } else {
            log.info("Finished reading messages."); // null 반환 시 해당 Chunk 종료
        }
        return message;
    }
}
