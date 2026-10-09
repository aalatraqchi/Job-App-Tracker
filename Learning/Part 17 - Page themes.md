## Files created

| File | What changed |
| --- | --- |
| `static/js/theme.js` | Restores a valid saved theme before the page paints, then connects the selector to instant changes and browser storage. Falls back to Dark when storage is unavailable or invalid. |

## Files edited

| File | What changed |
| --- | --- |
| `templates/tracker/fragments.html` | Loads the script and adds a labeled selector to the shared header on every tracker page. |
| `static/css/tracker.css` | Defines Dark, Light, Ocean, Forest, and Sunset palettes; uses their colors for cards, forms, badges, notices, buttons, and focus outlines. Wraps the header on small screens. |
| `README.md`, `Project Outline.md`, `AGENTS.md` | Documents theme use and records this milestone's progress. |

## New content

| Use | Purpose |
| --- | --- |
| CSS custom properties, such as `var(--surface)` | Give a color a shared name so switching palettes updates every component that uses it. |
| `data-theme` on the root HTML element | Lets CSS select the active palette without changing each component. |
| `localStorage` | Remembers the theme in this browser for the same address/port, independently of PostgreSQL. |
| `DOMContentLoaded` and a `change` listener | Connect the selector after it exists and apply a theme when its selection changes. |
