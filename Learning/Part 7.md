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

- **HTML/CSS/JavaScript inside Spring Boot:** place index.html, styles.css, and app.js under src/main/resources/static. Open http://127.0.0.1:8080/ for the UI; JavaScript calls /applications. One running app. Suggested starting point to keep the Java refresher focused.
- **Thymeleaf:** Spring fills HTML templates with data before sending the page. Templates live under src/main/resources/templates. This may resemble your older projects, but we cannot know their setup without inspecting them.
- **Separate frontend, such as React:** a separate UI project calls the same API. Useful if you want to learn that framework too; adds setup and concepts. It can be adopted later.

Spring Boot supports static files and Thymeleaf templates: [official reference](https://docs.spring.io/spring-boot/reference/web/servlet.html). Stack choice is a proposal, not a decision made for you.

## Making it polished and responsive

Use consistent spacing/type/colours, clear status badges, and readable forms. Use flexible CSS layouts and screen-width rules: multiple columns on desktop, stacked content on phones. Include loading, empty, success, and error states; labelled controls and keyboard support. Test at both narrow and wide widths.

React or Thymeleaf does not automatically make a UI attractive or responsive. That comes from design and CSS. A CSS component library is optional; we can choose one when UI work begins.

## Possible first UI milestone

Application list + Add Application form. Connect them to existing GET/POST endpoints, then add detail/edit/delete controls as backend operations become ready. Frontend timing is not changed by this overview; no need to finish authentication/interviews just to try a small UI.

## I should be able to...

- [ ] Distinguish HTML, CSS, JavaScript, and backend responsibilities.
- [ ] Describe how clicking Save reaches our POST endpoint.
- [ ] Recognise that responsive layout is a CSS/design concern, independent of the backend.

This overview is not a required quiz before returning to Java. Ask for deeper detail only where useful.
