@echo off

rem This file is part of symfinder.
rem
rem symfinder is free software: you can redistribute it and/or modify
rem it under the terms of the GNU Lesser General Public License as published by
rem the Free Software Foundation, either version 3 of the License, or
rem (at your option) any later version.
rem
rem symfinder is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem GNU Lesser General Public License for more details.
rem
rem You should have received a copy of the GNU Lesser General Public License
rem along with symfinder.  If not, see <http://www.gnu.org/licenses/>.
rem
rem Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
rem Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
rem Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>


docker build -f docker/sources_fetcher/SourcesFetcherDockerfile -t symfinder-sources_fetcher .
docker-compose -f symfinder-compose.yaml build --build-arg BUILD_PARAMS="%*"
docker-compose -f runner-compose.yaml build
docker-compose -f visualization-compose.yaml build