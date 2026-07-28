-- ============================================================================
-- TICKET-ADV011
-- Recursive CTE: Trade Lifecycle Rollup
-- ============================================================================

WITH RECURSIVE trade_lifecycle AS (

    -------------------------------------------------------------------------
    -- Base Case (Stage 1)
    -------------------------------------------------------------------------
    SELECT
        t.trade_id,
        t.trade_ref,
        1 AS stage,
        'EXECUTION' AS stage_name,
        t.created_at AS event_at,
        t.status AS event_status

    FROM trades t

    UNION ALL

    -------------------------------------------------------------------------
    -- Recursive Step
    -------------------------------------------------------------------------
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,

        next_stage.stage_name,
        next_stage.event_at,
        next_stage.event_status

    FROM trade_lifecycle tl

    JOIN LATERAL (

        ---------------------------------------------------------------------
        -- Stage 2
        ---------------------------------------------------------------------
        SELECT
            'CONFIRMATION' AS stage_name,
            c.confirmed_at AS event_at,
            c.status AS event_status
        FROM confirmations c
        WHERE tl.stage = 1
          AND c.trade_id = tl.trade_id

        UNION ALL

        ---------------------------------------------------------------------
        -- Stage 3
        ---------------------------------------------------------------------
        SELECT
            'SETTLEMENT',
            s.settlement_date,
            s.status
        FROM settlements s
        WHERE tl.stage = 2
          AND s.trade_id = tl.trade_id

        UNION ALL

        ---------------------------------------------------------------------
        -- Stage 4
        ---------------------------------------------------------------------
        SELECT
            'RECON_BREAK',
            rb.created_at,
            rb.status
        FROM recon_breaks rb
        WHERE tl.stage = 3
          AND rb.trade_id = tl.trade_id

        UNION ALL

        ---------------------------------------------------------------------
        -- Stage 5
        ---------------------------------------------------------------------
        SELECT
            'RESOLUTION',
            rb.resolved_at,
            rb.resolution_status
        FROM recon_breaks rb
        WHERE tl.stage = 4
          AND rb.trade_id = tl.trade_id

    ) AS next_stage ON TRUE

    -------------------------------------------------------------------------
    -- Termination Guard
    -------------------------------------------------------------------------
    WHERE tl.stage < 5

)

SELECT
    trade_id,
    stage,
    stage_name,
    event_at,
    event_status

FROM trade_lifecycle

ORDER BY
    trade_id,
    stage;