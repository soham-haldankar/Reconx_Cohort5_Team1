package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import com.dbtraining.reconx.repository.ReconResultRepository;
import org.mockito.ArgumentCaptor;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;



/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();
    
    
    @Test
    void testReconcile_savesResultWithMatchedStatus() {
        // given
        ReconResultRepository repo = mock(ReconResultRepository.class);
        ReconciliationEngine engine = new ReconciliationEngine();
        ReconciliationService svc = new ReconciliationService(engine, repo);

        Trade i = new Trade("TRD-1", "CP-1", "SAP.DE",
                new BigDecimal("10"), new BigDecimal("100"), LocalDate.now());
        Trade e = new Trade("TRD-1", "CP-1", "SAP.DE",
                new BigDecimal("10"), new BigDecimal("100"), LocalDate.now());

        // when
        svc.runRecon(List.of(i), List.of(e));

        // then
        ArgumentCaptor<ReconResult> captor = ArgumentCaptor.forClass(ReconResult.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().tradeRef()).isEqualTo("TRD-1");
        assertThat(captor.getValue().status()).isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        var in  = List.<TradeType>of(equity("EQU-20260603-0001", "100.00", "10"));
        var out = List.<TradeType>of(equity("EQU-20260603-0001", "100.00", "10"));

        List<ReconResult> results = engine.reconcile(in, out, ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260603-0001");
    }
    }


    @ParameterizedTest(name="price diff {0} stays within 1% tolerance -> MATCHED")
    @ValueSource(strings = {"0.10", "0.50", "0.99"})
    void testReconcile_priceTolerance_withinThreshold(String diff) {
        // TODO(TICKET-ADV041): prices 100.00 vs 100.50 + PRICE_TOLERANCE_1PCT rule -> status MATCHED.
        BigDecimal priceDifference = new BigDecimal(diff);
        BigDecimal internalPrice=new BigDecimal("100.00");
        BigDecimal externalPrice=internalPrice.add(priceDifference);
        List<TradeType> internalTrade = List.<TradeType>of(equity("EQU-20260603-0002", internalPrice.toString(), "10"));
        List<TradeType> externalTrade =List.<TradeType>of(equity("EQU-20260603-0002", externalPrice.toString(), "10"));
        ReconciliationRule rule = ReconciliationRule.PRICE_TOLERANCE_1PCT;


        // Act
        List<ReconResult> results = reconciliationEngine.reconcile(
                internalTrade,
               externalTrade,
                rule
        );

        // Assert
        assertEquals(1, results.size());
        assertEquals(ReconStatus.MATCHED, results.get(0).status());
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        // TODO(TICKET-ADV042): internal trade with no external counterpart -> status BREAK,
        //                     discrepancyType = "MISSING_EXTERNAL".
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV042 not implemented yet");
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        List<ReconResult> results = engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT);
        assertThat(results).isEmpty();
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
