from string import digits
from functools import reduce
from re import compile, DOTALL

number_regex = compile(r"(-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?)\s*(.*)", DOTALL)
string_regex = compile(
    r'("(?:[^\\]|\\["\\/bfnrt]|\\u[0-9a-fA-F]{4})*?")\s*(.*)', DOTALL
)


def __parse_number(string: str):
    match = number_regex.match(string.strip())
    if match is not None:
        number, string = match.groups()
        return eval(number), string.strip()


def __parse_string(string: str):
    match = string_regex.match(string.strip())
    if match is not None:
        res, string = match.groups()
        return res[1:-1], string.strip()


def __parse_bool(string: str):
    if string.startswith("true"):
        return True, string[4:].strip()
    if string.startswith("false"):
        return False, string[5:].strip()


def __parse_null(string: str):
    if string.startswith("null"):
        return None, string[4:].strip()


def __parse_colon(string: str):
    if string.startswith(":"):
        return ":", string[1:].strip()


def __parse_comma(string: str):
    if string.startswith(","):
        return ",", string[1:].strip()


def __parse_keyvalue(string: str):
    parsed_string = __parse_string(string)
    if parsed_string is None:
        return None
    parsed_colon = __parse_colon(parsed_string[1])
    if parsed_colon is None:
        return None
    parsed_value = __parse_value(parsed_colon[1])
    if parsed_value is None:
        return None
    return (parsed_string[0], parsed_value[0]), parsed_value[1].strip()


def __parse_comma_separated_values(string: str):
    res = []
    while True:
        parsed_value = __parse_value(string)
        if parsed_value is None:
            break
        res.append(parsed_value[0])
        string = parsed_value[1]

        parsed_comma = __parse_comma(string)
        if parsed_comma is None:
            break
        string = parsed_comma[1]

    if not res:
        return None

    return res, string.strip()


def __parse_comma_separated_keyvalues(string: str):
    res = {}
    while True:
        parsed_keyvalue = __parse_keyvalue(string)
        if parsed_keyvalue is None:
            break
        key, value = parsed_keyvalue[0]
        res[key] = value
        string = parsed_keyvalue[1]

        parsed_comma = __parse_comma(string)
        if parsed_comma is None:
            break
        string = parsed_comma[1]
    return res, string.strip()


def __parse_array(string: str):
    if not string.startswith("["):
        return None
    parsed_sepvalues = __parse_comma_separated_values(string[1:].strip())
    if parsed_sepvalues is not None:
        arr, string = parsed_sepvalues
    else:
        arr, string = [], string[1:]
    if not string.startswith("]"):
        return None
    return arr, string[1:].strip()


def __parse_object(string: str):
    if not string.startswith("{"):
        return None
    arr, string = __parse_comma_separated_keyvalues(string[1:].strip())
    if not string.startswith("}"):
        return None
    return arr, string[1:].strip()


def __parse_value(string: str):
    res = reduce(
        lambda f, g: f if f(string) else g,
        [
            __parse_number,
            __parse_string,
            __parse_bool,
            __parse_null,
            __parse_array,
            __parse_object,
        ],
    )(string)
    if res is None:
        return None
    else:
        return res[0], res[1].strip()


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


def parse_json(string: str):
    string = string.strip()
    parsed_value = __parse_value(string)
    if parsed_value is None:
        raise ValueError("not a valid JSON string")
    if parsed_value[1].strip():
        raise ValueError("not a valid JSON string")

    return parsed_value[0]
