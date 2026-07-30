package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResponseTest {

    @Test
    void of_mapsPageContentAndPreservesPagingMetadata() {
        Trade t1 = new Trade();
        t1.setTradeRef("ref1");
        Trade t2 = new Trade();
        t2.setTradeRef("ref2");

        var page = new PageImpl<>(List.of(t1, t2), PageRequest.of(0, 10), 2);

        PagedResponse<String> result = PagedResponse.of(page, Trade::getTradeRef);

        assertThat(result.items()).containsExactly("ref1", "ref2");
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalPages()).isEqualTo(1);
    }
}