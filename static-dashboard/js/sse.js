/**
 * TICKET-ADV104 — Server-Sent Events subscription to /api/v1/trades/stream
 * TICKET-ADV105 — SSE handler with prepend-and-animate
 */

 (function () {
  const feed = document.getElementById('trade-feed');
  const statusBadge = document.getElementById('sse-status');
  if (!feed) return;

  const STREAM_URL = '/api/v1/trades/stream';
  let sse = null;

  // Helper: update connection status badge
  function updateStatus(text, variant) {
    if (statusBadge) {
      statusBadge.textContent = text;
      statusBadge.className = 'sse-badge sse-badge--' + variant;
    }
  }

  // Helper: escape HTML to prevent XSS
  function escapeHtml(str) {
    const map = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;'
    };
    return String(str).replace(/[&<>"']/g, c => map[c]);
  }

  // Helper: format quantity with commas
  function formatQty(n) {
    return new Intl.NumberFormat('en-US').format(Number(n));
  }

  // Helper: format price with 2-4 decimals
  function formatPrice(n) {
    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 4
    }).format(Number(n));
  }

  // Prepend a trade card to the feed
  function prependTradeRow(trade) {
    const statusModifier = trade.status ? 'trade-card--' + trade.status.toLowerCase() : '';
    const row = document.createElement('article');
    row.className = 'trade-card ' + statusModifier + ' trade-card--new';
    row.innerHTML = `
      <header class="trade-card__header">
        <strong>${escapeHtml(trade.tradeRef)}</strong>
        <span>[${escapeHtml(trade.status || 'UNKNOWN')}]</span>
      </header>
      <div class="trade-card__body">
        <span>${escapeHtml(trade.symbol || '-')}</span>
        <span>qty=${formatQty(trade.quantity || 0)}</span>
        <span>price=${formatPrice(trade.price || 0)}</span>
      </div>
    `;
    feed.prepend(row);

    // Remove animation class after 500ms
    setTimeout(() => row.classList.remove('trade-card--new'), 500);

    // Cap feed at 50 entries
    while (feed.children.length > 50) {
      feed.lastElementChild.remove();
    }
  }

  // Connect to the SSE endpoint
  function connect() {
    updateStatus('Connecting…', 'connecting');
    sse = new EventSource(STREAM_URL);

    sse.onopen = () => {
      updateStatus('Live', 'live');
    };

    sse.onmessage = (event) => {
      try {
        const trade = JSON.parse(event.data);
        prependTradeRow(trade);
      } catch (err) {
        console.error('Failed to parse trade event:', err);
      }
    };

    sse.onerror = () => {
      updateStatus('Reconnecting…', 'error');
      // Do NOT reconnect here — let the browser auto-reconnect
      // Calling connect() here causes a DDoS
    };
  }

  // Clean up on page unload
  window.addEventListener('beforeunload', () => {
    if (sse) sse.close();
  });

  // Start the connection
  connect();
})();