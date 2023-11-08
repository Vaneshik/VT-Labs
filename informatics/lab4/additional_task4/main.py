import sys
sys.path.append("..")

import time
from main_task.json_parser import parse_json as parse_json_for
from additional_task1.main import json2xml
from additional_task2.json_parser_re import parse_json as parse_json_regex


def test(f, string):
    start_time = time.time()
    for _ in range(100):
        f(string)
    return time.time() - start_time


if __name__ == "__main__":
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/in.json", "r") as f:
        data = f.read()

    x1, x2, x3 = 0, 0, 0
    for _ in range(3):
        x1 += test(parse_json_for, data)
        x2 += test(json2xml, data)
        x3 += test(parse_json_regex, data)

    print(
        f"Own Parser: {round(x1 / 3, 6)} seconds avg.",
        f"Own Parser with Regex: {round(x3 / 3, 6)} seconds avg.",
        f"Python JSON Lib + dicttoxml: {round(x2 / 3, 6)} seconds avg.",
        sep="\n",
    )
