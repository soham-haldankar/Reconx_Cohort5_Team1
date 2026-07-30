# ADR-0001 — Partition the `trades` table by `trade_date`

- Status: Accepted
- Date: 2026-07-27
- Deciders: ReconX team

## Context

`trades` is our highest-volume table — ~50k inserts/day, 5-year retention
implies roughly 91M rows at steady state. The large majority of queries
(dashboards, recon runs, analyst lookups) filter on a date range, often a
single day or month. A single unpartitioned table forces full-table scans
for date-range deletes and complicates archiving old trade data to satisfy
the 5-year-retention requirement.

## Decision

Partition `trades` by RANGE on `trade_date`, with one partition per calendar
month. The primary key includes `trade_date` to satisfy Postgres' partitioning
constraint. Child partitions are named `trades_yYYYYmMM` and pre-created for
the next 12 months by a monthly maintenance job. A `trades_default` partition
catches any out-of-range inserts so writes are never rejected; the
maintenance job alerts on unexpected default-partition inserts.

## Consequences

**Positive**
- Partition pruning eliminates roughly 11/12 of the data on a typical
  month-filtered query.
- Archival becomes a DDL operation (`DETACH PARTITION`) instead of a
  row-level delete.
- Per-partition indexes are smaller and faster to maintain.

**Negative**
- Composite PK `(id, trade_date)` complicates JPA `@Id` mapping.
- Cross-partition unique constraints (e.g. `trade_ref`) require a workaround.
- Pre-creating partitions is a recurring ops task that must stay automated.

---

## Prompt used