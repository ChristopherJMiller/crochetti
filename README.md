# Crochetti

An Android app for tracking crochet pattern progress. Follow row-by-row instructions and count stitches as you work through your projects.

## Features

- Create and manage crochet projects
- Row-by-row pattern instructions with stitch counter
- Built-in stitch glossary
- Custom stitch support
- Track progress across pattern components

## Development

### Using Nix (recommended)

```bash
nix develop
./gradlew assembleDebug
./gradlew installDebug
```

### Manual Setup

Requires JDK 17 and Android SDK (API 26+).

```bash
./gradlew assembleDebug     # Build debug APK
./gradlew installDebug      # Install on connected device
```
