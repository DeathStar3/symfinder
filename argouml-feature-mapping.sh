#!/bin/bash

PROJECT_DIR="argoUML-bcae373"

if [[ "$1" == "--local" ]]; then
    export TAG=local
    ./build.sh -DskipTests
else
    export TAG=jss
fi

./run.sh $@ argoUML

docker run -it -e "PROJECT_DIR=${PROJECT_DIR}" -e "SYMFINDER_UID=$(id -u)" -e "SYMFINDER_GID=$(id -g)" -v $(pwd)/resources/$PROJECT_DIR:/project-sources -v $(pwd)/generated_visualizations:/generated_visualizations --rm deathstar3/features-extractor-argouml:${TAG}
