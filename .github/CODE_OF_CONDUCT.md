# Code of Conduct
**World Population Report (SET09803 – Group 13)**

> This document defines how we work together as a professional, respectful, and academically honest team.  
> It applies to all project spaces and tools (IntelliJ, Git/GitHub, Issues/PRs, Discussions, meetings, lab sessions, chat, email).

---

## 1) Our values

- **Respect & inclusion** – We welcome everyone and their ideas. No harassment, discrimination, or belittling.
- **Integrity** – We follow university academic rules. We write our own code, cite sources, and disclose assistance.
- **Quality** – We commit to readable code, automated checks, tests, and helpful reviews.
- **Reliability** – We communicate early, meet deadlines, and unblock teammates.
- **Learning mindset** – We give constructive feedback, document decisions, and help each other grow.

---

## 2) Expected behaviour

- Be courteous, patient, and professional in all communications.
- Use clear, constructive language in reviews and comments; challenge ideas, not people.
- Ask for help early; offer help when you can.
- Honour meeting times and task deadlines; signal risk early in the chat/issue tracker.
- Keep discussions in public project spaces (Issues/PRs) so the team and markers can follow decisions.
- Maintain a safe environment: no offensive jokes or imagery; no intimidation or stalking; no doxxing.
- Protect credentials & personal data; never commit secrets. Use `.env` **locally** and track only `.env.example`.

---

## 3) Unacceptable behaviour

- Harassment, discrimination, or personal attacks of any kind.
- Disruptive or hostile behaviour, trolling, spamming, or derailing threads.
- Plagiarism or breaching academic integrity (see §4).
- Sharing assessment solutions from other students/years or using prohibited materials.
- Committing secrets or private data; circumventing security checks.
- Bypassing code review / CI to merge knowingly broken code close to deadlines.

---

## 4) Academic integrity & use of AI tools

We strictly follow university academic regulations.

- **Your work must be your own.** You may discuss ideas, but code you submit must be authored by the team.
- **Cite external sources** (blogs, docs, StackOverflow, examples) in the PR description and/or comments.
- **AI assistants (e.g., ChatGPT, Copilot, Gemini)**
  - May be used for brainstorming, clarifying concepts, refactoring suggestions, or boilerplate.
  - **Must be disclosed** in the PR description (e.g., “Used AI to draft the README section; manually verified and edited”).
  - **Must be verified** by you; never paste outputs blindly. You are responsible for correctness, licensing, and security.
- If in doubt, **ask the module tutor** before submitting.

Academic misconduct may lead to removal of contribution marks and university penalties.

---

## 5) Collaboration workflow (Assessment GitFlow)

This repo uses **GitFlow** as required in the coursework. We keep work isolated in feature branches,
integrate on **develop**, and publish tagged releases from **master**.

- **master** → production-ready code only. Each release is **tagged** (`vX.Y.Z`). Protected.
- **develop** → integration branch; the default target for PRs. CI must be green.
- **feature/*** → short-lived branches from `develop` for new work  
  (e.g., `feature/compose-worlddb`).
- **release/*** → stabilization branch created from `develop` to prepare a version  
  Bug fixes and docs only; no new features.


**Feature flow**
1. Create your branch from develop
   `git checkout -b feature/<topic> develop`
2. Commit locally; push and open a PR **into `develop`**.
3. Requirements to merge: CI green + at least one review approval.
4. Squash/merge the PR into `develop`, then delete the feature branch "optional".

**Release flow**
1. Cut a release branch from `develop` and bump version/CHANGELOG:
2. On the release branch accept only fixes and documentation. CI must stay green.
3. Publish the release.
4. Bring version bumps back to integration and clean up.

---

## 6) Quality & security gates

- **Code style & static analysis:** Checkstyle/SpotBugs must be clean; fix warnings you introduce.
- **Tests:** Provide/maintain unit tests; do not reduce overall coverage without a reason.
- **CI/CD:** GitHub Actions must pass. If the pipeline fails on your change, it’s your job to fix or revert quickly.
- **Secrets:** `.env` is **local only** and **git-ignored**. Keep **`.env.example`** up-to-date with safe placeholders.
- **Dependencies:** Avoid vulnerable or unlicensed libraries; document changes in PRs.

---

## 7) Communication & meetings

- Primary channels: GitHub Issues/PRs (official record), plus our team chat for quick coordination.
- Post **daily progress** or blockers on the board/issue.
- Meeting etiquette: arrive on time, prepared, and with updates. Publish short minutes and decisions.
- Time-zones or accessibility needs: tell the team so we can schedule fairly.

---

## 8) Decision making & conflict resolution

- Prefer consensus. If stuck, the **maintainer for the area** proposes a decision referencing requirements, test data, and risks.
- If you disagree, propose an alternative with evidence. Keep it technical and respectful.
- Persistent conflict → escalate to the **module tutor** after summarising prior attempts to resolve.

---

## 9) Reporting & enforcement

If you experience or witness a conduct or academic-integrity issue:

- **Report privately** to a maintainer:
  - _Primary:_ **Team Lead / Repo Maintainer** — `<Khant Soe Thwin>`
  - _Secondary:_ **Module Tutor** — `<k.sim@napier.ac.uk>` (or official channel as instructed)
- Or open a **confidential issue** with label `conduct` (maintainers will lock the thread to protect privacy).

We will:

1. Acknowledge within **2 working days**.
2. Review impartially; gather context from all parties.
3. Decide on actions and follow up in writing.

**Possible actions** (graduated):

- Friendly reminder of this Code; request for change.
- Written warning in the project spaces; removal of offensive content.
- Temporary ban from repo interactions; removal from the team.
- Escalation to course staff for academic or disciplinary action.

We will respect confidentiality to the extent allowed by university policy and safeguarding obligations.

> **Emergency**: If anyone feels unsafe, prioritise immediate help using university channels before contacting maintainers.

---

## 10) Maintainers & roles (fill in)

- **Team Lead / Release Manager:** `<Khant Soe Thwin>`
- **CI/CD Maintainer:** `<Khant Soe Thwin>`
- **Database & Compose Owner:** `<Khant Soe Thwin>`
- **Documentation Owner:** `<Khant Soe Thwin>`
- **Module Tutor:** `<k.sim@napier.ac.uk>`

All maintainers are responsible for fair enforcement of this Code.

---

## 11) Acknowledgements & license

This Code of Conduct is adapted from widely used community standards (e.g., Contributor Covenant v2.1) and tailored to SET09803 coursework and our Java/Maven/Docker/GitHub workflow.

---

## 12) Versioning & changes

- Current version: **1.0.0** (2025-10-25)
- Changes to this Code require a PR, team review, and tutor awareness if required by the coursework brief.

---

### Quick PR checklist (paste into every PR)

- [ ] Linked Issue and clear scope
- [ ] Conventional Commit title
- [ ] Updated docs/README where relevant
- [ ] Tests added/updated; CI green
- [ ] No secrets committed; `.env.example` updated if config changed
- [ ] Reviewed by at least one teammate

---

_By contributing to this repository, you agree to abide by this Code of Conduct._
