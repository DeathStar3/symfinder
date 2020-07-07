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

import os
import yaml


def run_project():
    sources_package = os.path.join(xp_codename, xp_config["sourcePackage"])
    xp_language = str(xp_config.get("language", "java"))
    graph_output_path = "generated_visualizations/data/{}.json".format(xp_codename)
    jvm_args = str(xp_config.get("jvmArguments", ""))
    os.system(
        "bash rerun.sh {} {} {} {} {}".format(sources_package, graph_output_path, xp_codename, xp_language, jvm_args))
    if traces_dir:
        os.system("bash run_mapper.sh {} {}".format(graph_output_path, traces_dir))

with open('symfinder.yaml', 'r') as config_file:
    data = yaml.load(config_file.read(), Loader=yaml.FullLoader)
    with open("experiments/" + data["experimentsFile"], 'r') as experiments_file:
        experiments = yaml.load(experiments_file.read(), Loader=yaml.FullLoader)
        projects_to_analyse = os.getenv('SYMFINDER_PROJECTS')
        for xp_name, xp_config in experiments.items():
            traces_dir = str(xp_config.get("traces", ""))
            if not projects_to_analyse or xp_name in projects_to_analyse.split(" "):
                if "repositoryUrl" not in xp_config:
                    xp_codename = xp_name
                    run_project()
                for id in xp_config.get("tagIds", []) + xp_config.get("commitIds", []):
                    xp_codename = xp_name + "-" + str(id).replace("/", "_")
                    run_project()

