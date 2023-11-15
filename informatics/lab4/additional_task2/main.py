from json_parser_re import parse_json, dump2xml


if __name__ == "__main__":
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/in.json", "r") as f:
        data = f.read()

    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/out2.xml", "w") as f:
        header = '<?xml version="1.0" encoding="UTF-8" ?>'
        print(header + dump2xml(parse_json(data)), file=f)
