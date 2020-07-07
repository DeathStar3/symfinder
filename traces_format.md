# Features' traces format description

symfinder is able to automatically map the _vp_-s and variants it identified in the codebase with features' traces if they are available.

## Format

All the traces must be present in a single directory, whose path is specified using the `traces` argument when declaring a project to analyse.

Example:

```yaml
argoUML:
  repositoryUrl: https://github.com/marcusvnac/argouml-spl/
  sourcePackage: src/argouml-app
  commitIds:
    - bcae37
  traces: ArgoUML_groundTruth
```

Features' traces shall be separated in multiple files, one for each feature, and the name of the file corresponds to the name of the feature (_e.g._ `FEATURE_A.txt` contains all features for the `FEATURE_A` feature).

Traces may have multiple granularities, and should be declared this way:

- Class level trace (complete class or part of class): `<class_full_name>`
- Method level trace (complete method or part of method): `<class_full_name> <method_name_with_parameters>`

Examples:

- Class level trace: `org.argouml.uml.diagram.activity.ui.UMLActivityDiagram`
- Method level trace: `org.argouml.persistence.ModelMemberFilePersister registerDiagramsInternal(Project,Collection,boolean)`


