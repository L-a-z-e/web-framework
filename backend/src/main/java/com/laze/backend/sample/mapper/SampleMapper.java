package com.laze.backend.sample.mapper;

import com.laze.backend.sample.dto.SampleRequestDto;
import com.laze.backend.sample.dto.SampleResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // 필요시 사용

import java.util.List;
import java.util.Optional;

@Mapper
public interface SampleMapper {
    String getCurrentTime();

    /**
     * 새로운 샘플 데이터 저장
     * @param requestDto 저장할 데이터 DTO (id 는 자동 생성 가정)
     * @return 영향을 받은 행 수
     */
    int insertSample(SampleRequestDto requestDto);

    /**
     * ID로 샘플 데이터 조회
     * @param sampleId 조회할 샘플 ID
     * @return 조회된 샘플 데이터 DTO (Optional)
     */
    Optional<SampleResponseDto> findSampleById(@Param("sampleId") Long sampleId);

    /**
     * 모든 샘플 데이터 목록 조회 (페이징 미적용 예시)
     * @return 샘플 데이터 DTO 리스트
     */
    List<SampleResponseDto> findAllSamples();

    // 필요시 페이징 적용된 목록 조회 메소드 추가
    // List<SampleResponseDto> findSamplesPaged(Map<String, Object> params);
    // int countSamples(Map<String, Object> params); // 페이징 시 전체 개수 조회
}
