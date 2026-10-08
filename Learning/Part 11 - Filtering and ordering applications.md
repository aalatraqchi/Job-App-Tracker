## Files created

| File | What changed |
| --- | --- |
| `src/test/java/com/ashraf/jobapptracker/application/ApplicationFilterTests.java` | Four HTTP tests cover API filters/order/empty matches, invalid status JSON 400, retained page controls/results/reset links, and invalid status HTML 400. |

## Files edited

Java paths below are under `src/main/java/com/ashraf/jobapptracker/`; templates/CSS are under `src/main/resources/`.

| File | What changed |
| --- | --- |
| `application/ApplicationService.java` | Added `findAll(status, company)`. Status is optional; company matching trims the search and ignores case. Both supplied filters must match. Sorts by applied date descending, then ID descending; missing dates go last. Existing `findAll()` delegates with no filters. |
| `application/ApplicationController.java` | `listApps()` accepts optional `status` and `company` query parameters and calls the shared filter method. Example: `/applications?status=INTERVIEW&company=acme`. |
| `common/ApiExceptionHandler.java` | Added a status-specific JSON 400 message to `invalidParameter()`. |
| `web/TrackerPageController.java` | `list()` accepts the same filters, retains input values, and provides filtered results while summary cards count all applications. Invalid status uses the HTML 400 page. |
| `templates/tracker/list.html` | Added GET search/status controls, Apply/Clear buttons, result count, and a no-match state. |
| `templates/tracker/bad-request.html` | Accepts an error title/message for invalid filters while retaining invalid-ID behavior. |
| `static/css/tracker.css` | Added responsive filter layout; controls stack on phones. |
| `src/test/java/com/ashraf/jobapptracker/application/ApplicationServiceTests.java` | Added two tests for combined/case-insensitive/partial filters, blank/no matches, stable ordering, and unchanged storage. |
| `README.md`, `Project Outline.md`, `AGENTS.md` | Recorded PR #3's approved merge and completed step 5. All 41 tests and packaging pass; Chrome combined/no-match/reset and phone checks pass. Preview server stopped. |

## New content

| Use | Purpose |
| --- | --- |
| `stream().filter(...)` | Keep records satisfying a condition. Consecutive filters require both conditions to pass (AND). |
| `Locale.ROOT` | Normalize company text without relying on the computer's language settings. |
| `String.contains(...)` | Match part of a company name rather than requiring the entire name. |
| `Comparator.comparing(...).thenComparing(...)` | Sort by applied date first and use ID to break ties predictably. |
| `Comparator.reverseOrder()` / `.reversed()` | Put newer dates and larger IDs first. |
| `Comparator.nullsLast(...)` | Place any model with a missing date after dated records. API/form validation still requires dates. |
| `.sorted(order).toList()` | Return filtered records in the chosen order as an unmodifiable list. |
| `<form method="get">` | Put search selections in the URL so filtering can be refreshed or bookmarked without changing stored data. |
| `th:selected` | Retain the selected status option after the filter request. |
