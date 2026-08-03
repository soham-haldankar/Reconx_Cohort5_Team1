/**
 * TICKET-ADV106 — Advanced data table (sortable, resizable, frozen header)
 */

 (function () {
    const table = document.getElementById('trades-table');
    const tbody = document.getElementById('trades-tbody');
    let rows = []; // canonical data array
  
    // ========== Sortable columns ==========
    table.querySelectorAll('thead th').forEach(th => {
      th.addEventListener('click', (e) => {
        if (e.target.classList.contains('resize-handle')) return; // ignore resize clicks
  
        const col = th.dataset.col;
        const type = th.dataset.type || 'string';
        const currentDir = th.getAttribute('aria-sort');
        const dir = currentDir === 'ascending' ? 'descending' : 'ascending';
  
        // Clear all sort indicators
        table.querySelectorAll('thead th').forEach(o => o.removeAttribute('aria-sort'));
  
        // Set new sort
        th.setAttribute('aria-sort', dir);
  
        const mult = dir === 'ascending' ? 1 : -1;
        rows.sort((a, b) => {
          const av = a[col];
          const bv = b[col];
  
          if (type === 'number') {
            return (Number(av) - Number(bv)) * mult;
          }
          return String(av).localeCompare(String(bv)) * mult;
        });
  
        renderRows();
      });
    });
  
    // ========== Resizable columns ==========
    table.querySelectorAll('.resize-handle').forEach(handle => {
      handle.addEventListener('mousedown', (e) => {
        e.preventDefault();
  
        const th = handle.closest('th');
        const startX = e.clientX;
        const startWidth = th.offsetWidth;
  
        function onMove(ev) {
          th.style.width = (startWidth + ev.clientX - startX) + 'px';
        }
  
        function onUp() {
          document.removeEventListener('mousemove', onMove);
          document.removeEventListener('mouseup', onUp);
        }
  
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
      });
    });
  
    // ========== Render rows ==========
    function renderRows() {
      tbody.innerHTML = rows
        .map(
          r => `
            <tr>
              <td>${r.tradeRef || '-'}</td>
              <td>${r.symbol || '-'}</td>
              <td>${Number(r.quantity || 0).toLocaleString('en-US')}</td>
              <td>${Number(r.price || 0).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 4
              })}</td>
              <td>${r.status || '-'}</td>
            </tr>
          `
        )
        .join('');
    }
  
    // ========== Fetch initial data ==========
    fetch('/api/v1/trades?size=200')
      .then(r => r.json())
      .then(data => {
        rows = data.content || data || [];
        renderRows();
      })
      .catch(err => {
        console.error('Failed to load trades:', err);
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; padding: 20px;">Unable to load trades</td></tr>';
      });
  })();