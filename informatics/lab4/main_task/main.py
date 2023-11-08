from json_parser import parse_json


def dump2xml(dict_, tag_name="root"):
    xml = ""
    for key, value in dict_.items():
        underscores_key = key.replace(" ", "_")
        open_tag = f"<{underscores_key}>"
        close_tag = f"</{underscores_key}>"

        if isinstance(value, dict):
            xml += dump2xml(value, key)
        elif isinstance(value, list):
            xml += (
                open_tag
                + "".join([dump2xml({key + "_elem": elem}, "") for elem in value])
                + close_tag
            )
        else:
            xml += open_tag + str(value) + close_tag

    if tag_name == "":
        return xml

    return f'<{tag_name.replace(" ", "_")}>' + xml + f'</{tag_name.replace(" ", "_")}>'


if __name__ == "__main__":
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/in.json", "r") as f:
        data = f.read()

    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/out.xml", "w") as f:
        print(dump2xml(parse_json(data)), file=f)
