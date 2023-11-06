from task2 import replace_time


def test_example():
    string = """Уважаемые студенты! В эту субботу в
15:00 планируется доп. занятие на 2
часа. То есть в 17:00:01 оно уже точно
кончится."""

    excepted_string = """Уважаемые студенты! В эту субботу в
(TBD) планируется доп. занятие на 2
часа. То есть в (TBD) оно уже точно
кончится."""

    assert replace_time(string) == excepted_string


def test_wrong_hours():
    string = "Current time: 1124:12 (24:12:12)"
    assert replace_time(string) == string


def test_wrong_minutes():
    string = "Current time: 22:99 (22:12:12)"
    assert replace_time(string) == "Current time: 22:99 ((TBD))"


def test_wrong_seconds():
    string = "Current time: 12:12:250"
    assert replace_time(string) == string


def test_wrong_format():
    string = "Current time: 22:22:12:12"
    assert replace_time(string) == string
