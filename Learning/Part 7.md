# Part 7 — Frontend overview

Planning only: no UI implemented and backend work is paused for this explanation.

## The pieces

| Piece | Job in our tracker |
| --- | --- |
| HTML | Forms, buttons, application list, and detail panel. |
| CSS | Colours, spacing, typography, and layouts that fit desktop and phone screens. |
| JavaScript | Handle clicks, call our API, and update what the page displays. |
| Spring Boot API | Create/read/update/delete records, validate input, and store data. |

Example: click Save → JavaScript sends POST /applications → backend returns the created record → page displays it. The API returns data; HTML/CSS determine its appearance.

## Setup options

- **HTML/CSS/JavaScript inside Spring Boot:** place index.html, styles.css, and app.js under src/main/resources/static. Open http://127.0.0.1:8080/ for the UI; JavaScript calls /applications. One running app. An option that directly uses the JSON API we have built.
- **Thymeleaf:** Spring fills HTML templates with data before sending the page. Templates live under src/main/resources/templates. You confirmed your older projects used Thymeleaf. It still uses HTML/CSS and optional JavaScript.
- **Separate frontend, such as React:** a separate UI project calls the same API. Useful if you want to learn that framework too; adds setup and concepts. It can be adopted later.

Spring Boot supports static files and Thymeleaf templates: [official reference](https://docs.spring.io/spring-boot/reference/web/servlet.html). Stack choice remains a proposal until you choose it.

## Making it polished and responsive

Use consistent spacing/type/colours, clear status badges, and readable forms. Use flexible CSS layouts and screen-width rules: multiple columns on desktop, stacked content on phones. Include loading, empty, success, and error states; labelled controls and keyboard support. Test at both narrow and wide widths.

React or Thymeleaf does not automatically make a UI attractive or responsive. That comes from design and CSS. A CSS component library is optional; we can choose one when UI work begins.

## Possible first UI milestone

Application list + Add Application form. Connect them to existing GET/POST endpoints, then add detail/edit/delete controls as backend operations become ready. Frontend timing is not changed by this overview; no need to finish authentication/interviews just to try a small UI.

## I should be able to...

- [x] Distinguish HTML, CSS, JavaScript, and backend responsibilities.
- [x] Describe how clicking Save reaches our POST endpoint.
- [x] Recognise that responsive layout is a CSS/design concern, independent of the backend.

This overview is not a required quiz before returning to Java. Ask for deeper detail only where useful.

## Thymeleaf versus browser-side rendering

All options use HTML/CSS. The difference is where data is put into the page:

| Option | Showing the application list |
| --- | --- |
| Plain JavaScript + API | Browser calls GET /applications, then JavaScript builds the displayed list. |
| Thymeleaf | A Spring page controller gets applications from our service; Thymeleaf fills the HTML before sending it to the browser. JavaScript is optional for extra interactions. |
| Angular + API | Angular uses HTML/CSS and TypeScript to manage the UI and call the backend API. It adds a frontend framework and tooling. |

**Updated recommendation: Thymeleaf**, given your confirmed familiarity and current goal of practising Java/Spring. It can produce a polished responsive tracker with CSS; JavaScript can add interactions when needed. The earlier plain-JavaScript suggestion was aimed at directly using our existing API.

With Thymeleaf, keep our REST controllers for JSON and add a separate @Controller for HTML pages. Both reuse ApplicationService. Page controllers normally use the service directly rather than making HTTP calls to our own API. No backend rewrite required.

Angular can also pair with Spring Boot: Angular handles the UI; Spring Boot serves JSON through endpoints like ours. We cannot infer whether your past Angular projects used Spring Boot without seeing them. Angular and Thymeleaf are usually alternative main UI approaches; we do not need both here.

Sources: [Spring Thymeleaf example](https://spring.io/guides/gs/serving-web-content/), [Angular HTTP client](https://angular.dev/guide/http). No stack selected, installations, or frontend implementation performed during this explanation.
