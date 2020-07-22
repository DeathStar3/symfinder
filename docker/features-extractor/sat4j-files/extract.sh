#!/bin/sh

set -e

sh findfeatures.sh /project-sources/org.sat4j.core/src/main/java/

echo "Mapping on all vp-s"
python3 features_extractor.py features.md /generated_visualizations/data/${PROJECT_DIR}.json

echo "Mapping on hotspots only"
python3 features_extractor.py features.md /generated_visualizations/data/${PROJECT_DIR}.json hotspots

chown -R $SYMFINDER_UID:$SYMFINDER_GID /generated_visualizations