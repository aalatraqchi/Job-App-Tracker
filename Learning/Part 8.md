# Part 8 — Thymeleaf pages

The tracker now has a list, an add form, and a detail page. Both the pages and JSON API use the same `ApplicationService`, so an application created through either appears in both.

## Returning a page
Use `@Controller` when a method should return an HTML page. In `TrackerPageController`, `return "tracker/list"` selects `src/main/resources/templates/tracker/list.html`.

Keep `@RestController` for methods that return JSON, such as the existing `/applications` endpoints.

Use `model.addAttribute("applications", applications)` to give a template data. In the template:
- `th:each="job : ${applications}"` repeats a card for each application.
- `th:text="${job.company}"` displays its company safely as text.
- `th:href` builds a link; `th:if` shows something only when a condition is true.

## Receiving a form
`ApplicationForm` contains the fields on the add page. Use `th:object="${form}"` and `th:field="*{company}"` to connect an input to its field.

The POST method uses `@ModelAttribute("form") ApplicationForm form` to receive submitted form values. Use this for an HTML form; use `@RequestBody` for the JSON requests covered earlier.

`@Valid` checks the form's rules, such as `@NotBlank`. `BindingResult` holds errors so the method can show the same form with messages. These checks currently apply to the page form; REST validation is still a later task.

After saving, `return "redirect:/tracker/" + application.getId()` opens the detail page. Refreshing that page therefore does not submit the form again.

## Files and running
- Java page routes/form: `src/main/java/com/ashraf/jobapptracker/web/`.
- HTML: `src/main/resources/templates/tracker/`.
- Shared styling: `src/main/resources/static/css/tracker.css`.
- `/` or `/tracker`: list. `/tracker/new`: form. `/tracker/{id}`: detail.

Run `JobAppTrackerApplication` in IntelliJ, or ` .\mvnw.cmd spring-boot:run` in the project terminal. Open `http://localhost:8080/`. Stop with IntelliJ's stop button or Ctrl+C in the terminal that started it. Applications disappear when the server restarts because storage is still in memory.

## I should be able to...
- [x] Choose `@Controller` for pages and `@RestController` for JSON.
- [x] Find the template selected by a controller's return value.
- [x] Explain how a model attribute makes data available to HTML.
- [x] Identify where to change a form field or the page's styling.

Implementation and browser checks are complete; these understanding checks are for your next review.
