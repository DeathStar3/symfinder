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

create_directory() {
    if [[ ! -d "$1" ]]; then
        echo "Creating $1 directory"
        mkdir "$1"
    else
        echo "$1 directory already exists"
    fi
}

sed -i -e 's/experimentsFile: experiments.yaml/experimentsFile: test-experiments.yaml/g' symfinder.yaml
create_directory resources
cp -r test_projects/java/* resources/
cp -r test_projects/cpp resources/cpp
cp -r test_projects/cpp/* resources/
cp -r test_projects/pom.xml resources/

docker build -f docker/integration_tests/compile/Dockerfile -t test_projects_builder .
docker run --rm --name test_projects_builder -v "$(pwd)/resources":/usr/src/mymaven -w /usr/src/mymaven test_projects_builder

./build.sh -DskipTests
./run.sh --local

docker-compose -f integration-tests-compose.yaml build
docker-compose -f integration-tests-compose.yaml up --abort-on-container-exit --exit-code-from integration
RETURN_CODE=$?
docker-compose -f integration-tests-compose.yaml down

sed -i -e 's/experimentsFile: test-experiments.yaml/experimentsFile: experiments.yaml/g' symfinder.yaml

exit $RETURN_CODE
