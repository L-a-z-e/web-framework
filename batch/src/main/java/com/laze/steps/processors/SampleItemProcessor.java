package com.laze.steps.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 읽은 문자열 메시지를 대문자로 변환하고 접미사를 추가하는 샘플 ItemProcessor
 */
@Slf4j
@Component
public class SampleItemProcessor implements ItemProcessor<String, String> { // 입력 타입: String, 출력 타입: String

    /**
     * 입력된 아이템을 가공하여 반환합니다.
     * @param item ItemReader로부터 전달받은 아이템
     * @return 가공된 아이템
     */
    @Override
    public String process(String item) throws Exception {
        // 데이터를 대문자로 변환하고 "[PROCESSED]" 추가
        String processedItem = item.toUpperCase() + " [PROCESSED]";
        log.info("Processing item: {} -> {}", item, processedItem);
        // 만약 특정 조건에서 null을 반환하면 해당 아이템은 필터링되어 Writer로 넘어가지 않음
        return processedItem;
    }
}
