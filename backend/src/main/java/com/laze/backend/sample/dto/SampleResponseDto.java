package com.laze.backend.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder // 빌더 패턴 사용
@Schema(description = "샘플 데이터 응답 DTO") // Swagger 문서용 설명
public class SampleResponseDto {
    @Schema(description = "샘플 ID", example = "1")
    private Long sampleId;
    @Schema(description = "샘플 이름", example = "테스트 샘플")
    private String sampleName;
    @Schema(description = "설명", example = "이것은 샘플 데이터입니다.")
    private String description;
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    // 필요시 Entity -> DTO 변환 정적 팩토리 메소드 추가 가능
    // public static SampleResponseDto fromEntity(SampleEntity entity) { ... }
}
