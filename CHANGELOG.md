# Changelog

## 1.27 - 2025-02-13

- Fixes a bug in the task dependencies, that might cause an issue
  with an error saying that the `recorder` executable was not found.

## 1.26.4 - 2025-02-11

- No functional changes, but we updated the POM files to point back to
  the repository so that it can work with Renovate

## 1.26.2 - 2025-02-11

- No changes, only testing deployment

## 1.26 - 2025-02-07

### Changed
- Added SCM information to POM file, so that Renovate can handle it better
- Added this CHANGELOG.md file, also for Renovate

## 1.24 - 2025-02-06

### Changed

- Avoids one network request to fetch a shell script during every invocation
