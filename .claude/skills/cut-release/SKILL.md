---
name: cut-release
description: Cut a new Crochetti release — pick the next CalVer version, draft the Play Store "what's new" notes from merged work, then tag and push to trigger the release workflow. Use when the user wants to ship a release, cut a version, or publish an update.
---

# Cut a release

Crochetti releases are driven by a git tag in CalVer `YYYY.MM.PATCH` format on
`main`. Pushing the tag triggers `.github/workflows/release.yml`, which builds a
signed AAB and uploads it to the Play **internal** track (production promotion is a
separate, manually-gated step — see the `promote-release` skill).

Work through these steps. Confirm the version and notes with the user before tagging.

## 1. Pre-flight

- Ensure you're on `main` with a clean tree and up to date:
  `git switch main && git pull --ff-only`
- Confirm CI is green for the latest commit: `gh run list --workflow ci.yml --branch main --limit 1`.
  If CI is failing, stop and surface it — don't ship a broken build.

## 2. Compute the next version

- Find the latest release tag: `git tag -l '[0-9]*.[0-9]*.[0-9]*' | sort -V | tail -1`
  (ignore any legacy `v`-prefixed tags — the format is bare `YYYY.MM.PATCH`).
- Use today's year and month. Then:
  - If the latest tag is already `YEAR.MONTH.*`, increment its PATCH by 1.
  - Otherwise (new month/year), start PATCH at 0.
- Sanity-check the derived `versionCode` is strictly increasing vs. the last release.
  The release workflow computes it as `YYYY` + zero-padded `MM` + `PATCH`
  (e.g. `2025.12.4` -> `2025124`). Monotonicity is required by Play.

## 3. Draft the "what's new" notes

- Gather what changed since the last tag:
  `git log <last-tag>..HEAD --no-merges --pretty='- %s'`
  and, if useful, `gh pr list --state merged --base main --limit 30`.
- Write **user-facing** release notes — what a crocheter notices, not commit
  internals. Plain language, no ticket IDs or package names. Keep it under
  **500 characters** (Play's limit). One short intro line plus a few bullets is ideal.
- Show the proposed version **and** the drafted notes to the user and get approval.
  Revise until they're happy.

## 4. Ship it

Once approved:

1. Overwrite the Play notes file with the approved text:
   write to `distribution/whatsnew/whatsnew-en-US`.
2. Commit it: `git add distribution/whatsnew/whatsnew-en-US && git commit -m "Release notes for <version>"`
3. Push the commit: `git push origin main`
4. Tag and push the tag (this triggers the release):
   `git tag <version> && git push origin <version>`
5. Watch it: `gh run watch --workflow release.yml` (or `gh run list --workflow release.yml --limit 1`).
   If it fails, hand off to the `triage-ci` skill.

Report the released version, the internal-track upload status, and remind the user
that production promotion is done via the `promote-release` skill once they've
validated the internal build.
