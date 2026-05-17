<div align="center">

  <img src="https://github.com/tapframe/RovoTV/blob/main/assets/brand/app_logo_wordmark.png" alt="Rovo" width="300" />
  <br />
  <br />

  [![Contributors][contributors-shield]][contributors-url]
  [![Forks][forks-shield]][forks-url]
  [![Stargazers][stars-shield]][stars-url]
  [![Issues][issues-shield]][issues-url]
  [![License][license-shield]][license-url]

  <p>
    A modern media hub for Android and iOS built with Kotlin Multiplatform and Compose Multiplatform.
    <br />
    Stremio addon ecosystem • Cross-platform
  </p>

</div>

## About

Rovo is the current Kotlin Multiplatform rewrite of the original React Native app. It delivers a shared Compose UI for Android and iOS while keeping the playback-focused experience, collection tools, watch progress flows, downloads, and Stremio addon ecosystem integration that shaped the earlier app.

The mobile app is built from a single shared codebase in [composeApp](./composeApp), with native platform entry points for Android and iOS.

## Installation

### Android

Download the latest Android build from [GitHub Releases](https://github.com/RovoMedia/RovoMobile/releases/latest).

### iOS

- [TestFlight](https://testflight.apple.com/join/u4y7MHK9)

## Development

```bash
git clone https://github.com/RovoMedia/RovoMobile.git
cd RovoMobile
./scripts/run-mobile.sh android
# or
./scripts/run-mobile.sh ios
```

### Project Structure

- `composeApp/` contains the shared Kotlin Multiplatform and Compose Multiplatform app code.
- `composeApp/src/commonMain/` contains shared UI, features, repositories, and platform-agnostic logic.
- `composeApp/src/androidMain/` contains Android-specific integrations.
- `composeApp/src/iosMain/` contains iOS-specific integrations.
- `iosApp/` contains the native Xcode project and iOS entry point.

Useful commands:

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./scripts/build-distribution.sh
```

Versioning is driven from `iosApp/Configuration/Version.xcconfig`, which is used as the shared source of truth for both iOS and Android builds.

## Legal & DMCA

Rovo functions solely as a client-side interface for browsing metadata and playing media provided by user-installed extensions and/or user-provided sources. It is intended for content the user owns or is otherwise authorized to access.

Rovo is not affiliated with any third-party extensions, catalogs, sources, or content providers. It does not host, store, or distribute any media content.

For comprehensive legal information, including our full disclaimer, third-party extension policy, and DMCA/Copyright information, please visit our [Legal & Disclaimer Page](https://rovoapp.space/legal).

## Built With

- Kotlin Multiplatform
- Compose Multiplatform
- Kotlin
- AndroidX Media3
- AVFoundation and native iOS integrations

## Star History

<a href="https://www.star-history.com/#RovoMedia/RovoMobile&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=RovoMedia/RovoMobile&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=RovoMedia/RovoMobile&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=RovoMedia/RovoMobile&type=date&legend=top-left" />
 </picture>
</a>

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/RovoMedia/RovoMobile.svg?style=for-the-badge
[contributors-url]: https://github.com/RovoMedia/RovoMobile/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/RovoMedia/RovoMobile.svg?style=for-the-badge
[forks-url]: https://github.com/RovoMedia/RovoMobile/network/members
[stars-shield]: https://img.shields.io/github/stars/RovoMedia/RovoMobile.svg?style=for-the-badge
[stars-url]: https://github.com/RovoMedia/RovoMobile/stargazers
[issues-shield]: https://img.shields.io/github/issues/RovoMedia/RovoMobile.svg?style=for-the-badge
[issues-url]: https://github.com/RovoMedia/RovoMobile/issues
[license-shield]: https://img.shields.io/github/license/RovoMedia/RovoMobile.svg?style=for-the-badge
[license-url]: https://github.com/RovoMedia/RovoMobile/blob/main/LICENSE