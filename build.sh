#!/bin/bash
#
# This file is part of symfinder.
#
# symfinder is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# symfinder is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with symfinder. If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
# Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
# Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
#

docker build -f docker/sources_fetcher/Dockerfile -t deathstar3/symfinder-fetcher:local .
docker build -f docker/neo4j/Dockerfile -t deathstar3/symfinder-neo4j:local --build-arg BUILD_PARAMS="$@" .
docker build -f docker/symfinder/Dockerfile -t deathstar3/symfinder-engine:local --build-arg BUILD_PARAMS="$@" .
docker build -f docker/runner/Dockerfile -t deathstar3/symfinder-runner:local .
docker build -f docker/features-extractor/Dockerfile -t deathstar3/features-extractor:local .