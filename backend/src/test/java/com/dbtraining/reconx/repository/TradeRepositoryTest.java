package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Test
    void findByFilters_returnsTradesWithinDateRange() {
        Counterparty cp = new Counterparty();
        cp.setName("Test CP");
        ReflectionTestUtils.setField(cp, "region", "US");
        ReflectionTestUtils.setField(cp, "leiCode", "TESTLEI0000000001");
        cp = counterpartyRepository.saveAndFlush(cp);

        Instrument instrument = new Instrument();
        ReflectionTestUtils.setField(instrument, "symbol", "TST");
        ReflectionTestUtils.setField(instrument, "name", "Test Instrument");
        ReflectionTestUtils.setField(instrument, "currency", "USD");
        ReflectionTestUtils.setField(instrument, "assetClass", "EQUITY");
        instrument = instrumentRepository.saveAndFlush(instrument);

        Trade inRange = new Trade();
        inRange.setTradeRef("TRD-IN-RANGE");
        inRange.setCounterparty(cp);
        inRange.setInstrument(instrument);
        inRange.setAssetClass("EQUITY");
        inRange.setSide("BUY");
        inRange.setQuantity(new BigDecimal("100"));
        inRange.setPrice(new BigDecimal("50"));
        inRange.setTradeDate(LocalDate.of(2026, 6, 15));
        tradeRepository.saveAndFlush(inRange);

        Trade outOfRange = new Trade();
        outOfRange.setTradeRef("TRD-OUT-OF-RANGE");
        outOfRange.setCounterparty(cp);
        outOfRange.setInstrument(instrument);
        outOfRange.setAssetClass("EQUITY");
        outOfRange.setSide("BUY");
        outOfRange.setQuantity(new BigDecimal("100"));
        outOfRange.setPrice(new BigDecimal("50"));
        outOfRange.setTradeDate(LocalDate.of(2025, 1, 1));
        tradeRepository.saveAndFlush(outOfRange);

        Page<Trade> result = tradeRepository.findByFilters(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Trade::getTradeRef)
                .containsExactly("TRD-IN-RANGE");
    }
}