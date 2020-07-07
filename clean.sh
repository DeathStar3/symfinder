#!/bin/bash

rm -rf resources
rm -rf generated_visualizations
rm -rf target

docker rm $(docker stop $(docker ps -aq) &>/dev/null) &>/dev/null
docker system prune -f &>/dev/null

docker rmi symfinder-integration-tests:latest &>/dev/null
docker rmi deathstar3/symfinder-runner:local &>/dev/null
docker rmi deathstar3/symfinder-engine:local &>/dev/null
docker rmi deathstar3/symfinder-fetcher:local &>/dev/null
docker rmi test_projects_builder:latest &>/dev/null
