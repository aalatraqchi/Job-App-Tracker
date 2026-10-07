# Part 3 — Git and GitHub workflow

## Goal and current setup
Use version control for small, understandable milestones, including when working alone. This is a separate topic from [[Job Apps Tracker/Learning/Part 2|Part 2]], which remains our application-model lesson.

Repository: [aalatraqchi/Job-App-Tracker](https://github.com/aalatraqchi/Job-App-Tracker), private.

- Local folder: `E:\Codex\Job App Tracker`.
- Main branch: `main`, tracking `origin/main`.
- Remote name `origin` points to `https://github.com/aalatraqchi/Job-App-Tracker.git`.
- Initial commit: `75faa18`, `feat: establish Spring Boot starter and application model`.
- Initial verification: Maven verify passed, including two HTTP integration tests and JAR packaging. Application model fields were reviewed previously; these endpoint tests do not test model assignments.

The work done before GitHub setup is one initial snapshot. We did not manufacture separate historical commits for earlier exercises. Future work can have its own small commits.

## Git is local; GitHub hosts a remote copy
**Git** records snapshots and history in the local `.git` directory. **GitHub** hosts a repository that we can push to, browse, and use for collaboration. Saving a file in IntelliJ does not create a commit. Creating a local commit does not upload it.

```text
edited files → git add → staged snapshot → git commit → local history
                                                    → git push → GitHub
```

| Concept/command | Why we use it |
| --- | --- |
| Working tree | The files currently on disk, including uncommitted changes. |
| `git status` | Shows the current branch and staged, unstaged, and untracked changes. |
| `git diff` | Shows changes to tracked files that are not staged yet. New untracked files do not appear here. |
| `git add <paths>` | Selects the current contents of specific files for the next commit. Editing again afterward requires staging again. |
| `git diff --staged` | Reviews exactly the changes selected for the next commit. |
| `git commit -m "..."` | Saves the staged snapshot locally with a message. It does not automatically test or push. |
| `git push` | Uploads local commits to the configured upstream branch. |
| `git log --oneline -5` | Shows the five latest commits with short IDs and messages. |

`origin` is just a local nickname for a remote URL. `origin/main` is Git's local record of the fetched remote branch, not a separate server process. `git push -u origin main` established the upstream relationship so plain `git push` works from main. Commit IDs identify snapshots; a branch is a movable name pointing to a commit.

Our `.gitignore` excludes generated `target/`, IDE settings, logs, the starter archive, and local secret configuration. Ignoring a file does not remove it from an already recorded commit; check what is staged before committing. The repository includes source, the Maven Wrapper, run instructions, and snapshots of our learning/progress notes. The Obsidian vault remains authoritative; synchronize those notes deliberately before committing their updates. There is no automatic vault-to-repository sync.

**I should be able to...**
- [ ] Distinguish saving, staging, committing, and pushing.
- [ ] Explain the roles of Git, GitHub, main, and origin.
- [ ] Inspect the staged snapshot before committing.

## Our workflow for the next feature
The initial baseline is on main. For a new feature, we will create a branch, make small changes, test, push, and review a pull request before merging.

The following is the planned workflow for DTO work, **not commands already executed**:

```powershell
git switch main
git pull --ff-only
git switch -c feature/application-dtos

# Implement and review the agreed DTO changes, then:
.\mvnw.cmd verify
git status
git diff
git add src/main/java/com/ashraf/jobapptracker/application
git diff --staged
git commit -m "feat: add application request and response DTOs"
git push -u origin feature/application-dtos
```

Run these from the project folder. Start a new feature from a clean working tree. `pull --ff-only` fetches and advances the current branch only when that can happen without creating a merge commit; if it fails, inspect the history rather than forcing it. File paths and commit scope should match the changes actually made, including any agreed documentation changes.

A **pull request** compares the feature branch with main and gives us a place to review the diff and explain validation. It is not the same as `git pull`. Even solo, that review helps practice the collaboration process. After review and merge on GitHub, switch back to main and pull to receive the merged result locally. We will walk through that when a feature is ready; no DTO branch or pull request exists yet.

Commit one coherent change at a time. A useful message describes the outcome, such as adding DTOs, rather than "updates". Prefixes like `feat:` and `docs:` are our naming convention, not Git requirements. Commit small working increments; push them when ready to save/share remotely. Avoid rewriting published history as part of routine learning work.

**I should be able to...**
- [ ] Explain why a feature branch helps keep main usable.
- [ ] Describe how a pull request supports review before merging.
- [ ] Explain why a passing local build and a push are separate actions.

## My first Git practice checkpoint
From the project folder, run these read-only commands:

```powershell
git status
git remote -v
git log --oneline -5
```

Then open the repository's commit history in Chrome. Identify the initial project commit and the documentation commit that records this setup. A clean status means Git sees no uncommitted tracked changes or untracked non-ignored files; it does not mean the application is complete or bug-free.

- [ ] I can identify my current branch, remote, and latest commit.
- [ ] I can explain whether saving a Java file alone makes it appear on GitHub.

Next: review these commands together, then resume DTO learning from Part 2 on a feature branch. No learning checkbox is complete merely because the repository has been created.
