# Changelog

## 1.29.4 - 2025-06-10

### Changed

- Fixed crash in previous version when used with Compose Preview
  Screenshot Tests.


## 1.29.3 - 2025-06-10

### Changed

- Fixed support for Compose Preview Screenshot Tests,
  0.0.1-alpha10. alpha10 changed a directory location which would
  cause this plugin to crash.

## 1.29.2 - 2025-06-10

### Changed

- No-op. Accidental change, see 1.29.3 for the actual change.

## 1.29.0 - 2025-04-07

### Changed

- In a previous version, we moved the updateCommitGraph step to the
  root gradle module, intended to be an optimization. This caused a
  bunch of bugs, including not respecting api_hostname in certain
  situations. We undid this change, to simplify the logic and make the
  gradle plugin more robust.

## 1.28.2 - 2025-02-28

### Changed

- 1.28 had a bug that caused the task to fail on the CI step, this fixes it.

## 1.28 - 2025-02-27

### Changed

- Added a repoUrl configuration for the plugin, also updated the plugin to
  parse `--repo-url` from extraArgs for now. But don't rely on this in the
  future.
- Cleaned up this change log to more closely match Common Changelog. Hopefully
  that means that it will be parsed by Renovate correctly.

## 1.27 - 2025-02-13

### Changed

- Fixes a bug in the task dependencies, that might cause an issue
  with an error saying that the `recorder` executable was not found.

## 1.26.4 - 2025-02-11

### Changed

- No functional changes, but we updated the POM files to point back to
  the repository so that it can work with Renovate

## 1.26.2 - 2025-02-11

### Changed

- No changes, only testing deployment

## 1.26 - 2025-02-07

### Changed
- Added SCM information to POM file, so that Renovate can handle it better
- Added this CHANGELOG.md file, also for Renovate

## 1.24 - 2025-02-06

### Changed

- Avoids one network request to fetch a shell script during every invocation
