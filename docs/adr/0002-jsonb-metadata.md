# ADR-0002 — Use JSONB for `instruments.metadata`

- Status: Accepted
- Date: 2026-07-27
- Deciders: ReconX team

## Context

Instruments carry a growing set of attributes sourced from different feeds
(exchange codes, sector tags, vendor-specific identifiers) that vary by
asset class and change as new upstream sources are onboarded. Modeling
every attribute as its own column would mean a schema migration for every
new attribute, and most attributes only apply to a subset of the roughly
50,000 instruments we expect to track. Analysts also need to query and
filter on these attributes without waiting on a schema change.

## Decision

Add a single `metadata JSONB NOT NULL DEFAULT '{}'::JSONB` column to
`instruments` to hold variable, semi-structured attributes, instead of
adding new columns per attribute or introducing a generic EAV
(entity-attribute-value) side table.

## Consequences

**Positive**
- New attributes from a new feed require no DDL — just a data change.
- Postgres' JSONB supports indexed containment queries, avoiding the
  join overhead of an EAV design.
- Keeps `instruments` as a single row per instrument, simplifying joins
  elsewhere in the schema.

**Negative**
- Loses column-level type constraints and NOT NULL guarantees for
  individual attributes — validation must move to the application layer.
- Query plans against JSONB fields are less predictable than against typed
  columns without careful indexing (see ADR-0003).
- Risk of the column becoming a dumping ground without application-level
  discipline on which keys are permitted.

---

## Prompt used

```
You are an enterprise software architect. Write an Architecture Decision Record
(ADR) in the Michael Nygard format (Title, Status, Context, Decision,
Consequences) for the following decision.

System: ReconX, a near-prod trade reconciliation platform.
Stack: PostgreSQL 16, Spring Boot 3, Kafka, React.
Scale: ~50,000 trades/day, 5-year retention, 10 concurrent recon analysts.

Decision to record: Add a JSONB metadata column to instruments instead of
per-attribute columns or an EAV side table.

Alternatives we considered: one column per attribute with frequent
migrations; a generic entity-attribute-value (EAV) table; a separate
per-asset-class metadata table.

Constraints / forces: attributes vary by asset class and upstream feed;
new attributes arrive as new feeds are onboarded; analysts need to query
metadata without waiting on schema migrations.

Format: Markdown, Nygard 5-section template, no fluff. Keep under 300 words.
Include a "Status: Accepted | Date: <YYYY-MM-DD>" line.
```