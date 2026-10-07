# Learning workflow

This project is both a portfolio project and a way for Ashraf to relearn Java/Spring Boot. Optimize for understanding and working milestones, not completion speed.

- Implement only what is necessary for the current agreed feature.
- Explain important Java, Spring, database, and architectural concepts in separate Obsidian notes in the Learning folder, using actual project code and explaining why the design exists.
- Create a new numbered note (Part 2.md, Part 3.md, etc.) when a new learning topic or milestone begins; do not create a part for every chat message or routine clarification. Keep each part concise and organized by concept. Avoid trivial syntax and repetitive boilerplate. Include short "I should be able to..." checklists for major concepts.
- When a task follows from concepts already covered, ask the user to attempt it. Review their attempt and give hints before supplying or replacing it with a complete solution.
- Do not advance the implementation roadmap during a requested learning session. Let the user complete/review the learning exercise before proceeding, unless they explicitly request otherwise.
- Update the vault's Changes.md under the appropriate date and keep Project Outline.md progress accurate. Reading a lesson does not mean the user's understanding or exercise is verified.
- Source workspace: E:\Codex\Job App Tracker.
- Learning folder: C:\Users\Ashraf\Documents\ObsidianVaults\Roblox Game Tracking\Job Apps Tracker\Learning.
- Existing starter lesson: Learning\Part 1.md. Preserve it as the completed starter lesson; add relevant corrections there if needed. Current lesson: Learning\Part 2.md, application modeling; extend it for related step 3 concepts rather than creating a note for each clarification.
- Outline and Changes notes are in the parent Job Apps Tracker vault folder. Link to lessons with Obsidian paths such as [[Job Apps Tracker/Learning/Part 1|Part 1]]. Preserve existing content when updating notes; the vault notes are the authoritative progress records.

Starter learning checkpoint complete: user completed the capability checklists, explained request mapping and random test ports, implemented /about and its separate integration test, and reported two passing tests on October 6, 2026. Attempts were reviewed without replacing the user's code. Step 3 is now in progress: a minimal JobApplication model with company field, constructor, and getter is implemented as the Part 2 worked example. Existing two endpoint tests pass. Position exercise complete and reviewed: user added the private field, updated constructor assignments, and added getPosition() correctly; user reported tests passed. Part 2 now explains LocalDate and status enums. ApplicationStatus.java is implemented as a worked example and compiles. Date/status exercise complete and reviewed: user correctly added LocalDate appliedDate and ApplicationStatus status, constructor assignments, and getters. The parameter date correctly initializes appliedDate. User explained the benefit of typed values over arbitrary strings. Next: finish remaining model fields, then introduce DTOs. Remaining model fields, DTOs, storage, and application endpoints are pending; step 3 remains incomplete. Preserve the user implementation and continue the learning workflow.




Text-field exercise complete and reviewed: all six user-supplied model fields have correct constructor assignments and getters.


Text-field corrections reviewed: jobUrl and notes are private and getJobUrl is correct. The constructor parameter url is valid. Next checkpoint is request/response DTOs; introduce concepts with actual code and assign familiar patterns to the user. DTOs, ID, service, and application endpoints remain unimplemented.
