# Crochetti

## Getting Started

```sh

# Enter nix dev shell
# Allow unfree needed for Android Studio and accept android sdk licenses
NIXPKGS_ACCEPT_ANDROID_SDK_LICENSE=1 NIXPKGS_ALLOW_UNFREE=1 nix-shell

# Install npm packages
npm install

# Build!
npx tauri dev
npx tauri build
npx tauri android build
```
