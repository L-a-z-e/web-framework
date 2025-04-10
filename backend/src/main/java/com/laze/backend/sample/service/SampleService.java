package com.laze.backend.sample.service;

import com.laze.backend.sample.dto.SampleRequestDto;
import com.laze.backend.sample.dto.SampleResponseDto;

import java.util.List;

public interface SampleService {
    /** 샘플 생성 */
    SampleResponseDto createSample(SampleRequestDto requestDto);
    /** 샘플 단건 조회 */
    SampleResponseDto getSampleById(Long sampleId);
    /** 샘플 전체 목록 조회 */
    List<SampleResponseDto> getAllSamples();
    // 필요시 페이징 조회 메소드 등 추가
    // PageResponseDto<SampleResponseDto> findSamples(Pageable pageable, SampleSearchCondition condition);
}
