{ pkgs ? import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz") {} }:
  let
    overrides = (builtins.fromTOML (builtins.readFile ./rust-toolchain.toml));
    
    buildToolsVersion = "34.0.0";
    cmakeVersion = "3.10.2";
    androidComposition = pkgs.androidenv.composeAndroidPackages {
      cmdLineToolsVersion = "8.0";
      toolsVersion = "26.1.1";
      platformToolsVersion = "35.0.1";
      buildToolsVersions = [ buildToolsVersion ];
      includeEmulator = true;
      emulatorVersion = "35.1.4";
      platformVersions = [ "34" ];
      includeSources = false;
      includeSystemImages = false;
      systemImageTypes = [ "google_apis_playstore" ];
      abiVersions = [ "armeabi-v7a" "arm64-v8a" "x86" "x86_64" ];
      cmakeVersions = [ cmakeVersion ];
      includeNDK = true;
      ndkVersions = ["26.3.11579264"];
      useGoogleAPIs = false;
      useGoogleTVAddOns = false;
      includeExtras = [
        "extras;google;gcm"
      ];
    };
in
  pkgs.mkShell rec {
    buildInputs = with pkgs; [ 
        rustup
        pkg-config
        openssl
        udev alsa-lib vulkan-loader
        xorg.libX11 xorg.libXcursor xorg.libXi xorg.libXrandr
        wayland
        libxkbcommon
        nodejs_22
        androidComposition.androidsdk
        libunwind
        jdk
    ];
    RUSTC_VERSION = overrides.toolchain.channel;

    LIBCLANG_PATH = pkgs.lib.makeLibraryPath [ 
      pkgs.llvmPackages_latest.libclang.lib 
    ];

    LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath buildInputs;

    ANDROID_HOME = "${androidComposition.androidsdk}/libexec/android-sdk";
    NDK_HOME = "${ANDROID_HOME}/ndk-bundle";
    GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${ANDROID_HOME}/build-tools/${buildToolsVersion}/aapt2";

    shellHook = ''
      export PATH=$PATH:''${CARGO_HOME:-~/.cargo}/bin
      export PATH=$PATH:''${RUSTUP_HOME:-~/.rustup}/toolchains/$RUSTC_VERSION-x86_64-unknown-linux-gnu/bin/
      export PATH="$(echo "$ANDROID_HOME/cmake/${cmakeVersion}".*/bin):$PATH"
      '';
    # Add precompiled library to rustc search path
    RUSTFLAGS = (builtins.map (a: ''-L ${a}/lib'') [
      # add libraries here (e.g. pkgs.libvmi)
    ]);
    # Add glibc, clang, glib, and other headers to bindgen search path
    BINDGEN_EXTRA_CLANG_ARGS =
    # Includes normal include path
    (builtins.map (a: ''-I"${a}/include"'') [
      # add dev libraries here (e.g. pkgs.libvmi.dev)
      pkgs.glibc.dev
    ])
    # Includes with special directory paths
    ++ [
      ''-I"${pkgs.llvmPackages_latest.libclang.lib}/lib/clang/${pkgs.llvmPackages_latest.libclang.version}/include"''
      ''-I"${pkgs.glib.dev}/include/glib-2.0"''
      ''-I${pkgs.glib.out}/lib/glib-2.0/include/''
    ];
  }