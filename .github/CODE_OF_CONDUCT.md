# Code of Conduct
**Project:** World Population Reporting System (SET09803 – Group 13)  
**Applies To:** All project spaces and interactions (GitHub repo, Issues/PRs/Discussions, Zube boards, sprints, commits, release notes, meetings, chat, and assessment submissions).  
**Timezone:** Asia/Yangon (MMT, UTC+06:30)

---

## 1) Purpose
We commit to a respectful, inclusive, and productive environment. This Code sets clear expectations for behavior, collaboration, academic integrity, and workflow so our team can deliver high-quality software and meet **CR1**, **CR2**, and **Final Delivery** requirements.

---

## 2) Scope
This Code applies to:
- **All participants**: contributors, maintainers, collaborators, and reviewers.
- **All spaces**: GitHub, Zube, meetings, chats, and classroom presentations.
- **All phases**: setup, CR1, CR2, and Final Delivery (including late changes, hotfixes, and release tagging).

---

## 3) Our Standards

### Expected Behavior
- Be respectful and professional; assume good intent.
- Welcome questions; explain decisions with evidence (commits, CI logs, coverage, etc.).
- Give and accept constructive feedback on code, tests, documentation, and diagrams.
- Follow the agreed **GitFlow**, PR review practices, CI checks, and coding standards.
- Credit sources and teammates; document decisions in Issues/PRs.
- Keep discussions focused on the work, not the person.

### Unacceptable Behavior
- Harassment, discrimination, personal attacks, or trolling.
- Sharing others’ private information without consent.
- Sexualized language or imagery; unwelcome advances.
- Spamming Issues/PRs; derailing threads; “review gaming” (rubber-stamping).
- Bypassing CI, test suites, or reviews; force-pushing to protected branches.
- Plagiarism; submitting uncredited third-party or AI-generated work as original.

---

## 4) Coursework-Specific Rules (Napier SET09803)

### Academic Integrity
- Do **not** submit someone else’s code, diagrams, or text as your own.
- If external code/snippets are used, ensure license compatibility and **explicit attribution** in code comments and README.
- Follow your module’s rules on the use of AI assistance; if used, **declare it** in the README (e.g., “Assistance used for documentation/templates/refactoring suggestions; code reviewed and tested by team.”).

### Evidence for Assessment
- Keep Issues, PRs, release notes, CI runs, coverage reports, and screenshots as evidence.
- Ensure the **master** branch and README contain all required badges and the “evidence table” before each review and final submission.

### Deadlines & Timeboxes
- Respect CR1/CR2 meeting times and internal sprint timeboxes.
- Avoid last-minute large changes without peer review and a rollback plan.

---

## 5) Collaboration & Workflow Standards

### Git Strategy (GitFlow)
- Long-lived branches: `master` (assessed), `develop`.
- Short-lived: `feature/<scope>`, `release/<version>`, `hotfix/<version>`.
- **Never** commit directly to `master`. Use PRs from `release/*` or `hotfix/*`.

### Pull Requests
- PR title uses **Conventional Commits** (e.g., `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).
- PR must link to an Issue (user story/task/bug) and include:
  - What/Why/How (summary), screenshots for UI/docs, and test notes.
  - ✅ Passing CI (build, tests, coverage) and no critical CodeQL/PMD findings.
  - ✅ At least **1 reviewer approval** (or team policy).
- No “self-merge” without explicit team consent on small docs-only changes.

### Issues & User Stories
- Use clear titles, acceptance criteria, and Definition of Done (DoD).
- Link Issues to PRs and sprints (Zube).
- Avoid “catch-all” Issues; split work into reviewable chunks.

### Code Quality & Tests
- Follow style and static analysis (Checkstyle/PMD/CodeQL) with zero **blockers**.
- Keep unit/integration tests meaningful; **do not** disable tests to pass CI.
- Maintain or improve coverage on each PR (as agreed threshold).

### Documentation
- Keep README, API docs, diagrams, and evidence table up to date.
- Update change logs and release notes, linking CI artifacts and Docker images.

---

## 6) Security, Privacy & Secrets
- Do **not** commit secrets (tokens, passwords, `.env`) to the repo.
- Use `.gitignore` and sample files (e.g., `.env.example`).
- Report suspected vulnerabilities privately (see §9 Reporting); do not open public Issues with sensitive details.
- Follow Responsible Disclosure (§12).

---

## 7) Inclusivity & Accessibility
- Use inclusive language; avoid slang/idioms that reduce clarity.
- Prefer clear, simple English; summarize long discussions.
- Provide accessible alternatives (e.g., text with diagrams).

---

## 8) Assessment Milestones: Behavior Expectations

### CR1 (Week 6/7)
- **Governance ready**: Code of Conduct, CONTRIBUTING, Issue templates, GitFlow branches created.
- **Project hygiene**: Issues and sprints created; Zube integrated; use cases and diagram complete.
- **Buildability**: Maven builds a self-contained JAR; Dockerfile works; CI builds on PRs/branches.
- **First release**: Tag and release notes with evidence links (builds, images).

**CR1 Conduct Rules**
- No force-push to `master`; all changes via PRs.
- Keep discussions on-topic and respectful in Issues/PRs/meetings.
- Document all decisions (why we chose X over Y).

### CR2 (Week 12/13)
- **Quality**: Strong unit/integration tests; CI runs tests; coverage visible; CodeQL enabled.
- **Deployment**: Container build and run validated.
- **Badges**: Build (master/develop), coverage (master), latest release, license.

**CR2 Conduct Rules**
- Do not “silence” failing tests; fix root causes.
- No bypassing review to “rush” features.
- Provide traceable evidence for each met requirement.

### Final Delivery
- **Master is authoritative**: Ensure master contains the assessable version.
- **Release Management**: Tag final version; freeze master except for critical hotfixes via `hotfix/*`.
- **Evidence Table**: Completed and accurate, with screenshots and links.

**Final Delivery Conduct Rules**
- Respect the freeze window; avoid risky merges.
- Any urgent hotfix must include: Issue, PR review, passing CI, and release notes.

---

## 9) Reporting Incidents & Support
- **Report to:** `<team email or private Slack/Teams channel>` and CC `<module leader/tutor if required by course rules>`.
- **What to include:** Who/what/where/when; links (Issues/PRs/commits/CI); screenshots or logs; desired outcome.
- **Confidentiality:** We keep reports as confidential as feasible; details shared on a need-to-know basis.
- **Anti-retaliation:** Retaliation against anyone reporting in good faith is prohibited.

---

## 10) Enforcement Process & Consequences
**Maintainers/Moderators:** `<maintainer names or roles>`.

**Process (typical flow)**
1. **Community note** (public reminder) for minor issues.
2. **Private warning** with improvement steps and timeline.
3. **Temporary restrictions** (e.g., blocked from merging, assigned reviewer required).
4. **Removal or ban** from project spaces for repeated or severe violations.
5. **Escalation** to module leader if academic integrity or safety is implicated.

**Target timelines (best effort)**
- **Acknowledge** report within **24 hours** (MMT).
- **Initial action plan** within **48 hours**.
- **Resolution** target within **7 days**, or provide status updates.

**Appeals**
- Send appeal to `<appeals contact>` within 7 days of decision; include new info or rationale.

---

## 11) Release & Freeze Policy (Operational Safety)
- **Code freeze** applies ahead of Final Delivery; only critical fixes through `hotfix/*`.
- Each release/hotfix PR must include:
  - Linked Issue, passing CI, updated version, changelog, and release notes.
- No last-minute feature merges that reduce stability or evidence quality.

---

## 12) Responsible Disclosure (Security)
- Email `<security contact>` privately for vulnerabilities.
- Provide steps to reproduce and impact.
- Do not create public Issues until a fix/mitigation is available.

---

## 13) Changes to This Code
- Propose updates via PR to `CODE_OF_CONDUCT.md`.
- Require at least **2 approvals** during CR windows; document rationale in PR.

---

## 14) Attribution
This Code is adapted from community best practices, including the spirit of the Contributor Covenant (v2.1), and tailored to Napier SET09803 project workflows.

---

## 15) Contacts (Fill Before Committing)
- **Primary contact:** `<name> — <email>`
- **Backup contact:** `<name> — <email>`
- **Security contact:** `<name> — <email>`
- **Escalation (module leader/tutor):** `<name> — <email>`

---

## 16) Change History
- **v1.0.0 — <YYYY-MM-DD>**: Initial version for CR1.
- **v1.1.0 — <YYYY-MM-DD>**: Updates for CR2 (testing/coverage/enforcement details).
- **v1.2.0 — <YYYY-MM-DD>**: Final Delivery freeze/hotfix policy.
