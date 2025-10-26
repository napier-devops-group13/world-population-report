---
name: "User Story"
description: "Create a new user story aligned with SET09803 assessment (GitFlow, CI/CD, quality gates)."
title: "story: <short capability name>"
labels: ["story", "p1"]
assignees: []
---

## User Story
As a **<role>**, I want **<capability>** so that **<benefit>**.

## Acceptance Criteria (Given / When / Then)
- [ ] **Given** <precondition>, **when** <action>, **then** <observable outcome>.
- [ ] **Given** <precondition>, **when** <action>, **then** <observable outcome>.
- [ ] Validation errors return helpful messages (400/404) where appropriate.

## Definition of Done (applies to every story)
- [ ] Code builds on **JDK 21+** (Maven) locally and in **GitHub Actions**.
- [ ] **Unit + integration tests** added; CI is **green**.
- [ ] **Checkstyle + SpotBugs** pass (no new violations).
- [ ] **JaCoCo** coverage for changed code ≥ **80%** (or project CI threshold).
- [ ] Docs updated (README/API examples); screenshots or `curl` output attached.
- [ ] Branch **feature/<topic>** → PR **into `develop`**, ≥1 review (**CODEOWNERS**).
- [ ] Links: related issues/PRs; milestone and labels set.

## Context / Notes
- Endpoint(s):
- DB tables/queries touched:
- Non-functional: performance, error handling, pagination, security notes.

## Evidence to attach in PR
- CI run screenshot (green).
- Test results (coverage summary).
- Example responses (e.g., `curl` or Postman).
- Docker logs if relevant.

## Assessment mapping
- Labs: L1 (Git/GitFlow), L2 (CI), L3 (Maven/tests), L4 (Quality), L6–L7 (Docker/Compose), L8 (Coverage), L9 (Repo hygiene), L10 (Releases).
- Epic: <EPIC A–J>
- Priority: P1 | P2 | P3
