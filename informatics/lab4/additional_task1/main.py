from json import loads
from dicttoxml import dicttoxml


def json2xml(string):
    return dicttoxml(loads(string), attr_type=False, return_bytes=False)

if __name__ == "__main__":
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/in.json", "r") as f:
        data = f.read()

    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/out1.xml", "w") as f:
        print(json2xml(data), file=f)
