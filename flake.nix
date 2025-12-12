{
  description = "Crochetti - Native Android crochet pattern tracker";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            allowUnfree = true;
            android_sdk.accept_license = true;
          };
        };

        buildToolsVersion = "34.0.0";

        androidComposition = pkgs.androidenv.composeAndroidPackages {
          cmdLineToolsVersion = "8.0";
          platformToolsVersion = "35.0.1";
          buildToolsVersions = [ buildToolsVersion "35.0.0" ];
          platformVersions = [ "34" "35" ];
          abiVersions = [ "arm64-v8a" "armeabi-v7a" "x86_64" ];
          includeEmulator = true;
          emulatorVersion = "35.1.4";
          includeNDK = false;
          includeSources = false;
          includeSystemImages = false;
        };

        androidSdk = androidComposition.androidsdk;

      in {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            # Android SDK
            androidSdk

            # JDK 17 (required for Android Gradle Plugin 8.x)
            jdk17

            # Gradle (optional - project uses wrapper)
            gradle

            # Kotlin compiler
            kotlin

            # Useful dev tools
            ktlint
            openssl
          ];

          ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
          ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
          JAVA_HOME = "${pkgs.jdk17}";

          GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/${buildToolsVersion}/aapt2";

          shellHook = ''
            export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$PATH"

            echo ""
            echo "Crochetti Android development environment"
            echo "Android SDK: $ANDROID_HOME"
            echo "Java: $JAVA_HOME"
            echo ""
            echo "Commands:"
            echo "  ./gradlew assembleDebug    - Build debug APK"
            echo "  ./gradlew installDebug     - Install on connected device"
            echo "  adb devices                - List connected devices"
            echo ""
          '';
        };
      }
    );
}
