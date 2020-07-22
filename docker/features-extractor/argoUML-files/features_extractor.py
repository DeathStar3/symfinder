import json
import os
import re
from sys import argv

from feature_mapper import Mapper, SourceFile, Method

features_lists_dir = argv[1]
symfinder_json_output = argv[2]

out_of_scope_packages = ["org.argouml.model.", "org.argouml.sequence2."]

source_files = []
methods = []

regex = re.compile(r"^(.*)(\(.*\)) (.*)$", re.MULTILINE)


def is_in_out_of_scope_package(line):
    return any(line.startswith(package) for package in out_of_scope_packages)


def add_source_file(entity_name, feature):
    existing_sourcefile = [sf for sf in source_files if sf.name == entity_name]
    if not existing_sourcefile:
        source_files.append(SourceFile(entity_name, [feature]))
    else:
        source_files[source_files.index(existing_sourcefile[0])].features.append(feature)


def add_method(method_name, parent, feature):
    existing_method = [m for m in methods if m.name == method_name]
    if not existing_method:
        methods.append(Method(method_name, parent, [feature]))
    else:
        methods[methods.index(existing_method[0])].features.append(feature)


for filename in os.listdir(features_lists_dir):
    if filename.endswith(".txt"):
        feature_name = filename.split(".txt")[0]
        with open(os.path.join(features_lists_dir, filename), "r") as fil:
            lines = [line.strip() for line in fil.readlines() if not is_in_out_of_scope_package(line)]
            for line in lines:
                line = regex.sub(r"\g<1>() \g<3>".strip(), line)
                line_items = line.split()
                if len(line_items) == 1:  # class level
                    add_source_file(line_items[0], feature_name)
                elif len(line_items) == 2:
                    if "Refinement" in line_items:  # class level refinement
                        class_name, _ = line_items
                        add_source_file(class_name, feature_name)
                    else:  # method
                        class_name, method_name = line_items
                        add_source_file(class_name, feature_name)
                else:  # len == 3, method refinement
                    class_name, method_name, _ = line_items
                    add_source_file(class_name, feature_name)

with open(symfinder_json_output, "r") as fil:
    symfinder_output = json.load(fil)

mapper = Mapper(source_files, methods, symfinder_output)
if "hotspots" in argv:
    mapper.make_mapping(hotspots=True)
else:
    mapper.make_mapping(hotspots=False)
mapper.write_traces_file()
