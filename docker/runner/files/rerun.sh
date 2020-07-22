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
# Copyright 2018-2020 Johann Mortara <johann.mortara@univ-cotedazur.fr>
# Copyright 2018-2020 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
# Copyright 2018-2020 Philippe Collet <philippe.collet@univ-cotedazur.fr>
#

set -e


create_directory(){
    if [[ ! -d "$1" ]]; then
        echo "Creating $1 directory"
        mkdir -p "$1"
    else
        echo "$1 directory already exists"
    fi
}

export COMPOSE_CONVERT_WINDOWS_PATHS=1
export SOURCES_PACKAGE="$1"
export GRAPH_OUTPUT_PATH="$2"
export PROJECT_NAME="$3"


echo "Cleaning previous execution..."
docker-compose -f symfinder-compose.yaml down
docker-compose -f symfinder-compose.yaml up --abort-on-container-exit
docker-compose -f symfinder-compose.yaml down

