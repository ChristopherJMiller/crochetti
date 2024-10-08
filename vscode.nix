{ pkgs ? import (fetchTarball "https://github.com/NixOS/nixpkgs/archive/nixos-unstable.tar.gz") {} }:
  let
    overrides = (builtins.fromTOML (builtins.readFile ./rust-toolchain.toml));
in
  pkgs.mkShell rec {
    buildInputs = with pkgs; [ 
        rustup
        pkg-config
        openssl
        nodejs_22
        libunwind
        jdk
        gtk4
        webkitgtk_4_1
        typescript
        sqlite
    ];
    RUSTC_VERSION = overrides.toolchain.channel;

    LIBCLANG_PATH = pkgs.lib.makeLibraryPath [ 
      pkgs.llvmPackages_latest.libclang.lib 
    ];

    LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath buildInputs;

    shellHook = ''
      export PATH=$PATH:''${CARGO_HOME:-~/.cargo}/bin
      export PATH=$PATH:''${RUSTUP_HOME:-~/.rustup}/toolchains/$RUSTC_VERSION-x86_64-unknown-linux-gnu/bin/
      '';
    
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