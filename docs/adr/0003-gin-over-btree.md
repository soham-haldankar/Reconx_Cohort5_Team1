
# ADR-0003 — Use a GIN index (jsonb_path_ops) over a B-tree for `instruments.metadata`

- Status: Accepted
- Date: 2026-07-27
- Deciders: ReconX team

## Context

Following ADR-0002, `instruments.metadata` is JSONB. Analysts filter
instruments by containment queries on this column (e.g. "instruments where
metadata contains sector = Financials"), across roughly 50,000 instruments,
interactively from the recon UI. A B-tree index on a JSONB column can only
support equality on the whole document or expression indexes on specific
extracted keys, requiring a separate index per key analysts might query.

## Decision

Create a single GIN index on `instruments.metadata` using the
`jsonb_path_ops` operator class, rather than per-key B-tree expression
indexes or a plain (non-`jsonb_path_ops`) GIN index.

## Consequences

**Positive**
- One index supports containment queries (`@>`) across arbitrary metadata
  keys, without adding a new index each time a new attribute needs to be
  queried.
- `jsonb_path_ops` produces a smaller, faster index than the default GIN
  operator class for containment-style lookups, at the cost of not
  supporting key-existence (`?`) queries.
- Avoids proliferation of narrow B-tree expression indexes tied to specific
  keys, which would need maintenance as new attributes are introduced.

**Negative**
- `jsonb_path_ops` cannot accelerate `?`, `?|`, `?&` (key-existence)
  operators — only `@>` containment; any future need for existence checks
  requires an additional index.
- GIN indexes are more expensive to update than B-tree on write-heavy
  columns; acceptable here since `metadata` changes far less often than
  `trades` inserts.

---

## Prompt used