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

set -e

create_directory(){
    if [[ ! -d "$1" ]]; then
        echo "Creating $1 directory"
        mkdir "$1"
    else
        echo "$1 directory already exists"
    fi
}

ARGUMENT_LIST=(
    "local"
    "optimized"
)

SYMFINDER_PROJECTS=()

export TAG=splc2020
export SYMFINDER_COMPOSE_FILE="symfinder-compose.yaml"

create_directory resources
create_directory generated_visualizations

opts=$(getopt \
    --longoptions "$(printf "%s," "${ARGUMENT_LIST[@]}")" \
    --name "$(basename "$0")" \
    --options "" \
    -- "$@"
)

while [[ $# -gt 0 ]]; do
    case "$1" in
        --local)
            export TAG=local
            shift 1
            ;;

        --optimized)
            export SYMFINDER_COMPOSE_FILE="symfinder-compose-optimized.yaml"
            shift 1
            ;;

        *)
            SYMFINDER_PROJECTS+=("$1")
            break
            ;;
    esac
done

echo "Using $TAG images"

docker run -it -v $(pwd)/experiments:/experiments -v $(pwd)/symfinder.yaml:/symfinder.yaml -v $(pwd)/resources:/resources -v $(pwd)/d3:/d3 -v $(pwd)/generated_visualizations:/generated_visualizations --user $(id -u):$(id -g) -e SYMFINDER_VERSION=$(git rev-parse --short=0 HEAD) -e SYMFINDER_PROJECTS="${SYMFINDER_PROJECTS[@]}" --rm deathstar3/symfinder-fetcher:${TAG}

./rerun.sh "$SYMFINDER_PROJECTS"
