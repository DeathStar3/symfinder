import json


class JSONSerializable:

    def __str__(self):
        return json.dumps(self.__dict__, cls=MyJSONEncoder, indent=2)

    def __repr__(self):
        return str(self)


class MyJSONEncoder(json.JSONEncoder):
    def default(self, o):
        return o.__dict__
