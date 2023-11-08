import sys

sys.path.append("..")
from main_task.json_parser import parse_json


def gen_tsv(dict_):
    tsv = "id\ttitle\tgroup\tteacher/id\tteacher/name\tclass_type/id\tclass_type/name\tclass_format/id\tclass_format/name\tdate/day\tdate/weeks/0\tdate/weeks/1\tdate/weeks/2\tdate/weeks/3\tdate/weeks/4\tdate/weeks/5\tdate/weeks/6\tdate/weeks/7\ttime/class_number\ttime/from\ttime/to\tclassroom/id\tclassroom/name\tcampus/id\tcampus/adress\n"
    for elem in dict_["schedule"]:
        x = []
        x.append(elem["id"])
        x.append(elem["title"])
        x.append(elem["group"])
        x.append(elem["teacher"]["id"])
        x.append(elem["teacher"]["name"])
        x.append(elem["class_type"]["id"])
        x.append(elem["class_type"]["name"])
        x.append(elem["class_format"]["id"])
        x.append(elem["class_format"]["name"])
        x.append(elem["date"]["day"])

        weeks = elem["date"]["weeks"]
        for week_num in weeks:
            x.append(week_num)
        for _ in range(8 - len(weeks)):
            x.append("")

        x.append(elem["time"]["class_number"])
        x.append(elem["time"]["from"])
        x.append(elem["time"]["to"])
        x.append(elem["classroom"]["id"])
        x.append(elem["classroom"]["name"])
        x.append(elem["campus"]["id"])
        x.append(elem["campus"]["adress"])

        x = "\t".join(list(map(str, x)))
        tsv += x + "\n"

    return tsv


if __name__ == "__main__":
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/in.json", "r") as f:
        data = f.read()
    with open("/home/vaneshik/VT-Labs/informatics/lab4/data/out5.tsv", "w") as f:
        print(gen_tsv(parse_json(data)), file=f)
