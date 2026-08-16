# Changelog

All notable changes to Loophole will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2026-08-15

### Added
- Confirmation when adding the Quick Settings tile, reporting whether the tile
  was added or is already present

### Changed
- Turning Developer Options on now switches USB debugging and wireless debugging
  off first, so enabling it no longer restores a debugging session left on from
  last time. This ends any active adb connection.
- Long-pressing the Quick Settings tile now opens Loophole instead of the system
  App Info screen
- The app, tile, and widget now follow Developer Options live, so a change made
  anywhere on the device — system Settings, adb, or another one of Loophole's
  own surfaces — is reflected immediately
- Rebuilt on an MVVM architecture, with the settings layer covered by unit tests

## [1.0.2] - 2026-07-22

### Changed
- Enabled reproducible builds
- Enabled code and resource shrinking, reducing APK size

### Fixed
- Corrected the permission disclosure in the app description: the
  network-related permissions come from the Glance widget framework, not from
  any network activity by Loophole

## [1.0.1] - 2026-07-18

### Fixed
- Removed foojay-resolver Gradle plugin for F-Droid build compatibility

## [1.0.0] - 2026-07-18

### Added
- Quick Settings tile to toggle Android Developer Options in one tap
- In-app switch as a fallback / status view
- Live tile state reflecting current Developer Options status
- MIT license, initial public release

[Unreleased]: https://github.com/shubhang-d/loophole/compare/v1.1.0...HEAD
[1.0.0]: https://github.com/shubhang-d/loophole/releases/tag/v1.0.0
[1.0.1]: https://github.com/shubhang-d/loophole/releases/tag/v1.0.1
[1.0.2]: https://github.com/shubhang-d/loophole/releases/tag/v1.0.2
[1.1.0]: https://github.com/shubhang-d/loophole/releases/tag/v1.1.0
