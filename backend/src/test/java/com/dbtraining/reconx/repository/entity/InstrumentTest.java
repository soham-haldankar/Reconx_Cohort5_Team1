package com.dbtraining.reconx.repository.entity;

import com.dbtraining.reconx.model.TradeType.AssetClass;
import com.dbtraining.reconx.repository.InstrumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InstrumentTest {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Test
    void metadataRoundTripsThroughJsonbColumn() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("isin", "GB00B16GWD56");
        metadata.put("cusip", "037833100");

        Instrument instrument = new Instrument();
        instrument.setSymbol("VOD.L");
        instrument.setName("Vodafone Group plc");
        instrument.setAssetClass(AssetClass.EQUITY);
        instrument.setCurrency("GBP");
        instrument.setMetadata(metadata);

        instrumentRepository.saveAndFlush(instrument);
        instrumentRepository.flush();

        Optional<Instrument> reloaded = instrumentRepository.findBySymbol("VOD.L");

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getMetadata()).isEqualTo(metadata);
        assertThat(reloaded.get().getMetadata())
                .containsEntry("isin", "GB00B16GWD56")
                .containsEntry("cusip", "037833100");
    }
}