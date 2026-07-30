package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerivativeTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        DerivativeTrade t = sampleDerivative(
                "ADV-20260603-0001",
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 12, 19));
        assertThat(t.tradeRef().value()).isEqualTo("ADV-20260603-0001");
        assertThat(t.optionType()).isEqualTo(DerivativeTrade.OptionType.CALL);
        assertThat(t.notional().amount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(t.notional().currency().getCurrencyCode()).isEqualTo("USD");
        assertThat(t.assetClass()).isEqualTo(TradeType.AssetClass.DERIVATIVE);
    }

    @Test
    void builder_expiryInPastButAfterTradeDate_stillBuilds() {
        // Historical option: both tradeDate and expiry are in the past relative to
        // today, but expiry is still after tradeDate — this is valid replay/reconciliation data.
        DerivativeTrade t = sampleDerivative(
                "ADV-20200101-0002",
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2020, 1, 15));
        assertThat(t.expiry()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(t.assetClass()).isEqualTo(TradeType.AssetClass.DERIVATIVE);
    }

    @Test
    void builder_expiryBeforeTradeDate_throws() {
        assertThatThrownBy(() -> DerivativeTrade.builder()
                .tradeRef(TradeRef.of("ADV-20260603-0003"))
                .underlying("AAPL")
                .strike(new BigDecimal("100"))
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2026, 5, 1))
                .optionType(DerivativeTrade.OptionType.PUT)
                .currency("USD").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expiry cannot be before tradeDate");
    }

    @Test
    void builder_missingStrike_throws() {
        assertThatThrownBy(() -> DerivativeTrade.builder()
                .tradeRef(TradeRef.of("ADV-20260603-0004"))
                .underlying("AAPL")
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2026, 12, 19))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("strike");
    }

    private DerivativeTrade sampleDerivative(String ref, LocalDate tradeDate, LocalDate expiry) {
        return DerivativeTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .underlying("AAPL")
                .strike(new BigDecimal("100"))
                .quantity(new BigDecimal("50"))
                .expiry(expiry)
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD").side(Side.BUY)
                .tradeDate(tradeDate)
                .counterpartyId(1L).build();
    }
}