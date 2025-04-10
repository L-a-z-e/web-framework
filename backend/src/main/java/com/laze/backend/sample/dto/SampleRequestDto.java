package com.laze.backend.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank; // Validation 어노테이션
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor; // 기본 생성자 (for Jackson/Validation)
import lombok.Setter; // Setter 필요 (요청 Body 매핑 위해)

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "샘플 데이터 생성 요청 DTO")
public class SampleRequestDto {

    @NotBlank(message = "샘플 이름은 필수입니다.") // Validation: 비어있으면 안됨
    @Size(max = 100, message = "샘플 이름은 최대 100자까지 가능합니다.") // Validation: 길이 제한
    @Schema(description = "샘플 이름", requiredMode = Schema.RequiredMode.REQUIRED, example = "새 샘플")
    private String sampleName;

    @Size(max = 255, message = "설명은 최대 255자까지 가능합니다.")
    @Schema(description = "설명", example = "새로운 샘플 데이터에 대한 설명입니다.")
    private String description;

    // Service 에서 사용할 Entity 변환 메소드 (선택 사항)
    // public SampleEntity toEntity() { ... }
}
