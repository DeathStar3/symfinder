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

FROM python:3.8.2-alpine3.11
RUN apk add --no-cache bash py-pip && apk add --no-cache --virtual buildpacks python-dev libffi-dev openssl-dev gcc libc-dev make && pip install docker-compose && apk del buildpacks
ADD docker/runner/files/* /
ADD docker/* /docker/
CMD python3 run_xps.py
