## Goal and current setup
Use version control for small, understandable milestones, including when working alone. This is a separate topic from [[Job Apps Tracker/Learning/Part 2 - Modeling a job application|Part 2]], which remains our application-model lesson.

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

| Concept/command        | Why we use it                                                                                                       |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------- |
| Working tree           | The files currently on disk, including uncommitted changes.                                                         |
| `git status`           | Shows the current branch and staged, unstaged, and untracked changes.                                               |
| `git diff`             | Shows changes to tracked files that are not staged yet. New untracked files do not appear here.                     |
| `git add <paths>`      | Selects the current contents of specific files for the next commit. Editing again afterward requires staging again. |
| `git diff --staged`    | Reviews exactly the changes selected for the next commit.                                                           |
| `git commit -m "..."`  | Saves the staged snapshot locally with a message. It does not automatically test or push.                           |
| `git push`             | Uploads local commits to the configured upstream branch.                                                            |
| `git log --oneline -5` | Shows the five latest commits with short IDs and messages.                                                          |

`origin` is just a local nickname for a remote URL. `origin/main` is Git's local record of the fetched remote branch, not a separate server process. `git push -u origin main` established the upstream relationship so plain `git push` works from main. Commit IDs identify snapshots; a branch is a movable name pointing to a commit.

Our `.gitignore` excludes generated `target/`, IDE settings, logs, the starter archive, and local secret configuration. Ignoring a file does not remove it from an already recorded commit; check what is staged before committing. The repository includes source, the Maven Wrapper, run instructions, and snapshots of our learning/progress notes. The Obsidian vault remains authoritative; synchronize those notes deliberately before committing their updates. There is no automatic vault-to-repository sync.

**I should be able to...**
- [x] Distinguish saving, staging, committing, and pushing.
- [x] Explain the roles of Git, GitHub, main, and origin.
- [x] Inspect the staged snapshot before committing.

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
- [x] Explain why a feature branch helps keep main usable.
- [x] Describe how a pull request supports review before merging.
- [x] Explain why a passing local build and a push are separate actions.

## My first Git practice checkpoint
From the project folder, run these read-only commands:

```powershell
git status
git remote -v
git log --oneline -5
```

Then open the repository's commit history in Chrome. Identify the initial project commit and the documentation commit that records this setup. A clean status means Git sees no uncommitted tracked changes or untracked non-ignored files; it does not mean the application is complete or bug-free.

- [x] I can identify my current branch, remote, and latest commit.
- [x] I can explain whether saving a Java file alone makes it appear on GitHub.

Next: review these commands together, then resume DTO learning from Part 2 on a feature branch. No learning checkbox is complete merely because the repository has been created.

## Clarifying main, origin, and the workflow
Keeping main usable is our workflow goal, not something Git guarantees. Git records committed snapshots, compares changes, and supports different lines of development. It can record broken code too. Testing and review are how we try to keep main reliable; uncommitted edits are not automatically recoverable from history.

| Name | What it actually is in this project |
| --- | --- |
| Git | Local version-control software recording snapshots/history in .git. |
| GitHub | The online service hosting our repository and pull requests. |
| main | A local branch name pointing to the latest commit on that line of development. |
| origin | A nickname stored in local Git configuration for https://github.com/aalatraqchi/Job-App-Tracker.git. It is not a folder or branch. |
| main on GitHub | The branch in the remote repository. It may differ from local main until changes are transferred. |
| origin/main | A local remote-tracking reference: our last known state of main at origin, refreshed by fetching and normally updated after successful pushes to it. It is not a live connection to GitHub. |

Think of origin as a named destination, with branches such as main and feature/application-dtos available at that destination. `git push origin main` says which destination and branch to send. It does not mean "put main inside an origin folder."

### What each workflow step does
Using our planned DTO feature as an example (not implementation already completed):

| Step | Action and result |
| --- | --- |
| Branch | Create feature/application-dtos from the current main commit. This starts a separate line of history. A branch is not a second project folder; switching branches changes the files shown in the working folder. |
| Implement | Edit and save the DTO source files. These are working-tree changes, not yet committed snapshots. |
| Test | Run Maven verification and inspect behavior to catch problems before integrating. Tests only check what they cover. |
| Commit | First stage the intended file contents and inspect them, then save a named snapshot to local history on the feature branch. |
| Push | Transfer the feature branch's commits to GitHub. Pushing that branch does not merge it into main. |
| Pull request | Open a GitHub proposal to integrate feature/application-dtos into main, with a description and validation results. It is different from the git pull command. |
| Review | Inspect the proposed diff and checks; ask questions or make corrections. Fixes become further commits on the feature branch and are pushed to update the same PR. A solo author can review their own diff. |
| Merge | Integrate the accepted feature into main. If done on GitHub, update local main afterward by switching to it and pulling. |

Repeat implement/test/commit/push as needed; a feature is not limited to one commit. A PR is not required for Git to work, but we use it to practice review and collaboration.

### The three checklist points in plain terms
1. **Saving, staging, committing, pushing:** saving writes the file to disk; staging selects its current contents for the next snapshot; committing records that snapshot locally; pushing sends commits to GitHub. None automatically implies the next action.
2. **Git/GitHub/main/origin:** tool, online host, branch, and nickname for the remote destination. Main is not automatically a backup of every edit, and origin is not a directory.
3. **Inspecting the staged snapshot:** run `git diff --staged` after `git add` to see what the next commit will change. `git status` lists the selected files; `git diff --staged` shows their actual changes, including staged new files. Plain `git diff` shows tracked edits not staged yet.

Example: if JobApplication.java and README.md are edited but only JobApplication.java is staged, the next commit includes the staged model changes and leaves the README edit uncommitted. If the model is edited again after staging, its newer edits also stay out until it is staged again. Git records the staged version, not whatever happens to be open in IntelliJ at commit time.

These explanations do not mark the capability checkboxes complete. Practice checkpoint: if you save and commit a model change locally but do not push, where can you see that new commit, and has GitHub changed?

## Agreed division of version-control work
The user has learned the concepts and delegates staging, commits, pushes, branches, and PR setup to the assistant. The assistant reports what was staged, committed, and pushed. A practical Git exercise is no longer a prerequisite for application learning; unverified operational checkboxes are optional rather than assumed complete.

When a feature is finished, both user and assistant review the diff and verification results. The user explicitly confirms the merge; only then does the assistant merge. Creating or pushing a feature branch is not merge approval. DTO work now runs on feature/application-dtos; main remains unchanged until an approved merge. Git remains part of the workflow without replacing the Java/Spring learning exercises.
