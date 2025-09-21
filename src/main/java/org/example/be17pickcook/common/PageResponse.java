package org.example.be17pickcook.common;

import lombok.Getter;
import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final int currentPage;
    private final int totalPages;
    private final long totalElements;
    private final int size;
    private final Boolean hasNext;

    public PageResponse(List<T> content, int currentPage, int totalPages, long totalElements, int size, Boolean hasNext) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.hasNext = hasNext;
    }


    // 기존 Page<T> 사용 시
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                null // 기존 호출에서는 hasNext null
        );
    }

    // 무한 스크롤용 Page<T> 변환 시
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page, boolean hasNext) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                hasNext
        );
    }
}
