package com.laze.backend.sample.controller;

// --- !!! 반환 타입 변경 및 ResponseEntity 제거 !!! ---
import com.laze.backend.common.aop.NoApiResponseWrapper;
import com.laze.backend.sample.dto.SampleRequestDto;
import com.laze.backend.sample.dto.SampleResponseDto;
import com.laze.backend.sample.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sample API", description = "샘플 데이터 관리 API")
@RestController
@RequestMapping("/api/sample")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @Operation(summary = "샘플 목록 조회")
    @GetMapping
    public List<SampleResponseDto> getAllSamples() {
        return sampleService.getAllSamples();
    }

    @Operation(summary = "샘플 상세 조회")
    @GetMapping("/{sampleId}")
    public SampleResponseDto getSampleById(@PathVariable Long sampleId) {
        return sampleService.getSampleById(sampleId);
    }

    @Operation(summary = "샘플 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 성공 시 201 상태 코드 설정
    public SampleResponseDto createSample(@Valid @RequestBody SampleRequestDto requestDto) {
        return sampleService.createSample(requestDto);
    }

    @Operation(summary = "샘플 삭제")
    @DeleteMapping("/{sampleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSample(@PathVariable Long sampleId) {
//        sampleService.deleteSample(sampleId);
        // void 를 반환하면 ApiResponseWrapper 가 data 없는 성공 응답으로 처리 가능
        // 또는 명시적으로 null 반환: return null;
    }

    // --- !!! 파일 다운로드 예시 (@NoApiResponseWrapper 사용) !!! ---
    @Operation(summary = "샘플 파일 다운로드 (래핑 제외 예시)")
    @GetMapping("/{sampleId}/download")
    @NoApiResponseWrapper // 이 메소드는 자동 래핑에서 제외
    public ResponseEntity<Resource> downloadSampleFile(@PathVariable Long sampleId) {
        // Resource file = sampleService.getSampleFile(sampleId); // 파일 Resource 로드 로직
        // return ResponseEntity.ok()
        //        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
        //        .body(file);

        // 임시 구현 (실제 파일 로드 로직 필요)
        return ResponseEntity.notFound().build();
    }
}
