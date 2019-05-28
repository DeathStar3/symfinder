# symfinder

## Technical Requirements

- Docker
    - Instruction to install Docker are available [here](https://docs.docker.com/install/#supported-platforms)
- Docker Compose
    - Instruction to install Docker Compose are available [here](https://docs.docker.com/compose/install/#install-compose)

## Setup and Running

### _symfinder_ configuration

The application's settings are set up using a YAML file, called `symfinder.yaml`, that must be at the root of the project.
Here is an example:

```yaml
neo4j:
  boltAddress: bolt://localhost:7687
  user: neo4j
  password: root

experiments_file: experiments.yaml
```

#### Neo4j parameters

- `boltAddress`: address where Neo4j's bolt driver is exposed
- `user`: username
- `password`: the password to access the database

#### Experiments

`experiments_file` corresponds to the path of a YAML file containing the description of the different source codes you want to analyse. Here is an example:

```yaml
junit:
  repositoryUrl: https://github.com/junit-team/junit4
  sourcePackage: .
  tagIds:
    - r4.12
javaGeom:
  repositoryUrl: https://github.com/dlegland/javaGeom
  sourcePackage: src
  commitIds:
    - 7e5ee60ea9febe2acbadb75557d9659d7fafdd28
```


You can specify as many experiments as you want.
Each project is defined by different parameters:
- `repositoryUrl`: URL of the project's Git repository
- `sourcePackage`: relative path of the package containing the sources of the project to analyse. `.` corresponds to the root of the project to be analysed.
- `commitIds`: IDs of the commits to checkout
- `tagsIds`: IDs of the tags to checkout

For an experiment, you can mix different commits and different tags to checkout. For example, we could have :

```yaml
junit:
  repositoryUrl: https://github.com/junit-team/junit4
  sourcePackage: .
  tagIds:
    - r4.12
    - r4.11
  commitIds:
    - c3715204786394f461d94953de9a66a4cec684e9
```

Each checkout of tag or commit ID `<id>` will be placed in a directory whose path is : `resources/<experimentName>-<id>`.
For example, the previous example will create the following tree:

```
resources/
├── junit-c3715204786394f461d94953de9a66a4cec684e9
├── junit-r4.11
└── junit-r4.12
```

### Building the project

Run

```bash
./symfinder.sh build
```

This script will build the integrality of the project (sources fetcher, _symfinder_ runner, _symfinder_ engine and visualization viewer).
If you want to build only a part of the _symfinder_ toolchain, you may add one or more of the following parameters:
- `sources-fetcher`: builds a Docker image running the _symfinder_ engine on each project 
- `runner`: builds a Docker image containing the scripts to clone Git repositories 
- `symfinder-engine`: builds the _symfinder_ engine and the corresponding Docker image
- `symfinder-engine_skip_tests`: builds the _symfinder_ engine without running tests and the corresponding Docker image
- `symfinder`: only rebuilds the _symfinder_ image, useful if you only applied changes to the `symfinder.yaml` file
- `visualization`: builds a Docker image starting a light web server to expose the generated visualization


### Running the project

To do this, run

```bash
./symfinder.sh run
```

This script will first execute a Python script to download the sources of the projects, then start a Docker Compose environment:
 - one container contains the Neo4j database;
 - another container contains the _symfinder_ engine to analyse them.
During the execution, the names of the parsed classes are output on the console.

The generated visualizations are placed in the `generated_visualizations` directory.

If you just want to rerun the analyses, run

```bash
./symfinder.sh rerun
```

This command will skip the download of sources and generation of HTML pages needed for visualization.

### Visualizing the generated graphs

Run

```bash
./symfinder.sh visualization
```

This will start a Docker container exposing the visualizations in a web server.
You will be able to access the generated visualizations at `http://localhost:8181`.