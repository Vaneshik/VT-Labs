from task1 import count_smiles


def test_count_smiles1():
    string = "=-{wefwfe=-{wefwef=-=-{)=-{)=-{)=-{)koi"
    assert count_smiles(string) == 4


def test_count_smiles2():
    string = "===----{{{{))))"
    assert count_smiles(string) == 0


def test_count_smiles3():
    string = "Hello! My face looks like =-{)"
    assert count_smiles(string) == 1


def test_count_smiles4():
    string = "brrrr = - { )"
    assert count_smiles(string) == 0


def test_count_smiles5():
    string = "=-{))))"
    assert count_smiles(string) == 1
