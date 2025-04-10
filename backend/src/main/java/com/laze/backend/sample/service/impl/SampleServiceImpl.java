package com.laze.backend.sample.service.impl;

import com.laze.backend.common.exception.BusinessException;
import com.laze.backend.common.exception.ErrorCode;
import com.laze.backend.sample.dto.SampleRequestDto;
import com.laze.backend.sample.dto.SampleResponseDto;
import com.laze.backend.sample.mapper.SampleMapper;
import com.laze.backend.sample.service.SampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleServiceImpl implements SampleService {

    private final SampleMapper sampleMapper;

    @Override
    @Transactional // 데이터 변경이 있으므로 트랜잭션 적용
    public SampleResponseDto createSample(SampleRequestDto requestDto) {
        log.info("Creating new sample: {}", requestDto.getSampleName());
        // 여기서 DTO -> Entity 변환이 필요하다면 수행
        // 예: SampleEntity sampleEntity = requestDto.toEntity();
        //     sampleMapper.insertSampleEntity(sampleEntity);

        // 현재는 DTO 를 바로 Mapper 로 전달 (간단한 경우)
        int insertedCount = sampleMapper.insertSample(requestDto);
        log.info("Inserted count: {}", insertedCount);

        // 삽입 후 ID를 알 수 있다면 해당 ID로 다시 조회하여 반환하는 것이 일반적
        // (MyBatis <selectKey> 등을 사용하거나, 별도 조회 쿼리 실행)
        // 여기서는 임시로 null 또는 단순 메시지 반환 가정 (개선 필요)
        // Long generatedId = requestDto.getSampleId(); // <selectKey> 로 ID 가 설정되었다면
        // if (generatedId != null) {
        //     return getSampleById(generatedId);
        // }
        // 임시 반환 (실제로는 생성된 리소스 정보 반환 권장)
        return SampleResponseDto.builder()
            .sampleName(requestDto.getSampleName())
            .description(requestDto.getDescription())
            .build();
    }

    @Override
    @Transactional(readOnly = true) // 조회는 readOnly 트랜잭션
    public SampleResponseDto getSampleById(Long sampleId) {
        log.info("Finding sample by ID: {}", sampleId);
        return sampleMapper.findSampleById(sampleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Sample not found with ID: " + sampleId));
        // .orElseThrow(() -> new ResourceNotFoundException("Sample not found with ID: " + sampleId)); // 커스텀 예외 사용
    }

    @Override
    @Transactional(readOnly = true)
    public List<SampleResponseDto> getAllSamples() {
        log.info("Finding all samples");
        return sampleMapper.findAllSamples();
    }
}
