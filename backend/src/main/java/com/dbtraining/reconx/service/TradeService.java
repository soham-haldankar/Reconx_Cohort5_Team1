package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.dto.TradeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                         CounterpartyRepository cpRepo,
                         InstrumentRepository instRepo,
                         TradeEventProducer events,
                         TradeMetrics metrics) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }

    public Trade create(TradeRequest req, String actor) {
        // TICKET-ADV064
        if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {
            throw new DuplicateTradeRefException(req.tradeRef());
        }

        Trade t = new Trade();
        t.setTradeRef(req.tradeRef());
        t.setTradeDate(req.tradeDate());
        t.setQuantity(req.quantity());
        t.setPrice(req.price());
        t.setStatus("PENDING");

        t.setInstrument(instRepo.findById(req.instrumentId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Instrument not found: " + req.instrumentId())));

        t.setCounterparty(cpRepo.findById(req.counterpartyId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Counterparty not found: " + req.counterpartyId())));

        Trade saved = tradeRepo.save(t);

        // TICKET-ADV083
        metrics.incrementTradeCreated();
        metrics.recordTradeValue(saved.getQuantity().multiply(saved.getPrice()).doubleValue());

        // TICKET-ADV129
        events.publish(new TradeEvent(
                UUID.randomUUID(),
                saved.getTradeRef(),
                TradeEvent.EventType.TRADE_CREATED,
                Instant.now(),
                actor,
                null,
                saved.getStatus()
        ));

        return saved;
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        // TODO(TICKET-ADV065): load by id (throw TradeNotFoundException if missing),
        //   copy mutable fields from req, save, publish a TRADE_UPDATED event.
        throw new UnsupportedOperationException("TICKET-ADV065");
    }

    public Trade updateStatus(Long id, String status, String actor) {
        // TODO(TICKET-ADV066): load, setStatus(status), save, publish TRADE_UPDATED
        //   with the new status in the "after" slot of the event.
        throw new UnsupportedOperationException("TICKET-ADV066");
    }

    public void softDelete(Long id, String actor) {
        // TODO(TICKET-ADV067): load, call t.softDelete() (sets deleted_at), save,
        //   publish a TRADE_CANCELLED event.
        throw new UnsupportedOperationException("TICKET-ADV067");
    }

    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        Specification<Trade> spec = Specification
                .where(tradeDateBetween(from, to))
                .and(hasStatus(status))
                .and(hasCounterparty(counterpartyId));
        return tradeRepo.findAll(spec, pageable);
    }
}


