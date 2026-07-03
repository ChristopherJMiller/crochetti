---
name: triage-ci
description: Diagnose a failed GitHub Actions run for Crochetti (CI, release, or promote) — pull the logs, find the failing step, and explain the cause and fix. Use when a build/release/promotion fails or the user asks why a workflow is red.
---

# Triage a failed workflow run

Crochetti has three workflows: `ci.yml` (lint/test/build on PRs + main),
`release.yml` (tag -> signed AAB -> Play internal), and `promote.yml`
(internal -> production). Diagnose failures across all three.

## 1. Find the failing run

- If the user named a workflow, target it; otherwise list recent failures:
  `gh run list --status failure --limit 5`
- Get the failing run's details and the failing job:
  `gh run view <run-id>` then `gh run view <run-id> --log-failed`
  (`--log-failed` prints only the failed steps' logs — start there).

## 2. Diagnose

Map the failure to a cause. Common ones for this repo:

- **`release.yml` build failure** — usually a signing-secret problem
  (`KEYSTORE_BASE64` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`) or a
  compile/test break that CI would also show. Check the secrets exist:
  `gh secret list`.
- **Tag rejected by "Validate tag format"** — the tag isn't bare CalVer
  `YYYY.MM.PATCH` (a legacy `v` prefix is the classic offender). The fix is a
  correctly-formatted tag; delete the bad one (`git tag -d`, `git push --delete origin`).
- **Play upload failure** — often a duplicate `versionCode` (that code already
  exists on Play), a bad/expired `PLAY_STORE_JSON_KEY`, or the service account
  lacking permission on the track. Duplicate versionCode means the tag's derived
  code was already shipped — cut a new patch instead.
- **`promote.yml` failure** — commonly the `versionCode` isn't present on the
  internal track yet (promote a version that actually shipped), a fastlane/Play
  auth issue, or the rollout/status combination (rollout `1.0` must be `completed`,
  which the Fastfile already handles).
- **`ci.yml` test failure** — a real regression; read the Robolectric output.
  ktlint is `continue-on-error`, so lint alone won't fail the run.

Read the actual log lines — don't guess. Quote the specific error to the user.

## 3. Report and fix

Explain the root cause in plain terms, then propose the concrete fix. If it's a
code/config change in the repo, offer to make it. If it's a secret or a Play
Console/permissions issue (things you can't touch), give the user the exact steps.
Offer to re-run once fixed: `gh run rerun <run-id>` (or `--failed` for just the
failed jobs).
