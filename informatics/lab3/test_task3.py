from task3 import delete_groupmates


def test_example():
    string = """Петров П.П. P0000
Анищенко А.А. P33113
Примеров Е.В. P0000
Иванов И.И. P0000"""
    group = "P0000"

    excepted_string = "Анищенко А.А. P33113\nПримеров Е.В. P0000"
    assert delete_groupmates(string, group) == excepted_string


def test_complex_surname():
    string = """Петров-Яковлев П.П. P1212
Анищенко-Рофлер А.А. P33113
Примеров Е.Е. P1212
Цаль И.И. P12342
Папизи И.Ф. P1212"""
    group = "P1212"

    excepted_string = "Анищенко-Рофлер А.А. P33113\nЦаль И.И. P12342\nПапизи И.Ф. P1212"
    assert delete_groupmates(string, group) == excepted_string


def test_group_numbers():
    string = """Петров-Яковлев П.П. P1212
Анищенко-Рофлер А.А. P12121
Примеров Е.Е. P1212
Цаль И.И. P12121
Папизи И.Ф. P1212"""
    group = "P1212"

    excepted_string = "Анищенко-Рофлер А.А. P12121\nЦаль И.И. P12121\nПапизи И.Ф. P1212"
    assert delete_groupmates(string, group) == excepted_string


def test_bad_format():
    string = """Петров-Яковлев П.П. P1212
Анищенко-Рофлер А.А. P12121
Примеров Е.Е. P1212 Папизи И.Ф. P1212"""
    group = "P1212"

    excepted_string =  "Анищенко-Рофлер А.А. P12121\nПримеров Е.Е. P1212 Папизи И.Ф. P1212"
    assert delete_groupmates(string, group) == excepted_string


def test_incorrect_initials():
    string = """Петров П.П.П P0000
Анищенко А.А.К P33113
Примеров Е.В. P0000
Иванов И.И. P0000"""
    group = "P0000"

    excepted_string = "Петров П.П.П P0000\nАнищенко А.А.К P33113\nПримеров Е.В. P0000"
    assert delete_groupmates(string, group) == excepted_string
