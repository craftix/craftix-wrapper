# The Craftix Wrapper

The wrapper is a bootstrap, a java launcher that will launch the [Craftix server](https://github.com/craftix/craftix), and your launcher written in html/css.
It will also update the launcher using [S-Update](https://github.com/Litarvan/S-Update).

## Usage

The wrapper is automatically used by [the CLI](https://github.com/craftix/craftix-cli) when building, but to use it manually, just fill the craftix-wrapper.json (see [the doc](https://craftix.github.io/Manual_wrapper_setup)) file in src/main/resources, you can also add a icon.png file and change the splash.png.