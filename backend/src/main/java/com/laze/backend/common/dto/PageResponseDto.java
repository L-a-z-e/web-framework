package com.laze.backend.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponseDto<T> {
    private final List<T> content;
    private final int totalPages;
    private final long totalElements;
    private final int size;
    private final int number; // 현재 페이지 번호 (0부터 시작)
    private final boolean first;
    private final boolean last;
    private final int numberOfElements; // 현재 페이지의 요소 개수

    // 생성자 등 필요에 따라 추가
    private PageResponseDto(Page<T> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.numberOfElements = page.getNumberOfElements();
    }

    // Spring Data Page 객체로부터 생성하는 정적 팩토리 메소드
    public static <T> PageResponseDto<T> fromPage(Page<T> page) {
        return new PageResponseDto<>(page);
    }

    // 직접 데이터를 받아 생성하는 생성자 (MyBatis 등 사용 시)
    public PageResponseDto(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.size = pageSize;
        this.number = pageNumber;
        this.totalElements = totalElements;
        this.totalPages = (pageSize == 0) ? 1 : (int) Math.ceil((double) totalElements / (double) pageSize);
        this.first = (pageNumber == 0);
        this.last = (pageNumber >= totalPages - 1);
        this.numberOfElements = content.size();
    }
}
