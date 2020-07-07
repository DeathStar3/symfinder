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
ADD procedures/ /
RUN mvn --no-transfer-progress clean install $BUILD_PARAMS
RUN apk add --no-cache wget && \
    wget -q https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/4.0.0.15/apoc-4.0.0.15-all.jar && \
    apk del wget

FROM neo4j:4.0.3
COPY --from=building /target/original-symfinder-neo4j-procedures-1.0-SNAPSHOT.jar /plugins/procedures.jar
COPY --from=building /apoc-4.0.0.15-all.jar /plugins/apoc.jar