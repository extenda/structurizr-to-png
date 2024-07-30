[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=extenda_structurizr-to-png&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=extenda_structurizr-to-png)

# structurizr-to-png

Create PNGs from [Structurizr DSL](https://github.com/structurizr/dsl#readme) files.

This project uses [PlantUML](https://plantuml.com) or [Graphviz](https://graphviz.org) to render Structurizr DSL to PNG
images with various rendering strategies. Image rendering is based on exports from the Structurizr CLI.

A default [`theme.json`](./src/main/resources/themes/theme.json) is used when no user provided theme is referenced. The
default theme maps tags to shapes and has some conventions around external systems.

## Features

  * Create PNGs from DSL files
  * Live preview of PNGs in your browser
  * Support for multiple renderers with `--render-with` flag
    * `c4plantuml` - [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML) (**default**)
    * `graphviz` - Dot with Graphviz
    * `structurizr` - Structurizr PlantUML
  * Control [appearance](#appearance) with themes and view properties

The [examples](#examples) section showcases the output from the tool.

# Usage

The project is intended to be used locally while authoring diagrams and in build pipelines workflows to automatically
update diagrams in the source repository and READMEs.

## :rocket: Helper scripts

The easiest way to get started with the project is to create a helper script. If using bash, create the following script
and name it `dsl2png.sh`.

```bash
#!/bin/sh
set -e
docker pull --quiet extenda/structurizr-to-png
exec docker run --rm -it -v "$(pwd)":/docs -p 3000:3000 extenda/structurizr-to-png "$@"
```

Or, if you're using Windows, create `dsl2png.cmd`.

```cmd
@echo off
docker pull --quiet extenda/structurizr-to-png
docker run --rm -it -v "%CD%":/docs -p 3000:3000 extenda/structurizr-to-png %*
```

With these scripts, users will be upgraded to the latest available version and also don't need to remember the lengthy
docker command. To watch a directory for changes to DSL files, simply run:

```bash
./dsl2png.sh --watch
```

## :whale: Docker

```bash
$ docker run --rm -it -v $(pwd):/docs extenda/structurizr-to-png
```

The convention is to generate diagrams to an `images/` directory inside the working directory. The default working
directory used in the container is `/docs`. The above command will render all `*.dsl` files in the current working
directory and it's subdirectories.

To render particular dsl files, use the `--path` option (glob is supported). Relative paths are treated from DSL file directory.

```bash
$ docker run --rm -it -v $(pwd):/docs extenda/structurizr-to-png --path workspace.ecd.dsl
```

To change the default output location, use the `--output` option. If specified as a relative path, it is resolved from
DSL file directory.

```bash
$ docker run --rm -it -v $(pwd):/docs extenda/structurizr-to-png --output c4-diagrams
```

The above example will write files to the `c4-diagrams` directory in the current working directory.

### Live preview in browser

To use the live preview functionality, we must add the `--watch` flag and a port binding.

```bash
$ docker run --rm -it -v $(pwd):/docs -p 3000:3000 extenda/structurizr-to-png --watch
```

Open https://localhost:3000 in your browser to see the preview images. The images will update when the DSL is changed.

# Examples

The examples are created from [demo.dsl](demo.dsl).

### System context

<details open>
<summary><b>C4PlantUML</b></summary>

![System context](images/c4plantuml/structurizr-PriceTracker-SystemContext.png)
</details>

<details>
<summary><b>Graphviz</b></summary>

![System context](images/graphviz/structurizr-PriceTracker-SystemContext.png)
</details>

<details>
<summary><b>Structurizr</b></summary>

![System context](images/structurizr/structurizr-PriceTracker-SystemContext.png)
</details>

### Containers

<details open>
<summary><b>C4PlantUML</b></summary>

![Container view](images/c4plantuml/structurizr-PriceTracker-Container.png)
</details>

<details>
<summary><b>Graphviz</b></summary>

![Container view](images/graphviz/structurizr-PriceTracker-Container.png)
</details>

<details>
<summary><b>Structurizr</b></summary>

![Container view](images/structurizr/structurizr-PriceTracker-Container.png)
</details>

# Appearance

All renderers support Structurizr themes. The theme must be reachable on a public URL. If no theme is specified
the [dsl-to-png theme](./src/main/resources/themes/theme.json) will be used.

Renderers may also support output options, typically controlled with view properties.

## C4PlantUML options

The following view properties are supported with the `c4plantuml` renderer.

| Property            | Default Value | Description                      |
|---------------------|---------------|----------------------------------|
| `c4plantuml.legend` | `true`        | Include a dynamic diagram legend |
| `plantuml.title`    | `true`        | Include the diagram title        |

Here's an example on how to use them to disable the diagram legend and the title.

```
views {
  container system {
    include *
    properties {
      c4plantuml.legend false
      plantuml.title false
    }
  }
}
```

## Structurizr options

The following view properties are supported with the `structurizr` renderer.

| Property          | Default Value | Description                 |
|-------------------|---------------|-----------------------------|
| `plantuml.shadow` | `false`       | Render elements with shadow |
| `plantuml.title`  | `true`        | Include the diagram title   |

# Development

  * The project is built with Java 21 and Maven.
    * `mvn verify` to build and run all tests
    * `java -jar target/structurizr-to-png.jar` runs the client on your local machine
  * Running locally requires Graphviz
  * Make sure to install [pre-commit](https://pre-commit.com) hooks and use them
  ```bash
  pre-commit install -t pre-commit -t commit-msg
  ```
  * Commit messages must follow [conventional commits](https://conventionalcommits.org)
  * [SonarCloud](https://sonarcloud.io/dashboard?id=extenda_structurizr-to-png)
    quality gates must pass for all pull requests

# :balance_scale: License

This project is licensed under the [MIT license](LICENSE).
