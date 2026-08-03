/**
 * TICKET-ADV100 — Theme toggle with localStorage persistence
 * Runs in <head> before stylesheet loads to avoid FOUC (flash of unstyled content)
 */

 (function () {
  // Read saved theme from localStorage (default to light)
  const stored = localStorage.getItem('reconx-theme') || 'light';
  document.documentElement.dataset.theme = stored;

  // Attach click handler once DOM is ready
  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('theme-toggle');
    if (btn) {
      btn.addEventListener('click', () => {
        const current = document.documentElement.dataset.theme;
        const next = current === 'light' ? 'dark' : 'light';
        document.documentElement.dataset.theme = next;
        localStorage.setItem('reconx-theme', next);
      });
    }
  });
})();