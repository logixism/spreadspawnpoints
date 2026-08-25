{
  description = "Java mod template";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        lib = nixpkgs.lib;
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfreePredicate =
            pkg:
            builtins.elem (lib.getName pkg) [
              # "vscode"
            ];
        };
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            #########################
            # Java Development Kits #
            #########################

            # jdk # latest LTS
            # jdk8
            # jdk11
            # jdk17
            # jdk21
            jdk25
            kotlin

            ###########
            # Editors #
            ###########

            # vscodium # https://wiki.nixos.org/wiki/VSCodium
            # vscode # https://wiki.nixos.org/wiki/Visual_Studio_Code
            # jetbrains.idea-community-bin # -bin to avoid bulding from source
            # jetbrains.idea-ultimate

            ###################
            # Editor features #
            ###################
            jdt-language-server
            python312
            # google-java-format
            llvmPackages_20.clang-tools
          ];

          #####################
          # Environment setup #
          #####################

          # nix-ld setup for Java VSCode extension
          # NIX_LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath [
          #   pkgs.stdenv.cc.cc
          #   pkgs.openssl
          # ];
          # NIX_LD = pkgs.lib.fileContents "${pkgs.stdenv.cc}/nix-support/dynamic-linker";

          JDK25 = pkgs.jdk25.home;
          JDK21 = pkgs.jdk21.home;

          shellHook =
            let
              libs = pkgs.lib.makeLibraryPath (
                with pkgs;
                [
                  libglvnd # needed for game to create window
                  vulkan-loader # Minecraft supports Vulkan since 26.2
                  flite # narrator, not needed, will produce a long stacktrace if missing

                  # audio
                  openal
                  alsa-lib
                  libjack2
                  libpulseaudio
                  pipewire
                ]
              );
            in
            ''
              export LD_LIBRARY_PATH="''${LD_LIBRARY_PATH}''${LD_LIBRARY_PATH:+:}${libs}"
            '';
        };
      }
    );
}
