package com.swer313.projectstep1.availabilitypricing.availability;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void from_copiesPageMetadataAndMapsContent() {
        List<String> content = List.of("a", "b");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 5);

        List<Integer> mapped = List.of(1, 2);
        PagedResponse<Integer> resp = PagedResponse.from(page, mapped);

        assertEquals(mapped, resp.getContent());
        assertEquals(1, resp.getPageNumber());
        assertEquals(2, resp.getPageSize());
        assertEquals(5, resp.getTotalElements());
        assertEquals(page.getTotalPages(), resp.getTotalPages());
        assertEquals(page.isFirst(), resp.isFirst());
        assertEquals(page.isLast(), resp.isLast());
    }
}

