#!/bin/sh

set -e

echo "Mapping on all vp-s"
python3 features_extractor.py groundTruth /generated_visualizations/data/${PROJECT_DIR}.json

echo "Mapping on hotspots only"
python3 features_extractor.py groundTruth /generated_visualizations/data/${PROJECT_DIR}.json hotspots

chown -R $SYMFINDER_UID:$SYMFINDER_GID /generated_visualizations