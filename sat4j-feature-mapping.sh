#!/bin/bash

PROJECT_DIR="sat4j-22374e5e"

if [[ "$1" == "--local" ]]; then
    export TAG=local
    ./build.sh -DskipTests
else
    export TAG=latest
fi

./run.sh $@ sat4j

docker run -it -e "PROJECT_DIR=${PROJECT_DIR}" -e "SYMFINDER_UID=$(id -u)" -e "SYMFINDER_GID=$(id -g)" -v $(pwd)/resources/$PROJECT_DIR:/project-sources -v $(pwd)/generated_visualizations:/generated_visualizations --rm deathstar3/features-extractor-sat4j:${TAG}
