import unittest
from feature_mapper import Mapper, SourceFile, Method


class MyTestCase(unittest.TestCase):

    def test_one_true_positive(self):
        mapper = Mapper(
            [SourceFile("Class1", ["feature1"])],
            [],
            {"nodes": [{"name": "Class1", "types": ["VP", "HOTSPOT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping()
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
            {"nodes": [{"name": "Class2", "types": ["VP", "HOTSPOT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping()
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
            {"nodes": [{"name": "Class2", "types": ["VP", "HOTSPOT"], "methods": [], "constructors": []},
                       {"name": "Class3", "types": ["VARIANT", "HOTSPOT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping()
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
            {"nodes": [{"name": "Class3", "types": ["VARIANT", "HOTSPOT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping()
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
            {"nodes": [{"name": "Class1", "types": ["VARIANT", "HOTSPOT"], "methods": [], "constructors": []}], "links": []})
        mapper.make_mapping()
        result = mapper.calculate_measures()
        self.assertEqual(1, result.get_true_positives())
        self.assertEqual(0, result.get_false_positives())
        self.assertEqual(0, result.get_false_negatives())
        self.assertEqual(1, result.get_number_of_traces())
        self.check_sums(result)


    def check_sums(self, result):
        # self.assertTrue(True)
        self.assertEqual(result.get_number_of_traces(), result.get_true_positives() + result.get_false_negatives())
        self.assertEqual(result.get_number_of_vps_and_vs(), result.get_true_positives() + result.get_false_positives())


if __name__ == '__main__':
    unittest.main()
