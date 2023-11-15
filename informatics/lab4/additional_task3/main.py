import sys

sys.path.append("..")
from main_task.json_parser import parse_json, dump2xml

if __name__ == "__main__":
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/in.json", "r") as f:
        data = f.read()

    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/out3.xml", "w") as f:
        header = '<?xml version="1.0" encoding="UTF-8" ?>'
        print(header + dump2xml(parse_json(data)), file=f)
