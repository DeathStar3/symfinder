#!/bin/sh

set -e

python3 features_extractor.py /traces ${GRAPH_OUTPUT_PATH}

chown -R $SYMFINDER_UID:$SYMFINDER_GID /generated_visualizations
