import unittest
from feature_mapper import Mapper, SourceFile, Method


class MyTestCase(unittest.TestCase):

    def test_one_true_positive(self):
        mapper = Mapper(
            [SourceFile("Class1", ["feature1"])],
            [],
            {"nodes": [{"name": "Class1", "types": ["VP"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_one_false_positive_one_false_negative(self):
        mapper = Mapper(
            [SourceFile("Class1", ["feature1"])],
            [],
            {"nodes": [{"name": "Class2", "types": ["VP"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(1, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_two_false_positives_one_false_negative(self):
        mapper = Mapper(
            [SourceFile("Class1", ["feature1"])],
            [],
            {"nodes": [{"name": "Class2", "types": ["VP"], "methods": [], "constructors": []},
                       {"name": "Class3", "types": ["VARIANT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(2, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_one_false_positive_two_false_negatives(self):
        mapper = Mapper(
            [SourceFile("Class1", ["feature1"]), SourceFile("Class2", ["feature2"])],
            [],
            {"nodes": [{"name": "Class3", "types": ["VARIANT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(1, result.get_false_positives())
        self.assertEqual(2, result.get_false_negatives())
        self.assertEqual(2, result.get_number_of_traces())
        self.check_sums(result)

    def test_multiple_traces_for_one_class(self):
        mapper = Mapper(
            [SourceFile("Class1", ["feature1", "feature2"])],
            [],
            {"nodes": [{"name": "Class1", "types": ["VARIANT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_method_trace_no_variant(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1"])],
            {"nodes": [{"name": "Class1", "types": [], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_two_method_traces_not_vp(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1", "feature2"])],
            {"nodes": [{"name": "Class1", "types": [], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_method_trace_one_variant(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1"])],
            {"nodes": [{"name": "Class1", "types": [], "methods": [{"name": "method1", "number": 1}], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_method_trace_vp(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1"])],
            {"nodes": [{"name": "Class1", "types": [], "methods": [{"name": "method1", "number": 2}], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_method_no_trace_but_vp(self):
        mapper = Mapper(
            [],
            [],
            {"nodes": [{"name": "Class1", "types": [], "methods": [{"name": "method1", "number": 2}], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(1, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(0, result.get_number_of_traces())
        self.check_sums(result)

    def test_method_with_two_traces_and_vp(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1", "feature2"])],
            {"nodes": [{"name": "Class1", "types": [], "methods": [{"name": "method1", "number": 2}], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_two_methods_with_traces_and_vp(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1"]), Method("method2", "Class1", ["feature2"])],
            {"nodes": [{"name": "Class1", "types": ["METHOD_LEVEL_VP"], "methods": [{"name": "method1", "number": 2}, {"name": "method2", "number": 3}], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(2, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(2, result.get_number_of_traces())
        self.check_sums(result)

    def test_two_methods_with_traces_and_but_homonym_in_another_class_is_vp(self):
        mapper = Mapper(
            [],
            [Method("method1", "Class1", ["feature1"]), Method("method2", "Class1", ["feature2"])],
            {"nodes": [{"name": "Class1", "types": ["METHOD_LEVEL_VP"], "methods": [{"name": "method1", "number": 2}], "constructors": []}, {"name": "Class2", "types": ["METHOD_LEVEL_VP"], "methods": [{"name": "method2", "number": 3}], "constructors": []}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(1, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(2, result.get_number_of_traces())
        self.check_sums(result)

    def test_constructor_true_positive(self):
        mapper = Mapper(
            [],
            [Method("Class1", "Class1", ["feature1"])],
            {"nodes": [{"name": "Class1", "types": ["METHOD_LEVEL_VP"], "methods": [], "constructors": [{"name": "Class1", "number": 2}]}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def test_constructor_false_positive(self):
        mapper = Mapper(
            [],
            [],
            {"nodes": [{"name": "Class1", "types": ["METHOD_LEVEL_VP"], "methods": [], "constructors": [{"name": "Class1", "number": 2}]}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(1, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(0, result.get_number_of_traces())
        self.check_sums(result)

    def test_constructor_false_negative(self):
        mapper = Mapper(
            [],
            [Method("Class1", "Class1", ["feature1"])],
            {"nodes": [{"name": "Class1", "types": [], "methods": [], "constructors": [{"name": "Class1", "number": 1}]}], "links": []})
        mapper.make_mapping_with_method_level()
        result = mapper.calculate_measures()
        self.assertEqual(0, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(1, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)

    def check_sums(self, result):
        # self.assertTrue(True)
        self.assertEqual(result.get_number_of_traces(), result.get_true_positives() + result.get_false_negatives())
        self.assertEqual(result.get_number_of_vps_and_vs(), result.get_true_positives() + result.get_false_positives())


if __name__ == '__main__':
    unittest.main()
