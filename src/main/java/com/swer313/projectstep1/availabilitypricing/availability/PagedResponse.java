package com.swer313.projectstep1.availabilitypricing.availability;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

    private final List<T> content;
    private final int     pageNumber;
    private final int     pageSize;
    private final long    totalElements;
    private final int     totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PagedResponse(
            List<T> content,
            int pageNumber, int pageSize,
            long totalElements, int totalPages,
            boolean first, boolean last,
            boolean hasNext, boolean hasPrevious) {

        this.content       = content;
        this.pageNumber    = pageNumber;
        this.pageSize      = pageSize;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.first         = first;
        this.last          = last;
        this.hasNext       = hasNext;
        this.hasPrevious   = hasPrevious;
    }

    /** Factory — يبني من Page<T> مع mapped content. */
    public static <S, R> PagedResponse<R> from(Page<S> page, List<R> mappedContent) {
        return new PagedResponse<>(
                mappedContent,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    public List<T> getContent()       { return content; }
    public int     getPageNumber()    { return pageNumber; }
    public int     getPageSize()      { return pageSize; }
    public long    getTotalElements() { return totalElements; }
    public int     getTotalPages()    { return totalPages; }
    public boolean isFirst()          { return first; }
    public boolean isLast()           { return last; }
    public boolean isHasNext()        { return hasNext; }
    public boolean isHasPrevious()    { return hasPrevious; }
}