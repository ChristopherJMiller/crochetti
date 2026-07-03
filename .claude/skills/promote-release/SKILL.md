---
name: promote-release
description: Promote a Crochetti release from the Play internal track to production with a staged rollout. Use when the user wants to release to production, roll out to users, or bump a staged rollout percentage.
---

# Promote a release to production

Production promotion runs `.github/workflows/promote.yml`, which uses fastlane to
reassign an already-reviewed internal-track bundle to the production track at a
staged rollout fraction. The workflow is gated by the `production` GitHub
Environment, so a human approval is still required after you trigger it.

## 1. Decide what and how much

- Confirm which version to promote. List recent releases:
  `git tag -l '[0-9]*.[0-9]*.[0-9]*' | sort -V | tail -5`
  and check what actually reached internal: `gh run list --workflow release.yml --limit 5`.
- Recommend a **conservative first rollout** (e.g. `0.2`) unless the user asks for
  full release. For a version already in staged rollout, recommend the next step up
  (e.g. 0.2 -> 0.5 -> 1.0). `1.0` completes the rollout to everyone.
- Before recommending a bump, ask the user to confirm the internal build looks
  healthy (no crashes/regressions they've noticed). You cannot read Play Vitals from
  here — that judgment is theirs.

## 2. Trigger the promotion

Confirm the version + rollout with the user, then:

```
gh workflow run promote.yml -f version=<version> -f rollout=<fraction>
```

Then tell the user to **approve the run in the `production` environment** (GitHub
will show a pending approval on the run). Watch it:
`gh run list --workflow promote.yml --limit 1`.

## 3. Report

Report the promoted version, the rollout fraction now live, and the next suggested
step (e.g. "currently at 20% — bump to 50% once Vitals look clean, then 100%"). If
the workflow fails, hand off to the `triage-ci` skill.

## Notes

- If the `production` environment or its required reviewers aren't set up yet, the
  run won't gate — tell the user to configure required reviewers in
  Settings -> Environments -> production.
- To **halt** a bad rollout, that's currently a Play Console action (Release
  overview -> halt rollout) — surface this to the user; the API/fastlane halt path
  isn't wired into a workflow yet.
