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

SET COMPOSE_CONVERT_WINDOWS_PATHS=1
SET SYMFINDER_UID=1000
SET SYMFINDER_GID=1000
SET TAG=latest
SET SYMFINDER_PROJECTS=%*
set raw_path=%cd%
set after_slash=%raw_path:\=/%
SET PWD=%after_slash:C:=/c%

docker-compose -f runner-compose.yaml up
docker-compose -f runner-compose.yaml down