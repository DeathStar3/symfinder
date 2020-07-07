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

FROM maven:3.6.1-jdk-13-alpine as building
ARG BUILD_PARAMS
ARG SOURCES_PACKAGE
ARG GRAPH_OUTPUT_PATH
ARG SYMFINDER_LANGUAGE
ARG JVM_ARGUMENTS
ADD src/ /src
ADD pom.xml /pom.xml
RUN mvn --no-transfer-progress clean install $BUILD_PARAMS

FROM openjdk:14-jdk-alpine3.10
RUN apk add --no-cache git g++
COPY --from=building target/symfinder-1.0-SNAPSHOT-jar-with-dependencies.jar /symfinder.jar
ADD docker/symfinder/exec_symfinder.sh /exec_symfinder.sh
ENTRYPOINT sh exec_symfinder.sh $SOURCES_PACKAGE $GRAPH_OUTPUT_PATH $SYMFINDER_LANGUAGE $JVM_ARGUMENTS