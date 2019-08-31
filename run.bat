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

if not exist "resources\NUL" mkdir resources
if not exist "generated_visualizations\NUL" mkdir generated_visualizations

for /f "usebackq tokens=*" %%a in (`git rev-parse --verify HEAD`) do @set symfinder_version=%%a

docker run -it -v %cd%\experiments:/experiments -v %cd%\symfinder.yaml:/symfinder.yaml -v %cd%\resources:/resources -v %cd%\d3:/d3 -v %cd%\generated_visualizations:/generated_visualizations --user 1000:1000 -e SYMFINDER_VERSION=%symfinder_version%  -e SYMFINDER_PROJECTS="%*" --rm deathstar3/symfinder-fetcher:latest
rerun.bat %*