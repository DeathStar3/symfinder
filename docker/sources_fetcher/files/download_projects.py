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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with symfinder.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2018-2019 Johann Mortara <johann.mortara@univ-cotedazur.fr>
# Copyright 2018-2019 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
# Copyright 2018-2019 Philippe Collet <philippe.collet@univ-cotedazur.fr>
#

import os
import subprocess
import yaml


def download_project():
    subprocess.run(["./download_project.sh", "download", repository_url, project_directory])


def checkout_versions(id_type, *ids):
    subprocess.run(["./download_project.sh", id_type, project_directory, *ids])


def delete_project():
    subprocess.run(["./download_project.sh", "delete", project_directory])


with open('symfinder.yaml', 'r') as config_file:
    data = yaml.load(config_file.read(), Loader=yaml.FullLoader)
    with open("experiments/" + data["experimentsFile"], 'r') as experiments_file:
        experiments = yaml.load(experiments_file.read(), Loader=yaml.FullLoader)
        projects_to_analyse = os.getenv('SYMFINDER_PROJECTS')
        if projects_to_analyse:
            for p in projects_to_analyse.split(" "):
                if p not in experiments.keys():
                    print("WARNING: project {} does not exist".format(p))
        for xp_name, xp_config in experiments.items():
            if not projects_to_analyse or xp_name in projects_to_analyse.split(" "):
                projects_package = "resources"
                repository_url = xp_config["repositoryUrl"]
                project_directory = os.path.join(projects_package, xp_name)
                download_project()
                version_ids = []
                if "tagIds" in xp_config:
                    # cast to string in case of numerical tag id (e.g. 1.0)
                    checkout_versions("tag", *[str(id) for id in xp_config["tagIds"]])
                    version_ids = [str(id) for id in xp_config["tagIds"]]
                    checkout_versions("tag", *version_ids)
                if "commitIds" in xp_config:
                    version_ids = xp_config["commitIds"]
                    checkout_versions("commit", *version_ids)
                delete_project()
