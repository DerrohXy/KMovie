package com.kmovie.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> items;
    private final int page;
    private final int count;
    private final long totalItems;
    private final int totalPages;

    public PageResponse(List<T> items, int page, int count, long totalItems) {
        this.items = items;
        this.page = page;
        this.count = count;
        this.totalItems = totalItems;
        this.totalPages = count == 0 ? 0 : (int) Math.ceil((double) totalItems / count);
    }
}
