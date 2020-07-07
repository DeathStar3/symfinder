import json
import os

from utils import JSONSerializable


class Asset(JSONSerializable):

    def __init__(self, name, features=[]):
        self.name = name
        self.features = features
        self.vp = False
        self.variant = False

    def is_vp_or_variant(self):
        return self.is_vp() or self.is_variant()

    def is_vp(self):
        return self.vp

    def is_variant(self):
        return self.variant


class SourceFile(Asset):

    def __init__(self, name, features=[]):
        super().__init__(name, features)

    def __eq__(self, other):
        return isinstance(other, SourceFile) and self.name == other.name


class Method(Asset):

    def __init__(self, name, parent_class, features=[]):
        super().__init__(name, features)
        self.parent_class = parent_class

    def __eq__(self, other):
        return isinstance(other, Method) and self.name == other.name and self.parent_class == other.parent_class


class JSONFilter:

    def __init__(self, symfinder_json_content, selected_classes):
        self.symfinder_json_content = symfinder_json_content
        self.selected_classes = selected_classes

    def get_selected_classes_variants(self):
        new_classes = []
        for clazz in self.selected_classes:
            new_classes += self.get_children(clazz, [])
        return new_classes

    def get_children(self, root_node, nodes_list):
        if root_node in [n["name"] for n in self.symfinder_json_content["nodes"]]:
            nodes_list.append(root_node)
            for node in [l["target"] for l in self.symfinder_json_content["links"] if l["source"] == root_node]:
                self.get_children(node, nodes_list)
        return nodes_list

    def get_filtered_json(self):
        all_selected_classes_ = set(self.selected_classes + self.get_selected_classes_variants())
        filtered_json = {"nodes": [], "links": self.symfinder_json_content["links"]}
        for node in self.symfinder_json_content["nodes"]:
            if node["name"] in all_selected_classes_:
                filtered_json["nodes"].append(node)
        return filtered_json


class Mapper:

    def __init__(self, classes_with_feature, methods_with_feature, json_output):
        self.classes_list = classes_with_feature.copy()
        self.methods_list = methods_with_feature.copy()
        self.json_output = json_output.copy()

    def make_mapping(self):
        for node in self.json_output["nodes"]:
            self.map_class(node)
        print(self.calculate_measures())

    def make_mapping_with_method_level(self):
        for node in self.json_output["nodes"]:
            node_name = self.map_class_with_method_level(node)
            if node["constructors"]:
                constructor = node["constructors"][0]
                self.map_method(constructor, node_name)
            if node["methods"]:
                for method in node["methods"]:
                    self.map_method(method, node_name)

    def map_class(self, class_object):
        node_name = class_object["name"]
        new_file = SourceFile(node_name)
        if new_file not in self.classes_list:
            self.classes_list.append(new_file)
        source_file = self.classes_list[self.classes_list.index(new_file)]
        source_file.vp = "VP" in class_object["types"] or "METHOD_LEVEL_VP" in class_object["types"]
        source_file.variant = "VARIANT" in class_object["types"]
        return node_name

    def map_class_with_method_level(self, class_object):
        node_name = class_object["name"]
        new_file = SourceFile(node_name)
        if new_file not in self.classes_list:
            self.classes_list.append(new_file)
        source_file = self.classes_list[self.classes_list.index(new_file)]
        source_file.vp = "VP" in class_object["types"]
        source_file.variant = "VARIANT" in class_object["types"]
        return node_name

    def map_method(self, method, parent_name):
        if method["number"] > 1:
            new_method = Method(method["name"], parent_name)
            if new_method not in self.methods_list:
                self.methods_list.append(new_method)
            self.methods_list[self.methods_list.index(new_method)].vp = True

    def calculate_measures(self):
        return MappingResults(self.classes_list + self.methods_list)

    def write_traces_file(self):
        traces_dict = {f.name: list(set(f.features)) for f in self.classes_list}
        with open(os.environ['GRAPH_OUTPUT_PATH'].split(".json")[0] + "-traces.json", 'w') as fil:
            fil.write(json.dumps(traces_dict))

class MappingResults:

    def __init__(self, assets):
        self.assets = assets.copy()

    # Number of traces
    def get_number_of_traces(self):
        return len([a for a in self.assets if a.features])

    # Number of traces
    def get_number_of_vps_and_vs(self):
        return len([a for a in self.assets if a.is_vp_or_variant()])

    # Number of traces feature <--> asset where the asset is a VP or variant
    def get_true_positives(self):
        return len([a for a in self.assets if a.features and a.is_vp_or_variant()])

    # Number of assets being a VP or variant but not linked to any feature
    def get_false_positives(self):
        return len([a for a in self.assets if not a.features and a.is_vp_or_variant()])

    # Number of traces feature <--> asset where the asset is not a VP nor variant
    def get_false_negatives(self):
        return len([a for a in self.assets if a.features and not a.is_vp_or_variant()])

    def get_precision(self):
        tp = self.get_true_positives()
        fp = self.get_false_positives()
        return tp / (tp + fp)

    def get_recall(self):
        tp = self.get_true_positives()
        fn = self.get_false_negatives()
        return tp / (tp + fn)

    def __str__(self):
        return """Number of VPs and variants linked to features (TP): %s
Number of VPs and variants not linked to features (FP): %s
Number of features traces not linked to any VP nor variant (FN): %s
Number of traces (TP + FN): %s
Number of VPs / variants (TP + FP): %s
Precision = TP / (TP + FP): %s
Recall = TP / (TP + FN): %s""" % (self.get_true_positives(), self.get_false_positives(), self.get_false_negatives(),
                                  self.get_number_of_traces(), self.get_number_of_vps_and_vs(),
                                  self.get_precision(), self.get_recall())


def print_stat(message, value):
    print("%s: %s" % (message, str(value)))
